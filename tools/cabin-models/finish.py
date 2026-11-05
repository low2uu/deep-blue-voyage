"""Generate localized interactions, design specifications and block metadata."""
from pathlib import Path
import json,copy
ROOT=Path(__file__).resolve().parents[2];RES=ROOT/'mods/dbv-world/src/main/resources'
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
messages={
 'permission':('没有操作这扇舱门的权限。','You cannot operate this hatch.'),
 'incomplete':('人员舱门结构不完整，请重新放置。','The personnel hatch is incomplete; replace it.'),
 'unloaded':('附近区块未完整加载，无法安全操作。','Nearby chunks are not fully loaded.'),
 'core':('每组相连的大舱门结构必须恰好有一个核心。','Each connected hangar door needs exactly one core.'),
 'close_first':('请先关闭舱门，再上锁密封。','Close the hatch before locking it.'),
 'unlock_first':('舱门已锁定。空手潜行右键解锁。','Hatch locked. Sneak-use with an empty hand to unlock.'),
 'unlocked':('已解锁：当前不气密。空手右键再次检查并开启。','Unlocked: no longer airtight. Use with an empty hand to check again and open.'),
 'locked':('舱门已上锁密封；整舱气密性仍由实际结构检测。','Hatch locked and sealed; room integrity depends on the surrounding structure.'),
 'closed':('已关闭，尚未气密。空手潜行右键上锁。','Closed but not sealed. Sneak-use with an empty hand to lock.'),
 'opened':('舱门已开启。','Hatch opened.'),
 'check_unavailable':('气密检查：当前不是物理船体，暂无舱室数据。','Airtightness check: no physical vessel compartment data.'),
 'check_pending':('气密检查：舱室数据更新中；本次操作继续。','Airtightness check: compartment data updating; operation continues.'),
 'check_sealed':('气密检查：相邻舱室当前密闭。','Airtightness check: adjacent compartments are currently sealed.'),
 'check_unsealed':('气密检查：未确认相邻舱室密闭；本次操作继续。','Airtightness check: adjacent compartments not confirmed sealed; operation continues.')}
for lang,index in [('zh_cn',0),('en_us',1)]:
 p=RES/f'assets/dbv_world/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for old in ['facing','limit','mixed','occupied','no_space','main_cabin','isolation','pending','scan_limit']:data.pop('message.dbv_world.door.'+old,None)
 for key,values in messages.items():data['message.dbv_world.door.'+key]=values[index]
 for key,values in {
  'hatch_controls':('空手右键开关；空手潜行右键上锁/解锁。','Empty-hand use: open/close. Sneak-use: lock/unlock.'),
  'hatch_seal':('关闭且上锁才气密；开门前显示气密检查结果，不强制拦截。','Only closed and locked hatches seal; pre-opening checks are informational.'),
  'hangar_assembly':('六面连接，整组恰好一个核心；形状自由。','Connect sections by their faces, with exactly one core; freeform outline.')}.items():data['tooltip.dbv_world.'+key]=values[index]
 write(p,data)
ids=['personnel_hatch','hangar_door_core','hangar_door_panel']
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';data=json.loads(p.read_text(encoding='utf-8'))
data['values']=list(dict.fromkeys(data['values']+['dbv_world:'+i for i in ids]));write(p,data)
# Do not put these specs under prototypes/: that generator registers ordinary items and mobs only.
template=json.loads((ROOT/'tools/content-designer/iron-submarine-hull.example.json').read_text(encoding='utf-8'))
labels=[('jiuhu_console','酒壶的控制台',"Jiuhu's Console",3.5,'继承Deep Seas中央控制功能；仪表和按键外观。'),('cabin_panel','舱内信息面板','Cabin Information Panel',2.5,'主艇信息与存储终端；支持朝向放置。'),('personnel_hatch','人员密闭舱门','Personnel Hatch',5,'1×2格；关闭不气密，上锁才密封；开门前显示气密检查结果。'),('hangar_door_core','潜艇大舱门核心','Submarine Hangar Door Core',5,'一个核心管理相连的结构板；自由轮廓，整组开闭和锁定。'),('hangar_door_panel','潜艇大舱门结构板','Submarine Hangar Door Panel',5,'与核心六面连接；非固定尺寸，开启后收起且无碰撞。')]
for id,zh,en,hardness,desc in labels:
 d=copy.deepcopy(template)
 for section in d['sections'].values():
  for field in section.values():field.update(status='pending',value=None,reference='',notes='未确认的数值不继承铁质潜艇壳模板。')
 def setfield(sec,key,val):
  if key in d['sections'][sec]:d['sections'][sec][key]={'status':'specified','value':val,'reference':'tools/cabin-models','notes':'开发原型，未实机验收。'}
 for key,val in {'registryId':'dbv_world:'+id,'nameZh':zh,'nameEn':en,'kind':'block','category':'潜艇舱内设备','summary':desc}.items():setfield('identity',key,val)
 for key,val in {'model':'custom','visualDescription':desc,'assetReferences':[f'dbv_world:models/item/{id}.json'],'textureAll':'dbv_world:item/prototype_palette','textureResolution':16}.items():setfield('appearance',key,val)
 setfield('harvesting','hardness',hardness)
 setfield('protection','explosionResistance',6 if id in ids else hardness)
 setfield('item','maxStackSize',64)
 setfield('item','useBehavior',desc)
 d['metadata']['updatedAt']='2026-10-22T00:00:00.000Z'
 write(ROOT/f'content/cabin-equipment/{id}.json',d)
print('Generated bilingual messages, tool tags and five full-format design specifications.')
