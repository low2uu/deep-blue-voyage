from pathlib import Path
import json,math,collections
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
r=json.loads((ROOT/'实测数据.json').read_text(encoding='utf-8'))
N={'iron':'铁','copper':'铜','gold':'金','coal':'煤','diamond':'钻石','emerald':'绿宝石','redstone':'红石','lapis':'青金石','zinc':'锌','amethyst':'紫水晶'}
C={'iron':'#c5cdd5','copper':'#df9668','gold':'#f5d26b','coal':'#8395a4','diamond':'#68dce6','emerald':'#62d69f','redstone':'#ef7a85','lapis':'#8899fa','zinc':'#aacbb4','amethyst':'#c394ec'}
BG='#081d2b';GRID='#294451';INK='#edf4fb';MUTED='#a8bdca';GREEN='#92efc3'
font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
checked={(s['x'],s['z']):i for i,s in enumerate(r['checkedSites'])}
def chart(extent,step,title,subtitle):
 im=Image.new('RGB',(1640,1390),BG);d=ImageDraw.Draw(im)
 d.text((55,30),title,font=font(36),fill=INK);d.text((55,88),subtitle,font=font(22),fill=MUTED)
 left,top,size=100,175,1050
 def xy(x,z):return (left+(x+extent)/(2*extent)*size,top+(z+extent)/(2*extent)*size)
 for value in range(-extent,extent+1,step):
  x,y=xy(value,value);d.line((x,top,x,top+size),fill=GRID);d.line((left,y,left+size,y),fill=GRID)
  if value not in (-extent,extent):
   d.text((x,top+size+12),str(value),font=font(16),fill=MUTED,anchor='mt');d.text((left-12,y),str(value),font=font(16),fill=MUTED,anchor='rm')
 d.rectangle((left,top,left+size,top+size),outline='#779fb0',width=2)
 d.text((left+size/2,top-32),'北  −Z',font=font(18),fill=MUTED,anchor='mm')
 d.text((left+size+5,top+size+42),'东 +X →',font=font(18),fill=MUTED,anchor='rt')
 d.text((left+size/2,top+size+55),'南 +Z ↓',font=font(18),fill=MUTED,anchor='mt')
 return im,d,xy,size
im,d,xy,size=chart(5000,1000,'深蓝远航 · v0.1.4 中心海域','当前种子布局｜3 个区域占位 · 24 座资源岛｜最近三岛完整矿脉实测')
local=[s for s in r['seedLayout'] if -5000<=s['x']<5000 and -5000<=s['z']<5000]
for s in local:
 x,y=xy(s['x'],s['z']);m=s['mineral']
 if m=='main':
  rad=s['radius']/10000*size;d.ellipse((x-rad,y-rad,x+rad,y+rad),outline='#77d7e5',width=2)
  d.polygon([(x,y-7),(x+7,y),(x,y+7),(x-7,y)],fill='#b5edf4');d.text((x,y-rad-10),s['kind'],font=font(20),fill='#b5edf4',anchor='mb');continue
 i=checked.get((s['x'],s['z']));color=C[m]
 if i is not None:
  d.ellipse((x-11,y-11,x+11,y+11),outline=GREEN,width=2);d.ellipse((x-6,y-6,x+6,y+6),fill=color)
  label=f'近{i+1} {N[m]}'
 else:
  d.ellipse((x-5,y-5,x+5,y+5),outline=color,width=2);label=f"{s['index']-2:02} {N[m]}"
 d.text((x+13,y-10),label,font=font(18 if i is None else 20),fill=color)
