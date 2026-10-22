// Compile reviewed full designer specs into the subset supported by the game prototype.
const fs=require('node:fs'),path=require('node:path'),S=require('./core.js');
const root=path.resolve(__dirname,'../..'), res=path.join(root,'mods/dbv-world/src/main/resources');
function write(p,v){fs.mkdirSync(path.dirname(p),{recursive:true});fs.writeFileSync(p,JSON.stringify(v,null,2)+'\n');}
function load(folder){return fs.readdirSync(path.join(root,'content/prototypes',folder)).filter(f=>f.endsWith('.json')).sort().map(f=>{
 const d=S.importDocument(JSON.parse(fs.readFileSync(path.join(root,'content/prototypes',folder,f),'utf8')));const errors=S.validate(d).errors;if(errors.length)throw Error(f+': '+errors.join('; '));return d;
});}
function value(d,g,k){const f=d.sections[g][k];if(f.status!=='specified'||f.value===null)throw Error(`${g}.${k} must be specified`);return f.value;}
const zh=JSON.parse(fs.readFileSync(path.join(res,'assets/dbv_world/lang/zh_cn.json'),'utf8')),en=JSON.parse(fs.readFileSync(path.join(res,'assets/dbv_world/lang/en_us.json'),'utf8'));
const ids=new Set(),runtime={items:[],mobs:[]};
function id(d){const full=value(d,'identity','registryId');if(!/^dbv_world:[a-z0-9_]+$/.test(full)||ids.has(full))throw Error('Invalid/duplicate ID '+full);ids.add(full);return full.split(':')[1];}
function names(d,key){zh[key]=value(d,'identity','nameZh');en[key]=value(d,'identity','nameEn');}
for(const d of load('items')){const key=id(d);if(value(d,'identity','kind')!=='item')throw Error('Only plain prototype items supported');const stack=value(d,'item','maxStackSize');runtime.items.push({id:key,stack});names(d,'item.dbv_world.'+key);write(path.join(res,'assets/dbv_world/models/item',key+'.json'),{parent:'minecraft:item/generated',textures:{layer0:value(d,'appearance','textureAll')}});}
for(const d of load('mobs')){const key=id(d),get=k=>value(d,'monster',k);if(get('naturalSpawn')!==false)throw Error('Natural spawning is not implemented');if(!['squid','guardian','drowned'].includes(get('base')))throw Error('Unsupported base');
 const m={id:key};for(const k of ['base','health','plannedHealth','width','height','scale','speed','attackDamage','armor','followRange','knockbackResistance','boss'])m[k]=get(k);
 for(const k of ['eggPrimary','eggSecondary']){const hex=get(k);if(!/^[0-9a-fA-F]{6}$/.test(hex))throw Error('Invalid egg color');m[k]=parseInt(hex,16);}
 runtime.mobs.push(m);names(d,'entity.dbv_world.'+key);zh['entity.dbv_world.'+key]+='（原型）';en['entity.dbv_world.'+key]+=' (Prototype)';zh['item.dbv_world.'+key+'_spawn_egg']=value(d,'identity','nameZh')+'刷怪蛋（原型）';en['item.dbv_world.'+key+'_spawn_egg']=value(d,'identity','nameEn')+' Spawn Egg (Prototype)';
 write(path.join(res,'assets/dbv_world/models/item',key+'_spawn_egg.json'),{parent:'minecraft:item/template_spawn_egg'});
 write(path.join(res,'data/dbv_world/loot_table/entities',key+'.json'),{type:'minecraft:entity',pools:get('drops').map(drop=>{if(!ids.has(drop.item))throw Error('Unknown drop '+drop.item);return {rolls:1,conditions:drop.chance===1?[]:[{condition:'minecraft:random_chance',chance:drop.chance}],entries:[{type:'minecraft:item',name:drop.item,functions:[{function:'minecraft:set_count',count:drop.min===drop.max?drop.min:{type:'minecraft:uniform',min:drop.min,max:drop.max}}]}]};})});
}
zh['tooltip.dbv_world.prototype']='原型物品：功能与配方尚未实现，数值待定';en['tooltip.dbv_world.prototype']='Prototype item: functions and recipes pending; balance provisional';
write(path.join(res,'dbv_prototypes.json'),runtime);write(path.join(res,'assets/dbv_world/lang/zh_cn.json'),zh);write(path.join(res,'assets/dbv_world/lang/en_us.json'),en);
console.log(`Compiled ${runtime.items.length} items and ${runtime.mobs.length} mobs.`);
