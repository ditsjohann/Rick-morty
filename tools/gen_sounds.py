"""Synthesises every sound effect (and the music disc) from scratch, then encodes to OGG Vorbis via ffmpeg.

Run from the repo root:  python3 tools/gen_sounds.py
"""
import math
import os
import subprocess
import tempfile
import wave

import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'src/main/resources/assets/mayhem/sounds')
SR = 22050
rng = np.random.default_rng(1337)


def t_axis(dur):
    return np.arange(int(dur * SR)) / SR


def osc(freq, dur, kind='sine', phase=0.0):
    """freq may be a scalar or an array (per-sample frequency glide)."""
    n = int(dur * SR)
    f = np.full(n, freq, dtype=np.float64) if np.isscalar(freq) else np.asarray(freq, dtype=np.float64)[:n]
    ph = np.cumsum(f) / SR + phase
    x = ph % 1.0
    if kind == 'sine':
        return np.sin(2 * np.pi * ph)
    if kind == 'square':
        return np.where(x < 0.5, 1.0, -1.0)
    if kind == 'pulse25':
        return np.where(x < 0.25, 1.0, -1.0)
    if kind == 'saw':
        return 2 * x - 1
    if kind == 'tri':
        return 2 * np.abs(2 * x - 1) - 1
    raise ValueError(kind)


def glide(f0, f1, dur, curve=1.0):
    t = np.linspace(0, 1, int(dur * SR)) ** curve
    return f0 * (f1 / f0) ** t


def env(dur, a=0.01, d=0.1, s=0.7, r=0.2):
    """ADSR envelope; segments are squeezed to fit inside dur."""
    n = int(dur * SR)
    ai, di, ri = int(a * SR), int(d * SR), int(r * SR)
    total = ai + di + ri
    if total > n and total > 0:
        k = n / total
        ai, di, ri = int(ai * k), int(di * k), int(ri * k)
    si = max(0, n - ai - di - ri)
    parts = [np.linspace(0, 1, ai, endpoint=False), np.linspace(1, s, di, endpoint=False), np.full(si, s), np.linspace(s, 0, n - ai - di - si)]
    return np.concatenate(parts)[:n]


def perc(dur, decay=8.0):
    t = t_axis(dur)
    return np.exp(-t * decay)


def noise(dur):
    return rng.uniform(-1, 1, int(dur * SR))


def lowpass(x, cutoff):
    """One-pole lowpass; cutoff scalar or array."""
    c = np.full(len(x), cutoff) if np.isscalar(cutoff) else cutoff
    a = 1 - np.exp(-2 * np.pi * c / SR)
    y = np.zeros_like(x)
    acc = 0.0
    for i in range(len(x)):
        acc += a[i] * (x[i] - acc)
        y[i] = acc
    return y


def highpass(x, cutoff):
    return x - lowpass(x, cutoff)


def bandpass(x, f, q=5.0):
    """Biquad bandpass (constant peak gain)."""
    w0 = 2 * np.pi * f / SR
    alpha = np.sin(w0) / (2 * q)
    b0, b1, b2 = alpha, 0.0, -alpha
    a0, a1, a2 = 1 + alpha, -2 * np.cos(w0), 1 - alpha
    b0, b1, b2, a1, a2 = b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0
    y = np.zeros_like(x)
    x1 = x2 = y1 = y2 = 0.0
    for i in range(len(x)):
        xi = x[i]
        yi = b0 * xi + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2, x1, y2, y1 = x1, xi, y1, yi
        y[i] = yi
    return y


def crush(x, bits=6):
    q = 2 ** bits
    return np.round(x * q) / q


def fit(*parts):
    n = max(len(p) for p in parts)
    out = np.zeros(n)
    for p in parts:
        out[:len(p)] += p
    return out


def concat(*parts, gap=0.0):
    g = np.zeros(int(gap * SR))
    out = []
    for p in parts:
        out.append(p)
        out.append(g)
    return np.concatenate(out)


