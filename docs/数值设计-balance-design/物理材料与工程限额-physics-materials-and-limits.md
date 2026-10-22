# 物理材料与工程限额

核对基线：v0.1.13，2026-10-22。这里区分分发配置、材料目标与工程保护上限，不调整游戏参数。

## Deep Seas 分发参数

来源：[分发配置](../../pack/overrides/config/create_submarine-server.toml)。这是仓库默认配置，已有存档或服务器最终配置可能不同；本次未启动游戏核对运行态。

| 参数 | 分发值 | 解释 |
| --- | ---: | --- |
| ballastLiftPerTank | 75.0 | 单压载舱驱动的质量额度，非额外自研升力 |
| floaterLift | 25.0 | 单浮筒额度 |
| ballastForceMultiplier | 1.0 | 压载力倍率 |
| ballastVerticalSpeed | 2.0 | 格/tick，上限参数不是承诺实际升降速度 |
| ballastTransferRateMultiplier | 2.0 | 压载水转移速率倍率 |
| waterThrusterPowerMultiplier | 6.0 | 水下推进器倍率 |
| submarinePropellerPowerMultiplier | 3.0 | 潜艇螺旋桨倍率 |
| disableImplosion | false | 未禁用压力压溃 |
| implosionDepth | 120 | 船壳失效后的压溃行为深度参数，不是所有材料耐深 |
| globalMaxDepthCap | 400 | 上游材料推算相关参数；不能仅凭注释把显式材料都裁到400 |
| maxDepthMultiplier / implosionChanceMultiplier | 1.0 / 1.0 | 材料耐深和压溃参数倍率 |
| oxygenMaxFillBlocks | 500000 | 上游制氧填充扫描预算，独立于主艇扫描 |

原生控制的恢复与历史调参区别见[原生控制记录](../development/原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md)。保留原Mod算法；不以本表重新引入已撤销调参。

## 铁质潜艇壳与后续材料

当前[HullBlocks.java](../../mods/dbv-world/src/main/java/io/github/low2uu/dbv/world/HullBlocks.java)仍使用黑曜石属性复制；“采集像铁块”是目标，未在本次整理中实施。

| 属性 | 当前实现/证据 | 目标或边界 |
| --- | --- | --- |
| 硬度 | 50，复制黑曜石 | 5，参考铁块，待实现 |
| 工具等级 | 钻石级标签 | 石镐及以上，待实现 |
| 爆炸抗性 | 1200 | 保持1200 |
| 活塞反应 | BLOCK | 不代表物理船不能移动 |
| Sable质量 / 体积 | 未显式照搬第三方属性 | 参考黑曜石2.0 / 1.0；最终值须核对 |
| 摩擦 / 回弹 / 易碎 | 第三方属性待适配核对 | 目标1.0 / 0.0 / false |
| Deep Seas材料耐深 / 压溃参数 | 未显式适配 | 目标参考当前黑曜石500 / 0.06；不是每秒6% |

完整属性、证据与新增方块模板保留在[方块属性规范](../development/方块属性规范与对照表-block-property-specification.md)，其原路径供设计器打包使用。不要把草案140/220/300格的整艇升级目标与黑曜石材料500格参数视作同一个字段。

## 工程限制（不是成长上限）

依据：[扫描上限](../development/潜艇扫描上限-vessel-scan-limits.md)。

| 项目 | 当前记录 | 含义 |
| --- | ---: | --- |
| 主艇扫描包围盒体积 | 1048576格 | 长×高×宽，包含空气；128×64×128恰达上限 |
| 改装事务NBT读取核算 | 512 MiB | 读取器预算，不是文件大小或全进程内存 |
| 单容器槽位上限 | 4096 | 独立于船体空间扫描 |
| 旧版活动改装限制 | 4096格 | 旧事务完成前保留，不代表新流程限额 |

百万格安全运行尚未验证，船坞、区块加载、上游密闭和物理限制仍有效。业务JSON档案与既有NBT改装事务不是同一层数据。
