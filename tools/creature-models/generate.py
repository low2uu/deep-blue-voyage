"""Editable voxel rigs -> client ModelPart geometry, palette, specs and offline previews.

Coordinates in this source: +Y up, nose toward -Z, one block = 16 units.
Each joint has a world-space rest pivot; export converts to Minecraft local +Y-down.
This script never launches Minecraft. Requires Pillow and NumPy for preview rendering.
"""
import importlib.util
import json
import math
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT/'mods/dbv-world/src/main/resources/assets/dbv_world'
OUT = ROOT/'docs/art/creature-models'
PALETTE = {'iron':'#81949d','edge':'#d0dce0','dark':'#293e49','black':'#17272f',
 'copper':'#b87948','gold':'#e8b75e','rust':'#815142','rubber':'#384942',
 'teal':'#397e84','aqua':'#81d5d2','blue':'#438bb3','ice':'#d3f1ed',
 'violet':'#8175b5','lilac':'#c3b9ee','bone':'#cbbfa0','cream':'#f0dfb5',
 'red':'#a65251','rose':'#df9c8f','orange':'#ed9850','yellow':'#f3d477',
 'green':'#618c60','lime':'#b4cc70','paper':'#decfa8','ink':'#304b5b'}
RIGS = {}
parts = []
active = None


def part(name, pivot=(0,0,0), parent='root', axis='y', amplitude=0, rate=.12, phase=0, motion='wave'):
    global active
    active={'name':name,'parent':parent,'pivot':list(pivot),'axis':axis,'amplitude':amplitude,
            'rate':rate,'phase':phase,'motion':motion,'boxes':[]}
    parts.append(active)
    return name


def box(x,y,z,w,h,d,color):
    assert min(w,h,d)>0
    active['boxes'].append({'from':[x,y,z],'size':[w,h,d],'color':color})


def eye(x,y,z, color='yellow', size=1.5):
    box(x-.3,y-.3,z-.05,size+.6,size+.6,.5,'black')
    box(x,y,z-.2,size,size,.25,color)
    box(x+.2,y+size*.5,z-.3,.35,.35,.15,'ice')


def band(x,y,z,w,h,d,color):
    box(x,y,z,w,.6,d,color);box(x,y+h-.6,z,w,.6,d,color)
    box(x,y+.6,z,.6,h-1.2,d,color);box(x+w-.6,y+.6,z,.6,h-1.2,d,color)


def tail(parent='body', start=(0,6,5), count=4, color='teal', thickness=2, length=4):
    x,y,z=start
    for i in range(count):
        p=part('tail_'+str(i),(x,y,z+i*length),parent,axis='y',amplitude=.18,phase=-i*.6)
        t=max(.7,thickness-i*.3);box(x-t/2,y-t/2,z+i*length,t,t,length+.1,color)
        parent=p
    return parent


def fins(y=6,z=0,color='teal',wide=6):
    for sign in (-1,1):
        part('fin_'+str(sign),(sign*3,y,z),'body',axis='z',amplitude=.18,phase=0 if sign<0 else math.pi)
        for j in range(3):
            x=3+j*(wide/3);a=x if sign>0 else -x-wide/3
            box(a,y-j*.3,z+j,wide/3+.1,.8,5-j,color)


def crab(heavy=False):
    c='teal' if heavy else 'rust'
    part('body',(0,5,0));box(-5,3,-4,10,4,8,c);box(-4,7,-3,8,1.5,6,'iron' if heavy else 'copper')
    for x in (-3,0,3):box(x-.4,7.8,-2,.8,1.1,4,'gold' if heavy else 'rust')
    for s in (-1,1):
        for j in range(4):
            z=-3+j*2
            part(f'leg_{s}_{j}',(s*4,4,z),'body',axis='z',amplitude=.17,phase=j*.9+(math.pi if s<0 else 0))
            box(4 if s>0 else -9,2.5,z,5,1.2,1,c)
            box(8 if s>0 else -9,0,z,1,3.5,1,'dark')
        part(f'eye_{s}',(s*3,7,-3),'body',axis='x',amplitude=.045,phase=s)
        box(s*3-.5,7,-3,1,2,1,'dark');eye(s*3-.65,8.4,-3.4,'aqua' if heavy else 'yellow',1.3)
        part(f'claw_{s}',(s*4,5,-3),'body',axis='y',amplitude=.10,phase=s)
        big=1.5 if heavy and s>0 else 1
        x=4 if s>0 else -4-3*big
        box(x,3,-8,3*big,3,5,c);box(x,3,-11,1,3,4,'bone')
        part(f'pincer_{s}',(x+3*big,4,-8),f'claw_{s}',axis='y',amplitude=.16,phase=-s,rate=.09)
        box(x+2*big,3,-11,1,3,4,'copper' if heavy else 'cream')
    if heavy:
        part('back_armor',(0,7,2),'body')
        for j in range(3):box(-5+j,6+j*.7,j,10-2*j,2,2,'dark');box(-.5,8+j*.7,j,1,2,1,'gold')


