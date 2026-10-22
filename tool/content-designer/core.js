(function (root) {
  "use strict";
  const catalog = root.ContentCatalog || require("./catalog.js");
  const FORMAT = "dbv-content-spec";
  const states = ["specified", "inherit", "pending", "not_applicable"];
  const copy = value => JSON.parse(JSON.stringify(value));
  const options = f => f.options.map(x => x.split("|")[0]);
  function emptyValue(f) {
    if (f.type === "rows" || f.type === "lines") return [];
    if (f.type === "boolean") return false;
    if (f.type === "number" || f.type === "integer") return null;
    if (f.type === "enum") return options(f)[0];
    return "";
  }
  function newDocument() {
    const doc = { format: FORMAT, schemaVersion: 2, purpose: "implementation_specification",
      metadata: { createdAt: new Date().toISOString(), updatedAt: new Date().toISOString() },
      sections: {}, extensions: [] };
    for (const g of catalog.groups) {
      doc.sections[g.id] = {};
      for (const f of g.fields) doc.sections[g.id][f.id] = { status: "pending", value: null, reference: "", notes: "" };
    }
    for (const [key, value] of Object.entries({ minecraft: "1.21.1", loader: "neoforge", loaderVersion: "21.1.259", java: 21, namespace: "dbv_world", installationSide: "both" }))
      set(doc, "platform", key, value);
    return doc;
  }
  function set(doc, group, key, value, reference = "", notes = "") {
    doc.sections[group][key] = { status: "specified", value, reference, notes };
  }
  function hullPreset() {
    const d = newDocument();
    const values = {
      identity: { registryId: "dbv_world:iron_submarine_hull", nameZh: "铁质潜艇壳", nameEn: "Iron Submarine Hull", kind: "block", category: "潜艇外壳",
        summary: "灰色平滑金属潜艇外壳，边缘带螺丝接缝，六面相同。采集按铁块；防护、航空学物理与深海学耐压按黑曜石。", creativeTabs: ["dbv_world:deep_blue_voyage"] },
      appearance: { model: "cube_all", visualDescription: "灰色平滑金属，四边拼接缝、四角螺丝，六面材质相同。", textureAll: "dbv_world:block/iron_submarine_hull", textureResolution: 64, renderLayer: "solid", lightLevel: 0, occludes: true, fullCube: true, waterlogged: false, facing: "none", soundType: "minecraft:obsidian（石质音效）" },
      harvesting: { referenceBlock: "minecraft:iron_block", hardness: 5, tool: "pickaxe", minimumTier: "stone", requiresCorrectTool: true, dropsSelf: true, silkTouch: "掉落自身", fortune: "不增加掉落", experience: 0 },
      protection: { referenceBlock: "minecraft:obsidian", explosionResistance: 1200, pistonReaction: "block", dragonImmune: true, witherImmune: false, flammability: 0, fireSpread: 0, replaceable: false, gravityAffected: false },
      physics: { referenceBlock: "minecraft:obsidian", bindingPolicy: "follow_runtime", mass: 2, volume: 1, friction: 1, restitution: 0, fragile: false, inertia: "engine", assemblyAllowed: true },
      pressure: { referenceBlock: "minecraft:obsidian", bindingPolicy: "follow_runtime", maxWaterDepth: 500, implosionChance: 0.06, useGlobalMultipliers: true, sealsCompartment: true },
      item: { maxStackSize: 64, rarity: "common", fireResistant: false },
      persistence: { hasBlockEntity: false, vesselOwned: true, authoritativeSource: "实际世界方块为权威；主艇独立JSON保存自身ID、状态与相对位置。" },
      integration: { excludedBehaviors: ["不继承黑曜石传送门或配方身份", "不改变全局物理与耐压算法"], knownConflicts: "这是待实施目标，不代表v0.1.5已具备这些跨Mod属性。质量和耐压数值为核对基线，运行时应跟随同一世界的黑曜石。" },
      acceptance: { openQuestions: ["是否增加合成配方？", "是否改用金属音效？"], nonGoals: ["自动替换已有潜艇外壳"],
        tests: [{ name: "采集与防护分离", setup: "同状态玩家；石镐、金镐及铁块对照", steps: "比较速度与掉落；读取爆炸抗性", expected: "采集同铁块；金镐不掉落；爆炸抗性1200", priority: "required" },
          { name: "物理与耐压一致", setup: "相同世界中黑曜石与新壳块", steps: "读取Sable和Deep Seas最终属性并保存重进", expected: "对应材料属性一致；备份恢复仍是新壳ID", priority: "required" }] }
    };
    for (const [g, fields] of Object.entries(values)) for (const [key, value] of Object.entries(fields)) set(d, g, key, value);
    for (const key of ["mass", "volume", "friction", "restitution", "fragile", "inertia"]) {
      d.sections.physics[key].reference = "minecraft:obsidian";
      d.sections.physics[key].notes = "当前安装版本的基线值；follow_runtime策略要求实现时读取参考方块最终值。";
    }
    for (const key of ["maxWaterDepth", "implosionChance"]) {
      d.sections.pressure[key].reference = "minecraft:obsidian";
      d.sections.pressure[key].notes = "基线值，不是整船性能保证；遵循follow_runtime策略。";
    }
    for (const key of ["durability", "equipmentSlot", "attackDamage", "attackSpeed", "armor", "toughness", "knockbackResistance", "repairIngredients", "enchantments"])
      d.sections.item[key].status = "not_applicable";
    for (const key of ["floatingMaterial", "floatingScale", "customInertia"]) d.sections.physics[key].status = "not_applicable";
    for (const rec of Object.values(d.sections.machine)) rec.status = "not_applicable";
    return d;
  }
  const plain = v => v !== null && typeof v === "object" && !Array.isArray(v);
  function structuralErrors(doc, allowIncompleteValues = false) {
    const errors = [];
    const fail = s => errors.push(s);
    const keys = (obj, expected, where) => {
      if (!plain(obj)) { fail(`${where} 必须是对象`); return false; }
      for (const key of expected) if (!Object.hasOwn(obj, key)) fail(`${where} 缺少 ${key}`);
      for (const key of Object.keys(obj)) if (!expected.includes(key)) fail(`${where} 含未知字段 ${key}；请放入扩展需求，不能静默丢弃`);
      return true;
    };
    if (!keys(doc, ["format", "schemaVersion", "purpose", "metadata", "sections", "extensions"], "文档")) return errors;
    if (doc.format !== FORMAT || doc.schemaVersion !== 2 || doc.purpose !== "implementation_specification") fail("文档格式/版本不支持；请通过导入升级旧版规格");
    if (keys(doc.metadata, ["createdAt", "updatedAt"], "metadata")) for (const v of Object.values(doc.metadata)) if (typeof v !== "string" || !Number.isFinite(Date.parse(v))) fail("时间格式无效");
    if (keys(doc.sections, catalog.groups.map(x => x.id), "sections")) {
      for (const g of catalog.groups) {
        if (!keys(doc.sections[g.id], g.fields.map(x => x.id), g.title)) continue;
        for (const f of g.fields) {
          const r = doc.sections[g.id][f.id], path = `${g.title} / ${f.label}`;
          if (!keys(r, ["status", "value", "reference", "notes"], path)) continue;
          if (!states.includes(r.status)) fail(`${path} 状态无效`);
          if (typeof r.reference !== "string" || typeof r.notes !== "string") fail(`${path} 来源和备注必须是文本`);
          if (r.value !== null) validateValue(f, r.value, path, fail, keys, allowIncompleteValues);
          if (["pending", "not_applicable"].includes(r.status) && r.value !== null) fail(`${path} 待定/不适用的值必须为空`);
        }
      }
    }
    if (!Array.isArray(doc.extensions)) fail("扩展需求必须是数组");
    else for (const row of doc.extensions) {
      if (keys(row, ["key", "description", "acceptance"], "扩展需求"))
        for (const v of Object.values(row)) if (typeof v !== "string") fail("扩展需求必须为文本");
    }
    return errors;
  }
  function validateValue(f, value, path, fail, keys, allowIncompleteValues = false) {
    if (f.type === "rows") {
      if (!Array.isArray(value)) return fail(`${path} 必须是记录数组`);
      if (value.length > 500) return fail(`${path} 最多500条记录`);
      value.forEach((row, ix) => {
        if (keys(row, f.columns.map(x => x.id), `${path} 第${ix + 1}条`))
          f.columns.forEach(c => { if (row[c.id] !== null) validateValue(c, row[c.id], `${path}/${c.label}`, fail, keys, allowIncompleteValues); else if (!["number", "integer"].includes(c.type)) fail(`${path}/${c.label} 类型无效`); });
      });
    } else if (f.type === "lines") {
      if (!Array.isArray(value) || value.some(x => typeof x !== "string")) fail(`${path} 必须是文本数组`);
    } else if (f.type === "number" || f.type === "integer") {
      if (typeof value !== "number" || !Number.isFinite(value) || (!allowIncompleteValues && f.type === "integer" && !Number.isInteger(value))) fail(`${path} 必须是有效${f.type === "integer" ? "整数" : "数值"}`);
      else if (!allowIncompleteValues && ((f.min !== undefined && value < f.min) || (f.max !== undefined && value > f.max))) fail(`${path} 超出范围 ${f.min ?? "不限"}～${f.max ?? "不限"}`);
    } else if (f.type === "boolean") {
      if (typeof value !== "boolean") fail(`${path} 必须是布尔值`);
    } else if (typeof value !== "string") fail(`${path} 必须是文本`);
    else if (f.type === "enum" && !options(f).includes(value)) fail(`${path} 选项无效`);
    else if (!allowIncompleteValues && f.pattern && !new RegExp(f.pattern).test(value)) fail(`${path} 格式无效`);
  }
  function validate(doc) {
    const errors = structuralErrors(doc), warnings = [];
    if (errors.length) return { errors, warnings };
    let pending = 0;
    for (const g of catalog.groups) for (const f of g.fields) {
      const r = doc.sections[g.id][f.id];
      if (r.status === "pending") pending++;
      if (r.status === "specified" && r.value === null) errors.push(`${g.title} / ${f.label} 已指定但缺少值`);
      if (r.status === "inherit" && !r.reference.trim()) errors.push(`${g.title} / ${f.label} 继承必须填写参考来源`);
      if (f.essential && (r.status !== "specified" || r.value === null || (typeof r.value === "string" && !r.value.trim()))) errors.push(`${g.title} / ${f.label} 必须明确填写，不能待定或继承`);
      if (r.status === "specified" && typeof r.value === "string" && !r.value.trim()) warnings.push(`${g.title} / ${f.label} 已指定但文本为空`);
      if (f.type === "rows" && Array.isArray(r.value)) {
        const seen = new Set();
        r.value.forEach((row, ix) => {
          const first = row[f.columns[0].id];
          if (!first?.trim()) errors.push(`${f.label} 第${ix + 1}条缺少 ${f.columns[0].label}`);
          else if (seen.has(first)) errors.push(`${f.label} 出现重复 ${f.columns[0].label}：${first}`);
          else seen.add(first);
          for (const col of f.columns) if (row[col.id] === null) warnings.push(`${f.label} / ${first || ix + 1} / ${col.label} 尚未填写`);
          const required = {
            recipes: ["id", "type", "ingredients", "result", "count"],
            actions: ["name", "trigger", "steps", "success"],
            ports: ["name", "resource", "direction", "faces", "unit"],
            tests: ["name", "setup", "steps", "expected"],
            fields: ["key", "type", "meaning"],
            adapters: ["modId", "capability", "mechanism"],
            dependencies: ["modId", "version", "purpose"],
            drops: ["item", "min", "max", "chance"]
          }[f.id] || [];
          for (const key of required) if (row[key] === null || (typeof row[key] === "string" && !row[key].trim())) errors.push(`${f.label} 第${ix + 1}条缺少 ${f.columns.find(c => c.id === key).label}`);
          const idKeys = { recipes: ["id", "type", "result"], drops: ["item"], faceTextures: ["texture"] }[f.id] || [];
          for (const key of idKeys) if (row[key] && !/^[a-z0-9_.-]+:[a-z0-9/._-]+$/.test(row[key])) errors.push(`${f.label} 第${ix + 1}条 ${key} 需要 namespace:path 格式`);
          if (Object.hasOwn(row, "min") && row.min !== null && row.max !== null && row.min > row.max) errors.push(`${f.label} 第${ix + 1}条最小值大于最大值`);
        });
      }
    }
    if (pending) warnings.unshift(`${pending} 项仍待设计；JSON会完整保留这些待定项，不应由开发者擅自补值`);
    const get = (g, k) => doc.sections[g][k].status === "specified" ? doc.sections[g][k].value : null;
    const id = get("identity", "registryId"), ns = get("platform", "namespace");
    if (id && ns && id.split(":")[0] !== ns) errors.push("注册ID命名空间与所属Mod不一致");
    if (get("item", "durability") > 0 && get("item", "maxStackSize") > 1) errors.push("具有耐久的物品不能同时设置堆叠数大于1");
    if (get("appearance", "model") === "cube_all" && !get("appearance", "textureAll")) warnings.push("六面同材质模型尚未指定共用贴图资源ID");
    if (get("appearance", "fullCube") === true && ["slab", "stairs"].includes(get("appearance", "model"))) errors.push("半砖/楼梯模型不能同时声明为完整立方体");
    if (get("identity", "kind") === "item" && get("persistence", "hasBlockEntity") === true) errors.push("普通物品不能直接拥有方块实体");
    if (get("identity", "kind") === "monster") {
      for (const key of ["eggPrimary", "eggSecondary"]) {
        const color = get("monster", key);
        if (color !== null && !/^[0-9a-fA-F]{6}$/.test(color)) errors.push("刷怪蛋颜色需要六位十六进制，例如 526963");
      }
      if (get("monster", "plannedHealth") > 1024) warnings.push("目标生命超过原版属性上限1024；需要独立实现，不能直接写入最大生命属性");
      if (get("monster", "naturalSpawn") === true && !get("monster", "spawnRules")) warnings.push("自然生成已开启，但尚未填写群系、数量和刷新条件");
    }
    if (get("harvesting", "hardness") < 0 && get("harvesting", "dropsSelf") === true) warnings.push("不可挖掘方块的掉落需要说明特殊触发方式");
    const resolution = get("appearance", "textureResolution");
    if (resolution && (resolution & (resolution - 1))) errors.push("纹理边长应为2的整数次幂，例如16、32、64");
    for (const g of ["physics", "pressure"]) if (get(g, "bindingPolicy") === "follow_runtime" && !get(g, "referenceBlock")) errors.push(`${g === "physics" ? "物理" : "耐压"}跟随策略必须指定参考方块`);
    const extKeys = new Set();
    doc.extensions.forEach((r, ix) => {
      if (!r.key.trim() || !r.description.trim()) errors.push(`扩展需求第${ix + 1}条需要名称与描述`);
      if (extKeys.has(r.key)) errors.push(`扩展需求重复：${r.key}`);
      extKeys.add(r.key);
    });
    return { errors, warnings, pending };
  }
  function valueSchema(f) {
    const type = f.type;
    if (type === "rows") return { type: "array", maxItems: 500, items: { type: "object", additionalProperties: false, required: f.columns.map(c => c.id), properties: Object.fromEntries(f.columns.map(c => [c.id, ["number", "integer"].includes(c.type) ? { anyOf: [valueSchema(c), { type: "null" }] } : valueSchema(c)])) } };
    if (type === "lines") return { type: "array", items: { type: "string" } };
    if (type === "enum") return { type: "string", enum: options(f) };
    if (type === "boolean") return { type: "boolean" };
    if (["number", "integer"].includes(type)) return { type, ...(f.min !== undefined ? { minimum: f.min } : {}), ...(f.max !== undefined ? { maximum: f.max } : {}) };
    return { type: "string", ...(f.pattern ? { pattern: f.pattern } : {}) };
  }
  function schema() {
    const object = properties => ({ type: "object", additionalProperties: false, required: Object.keys(properties), properties });
    const sections = {};
    for (const g of catalog.groups) {
      const fields = {};
      for (const f of g.fields) {
        fields[f.id] = { title: f.label, ...object({
          status: { enum: states }, value: { anyOf: [valueSchema(f), { type: "null" }] },
          reference: { type: "string" }, notes: { type: "string" }
        }), allOf: [
          { if: { properties: { status: { enum: ["pending", "not_applicable"] } } }, then: { properties: { value: { type: "null" } } } }
        ] };
      }
      sections[g.id] = { title: g.title, ...object(fields) };
    }
    return { $schema: "https://json-schema.org/draft/2020-12/schema", title: "深蓝远航内容开发规格 v2", ...object({
      format: { const: FORMAT }, schemaVersion: { const: 2 }, purpose: { const: "implementation_specification" },
      metadata: object({ createdAt: { type: "string", format: "date-time" }, updatedAt: { type: "string", format: "date-time" } }),
      sections: object(sections),
      extensions: { type: "array", items: object({ key: { type: "string" }, description: { type: "string" }, acceptance: { type: "string" } }) }
    }) };
  }
  function importDocument(input) {
    const d = copy(input);
    if (d.format === FORMAT && d.schemaVersion === 1 && d.sections && !Object.hasOwn(d.sections, "monster")) {
      d.schemaVersion = 2;
      d.sections.monster = newDocument().sections.monster;
    }
    const errors = structuralErrors(d, true);
    if (errors.length) throw new Error(errors.slice(0, 8).join("\n"));
    return d;
  }
  function monsterPreset() {
    const d = newDocument();
    set(d, "identity", "kind", "monster");
    set(d, "identity", "category", "海洋怪物原型");
    set(d, "monster", "stage", "prototype");
    set(d, "monster", "naturalSpawn", false);
    return d;
  }
  const api = { newDocument, hullPreset, monsterPreset, importDocument, emptyValue, set, validate, structuralErrors, schema, copy, states };
  root.ContentSpec = api;
  if (typeof module !== "undefined") module.exports = api;
})(typeof globalThis !== "undefined" ? globalThis : this);
