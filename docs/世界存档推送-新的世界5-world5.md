# 世界存档「新的世界5」推送记录

2026-10-18。交付世界存档 `新的世界5`：含 Sable / 航空学物理化结构 2 个，其中重新制作的潜艇已确认。ZIP 不含 Mod 代码，需配合最新源码使用。

## 1 交付内容

| 项目 | 内容 |
| --- | --- |
| 交付文件 | [pack/worlds/新的世界5.zip](https://github.com/low2uu/deep-blue-voyage/blob/v0.1.1/pack/worlds/%E6%96%B0%E7%9A%84%E4%B8%96%E7%95%8C5.zip)（约 4.2 MB） |
| 本机路径 | `runtime/client/saves/新的世界5` |
| 解压体量 | 约 28.5 MB |
| 平台 | Minecraft 1.21.1 / NeoForge 21.1.259 |
| 打包处理 | 排除 `session.lock`、`level.dat_old`；设计快照仅保留 `潜艇改好版.excraft` |
| 游戏内名称 | 新的世界5 |

## 2 Sable 物理化结构（共 2 个）

占用表 `data/sable_sub_level_occupancy.dat` = `[1, 0, 1]`。

### 2.1 物理体 A：初始艇「酒狐号·初始艇」

| 项目 | 内容 |
| --- | --- |
| Sable bodyId | `4e5690e1-40a4-4337-a80f-daf986d39430` |
| DBV vesselId | `1c0035ba-d85b-4363-b2f1-ce2dc82b6e99` |
| 登记名 | 酒狐号·初始艇（generation 1） |
| 包围盒 | 约 15×11×24，position ≈ (67.5, 137.9, 161.8) |
| 特征 | 铁壳、船载电脑、声呐、压载、浮筒 16、双桨、柴油机、舱内信息面板 |

### 2.2 物理体 B：重新制作的潜艇物理结构

| 项目 | 内容 |
| --- | --- |
| **Sable SubLevelID** | **`66d01998-fbc7-47d8-b8ac-7097dadc716a`** |
| 追踪点 | `sable_tracking_points.dat` → `c1be2294-b93b-3dd9-96ca-1e83ecf422e6` |
| 物理锁 | `simulated_physics_staff_lock_data.dat` 同步该 UUID |
| 包围盒 | 约 26×12×46，position ≈ (44.2, 127.4, 326.3) |
| 线速度 | ≈ (0, 0.30, 0)（保存时上浮中） |
| 名称 | 暂不补 display_name / 名牌 |

特征：黑曜石压载围壳、耐压玻璃；大型柴油机 2、航空学黄铜万向节、螺旋桨 8；压载水罐组与泵控制器；DashPanels 中文驾驶模块（左发单控/动力/倒车、右发单控/动力/倒车、双发双控/动力、倒车、排空上浮）；声呐、船载电脑、电解制氧、工业警报器等。

### 2.3 航空学设计快照

`enxv_aeronautics_server_structures/c1be2294-b93b-3dd9-96ca-1e83ecf422e6/潜艇改好版.excraft`（gzip NBT）。与物理体 B 同源；初版与过程稿不纳入交付包。

## 3 导入方式

1. 退出 Minecraft。
2. 将 ZIP 内 `新的世界5` 解压到 `runtime/client/saves/`。
3. 已有同名存档先完整备份并移出，禁止合并文件夹。
4. `tools/dev.ps1 build` 后 `tools/dev.ps1 run`。

`/runtime/` 不纳入 Git；ZIP 为固定交付快照。

## 4 与「新的世界4」对照

| | 新的世界4 | 新的世界5 |
| --- | --- | --- |
| 初始艇 bodyId | `6c5312c3-…` | `4e5690e1-…` |
| 新建/重制潜艇 ID | `451cbb6f-…` | `66d01998-…` |
| 重制艇尺寸 | 约 15×13×46 | 约 26×12×46 |
| 设计快照 | 无 | `潜艇改好版.excraft` |
| 动力 | Power Grid + 大型柴油 | 大型柴油 + 航空学万向节 |

## 5 相关文档

- [世界存档与汉化资源推送（世界4）](世界存档与汉化资源推送-world4-and-l10n.md)
- [世界包目录说明](../pack/worlds/README.md)

> v0.1.2 清理说明：旧 ZIP 已从当前分支移除，本文保留历史记录，以上交付链接指向 v0.1.1。当前请使用联合开发存档。
