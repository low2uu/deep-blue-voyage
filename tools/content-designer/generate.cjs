// Rebuild checked-in schema/example from the exact definitions used by the UI.
const fs = require("node:fs");
const path = require("node:path");
const S = require("./core.js");
const C = require("./catalog.js");
fs.writeFileSync(path.join(__dirname, "content-spec.schema.json"), JSON.stringify(S.schema(), null, 2) + "\n");
const example = S.hullPreset();
example.metadata = { createdAt: "2026-10-20T00:00:00.000Z", updatedAt: "2026-10-20T00:00:00.000Z" };
fs.writeFileSync(path.join(__dirname, "iron-submarine-hull.example.json"), JSON.stringify(example, null, 2) + "\n");
const fieldDoc = ["# 内容设计器字段目录", "", "由 `node tools/content-designer/generate.cjs` 根据UI的同一字段目录生成。字段总数随目录更新；每项均导出状态、值、来源和备注。列表内子字段同时列出。", ""];
const clean = s => s.replaceAll("|", " / ");
for (const g of C.groups) {
  fieldDoc.push(`## ${g.title}（${g.id}）`, "", g.help, "", "| 字段键 | 中文名 | 类型/选项 | 必填 |", "| --- | --- | --- | --- |");
  for (const f of g.fields) fieldDoc.push(`| ${g.id}.${f.id} | ${f.label} | ${clean(f.type + (f.options ? "：" + f.options.join("；") : ""))} | ${f.essential ? "是" : "可标待定/不适用"} |`);
  fieldDoc.push("");
  for (const f of g.fields.filter(x => x.columns)) {
    fieldDoc.push(`**${f.label}每条记录的字段：**`, "");
    for (const c of f.columns) fieldDoc.push(`- \`${c.id}\`：${c.label}（${clean(c.type + (c.options ? "；" + c.options.join("；") : ""))}）`);
    fieldDoc.push("");
  }
}
fs.writeFileSync(path.join(__dirname, "../../docs/development/内容设计器字段目录-content-designer-fields.md"), fieldDoc.join("\n").trimEnd() + "\n");
console.log("已生成Schema及完整铁质潜艇壳示例。");
