package io.github.low2uu.dbv.world;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/** Conservative evidence-based discovery, performed after biome modifiers and before chunk generation. */
public final class MineralCatalog {
    public record Mineral(String id, String block, String raw, boolean crystal) {
        public Block oreBlock() { return requiredBlock(block); }
        public Block rawBlock() { return raw==null?null:requiredBlock(raw); }
    }
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final List<Mineral> minerals;
    public MineralCatalog(List<Mineral> values) {
        minerals=List.copyOf(values);
        if(minerals.isEmpty())throw new IllegalStateException("主世界矿种列表为空，停止生成以防存档损坏");
        for(var m:minerals){m.oreBlock();m.rawBlock();}
        for(String essential:List.of("iron","copper","gold"))if(minerals.stream().noneMatch(m->m.id.equals(essential)))
            throw new IllegalStateException("主世界缺少初期必需矿种："+essential);
    }
    public List<Mineral> minerals(){return minerals;}
    public List<String> ids(){return minerals.stream().map(Mineral::id).toList();}
    public Mineral get(int index){return minerals.get(index);}
    private static Block requiredBlock(String id){
        var key=ResourceLocation.parse(id);
        if(!BuiltInRegistries.BLOCK.containsKey(key))throw new IllegalStateException("存档矿种方块已缺失，请恢复对应模组："+id);
        return BuiltInRegistries.BLOCK.get(key);
    }
    public static void beforeWorld(ServerAboutToStartEvent event) {
        var server=event.getServer();
        var stem=server.registryAccess().registryOrThrow(Registries.LEVEL_STEM).get(LevelStem.OVERWORLD);
        if(stem==null||!(stem.generator() instanceof FloodedWorldGenerator generator))return;
        var report=new JsonObject();var discovered=discover(server,generator,report);
        var folder=server.getWorldPath(LevelResource.ROOT).resolve("dbv");
        var snapshot=folder.resolve("worldgen-minerals.json");
        try {
            Files.createDirectories(folder);
            MineralCatalog selected;
            if(Files.exists(snapshot)) {
                var saved=JSON.fromJson(Files.readString(snapshot),JsonObject.class);
                if(saved.get("schema").getAsInt()!=1)throw new IllegalStateException("未知矿种存档版本");
                selected=new MineralCatalog(Arrays.asList(JSON.fromJson(saved.get("minerals"),Mineral[].class)));
            } else {
                selected=discovered;var saved=new JsonObject();saved.addProperty("schema",1);saved.add("minerals",JSON.toJsonTree(selected.minerals));
                atomicWrite(snapshot,JSON.toJson(saved));
            }
            report.add("activeSnapshot",JSON.toJsonTree(selected.minerals));
            atomicWrite(folder.resolve("mineral-scan-report.json"),JSON.toJson(report));
            generator.setMinerals(selected);
            com.mojang.logging.LogUtils.getLogger().info("DBV 主世界矿种扫描完成：{}",selected.ids());
        } catch(java.io.IOException ex){throw new IllegalStateException("无法保存世界矿种快照，停止生成",ex);}
    }
    private static void atomicWrite(Path destination,String text)throws java.io.IOException {
        var temp=destination.resolveSibling(destination.getFileName()+".tmp");Files.writeString(temp,text);
        try{Files.move(temp,destination,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(AtomicMoveNotSupportedException ex){Files.move(temp,destination,StandardCopyOption.REPLACE_EXISTING);}
    }
    private static MineralCatalog discover(MinecraftServer server,FloodedWorldGenerator generator,JsonObject report) {
        var natural=new HashMap<Block,SortedSet<String>>();var geodes=new HashSet<Block>();
        var seen=Collections.newSetFromMap(new IdentityHashMap<ConfiguredFeature<?,?>,Boolean>());
        var features=server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        for(var biome:generator.getBiomeSource().possibleBiomes())
            for(var step:biome.value().getGenerationSettings().features())for(var placed:step)
                placed.value().getFeatures().forEach(feature->{
                    if(!seen.add(feature))return;
                    String evidence=String.valueOf(features.getKey(feature));
                    if((feature.feature()==Feature.ORE||feature.feature()==Feature.SCATTERED_ORE)&&feature.config() instanceof OreConfiguration ore&&ore.size>0)
                        for(var target:ore.targetStates)natural.computeIfAbsent(target.state.getBlock(),b->new TreeSet<>()).add(evidence);
                    else if(feature.feature()==Feature.GEODE&&feature.config() instanceof GeodeConfiguration geode)
                        for(var state:geode.geodeBlockSettings.innerPlacements){geodes.add(state.getBlock());natural.computeIfAbsent(state.getBlock(),b->new TreeSet<>()).add(evidence);}
                });
        var groups=new TreeMap<String,List<Mineral>>();var included=new JsonArray();var rejected=new JsonArray();int scanned=0;
        var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        for(var item:BuiltInRegistries.ITEM) {
            scanned++;if(!(item instanceof BlockItem bi))continue;var block=bi.getBlock();
            String blockId=BuiltInRegistries.BLOCK.getKey(block).toString();
            String oreGroup=block.builtInRegistryHolder().tags().map(t->t.location()).filter(k->k.getNamespace().equals("c")&&k.getPath().startsWith("ores/"))
                    .map(k->k.getPath().substring(5)).sorted().findFirst().orElse(null);
            boolean tagged=oreGroup!=null||block.defaultBlockState().is(net.neoforged.neoforge.common.Tags.Blocks.ORES)
                    ||item.builtInRegistryHolder().is(net.neoforged.neoforge.common.Tags.Items.ORES);
            if(!tagged&&!geodes.contains(block))continue;
            if(!natural.containsKey(block)) {rejected.add(blockId+"：没有已挂载的主世界自然生成证据");continue;}
            if(block.defaultBlockState().getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE,net.minecraft.core.BlockPos.ZERO)<0)continue;
            var loot=server.reloadableRegistries().getLootTable(block.getLootTable());
            var encoded=LootTable.DIRECT_CODEC.encodeStart(ops,loot).result();
            if(encoded.isEmpty()) {rejected.add(blockId+"：无法解析挖掘掉落");continue;}
            var drops=new TreeSet<String>();collectDrops(encoded.get(),drops);
            if(drops.isEmpty()){rejected.add(blockId+"：没有可确认的挖掘掉落");continue;}
            boolean crystal=geodes.contains(block);
            String group=oreGroup;
            if(crystal){
                group=drops.stream().map(ResourceLocation::tryParse).filter(Objects::nonNull).filter(BuiltInRegistries.ITEM::containsKey)
                    .flatMap(id->BuiltInRegistries.ITEM.get(id).builtInRegistryHolder().tags()).map(TagKey::location)
                    .filter(k->k.getNamespace().equals("c")&&k.getPath().startsWith("gems/"))
                    .map(k->k.getPath().substring(5)).sorted().findFirst().orElse(null);
                if(group==null){rejected.add(blockId+"：晶芽没有宝石掉落证据");continue;}
            }
            if(group==null)group=blockId; // Never guess a metal solely from an item name.
            String raw=null;
            if(!crystal){
                var rawTag=TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("c","storage_blocks/raw_"+group));
                raw=BuiltInRegistries.BLOCK.getTag(rawTag).stream().flatMap(set->set.stream()).map(h->BuiltInRegistries.BLOCK.getKey(h.value()).toString()).sorted().findFirst().orElse(null);
            }
            var mineral=new Mineral(group,blockId,raw,crystal);groups.computeIfAbsent(group,k->new ArrayList<>()).add(mineral);
            var row=new JsonObject();row.addProperty("block",blockId);row.addProperty("mineral",group);row.add("features",JSON.toJsonTree(natural.get(block)));row.add("lootItems",JSON.toJsonTree(drops));included.add(row);
        }
        var list=new ArrayList<Mineral>();
        for(var alternatives:groups.values()){
            alternatives.sort(Comparator.comparingInt((Mineral m)->m.block.contains("deepslate")?1:0).thenComparing(Mineral::block));list.add(alternatives.get(0));
        }
        report.addProperty("scannedItems",scanned);report.add("eligibleBlocks",included);report.add("excludedCandidates",rejected);
        report.addProperty("policy","只自动接纳矿石标签与主世界标准矿脉生成的交集，或晶洞中掉落宝石的晶簇；未知自定义生成器不猜测。原有世界生成注册表不修改。");
        return new MineralCatalog(list);
    }
    private static void collectDrops(JsonElement value,Set<String> out) {
        if(value.isJsonArray()){for(var v:value.getAsJsonArray())collectDrops(v,out);return;}
        if(!value.isJsonObject())return;var object=value.getAsJsonObject();
        if(object.has("type")&&object.get("type").isJsonPrimitive()&&object.get("type").getAsString().equals("minecraft:item")&&object.has("name"))out.add(object.get("name").getAsString());
        for(var entry:object.entrySet())if(entry.getValue().isJsonObject()||entry.getValue().isJsonArray())collectDrops(entry.getValue(),out);
    }
}
