const fs = require("node:fs");
const S = require("./core.js");
const file = process.argv.slice(2).find(x => !x.startsWith("--"));
if (!file) {
  console.error("用法：node tools/content-designer/validate.cjs 文件.json [--draft]");
  process.exitCode = 2;
} else {
  try {
    if (fs.statSync(file).size > 5 * 1024 * 1024) throw new Error("文件超过5 MiB，请分拆设计");
    const doc = S.importDocument(JSON.parse(fs.readFileSync(file, "utf8").replace(/^\uFEFF/, "")));
    const result = process.argv.includes("--draft")
      ? { errors: S.structuralErrors(doc, true), warnings: [] }
      : S.validate(doc);
    console.log(JSON.stringify(result, null, 2));
    process.exitCode = result.errors.length ? 1 : 0;
  } catch (error) { console.error(error.message); process.exitCode = 1; }
}
