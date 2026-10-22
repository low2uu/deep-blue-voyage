"""Native cabin equipment geometry and offline previews; no game launch."""
import importlib.util,json,zipfile
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[2]
A=ROOT/'mods/dbv-world/src/main/resources/assets/dbv_world'
OUT=ROOT/'docs/art/cabin-models'
loader=importlib.util.spec_from_file_location('items',ROOT/'tools/item-models/generate.py')
items=importlib.util.module_from_spec(loader);loader.loader.exec_module(items)
designs={};boxes=[]
def box(x,y,z,w,h,d,c):
 e=len(boxes)*.0002
 boxes.append(([x-e,y-e,z-e],[x+w+e,y+h+e,z+d+e],c))
def bolts(x,y,z,w,h):
 for a,b in [(x,y),(x+w,y),(x,y+h),(x+w,y+h)]:
  box(a,b,z,.65,.65,.3,'edge');box(a+.12,b+.27,z-.02,.4,.12,.05,'dark')
def screen(x,y,z,w,h):
 box(x-.5,y-.5,z,w+1,h+1,.6,'black');box(x,y,z-.05,w,h,.1,'teal')
 for j in range(3):box(x+.5,y+.6+j*1.2,z-.12,w*(.4+j*.13),.3,.1,'aqua')
def save(id):
 designs[id]=list(boxes);boxes.clear()

# Console keeps the upstream 16x13x16 interaction envelope and facing.
box(0,0,0,16,2,16,'dark');box(.6,2,.6,14.8,8.5,14.8,'iron')
box(0,10.5,0,16,2.1,16,'dark');box(.5,10.7,.5,15,1.9,15,'copper')
screen(2,5,.5,7,4);box(10,5,.3,3,4,.3,'black')
for j,c in enumerate(['green','yellow','red']):box(10.5,5.5+j,0.1,1.8,.5,.3,c)
box(2,12.65,5,7,.2,6,'black');box(2.5,12.87,5.5,6,.1,5,'teal')
for j in range(4):box(3,12.98,6+j,3+j*.4,.02,.22,'aqua')
for x in [11,13]:
 for z in [5,7,9]:box(x,12.65,z,1,.3,1,'cream')
bolts(1,2.5,.5,13,7);box(1,3,15.4,14,5,.2,'dark')
for x in range(2,14,2):box(x,3.5,15.6,.6,4,.15,'black')
save('jiuhu_console')
# A backed monitor cabinet: full cube retained for existing collision/sealing behavior.
box(0,0,0,16,16,16,'dark');box(.4,.4,0,15.2,15.2,.6,'iron')
screen(2,5,-.05,12,8);box(2,1.8,-.2,8,1.5,.4,'black')
for x in range(3,10,2):box(x,2.2,-.3,1,.6,.2,'cream')
box(12,2,-.3,1.3,1.3,.3,'green');bolts(.8,.8,-.35,13.4,13.4)
for x in [0,15.8]:box(x,2,4,.2,12,8,'copper')
box(2,2,15.9,12,12,.1,'black');save('cabin_panel')

def hatch(upper=False,locked=False):
 box(0,0,0,16,16,3,'dark');box(.7,.5,.2,14.6,15,2.6,'iron')
 for x in [0.7,14.3]:box(x,0,-.15,1,16,.5,'copper')
 box(2,1,-.25,12,1,.5,'edge')
 if upper:
  box(3,5,-.35,10,8,.4,'black');box(4,6,-.45,8,6,.2,'blue')
  box(5,7,-.52,1,4,.1,'ice');box(4,11,-.52,7,.4,.1,'aqua')
  box(11,1.5,-.6,2,2,.7,'green' if locked else 'orange')
 else:
  for x,y,w,h in [(5,7,6,1),(5,12,6,1),(5,8,1,4),(10,8,1,4)]:box(x,y,-1,w,h,.8,'copper')
  box(7.5,8,-1.3,1,4,.4,'edge');box(6,9.5,-1.3,4,1,.4,'edge')
  for y in [3,5]:box(3,y,-.4,10,.5,.5,'dark')
 bolts(1.7,2,-.5,11.7,11)
 geo=list(boxes);boxes.clear();return geo

def hangar(core=False,locked=False,opened=False):
 if opened:
  # Retracted sections remain addressable, but have no collision or sealing.
  for x in [0,15.5]:box(x,0,14,.5,16,2,'dark')
  if core:screen(1,6,14,5,5)
 else:
  box(0,0,0,16,16,16,'dark');box(.3,.3,0,15.4,15.4,.7,'iron')
  for x in [1,13]:box(x,0,-.2,2,16,.6,'copper')
  for y in [2,7,12]:box(3,y,-.15,10,1.3,.4,'edge')
  for y in range(1,15,3):
   box(1.3,y,-.35,1.4,1.5,.3,'yellow');box(13.3,y+1,-.35,1.4,1.5,.3,'black')
  bolts(3.5,1,-.5,8,13)
  if core:
   screen(4.5,6,-.5,7,6);box(6,2,-.6,4,2,.5,'green' if locked else 'orange')
  else:box(7,7,-.4,2,2,.4,'green' if locked else 'orange')
 geo=list(boxes);boxes.clear();return geo

