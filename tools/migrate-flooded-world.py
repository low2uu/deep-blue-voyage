"""Copy-only conversion of the inspected construction ocean; run with Minecraft closed.

Preserve complete starter-core chunks and all Sable/business/player data. Discard
only surrounding placeholder terrain so it regenerates on demand. Refuse exterior
block entities or entities rather than deleting unknown player work.
"""
import argparse,hashlib,importlib.util,json,shutil,zipfile
from pathlib import Path
spec=importlib.util.spec_from_file_location('height',Path(__file__).with_name('migrate-world-height.py'))
h=importlib.util.module_from_spec(spec);spec.loader.exec_module(h)

def core(cx,cz):return -18<=cx<18 and -14<=cz<19
def manifest(root):return {str(p.relative_to(root)):hashlib.sha256(p.read_bytes()).hexdigest() for p in root.rglob('*') if p.is_file()}

def migrate(source,destination,backup):
    source=source.resolve();destination=destination.resolve();backup=backup.resolve()
    if source==destination or source in destination.parents or source in backup.parents or destination in backup.parents:raise ValueError('Paths must be separate')
    if destination.exists() or backup.exists():raise ValueError('Never overwrite existing destination or backup')
    level=h.n.load(source/'level.dat')
    ow=level['Data']['WorldGenSettings']['dimensions']['minecraft:overworld']
    if str(ow['type'])!='dbv_world:ocean' or str(ow['generator']['type'])!='dbv_world:starter_island':raise ValueError('Expected migrated legacy island')
    for f in (source/'region').glob('*.mca'):
        for _,t in h.read_region(f):
            if not core(int(t['xPos']),int(t['zPos'])) and (t.get('block_entities') or t.get('structures',{}).get('starts')):
                raise ValueError('Unexpected exterior content: '+str(f))
    for f in (source/'entities').glob('*.mca'):
        for _,t in h.read_region(f):
            for e in t.get('Entities',[]):
                pos=e.get('Pos',[])
                if len(pos)!=3:raise ValueError('Unknown entity position')
                x,z=float(pos[0]),float(pos[2])
                if abs(x)<1000000 and abs(z)<1000000 and not core(int(x//16),int(z//16)):raise ValueError('Exterior entity must be preserved explicitly')
    before=manifest(source);backup.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(backup,'x',zipfile.ZIP_DEFLATED) as z:
        for name in before:z.write(source/name,name)
    shutil.copytree(source,destination)
    kept=removed=0
    for f in (destination/'region').glob('*.mca'):
        old=h.read_region(f);new=[]
        for slot,t in old:
            if core(int(t['xPos']),int(t['zPos'])):new.append((slot,t));kept+=1
            else:removed+=1
        if new:h.write_region(f,new)
        else:f.unlink()
    # Stale exterior points-of-interest must not survive regenerated terrain.
    for f in (destination/'poi').glob('*.mca'):
        rx,rz=map(int,f.stem.split('.')[1:]);entries=h.read_region(f);new=[]
        for slot,t in entries:
            cx,cz=rx*32+slot%32,rz*32+slot//32
            if core(cx,cz) or abs(cx)>100000 or abs(cz)>100000:new.append((slot,t))
        if new:h.write_region(f,new)
        else:f.unlink()
    for f in [destination/'level.dat',destination/'level.dat_old']:
        if not f.exists():continue
        t=h.n.load(f);d=t['Data'];ow=d['WorldGenSettings']['dimensions']['minecraft:overworld']
        ow['generator']=h.n.Compound({'type':h.n.String('dbv_world:flooded_world'),'settings':h.n.String('dbv_world:flooded_overworld'),
            'biome_source':h.n.Compound({'type':h.n.String('minecraft:multi_noise'),'preset':h.n.String('minecraft:overworld')})})
        d['LevelName']=h.n.String('Deep Blue Voyage - Flooded World v4')
        d['GameRules']['doMobSpawning']=h.n.String('true')
        for name,value in [('BorderCenterX',0),('BorderCenterZ',0),('BorderSize',10000),('BorderSizeLerpTarget',10000)]:d[name]=h.n.Double(value)
        d['BorderSizeLerpTime']=h.n.Long(0)
        t.save(f)
    rules=destination/'dbv-construction-rules.properties'
    if rules.exists():
        rules.write_text(rules.read_text(encoding='utf-8').replace('doMobSpawning=false','doMobSpawning=true'),encoding='utf-8')
    if manifest(source)!=before:raise RuntimeError('Source changed during migration')
    for folder in ['sublevels','dbv','playerdata']:
        assert manifest(source/folder)==manifest(destination/folder),folder+' must be unchanged'
    report={'source':str(source),'destination':str(destination),'backup':str(backup),'preserved_chunks':kept,'discarded_placeholder_chunks':removed,
        'source_unchanged':True,'ship_business_player_files_unchanged':True,'generation':'on_demand','border_size':10000,'core_blocks':[-288,288,-224,304],
        'note':'Outside boundary may contain empty engine dependency chunks, never intentional terrain pre-generation.'}
    (destination/'dbv-flooded-migration.json').write_text(json.dumps(report,indent=2),encoding='utf-8');print(json.dumps(report,indent=2))

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('source',type=Path);p.add_argument('destination',type=Path);p.add_argument('backup',type=Path)
    a=p.parse_args();migrate(a.source,a.destination,a.backup)
