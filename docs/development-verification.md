# 正式开发工程检查记录

日期：2026-10-14。使用 Zulu Java 21.0.3、Minecraft 1.21.1、NeoForge 21.1.259，通过 `tools/dev.ps1` 操作同一正式工程和 `runtime/client`。

## 实际建档验证更新

2026-10-14 已通过本机一次性 KubeJS 脚本调用 Minecraft 建档接口，创建并实际进入 `runtime/client/saves/DBV-Construction`，显示名 Deep Blue Voyage Construction。游戏渲染截图已保存到 `outputs/island-acceptance-20261014/spawn.png`。这是实际集成服务端和客户端运行，不是仅计算地形函数；窗口操作工具仍无法列出 Java 游戏窗口，因此没有手动键鼠游玩。

| 实际检查 | 结果 |
| --- | --- |
| 世界生成器 | StarterIslandGenerator 实际加载，生成存档及区块文件 |
| 世界出生点 | (-80, 69, -40)；玩家初次出生受原版 spawnRadius=10 影响，会在附近落点 |
| 出生平台 | (-80, 68, -40) 为 grass_block |
| 船坞海床 | (64, 12, 128) 为 bedrock；上方 Y=13 为水 |
| 外围海床 | (512, -45, 0) 为 bedrock；上方 Y=-44 为水；只抽查一处外围区块，没有预生成远海 |
| 实际规则 | doMobSpawning、doPatrolSpawning、doTraderSpawning、doInsomnia、doWardenSpawning 均为 false；disableRaids=true；spawnChunkRadius=0 |
| 视距与模拟 | 集成服务端日志确认 6 / 5 |
| 工具加载 | WorldEdit 注册世界命令，Jade 接收服务端配置；未测试全部编辑命令、ProbeJS 导出和性能采样 |
| 保存 | 最终一轮调用游戏的 saveEverything(false, true, true)，返回 true；日志确认 All chunks are saved / All dimensions are saved，并检查实际文件大小 |
| 正常退出 | 最终一轮客户端 stop 后完成玩家和全部维度保存；runClient 返回 BUILD SUCCESSFUL、进程退出码 0；临时脚本已移出运行目录 |

首次尝试存档已保留到 `runtime/backups/DBV-Construction-first-entry-20261014`。重新打开自定义生成器世界时可能出现 NeoForge 实验性世界备份提示；本次未完成跨重启进入后的规则恢复验收。中途临时脚本曾因 KubeJS 保留名称和不暴露的 getClass 调用报错，已修正，最终采样执行完成；这些不是正式生成器的崩溃。

中间一轮通过脚本命令执行保存未落盘，该尝试已保留为 `runtime/backups/DBV-Construction-incomplete-save-20261014`，不作为可用存档。最终改为直接调用游戏保存 API，明确获得保存成功日志。退出改用客户端 stop 标志，让游戏主循环在脚本回调结束后处理关闭，避免在事件回调内阻塞等待集成服务器。

发现 spark 默认开启后台 profiler，已将本机及 `pack/overrides/config/spark/config.json` 的 backgroundProfiler 设为 false。后续只在诊断时手动采样。临时建档与验收脚本在检查后移出运行脚本目录，不随正式玩法发布。

以下为较早的启动检查记录；其中未建档的状态已由上面的实际结果更新。尚未验证潜艇航行、全部海底区块、长期实体生成、建筑编辑及关闭建设模式后的规则恢复。

| 检查 | 结果 |
| --- | --- |
| 编译与打包 | `build` 成功，生成 `mods/dbv-world/build/libs/dbv-world-0.1.0.jar` |
| 运行依赖解析 | 成功解析六个明确配置的 Mod 及 NeoForge 依赖 |
| 游戏资源下载 | Gradle 完成 downloadAssets，随后进入 runClient |
| 客户端启动 | 创建 Minecraft NeoForge 1.21.1 窗口；OpenGL、声音、纹理图集和着色器加载完成；未发现本次启动的崩溃报告 |
| 自研模块 | 日志确认 dbv-world 加载并注册 starter island preset |
| 首批核心模块 | 日志列出 Create 6.0.10、Sable 2.0.6、航空学 1.3.2、Deep Seas 3.3.0、Power Grid 0.6.2、Architectury 13.0.11；其内嵌组件同时加载 |
| 建设启动参数 | 生成的 JVM 参数包含 `-Ddbv.construction=true` |
| 临时客户端选项恢复 | 使用隔离的脚本夹具验证：应用 6/5/30 设置、重复启动不覆盖备份、关闭后恢复原值并保留无关偏好 |
| 地形算法 | 直接运行现有 Java 地形类，检查 4,198,401 个坐标：高度位于 -45～68，出生点周围干燥，南侧航道海床≤12，远处海底平坦 |
| 世界创建及游戏内验收 | 尚未执行，未生成正式存档；不能据此断言世界编解码、规则恢复及全部 Mod 在世界内均已通过 |

检查结束后已停止本次客户端运行，避免在用户工作时持续占用显卡和内存。Gradle 返回的中断退出码来自主动停止，不是游戏崩溃。游戏运行日志位于本机 `runtime/client/logs/latest.log`，没有纳入 Git。

## 待验证事项

Power Grid 报告 `powergridNative7` 本机加速库未加载，该发布 JAR 中未发现 DLL、SO 或 dylib 文件。它没有阻断客户端启动；电网求解性能与实际工作情况尚未验证，不为消除此信息额外下载来源不明的本机库。

Sable 提示与 Flywheel 的渲染兼容性需关注。Veil 和 Power Grid 对未安装 Iris、Farmer's Delight 的可选 Mixin 目标发出警告，未阻断本次加载，不据此擅自添加未在首批启用的 Mod。

还需要创建起始岛世界，检查水下基岩、出生位置、区块边界、刷怪规则应用及关闭建设模式后的存档规则恢复。临时规则主要覆盖原版生成行为，其他 Mod 自行生成实体的路径仍需世界内验证。当前没有操作原生游戏界面的可用工具，因此本次没有自动点击建档，也没有把启动日志当作实际画面检查。
