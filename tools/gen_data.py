"""Generates every JSON asset and data file for Multiverse Mayhem.

Run from the repo root:  python3 tools/gen_data.py
"""
import copy
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'src/main/resources')
A = os.path.join(RES, 'assets/mayhem')
D = os.path.join(RES, 'data/mayhem')
MC = os.path.join(RES, 'data/minecraft')
VANILLA = os.environ.get('VANILLA_DATA', '/root/vanilla/data/minecraft')
NS = 'mayhem'


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')


def m(x):
    return x if ':' in x else NS + ':' + x


# =========================================================== registry mirrors
CUBE_ALL = ['flux_crystal_ore', 'deepslate_flux_crystal_ore', 'flux_block', 'dark_matter_block', 'gloop_block', 'gloopstone',
            'gloopstone_bricks', 'flesh_block', 'eye_flesh', 'gristle', 'dark_matter_ore']
CROSS = ['wobble_shroom', 'flux_cluster']
ALL_BLOCKS = CUBE_ALL + CROSS + ['gloop_grass', 'landing_pad', 'interdimensional_tv']

FLAT_ITEMS = ['flux_crystal', 'dark_matter', 'gloop_ball', 'portal_fluid', 'watcher_eye', 'omni_lens', 'raw_mystery_meat',
              'cooked_mystery_meat', 'burnt_toast', 'hyper_seed', 'wobble_stew', 'portal_orb', 'plasma_orb', 'eye_beam_orb',
              'coordinate_card', 'mystery_jar', 'toaster_core', 'eye_bait', 'microverse_battery', 'dimensional_permit',
              'lab_goggles', 'gloop_boots', 'music_disc_interdimensional_jam', 'swapper', 'pocket_black_hole', 'plasma_blaster', 'size_ray']
HANDHELD = ['flux_sword', 'flux_pickaxe', 'flux_axe', 'flux_shovel', 'stun_baton']
MOBS = ['glooper', 'meat_crawler', 'watcher', 'customs_agent', 'zorp', 'defense_toaster', 'thought_leech', 'omni_eye']