def write_model(id,geo):
 model=items.model_json(geo,False);model['parent']='minecraft:block/block';model['gui_light']='side'
 (A/f'models/block/{id}.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
def dump(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def main():
 OUT.mkdir(parents=True,exist_ok=True)
 for id,geo in designs.items():
  model=items.model_json(geo,False);model['parent']='minecraft:block/block';model['gui_light']='side'
  (A/f'models/block/{id}.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
  (A/f'models/item/{id}.json').write_text(json.dumps({'parent':f'dbv_world:block/{id}'},indent=2)+'\n',encoding='utf-8')
  (A/f'blockstates/{id}.json').write_text(json.dumps({'variants':{f'facing={f}':{'model':f'dbv_world:block/{id}','y':angle} for f,angle in [('north',0),('east',90),('south',180),('west',270)]}},indent=2)+'\n',encoding='utf-8')
  flipped=[([16-b[0],a[1],16-b[2]],[16-a[0],b[1],16-a[2]],c) for a,b,c in geo]
  items.render(flipped,400).save(OUT/(id+'.png'))
 # Vanilla 1.21.1 door orientation, including separate hinge pivots when open.
 vanilla={'variants':{}}
 for facing,angle in [('east',0),('north',270),('south',90),('west',180)]:
  for half,label in [('lower','bottom'),('upper','top')]:
   for hinge in ['left','right']:
    for opened in [False,True]:
     key=f'facing={facing},half={half},hinge={hinge},open={str(opened).lower()}'
     vanilla['variants'][key]={'model':f'minecraft:block/iron_door_{label}_{hinge}'+('_open' if opened else ''),'y':(angle+((90 if hinge=='left' else 270) if opened else 0))%360}
 variants={}
 for key,value in vanilla['variants'].items():
  for locked in [False,True]:
   old=value['model'].split('/')[-1].replace('iron_door_','');id='personnel_hatch_'+old+('_locked' if locked else '')
   geo=hatch('top' in old,locked)
   if 'right' in old:geo=[([16-b[0],a[1],a[2]],[16-a[0],b[1],b[2]],c) for a,b,c in geo]
   # Match vanilla door plane (x=0..3) and orientation for every hinge/open variant.
   geo=[([a[2],a[1],16-b[0]],[b[2],b[1],16-a[0]],c) for a,b,c in geo]
   write_model(id,geo);variants[key+',locked='+str(locked).lower()]={**value,'model':'dbv_world:block/'+id}
 dump(A/'blockstates/personnel_hatch.json',{'variants':variants})
 full=hatch(False,True)+[([a[0],a[1]+16,a[2]],[b[0],b[1]+16,b[2]],c) for a,b,c in hatch(True,True)]
 itemgeo=[([4+a[0]*.5,a[1]*.5,7+a[2]*.5],[4+b[0]*.5,b[1]*.5,7+b[2]*.5],c) for a,b,c in full]
 dump(A/'models/item/personnel_hatch.json',items.model_json(itemgeo,False));designs['personnel_hatch']=itemgeo
 for id in ['hangar_door_core','hangar_door_panel']:
  variants={}
  for opened in [False,True]:
   for locked in [False,True]:
    suffix=('_open' if opened else '')+('_locked' if locked else '')
    write_model(id+suffix,hangar(id.endswith('core'),locked,opened))
    for f,angle in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={f},open={str(opened).lower()},locked={str(locked).lower()}']={'model':'dbv_world:block/'+id+suffix,'y':angle}
  dump(A/f'blockstates/{id}.json',{'variants':variants});dump(A/f'models/item/{id}.json',{'parent':'dbv_world:block/'+id})
  designs[id]=hangar(id.endswith('core'),True,False)
 for id in ['personnel_hatch','hangar_door_core','hangar_door_panel']:
  pool={'rolls':1,'entries':[{'type':'minecraft:item','name':'dbv_world:'+id}],'conditions':[{'condition':'minecraft:survives_explosion'}]}
  if id=='personnel_hatch':pool['conditions'].append({'condition':'minecraft:block_state_property','block':'dbv_world:'+id,'properties':{'half':'lower'}})
  dump(ROOT/f'mods/dbv-world/src/main/resources/data/dbv_world/loot_table/blocks/{id}.json',{'type':'minecraft:block','pools':[pool]})
 for lang,labels in [('zh_cn',['人员密闭舱门','潜艇大舱门核心','潜艇大舱门结构板']),('en_us',['Personnel Hatch','Submarine Hangar Door Core','Submarine Hangar Door Panel'])]:
  path=A/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'))
  for id,label in zip(['personnel_hatch','hangar_door_core','hangar_door_panel'],labels):data['block.dbv_world.'+id]=label
  dump(path,data)
 names=['酒壶的控制台','舱内信息面板','人员舱门（1×2格）','大舱门核心','大舱门结构板']
 font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',23)
 sheet=Image.new('RGB',(1500,450),'#10232e');draw=ImageDraw.Draw(sheet)
 for i,(id,geo) in enumerate(designs.items()):
  flipped=[([16-b[0],a[1],16-b[2]],[16-a[0],b[1],16-a[2]],c) for a,b,c in geo]
  pic=items.render(flipped,280);sheet.paste(pic,(i*300,65),pic);draw.text((i*300+10,365),names[i],font=font,fill='#ddd6c3');pic.save(OUT/(id+'.png'))
 draw.text((20,15),'舱内设备与舱门 · 离线几何预览（非游戏截图）',font=font,fill='#ddd6c3');sheet.save(OUT/'cabin-model-contact-sheet.png')
 print('Generated five block/item designs, door states, loot and previews.')
if __name__=='__main__':main()
