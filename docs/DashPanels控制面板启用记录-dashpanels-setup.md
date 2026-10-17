# DashPanels 控制面板启用记录

2026-10-17，用户明确同意加入本体；已先同步远端 develop 的 ad2ce65。DashPanels: Extra 未加入。

- 版本：2.1+neoforge1.21.1，Modrinth版本ID `aH1Oe6Kk`，模组ID `dashpanels`。
- 来源：https://modrinth.com/mod/dashpanels/version/aH1Oe6Kk 。官方发布元数据见 [证据](dashpanels-mod-evidence.json)。
- SHA-1：`2d8b1f07b0ef3f24610324ec3e594edf356b8f36`。
- JAR声明：Minecraft 1.21.1、NeoForge ≥21.1.235；Create可选联动 ≥6.0.10，ComputerCraft可选联动 ≥1.119.0。现有NeoForge 21.1.259和Create 6.0.10满足范围，不额外安装ComputerCraft。
- 客户端和服务端均需安装。正式开发环境通过 Gradle 的 localRuntime 自动加载，无需另复制同一JAR到mods文件夹，以免重复。

用途为驾驶舱实体红石操纵面板，连接离合器、换向器、照明等；不替代中央控制器、主艇身份、改装及独立JSON备份。船体遥测和停泊按钮接入面板尚未开发。

验证：build（含现有单元测试）、prepareClientRun通过；实际解析runtimeClasspath，确认只有一个DashPanels JAR并匹配官方SHA-1。尚未启动游戏验证面板交互、Sable移动船体、改装前后接线和模块配置恢复，因此不宣称这些场景已通过验收。岛屿及潜艇存档未改动。

使用：更新develop后按现有流程运行 `tools/dev.ps1 run`，从创造物品栏或JEI搜索Dashpanels。模块安装方式以当前版本物品提示及作者说明为准。
