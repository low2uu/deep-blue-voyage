"""Generate localized interactions, design specifications and block metadata."""
from pathlib import Path
import json,copy
ROOT=Path(__file__).resolve().parents[2];RES=ROOT/'mods/dbv-world/src/main/resources'
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
messages={
 'permission':('没有操作这扇舱门的权限。','You cannot operate this hatch.'),
 'incomplete':('人员舱门结构不完整，请重新放置。','The personnel hatch is incomplete; replace it.'),
 'facing':('大舱门的核心与结构板必须朝向一致。','The core and all panels must face the same direction.'),
 'unloaded':('附近区块未完整加载，无法安全操作。','Nearby chunks are not fully loaded.'),
 'limit':('连接结构超过4096块，请拆分为独立舱门。','More than 4096 connected sections; separate the doors.'),
 'core':('每组相连的大舱门结构必须恰好有一个核心。','Each connected hangar door needs exactly one core.'),
 'mixed':('门板开闭状态不一致，请先拆除新接入或异常的门板。','Door panels have inconsistent open states; remove newly attached or inconsistent panels.'),
 'close_first':('请先关闭舱门，再上锁密封。','Close the hatch before locking it.'),
 'unlock_first':('舱门已锁定。空手潜行右键，检查隔离后解锁。','Hatch locked. Sneak-use with an empty hand to check isolation and unlock.'),
 'unlocked':('已解锁：当前不气密。空手右键再次检查并开启。','Unlocked: no longer airtight. Use with an empty hand to check again and open.'),
 'locked':('舱门已上锁密封；整舱气密性仍由实际结构检测。','Hatch locked and sealed; room integrity depends on the surrounding structure.'),
 'closed':('已关闭，尚未气密。空手潜行右键上锁。','Closed but not sealed. Sneak-use with an empty hand to lock.'),
 'opened':('隔离检查通过，舱门已开启。','Isolation check passed; hatch opened.'),
 'occupied':('门口有实体，暂不能关闭。','An entity is in the doorway; cannot close.'),
 'no_space':('门两侧没有可识别通道，请检查布局。','No accessible passage was found beside the hatch.'),
 'main_cabin':('开门会暴露中央控制器所在舱室。请先关闭并锁定内舱门。','Opening would expose the command compartment. Close and lock its inner hatch first.'),
 'isolation':('未确认独立且气密的主控舱，拒绝向外开门。请建立气闸并锁好内门。','No isolated sealed command compartment confirmed. Build an airlock and lock the inner hatch.'),
 'pending':('气密数据正在更新，请稍后重试。','Compartment data is updating; try again shortly.'),
 'scan_limit':('隔离检测超过预算，不能确认安全；请缩小气闸舱或检查漏口。','Isolation scan budget exceeded; reduce the chamber size or check for leaks.')}
for lang,index in [('zh_cn',0),('en_us',1)]:
 p=RES/f'assets/dbv_world/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for key,values in messages.items():data['message.dbv_world.door.'+key]=values[index]
 for key,values in {
  'hatch_controls':('空手右键开关；空手潜行右键上锁/解锁。','Empty-hand use: open/close. Sneak-use: lock/unlock.'),
  'hatch_seal':('关闭且上锁才气密；解锁和开启前检查舱室隔离。','Only closed and locked hatches seal; isolation is checked before unlocking/opening.'),
  'hangar_assembly':('同向六面连接，整组恰好一个核心；形状自由。','Connect same-facing sections by their faces, with exactly one core; freeform outline.')}.items():data['tooltip.dbv_world.'+key]=values[index]
 write(p,data)
ids=['personnel_hatch','hangar_door_core','hangar_door_panel']
p=RES/'data/minecraft/tags/block/mineable/pickaxe.json';data=json.loads(p.read_text(encoding='utf-8'))
data['values']=list(dict.fromkeys(data['values']+['dbv_world:'+i for i in ids]));write(p,data)
# Do not put these specs under prototypes/: that generator registers ordinary items and mobs only.
template=json.loads((ROOT/'tools/content-designer/iron-submarine-hull.example.json').read_text(encoding='utf-8'))
labels=[('jiuhu_console','酒壶的控制台',"Jiuhu's Console",3.5,'继承Deep Seas中央控制功能；仪表和按键外观。'),('cabin_panel','舱内信息面板','Cabin Information Panel',2.5,'主艇信息与存储终端；支持朝向放置。'),('personnel_hatch','人员密闭舱门','Personnel Hatch',5,'1×2格；关闭不气密，上锁才密封；解锁和开门均先检查隔离。'),('hangar_door_core','潜艇大舱门核心','Submarine Hangar Door Core',5,'一个核心管理相连的同向结构板；自由轮廓，整组开闭和锁定。'),('hangar_door_panel','潜艇大舱门结构板','Submarine Hangar Door Panel',5,'与核心六面连接；非固定尺寸，开启后收起且无碰撞。')]
for id,zh,en,hardness,desc in labels:
 d=copy.deepcopy(template)
 for section in d['sections'].values():
  for field in section.values():field.update(status='pending',value=None,reference='',notes='未确认的数值不继承铁质潜艇壳模板。')
 def setfield(sec,key,val):
  if key in d['sections'][sec]:d['sections'][sec][key]={'status':'specified','value':val,'reference':'tools/cabin-models','notes':'开发原型，未实机验收。'}
 for key,val in {'registryId':'dbv_world:'+id,'nameZh':zh,'nameEn':en,'kind':'block','category':'潜艇舱内设备','summary':desc}.items():setfield('identity',key,val)
 for key,val in {'model':'custom','visualDescription':desc,'assetReferences':[f'dbv_world:models/item/{id}.json'],'textureAll':'dbv_world:item/prototype_palette','textureResolution':128}.items():setfield('appearance',key,val)
 setfield('harvesting','hardness',hardness)
 setfield('protection','explosionResistance',6 if id in ids else hardness)
 setfield('item','maxStackSize',64)
 setfield('item','useBehavior',desc)
 d['metadata']['updatedAt']='2026-10-22T00:00:00.000Z'
 write(ROOT/f'content/cabin-equipment/{id}.json',d)
print('Generated bilingual messages, tool tags and five full-format design specifications.')
