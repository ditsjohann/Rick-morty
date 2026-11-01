"""Generates every item, block, GUI, armor, effect and special-entity texture.

Run from the repo root:  python3 tools/gen_textures.py
"""
import json
import math
import os
import random
import sys

sys.path.insert(0, os.path.dirname(__file__))
from PIL import Image, ImageDraw  # noqa
from paint import *  # noqa
from sprites import S, P  # noqa

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, 'src/main/resources/assets/mayhem')
ITEM = os.path.join(A, 'textures/item')
BLOCK = os.path.join(A, 'textures/block')
ENT = os.path.join(A, 'textures/entity')
for d in (ITEM, BLOCK, ENT, os.path.join(A, 'textures/mob_effect'), os.path.join(A, 'textures/models/armor')):
    os.makedirs(d, exist_ok=True)


def save(img, path, frametime=None, interpolate=False):
    img.save(path)
    if frametime:
        with open(path + '.mcmeta', 'w') as f:
            json.dump({'animation': {'frametime': frametime, 'interpolate': interpolate}}, f, indent=2)


def render_sprite(name):
    rows, pal = S[name], P[name]
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.':
                img.putpixel((x, y), pal[ch])
    return img


# ------------------------------------------------------------------ procedural items
def orb(base, rim, highlight=(255, 255, 255, 255), r=6.0, cx=7.5, cy=7.5, alpha=255, seed=0, stars=None, swirl=False, outline=None):
    rng = random.Random(seed)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = x - cx, y - cy
            d = math.sqrt(dx * dx + dy * dy)
            if d <= r:
                t = d / r
                light = (-dx - dy) / (r * 1.8)
                c = shade(base, light * 0.6)
                if t > 0.78:
                    c = mix(c, rim, (t - 0.78) / 0.22)
                if swirl:
                    ang = math.atan2(dy, dx) + d * 0.9
                    if math.sin(ang * 3) > 0.55:
                        c = shade(c, 0.25)
                if stars and rng.random() < 0.08 and t < 0.8:
                    c = rng.choice(stars)
                img.putpixel((x, y), (c[0], c[1], c[2], alpha))
            elif d <= r + 1 and outline:
                img.putpixel((x, y), outline)
    hx, hy = int(cx - r * 0.45), int(cy - r * 0.45)
    img.putpixel((hx, hy), highlight)
    img.putpixel((hx + 1, hy), mix(highlight, base, 0.4))
    img.putpixel((hx, hy + 1), mix(highlight, base, 0.4))
    return img


def eye_item(iris, seed=3):
    img = orb(hexc('F2EEE6'), hexc('D8B8B0'), r=6, outline=hexc('3A1418'), seed=seed)
    cv = Canvas(16, 16, seed)
    cv.img = img
    cv.px = img.load()
    rng = random.Random(seed)
    for _ in range(14):
        a = rng.random() * math.tau
        rr = 4.5 + rng.random() * 1.2
        x, y = int(7.5 + math.cos(a) * rr), int(7.5 + math.sin(a) * rr)
        if cv.get(x, y)[3]:
            cv.put(x, y, hexc('C8203A'))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 8.5, y - 8)
            if d <= 3.2:
                cv.put(x, y, mix(shade(iris, 0.3), shade(iris, -0.3), d / 3.2))
            if abs(x - 8.5) <= 0.6 and abs(y - 8) <= 2.5:
                cv.put(x, y, hexc('0A0A10'))
    cv.put(7, 6, (255, 255, 255, 255))
    # nerve tail
    for i, (x, y) in enumerate([(13, 13), (14, 14), (15, 15)]):
        img.putpixel((x, y), hexc('B8364A'))
    return img


def lens_item():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 7.4:
                if d > 5.8:
                    c = shade(hexc('F2C230'), 0.2 - (d - 5.8) * 0.3)
                    if (x + y) % 3 == 0:
                        c = shade(c, -0.25)
                else:
                    t = d / 5.8
                    c = mix(hexc('F0C8FF'), hexc('5A0088'), t)
                    ang = math.atan2(y - 7.5, x - 7.5)
                    if math.sin(ang * 6 + d) > 0.6:
                        c = shade(c, 0.3)
                    if abs(x - 7.5) < 0.8 and abs(y - 7.5) < 3.5:
                        c = hexc('0A0010')
                img.putpixel((x, y), c)
    img.putpixel((5, 4), (255, 255, 255, 255))
    img.putpixel((4, 5), (255, 255, 255, 200))
    return img


