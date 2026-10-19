# 世界存档「新的世界4」与汉化资源推送记录

2026-10-18。本次推送包含两部分内容：交付世界存档 `新的世界4`（含 Sable / 航空学物理化结构），以及配套中文语言资源。世界 ZIP 与资源包均不包含 Mod 代码，需配合最新源码使用。

## 1 世界存档「新的世界4」

| 项目 | 内容 |
| --- | --- |
| 交付文件 | [pack/worlds/新的世界4.zip](https://github.com/low2uu/deep-blue-voyage/blob/v0.1.1/pack/worlds/%E6%96%B0%E7%9A%84%E4%B8%96%E7%95%8C4.zip) |
| 本机路径 | `runtime/client/saves/新的世界4` |
| 平台 | Minecraft 1.21.1 / NeoForge 21.1.259 |
| 体量 | 压缩后约 4.1 MB；解压后约 24.7 MB |
| 打包处理 | 已排除 `session.lock`、`level.dat_old`；其余与本机存档一致 |

### 1.1 Sable 物理化结构（共 2 个）

物理体数据在 `sublevels/`（`*.slvls` / `*.slvlr` / `*.r`），占用表 `data/sable_sub_level_occupancy.dat` 为 `[1, 0, 1]`，即两个在用槽位。另有 `data/sable_tracking_points.dat`、`data/aeronautics_unloaded_balloons.dat`、`data/simulated_physics_staff_lock_data.dat`。

两个物理体均已在 `sublevels/r.0.0.0.slvls` 的 gzip 子层级流中解析到完整 NBT（`plot` / `pose` / `world_bounds` / `uuid`）。

#### 物理体 A：初始艇「酒狐号·初始艇」（DBV 舰队主艇）

| 项目 | 内容 |
| --- | --- |
| Sable UUID（bodyId） | `6c5312c3-c771-40b4-b5f2-d1925a3a2e59` |
| DBV vesselId | `14c53bb2-ba12-4e00-aab7-d2944db21c91` |
| 登记名 | 酒狐号·初始艇（`dbv/fleet/registry.json`） |
| user_data | `dbv_world:flagship`，generation 1 |
| 世界包围盒 | 约 (60.1, 78.0, 148.8) – (75.1, 89.0, 172.8) |
| 位姿 | position ≈ (67.5, 82.9, 160.6)，接近正交 |
| 结构特征 | 铁块耐压壳、船载电脑、声呐、压载 4 罐、浮筒、双桨、柴油机、舱内信息面板 |

这是集成包自带的 DBV 初始潜艇，由 `dbv/fleet` 独立 JSON 档案管理，也是当前舰队 `flagship`。

#### 物理体 B：新建潜艇「酒狐号」（航空学 / Sable 物理结构）

| 项目 | 内容 |
| --- | --- |
| Sable SubLevelID | `451cbb6f-58f0-4c7f-bb97-f55ec94ea261` |
| display_name | 酒狐号 |
| 追踪点 | `data/sable_tracking_points.dat` 键 `c1be2294-b93b-3dd9-96ca-1e83ecf422e6` 的 `SubLevelID` 即为该 UUID（IntArray 四段） |
| 世界包围盒 | 约 (-4.5, 111.5, 186.9) – (11.0, 124.1, 233.1) |
| 位姿 | position ≈ (3.3, 116.4, 210.2)，带姿态四元数 |
| 线速度 | linear_velocity ≈ (0, 0.165, 0.0016)（保存时仍在运动） |
| 名牌 | 3× `simulated:white_nameplate`，文本「酒狐号」 |

结构特征（独立于 DBV 初始艇，用户在航空学 / Sable 中另建）：

- 耐压壳与压载：`create_submarine` 耐压玻璃、压载水罐/排口、泵控制器、减压舱
- 动力：大型柴油机、Power Grid 发电机/励磁/整流/电池/碳堆/变压器、恒速电机、电力泵
- 推进：螺旋桨、喷水推进器、舵系与传动
- 驾驶：DashPanels 控制面板与摇杆/开关模块、座椅、显示链、电压表
- 辅助：声呐、船载电脑、工业警报器、电解制氧、氧气扩散器、模拟传感器与帆装

> 标识对照：用户提供的序列 ID `451cbb6f-58f0-4c7f-bb97-f55ec94ea261` **就是**该新建潜艇的 Sable SubLevelID（存于 `sable_tracking_points.dat` 与子层级 NBT 的 `uuid` 字段，编码为四个大端 int）。此前仅检索 `dbv/fleet` 时未覆盖到 Sable 存储，本次已从 `sublevels` 解压确认。

### 1.2 导入方式

1. 退出 Minecraft。
2. 将 ZIP 内的 `新的世界4` 文件夹解压到 `runtime/client/saves/`。
3. 若已有同名存档，先完整备份并移出，**禁止合并文件夹**。
4. 使用最新源码执行 `tools/dev.ps1 build` 与 `tools/dev.ps1 run`，在单人列表中进入该世界。

本机 `runtime/client/saves/新的世界4` 为当前工作存档，不必再次导入。`/runtime/` 不纳入 Git；ZIP 是固定交付快照，之后本机游玩进度不会自动同步。

## 2 汉化资源

中文与英文分开维护：`zh_cn.json` 只放中文，`en_us.json` 只放英文；默认中文由客户端语言配置实现。

### 2.1 资源包 `dbv-zh-cn`

路径：[pack/overrides/resourcepacks/dbv-zh-cn/](../pack/overrides/resourcepacks/dbv-zh-cn/)

| 命名空间 | 说明 |
| --- | --- |
| `create` | 机械动力本体 |
| `create_submarine` | 深海（Deep Seas） |
| `create_abyss` / `create_high_seas` | 渊海与高海扩展 |
| `powergrid` | 电网 |
| `sable` | Sable |
| `dashpanels` | DashPanels 驾驶舱面板 |
| `jei` / `cloth-config2` / `probejs` | 合成表、配置界面、ProbeJS |
| `xaerominimap` / `xaeroworldmap` / `xaerobetterpvp` | Xaero 系列 |

`pack_format` 34。启用方式：将 `dbv-zh-cn` 放入 `resourcepacks/`，在资源包界面启用，语言选择“简体中文”。

### 2.2 模组内语言资源

路径：`mods/dbv-world/src/main/resources/assets/`

- `dbv_world`：本模组界面文案（存储终端、潜艇管理、初始艇驾驶指南）
- `create_submarine` 及其他命名空间：与资源包对应的中英文条目，便于联调时直接生效

本次同步：`dbv_world` 增加菜单与界面键；`create_submarine/zh_cn.json` 大幅补全；操作提示统一为“退出键 / 潜行键”；资源包与模组内中文措辞已对齐。

## 3 验证与边界

- 世界 ZIP 可解压，目录名为 `新的世界4`。
- 两个 Sable 物理体 NBT 均可解析；酒狐号 SubLevelID 与用户提供序列 ID 一致。
- 汉化 JSON 均为合法 UTF-8 JSON。
- 尚未在新电脑完整走一遍“解压 ZIP → 启用资源包 → 确认两个物理体加载”的验收。

## 4 相关文档

- [主潜艇与酒狐实现方案](game-mechanics/主潜艇与酒狐实现方案-flagship-submarine-plan.md)
- [原生潜艇控制与返航停泊](development/原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md)
- [部署、启动地图与版本更新](部署启动与版本更新-deployment-launch-update.md)
- [世界包目录说明](../pack/worlds/README.md)

> v0.1.2 清理说明：旧 ZIP 已从当前分支移除，本文保留历史记录，以上交付链接指向 v0.1.1。当前请使用联合开发存档。
