package io.github.low2uu.dbv.content.client;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Client-only adapter from the bundled, versioned rig data to vanilla ModelParts. */
public final class CreatureGeometry {
    public record Cube(float[] origin, float[] size, int[] uv) {}
    public record Joint(String name, String parent, float[] pivot, List<Cube> cubes,
            String axis, float amplitude, float rate, float phase, String motion) {}
    public record Rig(String format, int version, String texture, int textureWidth, int textureHeight,
            double cullingRadius, List<Joint> parts) {
        public LayerDefinition layer() {
            MeshDefinition mesh = new MeshDefinition();
            Map<String, PartDefinition> nodes = new LinkedHashMap<>();
            nodes.put("root", mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0,24,0)));
            for (var p : parts) {
                var cubes = CubeListBuilder.create();
                for (var c : p.cubes()) cubes.texOffs(c.uv()[0],c.uv()[1]).addBox(
                        c.origin()[0],c.origin()[1],c.origin()[2],c.size()[0],c.size()[1],c.size()[2]);
                nodes.put(p.name(), nodes.get(p.parent()).addOrReplaceChild(p.name(), cubes,
                        PartPose.offset(p.pivot()[0],p.pivot()[1],p.pivot()[2])));
            }
            return LayerDefinition.create(mesh, textureWidth, textureHeight);
        }
    }
    private static final Map<String,Rig> CACHE = new HashMap<>();
    public static ModelLayerLocation location(String id) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("dbv_world", id), "main");
    }
    public static synchronized Rig load(String id) { return CACHE.computeIfAbsent(id, CreatureGeometry::read); }
    private static Rig read(String id) {
        try (var in = new InputStreamReader(Objects.requireNonNull(CreatureGeometry.class.getResourceAsStream(
                "/assets/dbv_world/models/entity/" + id + ".json")), StandardCharsets.UTF_8)) {
            Rig rig = new Gson().fromJson(in, Rig.class);
            if (!"dbv-creature-model".equals(rig.format()) || rig.version()!=1 || rig.parts()==null
                    || rig.parts().isEmpty() || rig.textureWidth()<=0 || rig.textureHeight()<=0
                    || !Double.isFinite(rig.cullingRadius()) || rig.cullingRadius()<=0) throw new IllegalArgumentException("Unsupported rig");
            var seen = new java.util.HashSet<String>(); seen.add("root");
            for (var p : rig.parts()) {
                if (!seen.contains(p.parent()) || !seen.add(p.name()) || p.pivot().length!=3 || p.cubes()==null
                        || !List.of("x","y","z").contains(p.axis())) throw new IllegalArgumentException("Invalid joint: "+p.name());
                for (var c : p.cubes()) {
                    if (c.origin().length!=3 || c.size().length!=3 || c.uv().length!=2) throw new IllegalArgumentException("Invalid cube");
                    for (float v : c.size()) if (!Float.isFinite(v) || v<=0) throw new IllegalArgumentException("Invalid cube dimensions");
                }
            }
            return rig;
        } catch (Exception e) { throw new IllegalStateException("Cannot load creature geometry: " + id,e); }
    }
    private CreatureGeometry() {}
}
