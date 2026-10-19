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
        {"guide.dbv_world.0.0","guide.dbv_world.0.1","guide.dbv_world.0.2","guide.dbv_world.0.3","guide.dbv_world.0.4"},
        {"guide.dbv_world.1.0","guide.dbv_world.1.1","guide.dbv_world.1.2","guide.dbv_world.1.3","guide.dbv_world.1.4"},
        {"guide.dbv_world.2.0","guide.dbv_world.2.1","guide.dbv_world.2.2","guide.dbv_world.2.3","guide.dbv_world.2.4"}
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
        g.drawString(font,"初始艇驾驶指南 · "+Component.translatable(PAGES[page][0]).getString()+" · "+(page+1)+"/"+PAGES.length,x,y,0x75DFC3,false);y+=23;
        for(int i=1;i<PAGES[page].length;i++){
            for(var line:font.split(Component.translatable(PAGES[page][i]),w)){g.drawString(font,line,x,y,0xE6F7F6,false);y+=12;}
            y+=9;
        }
        super.render(g,mx,my,partial);
    }
}
