package dbv.acceptance;
import java.nio.file.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.*;
@EventBusSubscriber(modid="dbv_world",value=Dist.CLIENT)
public class RenderCheck {
 static int frames; static boolean done; static List<LivingEntity> mobs=new ArrayList<>(); static List<ItemStack> items=new ArrayList<>();
 @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
  var mc=Minecraft.getInstance(); if(done||mc.level==null||mc.player==null)return;
  try {
   if(mobs.isEmpty()) {
    var c=JsonParser.parseReader(new InputStreamReader(RenderCheck.class.getResourceAsStream("/dbv_prototypes.json"))).getAsJsonObject();
    for(var x:c.getAsJsonArray("mobs")) {var id=ResourceLocation.fromNamespaceAndPath("dbv_world",x.getAsJsonObject().get("id").getAsString());mobs.add((LivingEntity)BuiltInRegistries.ENTITY_TYPE.get(id).create(mc.level));}
    for(var x:c.getAsJsonArray("items")) {var id=ResourceLocation.fromNamespaceAndPath("dbv_world",x.getAsJsonObject().get("id").getAsString());var s=new ItemStack(BuiltInRegistries.ITEM.get(id));if(mc.getItemRenderer().getModel(s,mc.level,mc.player,0)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing model "+id);items.add(s);}
   }
   var g=event.getGuiGraphics();g.fill(0,0,g.guiWidth(),g.guiHeight(),0xff173040);
   int i=0;for(var mob:mobs){mob.tickCount=frames;InventoryScreen.renderEntityInInventory(g,35+(i%5)*75,65+(i/5)*65,32,new Vector3f(),new Quaternionf().rotationZ((float)java.lang.Math.PI),null,mob);i++;}
   i=0;for(var s:items){g.renderItem(s,10+(i%27)*18,220+(i/27)*20);i++;}
   if(++frames>=120){done=true;var r=new JsonObject();r.addProperty("passed",true);r.addProperty("renderedMobs",mobs.size());r.addProperty("renderedItems",items.size());r.addProperty("frames",frames);Files.writeString(mc.gameDirectory.toPath().resolve("render-check.json"),r.toString());net.minecraft.client.Screenshot.grab(mc.gameDirectory,"models.png",mc.getMainRenderTarget(),x->{});}
  }catch(Throwable t){done=true;try{Files.writeString(mc.gameDirectory.toPath().resolve("render-check.json"),"ERROR: "+t);}catch(Exception ignored){}throw new RuntimeException(t);}
 }
}


