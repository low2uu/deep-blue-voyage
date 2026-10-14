package io.github.low2uu.dbv.fleet.client;

import io.github.low2uu.dbv.fleet.ui.PanelAction;
import io.github.low2uu.dbv.fleet.ui.PanelView;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Non-pausing, resizable, keyboard-accessible shipyard panel. Server owns every action. */
public final class FleetScreen extends Screen {
    private PanelView view;
    private int left,top,panelWidth,panelHeight,ticks,page;
    private boolean equipment,storageTab,confirmRestore,confirmCancel,busy;
    private Button begin,check,commit,cancel,backup,restore;
    public FleetScreen(PanelView view){super(Component.literal("潜艇管理"));this.view=view;}
    public UUID session(){return view.session();}
    public PanelView snapshot(){return view;}
    public static void receive(PanelView view){
        var mc=Minecraft.getInstance();
        if(view.open()){
            if(mc.screen instanceof FleetScreen current&&current.session().equals(view.session()))current.update(view);
            else mc.setScreen(new FleetScreen(view));
        }else if(mc.screen instanceof FleetScreen current&&current.session().equals(view.session()))current.update(view);
    }
    private void update(PanelView next){view=next;busy=false;updateButtons();}
    @Override protected void init(){
        panelWidth=Math.min(520,width-16);panelHeight=Math.min(340,height-16);
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        addRenderableWidget(Button.builder(Component.literal("×"),b->onClose()).bounds(left+panelWidth-30,top+9,20,20).build());
        addRenderableWidget(Button.builder(Component.literal("船况与改装"),b->{equipment=false;storageTab=false;page=0;updateButtons();}).bounds(left+14,top+38,92,20).build());
        addRenderableWidget(Button.builder(Component.literal("艇内设备"),b->{equipment=true;storageTab=false;page=0;updateButtons();}).bounds(left+112,top+38,80,20).build());
        addRenderableWidget(Button.builder(Component.literal("舱内存储"),b->{equipment=false;storageTab=true;page=0;updateButtons();}).bounds(left+198,top+38,80,20).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->{page=Math.max(0,page-1);}).bounds(left+panelWidth-66,top+38,22,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->{page=Math.min(pages()-1,page+1);}).bounds(left+panelWidth-40,top+38,22,20).build());
        int w=(panelWidth-46)/4,y=top+panelHeight-52;
        begin=addRenderableWidget(Button.builder(Component.literal("开始改装"),b->request(PanelAction.BEGIN)).bounds(left+14,y,w,20).build());
        check=addRenderableWidget(Button.builder(Component.literal("重新检查"),b->{confirmCancel=false;request(PanelAction.CHECK);}).bounds(left+20+w,y,w,20).build());
        commit=addRenderableWidget(Button.builder(Component.literal("完成改装"),b->request(PanelAction.COMMIT)).bounds(left+26+2*w,y,w,20).build());
        cancel=addRenderableWidget(Button.builder(Component.literal("取消还原"),b->{
            if(confirmCancel){confirmCancel=false;request(PanelAction.CANCEL);}else{confirmCancel=true;equipment=false;updateButtons();}
        }).bounds(left+32+3*w,y,w,20).build());
        backup=addRenderableWidget(Button.builder(Component.literal("备份存储"),b->request(PanelAction.STORAGE_BACKUP)).bounds(left+14,y,110,20).build());
        restore=addRenderableWidget(Button.builder(Component.literal("恢复最近备份"),b->{if(confirmRestore){confirmRestore=false;request(PanelAction.STORAGE_RESTORE);}else{confirmRestore=true;updateButtons();}}).bounds(left+130,y,130,20).build());
        updateButtons();
    }
    private void updateButtons(){
        if(begin==null)return;
        begin.visible=check.visible=commit.visible=cancel.visible=!storageTab;
        if(backup!=null){
            backup.visible=restore.visible=storageTab;backup.active=restore.active=!busy&&view.has(PanelView.STORAGE_EDITABLE);
            restore.setMessage(Component.literal(confirmRestore?"确认覆盖当前物品":"恢复最近备份"));
            restore.setTooltip(Tooltip.create(Component.literal("将覆盖容器当前内容；恢复前另存快照。需在船坞内，结束改装后由创造管理员操作。")));
        }
        boolean editable=view.has(PanelView.EDITABLE)&&!busy;
        begin.active=editable&&!view.has(PanelView.ACTIVE);
        commit.active=editable&&view.has(PanelView.ACTIVE)&&!view.has(PanelView.CHECKING);
        cancel.active=editable&&view.has(PanelView.ACTIVE)&&!view.has(PanelView.CHECKING);
        check.active=!busy&&view.has(PanelView.CONNECTED);
        check.setTooltip(Tooltip.create(Component.literal(view.notice().startsWith("检查完成")?view.notice():"检查当前资格与权限，并显示结果；不会自动修复舱室或设备。")));
        begin.setTooltip(Tooltip.create(Component.literal(beginProblem().isEmpty()?"先保存改装前备份，再允许编辑船体。":beginProblem())));
        cancel.setMessage(Component.literal(confirmCancel?"确认还原":"取消还原"));
    }
    private String beginProblem(){
        if(!view.has(PanelView.CONNECTED))return "无法开始："+view.status()+"；"+view.notice();
        if(busy)return "正在等待服务器处理，请稍候。";
        if(!view.has(PanelView.REGISTERED))return "无法开始：此船尚未登记为唯一主潜艇。";
        if(!view.has(PanelView.EDITABLE))return "无法开始："+view.status();
        if(view.has(PanelView.ACTIVE))return "已在改装中：关闭窗口即可施工，完成后提交或取消还原。";
        return ""; // Operating qualification is checked at completion, never admission to the dock.
    }
    private void request(int action){busy=true;ticks=0;updateButtons();PacketDistributor.sendToServer(new PanelAction(view.session(),action));}
    @Override public void tick(){if(++ticks>=20&&view.has(PanelView.CONNECTED)){ticks=0;PacketDistributor.sendToServer(new PanelAction(view.session(),PanelAction.REFRESH));}}
    @Override public void onClose(){PacketDistributor.sendToServer(new PanelAction(view.session(),PanelAction.CLOSE));super.onClose();}
    @Override public boolean isPauseScreen(){return false;}
    // Screen.render calls this after our panel is drawn; do not blur the panel itself.
    @Override public void renderBackground(GuiGraphics g,int mouseX,int mouseY,float partial){}
    private int rows(){return Math.max(1,(panelHeight-130)/38);}
    private java.util.List<String> displayRows(){return storageTab?view.storage():view.devices();}
    private int pages(){return Math.max(1,(displayRows().size()+rows()-1)/rows());}
    private void clipped(GuiGraphics g,String text,int x,int y,int maxWidth,int color){
        String value=font.width(text)>maxWidth?font.plainSubstrByWidth(text,Math.max(0,maxWidth-12))+"…":text;
        g.drawString(font,value,x,y,color,false);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){
        g.fill(0,0,width,height,0xA009111B);
        g.fill(left-1,top-1,left+panelWidth+1,top+panelHeight+1,0xFF3A7886);
        g.fill(left,top,left+panelWidth,top+panelHeight,0xFF10212B);
        g.fill(left,top,left+panelWidth,top+32,0xFF193542);
        clipped(g,view.title()+"  ·  潜艇管理",left+14,top+13,panelWidth-56,0xE6F7F6);
        if(equipment||storageTab)renderDevices(g,mouseX,mouseY);else renderOverview(g);
        clipped(g,"船坞改装 · 三项条件在完成时验收 · 关闭窗口可施工",left+14,top+panelHeight-19,panelWidth-28,0x8AABB8);
        super.render(g,mouseX,mouseY,partial);
        if(!equipment&&!storageTab&&mouseX>=left+14&&mouseX<left+panelWidth-14&&mouseY>=top+112&&mouseY<top+panelHeight-60)
            g.renderTooltip(font,font.split(Component.literal(beginProblem()+"\n"+view.status()+"\n"+view.notice()),Math.min(320,panelWidth-30)),mouseX,mouseY);
    }
    private void renderOverview(GuiGraphics g){
        int cardWidth=(panelWidth-40)/3;
        String[] labels={"中央控制器","氧气制造与供给","密闭空间"};int[] flags={1,2,4};
        for(int i=0;i<3;i++){
            int x=left+14+i*(cardWidth+6),y=top+67;
            g.fill(x,y,x+cardWidth,y+35,0xFF1B3440);
            clipped(g,labels[i],x+6,y+5,cardWidth-12,0xC8DAE2);
            String state=view.has(PanelView.BLOCK_MODE)&&i>0?"完成时检测":view.has(flags[i])?"符合":(i>0&&view.has(PanelView.PENDING)?"检测中":"未满足");
            g.drawString(font,state,x+6,y+21,view.has(flags[i])?0x75DFC3:0xF0BA77,false);
        }
        int y=top+112;
        String identity=view.has(PanelView.REGISTERED)?"身份：唯一主潜艇":"身份：尚未登记为主潜艇";
        g.drawString(font,identity,left+14,y,0x96D7E1,false);y+=16;
        String problem=beginProblem();
        String[] paragraphs={view.vesselInfo(),confirmCancel?"确认还原？本次改装的新增和修改都将撤销。再次点击“确认还原”执行。":(view.notice().startsWith("检查完成")?view.notice():(!problem.isEmpty()&&!view.has(PanelView.ACTIVE)?problem:view.notice())),view.status()};
        for(int p=0;p<paragraphs.length;p++){
            for(var line:font.split(Component.literal(paragraphs[p]),panelWidth-28)){
                if(y>top+panelHeight-70)break;
                g.drawString(font,line,left+14,y,p==0?0xC8DAE2:0xF0CD91,false);y+=12;
            }
            y+=8;
        }
    }
    private void renderDevices(GuiGraphics g,int mouseX,int mouseY){
        page=Math.min(page,pages()-1);
        g.drawString(font,(storageTab?"存储清单":"设备清单")+" · "+displayRows().size()+" 项 · 第 "+(page+1)+" / "+pages()+" 页",left+14,top+68,0x96D7E1,false);
        if(storageTab)clipped(g,view.notice(),left+14,top+panelHeight-65,panelWidth-28,0xF0CD91);
        if(displayRows().isEmpty())g.drawString(font,storageTab?"未发现可读取的舱内容器。":"暂未识别到设备，请重新检查。",left+14,top+92,0xC8DAE2,false);
        for(int i=page*rows();i<Math.min(displayRows().size(),(page+1)*rows());i++){
            int y=top+84+(i-page*rows())*38;String text=displayRows().get(i);String[] lines=text.split("\n",2);
            g.fill(left+14,y,left+panelWidth-14,y+33,0xFF1B3440);
            clipped(g,lines[0],left+20,y+5,panelWidth-40,0xE6F7F6);
            if(lines.length>1)clipped(g,lines[1],left+20,y+19,panelWidth-40,0xA3C3CD);
            if(mouseX>=left+14&&mouseX<=left+panelWidth-14&&mouseY>=y&&mouseY<y+33)
                g.renderTooltip(font,font.split(Component.literal(text),Math.min(320,panelWidth-30)),mouseX,mouseY);
        }
    }
}