def delay(x, secs, fb=0.35, mixv=0.35, reps=4):
    d = int(secs * SR)
    out = np.concatenate([x, np.zeros(d * reps)])
    for k in range(1, reps + 1):
        out[d * k:d * k + len(x)] += x * (fb ** k) * mixv / fb
    return out


def norm(x, peak=0.9):
    m = np.max(np.abs(x)) or 1.0
    return x / m * peak


def fade(x, ms=8):
    n = min(len(x) // 2, int(ms / 1000 * SR))
    if n > 0:
        x[:n] *= np.linspace(0, 1, n)
        x[-n:] *= np.linspace(1, 0, n)
    return x


def write(name, x, peak=0.85):
    x = fade(norm(x, peak))
    os.makedirs(OUT, exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as tmp:
        path = tmp.name
    with wave.open(path, 'wb') as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(SR)
        wf.writeframes((np.clip(x, -1, 1) * 32767).astype('<i2').tobytes())
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', path, '-c:a', 'libvorbis', '-q:a', '4', os.path.join(OUT, name + '.ogg')], check=True)
    os.unlink(path)


# ----------------------------------------------------------------- voice-ish helpers
def syllable(f0, dur, formants, kind='saw', vib=6.0, vib_depth=0.03):
    t = t_axis(dur)
    f = f0 * (1 + vib_depth * np.sin(2 * np.pi * vib * t))
    src = osc(f, dur, kind)
    y = sum(bandpass(src, fm, 6.0) * g for fm, g in formants)
    return y * env(dur, 0.01, 0.05, 0.8, dur * 0.4)


VOWELS = {'a': [(800, 1.0), (1200, 0.6)], 'e': [(400, 1.0), (2200, 0.5)], 'i': [(300, 1.0), (2600, 0.5)],
          'o': [(500, 1.0), (900, 0.6)], 'u': [(350, 1.0), (700, 0.6)]}


def babble(pitches, vowels, durs, kind='saw', vib=6.0):
    return concat(*[syllable(p, d, VOWELS[v], kind, vib) for p, v, d in zip(pitches, vowels, durs)], gap=0.02)


# ----------------------------------------------------------------- effects
def gen_effects():
    # PORTAL GUN FIRE: descending zappy FM + wet splat
    d = 0.45
    f = glide(1800, 220, d, 0.6)
    mod = osc(f * 2.01, d)
    zap = np.sin(2 * np.pi * np.cumsum(f * (1 + 0.6 * mod)) / SR) * perc(d, 7)
    splat = lowpass(noise(0.25), 1800) * perc(0.25, 14)
    write('portal_gun_fire', fit(zap, splat * 0.6))

    write('portal_gun_empty', fit(osc(1400, 0.06, 'square') * perc(0.06, 50), np.concatenate([np.zeros(int(0.09 * SR)), osc(900, 0.06, 'square') * perc(0.06, 50)])))
    write('portal_gun_mode', concat(osc(880, 0.06, 'pulse25') * env(0.06, 0.002, 0.02, 0.6, 0.02), osc(1320, 0.08, 'pulse25') * env(0.08, 0.002, 0.02, 0.6, 0.04), gap=0.015))

    # PORTAL OPEN: rising whoosh + shimmering chord
    d = 1.2
    wh = bandpass(noise(d), 600, 1.2)
    wh = lowpass(noise(d), glide(200, 4000, d, 0.5)) * env(d, 0.4, 0.2, 0.6, 0.5)
    chord = sum(osc(glide(f0 * 0.5, f0, d, 0.3), d, 'tri') for f0 in (330, 415, 494, 660)) * env(d, 0.3, 0.3, 0.5, 0.5) * 0.25
    wob = osc(glide(60, 110, d), d) * env(d, 0.05, 0.3, 0.6, 0.6) * (0.6 + 0.4 * osc(7, d))
    write('portal_open', fit(wh * 0.7, chord, wob * 0.8))

    d = 0.9
    whc = lowpass(noise(d), glide(4000, 150, d, 0.4)) * env(d, 0.02, 0.2, 0.6, 0.5)
    slurp = osc(glide(500, 60, d, 0.5), d) * perc(d, 4)
    write('portal_close', fit(whc * 0.7, slurp))

    d = 0.7
    tr = lowpass(noise(d), glide(300, 3000, d, 0.3)) * env(d, 0.05, 0.2, 0.5, 0.4)
    fl = osc(glide(200, 1200, d, 2), d, 'sine') * env(d, 0.05, 0.1, 0.6, 0.4) * (0.6 + 0.4 * osc(18, d))
    write('portal_travel', fit(tr, fl * 0.6))

    d = 0.35
    pew = osc(glide(2400, 300, d, 0.4), d, 'square') * perc(d, 9)
    write('plasma_shot', fit(lowpass(pew, 5000) * 0.8, highpass(noise(0.1), 3000) * perc(0.1, 30) * 0.5))

    d = 0.7
    f = 600 + 400 * np.sin(2 * np.pi * 9 * t_axis(d))
    ray = osc(f, d, 'tri') * env(d, 0.01, 0.1, 0.7, 0.3) + osc(f * 1.5, d, 'sine') * 0.4 * env(d, 0.01, 0.1, 0.6, 0.3)
    write('size_ray', crush(ray, 5))

    d = 0.6
    sw = osc(glide(300, 1500, d / 2, 1), d / 2, 'saw')
    sw2 = osc(glide(1500, 300, d / 2, 1), d / 2, 'saw')
    write('swapper', lowpass(np.concatenate([sw, sw2]), 3000) * env(d, 0.01, 0.1, 0.8, 0.2))

    d = 1.6
    hum = (osc(48, d, 'saw') * 0.6 + osc(72.5, d, 'sine') + osc(96, d, 'tri') * 0.4) * (0.7 + 0.3 * osc(3.2, d))
    write('black_hole_hum', lowpass(hum, 400) * env(d, 0.2, 0.2, 0.9, 0.4))
    d = 2.2
    boom = lowpass(noise(d), glide(3000, 60, d, 0.3)) * perc(d, 2.2) + osc(glide(120, 30, d), d) * perc(d, 2.5)
    suck = osc(glide(80, 900, 0.35, 2), 0.35, 'saw') * env(0.35, 0.3, 0.02, 0.5, 0.03) * 0.5
    write('black_hole_collapse', np.concatenate([lowpass(suck, 2000), boom]))

    d = 0.4
    pop = osc(glide(300, 900, 0.05), 0.05) * perc(0.05, 40)
    fizz = highpass(noise(d), 2500) * perc(d, 6) * 0.4
    write('jar_open', fit(pop, np.concatenate([np.zeros(int(0.04 * SR)), fizz])))

    d = 0.5
    stat = highpass(noise(d), 1500) * env(d, 0.005, 0.05, 0.5, 0.2) * 0.6
    blip = np.concatenate([np.zeros(int(0.25 * SR)), osc(1000, 0.15, 'sine') * env(0.15, 0.005, 0.02, 0.5, 0.05)])
    write('tv_channel', fit(stat, blip))


def gen_mobs():
    # GLOOPER: bubbly wobbles
    def bloop(f0, f1, d):
        return osc(glide(f0, f1, d, 0.7), d) * env(d, 0.01, 0.05, 0.6, d * 0.5) * (0.7 + 0.3 * osc(25, d))
    write('glooper_ambient1', concat(bloop(220, 440, 0.12), bloop(300, 600, 0.1), bloop(260, 520, 0.15), gap=0.03))
    write('glooper_ambient2', concat(bloop(400, 200, 0.18), bloop(350, 700, 0.12), gap=0.05))
    write('glooper_hurt', concat(bloop(700, 250, 0.2), gap=0) + 0)
    write('glooper_death', concat(bloop(500, 120, 0.4), bloop(300, 80, 0.5), gap=0.05))

    # MEAT CRAWLER: wet clicks and gurgles
    def click(n):
        parts = []
        for _ in range(n):
            parts.append(bandpass(noise(0.03), rng.uniform(800, 2400), 4) * perc(0.03, 80))
            parts.append(np.zeros(int(rng.uniform(0.02, 0.07) * SR)))
        return np.concatenate(parts)
    gurgle = lowpass(noise(0.6), 500) * (0.5 + 0.5 * osc(14, 0.6)) * env(0.6, 0.05, 0.1, 0.7, 0.3)
    write('crawler_ambient', fit(click(8) * 0.8, gurgle * 0.7))
    write('crawler_hurt', fit(lowpass(noise(0.25), 1200) * perc(0.25, 10), osc(glide(400, 150, 0.25), 0.25, 'saw') * perc(0.25, 8) * 0.5))
    write('crawler_death', fit(lowpass(noise(0.8), glide(2000, 200, 0.8)) * perc(0.8, 4), click(6) * 0.6))

    # WATCHER: eerie theremin-ish
    def ther(f0, d, depth=0.04):
        t = t_axis(d)
        return osc(f0 * (1 + depth * np.sin(2 * np.pi * 5.5 * t)), d, 'sine') * env(d, 0.15, 0.1, 0.8, 0.3)
    write('watcher_ambient', fit(ther(660, 1.2), ther(990, 1.2) * 0.3))
    write('watcher_hurt', osc(glide(1200, 500, 0.3), 0.3, 'tri') * perc(0.3, 8))
    write('watcher_death', fit(ther(800, 1.0, 0.1) * env(1.0, 0.01, 0.1, 0.6, 0.8), lowpass(noise(0.3), 2000) * perc(0.3, 12)))
    write('watcher_shoot', osc(glide(3000, 800, 0.3, 0.5), 0.3, 'square') * perc(0.3, 10) * 0.6 + osc(glide(1500, 400, 0.3), 0.3) * perc(0.3, 8))

    # CUSTOMS AGENT: muffled bureaucratic mumbling (low formant babble through a radio filter)
    def radio(x):
        return crush(bandpass(x, 1200, 0.9), 6)
    write('agent_ambient1', radio(babble([140, 130, 145, 120], 'aoeu', [0.12, 0.1, 0.14, 0.2], 'saw', 3)))
    write('agent_ambient2', radio(babble([150, 160, 140], 'eia', [0.15, 0.1, 0.25], 'saw', 3)))
    write('agent_hurt', radio(babble([200, 170], 'oa', [0.08, 0.15], 'saw', 3)))
    write('agent_death', radio(babble([180, 150, 110, 80], 'aaou', [0.12, 0.12, 0.2, 0.4], 'saw', 2)))
    d = 1.2
    siren = osc(700 + 250 * np.sign(np.sin(2 * np.pi * 2.5 * t_axis(d))), d, 'square') * env(d, 0.01, 0.1, 0.8, 0.1)
    write('agent_alert', lowpass(siren, 2500) * 0.7)

    # ZORP: high chipper alien chatter
    def chatter(n, base, seed):
        r = np.random.default_rng(seed)
        ps = [base * r.uniform(0.8, 1.6) for _ in range(n)]
        vs = ''.join(r.choice(list('aeiou')) for _ in range(n))
        ds = [r.uniform(0.05, 0.12) for _ in range(n)]
        return babble(ps, vs, ds, 'pulse25', 12)
    write('zorp_ambient1', chatter(6, 420, 1))
    write('zorp_ambient2', chatter(4, 380, 2))
    write('zorp_yes', babble([400, 600], 'ei', [0.1, 0.2], 'pulse25', 12))
    write('zorp_no', babble([500, 300], 'ou', [0.12, 0.22], 'pulse25', 12))

    # TOASTER
    spring = osc(glide(180, 1400, 0.12, 0.5), 0.12, 'tri') * perc(0.12, 12)
    clang = sum(osc(f, 0.4) * perc(0.4, 9) for f in (1250, 1871, 2630)) * 0.4
    write('toaster_pop', fit(spring, lowpass(noise(0.05), 3000) * perc(0.05, 60), np.concatenate([np.zeros(int(0.05 * SR)), clang])))
    write('toaster_ambient', sum(osc(f, 1.0) * perc(1.0, 3) for f in (2093, 2637, 3136)) * 0.5)
    write('toaster_hurt', fit(sum(osc(f, 0.3) * perc(0.3, 12) for f in (310, 470, 830, 1400)), lowpass(noise(0.1), 4000) * perc(0.1, 30)))

    # LEECH
    slurp = lowpass(noise(0.5), glide(300, 2500, 0.5)) * env(0.5, 0.05, 0.1, 0.7, 0.2) * (0.6 + 0.4 * osc(20, 0.5))
    write('leech_attach', fit(slurp, osc(glide(150, 600, 0.4), 0.4, 'saw') * perc(0.4, 6) * 0.3))
    write('leech_ambient', lowpass(noise(0.4), 900) * (0.5 + 0.5 * osc(9, 0.4)) * env(0.4, 0.05, 0.05, 0.8, 0.2))

    # OMNI-EYE
    d = 2.0
    t = t_axis(d)
    roar_src = osc(55 * (1 + 0.08 * np.sin(2 * np.pi * 4 * t)), d, 'saw') + osc(82.5, d, 'saw') * 0.6 + lowpass(noise(d), 300) * 0.8
    roar = sum(bandpass(roar_src, f, 3) * g for f, g in ((300, 1.0), (700, 0.6), (1600, 0.3))) * env(d, 0.2, 0.3, 0.8, 0.6)
    write('omni_roar', fit(roar, osc(glide(40, 30, d), d) * env(d, 0.2, 0.3, 0.8, 0.6) * 0.8))
    write('omni_hurt', fit(lowpass(osc(glide(180, 90, 0.5), 0.5, 'saw'), 1200) * perc(0.5, 5), lowpass(noise(0.3), 800) * perc(0.3, 10)))
    d = 3.0
    write('omni_death', fit(lowpass(osc(glide(200, 25, d, 0.6), d, 'saw'), 1500) * env(d, 0.05, 0.3, 0.8, 1.5),
                           lowpass(noise(d), glide(3000, 80, d)) * perc(d, 1.2) * 0.8,
                           sum(osc(glide(f, f / 4, d), d, 'sine') for f in (440, 554, 660)) * env(d, 0.3, 0.3, 0.4, 1.5) * 0.3))
    d = 0.6
    write('omni_laser', fit(osc(glide(2000, 200, d, 0.4), d, 'saw') * perc(d, 5) * 0.5,
                            osc(glide(1000, 100, d, 0.4), d, 'square') * perc(d, 6) * 0.4, highpass(noise(d), 2000) * perc(d, 10) * 0.3))


# ----------------------------------------------------------------- music disc
def gen_music():
    bpm = 126
    beat = 60 / bpm
    bars = 32
    total = bars * 4 * beat
    n = int(total * SR) + SR
    mixbuf = np.zeros(n)

    def place(x, start, gain=1.0):
        i = int(start * SR)
        e = min(n, i + len(x))
        mixbuf[i:e] += x[:e - i] * gain

    def note_freq(semi):  # semitones from A4
        return 440.0 * 2 ** (semi / 12)

    # E minor-ish progression with a weird borrowed chord: Em, C, G, D#dim -> D
    prog = [(-5, [0, 3, 7]), (-9, [0, 4, 7]), (-14, [0, 4, 7]), (-7, [0, 4, 7])]  # roots relative to A4: E4=-5, C4=-9, G3=-14, D4=-7
    scale = [0, 2, 3, 5, 7, 8, 10]  # natural minor from E
    melody_rng = np.random.default_rng(42)
    motif_a = [melody_rng.choice(scale) + 12 * melody_rng.integers(0, 2) for _ in range(8)]
    motif_b = [melody_rng.choice(scale) + 12 * melody_rng.integers(0, 2) for _ in range(8)]

    for bar in range(bars):
        root, chord = prog[bar % 4]
        section = (bar // 8) % 4  # intro, A, B, A'
        t0 = bar * 4 * beat
        # drums
        for b in range(4):
            tb = t0 + b * beat
            if section > 0 or b % 2 == 0:
                kick = osc(glide(150, 40, 0.18), 0.18) * perc(0.18, 18)
                place(kick, tb, 0.9)
            if b % 2 == 1 and section > 0:
                snare = highpass(noise(0.16), 1200) * perc(0.16, 18) + osc(190, 0.16, 'tri') * perc(0.16, 25) * 0.4
                place(snare, tb, 0.45)
            for h in range(2):
                hat = highpass(noise(0.04), 6000) * perc(0.04, 70)
                place(hat, tb + h * beat / 2, 0.18 if h else 0.12)
        # bass: octave-bouncing eighths
        for e in range(8):
            f = note_freq(root - 24 + (12 if e % 2 else 0))
            bs = lowpass(osc(f, beat / 2 * 0.95, 'square'), 900) * env(beat / 2 * 0.95, 0.005, 0.05, 0.6, 0.05)
            place(bs, t0 + e * beat / 2, 0.32)
        # arpeggio (pulse) in sections A/B
        if section >= 1:
            for s in range(16):
                semi = root + chord[s % 3] + (12 if (s // 3) % 2 else 0)
                ar = osc(note_freq(semi), beat / 4 * 0.9, 'pulse25') * env(beat / 4 * 0.9, 0.003, 0.03, 0.4, 0.03)
                place(ar, t0 + s * beat / 4, 0.09)
        # lead melody
        if section in (1, 3) or (section == 2 and bar % 2 == 0):
            motif = motif_a if section != 2 else motif_b
            for k in range(8):
                semi = -5 + motif[k] + (12 if section == 3 else 0)
                d = beat / 2
                t = t_axis(d * 0.95)
                f = note_freq(semi) * (1 + 0.01 * np.sin(2 * np.pi * 6 * t))
                ld = (osc(f, d * 0.95, 'square') * 0.5 + osc(f * 1.003, d * 0.95, 'saw') * 0.5) * env(d * 0.95, 0.01, 0.05, 0.7, 0.08)
                place(lowpass(ld, 3500), t0 + k * d, 0.16)
        # portal whoosh at the start of each 8-bar section
        if bar % 8 == 0:
            d = 2 * beat
            wh = lowpass(noise(d), glide(200, 5000, d, 0.6)) * env(d, d * 0.8, 0.05, 0.5, d * 0.15)
            place(wh, t0 - d if bar else 0, 0.35)
            if bar:
                zap = osc(glide(2000, 200, 0.4, 0.5), 0.4) * perc(0.4, 6)
                place(zap, t0, 0.25)
    # ending: big chord
    end = bars * 4 * beat
    d = 2.5
    fin = sum(osc(note_freq(-5 + s), d, 'saw') for s in (0, 7, 12, 15)) * env(d, 0.01, 0.4, 0.4, 1.8)
    place(lowpass(fin, 2500), end - 2 * beat, 0.2)
    out = delay(mixbuf, beat * 0.75, 0.3, 0.2, 2)[:n]
    write('music_disc_jam', out, 0.8)
    print('music length: %.1fs' % (n / SR))


if __name__ == '__main__':
    gen_effects()
    gen_mobs()
    gen_music()
    print('sounds generated:', len(os.listdir(OUT)))