def gloop_ball():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    base = hexc('B04CFF')
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, (y - 8.5) * 1.15
            wob = 5.6 + math.sin(math.atan2(dy, dx) * 5) * 0.5
            d = math.hypot(dx, dy)
            if d <= wob:
                c = shade(base, (-dx - dy) / 14)
                if d > wob - 1:
                    c = shade(base, -0.4)
                img.putpixel((x, y), (c[0], c[1], c[2], 220))
    for (x, y) in [(5, 5), (6, 5), (5, 6)]:
        img.putpixel((x, y), (255, 255, 255, 240))
    for (x, y) in [(9, 9), (10, 8)]:
        img.putpixel((x, y), hexc('E0A8FF'))
    # inner yellow core
    img.putpixel((8, 9), hexc('FFE45C'))
    img.putpixel((7, 9), hexc('FFF2A0'))
    return img


def items():
    for name in S:
        if name == 'stew_bowl_dummy':
            continue
        save(render_sprite(name), os.path.join(ITEM, name + '.png'))
    save(orb(hexc('1A0A2E'), hexc('8E00C9'), r=6, outline=hexc('05010A'), seed=5, stars=[hexc('FFFFFF'), hexc('FF9CF0'), hexc('C060FF')], swirl=True), os.path.join(ITEM, 'dark_matter.png'))
    save(gloop_ball(), os.path.join(ITEM, 'gloop_ball.png'))
    save(eye_item(hexc('E8341C')), os.path.join(ITEM, 'watcher_eye.png'))
    save(lens_item(), os.path.join(ITEM, 'omni_lens.png'))
    save(orb(hexc('7CFF3A'), hexc('E8FFB0'), r=5, seed=6, swirl=True), os.path.join(ITEM, 'portal_orb.png'))
    save(orb(hexc('3FE0FF'), hexc('FFFFFF'), r=4.5, seed=7, swirl=True), os.path.join(ITEM, 'plasma_orb.png'))
    save(orb(hexc('FF2A3A'), hexc('FFC0C8'), r=4, seed=8), os.path.join(ITEM, 'eye_beam_orb.png'))
    portal_gun_parts()


def portal_gun_parts():
    """16x16 texture atlas used by the 3D portal gun model."""
    for name, liquid, accent in (('portal_gun', '5CFF2E', 'C9CED6'), ('omni_portal_gun', 'C060FF', 'F2C230')):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
        cv = Canvas(16, 16, 4)
        cv.img, cv.px = img, img.load()
        Material(hexc('E9EEF4'), noise=0.06, border=0.25, grad=0.2).fill(cv, 0, 0, 8, 8, 'front')      # body shell
        Material(hexc(accent), noise=0.08, border=0.3).fill(cv, 8, 0, 8, 4, 'front')                     # accent rings
        Material(hexc('2A2D3A'), noise=0.1, border=0.2).fill(cv, 8, 4, 8, 4, 'front')                    # grip
        Material(hexc(liquid), noise=0.25, border=0.15, top=0.3).fill(cv, 0, 8, 8, 4, 'front')         # fluid tube
        for x in range(0, 8, 3):
            cv.put(x, 9, shade(hexc(liquid), 0.6))
        Material(hexc('9AA1AC'), noise=0.1, border=0.25).fill(cv, 8, 8, 8, 4, 'front')                   # emitter
        Material(shade(hexc(liquid), 0.4), noise=0.15, border=0.0).fill(cv, 0, 12, 4, 4, 'front')        # emitter glow
        Material(hexc('C8203A'), noise=0.1, border=0.2).fill(cv, 4, 12, 4, 4, 'front')                   # knob
        Material(hexc('454A5C'), noise=0.1, border=0.2).fill(cv, 8, 12, 8, 4, 'front')
        cv.put(10, 13, hexc('3FD66A'))
        cv.put(12, 13, hexc('FF3030'))
        save(img, os.path.join(ITEM, name + '.png'))


# ------------------------------------------------------------------ blocks
def noise_block(base, seed, noise=0.18, speckle=None, rate=0.0, scale=4, border=0.0):
    cv = Canvas(16, 16, seed)
    Material(base, noise=noise, speckle=speckle, speckle_rate=rate, border=border, side=0.0, scale=scale).fill(cv, 0, 0, 16, 16, 'front')
    return cv


