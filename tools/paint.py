"""Shared procedural pixel-art painting helpers for Multiverse Mayhem textures."""
import math
import random
from PIL import Image


def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, int(round(v))))


def hexc(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def mix(c1, c2, t):
    return tuple(clamp(c1[i] + (c2[i] - c1[i]) * t) for i in range(4))


def shade(c, f):
    """f > 0 lightens, f < 0 darkens."""
    if f >= 0:
        return (clamp(c[0] + (255 - c[0]) * f), clamp(c[1] + (255 - c[1]) * f), clamp(c[2] + (255 - c[2]) * f), c[3])
    f = -f
    return (clamp(c[0] * (1 - f)), clamp(c[1] * (1 - f)), clamp(c[2] * (1 - f)), c[3])


def hsv_shift(c, dh=0.0, ds=0.0, dv=0.0):
    import colorsys
    h, s, v = colorsys.rgb_to_hsv(c[0] / 255, c[1] / 255, c[2] / 255)
    h = (h + dh) % 1.0
    s = max(0, min(1, s + ds))
    v = max(0, min(1, v + dv))
    r, g, b = colorsys.hsv_to_rgb(h, s, v)
    return (clamp(r * 255), clamp(g * 255), clamp(b * 255), c[3])


class Canvas:
    def __init__(self, w, h, seed=1):
        self.w, self.h = w, h
        self.img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        self.glow = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        self.px = self.img.load()
        self.gx = self.glow.load()
        self.rng = random.Random(seed)

    def put(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = c

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[x, y]
        return (0, 0, 0, 0)

    def glow_put(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.gx[x, y] = c

    def rect(self, x0, y0, w, h, c):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                self.put(x, y, c)

    def save(self, path, glow_path=None):
        self.img.save(path)
        if glow_path:
            self.glow.save(glow_path)


def value_noise(rng, w, h, scale=4):
    gw, gh = w // scale + 2, h // scale + 2
    grid = [[rng.random() for _ in range(gw)] for _ in range(gh)]
    out = [[0.0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            fx, fy = x / scale, y / scale
            ix, iy = int(fx), int(fy)
            tx, ty = fx - ix, fy - iy
            tx = tx * tx * (3 - 2 * tx)
            ty = ty * ty * (3 - 2 * ty)
            a = grid[iy][ix] + (grid[iy][ix + 1] - grid[iy][ix]) * tx
            b = grid[iy + 1][ix] + (grid[iy + 1][ix + 1] - grid[iy + 1][ix]) * tx
            out[y][x] = a + (b - a) * ty
    return out


def fbm(rng, w, h, octaves=3, base=8):
    acc = [[0.0] * w for _ in range(h)]
    amp, tot = 1.0, 0.0
    s = base
    for _ in range(octaves):
        n = value_noise(rng, w, h, max(1, s))
        for y in range(h):
            for x in range(w):
                acc[y][x] += n[y][x] * amp
        tot += amp
        amp *= 0.5
        s = max(1, s // 2)
    return [[v / tot for v in row] for row in acc]


# ---------------------------------------------------------------- materials
class Material:
    """Fills a rectangular face with shaded, noisy colour."""

    def __init__(self, base, dark=None, light=None, noise=0.18, speckle=None, speckle_rate=0.0,
                 border=0.22, top=0.12, bottom=-0.25, side=-0.06, alpha=255, veins=None, vein_rate=0.0,
                 grad=0.0, scale=4, glow=None):
        self.base = base
        self.dark = dark or shade(base, -0.35)
        self.light = light or shade(base, 0.25)
        self.noise = noise
        self.speckle = speckle or []
        self.speckle_rate = speckle_rate
        self.border = border
        self.top, self.bottom, self.side = top, bottom, side
        self.alpha = alpha
        self.veins = veins
        self.vein_rate = vein_rate
        self.grad = grad
        self.scale = scale
        self.glow = glow

    def fill(self, cv, x0, y0, w, h, face):
        if w <= 0 or h <= 0:
            return
        rng = cv.rng
        n = value_noise(rng, w, h, self.scale)
        fshade = {'top': self.top, 'bottom': self.bottom}.get(face, self.side if face in ('west', 'east') else 0.0)
        if face == 'back':
            fshade = self.side * 1.5
        for y in range(h):
            for x in range(w):
                v = (n[y][x] - 0.5) * 2 * self.noise
                g = 0.0
                if self.grad and face not in ('top', 'bottom') and h > 1:
                    g = self.grad * (0.5 - y / (h - 1))
                c = self.base
                t = v + fshade + g
                c = shade(c, t)
                if self.speckle and rng.random() < self.speckle_rate:
                    c = rng.choice(self.speckle)
                edge = x == 0 or y == 0 or x == w - 1 or y == h - 1
                if edge and w > 2 and h > 2 and self.border:
                    c = shade(c, -self.border)
                c = (c[0], c[1], c[2], self.alpha if len(c) < 4 or c[3] == 255 else c[3])
                cv.put(x0 + x, y0 + y, c)
                if self.glow:
                    cv.glow_put(x0 + x, y0 + y, self.glow)
        if self.veins and w > 3 and h > 3:
            count = max(1, int(w * h * self.vein_rate))
            for _ in range(count):
                x, y = rng.randrange(w), rng.randrange(h)
                for _ in range(rng.randint(3, 8)):
                    cv.put(x0 + x, y0 + y, self.veins)
                    x = max(0, min(w - 1, x + rng.choice((-1, 0, 1))))
                    y = max(0, min(h - 1, y + rng.choice((-1, 0, 1))))


def faces(u, v, w, h, d):
    return {
        'top': (u + d, v, w, d),
        'bottom': (u + d + w, v, w, d),
        'west': (u, v + d, d, h),
        'front': (u + d, v + d, w, h),
        'east': (u + d + w, v + d, d, h),
        'back': (u + d + w + d, v + d, w, h),
    }


def paint_cuboid(cv, uv, size, mat, decals=None, face_mats=None):
    u, v = uv
    w, h, d = size
    for name, (x, y, fw, fh) in faces(u, v, w, h, d).items():
        m = (face_mats or {}).get(name, mat)
        m.fill(cv, x, y, fw, fh, name)
        if decals and name in decals:
            for dec in (decals[name] if isinstance(decals[name], list) else [decals[name]]):
                dec(cv, x, y, fw, fh)


# ---------------------------------------------------------------- decals
def eye_decal(iris, pupil=(10, 10, 14, 255), sclera=(245, 242, 232, 255), rel=(0.5, 0.5), r=None, glow=True, veins=True, slit=False):
    def draw(cv, x0, y0, w, h):
        cx = x0 + (w - 1) * rel[0]
        cy = y0 + (h - 1) * rel[1]
        rad = r if r is not None else min(w, h) * 0.45
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                dx, dy = x - cx, y - cy
                dist = math.sqrt(dx * dx + dy * dy)
                if dist <= rad:
                    c = sclera
                    if veins and dist > rad * 0.62 and cv.rng.random() < 0.18:
                        c = (205, 60, 70, 255)
                    if dist <= rad * 0.58:
                        t = dist / (rad * 0.58)
                        c = mix(shade(iris, 0.35), shade(iris, -0.25), t)
                        if glow:
                            cv.glow_put(x, y, shade(iris, 0.2))
                    pr = rad * 0.26
                    if (slit and abs(dx) <= max(0.6, pr * 0.45) and abs(dy) <= rad * 0.5) or (not slit and dist <= pr):
                        c = pupil
                        if glow:
                            cv.glow_put(x, y, (0, 0, 0, 0))
                    cv.put(x, y, c)
        # specular
        hx, hy = int(round(cx - rad * 0.3)), int(round(cy - rad * 0.3))
        cv.put(hx, hy, (255, 255, 255, 255))
        if glow:
            cv.glow_put(hx, hy, (255, 255, 255, 255))
    return draw


def mouth_decal(color=(40, 8, 18, 255), teeth=(240, 232, 200, 255), rel_y=0.72, width=0.6, height=2, tooth_every=2):
    def draw(cv, x0, y0, w, h):
        mw = max(2, int(w * width))
        mx = x0 + (w - mw) // 2
        my = y0 + int((h - 1) * rel_y)
        for yy in range(height):
            for xx in range(mw):
                cv.put(mx + xx, my + yy, color)
        for xx in range(0, mw, tooth_every):
            cv.put(mx + xx, my, teeth)
            if height > 2:
                cv.put(mx + xx + 1, my + height - 1, teeth)
    return draw


def smile_decal(color=(30, 10, 40, 255), rel_y=0.7):
    def draw(cv, x0, y0, w, h):
        cx = x0 + w / 2
        my = y0 + int(h * rel_y)
        half = max(2, w // 4)
        for i in range(-half, half):
            yy = my + (1 if abs(i + 0.5) < half - 1 else 0)
            cv.put(int(cx + i), yy, color)
    return draw


def spots_decal(color, count, rmax=1.2):
    def draw(cv, x0, y0, w, h):
        for _ in range(count):
            cx, cy = x0 + cv.rng.random() * w, y0 + cv.rng.random() * h
            r = 0.5 + cv.rng.random() * rmax
            for y in range(y0, y0 + h):
                for x in range(x0, x0 + w):
                    if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                        cv.put(x, y, color)
    return draw


def bubbles_decal(count, color=(255, 255, 255, 120)):
    def draw(cv, x0, y0, w, h):
        for _ in range(count):
            x, y = x0 + cv.rng.randrange(max(1, w)), y0 + cv.rng.randrange(max(1, h))
            base = cv.get(x, y)
            cv.put(x, y, mix(base, color, 0.6))
            if cv.rng.random() < 0.5:
                cv.put(x + 1, y, mix(base, color, 0.3))
    return draw


def stripe_decal(color, every=3, horizontal=True, offset=0):
    def draw(cv, x0, y0, w, h):
        rng = range(h) if horizontal else range(w)
        for i in rng:
            if (i + offset) % every == 0:
                if horizontal:
                    for x in range(w):
                        cv.put(x0 + x, y0 + i, color)
                else:
                    for y in range(h):
                        cv.put(x0 + i, y0 + y, color)
    return draw


def rect_decal(rx, ry, rw, rh, color, glow=None):
    """Rect in relative pixel coords inside the face (negative = from right/bottom)."""
    def draw(cv, x0, y0, w, h):
        xs = x0 + (rx if rx >= 0 else w + rx)
        ys = y0 + (ry if ry >= 0 else h + ry)
        for y in range(ys, ys + rh):
            for x in range(xs, xs + rw):
                if x0 <= x < x0 + w and y0 <= y < y0 + h:
                    cv.put(x, y, color)
                    if glow:
                        cv.glow_put(x, y, glow)
    return draw


def pixel_decal(points, color, glow=None):
    def draw(cv, x0, y0, w, h):
        for (px, py) in points:
            x = x0 + (px if px >= 0 else w + px)
            y = y0 + (py if py >= 0 else h + py)
            cv.put(x, y, color)
            if glow:
                cv.glow_put(x, y, glow)
    return draw


def border_decal(color, sides='lrtb'):
    def draw(cv, x0, y0, w, h):
        for x in range(w):
            if 't' in sides:
                cv.put(x0 + x, y0, color)
            if 'b' in sides:
                cv.put(x0 + x, y0 + h - 1, color)
        for y in range(h):
            if 'l' in sides:
                cv.put(x0, y0 + y, color)
            if 'r' in sides:
                cv.put(x0 + w - 1, y0 + y, color)
    return draw
