"""Validate and package creature geometry/specifications without launching Minecraft."""
from pathlib import Path
import hashlib
import json
import math
import zipfile
import re

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'mods/dbv-world/src/main/resources/assets/dbv_world'
version = re.search(r"version = '([^']+)'", (ROOT / 'mods/dbv-world/build.gradle').read_text(encoding='utf-8')).group(1)
files = {}
specs = sorted((ROOT / 'content/prototypes/mobs').glob('*.json'))
assert len(specs) == 15
for specpath in specs:
    name = specpath.stem
    spec = json.loads(specpath.read_text(encoding='utf-8'))
    path = ASSETS / f'models/entity/{name}.json'
    rig = json.loads(path.read_text(encoding='utf-8'))
    assert rig['format'] == 'dbv-creature-model' and rig['version'] == 1
    assert spec['sections']['appearance']['model']['value'] == 'custom'
    assert spec['sections']['appearance']['assetReferences']['value'] == [f'dbv_world:models/entity/{name}.json']
    seen = {'root'}
    for part in rig['parts']:
        assert part['parent'] in seen and part['name'] not in seen
        seen.add(part['name'])
        assert len(part['pivot']) == 3 and all(math.isfinite(v) for v in part['pivot'])
        for cube in part['cubes']:
            assert len(cube['size']) == len(cube['origin']) == 3
            assert all(math.isfinite(v) and v > 0 for v in cube['size'])
            assert all(math.isfinite(v) for v in cube['origin'])
            w,h,d = cube['size']; u,v = cube['uv']
            assert 0 <= u and 0 <= v and u + 2*(w+d) <= rig['textureWidth'] and v+h+d <= rig['textureHeight']
    files[f'geometry/{name}.json'] = path
    files[f'specs/{name}.json'] = specpath
files['textures/ocean_palette.png'] = ASSETS / 'textures/entity/ocean_palette.png'
files['README.md'] = ROOT / 'tools/creature-models/README.md'
files['creature-model-contact-sheet.png'] = ROOT / 'docs/art/creature-models/creature-model-contact-sheet.png'
archive = ROOT / f'tool/creature-models-v{version}.zip'
with zipfile.ZipFile(archive, 'x', zipfile.ZIP_DEFLATED) as z:
    for name,path in files.items():
        z.write(path,name)
archive.with_suffix('.zip.sha256').write_text(hashlib.sha256(archive.read_bytes()).hexdigest()+'  '+archive.name+'\n',encoding='ascii')
jar = ROOT / f'mods/dbv-world/build/libs/dbv-world-{version}.jar'
with zipfile.ZipFile(jar) as z:
    for specpath in specs:
        assert z.read(f'assets/dbv_world/models/entity/{specpath.stem}.json') == (ASSETS / f'models/entity/{specpath.stem}.json').read_bytes()
    assert z.read('assets/dbv_world/textures/entity/ocean_palette.png') == files['textures/ocean_palette.png'].read_bytes()
output = ROOT / f'dist/prototypes-v{version}'
output.mkdir(parents=True,exist_ok=True)
(output / jar.name).write_bytes(jar.read_bytes())
print(f'Validated 15 rigs, parent order, finite geometry, UV bounds, design references and JAR assets. Packaged {archive.name}.')