def eel(ice=False):
    c='ice' if ice else 'teal'
    part('body',(0,5,0));box(-2.5,3,-5,5,4,11,c);box(-2,2.6,-4,4,.5,10,'lilac' if ice else 'copper')
    part('head',(0,5,-4),'body',motion='look',amplitude=.14)
    box(-3,3,-9,6,5,5,c);box(-2,3.5,-9.4,4,1.3,.5,'black')
    eye(-2.7,6,-9.1,'blue' if ice else 'orange',1);eye(1.6,6,-9.1,'blue' if ice else 'orange',1)
    for x in (-1.4,.4):box(x,3.8,-9.6,.7,1,.3,'cream')
    if ice:
        for s in (-1,1):box(s*3-.2,5,-12,.4,.5,4,'blue')
    else:
        for z in (-3,1,4):band(-2.7,2.8,z,5.4,4.4,.8,'copper')
    last=tail(start=(0,5,6),count=5,color=c,thickness=3,length=3)
    part('tail_fin',(0,5,21),last,axis='y',amplitude=.12)
    box(-3,4.8,21,6,.5,3,'blue' if ice else 'aqua')
    part('dorsal',(0,7,0),'body',axis='z',amplitude=.07)
    for i in range(4):box(-.35,7,-3+i*3,.7,1.3+(i%2),2,'blue' if ice else 'aqua')


def ray(boss=False):
    part('body',(0,5,0));box(-4,3,-5,8,4,10,'dark' if boss else 'teal');box(-3,7,-3,6,1,6,'blue' if boss else 'aqua')
    part('head',(0,5,-4),'body',motion='look',amplitude=.08)
    box(-3,4,-7,6,2,3,'teal');eye(-2.7,6,-6.5,'gold' if boss else 'ice',1.3);eye(1.4,6,-6.5,'gold' if boss else 'ice',1.3)
    for s in (-1,1):
        part(f'wing_{s}',(s*3,5,0),'body',axis='z',amplitude=.22 if boss else .3,phase=0 if s>0 else math.pi,rate=.09)
        for j in range(4 if boss else 3):
            x=3+j*3 if s>0 else -6-j*3
            box(x,4.5,-5+j,3.1,1.2,10-j*2,'blue' if boss else 'teal')
            box(x,5.72,-4+j,3,.25,.5,'gold' if boss else 'aqua')
        if boss:
            box(12 if s>0 else -15,4.8,-2,3,1,2,'gold')
    tail(start=(0,5,5),count=5 if boss else 4,color='gold' if boss else 'teal',thickness=1.5,length=3)
    if boss:
        part('crest',(0,8,0),'body',axis='y',amplitude=.05)
        for x in (-2,0,2):box(x-.4,8,-1,.8,3+(x==0)*2,2,'gold')


