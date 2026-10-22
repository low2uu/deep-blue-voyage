# 相关资料索引

本目录收纳数值正文与汇总；以下跨主题资料保留原路径。使用时先看状态和日期，历史数字不自动覆盖当前规则。

| 领域 | 来源 | 状态与用途 |
| --- | --- | --- |
| 正式目标 | [1.0机制](../game-mechanics/1.0游戏机制说明-v1.0-confirmed-mechanics.md) | 确认范围入口；确认不等于实现 |
| 地图密度与航程 | [公开版地图布局](../game-mechanics/首个公开版地图布局-public-release-map-layout.md) | 区域距离带2200～4400格、尺度800～1200格、间距及24座支线岛是提案 |
| 旧科技路线 | [初版地图与科技路线](../game-mechanics/初版地图与科技路线-first-playable-design.md) | 七海域与0～480格等旧设定，作为扩展素材 |
| 准入机制 | [海域准入与潜艇限制](../game-mechanics/海域准入与潜艇限制-ocean-access-gates.md) | 速度、耐压、体积的规则与待定阈值 |
| 原型实现 | [物品与怪物清单](../game-mechanics/物品与怪物原型清单-item-and-mob-prototypes.md) | 已实现范围、历史验证及原型能力限制 |
| 设计规格 | [JSON目录](../../content/prototypes/README.md) | 54物品、15怪物完整可导入规格；不另复制一套可编辑源 |
| 原型数值来源 | [运行目录数据](../../mods/dbv-world/src/main/resources/dbv_prototypes.json) | 属性与堆叠，由规格生成 |
| 怪物掉落实现 | [掉落表目录](../../mods/dbv-world/src/main/resources/data/dbv_world/loot_table/entities) | 游戏实际读取的掉落定义 |
| 材料模板 | [方块属性规范](../development/方块属性规范与对照表-block-property-specification.md) | 当前值、目标、第三方适配与字段区别 |
| 设计工具 | [字段目录](../development/内容设计器字段目录-content-designer-fields.md) | 属性含义、状态、导入格式 |
| 原生物理与旧调参 | [原生控制](../development/原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md) | 以顶部恢复默认说明为准，旧海试不作现版结论 |
| 航速验收历史 | [初始艇推进](../development/初始潜艇航速与推进设备-cruise-speed-and-propulsion.md) | 船型与工况相关，不替代4格/秒目标 |
| 工程限额 | [扫描上限](../development/潜艇扫描上限-vessel-scan-limits.md) | 扫描、事务内存与容器上限 |
| 世界生成现状 | [自动矿种与无限探索](../development/自动矿种与无限探索-v0.1.4.md) | 已取消探索边界；主线范围不是世界边界 |
| 工厂与机器空间 | [工厂方案](../game-mechanics/起始岛工厂与蓝图方案-starter-factory-blueprint-plan.md)、[蓝图库](../game-mechanics/机器蓝图库-machine-blueprint-catalog.md) | 建筑净空、机器尺寸与适配状态，非战斗平衡值 |
| 起始资源 | [无限油田](../game-mechanics/起始岛无限油田-starter-oilfield.md) | 32×32原油区与使用条件；不能据此推导真实续航 |

后续补全重点：加工耗时/能耗、升级总材料账单、燃油与供氧消耗、命中率与攻击频率、自然刷新密度、任务收益、维修和恢复成本。现有资料没有确定值的部分继续标记待定，不补造数字。