def stone_like(base, seed, cracks=None):
    cv = Canvas(16, 16, seed)
    n = fbm(cv.rng, 16, 16, 3, 8)
    for y in range(16):
        for x in range(16):
            v = n[y][x]
            c = shade(base, (v - 0.5) * 0.5)
            if v > 0.68:
                c = shade(base, 0.18)
            if v < 0.32:
                c = shade(base, -0.22)
            cv.put(x, y, c)
    if cracks:
        for _ in range(3):
            x, y = cv.rng.randrange(16), cv.rng.randrange(16)
            for _ in range(5):
                cv.put(x, y, cracks)
                x = (x + cv.rng.choice((-1, 0, 1))) % 16
                y = (y + 1) % 16
    return cv


def ore(base_cv, gem_light, gem_mid, gem_dark, seed, clusters=((3, 3), (10, 5), (5, 11), (12, 12))):
    rng = random.Random(seed)
    cv = base_cv
    for (cx, cy) in clusters:
        pts = [(cx, cy), (cx + 1, cy), (cx, cy + 1), (cx + 1, cy + 1)]
        if rng.random() < 0.7:
            pts += [(cx + 2, cy + 1), (cx - 1, cy)]
        for i, (x, y) in enumerate(pts):
            cv.put(x % 16, y % 16, gem_mid)
        cv.put(cx % 16, cy % 16, gem_light)
        cv.put((cx + 1) % 16, (cy + 1) % 16, gem_dark)
        cv.put((cx - 1) % 16, (cy + 1) % 16, shade(gem_dark, -0.4))
    return cv


def bricks(base, seed):
    cv = Canvas(16, 16, seed)
    mortar = shade(base, -0.4)
    for y in range(16):
        row = y // 4
        for x in range(16):
            off = 0 if row % 2 == 0 else 4
            c = shade(base, (cv.rng.random() - 0.5) * 0.2)
            if y % 4 == 3 or (x + off) % 8 == 7:
                c = mortar
            elif y % 4 == 0:
                c = shade(c, 0.15)
            cv.put(x, y, c)
    return cv