def fish(kind):
    white=kind=='whiteout';angler=kind=='angler';c='ice' if white else 'dark' if angler else 'blue'
    part('body',(0,6,0));box(-4,3,-4,8,7,10,c);box(-3,2.5,-3,6,1,8,'lilac' if white else 'teal')
    part('head',(0,7,-3),'body',motion='look',amplitude=.12)
    box(-5,4,-8,10,6,5,c);box(-4,4.5,-8.3,8,3,.4,'black')
    eye(-4.4,8,-8.4,'blue' if white else 'yellow',1.6);eye(2.8,8,-8.4,'blue' if white else 'yellow',1.6)
    for x in (-3,-1,1,3):box(x,6.4,-8.7,.6,1.7,.5,'cream')
    part('jaw',(0,4,-5),'head',axis='x',amplitude=.06,motion='jaw')
    box(-4,3.5,-9,8,1,4,c)
    for x in (-2,0,2):box(x,4.5,-8.7,.7,1.4,.5,'cream')
    if kind=='ram':
        part('horn',(0,8,-7),'head')
        for i in range(4):box(-1.5+i*.3,7.5+i*.2,-10-i*2,3-i*.6,2-i*.3,2.2,'gold' if i>1 else 'bone')
    if angler:
        part('lure',(0,10,-4),'head',axis='z',amplitude=.10,rate=.07)
        box(-.4,10,-5,.8,5,.8,'teal');box(-.4,14,-10,.8,.8,6,'teal');box(-.4,12,-10,.8,3,.8,'teal')
        box(-1.5,10.5,-11,3,3,3,'yellow');box(-1,13.5,-10.5,2,.5,2,'ice')
    fins(y=6,z=0,color='ice' if white else 'teal',wide=10 if white else 5)
    last=tail(start=(0,6,6),count=3,color=c,thickness=3,length=3)
    part('tail_fin',(0,6,15),last,axis='y',amplitude=.15)
    box(-3,5.5,14,6,1,4,'blue' if white else 'aqua');box(-4,5.5,17,2,1,3,c);box(2,5.5,17,2,1,3,c)
    part('crest',(0,10,0),'body',axis='z',amplitude=.05)
    for i in range(3):box(-.5,10,-2+i*3,1,3-i*.7,2,'lilac' if white else 'iron')
    if white:
        for s in (-1,1):
            box(s*4-.5,10,-4,1,5,1,'cream');box(s*4-.5,14,-4,1,1,4,'cream');box(s*4-.5,14,-1,1,2,1,'ice')
        for z in (-2,1,4):band(-4.15,3,z,8.3,7.2,.6,'cream')


def jelly():
    part('body',(0,11,0),motion='pulse',amplitude=.05,rate=.1)
    box(-5,9,-5,10,3,10,'violet');box(-4,12,-4,8,2,8,'lilac');box(-2.5,14,-2.5,5,1,5,'aqua')
    box(-5.4,8.5,-5.4,10.8,.8,10.8,'aqua');box(-2,8.7,-5.8,4,1,.5,'ice')
    for i,(x,z) in enumerate([(-3,-3),(0,-3),(3,-3),(-3,0),(3,0),(-3,3),(0,3),(3,3)]):
        name=part(f'tentacle_{i}',(x,9,z),'body',axis='z' if i%2 else 'x',amplitude=.18,phase=i*.7,rate=.1)
        box(x-.5,4,z-.5,1,5,1,'violet');box(x-.6,6,z-.6,1.2,.5,1.2,'aqua')
        part(f'tentacle_tip_{i}',(x,4,z),name,axis='x' if i%2 else 'z',amplitude=.23,phase=i*.7-.7,rate=.1)
        box(x-.4,1+(i%2),z-.4,.8,3-(i%2),.8,'lilac')


def worm():
    part('body',(0,3,-5));box(-2.5,1,-7,5,4,4,'green')
    band(-2.5,1,-7.4,5,4,.6,'rose');box(-1.5,2,-7.6,3,2,.2,'black')
    for x in (-1,1):box(x,2.1,-8,.5,1.2,.4,'cream')
    parent='body'
    for i in range(6):
        z=-3+i*2.5;p=part(f'segment_{i}',(0,3,z),parent,axis='y',amplitude=.17,phase=-i*.6)
        box(-2+i*.18,1.5+i*.08,z,4-i*.36,3-i*.16,2.8,'green' if i%2 else 'lime')
        box(-2+i*.18,2,z,4-i*.36,.5,1,'rose');parent=p


def shrimp():
    part('body',(0,5,0));box(-3,3,-4,6,4,7,'blue')
    for i in range(4):box(-3+i*.35,5-i*.3,-2+i*2,6-i*.7,2,2.2,'ice' if i%2 else 'aqua')
    part('head',(0,5,-3),'body',motion='look',amplitude=.15);box(-2.5,3.5,-7,5,3,4,'ice')
    eye(-2.4,6,-6.8,'violet',1);eye(1.4,6,-6.8,'violet',1)
    for s in (-1,1):
        part(f'antenna_{s}',(s,6,-6),'head',axis='y',amplitude=.18,phase=s)
        box(s-.2,6,-12,.4,.5,6,'aqua');box(s*3-.2,6,-15,.4,.5,4,'aqua');box(min(s,s*3),6,-12,2,.5,.5,'aqua')
        for j in range(4):
            part(f'leg_{s}_{j}',(s*2,4,-2+j*2),'body',axis='z',amplitude=.2,phase=j+s)
            box(2 if s>0 else -5,2,-2+j*2,3,.7,.7,'lilac');box(4.3 if s>0 else -5,0,-2+j*2,.7,2.7,.7,'ice')
    last=tail(start=(0,4,4),count=2,color='blue',thickness=2.5,length=2)
    part('tail_fan',(0,4,8),last,axis='x',amplitude=.15);box(-4,3.5,8,8,.6,3,'ice')


