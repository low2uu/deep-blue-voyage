# 文档入口

先读[开发版本总览](development/开发版本总览-development-overview.md)，再查[文件整理记录](development/文件整理记录-repository-cleanup.md)与[逐文件职责索引](development/repository-files.json)。

当前开发应按下面的顺序阅读。游戏机制和数值设计完整保留；历史交付与初筛资料分区存放，避免把旧步骤当作当前用法。

## 当前开发

| 入口 | 负责的内容 |
| --- | --- |
| [开发版本总览](development/开发版本总览-development-overview.md) | 当前能力、代码边界、数据流、已知缺口与逐文件职责 |
| [部署、启动与更新](部署启动与版本更新-deployment-launch-update.md) | develop、构建/运行、导入地图、更新代码与常见错误 |
| [开发环境部署](开发环境部署-development-setup.md) | 新电脑安装Zulu JDK21、Git、VS Code和本地路径配置 |
| [系统开发索引](development/README.md) | 当前自研模块、改装与数据、世界生成、模型与设计器 |
| [当前Mod清单](当前Mod清单-current-mod-list.md) | 已确认、待定、备选及实际启用状态；固定依赖以Gradle为准 |
| [版本记录](版本记录-changelog.md) | 已发布与未提交改动的版本边界 |
| [服务器部署](../server/README.md) | 双人离线登录、IPv6、服务端更新与回退 |
| [世界存档交付](../pack/worlds/README.md) | 唯一当前地图快照、manifest与导入脚本 |

## 完整保留的设计资料

- [游戏机制](game-mechanics/README.md)：海域准入、唯一主艇、酒狐AI、科技路线、地图与起始岛、蓝图库。
- [数值设计](数值设计-balance-design/README.md)：待定平衡草案、当前原型、深度与材料边界。
- [玩法方向](玩法设计-design.md)：海洋远航主题及最早的设计原则。

这些设计中的计划、候选和目标数值不代表已经完成。整理没有删改这三处资料；当前实现由源码与实际验证记录确定。

## 资源与追溯

- [物品模型](art/item-models/物品模型目录-item-model-catalog.md)、[怪物模型](art/creature-models/怪物模型目录-creature-model-catalog.md)：离线预览与几何索引。
- [舱内设备规格](../content/cabin-equipment/README.md)、[物品怪物原型规格](../content/prototypes/README.md)：可导入内容设计器的JSON源数据。
- [历史归档](archive/README.md)：旧验收、世界4/5交接和第一轮Mod候选池。
- [验证证据导航](../outputs/README.md)：各批次日志、截图、JSON结果及其适用版本，区分已撤回实验参数。
- Mod来源证据保留在本目录的`*-evidence.json`；这些是静态核对材料，不是安装清单。已知排除依据见[剔除记录](Mod剔除记录-mod-exclusions.md)。

新增说明文件用中英文联合命名，README除外。不要用生成文档替代源规格；修改链接时保留机制文档的原始内容。
