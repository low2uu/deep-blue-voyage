# 物品与怪物原型 JSON

版本：0.1.13；Minecraft 1.21.1 / NeoForge 21.1.259。所有平衡数值仍待定。

- `items/`：54 份物品规格。
- `mobs/`：12 种普通怪和 3 个 Boss，共 15 份规格。
- 每份文件均使用内容设计器 v2 完整格式，包含全部字段、待定状态、来源、备注和扩展需求。通过“导入 JSON”逐份编辑，再导出交给开发者。
- `monster.health` 是当前游戏原型生命；`monster.plannedHealth` 是待定目标。三个 Boss 当前均为 1024；不代表目标数值已取消。
- 物品和怪物已有专属模型，怪物具有基础关节动画；AI 仍沿用原版，尚未制作专属技能、Boss 阶段、首杀奖励、自然刷新和船体攻击。物品目前用于展示、容器存储和掉落，武器、维修包和研究记录尚无功能。没有新增合成配方。

JSON 是开发规格，不是数据包，不能复制到游戏目录就自动新增内容。运行时使用打包进自研 Mod 的已支持字段。

开发者在仓库根目录执行：

```powershell
node tools/content-designer/build-prototypes.cjs
node --test tools/content-designer/test.cjs tools/content-designer/prototypes.test.cjs
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
```

生成脚本只实现明确支持的注册、属性、模型引用、翻译与掉落；修改技能文字不会自动产生对应 Java 代码。未经确认，不将这些怪物添加到自然刷新。

原料映射：基础密封件/密封件统一为通用密封件；人工替代料命名为人工导流替代料；鳗筋、感知组织、流纹材料等为来源分类，分别对应柔韧筋束/韧性纤维、感知须/完整听觉器官、流纹软骨等，尚不启用替代配方。铁板使用已有 `create:iron_sheet`，铁粒使用 `minecraft:iron_nugget`，不重复注册。附着蠕虫群以单只附着蠕虫实体预留，尚未实现群体控制。

完整清单与使用说明见仓库的 `docs/game-mechanics/物品与怪物原型清单-item-and-mob-prototypes.md`。
