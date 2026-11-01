"""Cross-checks Java registrations against generated assets/data."""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
J = os.path.join(ROOT, 'src/main/java/com/ditsjohann/mayhem')
JC = os.path.join(ROOT, 'src/client/java/com/ditsjohann/mayhem')
A = os.path.join(ROOT, 'src/main/resources/assets/mayhem')
D = os.path.join(ROOT, 'src/main/resources/data/mayhem')
errors = []


def src(path):
    return open(path).read()


def all_java():
    for base in (J, JC):
        for dp, _, fs in os.walk(base):
            for f in fs:
                if f.endswith('.java'):
                    yield os.path.join(dp, f)


lang = json.load(open(os.path.join(A, 'lang/en_us.json')))
items_src = src(os.path.join(J, 'registry/ModItems.java'))
blocks_src = src(os.path.join(J, 'registry/ModBlocks.java'))
items = re.findall(r'register\("([a-z_]+)"', items_src) + re.findall(r'registerHidden\("([a-z_]+)"', items_src)
items += [e + '_spawn_egg' for e in re.findall(r'egg\("([a-z_]+)"', items_src)]
blocks = re.findall(r'register\("([a-z_]+)"', blocks_src)
for it in items:
    if not os.path.exists(os.path.join(A, 'models/item', it + '.json')):
        errors.append('missing item model ' + it)
    if 'item.mayhem.' + it not in lang:
        errors.append('missing lang item ' + it)
    model = json.load(open(os.path.join(A, 'models/item', it + '.json'))) if os.path.exists(os.path.join(A, 'models/item', it + '.json')) else {}
    for tex in model.get('textures', {}).values():
        if tex.startswith('mayhem:'):
            p = os.path.join(A, 'textures', tex.split(':')[1] + '.png')
            if not os.path.exists(p):
                errors.append('missing texture %s for %s' % (tex, it))
for b in blocks:
    if not os.path.exists(os.path.join(A, 'blockstates', b + '.json')):
        errors.append('missing blockstate ' + b)
    if 'block.mayhem.' + b not in lang:
        errors.append('missing lang block ' + b)
    if not os.path.exists(os.path.join(D, 'loot_table/blocks', b + '.json')):
        errors.append('missing loot table ' + b)
for dp, _, fs in os.walk(os.path.join(A, 'models/block')):
    for f in fs:
        model = json.load(open(os.path.join(dp, f)))
        for tex in model.get('textures', {}).values():
            if tex.startswith('mayhem:') and not os.path.exists(os.path.join(A, 'textures', tex.split(':')[1] + '.png')):
                errors.append('missing block texture %s in %s' % (tex, f))
ents = re.findall(r'register\("([a-z_]+)"', src(os.path.join(J, 'registry/ModEntities.java'))) + re.findall(r'projectile\("([a-z_]+)"', src(os.path.join(J, 'registry/ModEntities.java')))
for e in ents:
    if 'entity.mayhem.' + e not in lang:
        errors.append('missing lang entity ' + e)
for e in re.findall(r'register\("([a-z_]+)"', src(os.path.join(J, 'registry/ModEffects.java'))):
    if 'effect.mayhem.' + e not in lang:
        errors.append('missing lang effect ' + e)
    if not os.path.exists(os.path.join(A, 'textures/mob_effect', e + '.png')):
        errors.append('missing effect icon ' + e)
sounds = json.load(open(os.path.join(A, 'sounds.json')))
for s in re.findall(r'register(?:Reference)?\("([a-z_.]+)"\)', src(os.path.join(J, 'registry/ModSounds.java'))):
    if s not in sounds:
        errors.append('missing sounds.json event ' + s)
for ev in sounds.values():
    for snd in ev['sounds']:
        n = snd['name'].split(':')[1]
        if not os.path.exists(os.path.join(A, 'sounds', n + '.ogg')):
            errors.append('missing ogg ' + n)
# translation keys used literally in code
for path in all_java():
    for key in re.findall(r'Text\.translatable\("([a-z0-9_.]+)"', src(path)):
        if key not in lang and not key.endswith((".", "page")):
            errors.append('missing lang key %s (%s)' % (key, os.path.basename(path)))
for path in all_java():
    for tex in re.findall(r'tex\("([a-z_]+)"\)', src(path)):
        if not os.path.exists(os.path.join(A, 'textures/entity', tex + '.png')):
            errors.append('missing entity texture ' + tex)
    for tex in re.findall(r'id\("(textures/[a-z_/]+\.png)"\)', src(path)):
        if not os.path.exists(os.path.join(A, tex)):
            errors.append('missing texture ' + tex)
print('\n'.join(errors) if errors else 'all assets accounted for')
sys.exit(1 if errors else 0)
