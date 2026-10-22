"""Build native cuboid item models, their palette and a review contact sheet.

No game launch. Geometry is the editable source; previews render that geometry.
Requires Pillow and NumPy for writing the palette and depth-rendered preview PNGs.
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import numpy as np

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'mods/dbv-world/src/main/resources/assets/dbv_world'
PREVIEW = ROOT / 'docs/art/item-models'
COLORS = {
    'iron': '#81949d', 'edge': '#d0dce0', 'dark': '#293e49', 'black': '#17272f',
    'copper': '#b87948', 'gold': '#e8b75e', 'rust': '#815142', 'rubber': '#384942',
    'teal': '#397e84', 'aqua': '#81d5d2', 'blue': '#438bb3', 'ice': '#d3f1ed',
    'violet': '#8175b5', 'lilac': '#c3b9ee', 'bone': '#cbbfa0', 'cream': '#f0dfb5',
    'red': '#a65251', 'rose': '#df9c8f', 'orange': '#ed9850', 'yellow': '#f3d477',
    'green': '#618c60', 'lime': '#b4cc70', 'paper': '#decfa8', 'ink': '#304b5b',
}
NAMES = list(COLORS)
MODELS = {}
BOXES = []


def box(x, y, z, dx, dy, dz, color):
    assert min(dx, dy, dz) > 0
    BOXES.append(([x, y, z], [x+dx, y+dy, z+dz], color))


def ring(x, y, z, w, h, t, depth, color):
    box(x,y,z,w,t,depth,color); box(x,y+h-t,z,w,t,depth,color)
    box(x,y+t,z,t,h-2*t,depth,color); box(x+w-t,y+t,z,t,h-2*t,depth,color)


def bolt(x,y,z):
    box(x,y,z,.7,.7,.45,'edge'); box(x+.15,y+.29,z+.45,.4,.12,.08,'dark')


def rivets(x,y,z,w,h):
    for a,b in [(x,y),(x+w,y),(x,y+h),(x+w,y+h)]: bolt(a,b,z)


def coil(x,y,z,w=6,h=8,color='cream'):
    ring(x,y,z,w,h,1,1,color)
    box(x+w/2-.6,y-1,z-.3,1.2,h+2,.6,'copper')


def gland(color, core, tall=False):
    height=2 if tall else 1.3
    for i,(w,d) in enumerate([(4,3),(6,5),(8,6),(8,7),(6,5),(3,3)]):
        box(8-w/2,2+i*height,8-d/2,w,height,d,color)
    box(6.5,5,11.5,2.8,2.5,.3,core)
    box(6.7,7,11.8,.8,.7,.2,'ice')


def crystal(x,y,z,w,h,color):
    box(x,y,z,w,h,w,color)
    box(x+.4,y+h,z+.4,w-.8,.8,w-.8,'ice')
    box(x+.8,y+h+.8,z+.8,w-1.6,.7,w-1.6,'ice')
    box(x+w*.55,y+.6,z+w-.1,.6,h-.4,.25,'ice')


def gear(x,y,z,size,color):
    ring(x,y,z,size,size,1.5,2,color)
    for a in [1.5,size-3]:
        box(x+a,y-1,z,1.5,1,2,color); box(x+a,y+size,z,1.5,1,2,color)
        box(x-1,y+a,z,1,1.5,2,color); box(x+size,y+a,z,1,1.5,2,color)


def plate(x,y,z,w,h,color):
    box(x,y,z,w,h,1.2,color); box(x+.5,y+h-.5,z+1.2,w-1,.4,.2,'edge')


def finish(id, description, hand=False):
    global BOXES
    assert BOXES, id
    # Separate layered exterior faces: rivets, bands and caps must not z-fight.
    BOXES=[([v-index*.002 for v in a],[v+index*.002 for v in b],c)
           for index,(a,b,c) in enumerate(BOXES)]
    # Normalize every handmade silhouette to a 14-unit envelope, centered for all contexts.
    lo=[min(b[0][i] for b in BOXES) for i in range(3)]
    hi=[max(b[1][i] for b in BOXES) for i in range(3)]
    scale=14/max(hi[i]-lo[i] for i in range(3))
    geom=[]
    for a,b,c in BOXES:
        geom.append(([round((a[i]-(lo[i]+hi[i])/2)*scale+8,4) for i in range(3)],
                     [round((b[i]-(lo[i]+hi[i])/2)*scale+8,4) for i in range(3)], c))
    MODELS[id]=(description,geom,hand)
    BOXES=[]


def designs():
    # Biological resources: silhouettes and structure carry identity, not color alone.
    for i in range(5): box(3+i*.5,3+i*1.8,6+i*.25,9-i*.9,1.8,1.2,'bone' if i%2 else 'rust')
    box(7,4,8,1,7,.4,'cream'); finish('shell_fragment','不规则锈色扇形壳片，浅色断口与中央壳脊。')
    plate(2,3,5,7,7,'rust'); plate(6,6,7,6,5,'iron'); rivets(3,4,6.3,4,4)
    finish('reclaimed_metal','两片带铆钉的锈蚀回收铁片，错位叠放。')
    coil(3,3,6);coil(7,4,7,5,8,'bone');finish('tough_fiber','米色粗纤维双束，铜色束带。')
    gland('teal','aqua');box(4,5,11.2,1,4,.4,'copper');box(10,4,10.8,1,3,.5,'gold')
    finish('conductive_mucus','蓝绿色黏液团，包裹铜质导电颗粒。')
    gear(2,3,5,6,'rust');box(9,2,7,2,10,2,'iron');box(8,8,8,5,1,1,'edge');finish('scrap_parts','破旧齿轮、断轴与横销构成的零件组。')
    box(6,2,7,2,7,2,'copper');ring(4,8,6,6,5,1.5,3,'iron');box(4,11.5,6,2,1.5,3,'dark')
    finish('repair_tool_fragment','断裂扳手头与短柄，缺口用暗色金属表现。')
    for i in range(6):box(3+i,3+i*.5,7,1,10-i*1.3,1.5,'teal' if i%2 else 'aqua')
    box(3,2,6,2,11,2,'bone');finish('flow_fin','向后收尖的水动力鳍片，浅色骨条支撑。')
    for i in range(5):box(4+i*.8,2+i*2,6,4,1.6,3,'aqua' if i%2 else 'bone')
    box(6,3,7,1,9,1,'cream');finish('flow_cartilage','弧形分节软骨，蓝色软组织和浅色骨节。')
    gland('lime','cream');box(3,3,7,3,1,3,'green');finish('insulating_gel','浅黄绿色绝缘胶团，宽底和圆润阶梯轮廓。')
    for i in range(4):
        box(3+i*2,3+i%2,6+i%2,1.3,8-i%2,1.3,'violet')
        for y in (4,7,10):box(3+i*2,y,7.4+i%2,.9,.6,.4,'lilac')
    box(2.5,10,5.7,8,1.5,3,'rose');finish('intact_tentacles','四根长短不同的紫色触须，带浅色吸盘与根部组织。')
    for i in range(5):box(3+i,2+i*2,6+i*.3,7-i*1.1,2,3-i*.4,'bone' if i<3 else 'cream')
    finish('hard_horn','弯曲收尖的硬质角片，厚根与明亮角尖。')
    gland('red','orange',True);box(7,2,7,2,2,2,'gold');finish('impact_gland','红褐色冲击腺体，橙色中央腺泡与短颈。')
    for i in range(3):plate(3+i,3+i*3,5+i*.7,9-i*1.5,4,'dark' if i%2 else 'teal')
    box(7,3,9,1.4,10,.6,'iron');finish('dense_carapace','三层深海厚甲片，背面中脊和深色重叠边缘。')
    ring(3,3,6,10,9,1,1.5,'bone')
    for i in range(4):box(4,4+i*1.7,6.2,8,1,.5,'rose')
    box(7,3,7,1,9,.5,'cream');finish('joint_membrane','浅色关节骨框内的粉色褶皱膜片。')
    gland('teal','yellow',True);box(7,12,7,1,2,1,'green');finish('luminous_sac','带细蒂的发光囊，蓝绿外膜与黄色发光核心；仅明亮配色，不发动态光。')
    ring(3,4,6,10,8,2,2,'rose');box(5,6,7,6,4,2,'blue');box(6,7,9,3,2,.3,'ice');finish('lens_tissue','肉质边缘包裹的蓝色透镜组织，中心反光面。')
    gland('green','lime');box(2,2,6,10,1.5,4,'green');finish('sealing_mucus','暗绿密封黏质，低矮胶团与扩散的黏液底缘。')
    gland('orange','yellow',True)
    for x,y in [(4,5),(10,8),(6,3)]:box(x,y,10,1.5,1.5,1.3,'red')
    finish('active_mucus','橙色活性胶团，红色囊泡和亮黄核心。')
    for i in range(3):coil(3+i*2.5,2+i%2,6+i*.5,3.2,10,'rose')
    finish('flexible_tendon','三股细长粉色筋腱，回环和扎束结构。')
    box(6,2,6,4,2,3,'rose')
    for i in range(3):
        box(5+i*2,4,7,1,5+i,1,'aqua');box(4+i*3,9+i,7,.8,3,.8,'cream')
        box(3.6+i*3,11+i,6.6,1.6,1.6,1.6,'ice')
    finish('sensory_barbel','分叉感知须，柔软根部与三个发亮感知端点。')
    for i in range(4):plate(3+i,3+i*2.5,6+i*.6,9-i*1.5,3,'blue' if i%2 else 'ice')
    finish('mirror_carapace','四层镜质甲片，冷色反光与蓝色夹层。')
    crystal(6,2,6,4,9,'aqua');finish('clear_crystal','单根清澈六棱感方晶，浅蓝主体与白色晶尖。')
    box(7,2,7,1.5,11,1.5,'bone')
    for y,w in [(4,7),(7,9),(10,7)]:ring(8-w/2,y,6,w,2.2,.6,1,'cream')
    finish('light_skeleton','镂空肋骨与细脊柱，强调轻质骨架。')
    ring(3,3,6,10,10,1.5,2,'rose');ring(5,5,7,6,6,1,2,'bone');box(7,6,9,2,4,1,'red')
    finish('auditory_organ','耳蜗状双层环组织，中央红色听觉膜。')
    for i in range(3):box(3+i*3,3,6,1.5,7+i,2,'aqua');box(3+i*3,9+i,6,1.5,2,2,'ice')
    box(2,2,5,10,2,4,'gold');finish('premium_flow_material','金色基座中的三片优质导流晶鳍。')
    box(3,3,5,9,6,6,'dark');box(5,9,6,5,3,4,'iron');box(3,5,11,4,1,.3,'teal')
    finish('dense_material','厚重深色矿质块，阶梯断面与蓝绿矿脉。')
    ring(3,3,6,10,10,2,3,'dark');ring(4,4,9,8,8,1,1,'gold');rivets(3,3,9,9,9)
    finish('high_pressure_sealant','加固高压密封环，金属压边与四角螺钉。')
    crystal(3,2,6,3,7,'violet');crystal(7,2,6,4,11,'lilac');crystal(11,2,7,2.5,5,'blue')
    finish('acoustic_crystal','三支不同高度的声学晶簇，紫色晶体与浅色晶尖。')
    ring(3,3,5,10,9,1,1,'ice');ring(4,4,8,8,7,1,1,'iron')
    for y in (4,8):box(3,y,5,1,1,4,'blue');box(12,y,5,1,1,4,'blue')
    finish('light_structural_material','双层空心轻质框架，少量蓝色连接节点。')
    # Engineered components.
    ring(3,3,6,10,10,2,2,'rubber');box(3,3,8,3,1,.2,'lime');finish('universal_seal','方圆风格通用橡胶密封圈，带方向色标。')
    for i in range(5):box(3+i*2,2,6,1.3,11,2,'cream' if i%2 else 'bone')
    box(2.5,4,5.7,10.5,1.5,2.6,'teal');box(2.5,9,5.7,10.5,1.5,2.6,'teal')
    finish('reinforced_fiber','五股直纤维加强束，两道工业绑带。')
    ring(2,2,5,12,12,1.2,3,'iron')
    for i in range(4):box(4+i*2.5,4,6,1,8,2,'copper')
    box(6,6,8,4,4,1.2,'teal');finish('flow_assembly','金属方框中的铜制导流叶片与中央轴毂。')
    plate(2,2,5,12,12,'dark');plate(2.5,2.5,6.2,11,11,'teal');plate(3,3,7.4,10,10,'iron');rivets(3.5,3.5,8.6,8,8)
    finish('pressure_composite_plate','三层耐压复合板，金属面板、密封夹层和四角铆钉。')
    box(3,3,5,10,9,3,'dark');ring(5,5,8,6,6,1,1,'copper');box(6,6,8,4,4,1,'violet')
    for x in (4,7,10):box(x,2,6,1,1,1,'gold')
    finish('precision_acoustic_assembly','精密声学模块，铜环晶体传感头与底部接线端。')
    ring(2,3,5,12,9,1.2,1.2,'iron');box(2,7,5,12,1,1.2,'edge');box(7.5,3,5,1,9,1.2,'edge');rivets(2,3,6.2,11,8)
    finish('light_structure','十字支撑的镂空轻量金属框架。')
    box(2,3,5,12,8,6,'teal');box(2,9,5,12,2,6,'iron');ring(5,11,7,6,3,1,2,'dark')
    box(7,4,11,2,5,.3,'cream');box(5.5,5.5,11.3,5,2,.3,'cream');box(3,8,11,1,2,.5,'copper')
    finish('standard_repair_kit','蓝绿维修工具箱，提手、金属箱沿和浅色十字标识。',True)
    box(3,3,6,10,6,4,'rubber');box(4,9,6.5,8,1,3,'green');box(4,5,10,6,.5,.2,'dark');finish('base_rubber','未加工胶料砖，暗绿表面和一道压制纹。')
    for i in range(5):box(3+i*2,2+i%2,6+i*.25,.8,10-i%3,1,'green' if i%2 else 'lime')
    box(2.5,5,5.7,10,1,2,'bone');finish('plant_fiber','松散植物纤维束，长短不一，浅色捆绳。')
    gear(3,3,6,10,'iron');ring(6,6,8,4,4,1,1,'copper');finish('standard_mechanical_part','完整标准齿轮，钢制轮齿与铜色轴套。')
    box(2,3,6,12,2,4,'iron')
    for i in range(5):box(3+i*2,5,6.5,1,6,3,'teal')
    finish('artificial_flow_material','规则工业导流坯，五片整齐散热鳍。')
    box(3,3,5,10,6,6,'iron');box(4,9,6,8,1,4,'edge');box(3,5,11,10,1,.3,'dark')
    finish('processed_dense_material','处理后的致密金属坯，规整断面与加工刻线。')
    box(2,3,7,12,10,1,'green');box(5,6,8,6,4,1,'dark')
    for x in (3,5,7,9,11):box(x,2,7,1,1,1,'gold');box(x,12,8,1,1,.4,'gold')
    box(3,5,8,1,4,.3,'copper');box(11,5,8,1,5,.3,'copper');finish('electronic_component','绿色电路板，深色集成芯片、铜走线和金色触点。')
    ring(3,3,6,9,9,2,3,'copper');box(6,12,6,3,2,3,'gold');box(6,1,6,3,2,3,'gold');finish('copper_part','铜制法兰环，带两处连接凸耳。')
    plate(2,2,6,11,12,'iron');plate(4,4,7.3,9,10,'edge');box(5,5,8.5,1,8,.2,'dark');finish('metal_sheet','两张银灰金属薄片，折边与切割刻线。')
    box(4,3,5,8,7,6,'bone');box(5,10,6,6,2,4,'dark');box(6,12,7,4,1,2,'copper')
    box(6,5,11,4,3,.3,'orange');box(7,6,11.3,2,1,.2,'black');finish('base_propellant','封口推进药袋，橙色警示牌和扎紧袋口。')
    # Equipment: native cuboids with distinct barrel, grip and working head.
    box(3,6,6,9,3,3,'dark');box(2,6.5,6.5,12,1,1,'iron');box(13,6,6,2,2,2,'edge')
    box(5,2,6.5,2,4,2,'copper');ring(8,3,6.5,4,3,1,1,'iron');box(4,9,7,4,1,1,'teal')
    box(12,8,7,2,1,1,'edge');box(12,5,7,2,1,1,'edge');finish('hand_harpoon','手持鱼叉器：滑轨、叉头、铜色握柄和护圈。',True)
    for upgraded in (False,True):
        box(2,5,5,7,5,6,'dark');box(8,6,6,6,3,4,'iron');box(13,5.5,5.5,2,4,5,'copper')
        box(15,6.5,6.5,.3,2,3,'black');box(3,2,6,4,3,4,'teal');box(2,1,4,7,1,8,'iron')
        box(3,10,6,2,2,4,'copper');rivets(3,6,11,4,2)
        if upgraded:
            for x in (8,10,12):box(x,5.5,5.5,.6,4,5,'edge')
            box(3,6,11,4,2,2,'gold');box(4,11,7,4,1,1,'blue')
        finish('upgraded_light_cannon' if upgraded else 'light_nail_cannon',
               '升级轻炮：加强炮口、散热箍、侧挂供弹盒与蓝色瞄具。' if upgraded else '潜艇轻型射钉炮：短粗炮管、底座、侧铆钉与上方供弹口。',True)
    box(4,6,6,8,4,4,'iron');box(3,6.5,6.5,1,3,3,'copper');box(2,7,7,1,2,2,'copper');box(12,6.5,6.5,2,3,3,'dark')
    box(11,4,7.5,3,8,1,'teal');box(11,7.5,4,3,1,8,'teal');box(6,6,6,1,4,4,'yellow')
    finish('standard_torpedo','标准鱼雷：铜色收尖战斗部、钢制筒身、黄色识别带和十字尾翼。')
    box(3,5,6,7,4,4,'teal');box(4,2,7,2,3,2,'dark');box(9,6,6.5,2,2,3,'iron')
    gear(11,4,7,5,'edge');box(12.5,5.5,9,2,2,.4,'copper');box(3,9,7,5,1,2,'copper')
    finish('work_cutter','作业切割器：蓝绿机壳、下握柄、铜管和前端锯齿切割轮。',True)
    for x,y in [(3,2),(7,3),(11,2)]:
        box(x,y,6,2,7,2,'copper');box(x,y+7,6,2,2,2,'iron');box(x+.5,y+9,6.5,1,1,1,'edge')
    box(2.5,4,5.5,11,1,3,'dark');finish('light_cannon_round','三发轻炮钉弹，由深色弹夹固定，铜壳钢尖。')
    # Research folders use geometry emblems so they remain recognizable at small sizes.
    for id,accent,symbol in [('propulsion_research','teal','fan'),('damage_control_research','red','cross'),('detection_research','blue','sonar')]:
        box(3,2,6,10,12,1.5,'paper');box(2,2,6,1,12,2,'copper');box(4,12,7.5,3,2,.3,accent)
        for y in (4,5.5):box(5,y,7.5,6,.4,.25,'ink')
        if symbol=='fan':
            box(7,7,7.5,2,5,.4,accent);box(5.5,8.5,7.5,5,2,.4,accent);box(7.5,9,8,1,1,.3,'gold')
        elif symbol=='cross':
            box(7,7,7.5,2,5,.4,accent);box(5.5,8.5,7.5,5,2,.4,accent);ring(4.5,6.5,7.5,7,6, .5,.2,'ink')
        else:
            ring(5,7,7.5,6,5,.6,.3,accent);ring(6.5,8.2,7.8,3,2.6,.5,.3,accent);box(8,8,8.2,.6,3,.3,'gold')
        finish(id,{'fan':'推进研究记录：铜装订纸册，螺旋桨式推进图标。','cross':'损管研究记录：铜装订纸册，红色维修十字与防护框。','sonar':'探测研究记录：铜装订纸册，蓝色声呐环与金色指针。'}[symbol])


def model_json(geometry, hand):
    elements=[]
    for a,b,c in geometry:
        idx=NAMES.index(c);u=(idx%8)*2;v=(idx//8)*2
        faces={face:{'uv':[u+.25,v+.25,u+1.75,v+1.75],'texture':'#palette'}
               for face in ('north','south','east','west','up','down')}
        elements.append({'from':a,'to':b,'faces':faces})
    return {'credit':'Deep Blue Voyage native item geometry','gui_light':'front',
        'textures':{'palette':'dbv_world:item/prototype_palette','particle':'dbv_world:item/prototype_palette'},
        'elements':elements,'display':{
            'gui':{'rotation':[25,45,0],'translation':[0,0,0],'scale':[.9,.9,.9]},
            'ground':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.4,.4,.4]},
            'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[.7,.7,.7]},
            'head':{'rotation':[0,180,0],'translation':[0,13,0],'scale':[.7,.7,.7]},
            'thirdperson_righthand':{'rotation':[0,-90,0] if hand else [0,0,0],'translation':[0,2,0],'scale':[.65,.65,.65]},
            'thirdperson_lefthand':{'rotation':[0,90,0] if hand else [0,0,0],'translation':[0,2,0],'scale':[.65,.65,.65]},
            'firstperson_righthand':{'rotation':[0,-90,10] if hand else [0,30,0],'translation':[1,2,0],'scale':[.65,.65,.65]},
            'firstperson_lefthand':{'rotation':[0,90,-10] if hand else [0,-30,0],'translation':[1,2,0],'scale':[.65,.65,.65]}}}


def render(geometry, size=256):
    pixels=np.zeros((size,size,4),dtype=np.uint8)
    depth_buffer=np.full((size,size),-np.inf)
    def proj(p):
        x,y,z=[v-8 for v in p]
        return (size/2+(x-z)*.7071*size/26, size/2+((x+z)*.30-y*.9063)*size/24)
    def triangle(points, depths, color):
        x0=max(0,math.floor(min(p[0] for p in points)));x1=min(size-1,math.ceil(max(p[0] for p in points)))
        y0=max(0,math.floor(min(p[1] for p in points)));y1=min(size-1,math.ceil(max(p[1] for p in points)))
        if x1<x0 or y1<y0:return
        (ax,ay),(bx,by),(cx,cy)=points
        denom=(by-cy)*(ax-cx)+(cx-bx)*(ay-cy)
        if abs(denom)<1e-8:return
        yy,xx=np.mgrid[y0:y1+1,x0:x1+1];xx=xx+.5;yy=yy+.5
        u=((by-cy)*(xx-cx)+(cx-bx)*(yy-cy))/denom
        v=((cy-ay)*(xx-cx)+(ax-cx)*(yy-cy))/denom
        w=1-u-v;depth=u*depths[0]+v*depths[1]+w*depths[2]
        old=depth_buffer[y0:y1+1,x0:x1+1]
        mask=(u>=-1e-7)&(v>=-1e-7)&(w>=-1e-7)&(depth>=old)
        old[mask]=depth[mask];pixels[y0:y1+1,x0:x1+1][mask]=[*color,255]
    for a,b,c in geometry:
        x,y,z=a;X,Y,Z=b
        for points,shade in [([(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],1.10),
                             ([(X,y,z),(X,Y,z),(X,Y,Z),(X,y,Z)],.78),
                             ([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)],.98)]:
            rgb=tuple(int(COLORS[c][i:i+2],16) for i in (1,3,5))
            color=tuple(min(255,int(v*shade)) for v in rgb)
            projected=[proj(p) for p in points]
            depths=[p[0]*.6409+p[1]*.4226+p[2]*.6409 for p in points]
            for indices in [(0,1,2),(0,2,3)]:triangle([projected[i] for i in indices],[depths[i] for i in indices],color)
    return Image.fromarray(pixels)


def main():
    designs()
    specs={p.stem:p for p in (ROOT/'content/prototypes/items').glob('*.json')}
    assert set(specs)==set(MODELS), (set(specs)-set(MODELS),set(MODELS)-set(specs))
    (ASSETS/'models/item/prototypes').mkdir(parents=True,exist_ok=True)
    (ASSETS/'textures/item').mkdir(parents=True,exist_ok=True)
    PREVIEW.mkdir(parents=True,exist_ok=True)
    palette=Image.new('RGBA',(128,128),(255,255,255,255));pd=ImageDraw.Draw(palette)
    # Sixteen texels per swatch keep adjacent colors apart through four mip levels.
    for i,name in enumerate(NAMES):pd.rectangle([(i%8*16,i//8*16),(i%8*16+15,i//8*16+15)],fill=COLORS[name])
    palette.save(ASSETS/'textures/item/prototype_palette.png')
    font_path=Path('C:/Windows/Fonts/msyh.ttc')
    font=ImageFont.truetype(str(font_path),18) if font_path.exists() else ImageFont.load_default()
    small=ImageFont.truetype(str(font_path),12) if font_path.exists() else ImageFont.load_default()
    sheet=Image.new('RGB',(1440,9*224+100),'#10232e');sd=ImageDraw.Draw(sheet)
    sd.text((30,20),'深蓝远航 · 54 件三维物品模型',font=font,fill='#e3dabf')
    sd.text((30,52),'几何预览 · 非游戏截图 · 怪物与刷怪蛋外观保持不变',font=font,fill='#9eb6bc')
    table=['# 物品模型目录','','54 个物品使用独立原生三维模型；这里是几何离线预览，未进行客户端验收。',
           '','![模型总览](item-model-contact-sheet.png)','','| 物品 | 模型 ID | 结构说明 |','| --- | --- | --- |']
    for index,(id,(description,geometry,hand)) in enumerate(MODELS.items()):
        model=model_json(geometry,hand)
        assert len(geometry)<=100
        for a,b,_ in geometry:assert all(0<=a[i]<b[i]<=16 for i in range(3)),id
        (ASSETS/f'models/item/prototypes/{id}.json').write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        spec=json.loads(specs[id].read_text(encoding='utf-8'))
        for key,value in {'model':'custom','textureAll':'dbv_world:item/prototype_palette','visualDescription':description,
                          'textureResolution':128,'assetReferences':[f'dbv_world:item/prototypes/{id}']}.items():
            spec['sections']['appearance'][key]={'status':'specified','value':value,'reference':'tools/item-models/generate.py','notes':'原生三维物品模型；无新增使用功能。'}
        specs[id].write_text(json.dumps(spec,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        x=(index%6)*240;y=100+(index//6)*224
        sd.rounded_rectangle([x+6,y+4,x+234,y+218],radius=10,fill='#1c3541')
        icon=render(geometry,192);sheet.paste(icon,(x+24,y+2),icon)
        name=spec['sections']['identity']['nameZh']['value']
        sd.text((x+16,y+182),name,font=font,fill='#e4e4d5')
        sd.text((x+16,y+205),id,font=small,fill='#8ca8b1')
        table.append(f'| {name} | `dbv_world:item/prototypes/{id}` | {description} |')
    sheet.save(PREVIEW/'item-model-contact-sheet.png')
    (PREVIEW/'物品模型目录-item-model-catalog.md').write_text('\n'.join(table)+'\n',encoding='utf-8')
    print(f'Generated {len(MODELS)} native models, palette, matching specs and contact sheet.')


if __name__=='__main__':main()
