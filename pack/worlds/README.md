# 世界存档交付

本目录存放可导入 `runtime/client/saves/` 的固定交付快照。ZIP 不包含 Mod 代码，请配合最新源码与配置使用。`/runtime/` 下的本机游玩进度不通过 Git 同步。

## 当前交付

### `新的世界5.zip`（2026-10-18）

含 Sable / 航空学物理化结构 **2 个**：

1. **初始艇「酒狐号·初始艇」** — bodyId `4e5690e1-40a4-4337-a80f-daf986d39430`，vesselId `1c0035ba-d85b-4363-b2f1-ce2dc82b6e99`。
2. **重新制作的潜艇物理结构** — Sable SubLevelID `66d01998-fbc7-47d8-b8ac-7097dadc716a`。约 26×12×46，大型柴油双机、螺旋桨 8、航空学万向节、DashPanels 中文驾驶面板；附设计快照 `潜艇改好版.excraft`。

导入：关闭 Minecraft，将 ZIP 内的 `新的世界5` 解压到 `runtime/client/saves/`。已有同名存档先完整备份并移出，禁止合并文件夹。

详见[世界存档推送说明](../../docs/世界存档推送-新的世界5-world5.md)。

### `新的世界4.zip`（2026-10-18）

含 Sable / 航空学物理化结构 **2 个**：

1. **初始艇「酒狐号·初始艇」**（DBV 舰队主艇）— bodyId `6c5312c3-c771-40b4-b5f2-d1925a3a2e59`，vesselId `14c53bb2-ba12-4e00-aab7-d2944db21c91`。铁壳、船载电脑、压载与双桨，由 `dbv/fleet` 档案管理。
2. **新建潜艇「酒狐号」** — Sable SubLevelID `451cbb6f-58f0-4c7f-bb97-f55ec94ea261`。用户在航空学/Sable 中另建的物理结构，含 Power Grid 电力、大型柴油机、DashPanels 驾驶台、名牌「酒狐号」等。

导入：关闭 Minecraft，将 ZIP 内的 `新的世界4` 解压到 `runtime/client/saves/`。已有同名存档先完整备份并移出，禁止合并文件夹。

详见[世界存档与汉化资源推送记录](../../docs/世界存档与汉化资源推送-world4-and-l10n.md)。本次同批推送中文资源包 `pack/overrides/resourcepacks/dbv-zh-cn`。

### `DBV-Island-v2.zip`（2026-10-15，历史快照）

原生潜艇控制与停泊版。保留原起始岛、油田和唯一主艇身份，初始艇为第 5 代灯笼鱼型，双桨停车、换向复位、已停泊，玩家位于舱内。

使用 Minecraft 1.21.1 / NeoForge 开发环境，通过 `tools/dev.ps1 run` 启动；2026-10-16 已恢复 Mod 默认压载、浮筒和升降速度配置，启动脚本不再强制覆盖这些参数。操作与验证范围见[原生潜艇控制与返航停泊](../../docs/development/原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md)。

本机 `runtime/client/saves/DBV-Island-v2` 已更新，无需再次导入。其他开发者先关闭 Minecraft，再将 ZIP 内的 `DBV-Island-v2` 文件夹解压到 `runtime/client/saves/`。已有同名存档请先完整备份并移出，禁止合并文件夹。

当前初始艇含两台柴油机、两侧水下螺旋桨、四角 4 个压载舱、16 个原生浮筒，以及有限柴油/淡水储备和制氧设备。默认目标 1 米、中速。此前海试使用自定义平衡参数，恢复默认值后不再代表当前运行表现。更深水域、负载变化、长途续航和气闸完整水下往返尚未验收。

返回船坞，断开两侧推进离合器并等待稳定，管理面板的“出航”按钮会变为“停泊”。此版本仍使用建设模式创造管理员权限，不会自动返航。

这是固定交付快照；之后的本机游玩进度不会自动同步 Git。更新前原世界在本机 `runtime/world-archives/native-upgrade-20261015/` 完整保留，测试副本和恢复归档不纳入本目录。
