var opened=false,ticks=0,checked=false;
NativeEvents.onEvent('net.neoforged.neoforge.client.event.ClientTickEvent$Post',e=>{
var mc=Java.loadClass('net.minecraft.client.Minecraft').getInstance();
if(!opened&&mc.screen!=null&&(String(mc.screen).includes('TitleScreen')||String(mc.screen).includes('DeepSeasWelcomeScreen'))){opened=true;mc.createWorldOpenFlows().openWorld('DBV-World5-Fresh2',()=>console.error('WORLD5 FAIL open'));}
if(mc.level==null)return;++ticks;
if(ticks%40==0){
var ready=JsonIO.read('kubejs/ui-ready.json');if(ready!=null&&ready.ready&&!checked){checked=true;try{
var R=Java.loadClass('net.minecraft.core.registries.BuiltInRegistries'),L=Java.loadClass('net.minecraft.resources.ResourceLocation');
var item=R.ITEM.get(L.parse('dbv_world:jiuhu_console')),tab=R.CREATIVE_MODE_TAB.get(L.parse('dbv_world:deep_blue_voyage'));
if(!tab.getDisplayItems().stream().anyMatch(x=>String(x.getItem())=='dbv_world:jiuhu_console'))throw Error('creative item missing');
console.info('WORLD5 CREATIVE PASS '+item.getDescriptionId()+' name='+Java.loadClass('net.minecraft.client.resources.language.I18n').get('block.dbv_world.jiuhu_console'));
console.info('WORLD5 UI '+mc.screen);Java.loadClass('net.minecraft.client.Screenshot').grab(mc.gameDirectory,'world5-console-management.png',mc.getMainRenderTarget(),x=>console.info('WORLD5 screenshot '+x.getString()));
}catch(x){console.error('WORLD5 CLIENT FAIL '+x);}}
var done=JsonIO.read('kubejs/done.json');if(done!=null&&done.done)mc.stop();}
if(ticks>6000)mc.stop();});
