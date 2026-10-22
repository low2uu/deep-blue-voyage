const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path'),S=require('./core.js');
const root=path.resolve(__dirname,'../..'),res=path.join(root,'mods/dbv-world/src/main/resources');
const read=p=>JSON.parse(fs.readFileSync(p,'utf8'));
test('69份设计器JSON往返一致，运行时、翻译、模型和掉落完整对应',()=>{
 const runtime=read(path.join(res,'dbv_prototypes.json')),ids=new Set(runtime.items.map(i=>'dbv_world:'+i.id));
 assert.equal(runtime.items.length,54);assert.equal(runtime.mobs.length,15);
 const zh=read(path.join(res,'assets/dbv_world/lang/zh_cn.json')),en=read(path.join(res,'assets/dbv_world/lang/en_us.json'));
 for(const folder of ['items','mobs'])for(const f of fs.readdirSync(path.join(root,'content/prototypes',folder)).filter(f=>f.endsWith('.json'))){
  const d=read(path.join(root,'content/prototypes',folder,f));assert.deepEqual(S.validate(d).errors,[],f);assert.deepEqual(S.importDocument(d),d);
  const id=d.sections.identity.registryId.value.split(':')[1],key=(folder==='items'?'item.':'entity.')+'dbv_world.'+id;
  assert.ok(zh[key]);assert.ok(en[key]);assert.ok(fs.existsSync(path.join(res,'assets/dbv_world/models/item',id+(folder==='mobs'?'_spawn_egg':'')+'.json')));
  if(folder==='mobs'){
   const m=runtime.mobs.find(m=>m.id===id);assert.ok(m);assert.equal(m.health,d.sections.monster.health.value);assert.equal(d.sections.monster.naturalSpawn.value,false);
   const loot=read(path.join(res,'data/dbv_world/loot_table/entities',id+'.json'));
   assert.equal(loot.pools.length,d.sections.monster.drops.value.length);
   loot.pools.forEach((pool,i)=>{assert.ok(ids.has(pool.entries[0].name));assert.equal(pool.entries[0].name,d.sections.monster.drops.value[i].item);});
  }else assert.equal(runtime.items.find(i=>i.id===id).stack,d.sections.item.maxStackSize.value);
 }
});
