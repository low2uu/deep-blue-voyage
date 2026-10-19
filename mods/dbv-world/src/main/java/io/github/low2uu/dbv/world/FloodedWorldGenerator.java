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
    private boolean inside(ChunkAccess c){return OceanBounds.chunkIntersects(c.getPos().x,c.getPos().z);}
    private boolean core(ChunkAccess c){return OceanBounds.preserved(c.getPos().getMinBlockX(),c.getPos().getMinBlockZ());}
    @Override public int getSpawnHeight(LevelHeightAccessor l){return IslandTerrain.PLATFORM_Y+1;}

    @Override public int getBaseHeight(int x,int z,Heightmap.Types type,LevelHeightAccessor level,RandomState random){
        if(!OceanBounds.contains(x,z))return level.getMinBuildHeight();
        double weight=OceanBounds.islandWeight(x,z);
        if(weight==0)return super.getBaseHeight(x,z,type,level,random);
        int natural=super.getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,level,random)-1;
        int ground=(int)Math.round(natural*(1-weight)+IslandTerrain.groundHeight(x,z)*weight);
        return type.isOpaque().test(Blocks.WATER.defaultBlockState())?Math.max(ground+1,WorldHeights.SEA_LEVEL):ground+1;
    }

    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Blender b,RandomState r,StructureManager s,ChunkAccess c){
        if(!inside(c))return CompletableFuture.completedFuture(c);
        return super.fillFromNoise(b,r,s,c).thenApply(chunk->{
            var p=new BlockPos.MutableBlockPos();
            for(int lx=0;lx<16;lx++)for(int lz=0;lz<16;lz++){
                int x=chunk.getPos().getMinBlockX()+lx,z=chunk.getPos().getMinBlockZ()+lz;
                if(!OceanBounds.contains(x,z)){
                    for(int y=WorldHeights.MIN_Y;y<WorldHeights.MAX_Y_EXCLUSIVE;y++)chunk.setBlockState(p.set(x,y,z),Blocks.AIR.defaultBlockState(),false);
                    continue;
                }
                int natural=chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,lx,lz);
                double weight=OceanBounds.islandWeight(x,z);
                int ground=(int)Math.round(natural*(1-weight)+IslandTerrain.groundHeight(x,z)*weight);
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
        if(!inside(chunk))return;
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
        if(inside(c)&&!OceanBounds.protectedSurface(c.getPos().getMiddleBlockX(),c.getPos().getMiddleBlockZ()))
            super.applyCarvers(r,seed,state,bm,sm,c,step);
    }
    @Override public void applyBiomeDecoration(WorldGenLevel level,ChunkAccess chunk,StructureManager sm){
        if(!inside(chunk))return;
        super.applyBiomeDecoration(level,chunk,sm);
        if(core(chunk))IslandArchitecture.decorate(chunk);
    }
    @Override public void createStructures(RegistryAccess access,ChunkGeneratorStructureState state,StructureManager sm,ChunkAccess c,StructureTemplateManager tm){
        if(!inside(c)||OceanBounds.protectedSurface(c.getPos().getMiddleBlockX(),c.getPos().getMiddleBlockZ()))return;
        int x=c.getPos().getMiddleBlockX(),z=c.getPos().getMiddleBlockZ();
        var biome=getBiomeSource().getNoiseBiome(x>>2,32,z>>2,state.randomState().sampler());
        if(super.getBaseHeight(x,z,Heightmap.Types.OCEAN_FLOOR_WG,c,state.randomState())<WorldHeights.SEA_LEVEL
                &&!biome.is(BiomeTags.IS_OCEAN)&&!biome.is(BiomeTags.IS_RIVER))return;
        super.createStructures(access,state,sm,c,tm);
    }
    @Override public void spawnOriginalMobs(WorldGenRegion region){
        var c=region.getCenter();
        if(OceanBounds.contains(c.getMinBlockX(),c.getMinBlockZ())&&OceanBounds.contains(c.getMaxBlockX(),c.getMaxBlockZ())
                )super.spawnOriginalMobs(region);
    }
}
