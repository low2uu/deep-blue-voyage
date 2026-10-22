const { test } = require("node:test");
const assert = require("node:assert/strict");
const C = require("./catalog.js");
const S = require("./core.js");

test("v1导入迁移无损保留旧字段，v2往返一致，拒绝未知字段", () => {
  const old = S.hullPreset(); delete old.sections.monster; old.schemaVersion = 1;
  const before = structuredClone(old.sections);
  const upgraded = S.importDocument(old);
  assert.equal(upgraded.schemaVersion, 2);
  for (const key of Object.keys(before)) assert.deepEqual(upgraded.sections[key], before[key]);
  assert.deepEqual(S.importDocument(upgraded), upgraded);
  old.sections.intruder = {value: 1};
  assert.throws(() => S.importDocument(old), /未知字段/);
});
test("怪物生命、颜色和掉落范围拒绝非法输入", () => {
  const d = S.hullPreset(); S.set(d, "identity", "kind", "monster");
  S.set(d, "monster", "health", 2000);
  assert.ok(S.validate(d).errors.some(e => e.includes("生命")));
  S.set(d, "monster", "health", 40); S.set(d, "monster", "eggPrimary", "not-a-color");
  assert.ok(S.validate(d).errors.some(e => e.includes("颜色")));
  S.set(d, "monster", "eggPrimary", "123abc");
  S.set(d, "monster", "drops", [{item:"dbv_world:shell_fragment",min:3,max:1,chance:1,conditions:"死亡"}]);
  assert.ok(S.validate(d).errors.some(e => e.includes("最小值")));
});

test("预设完整往返，显式保留每个字段的值、状态、来源和备注", () => {
  const d = S.hullPreset();
  assert.deepEqual(S.validate(d).errors, []);
  assert.ok(S.validate(d).pending > 0);
  const roundtrip = JSON.parse(JSON.stringify(d));
  assert.deepEqual(roundtrip, d);
  for (const g of C.groups) assert.deepEqual(Object.keys(d.sections[g.id]), g.fields.map(f => f.id));
  assert.equal(d.sections.harvesting.hardness.value, 5);
  assert.equal(d.sections.harvesting.minimumTier.value, "stone");
  assert.equal(d.sections.physics.mass.value, 2);
  assert.equal(d.sections.pressure.maxWaterDepth.value, 500);
});
test("未完成的数值能作为草稿重新导入，但不能交付", () => {
  const d = S.hullPreset();
  S.set(d, "physics", "mass", null);
  assert.deepEqual(S.structuralErrors(d), []);
  assert.ok(S.validate(d).errors.some(x => x.includes("单块质量")));
  d.sections.physics.mass.status = "inherit";
  assert.deepEqual(S.structuralErrors(d), []);
  assert.ok(S.validate(d).errors.some(x => x.includes("参考来源")));
});
test("草稿中未修正的范围/ID错误仍能恢复，正式交接会阻止", () => {
  const d = S.hullPreset();
  S.set(d, "identity", "registryId", "");
  S.set(d, "physics", "mass", -3);
  assert.deepEqual(S.structuralErrors(d, true), []);
  assert.ok(S.validate(d).errors.length >= 2);
});
test("缺字段、错误版本与未知字段被拒绝，不能静默丢失扩展", () => {
  const d = S.hullPreset();
  d.schemaVersion = 999;
  delete d.sections.physics.mass;
  d.sections.pressure.unknown = "keep me";
  const errors = S.structuralErrors(d);
  assert.ok(errors.some(x => x.includes("版本")));
  assert.ok(errors.some(x => x.includes("缺少 mass")));
  assert.ok(errors.some(x => x.includes("未知字段")));
});
test("类型、数值范围、继承来源、ID冲突均被识别", () => {
  const d = S.hullPreset();
  S.set(d, "appearance", "lightLevel", 16);
  S.set(d, "pressure", "implosionChance", -0.1);
  d.sections.physics.mass.status = "inherit";
  d.sections.physics.mass.reference = "";
  assert.ok(S.validate(d).errors.length >= 2);
  S.set(d, "appearance", "lightLevel", 0);
  S.set(d, "pressure", "implosionChance", 0.06);
  assert.ok(S.validate(d).errors.some(x => x.includes("参考来源")));
  const d2 = S.hullPreset(); S.set(d2, "identity", "registryId", "other:hull");
  assert.ok(S.validate(d2).errors.some(x => x.includes("命名空间")));
});
test("耐久与堆叠、形状与模型的冲突阻止交付", () => {
  const d = S.hullPreset();
  S.set(d, "item", "durability", 100);
  S.set(d, "appearance", "model", "slab");
  assert.ok(S.validate(d).errors.some(x => x.includes("堆叠")));
  assert.ok(S.validate(d).errors.some(x => x.includes("立方体")));
});
test("重复配方和无效掉落区间阻止交付", () => {
  const d = S.hullPreset();
  const f = C.groups.find(x => x.id === "acquisition").fields.find(x => x.id === "recipes");
  const row = Object.fromEntries(f.columns.map(x => [x.id, S.emptyValue(x)]));
  row.id = "dbv_world:test"; row.count = 1;
  S.set(d, "acquisition", "recipes", [row, { ...row }]);
  S.set(d, "harvesting", "drops", [{ item: "minecraft:iron_ingot", min: 3, max: 1, chance: 1, conditions: "" }]);
  const errors = S.validate(d).errors;
  assert.ok(errors.some(x => x.includes("重复")));
  assert.ok(errors.some(x => x.includes("最小值")));
});
test("附加需求不丢失；待定字段不能携带矛盾的具体值", () => {
  const d = S.hullPreset();
  d.extensions.push({ key: "额外功能", description: "<script>不会执行</script>", acceptance: "纯文本保留" });
  assert.deepEqual(S.validate(d).errors, []);
  d.sections.physics.mass.status = "pending";
  assert.ok(S.structuralErrors(d).some(x => x.includes("值必须为空")));
});
test("JSON Schema覆盖所有字段并拒绝额外字段", () => {
  const schema = S.schema();
  for (const g of C.groups) {
    const section = schema.properties.sections.properties[g.id];
    assert.deepEqual(section.required, g.fields.map(x => x.id));
    assert.equal(section.additionalProperties, false);
  }
});
