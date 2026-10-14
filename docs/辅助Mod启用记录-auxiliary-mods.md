# 辅助Mod启用记录

2026-10-14；Minecraft 1.21.1、NeoForge 21.1.259。用户明确选择JEI、JER、Xaero世界地图和小地图、BetterF3、Jade Sable Compat。安装在同一正式开发工程，通过`tools/dev.ps1 run`启动。

| Mod | 锁定版本 | 用途 |
| --- | --- | --- |
| [Just Enough Items (JEI)](https://modrinth.com/mod/jei/version/ufHUqt9b) | 19.51.0.418 | JEI 物品/配方查询 |
| [Just Enough Resources (JER)](https://modrinth.com/mod/just-enough-resources-jer/version/TgNFki8j) | 1.6.0.17 | JEI资源与掉落信息 |
| [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap/version/irjR7dHk) | neoforge-1.21.1-26.6.0 | Xaero 小地图（实时小地图与路径点） |
| [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map/version/SnQ4SoDe) | neoforge-1.21.1-1.47.0 | Xaero 世界地图（全图浏览与标记） |
| [BetterF3](https://modrinth.com/mod/betterf3/version/maXNB1dn) | 11.0.3 | 更清晰的F3调试信息 |
| [Jade Sable Compat](https://modrinth.com/mod/jade-sable-compat/version/cxUohR7A) | 1.3.0 | Jade 信息显示集成 |
| [Cloth Config API](https://modrinth.com/mod/cloth-config/version/izKINKFg) | 15.0.140+neoforge | BetterF3必需配置界面前置 |

Cloth Config是BetterF3必需前置；XaeroLib 1.7.3由两个地图JAR内置，加载器只加载一份，不另装独立包。没有加入Mouse Tweaks、AppleSkin、Controlling或其他未选定辅助Mod。

所有下载文件核对SHA-1，JAR依赖元数据见[证据](auxiliary-mod-evidence.json)。JEI 19.51.0.418保留原版范围声明，未改JAR或依赖限制，在当前正式开发环境实际启动、进世界和GUI初始化通过。此前“仅凭范围声明暂缓”的结论由本次运行证据更新；普通发布包和其他加载器构建仍须各自验证。

验证使用正式客户端与`DBV-Auxiliary-Acceptance`存档副本，六项Mod、Cloth Config及内置XaeroLib均出现在加载列表；JER分类和配方、Jade Sable插件完成注册。客户端进入世界并正常退出。地图在线版本/赞助信息检查超时、交错电网既有本机加速库错误不阻断本轮加载。未验证每种配方、移动船体HUD准确性、地图长航程和多人同步。

临时自动进世界脚本在验收后移走，测试副本归档，正式起始岛未改动。运行日志节选见[验收记录](../outputs/auxiliary-mods-20261014/startup.log)。
