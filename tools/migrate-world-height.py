"""Offline, copy-only migration of the inspected DBV construction world (+65 Y).

Requires numpy and nbtlib. Refuses unknown block entities, structure starts, active
refits and external region chunks rather than guessing how to migrate their data.
Never overwrites the source or an existing destination. Run with Minecraft closed.
"""
from pathlib import Path
import argparse, gzip, hashlib, io, json, math, shutil, struct, sys, time, zipfile, zlib
sys.path.insert(0, str(Path(__file__).resolve().parents[1]/'.local/python-libs'))
import numpy as np
import nbtlib as n

SHIFT=65
MIN_Y=-256
MAX_Y=320

def parse(data): return n.File.parse(io.BytesIO(data))
def serialize(tag):
    stream=io.BytesIO();tag.write(stream);return stream.getvalue()

def read_region(path, sector=4096, header=8192, sable=False):
    raw=path.read_bytes()
    if not raw:return []
    if len(raw)<header:raise ValueError(f'Truncated file: {path}')
    entries=[]
    for i in range(1024):
        span=struct.unpack_from('>I',raw,i*4)[0]
        if not span:continue
        off=(span>>8)*sector
        length,kind=struct.unpack_from('>IB',raw,off)
        if kind & (16 if sable else 128):raise ValueError(f'External entry: {path}:{i}')
        payload=raw[off+5:off+4+length]
        if sable:payload=gzip.decompress(payload)
        elif kind==2:payload=zlib.decompress(payload)
        elif kind==1:payload=gzip.decompress(payload)
        elif kind!=3:raise ValueError(f'Unsupported compression {kind}')
        entries.append((i,parse(payload)))
    return entries

