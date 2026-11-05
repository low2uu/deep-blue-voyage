# 深蓝远航 · Deep Blue Voyage

Minecraft **1.21.1 / NeoForge 21.1.259 / Java 21** 的潜艇探索整合包，自研 Mod 为 `dbv-world`。

当前开发版本 **0.1.16**，对应`develop`分支和`v0.1.16`标签（2026-11-05）。地图交付版本单独管理，`pack/worlds` 当前为 **0.1.4**，不代表Mod代码版本。查看[版本记录](docs/版本记录-changelog.md)。

## 从这里开始

- **理解当前开发版本**：[开发版本总览](docs/development/开发版本总览-development-overview.md)。说明系统边界、数据存放和已实现/未实现功能。
- **运行与更新**：[部署启动与版本更新](docs/部署启动与版本更新-deployment-launch-update.md)；全新电脑先看[开发环境部署](docs/开发环境部署-development-setup.md)。
- **双人联机**：[服务器部署](server/README.md)，目前采用用户选择的离线登录和IPv6。
- **玩法设计**：[游戏机制](docs/game-mechanics/README.md)；所有机制及数值设计完整保留，方案不等于代码已实现。
- **Mod选择**：[当前清单](docs/当前Mod清单-current-mod-list.md)。实际加载依赖以`mods/dbv-world/build.gradle`和`pack/local-mods/manifest.json`为准。
- **全部文档**：[文档索引](docs/README.md)；本次整理见[文件整理记录](docs/development/文件整理记录-repository-cleanup.md)。

## 开发入口

用VS Code打开本目录，或打开`deep-blue-voyage.code-workspace`。配置Zulu JDK 21的`JAVA_HOME`，从项目根目录执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 run
```

`build`构建代码，`run`启动正式开发客户端，`debug`启动可附加调试的同一工程。已有世界直接从单人游戏进入；首次导入交付地图使用`tools/restore-world.ps1`，不要覆盖自己的游玩进度。

`config/development-session.json`当前开启建设模式，限制渲染/模拟距离及帧率；这是开发设置。当前主艇改装、出航、停泊主要面向建设模式创造管理员，尚不是完整生存玩法。

## 目录职责

| 路径 | 内容 |
| --- | --- |
| `mods/dbv-world/` | Java代码、游戏资源、44项当前测试、Gradle构建 |
| `content/` | 物品、怪物、舱内设备的设计JSON来源 |
| `tools/` | 开发/地图/生成器/设计器的可维护源码 |
| `tool/content-designer/` | 设计器便携发行；修改应回到tools源码并重打包 |
| `pack/` | 配置、语言资源包、交付地图及两项本地Mod原件 |
| `server/` | 服务端部署、更新、备份与客户端分包脚本 |
| `docs/` | 当前说明、完整机制与数值设计、模型预览；历史说明在archive |
| `outputs/` | 历史测试证据，部分被机制文档引用，保留用于追溯 |
| `assets/` | 原始素材和用户艇来源，不作为当前运行目录 |
| `runtime/client/`、`runtime/server/` | 本机游戏、活动存档与服务器状态，不进Git |
| `runtime/backups/`、`runtime/server-backups/` | 本机回退资料，不随代码更新覆盖 |
| `.local/`、`dist/` | 本机构建缓存、检查结果和可重新生成的发行包，不进Git |

项目默认中文；新贴图默认16×16。自研业务数据以船体ID组织，但目前仍有改装事务/泊位NBT，详见总览。旧恢复代码承担兼容职责，不能仅因名字含Legacy就删除。
