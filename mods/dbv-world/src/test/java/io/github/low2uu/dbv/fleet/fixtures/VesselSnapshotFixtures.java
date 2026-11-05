package io.github.low2uu.dbv.fleet.fixtures;

import com.google.gson.*;
import java.util.UUID;

/** Small captured-data fixtures: persistence tests treat the upstream typed blueprint as opaque JSON. */
public final class VesselSnapshotFixtures {
    public static final String IRON="minecraft:iron_block",CHEST="minecraft:chest",GLASS="minecraft:glass";
    private VesselSnapshotFixtures() {}

    public static JsonObject recoveryTransaction(UUID vessel,UUID body,UUID candidate,String phase){
        var tx=new JsonObject();tx.addProperty("phase",phase);tx.addProperty("vesselId",vessel.toString());
        tx.addProperty("originalBody",body.toString());tx.addProperty("candidate",candidate.toString());
        tx.addProperty("startedAt","2026-11-05T06:00:00Z");
        tx.add("source",snapshot(vessel,64,IRON,CHEST,GLASS));tx.add("before",snapshot(vessel,3,IRON,CHEST));
        var originals=new JsonArray();originals.add(body.toString());tx.add("originalBodies",originals);
        var candidates=new JsonArray();candidates.add(candidate.toString());tx.add("candidates",candidates);return tx;
    }

    public static JsonObject snapshot(UUID vessel,int diamonds,String... blocks){
        var root=new JsonObject();root.addProperty("schemaVersion",1);root.addProperty("vesselId",vessel.toString());
        var modules=new JsonObject();root.add("modules",modules);
        var structure=new JsonObject();structure.addProperty("schemaVersion",1);structure.addProperty("metadataAdapter","typed-json-v1");
        var entries=new JsonArray();
        for(int i=0;i<blocks.length;i++){
            var block=new JsonObject();block.addProperty("x",i);block.addProperty("y",0);block.addProperty("z",0);
            var state=new JsonObject();state.addProperty("Name",blocks[i]);block.add("state",state);entries.add(block);
        }
        structure.add("blocks",entries);
        structure.add("nativeBlueprint",JsonParser.parseString("""
                {"type":10,"value":{"name":{"type":8,"value":"设备与库存恢复测试艇"},
                "equipment":{"type":10,"value":{"diesel":{"type":3,"value":"8123"},"energy":{"type":4,"value":"9007199254740993"}}}}}
                """));
        modules.add("structure",structure);
        var storage=JsonParser.parseString("""
                {"schemaVersion":1,"containers":[{"position":"1, 0, 0","block":"minecraft:chest","slots":27,"restorable":true,
                "items":[{"slot":4,"label":"钻石","stack":{"id":"minecraft:diamond","count":1}}]}]}
                """).getAsJsonObject();
        storage.getAsJsonArray("containers").get(0).getAsJsonObject().getAsJsonArray("items").get(0).getAsJsonObject()
                .getAsJsonObject("stack").addProperty("count",diamonds);
        modules.add("storage",storage);
        modules.add("navigation",JsonParser.parseString("{\"schemaVersion\":1,\"departed\":true}"));
        modules.add("berth",JsonParser.parseString("{\"schemaVersion\":1,\"dock\":\"starter\"}"));
        modules.add("refit",JsonParser.parseString("{\"schemaVersion\":1,\"active\":false}"));
        modules.add("captain",JsonParser.parseString("{\"questStep\":7,\"name\":\"酒狐\"}"));
        return root;
    }
}
