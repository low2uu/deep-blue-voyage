# 世界存档「新的世界4」与汉化资源推送记录

2026-10-18。本次推送包含两部分内容：交付世界存档 `新的世界4`（含航空学物理化结构与新建潜艇「酒狐号」），以及配套中文语言资源。世界 ZIP 与资源包均不包含 Mod 代码，需配合最新源码使用。

## 1 世界存档「新的世界4」

| 项目 | 内容 |
| --- | --- |
| 交付文件 | [pack/worlds/新的世界4.zip](../pack/worlds/新的世界4.zip) |
| 本机路径 | `runtime/client/saves/新的世界4` |
| 平台 | Minecraft 1.21.1 / NeoForge 21.1.259 |
| 体量 | 压缩后约 4.1 MB；解压后约 24.7 MB、101 个文件 |
| 打包处理 | 已排除 `session.lock`、`level.dat_old`；其余与本机存档一致 |

### 1.1 航空学物理化结构

存档内含 Create Aeronautics / Sable 物理化数据：

- `sublevels/`：Sable 子层级与物理区域文件（`*.slvls` / `*.slvlr` / `*.r`）
- `data/aeronautics_unloaded_balloons.dat`：未加载气球状态
- `data/simulated_physics_staff_lock_data.dat`：模拟物理锁定数据
- `dbv/fleet/`：本项目舰队登记、舰艇独立 JSON 档案与自动备份

### 1.2 新建潜艇「酒狐号」

存档内唯一登记主艇，名称 **酒狐号·初始艇**。

| 标识 | 值 | 说明 |
| --- | --- | --- |
| 用户登记序列 ID | `451cbb6f-58f0-4c7f-bb97-f55ec94ea261` | 用户提供的序列 ID；已在存档明文与解压区块中检索，未写入当前存档字节，作外部登记参考保留 |
| vesselId | `14c53bb2-ba12-4e00-aab7-d2944db21c91` | `dbv/fleet/registry.json` 与 `state.json` 中的舰艇档案 ID |
| bodyId | `6c5312c3-c771-40b4-b5f2-d1925a3a2e59` | 物理载体（Sable）身份 |
| generation | `1` | 当前恢复代次 |

结构摘要（来自 `state.json` 的 `modules.structure`）：

- 方块数 883，外包络约 15×11×24
- 船体主体为铁块，铁制耐压玻璃舷窗 39
- 动力与推进：柴油机 2、螺旋桨 2、传动轴/齿轮/离合/齿轮箱、机械泵 5
- 浮力与压载：浮筒 16、压载水罐 4、压载排口 4、泵控制器 4
- 舱室与设备：减压舱 2、声呐 1、船载电脑相关与储液罐/流体管道若干
- 自动备份：`2ea01d3b-1426-4f7a-8e83-ed19cb8aa808` 等 JSON 快照，指纹已写入 `state.json`

### 1.3 导入方式

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

`pack_format` 34。启用方式：将 `dbv-zh-cn` 解压或放入 `resourcepacks/`，在资源包界面启用，语言选择“简体中文”。

### 2.2 模组内语言资源

路径：`mods/dbv-world/src/main/resources/assets/`

- `dbv_world`：本模组界面文案（含存储终端、潜艇管理、初始艇驾驶指南）
- `create_submarine` 及其他命名空间：与资源包对应的中英文条目，便于联调时直接生效

本次同步：

- `dbv_world` 增加菜单与界面键的中英文对照
- `create_submarine/zh_cn.json` 大幅补全设备、Ponder、配置项与安全提示；三处操作提示统一为“退出键 / 潜行键”，不写死默认键名
- 资源包与模组内 `create_submarine` 中文措辞已对齐

## 3 验证与边界

- 世界 ZIP 可解压，目录名与游戏内世界名均为 `新的世界4`。
- 舰艇 `state.json` / `registry.json` 可解析，名称与方块统计已写入本文。
- 汉化 JSON 均为合法 UTF-8 JSON。
- 尚未在新电脑完整走一遍“解压 ZIP → 启用资源包 → 进入世界并加载酒狐号”的验收；更深深海、气闸往返、多人共享主艇也不在本次范围。

## 4 相关文档

- [主潜艇与酒狐实现方案](game-mechanics/主潜艇与酒狐实现方案-flagship-submarine-plan.md)
- [原生潜艇控制与返航停泊](development/原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md)
- [部署、启动地图与版本更新](部署启动与版本更新-deployment-launch-update.md)
- [世界包目录说明](../pack/worlds/README.md)
