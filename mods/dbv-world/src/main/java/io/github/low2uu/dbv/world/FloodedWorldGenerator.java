package io.github.low2uu.dbv.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Vanilla noise terrain submerged to 128, followed by final-biome surface/ecology. */
public final class FloodedWorldGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<FloodedWorldGenerator> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(FloodedWorldGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(FloodedWorldGenerator::generatorSettings)
    ).apply(i,FloodedWorldGenerator::new));
    public FloodedWorldGenerator(BiomeSource source,Holder<NoiseGeneratorSettings> settings) { super(source,settings); }
    @Override protected MapCodec<? extends ChunkGenerator> codec(){return CODEC;}
    private volatile MineralCatalog minerals;
    private final java.util.Map<String,java.util.List<VoyageLayout.Site>> tileCache=new java.util.LinkedHashMap<>();
    public void setMinerals(MineralCatalog minerals){this.minerals=minerals; synchronized(tileCache){tileCache.clear();}}
    public MineralCatalog minerals(){if(minerals==null)throw new IllegalStateException("矿种扫描尚未完成，禁止生成地图");return minerals;}
    public java.util.List<VoyageLayout.Site> sites(long seed,int minX,int minZ,int maxX,int maxZ){
        var result=new java.util.ArrayList<VoyageLayout.Site>();
        synchronized(tileCache){
            for(int tx=VoyageLayout.tile(minX);tx<=VoyageLayout.tile(maxX);tx++)for(int tz=VoyageLayout.tile(minZ);tz<=VoyageLayout.tile(maxZ);tz++){
                String key=seed+":"+tx+":"+tz;var sites=tileCache.get(key);
                if(sites==null){sites=VoyageLayout.assignedSites(seed,tx,tz,minerals().ids());if(tileCache.size()>=64)tileCache.remove(tileCache.keySet().iterator().next());tileCache.put(key,sites);}
                result.addAll(sites);
            }
        }return result;
    }
    public java.util.List<VoyageLayout.Site> sites(long seed,ChunkPos pos){return sites(seed,pos.getMinBlockX(),pos.getMinBlockZ(),pos.getMaxBlockX(),pos.getMaxBlockZ());}
    private boolean core(ChunkAccess c){return OceanBounds.preserved(c.getPos().getMinBlockX(),c.getPos().getMinBlockZ());}
    @Override public int getSpawnHeight(LevelHeightAccessor l){return IslandTerrain.PLATFORM_Y+1;}

    @Override public int getBaseHeight(int x,int z,Heightmap.Types type,LevelHeightAccessor level,RandomState random){
        type=SubmergedVillages.projection(type);
        double weight=OceanBounds.islandWeight(x,z);
        if(weight==0)return super.getBaseHeight(x,z,type,level,random);
        int natural=super.getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,level,random)-1;
        int ground=OceanBounds.terrainHeight(x,z,natural);
        return type.isOpaque().test(Blocks.WATER.defaultBlockState())?Math.max(ground+1,WorldHeights.SEA_LEVEL):ground+1;
    }

    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Blender b,RandomState r,StructureManager s,ChunkAccess c){
        return super.fillFromNoise(b,r,s,c).thenApply(chunk->{
            var p=new BlockPos.MutableBlockPos();
            for(int lx=0;lx<16;lx++)for(int lz=0;lz<16;lz++){
                int x=chunk.getPos().getMinBlockX()+lx,z=chunk.getPos().getMinBlockZ()+lz;
                int natural=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,lx,lz);
                double weight=OceanBounds.islandWeight(x,z);
                int ground=OceanBounds.terrainHeight(x,z,natural);
                if(weight>0){
                    for(int y=WorldHeights.MIN_Y;y<WorldHeights.MAX_Y_EXCLUSIVE;y++){
                        // Retain the noise-generated underground, including aquifers and caves.
                        if(y<Math.min(natural,ground)-4)continue;
                        BlockState state;
                        if(y==WorldHeights.MIN_Y)state=Blocks.BEDROCK.defaultBlockState();
                        else if(y>ground)state=(y<WorldHeights.SEA_LEVEL?Blocks.WATER:Blocks.AIR).defaultBlockState();
                        else if(y==ground&&ground>=WorldHeights.SEA_LEVEL+2)state=Blocks.GRASS_BLOCK.defaultBlockState();
                        else if(y>=ground-3)state=(ground>=WorldHeights.SEA_LEVEL+2?Blocks.DIRT:Blocks.SAND).defaultBlockState();
                        else state=Blocks.STONE.defaultBlockState();
                        chunk.setBlockState(p.set(x,y,z),state,false);
                    }
                }else{
                    // Fill only above the topmost solid surface; do not erase underground air pockets.
                    for(int y=natural+1;y<WorldHeights.SEA_LEVEL;y++)
                        if(chunk.getBlockState(p.set(x,y,z)).isAir())chunk.setBlockState(p,Blocks.WATER.defaultBlockState(),false);
                }
            }
            Heightmap.primeHeightmaps(chunk,EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG,Heightmap.Types.WORLD_SURFACE_WG));
            return chunk;
        });
    }

    @Override public void buildSurface(WorldGenRegion region,StructureManager structures,RandomState random,ChunkAccess chunk){
        var biomes=region.registryAccess().registryOrThrow(Registries.BIOME);
        int[] floors=new int[256];
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)floors[z*16+x]=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z);
        chunk.fillBiomesFromNoise((qx,qy,qz,sampler)->{
            int x=QuartPos.toBlock(qx),z=QuartPos.toBlock(qz),y=QuartPos.toBlock(qy);
            int floor=floors[(z&15)*16+(x&15)];
            var original=getBiomeSource().getNoiseBiome(qx,qy,qz,sampler);
            if(floor>=WorldHeights.SEA_LEVEL||y<floor-16)return original;
            float temperature=original.value().getBaseTemperature();
            boolean deep=WorldHeights.SEA_LEVEL-floor>=48;
            var key=temperature<0.15f?(deep?Biomes.DEEP_FROZEN_OCEAN:Biomes.FROZEN_OCEAN)
                    :temperature<0.5f?(deep?Biomes.DEEP_COLD_OCEAN:Biomes.COLD_OCEAN)
                    :temperature>1.0f?Biomes.WARM_OCEAN
                    :temperature>0.8f?(deep?Biomes.DEEP_LUKEWARM_OCEAN:Biomes.LUKEWARM_OCEAN)
                    :(deep?Biomes.DEEP_OCEAN:Biomes.OCEAN);
            return biomes.getHolderOrThrow(key);
        },random.sampler());
        super.buildSurface(region,structures,random,chunk);
    }

    @Override public void applyCarvers(WorldGenRegion r,long seed,RandomState state,BiomeManager bm,StructureManager sm,ChunkAccess c,GenerationStep.Carving step){
        if(!OceanBounds.protectedSurface(c.getPos().getMiddleBlockX(),c.getPos().getMiddleBlockZ()))
            super.applyCarvers(r,seed,state,bm,sm,c,step);
    }
    @Override public void applyBiomeDecoration(WorldGenLevel level,ChunkAccess chunk,StructureManager sm){
        super.applyBiomeDecoration(level,chunk,sm);
        // Templates can carve air after the initial flood. Fill village rooms only,
        // clipped to this chunk; leave unrelated underground cave air untouched.
        var cursor=new BlockPos.MutableBlockPos();
        for(var start:sm.startsForStructure(chunk.getPos(),structure->SubmergedVillages.isVillage(level.registryAccess(),structure)))
            for(var piece:start.getPieces()) {
                var box=piece.getBoundingBox();
                for(int x=Math.max(box.minX(),chunk.getPos().getMinBlockX());x<=Math.min(box.maxX(),chunk.getPos().getMaxBlockX());x++)
                    for(int z=Math.max(box.minZ(),chunk.getPos().getMinBlockZ());z<=Math.min(box.maxZ(),chunk.getPos().getMaxBlockZ());z++)
                        for(int y=Math.max(box.minY(),WorldHeights.MIN_Y);y<=Math.min(box.maxY(),WorldHeights.SEA_LEVEL-1);y++) {
                            var block=chunk.getBlockState(cursor.set(x,y,z));
                            if(block.isAir())chunk.setBlockState(cursor,Blocks.WATER.defaultBlockState(),false);
                            else if(block.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED))
                                chunk.setBlockState(cursor,block.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED,true),false);
                        }
            }
        Heightmap.primeHeightmaps(chunk,EnumSet.allOf(Heightmap.Types.class));
        if(core(chunk))IslandArchitecture.decorate(chunk);
        VoyageTerrain.decorate(this,level.getSeed(),chunk);
    }
    @Override public void createStructures(RegistryAccess access,ChunkGeneratorStructureState state,StructureManager sm,ChunkAccess c,StructureTemplateManager tm){
        if(OceanBounds.protectedSurface(c.getPos().getMiddleBlockX(),c.getPos().getMiddleBlockZ()))return;
        int x=c.getPos().getMiddleBlockX(),z=c.getPos().getMiddleBlockZ();
        var biome=getBiomeSource().getNoiseBiome(x>>2,32,z>>2,state.randomState().sampler());
        boolean submergedLand=super.getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,c,state.randomState())<WorldHeights.SEA_LEVEL
                &&!biome.is(BiomeTags.IS_OCEAN)&&!biome.is(BiomeTags.IS_RIVER);
        super.createStructures(access,state,sm,c,tm);
        // Keep the previous restriction for other land structures, but allow the
        // original villages to survive the flood at their actual terrain height.
        if(submergedLand)for(var structure:java.util.List.copyOf(c.getAllStarts().keySet()))
            if(!SubmergedVillages.isVillage(access,structure))
                c.setStartForStructure(structure,net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START);
    }
    @Override public void spawnOriginalMobs(WorldGenRegion region){
        super.spawnOriginalMobs(region);
    }
}