x,y=xy(0,0);d.ellipse((x-7,y-7,x+7,y+7),fill=INK);d.text((x+12,y-12),'起始岛',font=font(21),fill=INK)
d.text((1190,175),'最近三岛 · 已实测',font=font(25),fill=GREEN)
for i,s in enumerate(r['checkedSites'][:3]):
 y=225+i*160;d.text((1190,y),f"{i+1:02}  {N[s['mineral']]}矿岛",font=font(25),fill=C[s['mineral']]);
 for j,line in enumerate([f"X {s['x']}   Z {s['z']}",f"距起始岛约 {s['distance']:,} 格",f"矿石 {s['oreBlocks']:,} 块",f"粗矿块 {s['rawBlocks']:,} 块"]):d.text((1190,y+38+j*25),line,font=font(18),fill=MUTED)
d.text((1190,745),'中心海域矿种分布',font=font(23),fill=INK)
counts=collections.Counter(s['mineral'] for s in local if s['mineral']!='main')
for i,(m,c) in enumerate(sorted(counts.items())):
 y=790+i*32;d.ellipse((1192,y+5,1202,y+15),outline=C[m],width=2);d.text((1216,y),f'{N[m]}  {c} 岛',font=font(19),fill=C[m])
d.text((1190,1120),'绿环：本轮实测',font=font(18),fill=GREEN);d.text((1190,1152),'空心：种子定位，未逐岛验收',font=font(17),fill=MUTED)
d.text((100,1330),'布局示意，非游戏卫星图。区域圆圈为预留范围；矿量仅统计额外矿脉，不含周边天然矿。',font=font(20),fill=MUTED)
im.save(ROOT/'中心海域实测地图.png')
im,d,xy,size=chart(15000,5000,'深蓝远航 · v0.1.4 远海生成实测','东 / 西 / 北 / 南各一岛｜四处均超过距起始点 10000 格｜同一种子按探索生成')
for s in r['seedLayout']:
 x,y=xy(s['x'],s['z']);m=s['mineral']
 if m=='main':
  rad=s['radius']/30000*size;d.ellipse((x-rad,y-rad,x+rad,y+rad),outline='#365e70',width=1)
 else:d.ellipse((x-3,y-3,x+3,y+3),outline=C[m],width=1)
x1,y1=xy(-5000,-5000);x2,y2=xy(5000,5000);d.rectangle((x1,y1,x2,y2),outline='#f0c978',width=3);d.text((x1+10,y1+10),'原主线范围（边长 10000）',font=font(17),fill='#f0c978')
x,y=xy(0,0);d.ellipse((x-6,y-6,x+6,y+6),fill=INK);d.text((x+10,y-12),'起始岛',font=font(18),fill=INK)
for i,s in enumerate(r['checkedSites'][3:]):
 x,y=xy(s['x'],s['z']);m=s['mineral'];direction=['东','西','北','南'][i]
 d.ellipse((x-12,y-12,x+12,y+12),outline=GREEN,width=3);d.ellipse((x-6,y-6,x+6,y+6),fill=C[m])
 dx=18 if x<1000 else -18;anchor='lm' if dx>0 else 'rm';d.text((x+dx,y),f'{direction} · {N[m]}',font=font(23),fill=GREEN,anchor=anchor)
 y=215+i*205;d.text((1190,y),f"{direction}侧实测 · {N[m]}",font=font(25),fill=C[m])
 for j,line in enumerate([f"X {s['x']}   Z {s['z']}",f"距起始岛 {s['distance']:,} 格",f"{'晶簇' if m=='amethyst' else '矿石'} {s['oreBlocks']:,} 块",f"粗矿块 {s['rawBlocks']:,} 块","岛屿露出海面：是"]):d.text((1190,y+42+j*27),line,font=font(18),fill=MUTED)
d.text((1190,1080),f"本轮实测 {r['checkedChunks']:,} 区块",font=font(21),fill=INK)
d.text((1190,1120),'亮色绿环：实测位置',font=font(18),fill=GREEN);d.text((1190,1152),'其余小点：种子布局，未逐岛实测',font=font(16),fill=MUTED)
d.text((100,1330),'橙框不是世界边界。外部圆圈同为区域占位，不包含主线任务；图中未将整片远海预生成。',font=font(20),fill=MUTED)
im.save(ROOT/'远海岛屿实测地图.png')
