package dbv.acceptance;
import java.util.*;
import com.google.gson.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import io.github.low2uu.dbv.content.PrototypeContent;
import io.github.low2uu.dbv.fleet.FleetModule;
public class RuntimeCheck {
 static List<Mob> mobs=new ArrayList<>();
 public static int spawn(ServerLevel l) {
  var entries=new ArrayList<PrototypeContent.Entry<?>>();entries.addAll(PrototypeContent.SQUIDS);entries.addAll(PrototypeContent.GUARDIANS);entries.addAll(PrototypeContent.DROWNED);
  int i=0;for(var e:entries){var m=e.type().get().create(l);m.moveTo(16+(i%5)*8,120,128+(i/5)*8,0,0);m.setInvulnerable(true);m.setPersistenceRequired();if(!l.addFreshEntity(m))throw new IllegalStateException("Spawn failed");mobs.add(m);i++;}return i;
 }
 public static JsonObject finish(ServerLevel l) throws java.io.IOException {
  var r=new JsonObject();var a=new JsonArray();for(var m:mobs){if(m.isRemoved()||m.tickCount<100)throw new IllegalStateException("Mob tick failed "+m);var x=new JsonObject();x.addProperty("id",net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()).toString());x.addProperty("ticks",m.tickCount);x.addProperty("health",m.getHealth());a.add(x);m.discard();}
  var module=FleetModule.get(l.getServer());var b=module.refit().currentBody(module);if(b==null)throw new IllegalStateException("Ship not loaded");
  boolean eligible=module.inspection().inspect(b).qualification().eligible();if(!eligible)throw new IllegalStateException("Ship qualification failed");module.storage().backup(module);
  r.addProperty("passed",true);r.addProperty("shipEligible",eligible);r.addProperty("moored",module.starter().moored(b));r.addProperty("bodyCount",module.adapter().loaded(l).size());r.add("mobs",a);return r;
 }
}

