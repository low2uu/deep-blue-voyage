"""Copy-only refresh of plain placeholder sea near the starter island.
Keeps all island/harbour chunks and any chunk with structures, block entities or
non-placeholder blocks. Does not modify ships, players, entities or the source.
Requires Minecraft closed for the source; nbtlib/numpy as height migration.
"""
from pathlib import Path
import argparse, hashlib, importlib.util, json, math, shutil
spec=importlib.util.spec_from_file_location('height',Path(__file__).with_name('migrate-world-height.py'))
h=importlib.util.module_from_spec(spec);spec.loader.exec_module(h)
ALLOWED={'minecraft:'+x for x in ['air','cave_air','void_air','stone','deepslate','bedrock','water','sand','sandstone','dirt','grass_block','gravel']}
def manifest(root):
    return {str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in root.rglob('*') if p.is_file()}
def eligible(cx,cz):
    x,z=cx*16,cz*16
    return abs(x)<640 and abs(z)<640 and all(
        math.hypot(xx/256,zz/192)>1.08
        and not(-32<=xx<168 and 64<=zz<320)
        and not(160<=xx<224 and -80<=zz<-16)
        for xx in range(x,x+16) for zz in range(z,z+16))
def refresh(source,destination):
    source=source.resolve();destination=destination.resolve()
    if destination.exists() or source==destination or source in destination.parents:raise ValueError('Use a separate new destination')
    level=h.n.load(source/'level.dat')
    generator=str(level['Data']['WorldGenSettings']['dimensions']['minecraft:overworld']['generator']['type'])
    if generator!='dbv_world:flooded_world':raise ValueError('Expected flooded-world save')
    before=manifest(source);shutil.copytree(source,destination);removed=[]
    for f in (destination/'region').glob('*.mca'):
        rows=h.read_region(f);keep=[]
        for slot,t in rows:
            cx,cz=int(t['xPos']),int(t['zPos'])
            palette={str(p['Name']) for sec in t.get('sections',[]) for p in sec.get('block_states',{}).get('palette',[])}
            if eligible(cx,cz) and not t.get('block_entities') and not t.get('structures',{}).get('starts') and palette<=ALLOWED:removed.append([cx,cz])
            else:keep.append((slot,t))
        if len(rows)!=len(keep):h.write_region(f,keep)
    removed_set={tuple(x) for x in removed}
    for f in (destination/'poi').glob('*.mca'):
        rx,rz=map(int,f.stem.split('.')[1:]);rows=h.read_region(f)
        keep=[(slot,t) for slot,t in rows if (rx*32+slot%32,rz*32+slot//32) not in removed_set]
        if len(rows)!=len(keep):h.write_region(f,keep)
    if manifest(source)!=before:raise RuntimeError('Source changed during refresh; do not use destination')
    for folder in ['sublevels','dbv','playerdata','entities']:
        if manifest(source/folder)!=manifest(destination/folder):raise RuntimeError('Preservation failed: '+folder)
    report={'source':str(source),'destination':str(destination),'removed_placeholder_chunks':removed,'original_untouched':True,'ship_player_entities_unchanged':True}
    (destination/'dbv-near-island-refresh.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps({'removed_chunks':len(removed),'source':str(source),'destination':str(destination)}))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('source',type=Path);p.add_argument('destination',type=Path);a=p.parse_args();refresh(a.source,a.destination)
