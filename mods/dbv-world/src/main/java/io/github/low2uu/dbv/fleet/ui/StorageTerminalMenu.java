package io.github.low2uu.dbv.fleet.ui;

import io.github.low2uu.dbv.fleet.FleetModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.*;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;

/** Live slots, not copied stock: vanilla menu transactions arbitrate all clicks on the server. */
public final class StorageTerminalMenu extends AbstractContainerMenu {
    private static final DeferredRegister<MenuType<?>> TYPES=DeferredRegister.create(Registries.MENU,"dbv_world");
    public static final java.util.function.Supplier<MenuType<StorageTerminalMenu>> TYPE=TYPES.register("storage_terminal",
            ()->new MenuType<>(StorageTerminalMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static void register(IEventBus bus){TYPES.register(bus);}
    private record Cell(BlockPos pos,BlockEntity owner,IItemHandler handler,int slot){}
    private final List<Cell> cells=new ArrayList<>();
    private final ContainerData data=new SimpleContainerData(2);
    private final FleetModule module;
    private UUID bodyId;
    private final IItemHandler handler;
    public StorageTerminalMenu(int id,Inventory inventory){this(id,inventory,null);}
    private StorageTerminalMenu(int id,Inventory inventory,FleetModule module){
        super(TYPE.get(),id);this.module=module;
        if(module!=null){
            try{
                if(module.refit().active())throw new IllegalStateException("改装期间不能打开存储终端");
                var body=module.refit().currentBody(module);if(body==null)throw new IllegalStateException("主艇未加载");
                bodyId=body.getUniqueId();var level=inventory.player.level();
                for(var pos:module.refit().storagePositions(module)){
                    var be=level.getBlockEntity(pos);if(be==null)continue;
                    // Storage containers only: never expose engine upgrades or production-machine internals.
                    if(!(be instanceof RandomizableContainerBlockEntity)
                            &&!net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).toString().equals("create:item_vault"))continue;
                    if(be instanceof com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity vault&&!vault.isController())continue;
                    if(be instanceof BaseContainerBlockEntity locked&&!locked.canOpen(inventory.player))continue;
                    if(be instanceof RandomizableContainerBlockEntity loot)loot.unpackLootTable(inventory.player);
                    IItemHandler h=be instanceof net.minecraft.world.Container c?new InvWrapper(c):level.getCapability(Capabilities.ItemHandler.BLOCK,pos,null);
                    if(!(h instanceof IItemHandlerModifiable))continue;
                    if(h.getSlots()>4096||cells.size()+h.getSlots()>16384)throw new IllegalStateException("存储槽位过多，请分拆设备");
                    for(int slot=0;slot<h.getSlots();slot++)cells.add(new Cell(pos.immutable(),be,h,slot));
                }
                data.set(1,Math.max(1,(cells.size()+53)/54));
            }catch(RuntimeException e){throw new IllegalStateException("无法读取主艇存储："+e.getMessage(),e);}
            handler=new IItemHandlerModifiable(){
                private Cell cell(int slot){int index=data.get(0)*54+slot;return slot>=0&&slot<54&&index<cells.size()?cells.get(index):null;}
                public int getSlots(){return 54;}
                public ItemStack getStackInSlot(int slot){var c=cell(slot);return c==null?ItemStack.EMPTY:c.handler.getStackInSlot(c.slot);}
                public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){var c=cell(slot);return c==null?stack:c.handler.insertItem(c.slot,stack,simulate);}
                public ItemStack extractItem(int slot,int amount,boolean simulate){var c=cell(slot);return c==null?ItemStack.EMPTY:c.handler.extractItem(c.slot,amount,simulate);}
                public int getSlotLimit(int slot){var c=cell(slot);return c==null?0:c.handler.getSlotLimit(c.slot);}
                public boolean isItemValid(int slot,ItemStack stack){var c=cell(slot);return c!=null&&c.handler.isItemValid(c.slot,stack);}
                public void setStackInSlot(int slot,ItemStack stack){var c=cell(slot);if(c!=null)((IItemHandlerModifiable)c.handler).setStackInSlot(c.slot,stack);}
            };
        }else{handler=new ItemStackHandler(54);data.set(1,1);}
        for(int r=0;r<6;r++)for(int c=0;c<9;c++)addSlot(new SlotItemHandler(handler,r*9+c,25+c*18,24+r*18){
            @Override public void setChanged(){super.setChanged();int i=page()*54+getSlotIndex();if(module!=null&&i<cells.size())cells.get(i).owner.setChanged();}
        });
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inventory,9+r*9+c,25+c*18,156+r*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inventory,c,25+c*18,214));
        addDataSlots(data);
    }
    public static void open(ServerPlayer player,FleetModule module){
        player.openMenu(new SimpleMenuProvider((id,inv,p)->new StorageTerminalMenu(id,inv,module),Component.literal("舱内存储终端")));
    }
    public int page(){return data.get(0);}
    public int pages(){return data.get(1);}
    @Override public boolean stillValid(Player player){
        if(module==null)return true;
        try{
            var body=module.refit().currentBody(module);
            if(module.refit().active()||body==null||body.isRemoved()||!bodyId.equals(body.getUniqueId())||player.level()!=body.getLevel())return false;
            var p=body.logicalPose().transformPositionInverse(player.position());var b=body.getPlot().getBoundingBox();
            if(p.x<b.minX()-8||p.x>b.maxX()+8||p.y<b.minY()-8||p.y>b.maxY()+8||p.z<b.minZ()-8||p.z>b.maxZ()+8)return false;
            for(var cell:cells){
                if(player.level().getBlockEntity(cell.pos)!=cell.owner)return false;
                if(!(cell.owner instanceof net.minecraft.world.Container)
                        &&player.level().getCapability(Capabilities.ItemHandler.BLOCK,cell.pos,null)!=cell.handler)return false;
            }
            return true;
        }catch(Exception e){return false;}
    }
    @Override public boolean clickMenuButton(Player player,int button){
        if(!stillValid(player)||!getCarried().isEmpty()||button<0||button>1)return false;
        data.set(0,Math.clamp(page()+(button==0?-1:1),0,pages()-1));broadcastChanges();return true;
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(!stillValid(player)){player.closeContainer();return;}
        super.clicked(slot,button,type,player);snapshot(player);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;
        var source=slot.getItem();var original=source.copy();
        if(index<54){if(!moveItemStackTo(source,54,90,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(source,0,54,false))return ItemStack.EMPTY;
        slot.setByPlayer(source.isEmpty()?ItemStack.EMPTY:source);
        slot.onTake(player,source);return original;
    }
    private void snapshot(Player player){
        if(module==null)return;
        try{module.storage().capture(module);}catch(Exception e){player.displayClientMessage(Component.literal("物品已存取，但独立备份失败，请查看日志。"),false);com.mojang.logging.LogUtils.getLogger().error("Storage terminal snapshot failed",e);}
    }
    @Override public void removed(Player player){super.removed(player);}
}