# =========================================================== assets
def blockstates_and_models():
    bs = os.path.join(A, 'blockstates')
    bm = os.path.join(A, 'models/block')
    im = os.path.join(A, 'models/item')
    for b in CUBE_ALL:
        w(os.path.join(bs, b + '.json'), {'variants': {'': {'model': m('block/' + b)}}})
        w(os.path.join(bm, b + '.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': m('block/' + b)}})
        w(os.path.join(im, b + '.json'), {'parent': m('block/' + b)})
    for b in CROSS:
        w(os.path.join(bs, b + '.json'), {'variants': {'': {'model': m('block/' + b)}}})
        w(os.path.join(bm, b + '.json'), {'parent': 'minecraft:block/cross', 'render_type': 'minecraft:cutout', 'textures': {'cross': m('block/' + b)}})
        w(os.path.join(im, b + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': m('block/' + b)}})
    w(os.path.join(bs, 'gloop_grass.json'), {'variants': {'': [{'model': m('block/gloop_grass')}, {'model': m('block/gloop_grass'), 'y': 90},
                                                              {'model': m('block/gloop_grass'), 'y': 180}, {'model': m('block/gloop_grass'), 'y': 270}]}})
    w(os.path.join(bm, 'gloop_grass.json'), {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
        'top': m('block/gloop_grass_top'), 'side': m('block/gloop_grass_side'), 'bottom': m('block/gloopstone')}})
    w(os.path.join(im, 'gloop_grass.json'), {'parent': m('block/gloop_grass')})
    w(os.path.join(bs, 'landing_pad.json'), {'variants': {'': {'model': m('block/landing_pad')}}})
    w(os.path.join(bm, 'landing_pad.json'), {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
        'top': m('block/landing_pad'), 'side': m('block/landing_pad_side'), 'bottom': m('block/landing_pad_side')}})
    w(os.path.join(im, 'landing_pad.json'), {'parent': m('block/landing_pad')})

    # Interdimensional TV: cabinet + bezel screen + rabbit-ear antennae
    tv = {
        'parent': 'minecraft:block/block',
        'textures': {'particle': m('block/tv_cabinet'), 'cabinet': m('block/tv_cabinet'), 'antenna': m('block/tv_antenna'), 'screen': m('block/tv_screen_0')},
        'elements': [
            {'from': [1, 1, 2], 'to': [15, 14, 15], 'faces': {
                'north': {'uv': [0, 0, 16, 16], 'texture': '#screen'},
                'south': {'uv': [1, 2, 15, 15], 'texture': '#cabinet'},
                'east': {'uv': [1, 2, 14, 15], 'texture': '#cabinet'},
                'west': {'uv': [2, 2, 15, 15], 'texture': '#cabinet'},
                'up': {'uv': [1, 2, 15, 15], 'texture': '#cabinet'},
                'down': {'uv': [1, 2, 15, 15], 'texture': '#cabinet', 'cullface': 'down'}}},
            {'from': [2, 0, 3], 'to': [4, 1, 5], 'faces': {f: {'uv': [0, 0, 2, 2], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
            {'from': [12, 0, 3], 'to': [14, 1, 5], 'faces': {f: {'uv': [0, 0, 2, 2], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
            {'from': [2, 0, 12], 'to': [4, 1, 14], 'faces': {f: {'uv': [0, 0, 2, 2], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
            {'from': [12, 0, 12], 'to': [14, 1, 14], 'faces': {f: {'uv': [0, 0, 2, 2], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
            {'from': [6, 14, 8], 'to': [10, 15, 11], 'faces': {f: {'uv': [0, 0, 4, 3], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
            {'from': [7.5, 15, 9], 'to': [8.5, 23, 10], 'rotation': {'angle': 22.5, 'axis': 'z', 'origin': [8, 15, 9.5]},
             'faces': {f: {'uv': [0, 0, 1, 8], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
            {'from': [7.5, 15, 9], 'to': [8.5, 23, 10], 'rotation': {'angle': -22.5, 'axis': 'z', 'origin': [8, 15, 9.5]},
             'faces': {f: {'uv': [0, 0, 1, 8], 'texture': '#antenna'} for f in ('north', 'south', 'east', 'west', 'up', 'down')}},
        ],
    }
    w(os.path.join(bm, 'interdimensional_tv.json'), tv)
    variants = {}
    for i in range(4):
        w(os.path.join(bm, 'interdimensional_tv_%d.json' % i), {'parent': m('block/interdimensional_tv'), 'textures': {'screen': m('block/tv_screen_%d' % i)}})
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            v = {'model': m('block/interdimensional_tv_%d' % i)}
            if y:
                v['y'] = y
            variants['facing=%s,screen=%d' % (facing, i)] = v
    w(os.path.join(bs, 'interdimensional_tv.json'), {'variants': variants})
    w(os.path.join(im, 'interdimensional_tv.json'), {'parent': m('block/interdimensional_tv_2')})

    for it in FLAT_ITEMS:
        w(os.path.join(im, it + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': m('item/' + it)}})
    for it in HANDHELD:
        w(os.path.join(im, it + '.json'), {'parent': 'minecraft:item/handheld', 'textures': {'layer0': m('item/' + it)}})
    for mob in MOBS:
        w(os.path.join(im, mob + '_spawn_egg.json'), {'parent': 'minecraft:item/template_spawn_egg'})
    portal_gun_models(im)


def box(fr, to, uv_map, tex='#gun', rot=None):
    """uv_map: dict face -> [u0,v0,u1,v1] or a single list used for all faces."""
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        uv = uv_map[f] if isinstance(uv_map, dict) else uv_map
        faces[f] = {'uv': uv, 'texture': tex}
    e = {'from': fr, 'to': to, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def portal_gun_models(im):
    SHELL = [0, 0, 8, 8]
    ACCENT = [8, 0, 16, 4]
    GRIP = [8, 4, 16, 8]
    TUBE = [0, 8, 8, 12]
    EMIT = [8, 8, 16, 12]
    GLOW = [0, 12, 4, 16]
    KNOB = [4, 12, 8, 16]
    PANEL = [8, 12, 16, 16]
    elements = [
        box([3, 7, 6.5], [11, 11, 9.5], SHELL),                 # main body
        box([2, 7.5, 7], [3, 10.5, 9], ACCENT),                  # rear cap
        box([11, 7.5, 7], [12, 10.5, 9], ACCENT),                # front ring
        box([12, 8, 7.25], [14, 10, 8.75], EMIT),                # emitter barrel
        box([14, 8.25, 7.5], [15, 9.75, 8.5], GLOW),             # glowing tip
        box([5, 11, 7.25], [10, 12.5, 8.75], TUBE),              # fluid tube on top
        box([4.5, 10.75, 7], [5.5, 12.75, 9], ACCENT),           # tube clamp
        box([9.5, 10.75, 7], [10.5, 12.75, 9], ACCENT),          # tube clamp
        box([4, 3, 7], [6, 7, 9], GRIP, rot={'angle': 22.5, 'axis': 'z', 'origin': [5, 7, 8]}),  # handle
        box([6, 6, 7.5], [7, 7, 8.5], KNOB),                     # trigger
        box([7, 9, 9.5], [10, 10, 10], PANEL),                   # side panel
        box([8, 11, 8.75], [9, 12, 9.5], KNOB),                  # dial knob
    ]
    display = {
        'thirdperson_righthand': {'rotation': [0, -90, 0], 'translation': [0, 1.5, 1], 'scale': [0.9, 0.9, 0.9]},
        'thirdperson_lefthand': {'rotation': [0, 90, 0], 'translation': [0, 1.5, 1], 'scale': [0.9, 0.9, 0.9]},
        'firstperson_righthand': {'rotation': [0, -90, 5], 'translation': [1.5, 2.5, 0], 'scale': [0.8, 0.8, 0.8]},
        'firstperson_lefthand': {'rotation': [0, 90, -5], 'translation': [1.5, 2.5, 0], 'scale': [0.8, 0.8, 0.8]},
        'ground': {'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
        'gui': {'rotation': [20, -135, 0], 'translation': [0.5, -1, 0], 'scale': [0.95, 0.95, 0.95]},
        'fixed': {'rotation': [0, 180, 0], 'scale': [1, 1, 1]},
        'head': {'rotation': [0, 180, 0], 'translation': [0, 13, 7]},
    }
    for name in ('portal_gun', 'omni_portal_gun'):
        w(os.path.join(im, name + '.json'), {'textures': {'gun': m('item/' + name), 'particle': m('item/' + name)},
                                             'elements': elements, 'display': display, 'gui_light': 'front'})


def sounds():
    def ev(key, files, subtitle, stream=False):
        sounds_ = []
        for f in files:
            s = {'name': m(f)}
            if stream:
                s['stream'] = True
            sounds_.append(s)
        return key, {'sounds': sounds_, 'subtitle': 'subtitles.mayhem.' + key}
    table = [
        ('item.portal_gun.fire', ['portal_gun_fire'], 'Portal gun fires'),
        ('item.portal_gun.empty', ['portal_gun_empty'], 'Portal gun clicks empty'),
        ('item.portal_gun.mode', ['portal_gun_mode'], 'Portal gun beeps'),
        ('entity.portal.open', ['portal_open'], 'Portal opens'),
        ('entity.portal.close', ['portal_close'], 'Portal closes'),
        ('entity.portal.travel', ['portal_travel'], 'Something travels by portal'),
        ('item.plasma_blaster.fire', ['plasma_shot'], 'Plasma blaster fires'),
        ('item.size_ray.zap', ['size_ray'], 'Size ray zaps'),
        ('item.swapper.swap', ['swapper'], 'Quantum swap'),
        ('entity.black_hole.hum', ['black_hole_hum'], 'Singularity hums'),
        ('entity.black_hole.collapse', ['black_hole_collapse'], 'Singularity collapses'),
        ('item.mystery_jar.open', ['jar_open'], 'Mystery jar pops open'),
        ('block.interdimensional_tv.channel', ['tv_channel'], 'TV changes channel'),
        ('entity.glooper.ambient', ['glooper_ambient1', 'glooper_ambient2'], 'Glooper burbles'),
        ('entity.glooper.hurt', ['glooper_hurt'], 'Glooper squelches'),
        ('entity.glooper.death', ['glooper_death'], 'Glooper deflates'),
        ('entity.meat_crawler.ambient', ['crawler_ambient'], 'Meat Crawler skitters'),
        ('entity.meat_crawler.hurt', ['crawler_hurt'], 'Meat Crawler squishes'),
        ('entity.meat_crawler.death', ['crawler_death'], 'Meat Crawler splatters'),
        ('entity.watcher.ambient', ['watcher_ambient'], 'Watcher stares'),
        ('entity.watcher.hurt', ['watcher_hurt'], 'Watcher winces'),
        ('entity.watcher.death', ['watcher_death'], 'Watcher pops'),
        ('entity.watcher.shoot', ['watcher_shoot'], 'Watcher glares'),
        ('entity.customs_agent.ambient', ['agent_ambient1', 'agent_ambient2'], 'Customs Agent mutters'),
        ('entity.customs_agent.hurt', ['agent_hurt'], 'Customs Agent grunts'),
        ('entity.customs_agent.death', ['agent_death'], 'Customs Agent clocks out'),
        ('entity.customs_agent.alert', ['agent_alert'], 'Customs siren'),
        ('entity.zorp.ambient', ['zorp_ambient1', 'zorp_ambient2'], 'Zorp babbles'),
        ('entity.zorp.yes', ['zorp_yes'], 'Zorp approves'),
        ('entity.zorp.no', ['zorp_no'], 'Zorp disapproves'),
        ('entity.defense_toaster.pop', ['toaster_pop'], 'Toaster fires toast'),
        ('entity.defense_toaster.ambient', ['toaster_ambient'], 'Toaster dings'),
        ('entity.defense_toaster.hurt', ['toaster_hurt'], 'Toaster clanks'),
        ('entity.thought_leech.attach', ['leech_attach'], 'Thought Leech latches on'),
        ('entity.thought_leech.ambient', ['leech_ambient'], 'Thought Leech slurps'),
        ('entity.omni_eye.roar', ['omni_roar'], 'Omni-Eye roars'),
        ('entity.omni_eye.hurt', ['omni_hurt'], 'Omni-Eye flinches'),
        ('entity.omni_eye.death', ['omni_death'], 'Omni-Eye dies'),
        ('entity.omni_eye.laser', ['omni_laser'], 'Omni-Eye fires beams'),
    ]
    out = {}
    subs = {}
    for key, files, sub in table:
        k, v = ev(key, files, sub)
        out[k] = v
        subs['subtitles.mayhem.' + key] = sub
    out['music_disc.interdimensional_jam'] = {'sounds': [{'name': m('music_disc_jam'), 'stream': True}]}
    w(os.path.join(A, 'sounds.json'), out)
    return subs


# =========================================================== lang
def lang(subs):
    L = {}
    names = {
        'flux_crystal_ore': 'Flux Crystal Ore', 'deepslate_flux_crystal_ore': 'Deepslate Flux Crystal Ore', 'flux_block': 'Block of Flux',
        'dark_matter_block': 'Block of Dark Matter', 'gloop_block': 'Gloop Block', 'gloopstone': 'Gloopstone', 'gloopstone_bricks': 'Gloopstone Bricks',
        'gloop_grass': 'Gloop Grass', 'wobble_shroom': 'Wobble Shroom', 'flux_cluster': 'Flux Cluster', 'flesh_block': 'Flesh',
        'eye_flesh': 'Eye Flesh', 'gristle': 'Gristle', 'dark_matter_ore': 'Dark Matter Ore', 'interdimensional_tv': 'Interdimensional TV',
        'landing_pad': 'Emergency Landing Pad',
    }
    for k, v in names.items():
        L['block.mayhem.' + k] = v
    items = {
        'flux_crystal': 'Flux Crystal', 'dark_matter': 'Dark Matter', 'gloop_ball': 'Gloop Ball', 'portal_fluid': 'Portal Fluid',
        'watcher_eye': 'Watcher Eye', 'omni_lens': 'Omni-Lens', 'raw_mystery_meat': 'Raw Mystery Meat', 'cooked_mystery_meat': 'Cooked Mystery Meat',
        'burnt_toast': 'Burnt Toast', 'hyper_seed': 'Hyper Seed', 'wobble_stew': 'Wobble Stew', 'portal_orb': 'Portal Blob',
        'plasma_orb': 'Plasma Bolt', 'eye_beam_orb': 'Eye Beam', 'portal_gun': 'Portal Gun', 'omni_portal_gun': 'Omni Portal Gun',
        'coordinate_card': 'Coordinate Card', 'size_ray': 'Size Ray', 'swapper': 'Quantum Swapper', 'plasma_blaster': 'Plasma Blaster',
        'pocket_black_hole': 'Pocket Black Hole', 'mystery_jar': 'Jar of Questionable Contents', 'toaster_core': 'Toaster Core',
        'eye_bait': 'Eye Bait', 'microverse_battery': 'Microverse Battery', 'dimensional_permit': 'Dimensional Permit',
        'stun_baton': 'Customs Stun Baton', 'flux_sword': 'Flux Sword', 'flux_pickaxe': 'Flux Pickaxe', 'flux_axe': 'Flux Axe',
        'flux_shovel': 'Flux Shovel', 'lab_goggles': 'Lab Goggles', 'gloop_boots': 'Gloop Boots',
        'music_disc_interdimensional_jam': 'Music Disc', 'lab_notes': 'Lab Notes',
    }
    for k, v in items.items():
        L['item.mayhem.' + k] = v
    mobs = {'glooper': 'Glooper', 'meat_crawler': 'Meat Crawler', 'watcher': 'Watcher', 'customs_agent': 'Customs Agent', 'zorp': 'Zorp',
            'defense_toaster': 'Defense Toaster', 'thought_leech': 'Thought Leech', 'omni_eye': 'The Omni-Eye'}
    for k, v in mobs.items():
        L['entity.mayhem.' + k] = v
        L['item.mayhem.%s_spawn_egg' % k] = v + ' Spawn Egg'
    for k, v in {'portal': 'Portal', 'black_hole': 'Black Hole', 'portal_shot': 'Portal Blob', 'plasma_bolt': 'Plasma Bolt', 'toast': 'Toast',
                 'eye_beam': 'Eye Beam', 'thrown_black_hole': 'Pocket Black Hole'}.items():
        L['entity.mayhem.' + k] = v
    L['jukebox_song.mayhem.interdimensional_jam'] = 'Glorbo - Interdimensional Jam'
    L['itemGroup.mayhem'] = 'Multiverse Mayhem'
    L['effect.mayhem.shrunk'] = 'Shrunk'
    L['effect.mayhem.enlarged'] = 'Enlarged'
    L['effect.mayhem.hyper'] = 'Hyper'
    L['effect.mayhem.brain_drain'] = 'Brain Drain'
    L['biome.mayhem.gloop_highlands'] = 'Gloop Highlands'
    L['biome.mayhem.jelly_shallows'] = 'Jelly Shallows'
    L['biome.mayhem.flesh_caverns'] = 'Flesh Caverns'
    L['biome.mayhem.eye_gardens'] = 'Eye Gardens'

    tips = {
        'flux_crystal': ['Crackles with raw interdimensional energy.'],
        'dark_matter': ['Heavier than it looks. It also looks back.'],
        'gloop_ball': ['Wobbly. Sticky. Faintly smells of grape.'],
        'portal_fluid': ['Refills a Portal Gun with 4 charges.', 'Do not drink. Again.'],
        'watcher_eye': ['Still blinking.'],
        'omni_lens': ['The pupil of a god-thing.', 'Upgrades a Portal Gun into an Omni Portal Gun.'],
        'raw_mystery_meat': ['From an animal. Probably.'],
        'cooked_mystery_meat': ['Tastes like chicken from another dimension.'],
        'burnt_toast': ['Lightly carbonised. Heavily weaponised.'],
        'hyper_seed': ['Grants HYPER for a minute.', 'The crash afterwards is your problem.'],
        'wobble_stew': ['Soothing. Makes you float a little.'],
        'swapper': ['Use: swap places with the creature you aim at.', 'Range 32 blocks.'],
        'plasma_blaster': ['Fires superheated flux plasma.', 'Repair with Flux Crystals.'],
        'pocket_black_hole': ['Throw to open a short-lived singularity.', 'It swallows items and spits them back out.'],
        'mystery_jar': ['Use to open. Contents: yes.', 'Results may vary. Wildly.'],
        'toaster_core': ['Use on the ground to boot up a Defense Toaster.', 'It shoots burnt toast at monsters.'],
        'eye_bait': ['Use in the Meatverse to summon THE OMNI-EYE.', 'Bring friends. And armour.'],
        'microverse_battery': ['A tiny universe whose people pedal to power your gadgets.', 'Slowly recharges Portal Guns in your inventory.'],
        'dimensional_permit': ['Licensed for unlimited portal use.', 'Customs Agents leave you alone while you carry this.'],
        'stun_baton': ['Hits slow your target to a bureaucratic crawl.'],
        'flux_sword': ['Hits arc lightning to up to 3 nearby monsters.'],
        'flux_pickaxe': ['Mines 3x3. Sneak to mine a single block.'],
        'lab_goggles': ['Night vision while worn. Safety first.'],
        'gloop_boots': ['No fall damage. Extra bouncy.'],
        'size_ray': ['Use: zap a creature. Sneak-use: change mode.', 'Look straight down to zap yourself.'],
    }
    for k, lines in tips.items():
        for i, line in enumerate(lines):
            L['tooltip.mayhem.%s.%d' % (k, i)] = line
    L.update({
        'tooltip.mayhem.portal_gun.destination': 'Destination: %s',
        'tooltip.mayhem.portal_gun.charge': 'Charge: %s / %s',
        'tooltip.mayhem.portal_gun.infinite': 'Charge: infinite (powered by the Omni-Lens)',
        'tooltip.mayhem.portal_gun.help0': 'Use: fire a portal',
        'tooltip.mayhem.portal_gun.help1': 'Sneak + Use: change destination',
        'tooltip.mayhem.portal_gun.help2': 'Hold a Coordinate Card in the other hand to go to its location',
        'tooltip.mayhem.coordinate_card.stored': 'Stored: %s, %s, %s',
        'tooltip.mayhem.coordinate_card.blank': 'Blank',
        'tooltip.mayhem.coordinate_card.help': 'Use on a block to record it. Sneak-use to wipe.',
        'tooltip.mayhem.size_ray.mode': 'Mode: %s',
        'destination.mayhem.home': 'Home',
        'destination.mayhem.overworld': 'Overworld',
        'destination.mayhem.nether': 'The Nether',
        'destination.mayhem.gloopverse': 'The Gloopverse',
        'destination.mayhem.meatverse': 'The Meatverse',
        'destination.mayhem.scramble': 'Scramble (random!)',
        'destination.mayhem.end': 'The End',
        'message.mayhem.destination': 'Destination set: %s',
        'message.mayhem.portal_failed': 'The portal fizzles. That destination does not exist in this reality.',
        'message.mayhem.reloaded': 'Portal Fluid loaded (+4 charges)',
        'message.mayhem.no_fluid': 'Out of Portal Fluid!',
        'message.mayhem.card_saved': 'Coordinates recorded: %s %s %s in %s',
        'message.mayhem.card_cleared': 'Coordinate Card wiped.',
        'message.mayhem.size_mode.0': 'Shrink',
        'message.mayhem.size_mode.1': 'Enlarge',
        'message.mayhem.size_mode.2': 'Restore',
        'message.mayhem.confiscated': 'A Customs Agent confiscated your Portal Fluid!',
        'message.mayhem.leech_attached': 'A Thought Leech latched onto your head! Sneak rapidly to shake it off!',
        'message.mayhem.leech_shaken': 'You shook off the Thought Leech!',
        'message.mayhem.toaster_online': 'Defense Toaster online. Toast mode: LETHAL.',
        'message.mayhem.bait_wrong_place': 'The bait needs the stench of the Meatverse to work.',
        'message.mayhem.omni_summoned': 'THE OMNI-EYE HAS NOTICED YOU.',
        'message.mayhem.omni_enraged': 'The Omni-Eye turns blood red. It is VERY angry now.',
        'message.mayhem.omni_gaze': 'The Omni-Eye drags you into its gaze!',
        'message.mayhem.zorp_arrives': 'Zorp the interdimensional salesman followed your portal signature. Bargains!',
        'message.mayhem.low_gravity': 'Low gravity! Everything feels... wobbly.',
        'message.mayhem.customs.0': '[Customs] Unlicensed reality breach detected. Please remain where you are.',
        'message.mayhem.customs.1': '[Customs] Sir/Madam/Entity, we need to see your portal permit.',
        'message.mayhem.customs.2': '[Customs] You have exceeded your annual quota of holes in spacetime.',
        'message.mayhem.customs.3': '[Customs] This is a routine audit. Resistance will be noted on your file.',
        'message.mayhem.customs.4': '[Customs] Form 77-B, Section 9: no portalling without paperwork!',
        'tv.mayhem.channel': '[Ch. %s]',
        'tv.mayhem.something_crawled_out': 'Something crawled out of the screen...',
    })
    tv = [
        'Cooking With Gravity: "Today we drop a souffle from low orbit. Serves nobody." (A snack falls out of the screen.)',
        'Ask a Sentient Cloud: "Is it raining? I dunno, I\'m just vibing."',
        'INFOMERCIAL: The Inflatable Ladder! Climb ANYTHING (until it pops)!',
        'Breaking News: local dimension reports it is "mostly fine".',
        'Late Night With a Brick: [the brick says nothing for forty minutes]. The audience goes wild.',
        'Nature Documentary: "The Glooper bounces. And bounces. The narrator weeps softly."',
        'Workout With Grandpa Thunder: "Lift with your legs, children of all realities!"',
        'Weather in Dimension 7Q: 80% chance of meatballs. (One lands in your hands.)',
        'Public Service Announcement: "Portal responsibly. Interdimensional Customs is watching." You feel watched.',
        'Kids\' Show: "Count With Mister Eyeball! One. One. One. Onnnne."',
        'Shopping Channel: "Hyper Seeds! Side effects include: everything."',
        'Static... a faint voice whispers: "help, I\'m stuck in the TV..."',
        'Game Show: "Spin the Wheel of Consequences!"',
        'The Philosophy Hour: "If a portal opens and nobody walks through it, is it still a door?"',
    ]
    for i, t in enumerate(tv):
        L['tv.mayhem.broadcast.%d' % i] = t
    jar = [
        'The jar contains... crystals! Shiny ones!',
        'Diamonds?! Who just leaves diamonds in a jar?',
        'Seeds. Suspiciously energetic seeds.',
        'A Glooper pops out and immediately loves you.',
        'Oh no. Leeches. THOUGHT leeches.',
        'CONFETTI! Congratulations on opening a jar!',
        'The jar was full of anti-gravity. Whee!',
        'A shrinking gas! You feel... smaller.',
        'Portal Fluid! Practically a jackpot.',
        'Meat. Cooked, at least.',
        'The jar was empty. It was a metaphor.',
        'Bottled experience! Literally.',
        'Zorp was inside the jar. Zorp would like to sell you things.',
        'A speck of Dark Matter. It stares at you.',
        'It was a bomb jar. Of course it was a bomb jar.',
    ]
    for i, t in enumerate(jar):
        L['message.mayhem.jar.%d' % i] = t
    pages = [
        'LAB NOTES\n\nIf you are reading this, I left in a hurry. Reality is a lot more porous than they teach you.\n\nStep 1: dig deep. Find FLUX CRYSTAL ORE glowing below y=32.',
        'PORTAL FLUID\nFlux Crystal + Slime Ball + Glass Bottle.\n\nPORTAL GUN\nFlux, glass, a diamond, iron and some Portal Fluid. See the recipe book.\n\nEach Portal Fluid = 4 shots.',
        'USING THE GUN\nUse: fire. Wherever the blob lands, a portal opens - and a return portal opens on the other side.\n\nSneak+Use: pick a destination. HOME, the OVERWORLD, the NETHER, THE GLOOPVERSE, THE MEATVERSE, or SCRAMBLE.',
        'COORDINATE CARDS\nUse one on a block to bookmark it. Hold it in your off-hand when you fire to portal straight there.\n\nTip: crashed saucers in the Overworld hide useful salvage.',
        'THE GLOOPVERSE\nFloating islands of jelly. Low gravity. Friendly Gloopers (tame them with Flux Crystals, ride them on your head!). Wobble Shrooms, flux spires.',
        'THE MEATVERSE\nIt is made of meat. Do not sleep there. Meat Crawlers, Watchers and Thought Leeches (sneak quickly to shake one off).\n\nDARK MATTER hides in the gristle deep down.',
        'CUSTOMS\nOpen too many portals and Interdimensional Customs will come for you. They confiscate Portal Fluid.\n\nA DIMENSIONAL PERMIT makes them leave you alone. Zorp sells them. Agents sometimes drop them.',
        'THE OMNI-EYE\nCraft EYE BAIT from Watcher Eyes and use it in the Meatverse. Beat the Eye, take its LENS, and build the OMNI PORTAL GUN: unlimited charge, longer portals, and it can reach the End.',
        'OTHER TOYS\nSize Ray, Quantum Swapper, Plasma Blaster, Pocket Black Holes, Defense Toasters, Interdimensional TV, Mystery Jars...\n\nGood luck. Try not to get audited.',
    ]
    for i, p in enumerate(pages):
        L['book.mayhem.lab_notes.page%d' % i] = p
    L.update(subs)
    return L


# =========================================================== loot tables
SILK = {'condition': 'minecraft:match_tool', 'predicate': {'predicates': {'minecraft:enchantments': [{'enchantments': 'minecraft:silk_touch', 'levels': {'min': 1}}]}}}


def block_self(b):
    return {'type': 'minecraft:block', 'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{'type': 'minecraft:item', 'name': m(b)}],
                                                   'conditions': [{'condition': 'minecraft:survives_explosion'}]}], 'random_sequence': m('blocks/' + b)}


def block_ore(b, drop, count=None):
    fns = []
    if count:
        fns.append({'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': count[0], 'max': count[1]}, 'add': False})
    fns += [{'function': 'minecraft:apply_bonus', 'enchantment': 'minecraft:fortune', 'formula': 'minecraft:ore_drops'}, {'function': 'minecraft:explosion_decay'}]
    return {'type': 'minecraft:block', 'pools': [{'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': m(b), 'conditions': [SILK]},
        {'type': 'minecraft:item', 'name': m(drop), 'functions': fns}]}]}], 'random_sequence': m('blocks/' + b)}


def item_entry(name, mn=1, mx=1, weight=1, looting=None, extra=None):
    e = {'type': 'minecraft:item', 'name': m(name) if not name.startswith('minecraft:') else name, 'weight': weight}
    fns = []
    if (mn, mx) != (1, 1):
        fns.append({'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': mn, 'max': mx}, 'add': False})
    if looting:
        fns.append({'function': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting', 'count': {'type': 'minecraft:uniform', 'min': 0, 'max': looting}})
    if extra:
        fns += extra
    if fns:
        e['functions'] = fns
    return e


def pool(entries, rolls=1, conditions=None):
    p = {'rolls': rolls, 'bonus_rolls': 0.0, 'entries': entries}
    if conditions:
        p['conditions'] = conditions
    return p


def chance(c):
    return {'condition': 'minecraft:random_chance', 'chance': c}


KILLED = {'condition': 'minecraft:killed_by_player'}


def loot():
    lt = os.path.join(D, 'loot_table')
    for b in ['flux_block', 'dark_matter_block', 'gloop_block', 'gloopstone', 'gloopstone_bricks', 'wobble_shroom', 'eye_flesh', 'gristle',
              'interdimensional_tv', 'landing_pad']:
        w(os.path.join(lt, 'blocks', b + '.json'), block_self(b))
    w(os.path.join(lt, 'blocks/flux_crystal_ore.json'), block_ore('flux_crystal_ore', 'flux_crystal'))
    w(os.path.join(lt, 'blocks/deepslate_flux_crystal_ore.json'), block_ore('deepslate_flux_crystal_ore', 'flux_crystal'))
    w(os.path.join(lt, 'blocks/dark_matter_ore.json'), block_ore('dark_matter_ore', 'dark_matter'))
    w(os.path.join(lt, 'blocks/flux_cluster.json'), block_ore('flux_cluster', 'flux_crystal', (1, 2)))
    gg = {'type': 'minecraft:block', 'pools': [pool([{'type': 'minecraft:alternatives', 'children': [
        {'type': 'minecraft:item', 'name': m('gloop_grass'), 'conditions': [SILK]},
        {'type': 'minecraft:item', 'name': m('gloopstone'), 'conditions': [{'condition': 'minecraft:survives_explosion'}]}]}])]}
    w(os.path.join(lt, 'blocks/gloop_grass.json'), gg)
    fl = block_self('flesh_block')
    fl['pools'].append(pool([item_entry('raw_mystery_meat')], conditions=[chance(0.08), {'condition': 'minecraft:inverted', 'term': SILK}]))
    w(os.path.join(lt, 'blocks/flesh_block.json'), fl)

    ent = os.path.join(lt, 'entities')
    w(os.path.join(ent, 'glooper.json'), {'type': 'minecraft:entity', 'pools': [pool([item_entry('gloop_ball', 1, 3, looting=1)])]})
    w(os.path.join(ent, 'meat_crawler.json'), {'type': 'minecraft:entity', 'pools': [
        pool([item_entry('raw_mystery_meat', 0, 2, looting=1)]),
        pool([item_entry('minecraft:bone', 0, 1)])]})
    w(os.path.join(ent, 'watcher.json'), {'type': 'minecraft:entity', 'pools': [
        pool([item_entry('watcher_eye', 1, 1, looting=1)]),
        pool([item_entry('dark_matter')], conditions=[KILLED, chance(0.12)])]})
    w(os.path.join(ent, 'customs_agent.json'), {'type': 'minecraft:entity', 'pools': [
        pool([item_entry('minecraft:paper', 1, 3, looting=1)]),
        pool([item_entry('minecraft:iron_nugget', 1, 4, looting=2)]),
        pool([item_entry('flux_crystal', 1, 2)], conditions=[chance(0.5)]),
        pool([item_entry('dimensional_permit')], conditions=[KILLED, {'condition': 'minecraft:random_chance_with_enchanted_bonus', 'enchantment': 'minecraft:looting',
                                                                          'unenchanted_chance': 0.08, 'enchanted_chance': {'type': 'minecraft:linear', 'base': 0.12, 'per_level_above_first': 0.04}}])]})
    w(os.path.join(ent, 'zorp.json'), {'type': 'minecraft:entity', 'pools': [pool([item_entry('flux_crystal', 1, 3)])]})
    w(os.path.join(ent, 'defense_toaster.json'), {'type': 'minecraft:entity', 'pools': [
        pool([item_entry('minecraft:iron_ingot', 2, 4)]), pool([item_entry('burnt_toast', 1, 3)]), pool([item_entry('toaster_core')], conditions=[chance(0.5)])]})
    w(os.path.join(ent, 'thought_leech.json'), {'type': 'minecraft:entity', 'pools': [
        pool([item_entry('minecraft:ink_sac', 0, 1, looting=1)]), pool([item_entry('minecraft:experience_bottle')], conditions=[chance(0.2)])]})
    w(os.path.join(ent, 'omni_eye.json'), {'type': 'minecraft:entity', 'pools': [
        pool([item_entry('omni_lens')]),
        pool([item_entry('dark_matter', 4, 8, looting=2)]),
        pool([item_entry('watcher_eye', 2, 4)]),
        pool([item_entry('music_disc_interdimensional_jam')], conditions=[chance(0.5)])]})
    w(os.path.join(lt, 'chests/crashed_saucer.json'), {'type': 'minecraft:chest', 'pools': [
        pool([item_entry('portal_fluid', 2, 5, 3), item_entry('flux_crystal', 3, 8, 4), item_entry('coordinate_card', 1, 2, 2),
              item_entry('hyper_seed', 1, 3, 2), item_entry('mystery_jar', 1, 2, 2), item_entry('minecraft:iron_ingot', 2, 6, 3),
              item_entry('burnt_toast', 2, 5, 2), item_entry('minecraft:gold_ingot', 1, 4, 2), item_entry('gloop_ball', 2, 6, 2)],
             rolls={'type': 'minecraft:uniform', 'min': 4, 'max': 7}),
        pool([item_entry('portal_gun', extra=[{'function': 'minecraft:set_components', 'components': {'mayhem:charge': 4}}], weight=1),
              item_entry('size_ray', weight=2), item_entry('toaster_core', weight=3), item_entry('swapper', weight=2),
              item_entry('plasma_blaster', weight=2), {'type': 'minecraft:empty', 'weight': 8}])]})


# =========================================================== recipes
def item(x):
    return {'item': m(x) if ':' not in x else x} if not x.startswith('#') else {'tag': x[1:]}


def shaped(name, pattern, key, result, count=1, category='misc'):
    w(os.path.join(D, 'recipe', name + '.json'), {'type': 'minecraft:crafting_shaped', 'category': category, 'pattern': pattern,
                                                   'key': {k: item(v) for k, v in key.items()}, 'result': {'id': m(result), 'count': count}})


def shapeless(name, ingredients, result, count=1, category='misc'):
    w(os.path.join(D, 'recipe', name + '.json'), {'type': 'minecraft:crafting_shapeless', 'category': category,
                                                   'ingredients': [item(i) for i in ingredients], 'result': {'id': m(result), 'count': count}})


def cook(name, ingredient, result, xp=0.35, kinds=('smelting',), time=200):
    for k in kinds:
        t = time if k in ('smelting',) else (time // 2 if k in ('blasting', 'smoking') else 600)
        w(os.path.join(D, 'recipe', '%s_from_%s.json' % (name, k)), {'type': 'minecraft:' + k, 'category': 'food' if 'meat' in name or 'toast' in name else 'misc',
                                                                        'ingredient': item(ingredient), 'result': {'id': m(result)}, 'experience': xp, 'cookingtime': t})


def recipes():
    shaped('flux_block', ['###', '###', '###'], {'#': 'flux_crystal'}, 'flux_block', category='building')
    shapeless('flux_crystal_from_block', ['flux_block'], 'flux_crystal', 9)
    shaped('dark_matter_block', ['###', '###', '###'], {'#': 'dark_matter'}, 'dark_matter_block', category='building')
    shapeless('dark_matter_from_block', ['dark_matter_block'], 'dark_matter', 9)
    shaped('gloop_block', ['##', '##'], {'#': 'gloop_ball'}, 'gloop_block', category='building')
    shapeless('gloop_ball_from_block', ['gloop_block'], 'gloop_ball', 4)
    shaped('gloopstone_bricks', ['##', '##'], {'#': 'gloopstone'}, 'gloopstone_bricks', 4, 'building')
    cook('flux_crystal', 'flux_crystal_ore', 'flux_crystal', 1.0, ('smelting', 'blasting'))
    cook('flux_crystal_deepslate', 'deepslate_flux_crystal_ore', 'flux_crystal', 1.0, ('smelting', 'blasting'))
    cook('dark_matter', 'dark_matter_ore', 'dark_matter', 2.0, ('smelting', 'blasting'))
    cook('cooked_mystery_meat', 'raw_mystery_meat', 'cooked_mystery_meat', 0.35, ('smelting', 'smoking', 'campfire_cooking'))
    cook('burnt_toast', 'minecraft:bread', 'burnt_toast', 0.1, ('smelting', 'smoking'))
    shapeless('portal_fluid', ['flux_crystal', 'minecraft:slime_ball', 'minecraft:glass_bottle'], 'portal_fluid', 2)
    shapeless('portal_fluid_from_gloop', ['flux_crystal', 'gloop_ball', 'minecraft:glass_bottle'], 'portal_fluid', 3)
    shaped('portal_gun', ['FGP', 'IDI', 'I  '], {'F': 'flux_crystal', 'G': 'minecraft:lime_stained_glass', 'P': 'portal_fluid',
                                                   'I': 'minecraft:iron_ingot', 'D': 'minecraft:diamond'}, 'portal_gun', category='equipment')
    shaped('omni_portal_gun', [' L ', 'DPD', ' B '], {'L': 'omni_lens', 'D': 'dark_matter_block', 'P': 'portal_gun', 'B': 'microverse_battery'},
           'omni_portal_gun', category='equipment')
    shapeless('coordinate_card', ['minecraft:paper', 'flux_crystal', 'minecraft:redstone', 'minecraft:gold_nugget'], 'coordinate_card', 2)
    shaped('size_ray', ['  F', 'IAG', 'I  '], {'F': 'flux_crystal', 'I': 'minecraft:iron_ingot', 'A': 'minecraft:amethyst_shard',
                                               'G': 'minecraft:lime_stained_glass'}, 'size_ray', category='equipment')
    shaped('swapper', ['EFE', 'FPF', 'EFE'], {'E': 'minecraft:ender_pearl', 'F': 'flux_crystal', 'P': 'portal_fluid'}, 'swapper', category='equipment')
    shaped('plasma_blaster', ['IFB', 'IR ', 'I  '], {'I': 'minecraft:iron_ingot', 'F': 'flux_crystal', 'B': 'flux_block', 'R': 'minecraft:redstone'},
           'plasma_blaster', category='equipment')
    shaped('pocket_black_hole', ['OEO', 'EDE', 'OEO'], {'O': 'minecraft:obsidian', 'E': 'minecraft:ender_pearl', 'D': 'dark_matter'}, 'pocket_black_hole', 2)
    shapeless('mystery_jar', ['minecraft:glass_bottle', 'flux_crystal', 'gloop_ball', 'minecraft:rotten_flesh'], 'mystery_jar')
    shapeless('mystery_jar_meat', ['minecraft:glass_bottle', 'flux_crystal', 'raw_mystery_meat', 'minecraft:rotten_flesh'], 'mystery_jar')
    shaped('toaster_core', ['IBI', 'FRF', 'III'], {'I': 'minecraft:iron_ingot', 'B': 'minecraft:bread', 'F': 'flux_crystal', 'R': 'minecraft:redstone_block'},
           'toaster_core')
    shapeless('eye_bait', ['watcher_eye', 'watcher_eye', 'watcher_eye', 'watcher_eye', 'raw_mystery_meat', 'dark_matter', 'minecraft:bone'], 'eye_bait')
    shaped('microverse_battery', ['IRI', 'DFD', 'IRI'], {'I': 'minecraft:iron_ingot', 'R': 'minecraft:redstone_block', 'D': 'dark_matter', 'F': 'flux_block'},
           'microverse_battery')
    shaped('flux_sword', ['F', 'F', 'S'], {'F': 'flux_crystal', 'S': 'minecraft:stick'}, 'flux_sword', category='equipment')
    shaped('flux_pickaxe', ['FFF', ' S ', ' S '], {'F': 'flux_crystal', 'S': 'minecraft:stick'}, 'flux_pickaxe', category='equipment')
    shaped('flux_axe', ['FF', 'FS', ' S'], {'F': 'flux_crystal', 'S': 'minecraft:stick'}, 'flux_axe', category='equipment')
    shaped('flux_shovel', ['F', 'S', 'S'], {'F': 'flux_crystal', 'S': 'minecraft:stick'}, 'flux_shovel', category='equipment')
    shaped('lab_goggles', ['LIL', 'G G'], {'L': 'minecraft:leather', 'I': 'minecraft:iron_ingot', 'G': 'minecraft:lime_stained_glass_pane'},
           'lab_goggles', category='equipment')
    shaped('gloop_boots', ['G G', 'G G'], {'G': 'gloop_block'}, 'gloop_boots', category='equipment')
    shaped('interdimensional_tv', ['SAS', 'WGW', 'WRW'], {'S': 'minecraft:stick', 'A': 'flux_crystal', 'W': '#minecraft:planks',
                                                          'G': 'minecraft:glass_pane', 'R': 'minecraft:redstone'}, 'interdimensional_tv')
    shaped('landing_pad', ['ggg', 'III'], {'g': 'minecraft:glowstone_dust', 'I': 'minecraft:iron_ingot'}, 'landing_pad', 4, 'building')
    shapeless('wobble_stew', ['minecraft:bowl', 'wobble_shroom', 'wobble_shroom', 'gloop_ball'], 'wobble_stew')
    shapeless('hyper_seed', ['minecraft:wheat_seeds', 'flux_crystal', 'minecraft:sugar', 'wobble_shroom'], 'hyper_seed', 2)


# =========================================================== tags
def tags():
    t = os.path.join(MC, 'tags')
    w(os.path.join(t, 'block/mineable/pickaxe.json'), {'replace': False, 'values': [m(x) for x in [
        'flux_crystal_ore', 'deepslate_flux_crystal_ore', 'flux_block', 'dark_matter_block', 'gloopstone', 'gloopstone_bricks',
        'gristle', 'dark_matter_ore', 'landing_pad', 'flux_cluster']]})
    w(os.path.join(t, 'block/mineable/shovel.json'), {'replace': False, 'values': [m(x) for x in ['flesh_block', 'eye_flesh', 'gloop_grass']]})
    w(os.path.join(t, 'block/mineable/axe.json'), {'replace': False, 'values': [m('interdimensional_tv')]})
    w(os.path.join(t, 'block/needs_iron_tool.json'), {'replace': False, 'values': [m(x) for x in ['flux_crystal_ore', 'deepslate_flux_crystal_ore', 'flux_block']]})
    w(os.path.join(t, 'block/needs_diamond_tool.json'), {'replace': False, 'values': [m(x) for x in ['dark_matter_ore', 'dark_matter_block']]})
    w(os.path.join(t, 'item/swords.json'), {'replace': False, 'values': [m('flux_sword'), m('stun_baton')]})
    w(os.path.join(t, 'item/pickaxes.json'), {'replace': False, 'values': [m('flux_pickaxe')]})
    w(os.path.join(t, 'item/axes.json'), {'replace': False, 'values': [m('flux_axe')]})
    w(os.path.join(t, 'item/shovels.json'), {'replace': False, 'values': [m('flux_shovel')]})
    w(os.path.join(t, 'item/head_armor.json'), {'replace': False, 'values': [m('lab_goggles')]})
    w(os.path.join(t, 'item/foot_armor.json'), {'replace': False, 'values': [m('gloop_boots')]})
    w(os.path.join(t, 'item/enchantable/durability.json'), {'replace': False, 'values': [m(x) for x in ['size_ray', 'swapper', 'plasma_blaster']]})
    w(os.path.join(t, 'item/meat.json'), {'replace': False, 'values': [m('raw_mystery_meat'), m('cooked_mystery_meat')]})
    w(os.path.join(D, 'tags/worldgen/biome/gloopverse.json'), {'values': [m('gloop_highlands'), m('jelly_shallows')]})
    w(os.path.join(D, 'tags/worldgen/biome/meatverse.json'), {'values': [m('flesh_caverns'), m('eye_gardens')]})


# =========================================================== worldgen
def state(name, props=None):
    s = {'Name': m(name) if ':' not in name else name}
    if props:
        s['Properties'] = props
    return s


def block_rule(name):
    return {'type': 'minecraft:block', 'result_state': state(name)}


def cond(if_true, then_run):
    return {'type': 'minecraft:condition', 'if_true': if_true, 'then_run': then_run}


def floor(offset=0, add=False):
    return {'type': 'minecraft:stone_depth', 'offset': offset, 'surface_type': 'floor', 'add_surface_depth': add, 'secondary_depth_range': 0}


def ceiling(offset=0, add=False):
    return {'type': 'minecraft:stone_depth', 'offset': offset, 'surface_type': 'ceiling', 'add_surface_depth': add, 'secondary_depth_range': 0}


def noise_thr(lo, hi, noise='minecraft:surface'):
    return {'type': 'minecraft:noise_threshold', 'noise': noise, 'min_threshold': lo, 'max_threshold': hi}


def biome_is(*b):
    return {'type': 'minecraft:biome', 'biome_is': [m(x) for x in b]}


def worldgen():
    wg = os.path.join(D, 'worldgen')
    # ---------------- dimension types
    w(os.path.join(D, 'dimension_type/gloopverse.json'), {
        'ambient_light': 0.08, 'bed_works': True, 'coordinate_scale': 1.0, 'effects': 'minecraft:overworld', 'fixed_time': 12800,
        'has_ceiling': False, 'has_raids': False, 'has_skylight': True, 'height': 256, 'infiniburn': '#minecraft:infiniburn_overworld',
        'logical_height': 256, 'min_y': 0, 'monster_spawn_block_light_limit': 0, 'monster_spawn_light_level': {
            'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7}, 'natural': True, 'piglin_safe': False,
        'respawn_anchor_works': False, 'ultrawarm': False})
    w(os.path.join(D, 'dimension_type/meatverse.json'), {
        'ambient_light': 0.18, 'bed_works': False, 'coordinate_scale': 1.0, 'effects': 'minecraft:the_nether', 'fixed_time': 18000,
        'has_ceiling': True, 'has_raids': False, 'has_skylight': False, 'height': 192, 'infiniburn': '#minecraft:infiniburn_overworld',
        'logical_height': 192, 'min_y': -64, 'monster_spawn_block_light_limit': 15, 'monster_spawn_light_level': 15, 'natural': False,
        'piglin_safe': False, 'respawn_anchor_works': False, 'ultrawarm': False})

    # ---------------- noise settings (based on vanilla density functions)
    fi = json.load(open(os.path.join(VANILLA, 'worldgen/noise_settings/floating_islands.json')))
    fi['default_block'] = state('gloopstone')
    fi['default_fluid'] = state('minecraft:water', {'level': '0'})
    fi['sea_level'] = 0
    fi['surface_rule'] = {'type': 'minecraft:sequence', 'sequence': [
        cond(floor(), {'type': 'minecraft:sequence', 'sequence': [
            cond(biome_is('jelly_shallows'), cond(noise_thr(-0.25, 10.0), block_rule('gloop_block'))),
            block_rule('gloop_grass')]}),
        cond(floor(2, True), cond(noise_thr(0.35, 10.0), block_rule('gloop_block'))),
        cond(ceiling(), cond(noise_thr(-0.06, 0.06), block_rule('flux_block'))),
        cond(ceiling(1, True), cond(noise_thr(0.3, 10.0), block_rule('gloop_block'))),
    ]}
    w(os.path.join(wg, 'noise_settings/gloopverse.json'), fi)

    cv = json.load(open(os.path.join(VANILLA, 'worldgen/noise_settings/caves.json')))
    cv['default_block'] = state('flesh_block')
    cv['default_fluid'] = state('minecraft:water', {'level': '0'})
    cv['sea_level'] = 0
    bedrock = state('minecraft:bedrock')
    cv['surface_rule'] = {'type': 'minecraft:sequence', 'sequence': [
        cond({'type': 'minecraft:vertical_gradient', 'random_name': 'minecraft:bedrock_floor', 'true_at_and_below': {'above_bottom': 0},
              'false_at_and_above': {'above_bottom': 5}}, {'type': 'minecraft:block', 'result_state': bedrock}),
        cond({'type': 'minecraft:not', 'invert': {'type': 'minecraft:vertical_gradient', 'random_name': 'minecraft:bedrock_roof',
                                                   'true_at_and_below': {'below_top': 5}, 'false_at_and_above': {'below_top': 0}}},
             {'type': 'minecraft:block', 'result_state': bedrock}),
        cond({'type': 'minecraft:vertical_gradient', 'random_name': 'mayhem:gristle', 'true_at_and_below': {'absolute': -12},
              'false_at_and_above': {'absolute': 6}}, block_rule('gristle')),
        cond(floor(), {'type': 'minecraft:sequence', 'sequence': [
            cond(biome_is('eye_gardens'), cond(noise_thr(-0.1, 10.0), block_rule('eye_flesh'))),
            cond(noise_thr(0.45, 10.0), block_rule('eye_flesh'))]}),
        cond(ceiling(), cond(noise_thr(0.5, 10.0), block_rule('eye_flesh'))),
    ]}
    w(os.path.join(wg, 'noise_settings/meatverse.json'), cv)

    # ---------------- biomes
    def biome(name, sky, fog, water, water_fog, grass, particle, prob, creatures, monsters, features, ambient=None, temp=0.8):
        eff = {'sky_color': sky, 'fog_color': fog, 'water_color': water, 'water_fog_color': water_fog,
               'grass_color': grass, 'foliage_color': grass,
               'particle': {'options': {'type': particle}, 'probability': prob},
               'mood_sound': {'sound': 'minecraft:ambient.cave', 'tick_delay': 6000, 'block_search_extent': 8, 'offset': 2.0}}
        if ambient:
            eff['ambient_sound'] = ambient
        steps = [[] for _ in range(11)]
        for step, feats in features.items():
            steps[step] = [m(f) for f in feats]
        spawners = {k: [] for k in ('ambient', 'axolotls', 'creature', 'misc', 'monster', 'underground_water_creature', 'water_ambient', 'water_creature')}
        spawners['creature'] = [{'type': m(t), 'weight': wt, 'minCount': a, 'maxCount': b} for (t, wt, a, b) in creatures]
        spawners['monster'] = [{'type': m(t), 'weight': wt, 'minCount': a, 'maxCount': b} for (t, wt, a, b) in monsters]
        w(os.path.join(wg, 'biome', name + '.json'), {'has_precipitation': False, 'temperature': temp, 'downfall': 0.0, 'effects': eff,
                                                       'spawners': spawners, 'spawn_costs': {}, 'carvers': {}, 'features': steps})

    biome('gloop_highlands', 0xFF7AD9, 0xB58CFF, 0x3FE0FF, 0x1A6A80, 0x7CFF5A, 'minecraft:warped_spore', 0.006,
          [('glooper', 12, 2, 4), ('zorp', 1, 1, 1)], [('thought_leech', 2, 1, 1)],
          {4: ['flux_spire'], 6: ['ore_gloop_in_gloopstone'], 9: ['patch_wobble_shroom', 'patch_flux_cluster']})
    biome('jelly_shallows', 0x7AF2FF, 0x9BE8FF, 0xB04CFF, 0x5A1F88, 0x3FE0FF, 'minecraft:white_ash', 0.004,
          [('glooper', 16, 3, 5)], [],
          {4: ['flux_spire'], 6: ['ore_gloop_in_gloopstone'], 9: ['patch_wobble_shroom']}, temp=0.6)
    biome('flesh_caverns', 0x300008, 0x5A0A1A, 0x8A0010, 0x400008, 0xC2405A, 'minecraft:crimson_spore', 0.02,
          [], [('meat_crawler', 60, 1, 3), ('watcher', 20, 1, 1), ('thought_leech', 25, 1, 2)],
          {4: ['flesh_tendril'], 6: ['ore_gristle_blob', 'ore_dark_matter', 'ore_eye_flesh']}, ambient='minecraft:ambient.crimson_forest.loop', temp=1.0)
    biome('eye_gardens', 0x200010, 0x3A0A3A, 0x6A0020, 0x2A0010, 0xC2405A, 'minecraft:crimson_spore', 0.03,
          [], [('meat_crawler', 30, 1, 2), ('watcher', 45, 1, 2), ('thought_leech', 25, 1, 2)],
          {4: ['flesh_tendril'], 6: ['ore_gristle_blob', 'ore_dark_matter', 'ore_eye_flesh']}, ambient='minecraft:ambient.soul_sand_valley.loop', temp=1.0)

    # ---------------- dimensions
    def mnb(entries):
        out = []
        for b, t in entries:
            out.append({'biome': m(b), 'parameters': {'temperature': t, 'humidity': 0.0, 'continentalness': 0.0, 'erosion': 0.0,
                                                       'weirdness': 0.0, 'depth': 0.0, 'offset': 0.0}})
        return {'type': 'minecraft:multi_noise', 'biomes': out}
    w(os.path.join(D, 'dimension/gloopverse.json'), {'type': m('gloopverse'), 'generator': {
        'type': 'minecraft:noise', 'settings': m('gloopverse'), 'biome_source': mnb([('gloop_highlands', [-1.0, 0.2]), ('jelly_shallows', [0.2, 1.0])])}})
    w(os.path.join(D, 'dimension/meatverse.json'), {'type': m('meatverse'), 'generator': {
        'type': 'minecraft:noise', 'settings': m('meatverse'), 'biome_source': {'type': 'minecraft:checkerboard', 'biomes': [m('flesh_caverns'), m('flesh_caverns'), m('eye_gardens')], 'scale': 2}}})

    # ---------------- configured & placed features
    cf = os.path.join(wg, 'configured_feature')
    pf = os.path.join(wg, 'placed_feature')

    def ore(name, targets, size, discard=0.0):
        w(os.path.join(cf, name + '.json'), {'type': 'minecraft:ore', 'config': {'discard_chance_on_air_exposure': discard, 'size': size, 'targets': targets}})

    def target_tag(tag, block):
        return {'state': state(block), 'target': {'predicate_type': 'minecraft:tag_match', 'tag': tag}}

    def target_block(match, block):
        return {'state': state(block), 'target': {'predicate_type': 'minecraft:block_match', 'block': m(match)}}

    def placed(name, feature, placement):
        w(os.path.join(pf, name + '.json'), {'feature': m(feature), 'placement': placement})

    def count(n):
        return {'type': 'minecraft:count', 'count': n}

    sq = {'type': 'minecraft:in_square'}
    bi = {'type': 'minecraft:biome'}

    def hr(kind, lo, hi):
        return {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:' + kind, 'min_inclusive': lo, 'max_inclusive': hi}}

    flux_targets = [target_tag('minecraft:stone_ore_replaceables', 'flux_crystal_ore'), target_tag('minecraft:deepslate_ore_replaceables', 'deepslate_flux_crystal_ore')]
    ore('ore_flux_crystal', flux_targets, 6)
    placed('ore_flux_crystal', 'ore_flux_crystal', [count(6), sq, hr('trapezoid', {'absolute': -48}, {'absolute': 40}), bi])
    ore('ore_flux_crystal_buried', flux_targets, 9, 0.6)
    placed('ore_flux_crystal_buried', 'ore_flux_crystal_buried', [count(3), sq, hr('uniform', {'above_bottom': 0}, {'absolute': 0}), bi])
    w(os.path.join(cf, 'crashed_saucer.json'), {'type': m('crashed_saucer'), 'config': {}})
    placed('crashed_saucer', 'crashed_saucer', [{'type': 'minecraft:rarity_filter', 'chance': 320}, sq,
                                                 {'type': 'minecraft:heightmap', 'heightmap': 'WORLD_SURFACE_WG'}, bi])
    w(os.path.join(cf, 'flux_spire.json'), {'type': m('flux_spire'), 'config': {}})
    placed('flux_spire', 'flux_spire', [{'type': 'minecraft:rarity_filter', 'chance': 3}, sq, {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'}, bi])
    w(os.path.join(cf, 'flesh_tendril.json'), {'type': m('flesh_tendril'), 'config': {}})
    placed('flesh_tendril', 'flesh_tendril', [count(5), sq, hr('uniform', {'absolute': -40}, {'absolute': 100}), bi])
    ore('ore_gloop_in_gloopstone', [target_block('gloopstone', 'gloop_block')], 20)
    placed('ore_gloop_in_gloopstone', 'ore_gloop_in_gloopstone', [count(6), sq, hr('uniform', {'absolute': 0}, {'absolute': 255}), bi])
    ore('ore_gristle_blob', [target_block('flesh_block', 'gristle')], 33)
    placed('ore_gristle_blob', 'ore_gristle_blob', [count(10), sq, hr('uniform', {'absolute': -64}, {'absolute': 128}), bi])
    ore('ore_dark_matter', [target_block('gristle', 'dark_matter_ore'), target_block('flesh_block', 'dark_matter_ore')], 5)
    placed('ore_dark_matter', 'ore_dark_matter', [count(7), sq, hr('trapezoid', {'absolute': -64}, {'absolute': 24}), bi])
    ore('ore_eye_flesh', [target_block('flesh_block', 'eye_flesh')], 10)
    placed('ore_eye_flesh', 'ore_eye_flesh', [count(8), sq, hr('uniform', {'absolute': -64}, {'absolute': 128}), bi])

    def patch(name, block, tries, chance_):
        w(os.path.join(cf, name + '.json'), {'type': 'minecraft:random_patch', 'config': {'tries': tries, 'xz_spread': 7, 'y_spread': 3, 'feature': {
            'feature': {'type': 'minecraft:simple_block', 'config': {'to_place': {'type': 'minecraft:simple_state_provider', 'state': state(block)}}},
            'placement': [{'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
                {'type': 'minecraft:matching_blocks', 'blocks': 'minecraft:air'},
                {'type': 'minecraft:would_survive', 'state': state(block)}]}}]}}})
        placed(name, name, [{'type': 'minecraft:rarity_filter', 'chance': chance_}, sq, {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'}, bi])
    patch('patch_wobble_shroom', 'wobble_shroom', 32, 2)
    patch('patch_flux_cluster', 'flux_cluster', 12, 3)


# =========================================================== advancements
def advancements():
    adv = os.path.join(D, 'advancement')

    def a(name, parent, icon, title, desc, criteria, frame='task', hidden=False, toast=True, chat=True, xp=None):
        obj = {'display': {'icon': {'id': m(icon) if ':' not in icon else icon, 'count': 1}, 'title': title, 'description': desc,
                           'frame': frame, 'show_toast': toast, 'announce_to_chat': chat, 'hidden': hidden},
               'criteria': criteria, 'requirements': [list(criteria.keys())]}
        if parent:
            obj['parent'] = m(parent)
        else:
            obj['display']['background'] = 'mayhem:textures/block/gloopstone_bricks.png'
        if xp:
            obj['rewards'] = {'experience': xp}
        w(os.path.join(adv, name + '.json'), obj)

    def has(*items_):
        return {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': m(i)} for i in items_]}}

    def dim(d):
        return {'trigger': 'minecraft:changed_dimension', 'conditions': {'to': m(d)}}

    def kill(e):
        return {'trigger': 'minecraft:player_killed_entity', 'conditions': {'entity': [{'condition': 'minecraft:entity_properties', 'entity': 'this',
                                                                                         'predicate': {'type': m(e)}}]}}
    a('root', None, 'flux_crystal', 'Multiverse Mayhem', 'Reality is optional. Dig deep for Flux Crystals.', {'flux': has('flux_crystal')}, toast=False, chat=False)
    a('portal_gun', 'root', 'portal_gun', 'Hole Punch', 'Build a Portal Gun', {'gun': has('portal_gun')}, 'goal')
    a('gloopverse', 'portal_gun', 'gloop_block', 'Gloop, There It Is', 'Visit the Gloopverse', {'dim': dim('gloopverse')})
    a('meatverse', 'portal_gun', 'flesh_block', 'Meat and Greet', 'Visit the Meatverse. Try not to think about it.', {'dim': dim('meatverse')})
    a('tame_glooper', 'gloopverse', 'gloop_ball', 'Jelly Friend', 'Tame a Glooper with a Flux Crystal',
      {'tame': {'trigger': 'minecraft:tame_animal', 'conditions': {'entity': [{'condition': 'minecraft:entity_properties', 'entity': 'this', 'predicate': {'type': m('glooper')}}]}}})
    a('dark_matter', 'meatverse', 'dark_matter', 'It Stares Back', 'Obtain Dark Matter', {'dm': has('dark_matter')})
    a('customs', 'portal_gun', 'stun_baton', 'Paperwork', 'Defeat an Interdimensional Customs Agent', {'kill': kill('customs_agent')})
    a('permit', 'customs', 'dimensional_permit', 'Fully Licensed', 'Obtain a Dimensional Permit', {'p': has('dimensional_permit')}, 'goal')
    a('toaster', 'root', 'toaster_core', 'Toast of the Town', 'Build a Toaster Core', {'t': has('toaster_core')})
    a('black_hole', 'dark_matter', 'pocket_black_hole', 'Pocket Physics', 'Obtain a Pocket Black Hole', {'b': has('pocket_black_hole')})
    a('eye_bait', 'dark_matter', 'eye_bait', 'Here, Eyey Eyey', 'Craft Eye Bait', {'e': has('eye_bait')})
    a('omni_eye', 'eye_bait', 'omni_lens', 'I See What You Did There', 'Defeat the Omni-Eye', {'k': kill('omni_eye')}, 'challenge', xp=200)
    a('omni_gun', 'omni_eye', 'omni_portal_gun', 'Omnipresent', 'Build the Omni Portal Gun', {'g': has('omni_portal_gun')}, 'challenge', xp=100)
    a('meat_crawler', 'meatverse', 'raw_mystery_meat', 'Mystery Solved', 'Kill a Meat Crawler', {'k': kill('meat_crawler')})


def jukebox():
    w(os.path.join(D, 'jukebox_song/interdimensional_jam.json'), {'sound_event': m('music_disc.interdimensional_jam'),
                                                                 'description': {'translate': 'jukebox_song.mayhem.interdimensional_jam'},
                                                                 'length_in_seconds': 62.0, 'comparator_output': 13})


if __name__ == '__main__':
    for d in (os.path.join(A, 'blockstates'), os.path.join(A, 'models'), D, MC):
        if os.path.isdir(d):
            shutil.rmtree(d)
    blockstates_and_models()
    subs = sounds()
    w(os.path.join(A, 'lang/en_us.json'), lang(subs))
    loot()
    recipes()
    tags()
    worldgen()
    advancements()
    jukebox()
    print('data generated')
