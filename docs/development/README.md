# 系统开发文档

先读[开发版本总览](开发版本总览-development-overview.md)，再查[文件整理记录](文件整理记录-repository-cleanup.md)与[逐文件职责索引](repository-files.json)。

这里解释实现与数据边界。先按当前模块查阅；版本性实验报告用于追溯，不覆盖更新的源码和约定。

## 主潜艇与交互

| 文档 | 内容 |
| --- | --- |
| [主潜艇系统](主潜艇系统开发文档-flagship-system-development.md) | 模块、身份登记、资格与观测、数据契约 |
| [初始艇与改装](初始潜艇与改装流程-starter-submarine-refit.md) | 物理艇和世界方块转换、完成检测、备份回滚 |
| [独立数据与舱内面板](主艇独立数据与舱内面板-vessel-data-and-cabin-panel.md) | 主艇JSON档案、结构与实际容器存储 |
| [主艇完整恢复与JSON档案](主艇完整恢复与JSON档案-whole-vessel-recovery-and-json.md) | 整艇恢复点、损伤冻结、船坞一键恢复、JSON迁移与断点处理 |
| [存储终端](存储终端与定深修复-storage-terminal-and-depth-hold.md) | 可交互存储及曾经的定深修复；参数变更看后续原生控制记录 |
| [原生控制与停泊](原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md) | 原生算法、出航停泊及历次参数恢复 |
| [控制器深度](控制器深度与升降控制-controller-depth-and-ballast.md) | 物理艇坐标与深度依据 |
| [扫描范围](潜艇扫描上限-vessel-scan-limits.md) | 扫描配置与上限 |
| [舱内设备与舱门](舱内设备与模块化舱门-cabin-equipment-and-modular-hatches.md) | 自研模型、可拼装门、锁定气密、仅提示检查 |

## 内容制作

| 文档 | 内容 |
| --- | --- |
| [设计器使用](内容设计器设计与使用-content-designer.md) | 本地UI、JSON规格、导入导出、校验 |
| [设计器字段目录](内容设计器字段目录-content-designer-fields.md) | 规格字段及状态语义 |
| [方块属性对照](方块属性规范与对照表-block-property-specification.md) | 采集、防护、物理、耐压分层属性 |
| [铁质潜艇壳初版](铁质潜艇壳-iron-submarine-hull.md) | 0.1.5原始实现记录；后续属性以上一项为准 |
| [远景与动态光源](远景与动态光源接入-lod-and-dynamic-lights.md) | 默认Voxy/Roxy、暂停动态光源、本地LOD原件和验证边界 |

## 世界生成与阶段记录

最新生成规则见[自动矿种与无限探索v0.1.4](自动矿种与无限探索-v0.1.4.md)，配套地图以`pack/worlds/world-manifest.json`为准。下列阶段记录保留其当时的数值和结论：

- [淹没生成器](淹没世界生成与开发边界-flooded-world-generation.md)、[高度迁移](世界高度与旧岛迁移-world-height-migration.md)、[近海生成](起始岛近海生成与验证-near-island-generation.md)、[船坞缓坡](船坞海床坡度-dock-seabed-slope.md)。
- [随机主线与支线岛v0.1.2](随机主线与矿脉支线岛-v0.1.2.md)、[淹没村庄修复v0.1.3](淹没村庄生成修复-v0.1.3.md)。
- [旧灯笼鱼艇](灯笼鱼型潜艇与操作说明-angler-submarine-and-operation.md)、[航速与推进核对](初始潜艇航速与推进设备-cruise-speed-and-propulsion.md)。当前初始艇来自用户设计，不应用旧艇参数覆盖。
- [历史交付和验收](../archive/README.md)：世界5设计接入与联合地图合并记录等。

完整玩法设想在[游戏机制](../game-mechanics/README.md)，平衡草案在[数值设计](../数值设计-balance-design/README.md)。本目录不是第二套玩法规则来源。
