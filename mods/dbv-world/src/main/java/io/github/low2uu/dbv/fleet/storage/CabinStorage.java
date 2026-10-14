package io.github.low2uu.dbv.fleet.storage;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.fleet.persistence.VesselDataStore;
import java.io.IOException;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Physical inventories are authoritative; JSON is a portable, explicit recovery snapshot. */
public final class CabinStorage {
    private final MinecraftServer server;
    public CabinStorage(MinecraftServer server){this.server=server;}
    public VesselDataStore store(FleetModule m){return new VesselDataStore(server.getWorldPath(LevelResource.ROOT),m.registry().flagship().orElseThrow().vesselId());}
    private record Inventory(BlockPos pos,String block,Container container,IItemHandler handler){
        int size(){return container!=null?container.getContainerSize():handler.getSlots();}
        ItemStack get(int slot){return container!=null?container.getItem(slot):handler.getStackInSlot(slot);}
        boolean writable(){return container!=null||handler instanceof IItemHandlerModifiable;}
        void set(int slot,ItemStack stack){if(container!=null){container.setItem(slot,stack);container.setChanged();}else ((IItemHandlerModifiable)handler).setStackInSlot(slot,stack);}
    }
    private List<Inventory> inventories(FleetModule m) throws IOException {
        var result=new ArrayList<Inventory>();var level=server.overworld();
        for(var pos:m.refit().storagePositions(m)){
            if(!level.hasChunkAt(pos))throw new IOException("舱内区块未加载，保留上次存储数据");
            var be=level.getBlockEntity(pos);if(be==null)continue;
            Container c=be instanceof Container container?container:null;
            var h=c==null?level.getCapability(Capabilities.ItemHandler.BLOCK,pos,null):null;
            if(c!=null||h!=null){
                var entry=new Inventory(pos,BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString(),c,h);
                if(entry.size()>4096)throw new IOException("单设备槽位超过4096，停止不完整备份");result.add(entry);
            }
        }
        return result;
    }
    public JsonObject scan(FleetModule m) throws IOException {
        var origin=m.refit().storageOrigin(m);var root=new JsonObject();root.addProperty("schemaVersion",1);
        var containers=new JsonArray();var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        for(var inv:inventories(m)){
            var entry=new JsonObject();entry.addProperty("position",inv.pos.subtract(origin).toShortString());entry.addProperty("block",inv.block);
            entry.addProperty("slots",inv.size());entry.addProperty("restorable",inv.writable());var items=new JsonArray();
            for(int slot=0;slot<inv.size();slot++){
                var stack=inv.get(slot);if(stack.isEmpty())continue;
                var item=new JsonObject();item.addProperty("slot",slot);item.addProperty("label",stack.getHoverName().getString());
                item.add("stack",ItemStack.CODEC.encodeStart(ops,stack).getOrThrow());items.add(item);
            }
            entry.add("items",items);containers.add(entry);
        }
        root.add("containers",containers);return root;
    }
    public void capture(FleetModule m) throws IOException {
        store(m).requireStorageReady();captureUnchecked(m);
    }
    private void captureUnchecked(FleetModule m) throws IOException {
        var store=store(m);var storage=scan(m);var structure=structure(m);var key=new JsonObject();key.add("storage",storage);
        var geometry=new JsonArray();
        for(var value:structure.getAsJsonArray("blocks")){var entry=value.getAsJsonObject().deepCopy();entry.remove("metadata");geometry.add(entry);}
        key.add("geometry",geometry);
        var current=store.read().getAsJsonObject("modules");var business=new JsonObject();
        for(var entry:current.entrySet())if(!Set.of("structure","storage","vessel","automaticBackup","storageBackup").contains(entry.getKey()))business.add(entry.getKey(),entry.getValue());
        key.add("business",business);String fingerprint=VesselDataStore.fingerprint(key);
        var previous=current.getAsJsonObject("automaticBackup");
        var id=m.registry().flagship().orElseThrow();var identity=new JsonObject();identity.addProperty("name",id.name());identity.addProperty("dimension",id.dimension());
        identity.addProperty("bodyId",id.bodyId().toString());identity.addProperty("generation",id.generation());
        store.putModules(Map.of("storage",storage,"structure",structure,"vessel",identity));
        if(previous==null||!fingerprint.equals(previous.get("fingerprint").getAsString())){
            var marker=new JsonObject();marker.addProperty("latest",store.backupAutomatic().toString());marker.addProperty("fingerprint",fingerprint);store.putModule("automaticBackup",marker);
        }
    }
    public JsonObject structure(FleetModule m) throws IOException {
        var root=new JsonObject();root.addProperty("schemaVersion",1);root.addProperty("metadataAdapter","typed-json-v1");
        var origin=m.refit().storageOrigin(m);var blocks=new JsonArray();var level=server.overworld();
        var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        for(var pos:m.refit().storagePositions(m)){
            if(!level.hasChunkAt(pos))throw new IOException("船体区块未加载，不能更新结构备份");
            var state=level.getBlockState(pos);if(state.isAir())continue;
            var entry=new JsonObject();var relative=pos.subtract(origin);
            entry.addProperty("x",relative.getX());entry.addProperty("y",relative.getY());entry.addProperty("z",relative.getZ());
            entry.add("state",net.minecraft.world.level.block.state.BlockState.CODEC.encodeStart(ops,state).getOrThrow());
            var be=level.getBlockEntity(pos);if(state.hasBlockEntity()&&be==null)throw new IOException("设备未加载，不能更新结构备份");
            if(be!=null)entry.add("metadata",BlockEntityJson.encode(be.saveWithFullMetadata(level.registryAccess())));
            blocks.add(entry);
        }
        root.add("blocks",blocks);return root;
    }
    public UUID backup(FleetModule m) throws IOException {capture(m);return store(m).backup();}
    public String manualBackup(ServerPlayer p,FleetModule m) throws IOException {
        authorize(p,m);UUID id=backup(m);var pointer=new JsonObject();pointer.addProperty("latest",id.toString());store(m).putModule("storageBackup",pointer);
        return "舱内存储已独立备份："+id;
    }
    private void authorize(ServerPlayer p,FleetModule m){
        if(!p.isCreative()||!p.hasPermissions(2))throw new IllegalStateException("存储备份与恢复暂限创造模式管理员");
        if(m.refit().active())throw new IllegalStateException("请先完成或取消改装，再操作独立存储备份");
        if(m.refit().accessProblem(p,m)!=null)throw new IllegalStateException(m.refit().accessProblem(p,m));
    }
    public String restoreLatest(ServerPlayer p,FleetModule m) throws IOException {
        authorize(p,m);var store=store(m);store.requireStorageReady();var pointer=store.read().getAsJsonObject("modules").getAsJsonObject("storageBackup");
        if(pointer==null)throw new IllegalStateException("尚无手动存储备份");
        var id=UUID.fromString(pointer.get("latest").getAsString());var root=store.readBackup(id).getAsJsonObject("modules").getAsJsonObject("storage");
        if(root==null)throw new IOException("备份不含舱内存储");
        var current=inventories(m);var origin=m.refit().storageOrigin(m);var byPos=new HashMap<String,Inventory>();
        for(var inv:current)byPos.put(inv.pos.subtract(origin).toShortString(),inv);
        var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var before=new LinkedHashMap<Inventory,List<ItemStack>>();var planned=new LinkedHashMap<Inventory,List<ItemStack>>();
        var entries=root.getAsJsonArray("containers");
        if(entries.size()!=current.size())throw new IOException("容器数量已变化，请先恢复船体布局，未修改物品");
        for(var value:entries){
            var entry=value.getAsJsonObject();var inv=byPos.get(entry.get("position").getAsString());
            if(inv==null||planned.containsKey(inv)||!inv.writable()||!inv.block.equals(entry.get("block").getAsString())||inv.size()!=entry.get("slots").getAsInt())throw new IOException("容器位置、类型或槽位不匹配，未修改物品");
            var stacks=new ArrayList<ItemStack>(Collections.nCopies(inv.size(),ItemStack.EMPTY));var seen=new HashSet<Integer>();
            for(var item:entry.getAsJsonArray("items")){
                var data=item.getAsJsonObject();int slot=data.get("slot").getAsInt();
                if(slot<0||slot>=inv.size()||!seen.add(slot))throw new IOException("备份槽位无效");
                stacks.set(slot,ItemStack.CODEC.parse(ops,data.get("stack")).getOrThrow());
            }
            var old=new ArrayList<ItemStack>();for(int i=0;i<inv.size();i++)old.add(inv.get(i).copy());before.put(inv,old);planned.put(inv,stacks);
        }
        UUID safety=backup(m); // Persist the pre-restore contents before touching a single slot.
        store.beginStorageRecovery(id,safety);
        try{
            apply(planned);captureUnchecked(m);
            if(!server.saveEverything(false,true,true))throw new IOException("世界保存失败");
            store.finishStorageRecovery();
        }catch(Exception error){
            try{apply(before);captureUnchecked(m);if(!server.saveEverything(false,true,true))throw new IOException("回退保存失败");store.finishStorageRecovery();}catch(Exception rollback){error.addSuppressed(rollback);}
            throw new IOException("存储恢复失败；恢复前备份："+safety,error);
        }
        return "舱内存储已从 JSON 恢复；恢复前快照："+safety;
    }
    private void apply(Map<Inventory,List<ItemStack>> plan){
        for(var entry:plan.entrySet())for(int i=0;i<entry.getValue().size();i++)entry.getKey().set(i,entry.getValue().get(i).copy());
        for(var entry:plan.entrySet())for(int i=0;i<entry.getValue().size();i++)
            if(!ItemStack.matches(entry.getKey().get(i),entry.getValue().get(i)))throw new IllegalStateException("设备未完整接受恢复物品");
    }
    public List<String> lines(FleetModule m) throws IOException {
        var root=scan(m);var lines=new ArrayList<String>();
        for(var value:root.getAsJsonArray("containers")){
            var c=value.getAsJsonObject();String location=c.get("position").getAsString();
            lines.add("容器 · "+location+"\n"+c.get("block").getAsString()+" · "+c.get("slots").getAsInt()+"槽"+(c.get("restorable").getAsBoolean()?"":" · 仅查看"));
            for(var item:c.getAsJsonArray("items")){
                var e=item.getAsJsonObject();var stack=e.getAsJsonObject("stack");
                lines.add(e.get("label").getAsString()+" × "+(stack.has("count")?stack.get("count").getAsInt():1)+"\n容器 "+location+" · 槽位 "+(e.get("slot").getAsInt()+1));
            }
        }
        if(lines.size()>128){lines=new ArrayList<>(lines.subList(0,127));lines.add("显示已截断\n完整槽位信息保存在独立 JSON 档案中");}
        return lines;
    }
}
