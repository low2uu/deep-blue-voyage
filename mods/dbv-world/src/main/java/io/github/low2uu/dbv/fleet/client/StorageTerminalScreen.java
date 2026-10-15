package io.github.low2uu.dbv.fleet.client;

import io.github.low2uu.dbv.fleet.ui.StorageTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid="dbv_world",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class StorageTerminalScreen extends AbstractContainerScreen<StorageTerminalMenu> {
    public StorageTerminalScreen(StorageTerminalMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=212;imageHeight=238;inventoryLabelY=144;inventoryLabelX=25;titleLabelX=25;}
    @SubscribeEvent public static void register(RegisterMenuScreensEvent event){event.register(StorageTerminalMenu.TYPE.get(),StorageTerminalScreen::new);}
    @Override protected void init(){
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+147,topPos+5,20,16).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1)).bounds(leftPos+171,topPos+5,20,16).build());
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF122C38);
        for(var slot:menu.slots){int x=leftPos+slot.x,y=topPos+slot.y;g.fill(x-1,y-1,x+17,y+17,0xFF607D87);g.fill(x,y,x+16,y+16,0xFF213D49);}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,25,10,0xE5F7FF,false);
        g.drawString(font,"第 "+(menu.page()+1)+" / "+menu.pages()+" 页 · Shift 快速存取",25,134,0xA6D8DB,false);
        g.drawString(font,playerInventoryTitle,25,144,0xE5F7FF,false);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){super.render(g,mx,my,partial);renderTooltip(g,mx,my);}
}