def blocks():
    stone = hexc('7F7F7F')
    deep = hexc('4D4D55')
    c_light, c_mid, c_dark = hexc('C8FFFF'), hexc('2FD8E8'), hexc('127A8A')
    cv = ore(stone_like(stone, 1), c_light, c_mid, c_dark, 1)
    save(cv.img, os.path.join(BLOCK, 'flux_crystal_ore.png'))
    cv = ore(stone_like(deep, 2, cracks=hexc('34343C')), c_light, c_mid, c_dark, 2, clusters=((2, 4), (9, 2), (11, 10), (4, 12)))
    save(cv.img, os.path.join(BLOCK, 'deepslate_flux_crystal_ore.png'))

    # Flux block: animated pulsing crystal lattice
    frames = []
    for f in range(8):
        cv = Canvas(16, 16, 3)
        pulse = math.sin(f / 8 * math.tau) * 0.12
        for y in range(16):
            for x in range(16):
                facet = ((x // 4) + (y // 4)) % 2
                c = shade(hexc('2FD8E8'), (0.1 if facet else -0.1) + pulse + (cv.rng.random() - 0.5) * 0.1)
                if x % 4 == 0 or y % 4 == 0:
                    c = shade(hexc('127A8A'), pulse)
                if (x, y) in ((5, 5), (10, 10), (13, 2), (2, 13)):
                    c = (255, 255, 255, 255)
                cv.put(x, y, c)
        frames.append(cv.img)
    strip(frames, os.path.join(BLOCK, 'flux_block.png'), 3, True)

    # Dark matter block: swirling starfield animation
    frames = []
    rng = random.Random(9)
    stars = [(rng.random() * 16, rng.random() * 16, rng.random()) for _ in range(18)]
    for f in range(16):
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
        for y in range(16):
            for x in range(16):
                dx, dy = x - 7.5, y - 7.5
                ang = math.atan2(dy, dx) + f / 16 * math.tau
                d = math.hypot(dx, dy)
                v = (math.sin(ang * 2 + d * 0.8) + 1) / 2
                c = mix(hexc('05010A'), hexc('3A0A5E'), v * 0.8)
                img.putpixel((x, y), c)
        for (sx, sy, ph) in stars:
            a = f / 16 * math.tau * 0.5
            x = int((7.5 + (sx - 7.5) * math.cos(a) - (sy - 7.5) * math.sin(a)) % 16)
            y = int((7.5 + (sx - 7.5) * math.sin(a) + (sy - 7.5) * math.cos(a)) % 16)
            tw = (math.sin(f * 0.8 + ph * 10) + 1) / 2
            img.putpixel((x, y), mix(hexc('8E00C9'), hexc('FFFFFF'), tw))
        frames.append(img)
    strip(frames, os.path.join(BLOCK, 'dark_matter_block.png'), 2, False)

    # Gloop block (translucent jelly with inner square like slime)
    cv = Canvas(16, 16, 4)
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            inner = 3 <= x <= 12 and 3 <= y <= 12
            inner_edge = inner and (x in (3, 12) or y in (3, 12))
            c = hexc('B04CFF', 140)
            if edge:
                c = hexc('7A2BB8', 230)
            elif inner_edge:
                c = hexc('C870FF', 200)
            elif inner:
                c = hexc('D9A0FF', 170)
            if cv.rng.random() < 0.06 and not edge:
                c = (255, 255, 255, 190)
            cv.put(x, y, c)
    cv.put(5, 5, (255, 255, 255, 230)); cv.put(6, 5, (255, 255, 255, 200)); cv.put(5, 6, (255, 255, 255, 200))
    cv.put(9, 9, hexc('FFE45C', 230))
    save(cv.img, os.path.join(BLOCK, 'gloop_block.png'))

    gstone = hexc('9C7FC0')
    save(stone_like(gstone, 5, cracks=hexc('7A5E9E')).img, os.path.join(BLOCK, 'gloopstone.png'))
    save(bricks(hexc('A68AC8'), 6).img, os.path.join(BLOCK, 'gloopstone_bricks.png'))

    # Gloop grass: neon moss top, drippy side
    cv = Canvas(16, 16, 7)
    n = fbm(cv.rng, 16, 16, 2, 4)
    for y in range(16):
        for x in range(16):
            v = n[y][x]
            c = mix(hexc('3FD66A'), hexc('B8FF5A'), v)
            if cv.rng.random() < 0.05:
                c = hexc('FF9CF0')
            cv.put(x, y, c)
    save(cv.img, os.path.join(BLOCK, 'gloop_grass_top.png'))
    side = stone_like(gstone, 8, cracks=hexc('7A5E9E'))
    rng = random.Random(8)
    for x in range(16):
        depth = 2 + rng.randint(0, 2) + (3 if rng.random() < 0.2 else 0)
        for y in range(depth):
            side.put(x, y, mix(hexc('3FD66A'), hexc('B8FF5A'), rng.random()))
        side.put(x, depth, hexc('2BA84A'))
    save(side.img, os.path.join(BLOCK, 'gloop_grass_side.png'))

    # Wobble shroom (cross plant)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    cap = hexc('FF5FD8'); capd = hexc('B0208C'); stem = hexc('F5E6FF'); spot = hexc('FFF2A0')
    for y in range(16):
        for x in range(16):
            dx = x - 7.5
            if 3 <= y <= 8 and abs(dx) <= 6.5 - (8 - y) * 0.6 * (1 if y < 5 else 0.4):
                c = cap if y < 7 else capd
                if (x + y) % 5 == 0 and y < 7:
                    c = spot
                img.putpixel((x, y), c)
            elif 9 <= y <= 15 and abs(dx + math.sin(y * 0.9) * 0.8) <= 1.3:
                img.putpixel((x, y), stem if x % 2 else shade(stem, -0.12))
    # tiny side shroom
    for (x, y) in [(2, 11), (3, 11), (1, 12), (2, 12), (3, 12), (4, 12)]:
        img.putpixel((x, y), hexc('7CFF5A'))
    img.putpixel((2, 13), stem); img.putpixel((2, 14), stem); img.putpixel((2, 15), stem)
    save(img, os.path.join(BLOCK, 'wobble_shroom.png'))

    # Flux cluster (cross)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    shards = [(7, 2, 2, 14), (3, 6, 1, 10), (11, 5, 2, 11), (5, 9, 1, 7), (13, 9, 1, 7)]
    for (sx, top, half, height) in shards:
        for y in range(top, 16):
            w = half if y > top + 1 else 0
            for x in range(sx - w, sx + w + 1):
                if 0 <= x < 16:
                    c = hexc('2FD8E8') if x <= sx else hexc('127A8A')
                    if x == sx - w:
                        c = hexc('8FF7FF')
                    if y == top:
                        c = (255, 255, 255, 255)
                    img.putpixel((x, y), c)
    save(img, os.path.join(BLOCK, 'flux_cluster.png'))

    # Flesh
    def flesh(seed):
        cv = Canvas(16, 16, seed)
        n = fbm(cv.rng, 16, 16, 3, 8)
        for y in range(16):
            for x in range(16):
                v = n[y][x]
                c = mix(hexc('8E2238'), hexc('E0607A'), v)
                if v > 0.72:
                    c = hexc('F5B0B8')
                cv.put(x, y, c)
        for _ in range(4):
            x, y = cv.rng.randrange(16), cv.rng.randrange(16)
            for _ in range(7):
                cv.put(x, y, hexc('5E1024'))
                x = (x + cv.rng.choice((-1, 0, 1))) % 16
                y = (y + cv.rng.choice((-1, 0, 1, 1))) % 16
        for _ in range(5):
            cv.put(cv.rng.randrange(16), cv.rng.randrange(16), hexc('F2C9A0'))
        return cv
    save(flesh(10).img, os.path.join(BLOCK, 'flesh_block.png'))

    # Eye flesh: animated blinking eye
    frames = []
    lid_open = [1, 1, 1, 1, 1, 1, 1, 1, 0.6, 0.0, 0.6, 1, 1, 1, 1, 1]
    for f, opn in enumerate(lid_open):
        cv = flesh(11)
        for y in range(16):
            for x in range(16):
                dx, dy = x - 7.5, (y - 7.5) * 1.5
                d = math.hypot(dx, dy)
                if d <= 6:
                    if abs(y - 7.5) > 4 * opn:
                        cv.put(x, y, hexc('B83A54'))
                        continue
                    c = hexc('F5F0E8')
                    look = math.sin(f / 16 * math.tau) * 1.5
                    di = math.hypot(x - 7.5 - look, y - 7.5)
                    if di <= 3:
                        c = mix(hexc('FFB020'), hexc('C85A00'), di / 3)
                    if di <= 1.2:
                        c = hexc('100808')
                    cv.put(x, y, c)
        frames.append(cv.img)
    strip(frames, os.path.join(BLOCK, 'eye_flesh.png'), 4, False)

    # Gristle: pale fibrous bone-stone
    cv = Canvas(16, 16, 12)
    for y in range(16):
        for x in range(16):
            v = math.sin(x * 0.9 + math.sin(y * 0.5) * 2) * 0.5 + 0.5
            c = mix(hexc('D8C8A8'), hexc('F2E8D0'), v)
            if cv.rng.random() < 0.08:
                c = hexc('C0A880')
            if cv.rng.random() < 0.03:
                c = hexc('E07A8A')
            cv.put(x, y, c)
    save(cv.img, os.path.join(BLOCK, 'gristle.png'))
    gr = Image.open(os.path.join(BLOCK, 'gristle.png'))
    cv2 = Canvas(16, 16, 13)
    cv2.img, cv2.px = gr.copy(), None
    cv2.px = cv2.img.load()
    cv2 = ore(cv2, hexc('C060FF'), hexc('3A0A5E'), hexc('12001E'), 13, clusters=((3, 4), (11, 3), (8, 10), (2, 12), (13, 12)))
    save(cv2.img, os.path.join(BLOCK, 'dark_matter_ore.png'))

    # Landing pad: metal plate with glowing ring
    cv = Canvas(16, 16, 14)
    for y in range(16):
        for x in range(16):
            c = shade(hexc('5A6270'), (cv.rng.random() - 0.5) * 0.12)
            if x in (0, 15) or y in (0, 15):
                c = hexc('2A2D3A')
            d = math.hypot(x - 7.5, y - 7.5)
            if 4.5 <= d <= 5.8:
                c = hexc('7CFF3A')
            if (x in (7, 8) and 5 <= y <= 10) or (y in (7, 8) and 5 <= x <= 10):
                c = hexc('E8FFB0') if abs(x - 7.5) < 1 and abs(y - 7.5) < 1 else hexc('B8FF9A')
            if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
                c = hexc('C9CED6')
            cv.put(x, y, c)
    save(cv.img, os.path.join(BLOCK, 'landing_pad.png'))
    cv = Canvas(16, 16, 15)
    for y in range(16):
        for x in range(16):
            c = shade(hexc('454A5C'), (cv.rng.random() - 0.5) * 0.1)
            if y < 2:
                c = hexc('7CFF3A') if x % 4 < 2 else hexc('1E2024')
            cv.put(x, y, c)
    save(cv.img, os.path.join(BLOCK, 'landing_pad_side.png'))

    tv_textures()


def strip(frames, path, frametime, interpolate):
    img = Image.new('RGBA', (16, 16 * len(frames)))
    for i, f in enumerate(frames):
        img.paste(f, (0, i * 16))
    save(img, path, frametime, interpolate)


def tv_textures():
    # cabinet wood
    cv = Canvas(16, 16, 20)
    for y in range(16):
        for x in range(16):
            v = math.sin(y * 1.3 + math.sin(x * 0.4) * 1.5) * 0.5 + 0.5
            c = mix(hexc('6E4520'), hexc('9A6534'), v)
            if cv.rng.random() < 0.04:
                c = hexc('4A2A10')
            cv.put(x, y, c)
    save(cv.img, os.path.join(BLOCK, 'tv_cabinet.png'))
    cv = Canvas(16, 16, 21)
    Material(hexc('2A2D3A'), noise=0.1, border=0.3).fill(cv, 0, 0, 16, 16, 'front')
    save(cv.img, os.path.join(BLOCK, 'tv_antenna.png'))
    # Screens: 0 static, 1 test pattern bars, 2 swirling portal, 3 staring eye
    rng = random.Random(22)

    def framed(drawfn, n, path, ft):
        frames = []
        for f in range(n):
            img = Image.new('RGBA', (16, 16), (0, 0, 0, 255))
            px = img.load()
            for y in range(16):
                for x in range(16):
                    if x < 2 or x > 13 or y < 2 or y > 11:
                        px[x, y] = hexc('3A2410') if (y > 11 and y < 16) else hexc('6E4520')
                        if y >= 12:
                            px[x, y] = hexc('5A3A1A')
                    else:
                        px[x, y] = drawfn(f, x - 2, y - 2)
            # bezel and knobs
            for x in range(1, 15):
                px[x, 1] = hexc('1E1E24'); px[x, 12] = hexc('1E1E24')
            for y in range(1, 13):
                px[1, y] = hexc('1E1E24'); px[14, y] = hexc('1E1E24')
            px[4, 14] = hexc('C9CED6'); px[7, 14] = hexc('C9CED6'); px[11, 14] = hexc('FF3030')
            frames.append(img)
        strip(frames, path, ft, False)

    def static(f, x, y):
        v = rng.random()
        c = int(40 + v * 200)
        if (y + f) % 5 == 0:
            c = min(255, c + 40)
        return (c, c, int(c * 1.05) if c < 243 else 255, 255)

    bars = [hexc('FFFFFF'), hexc('FFE45C'), hexc('3FE0FF'), hexc('3FD66A'), hexc('FF4FD8'), hexc('FF3030'), hexc('3F5FFF')]

    def testcard(f, x, y):
        if y < 7:
            return bars[min(6, x * 7 // 12)]
        if (x + f) % 12 < 2:
            return hexc('FFFFFF')
        return hexc('1E1E24') if (x // 2 + y) % 2 else hexc('454A5C')

    def portal(f, x, y):
        dx, dy = x - 5.5, (y - 4.5) * 1.2
        d = math.hypot(dx, dy)
        ang = math.atan2(dy, dx) + f / 8 * math.tau
        v = (math.sin(ang * 3 + d * 1.4) + 1) / 2
        if d > 5:
            return hexc('0A1A08')
        return mix(hexc('2FBF2A'), hexc('E8FFB0'), v * (1 - d / 6))

    def eye(f, x, y):
        look = [0, 0, 1, 2, 2, 1, 0, -1, -2, -2, -1, 0][f % 12]
        dx, dy = x - 5.5, (y - 4.5) * 1.4
        d = math.hypot(dx, dy)
        if d > 5:
            return hexc('3A0A1A')
        di = math.hypot(x - 5.5 - look, y - 4.5)
        if di <= 1:
            return hexc('050505')
        if di <= 2.6:
            return hexc('E8341C')
        return hexc('F2EEE6')

    framed(static, 8, os.path.join(BLOCK, 'tv_screen_0.png'), 1)
    framed(testcard, 12, os.path.join(BLOCK, 'tv_screen_1.png'), 2)
    framed(portal, 8, os.path.join(BLOCK, 'tv_screen_2.png'), 2)
    framed(eye, 12, os.path.join(BLOCK, 'tv_screen_3.png'), 4)


# ------------------------------------------------------------------ special entity textures
def portal_textures():
    N = 128
    swirl = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    rim = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    core = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    c = (N - 1) / 2
    for y in range(N):
        for x in range(N):
            dx, dy = (x - c) / c, (y - c) / c
            d = math.hypot(dx, dy)
            if d > 1:
                continue
            ang = math.atan2(dy, dx)
            arms = (math.sin(ang * 4 + d * 9) + 1) / 2
            v = 0.45 + 0.55 * arms
            v *= 0.65 + 0.35 * (1 - d)
            a = 255 if d < 0.9 else int(255 * (1 - (d - 0.9) / 0.1))
            g = int(255 * min(1, v))
            swirl.putpixel((x, y), (g, g, g, a))
            # rim: bright ring near the edge
            ring = math.exp(-((d - 0.88) ** 2) / 0.004)
            flick = 0.7 + 0.3 * math.sin(ang * 13)
            ra = int(255 * min(1, ring * flick))
            rim.putpixel((x, y), (255, 255, 255, ra))
            # core flare
            ca = int(255 * max(0, 1 - d) ** 2)
            core.putpixel((x, y), (255, 255, 255, ca))
    # pixelate for a Minecrafty look
    swirl = swirl.resize((32, 32), Image.NEAREST).resize((N, N), Image.NEAREST)
    rim = rim.resize((32, 32), Image.NEAREST).resize((N, N), Image.NEAREST)
    core = core.resize((16, 16), Image.BILINEAR).resize((64, 64), Image.NEAREST)
    swirl.save(os.path.join(ENT, 'portal_swirl.png'))
    rim.save(os.path.join(ENT, 'portal_rim.png'))
    core.save(os.path.join(ENT, 'portal_core.png'))

    hole = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    for y in range(32):
        for x in range(32):
            d = math.hypot(x - 15.5, y - 15.5) / 15.5
            if d <= 0.62:
                hole.putpixel((x, y), (0, 0, 0, 255))
            elif d <= 1:
                t = (d - 0.62) / 0.38
                col = mix(hexc('FFE0A0'), hexc('8E00C9'), t)
                hole.putpixel((x, y), (col[0], col[1], col[2], int(255 * (1 - t) ** 1.5)))
    hole.save(os.path.join(ENT, 'black_hole.png'))
    disk = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    rng = random.Random(4)
    for y in range(64):
        for x in range(64):
            d = math.hypot(x - 31.5, y - 31.5) / 31.5
            if 0.45 <= d <= 1:
                t = (d - 0.45) / 0.55
                col = mix(hexc('FFFFFF'), hexc('FF6A00'), t)
                ang = math.atan2(y - 31.5, x - 31.5)
                streak = 0.75 + 0.25 * math.sin(ang * 9 + d * 20)
                a = int(255 * (1 - t) * streak)
                disk.putpixel((x, y), (col[0], col[1], col[2], a))
    disk = disk.resize((32, 32), Image.NEAREST).resize((64, 64), Image.NEAREST)
    disk.save(os.path.join(ENT, 'accretion_disk.png'))


def armor():
    # Lab goggles on the helmet region of layer_1 (head 8x8x8 at 0,0 with dilation)
    img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    cv = Canvas(64, 32, 30)
    cv.img, cv.px = img, img.load()
    strap = hexc('6E4520')
    for name, (x, y, w, h) in faces(0, 0, 8, 8, 8).items():
        if name in ('top', 'bottom'):
            continue
        for xx in range(w):
            cv.put(x + xx, y + 3, strap)
            cv.put(x + xx, y + 4, shade(strap, -0.2))
        if name == 'front':
            for (gx, gy) in ((1, 2), (5, 2)):
                for yy in range(3):
                    for xx in range(2):
                        cv.put(x + gx + xx, y + gy + yy, hexc('5CFF2E') if (xx, yy) != (0, 0) else hexc('E8FFB0'))
                cv.put(x + gx - 1, y + gy + 1, hexc('9AA1AC'))
                cv.put(x + gx + 2, y + gy + 1, hexc('9AA1AC'))
                for xx in range(-1, 3):
                    cv.put(x + gx + xx, y + gy - 1, hexc('D8DEE8'))
                    cv.put(x + gx + xx, y + gy + 3, hexc('9AA1AC'))
    img.save(os.path.join(A, 'textures/models/armor/lab_layer_1.png'))
    Image.new('RGBA', (64, 32), (0, 0, 0, 0)).save(os.path.join(A, 'textures/models/armor/lab_layer_2.png'))
    # Gloop boots on the leg region (0,16) bottom rows
    img = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    cv = Canvas(64, 32, 31)
    cv.img, cv.px = img, img.load()
    for name, (x, y, w, h) in faces(0, 16, 4, 12, 4).items():
        if name == 'top':
            continue
        if name == 'bottom':
            cv.rect(x, y, w, h, hexc('5A1F88', 255))
            continue
        for yy in range(h - 5, h):
            for xx in range(w):
                c = hexc('B04CFF', 220) if yy < h - 1 else hexc('7CFF5A')
                if yy == h - 5:
                    c = hexc('E0A8FF', 230)
                cv.put(x + xx, y + yy, c)
    img.save(os.path.join(A, 'textures/models/armor/gloop_layer_1.png'))
    Image.new('RGBA', (64, 32), (0, 0, 0, 0)).save(os.path.join(A, 'textures/models/armor/gloop_layer_2.png'))


def effects():
    out = os.path.join(A, 'textures/mob_effect')

    def icon(draw, name):
        img = Image.new('RGBA', (18, 18), (0, 0, 0, 0))
        draw(img)
        img.save(os.path.join(out, name + '.png'))

    def shrunk(img):
        d = ImageDraw.Draw(img)
        d.rectangle((7, 9, 10, 14), fill=hexc('7CFFCB'))
        d.rectangle((7, 6, 10, 8), fill=hexc('F0D0B0'))
        for (a, b) in (((1, 1), (5, 5)), ((16, 1), (12, 5)), ((1, 16), (5, 12)), ((16, 16), (12, 12))):
            d.line((a, b), fill=hexc('FFFFFF'))
            d.point(b, fill=hexc('3FD6B5'))

    def enlarged(img):
        d = ImageDraw.Draw(img)
        d.rectangle((5, 6, 12, 16), fill=hexc('FF7A3C'))
        d.rectangle((5, 1, 12, 5), fill=hexc('F0D0B0'))
        d.point((7, 3), fill=hexc('000000')); d.point((10, 3), fill=hexc('000000'))
        d.line((0, 8, 3, 8), fill=hexc('FFFFFF')); d.line((14, 8, 17, 8), fill=hexc('FFFFFF'))

    def hyper(img):
        d = ImageDraw.Draw(img)
        d.polygon([(10, 0), (3, 10), (8, 10), (6, 17), (14, 6), (9, 6)], fill=hexc('FF4FD8'), outline=hexc('FFE45C'))

    def brain(img):
        d = ImageDraw.Draw(img)
        d.ellipse((2, 3, 15, 14), fill=hexc('FF9CF0'), outline=hexc('9B3BFF'))
        d.line((8, 3, 8, 14), fill=hexc('C25AB0'))
        d.arc((3, 5, 8, 10), 0, 180, fill=hexc('C25AB0'))
        d.arc((9, 7, 14, 12), 180, 360, fill=hexc('C25AB0'))
        d.line((12, 14, 15, 17), fill=hexc('6A2FA8'), width=2)

    icon(shrunk, 'shrunk')
    icon(enlarged, 'enlarged')
    icon(hyper, 'hyper')
    icon(brain, 'brain_drain')


def mod_icon():
    N = 128
    img = Image.new('RGBA', (N, N), hexc('120A1E'))
    px = img.load()
    for y in range(N):
        for x in range(N):
            dx, dy = (x - 63.5) / 44, (y - 63.5) / 58
            d = math.hypot(dx, dy)
            if d <= 1:
                ang = math.atan2(dy, dx)
                v = (math.sin(ang * 4 + d * 10) + 1) / 2
                px[x, y] = mix(hexc('2FBF2A'), hexc('E8FFB0'), v * (1 - d * 0.6))
            elif d <= 1.12:
                px[x, y] = hexc('7CFF3A')
    # eyeball peeking through
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 64, y - 66)
            if d <= 20:
                c = hexc('F2EEE6')
                if d <= 11:
                    c = mix(hexc('C060FF'), hexc('5A0088'), d / 11)
                if abs(x - 64) <= 3 and abs(y - 66) <= 9:
                    c = hexc('0A0010')
                px[x, y] = c
    img = img.resize((32, 32), Image.NEAREST).resize((N, N), Image.NEAREST)
    img.save(os.path.join(A, 'icon.png'))


if __name__ == '__main__':
    items()
    blocks()
    portal_textures()
    armor()
    effects()
    mod_icon()
    print('textures generated')