def write_region(path, entries, sector=4096, header=8192, sable=False):
    raw=bytearray(header)
    for i,tag in entries:
        payload=gzip.compress(serialize(tag),mtime=0) if sable else zlib.compress(serialize(tag))
        record=struct.pack('>IB',len(payload)+1,0 if sable else 2)+payload
        count=math.ceil(len(record)/sector)
        if count>255:raise ValueError('External output would be required')
        struct.pack_into('>I',raw,i*4,((len(raw)//sector)<<8)|count)
        if not sable:struct.pack_into('>I',raw,4096+i*4,int(time.time()))
        raw.extend(record);raw.extend(b'\0'*(count*sector-len(record)))
    path.write_bytes(raw)

def unpack(data,bits,count):
    if data is None:return np.zeros(count,dtype=np.uint32)
    words=np.asarray(data,dtype=np.int64).view(np.uint64)
    per=64//bits;indices=np.arange(count,dtype=np.uint64)
    return ((words[indices//per] >> ((indices%per)*bits)) & ((1<<bits)-1)).astype(np.uint32)

def pack(values,bits):
    per=64//bits;words=np.zeros(math.ceil(len(values)/per),dtype=np.uint64)
    for offset in range(per):
        v=np.asarray(values[offset::per],dtype=np.uint64)
        words[:len(v)] |= v << (offset*bits)
    return n.LongArray(words.view(np.int64))

def key(state):return json.dumps(state.unpack(),sort_keys=True)

def migrate_chunk(tag,report):
    if abs(int(tag['xPos']))>100000 or abs(int(tag['zPos']))>100000:
        raise ValueError('Unexpected vanilla terrain at a Sable plot coordinate')
    if tag.get('structures',{}).get('starts'):raise ValueError('Structure migration not supported')
    sections=tag.get('sections',[])
    states=[n.Compound({'Name':n.String(x)}) for x in ['minecraft:air','minecraft:stone','minecraft:bedrock']]
    ids={key(s):i for i,s in enumerate(states)}
    old=np.zeros((384,256),dtype=np.uint32)
    old_biomes={}
    has_blocks=False
    for sec in sections:
        sy=int(sec['Y'])
        if 'biomes' in sec:old_biomes[sy]=sec['biomes']
        bs=sec.get('block_states')
        if bs is None:continue
        has_blocks=True
        palette=bs['palette'];lookup=[]
        for state in palette:
            k=key(state)
            if k not in ids:ids[k]=len(states);states.append(state)
            lookup.append(ids[k])
        values=np.array(lookup,dtype=np.uint32)[unpack(bs.get('data'),max(4,(len(palette)-1).bit_length()),4096)]
        if -4<=sy<20:old[(sy+4)*16:(sy+5)*16]=values.reshape(16,256)
        elif np.any(values):raise ValueError(f'Blocks outside legacy range: {sy}')
    if np.any(old[319-SHIFT+64+1:]):raise ValueError('Upper blocks would be clipped')
    new=np.zeros((576,256),dtype=np.uint32)
    if has_blocks:
        new[:257]=1;new[0]=2
        old[0][old[0]==2]=1
        length=min(len(old),576-257);new[257:257+length]=old[:length]
        # Every existing block above the old world floor must survive exactly.
        assert np.array_equal(new[258:257+length],old[1:length])
        report['preserved_block_cells']+=int((length-1)*256)
    new_sections=n.List[n.Compound]()
    default_biome=n.Compound({'palette':n.List[n.String]([n.String('minecraft:deep_ocean')])})
    for sy in range(-16,20):
        sec=n.Compound({'Y':n.Byte(sy)})
        # Existing world uses uniform deep_ocean/plains section biomes. Resample
        # vertical biome cells at their centres after the 65-block translation.
        bpalette=[];bids={};bvalues=[]
        for by in range(4):
            oy=sy*16+by*4+2-SHIFT;source=old_biomes.get(max(-4,min(19,oy//16)),default_biome)
            pal=source['palette'];decoded=unpack(source.get('data'),max(1,(len(pal)-1).bit_length()),64)
            for v in decoded[((oy%16)//4)*16:((oy%16)//4+1)*16]:
                name=str(pal[int(v)])
                if name not in bids:bids[name]=len(bpalette);bpalette.append(n.String(name))
                bvalues.append(bids[name])
        bio=n.Compound({'palette':n.List[n.String](bpalette)})
        if len(bpalette)>1:bio['data']=pack(bvalues,max(1,(len(bpalette)-1).bit_length()))
        sec['biomes']=bio
        if has_blocks:
            vals=new[(sy+16)*16:(sy+17)*16].reshape(-1)
            unique,inverse=np.unique(vals,return_inverse=True)
            bs=n.Compound({'palette':n.List[n.Compound]([states[int(i)] for i in unique])})
            if len(unique)>1:bs['data']=pack(inverse,max(4,(len(unique)-1).bit_length()))
            sec['block_states']=bs
        new_sections.append(sec)
    tag['sections']=new_sections;tag['yPos']=n.Int(-16);tag['isLightOn']=n.Byte(0)
    for hm,data in list(tag.get('Heightmaps',{}).items()):
        values=unpack(data,9,256)
        values=np.where(values>0,values+257,257 if has_blocks else 0)
        tag['Heightmaps'][hm]=pack(values,10)
    for be in tag.get('block_entities',[]):
        if str(be.get('id'))!='minecraft:sign':raise ValueError('Unknown stationary block entity '+str(be.get('id')))
        be['y']=n.Int(int(be['y'])+SHIFT);report['moved_signs']+=1
    for field in ['block_ticks','fluid_ticks']:
        for tick in tag.get(field,[]):tick['y']=n.Int(int(tick['y'])+SHIFT)
    # Protochunk postprocessing coordinates use packed section-local positions.
    post=n.List[n.List[n.Short]]([n.List[n.Short]() for _ in range(36)])
    for section_index,values in enumerate(tag.get('PostProcessing',[])):
        for raw in values:
            v=int(raw)&0xffff;old_y=(section_index-4)*16+((v>>4)&15);new_y=old_y+SHIFT
            if MIN_Y<=new_y<MAX_Y:post[new_y//16+16].append(n.Short((v&0xf0f)|((new_y&15)<<4)))
    tag['PostProcessing']=post
    for field in ['CarvingMasks','blending_data','below_zero_retrogen']:tag.pop(field,None)
    report['chunks']+=1

def shift_entity(tag):
    if 'Pos' in tag and len(tag['Pos'])==3:
        # Sable seats may be stored in virtual plot coordinates; leave those local positions intact.
        if abs(float(tag['Pos'][0]))<1000000 and abs(float(tag['Pos'][2]))<1000000:
            tag['Pos'][1]=n.Double(float(tag['Pos'][1])+SHIFT)
    for k in ['TileY','SleepingY','APY']:
        if k in tag:tag[k]=type(tag[k])(int(tag[k])+SHIFT)
    for passenger in tag.get('Passengers',[]):shift_entity(passenger)
    if 'Leash' in tag and 'Y' in tag['Leash']:tag['Leash']['Y']=n.Int(int(tag['Leash']['Y'])+SHIFT)

def shift_player(tag):
    if str(tag.get('Dimension','minecraft:overworld'))=='minecraft:overworld':shift_entity(tag)
    if 'SpawnY' in tag and str(tag.get('SpawnDimension','minecraft:overworld'))=='minecraft:overworld':tag['SpawnY']=n.Int(int(tag['SpawnY'])+SHIFT)
    death=tag.get('LastDeathLocation')
    if death and str(death.get('dimension'))=='minecraft:overworld':
        pos=[int(v) for v in death['pos']];pos[1]+=SHIFT;death['pos']=n.IntArray(pos)

def migrate_ship_plot(plot):
    # Sable section keys are ARRAY INDICES from minY, unlike vanilla signed Y.
    # Keep absolute virtual block positions unchanged as minY drops by 192.
    plot['isLightOn']=n.Byte(0)
    for chunk in plot['chunks'].values():
        chunk['sections']=n.Compound({str(int(index)+12):section for index,section in chunk['sections'].items()})
        for section in chunk['sections'].values():
            section.pop('BlockLight',None);section.pop('SkyLight',None)
        for name,data in list(chunk.get('heightmaps',{}).items()):
            values=unpack(data,9,256)
            chunk['heightmaps'][name]=pack(np.where(values>0,values+192,0),10)

def migrate(source,destination,backup):
    source=source.resolve();destination=destination.resolve();backup=backup.resolve()
    if source==destination or source in destination.parents:raise ValueError('Destination must be separate')
    if source in backup.parents or destination in backup.parents:raise ValueError('Backup must be outside source and destination')
    if destination.exists() or backup.exists():raise ValueError('Destination/backup already exists')
    for pattern in ['active-refit*','refit-session*','block-refit*']:
        if list((source/'dbv/fleet/dock').glob(pattern)):raise ValueError('Active refit must be resolved first')
    meta=n.load(source/'level.dat')
    overworld=meta['Data']['WorldGenSettings']['dimensions']['minecraft:overworld']
    if str(overworld['type'])!='minecraft:overworld' or str(overworld['generator']['type'])!='dbv_world:starter_island':raise ValueError('Not the legacy DBV world')
    manifest={str(p.relative_to(source)):hashlib.sha256(p.read_bytes()).hexdigest() for p in source.rglob('*') if p.is_file()}
    backup.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(backup,'x',zipfile.ZIP_DEFLATED) as z:
        for p in source.rglob('*'):
            if p.is_file():z.write(p,p.relative_to(source))
    shutil.copytree(source,destination)
    report={'source':str(source),'destination':str(destination),'backup':str(backup),'sea_level':128,'min_y':MIN_Y,'max_y_exclusive':MAX_Y,'offset':SHIFT,'chunks':0,'preserved_block_cells':0,'moved_signs':0,'bodies':[]}
    for f in (destination/'region').glob('*.mca'):
        entries=read_region(f)
        for _,tag in entries:migrate_chunk(tag,report)
        if entries:write_region(f,entries)
    for f in (destination/'entities').glob('*.mca'):
        entries=read_region(f)
        for _,tag in entries:
            for entity in tag.get('Entities',[]):shift_entity(entity)
        if entries:write_region(f,entries)
    # POI coordinates are world positions except virtual ship plots.
    for f in (destination/'poi').glob('*.mca'):
        entries=read_region(f)
        for _,tag in entries:
            new=n.Compound()
            for sk,section in tag.get('Sections',{}).items():
                for record in section.get('Records',[]):
                    pos=[int(v) for v in record['pos']];local=abs(int(pos[0]))>=1000000 or abs(int(pos[2]))>=1000000
                    if not local:pos[1]=int(pos[1])+SHIFT
                    record['pos']=n.IntArray(pos)
                    key=str(int(pos[1])//16)
                    if key not in new:new[key]=n.Compound({'Valid':n.Byte(0),'Records':n.List[n.Compound]()})
                    new[key]['Records'].append(record)
            tag['Sections']=new
        if entries:write_region(f,entries)
    for f in (destination/'sublevels').glob('*.slvls'):
        entries=read_region(f,header=4096,sable=True)
        for _,tag in entries:
            original_plot_sha=hashlib.sha256(serialize(n.File({'plot':tag['plot']}))).hexdigest()
            migrate_ship_plot(tag['plot'])
            pose=tag['pose']['position'];old_y=float(pose['y']);pose['y']=n.Double(old_y+SHIFT)
            for k in ['minY','maxY']:tag['world_bounds'][k]=n.Double(float(tag['world_bounds'][k])+SHIFT)
            report['bodies'].append({'uuid':[int(v) for v in tag['uuid']],'old_y':old_y,'new_y':float(pose['y']),'original_plot_sha256':original_plot_sha,'plot_sha256':hashlib.sha256(serialize(n.File({'plot':tag['plot']}))).hexdigest()})
        if entries:write_region(f,entries,header=4096,sable=True)
    for f in [destination/'level.dat',destination/'level.dat_old']:
        if not f.exists():continue
        data=n.load(f);d=data['Data'];ow=d['WorldGenSettings']['dimensions']['minecraft:overworld']
        ow['type']=n.String('dbv_world:ocean');ow['generator']['settings']['layers'][1]['height']=n.Int(272)
        d['SpawnY']=n.Int(int(d['SpawnY'])+SHIFT);d['LevelName']=n.String('Deep Blue Voyage - Sea 128')
        if 'Player' in d:shift_player(d['Player'])
        data.save(f)
    for f in (destination/'playerdata').glob('*.dat*'):
        data=n.load(f);shift_player(data);data.save(f)
    for f in (destination/'dbv/fleet/dock').rglob('*.nbt'):
        data=n.load(f)
        if 'worldY' in data:data['worldY']=n.Double(float(data['worldY'])+SHIFT)
        data.save(f)
    # Force-load pointers and tracking points in this inspected world refer to
    # unchanged virtual plots and X/Z holding chunks; they must NOT be lifted.
    after={str(p.relative_to(source)):hashlib.sha256(p.read_bytes()).hexdigest() for p in source.rglob('*') if p.is_file()}
    if after!=manifest:raise RuntimeError('Source changed during offline migration')
    report['source_unchanged']=True
    (destination/'dbv-height-migration.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps(report,indent=2))
    return report

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('source',type=Path);parser.add_argument('destination',type=Path);parser.add_argument('backup',type=Path)
    args=parser.parse_args();migrate(args.source,args.destination,args.backup)
