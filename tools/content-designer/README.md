# 内容设计器

双击 `index.html`，或在项目根目录运行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\content-designer.ps1
```

无需安装依赖、联网或启动Minecraft。目录中的HTML、CSS和JavaScript文件需要保留在一起。

先载入铁质潜艇壳预设或新建设计，把要填写的字段状态选成“明确指定”，填写属性、功能和验收要求。点击“检查填写内容”，最后导出开发规格JSON；未完成时导出草稿。浏览器下载到哪里由浏览器设置决定，工具不会写入Mod或存档。

- [详细设计与用法](../../docs/development/内容设计器设计与使用-content-designer.md)
- [全部字段](../../docs/development/内容设计器字段目录-content-designer-fields.md)
- [完整示例](iron-submarine-hull.example.json)
- [JSON Schema](content-spec.schema.json)

开发者可执行：

```powershell
node tools/content-designer/test.cjs
node tools/content-designer/generate.cjs
node tools/content-designer/validate.cjs tools/content-designer/iron-submarine-hull.example.json
```

`validate.cjs <文件> --draft` 仅检查可恢复的草稿结构；不加该参数会检查交接必填项、范围与冲突。Schema是结构/单项约束校验，跨属性语义仍由`core.js`校验。

新增“新建怪物 / Boss”入口；支持 v1 JSON 导入升级及 v2 全字段往返。69份原型规格位于 `content/prototypes/`，编辑后通过 `node tools/content-designer/build-prototypes.cjs` 生成已支持字段，再构建 Mod。技能描述仍需编写代码。