def stalker():
    part('body',(0,6,0));box(-4,3,-4,8,5,9,'violet');box(-3,8,-3,6,2,7,'iron')
    for i in range(3):box(-3+i*.5,9,-2+i*2,6-i,1.5,1,'ice')
    part('head',(0,6,-4),'body',motion='look',amplitude=.15);box(-4,4,-8,8,4,5,'dark')
    eye(-2.8,6.5,-8.3,'blue',1);eye(1.8,6.5,-8.3,'blue',1)
    box(-2,4.5,-8.4,4,1,.5,'black')
    for s in (-1,1):
        part(f'ear_{s}',(s*3,8,-4),'head',axis='z',amplitude=.09,phase=s)
        box(3 if s>0 else -7,8,-5,4,5,1,'lilac');box(4 if s>0 else -6,9,-5.1,2,3,.3,'aqua')
        for j in range(3):
            part(f'leg_{s}_{j}',(s*3,5,-3+j*3),'body',axis='z',amplitude=.16,phase=j+s)
            box(3 if s>0 else -9,3,-4+j*3,6,1.4,1.4,'iron');box(8 if s>0 else -9,0,-4+j*3,1,4,1.4,'ice')
        part(f'claw_{s}',(s*3,5,-6),'head',axis='y',amplitude=.10,phase=s)
        box(s*3-.6,1.5,-10,1.2,4,4,'ice')


def diver():
    part('body',(0,18,0));box(-4,11,-2.5,8,10,5,'rubber');box(-4.5,11,-3,9,1.5,6,'copper')
    box(-2.5,15,-3,5,4,.8,'iron');box(-1.4,16,-4,2.8,2,.5,'aqua')
    for x in (-3,1):box(x,12,2.5,2.5,8,3,'copper');box(x+.5,20,3,1.5,1,2,'gold')
    part('head',(0,21,0),'body',motion='look',amplitude=1)
    box(-4.5,21,-4,9,8,8,'copper');box(-3.5,29,-3,7,1,6,'rust');box(-5,21,-4.5,10,1,9,'gold')
    band(-3.5,23,-4.3,7,4.5,.6,'gold');box(-2.5,24,-4.5,5,2.5,.3,'ink');box(-1.8,24.4,-4.7,1,1.5,.2,'aqua')
    for s in (-1,1):
        name='right_arm' if s<0 else 'left_arm';part(name,(s*5,20,0),'body',axis='x',amplitude=.4,phase=0 if s<0 else math.pi,motion='arm')
        box(s*5-1.5,12,-1.5,3,8,3,'rubber');box(s*5-1.7,10.5,-1.7,3.4,2,3.4,'iron')
        part('right_leg' if s<0 else 'left_leg',(s*2,12,0),'body',axis='x',amplitude=.45,phase=math.pi if s<0 else 0,motion='leg')
        box(s*2-1.6,3,-1.6,3.2,9,3.2,'rubber');box(s*2-2,0,-4,4,3,6,'dark');box(s*2-2,0,-4,4,.8,6,'copper')
    part('hose',(5,22,1),'body');box(4,18,2,1,6,1,'black');box(3,23,1,2,1,2,'black')


def bell():
    part('body',(0,7,0));box(-5,3,-5,10,8,10,'copper');box(-4,11,-4,8,3,8,'rust');box(-3,14,-3,6,1.5,6,'gold')
    box(-6,2,-6,12,2,12,'gold');box(-4,1,-4,8,1,8,'dark')
    for x in (-4,0,4):box(x-.4,4,-5.3,.8,7,.5,'gold')
    eye(-1.5,7,-5.7,'aqua',3)
    part('crown',(0,15,0),'body',axis='y',amplitude=.04)
    band(-2,15,-1,4,4,2,'dark')
    for s in (-1,1):
        part(f'arm_{s}',(s*5,8,0),'body',axis='z',amplitude=.10,phase=0 if s>0 else math.pi)
        box(5 if s>0 else -10,6,-1,5,2,2,'dark');box(s*9-1,2,-1,2,6,2,'iron')
        box(s*9-3,1,-1.5,6,1.5,3,'copper')
        part(f'chain_{s}',(s*3,2,-2),'body',axis='z',amplitude=.13,phase=s,rate=.07)
        for j in range(3):band(s*3-.6,-j*1.3,-2,1.2,1.4,.7,'dark')


