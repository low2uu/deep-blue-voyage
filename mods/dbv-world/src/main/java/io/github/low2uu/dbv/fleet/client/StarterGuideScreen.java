package io.github.low2uu.dbv.fleet.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Readable onboard instructions; opening the guide never pauses vessel physics. */
public final class StarterGuideScreen extends Screen {
    private final Screen parent;
    private int page;
    private static final String[][] PAGES={
        {"首次出航", "1. 关闭舱门、顶舱盖。管理面板三项应符合，检查柴油、水和氧气余量。",
         "2. 出厂推进为停车，柴油机仍驱动制氧和压载泵。点击管理面板“出航”解除系泊与岸供。",
         "3. 普通右键中央控制器设置目标深度；按住潜行键再右键打开管理面板。先在浅水练习上浮、下潜，再开启推进。",
         "深度以中央控制器为基准，船底比它更深；不要只看目标数字判断是否会触底。"},
        {"推进与转向", "面朝艇首玻璃：左右两侧凹位拉杆控制各自推进离合器；出厂拉杆开启表示离合断开、桨停车。",
         "两侧推进接通后同向航行。发动机前上方的拉杆控制换向；一侧换向或停车可转弯。",
         "停车：两侧推进离合均断开。保留柴油机服务传动，压载和制氧需要它。螺旋桨必须浸水才能推进。",
         "回到船坞、双桨停车并减速后，管理面板点“停泊”恢复固定与岸供；不会自动返航。柴油、淡水有限，停车也耗油。"},
        {"气闸与应急", "出舱：先关内门，再启气闸泵注水；灌满停泵后开外门。内外门不得同时打开。",
         "返回：关外门，用机械动力扳手反转气闸机械泵朝向，启泵排水；排空停泵后开内门。",
         "无法升降时先检查：是否仍系泊、柴油是否耗尽、四组压载泵是否有转速、进排水口是否被堵。",
         "不要带漏水船深潜。耐压以实际最弱部件诊断为准；存储终端可取放物品，备份页保留独立物品备份。"}
    };
    public StarterGuideScreen(Screen parent){super(Component.literal("初始艇驾驶指南"));this.parent=parent;}
    @Override protected void init(){
        int y=height-30;
        addRenderableWidget(Button.builder(Component.literal("上一页"),b->{page=Math.max(0,page-1);}).bounds(width/2-145,y,80,20).build());
        addRenderableWidget(Button.builder(Component.literal("返回面板"),b->onClose()).bounds(width/2-40,y,80,20).build());
        addRenderableWidget(Button.builder(Component.literal("下一页"),b->{page=Math.min(PAGES.length-1,page+1);}).bounds(width/2+65,y,80,20).build());
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void tick(){if(parent!=null)parent.tick();}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        g.fill(0,0,width,height,0xF010212B);
        int w=Math.min(440,width-32),x=(width-w)/2,y=15;
        g.drawString(font,"初始艇驾驶指南 · "+PAGES[page][0]+" · "+(page+1)+"/"+PAGES.length,x,y,0x75DFC3,false);y+=23;
        for(int i=1;i<PAGES[page].length;i++){
            for(var line:font.split(Component.literal(PAGES[page][i]),w)){g.drawString(font,line,x,y,0xE6F7F6,false);y+=12;}
            y+=9;
        }
        super.render(g,mx,my,partial);
    }
}
