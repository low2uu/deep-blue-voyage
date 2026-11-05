"""Offline resource validation, no client/server launch."""
import json,itertools,re
from pathlib import Path
R=Path(__file__).resolve().parents[2];A=R/'mods/dbv-world/src/main/resources/assets/dbv_world'
ids=['jiuhu_console','cabin_panel','personnel_hatch','hangar_door_core','hangar_door_panel']
for id in ids:
 state=json.loads((A/f'blockstates/{id}.json').read_text(encoding='utf-8'))['variants']
 assert len(state)==(64 if id=='personnel_hatch' else 16 if id.startswith('hangar') else 4)
 for values in state.values():
  # Resource locations omit the models/ prefix.
  p=A/'models'/(values['model'].split(':')[1]+'.json')
  model=json.loads(p.read_text(encoding='utf-8'))
  for e in model['elements']:
   assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to']))
  for v in model['textures'].values():
   if v.startswith('dbv_world:'):assert (A/'textures'/(v.split(':')[1]+'.png')).exists()
 assert (A/f'models/item/{id}.json').exists()
 loot=R/f'mods/dbv-world/src/main/resources/data/dbv_world/loot_table/blocks/{id}.json'
 assert loot.exists()
 if id=='personnel_hatch':assert any(c.get('properties',{}).get('half')=='lower' for c in json.loads(loot.read_text())['pools'][0]['conditions'])
 for lang in ['zh_cn','en_us']:assert 'block.dbv_world.'+id in json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
java=(R/'mods/dbv-world/src/main/java/io/github/low2uu/dbv/fleet/door/CabinDoors.java').read_text(encoding='utf-8')+(R/'mods/dbv-world/src/main/java/io/github/low2uu/dbv/fleet/door/DoorSafety.java').read_text(encoding='utf-8')
keys=set(re.findall(r'IllegalStateException\("([a-z_]+)"\)',java))|set(re.findall(r'message="([a-z_]+)"',java))
for lang in ['zh_cn','en_us']:
 data=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
 assert all('message.dbv_world.door.'+k in data for k in keys)
print('Validated five item/block resources; 104 blockstate variants; geometry/textures, lower-half-only door loot and bilingual interaction messages.')

# The top panel must have an actual hole behind glass, not just a blue surface.
model=json.loads((A/'models/block/personnel_hatch_top_left.json').read_text())
assert model['render_type']=='minecraft:cutout'
assert any(any(f['texture']=='#glass' for f in e['faces'].values()) for e in model['elements'])
for e in model['elements']:
 if any(f['texture']=='#glass' for f in e['faces'].values()):continue
 assert not all(e['from'][i]<v<e['to'][i] for i,v in enumerate([1.5,9,8])), 'Opaque geometry blocks upper window'
from PIL import Image
for path in (A/'textures').rglob('*.png'):assert Image.open(path).size==(16,16),path
for lang in ['zh_cn','en_us']:
 data=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
 for key in ['check_unavailable','check_pending','check_sealed','check_unsealed']:
  assert 'message.dbv_world.door.'+key in data
print('Validated transparent window geometry, pre-opening report translations and all runtime textures at 16x16.')