def finish(id, description):
    global parts
    assert len({p['name'] for p in parts})==len(parts)
    # Give overlapping decorative parts distinct exterior planes, also in the preview.
    for index,b in enumerate(b for p in parts for b in p['boxes']):
        epsilon=index*.0003
        b['from']=[v-epsilon for v in b['from']]
        b['size']=[v+epsilon*2 for v in b['size']]
    positions={'root':[0,0,0]};result=[]
    for p in parts:
        assert p['parent'] in positions,p['name'];parent=positions[p['parent']];pivot=p['pivot'];positions[p['name']]=pivot
        cubes=[]
        for index,b in enumerate(p['boxes']):
            x,y,z=b['from'];w,h,d=b['size'];epsilon=0
            i=list(PALETTE).index(b['color']);u=i%8*128+4;v=i//8*128+4
            assert 2*(w+d)+8<128 and h+d+8<128,(id,b)
            cubes.append({'origin':[round(x-pivot[0]-epsilon,5),round(-(y+h-pivot[1])-epsilon,5),round(z-pivot[2]-epsilon,5)],
                          'size':[round(w+epsilon*2,5),round(h+epsilon*2,5),round(d+epsilon*2,5)],'uv':[u,v]})
        result.append({'name':p['name'],'parent':p['parent'],
                       'pivot':[pivot[0]-parent[0],-(pivot[1]-parent[1]),pivot[2]-parent[2]],
                       'cubes':cubes,'axis':p['axis'],'amplitude':p['amplitude'],'rate':p['rate'],
                       'phase':p['phase'],'motion':p['motion']})
    allboxes=[b for p in parts for b in p['boxes']]
    radius=max(math.sqrt(sum(max(abs(b['from'][i]),abs(b['from'][i]+b['size'][i]))**2 for i in range(3))) for b in allboxes)/16+1
    RIGS[id]={'description':description,'source':parts,'data':{'format':'dbv-creature-model','version':1,
          'texture':'dbv_world:textures/entity/ocean_palette.png','textureWidth':1024,'textureHeight':512,
          'cullingRadius':round(radius,3),'parts':result}}
    parts=[]


def main():
    crab();finish('rust_shell_crab','锈色扇形甲壳、八足、眼柄与可摆动双钳。')
    eel();finish('cable_eel','蓝绿长鳗、铜色缆绳环节、细背鳍与五段尾部。')
    diver();finish('wreck_diver','旧式铜盔潜水服、窥视窗、双背罐、软管、护手和配重靴。')
    ray();finish('current_ray','流线鳐身、分层宽翼、浅色翼缘与细鞭尾。')
    jelly();finish('propeller_jellyfish','紫蓝阶梯伞盖、浅色伞缘和八组双节触须。')
    fish('ram');finish('ramfish','蓝色重头鱼、前伸骨角、背棘、胸鳍和叉尾。')
    crab(True);finish('split_shell_beast','深色重叠甲片、外露金色关节和不对称重钳。')
    fish('angler');finish('lantern_lurker','深海灯笼鱼，宽口尖牙、独立下颌、弯曲灯须与明亮灯囊。')
    worm();finish('hull_worm','绿色分节蠕虫，粉色吸附口和六段可摆动腹部。')
    eel(True);finish('ice_fissure_eel','冰白细鳗，蓝色背鳍、前须和分节尾部。')
    shrimp();finish('mirror_shrimp','镜蓝叠甲虾、八足、两根长触角与扇形尾。')
    stalker();finish('ice_cave_stalker','六足伏猎者，低伏骨甲、展开的听觉膜和两只前爪。')
    ray(True);finish('vortex_ray','深蓝巨鳐，金色翼脉、冠状背鳍、加宽四节翼与长尾。')
    bell();finish('sunken_bell_guardian','沉钟式铜质机械守卫，钟冠、青色观察眼、锚形臂和悬链。')
    fish('whiteout');finish('whiteout_hunter','冰白骨甲猎手，分节肋甲、头部冰角、长刃胸鳍与獠牙。')
    specs={p.stem:p for p in (ROOT/'content/prototypes/mobs').glob('*.json')}
    assert set(specs)==set(RIGS)
    (ASSETS/'models/entity').mkdir(parents=True,exist_ok=True);(ASSETS/'textures/entity').mkdir(parents=True,exist_ok=True);OUT.mkdir(parents=True,exist_ok=True)
    palette=Image.new('RGB',(1024,512),'#17272f');draw=ImageDraw.Draw(palette)
    for i,color in enumerate(PALETTE.values()):draw.rectangle([i%8*128,i//8*128,i%8*128+127,i//8*128+127],fill=color)
    palette.save(ASSETS/'textures/entity/ocean_palette.png')
    # Reuse the deterministic depth renderer; no game or external image service involved.
    loader=importlib.util.spec_from_file_location('item_preview',ROOT/'tools/item-models/generate.py');preview=importlib.util.module_from_spec(loader);loader.loader.exec_module(preview)
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',22);small=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',14)
    sheet=Image.new('RGB',(1500,5*320+100),'#10232e');sd=ImageDraw.Draw(sheet)
    sd.text((28,20),'深蓝远航 · 15 种海洋生物专属模型',font=font,fill='#e4dec7')
    sd.text((28,56),'静态几何预览 · 非游戏截图 · 各格独立缩放，不代表生物间实际比例',font=small,fill='#a9c5cb')
    docs=['# 怪物模型目录','','12种普通怪与3个Boss的专属模型。沿用原型AI，未实现草案专属技能；本次不启动游戏验收。',
          '','![静态模型总览](creature-model-contact-sheet.png)','','| 生物 | 几何文件 | 外观 |','| --- | --- | --- |']
    for index,(id,rig) in enumerate(RIGS.items()):
        (ASSETS/f'models/entity/{id}.json').write_text(json.dumps(rig['data'],ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        spec=json.loads(specs[id].read_text(encoding='utf-8'))
        for key,val in {'model':'custom','visualDescription':rig['description'],
                        'textureAll':'dbv_world:entity/ocean_palette','assetReferences':[f'dbv_world:models/entity/{id}.json'],
                        'animations':'原生关节动画：鳍翼、尾部、步足、触须或手臂按模型配置摆动；无攻击命中结算。'}.items():
            spec['sections']['appearance'][key]={'status':'specified','value':val,'reference':'tools/creature-models/generate.py','notes':'专属模型；未进行游戏内验收。'}
        # Palette is non-square; the old square-only field does not describe this atlas.
        spec['sections']['appearance']['textureResolution']={'status':'not_applicable','value':None,'reference':'','notes':'共用非方形材质图1024×512，实际尺寸在几何JSON内。'}
        base=spec['sections']['monster']['base']['value']
        spec['sections']['monster']['implementedBehavior']['value']=f'使用专属模型和基础关节动画；行为仍沿用 {base} 原型，尚无目标特殊技能、首杀、多人缩放或船体伤害。'
        specs[id].write_text(json.dumps(spec,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        boxes=[b for p in rig['source'] for b in p['boxes']]
        # Rotate the preview 180 degrees around Y to show the forward (-Z) face.
        geo=[]
        for b in boxes:
            x,y,z=b['from'];w,h,d=b['size'];geo.append(([-x-w,y,-z-d],[-x,y+h,-z],b['color']))
        lo=[min(b[0][i] for b in geo) for i in range(3)];hi=[max(b[1][i] for b in geo) for i in range(3)]
        scale=15/max(hi[i]-lo[i] for i in range(3))
        norm=[([8+(a[i]-(lo[i]+hi[i])/2)*scale for i in range(3)],[8+(b[i]-(lo[i]+hi[i])/2)*scale for i in range(3)],c) for a,b,c in geo]
        icon=preview.render(norm,280);x=index%3*500;y=100+index//3*320
        sd.rounded_rectangle([x+8,y+4,x+492,y+312],radius=12,fill='#1b3440');sheet.paste(icon,(x+110,y-4),icon)
        zh=spec['sections']['identity']['nameZh']['value'];sd.text((x+22,y+260),zh,font=font,fill='#e6e1cf');sd.text((x+22,y+292),id,font=small,fill='#8eb2bd')
        icon.save(OUT/(id+'.png'))
        docs.append(f'| {zh} | `models/entity/{id}.json` | {rig["description"]} |')
    sheet.save(OUT/'creature-model-contact-sheet.png')
    (OUT/'怪物模型目录-creature-model-catalog.md').write_text('\n'.join(docs)+'\n',encoding='utf-8')
    print('Generated 15 hierarchical creature rigs, palette, design JSON and static previews.')


if __name__=='__main__':main()
