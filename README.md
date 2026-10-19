# 深蓝远航

当前开发版本：**0.1.1**（[版本记录](docs/版本记录-changelog.md)）。

以《海底两万里》式潜艇探险为主题的 Minecraft 整合包。玩家建造大型潜艇，在广阔、连通的海洋中远航，探索不同海洋群系、海底遗迹与海洋 Boss。

英文工作名为 Deep Blue Voyage，目录名为 `deep-blue-voyage`。

启动游戏、进入当前“深蓝远航·联合开发”存档、新电脑建图及更新最新版代码，先看 [部署启动与版本更新](docs/部署启动与版本更新-deployment-launch-update.md)。

文档入口见 [文档索引](docs/README.md)。新增说明文档使用中文文件名、中文标题和中文正文；各级 `README.md` 保持原名。文档改名时同步更新相对链接和文件路径引用，机器读取的 JSON、源代码及第三方依赖文件沿用原有命名。

## 开发环境

- Minecraft：1.21.1。
- 加载器：NeoForge 21.1.259，首批核心组合仍需运行验证。
- 自研 Mod 语言：Java 21。
- 本机 JDK：Zulu 21.0.3。
- 编辑器：VS Code，已配置 Java 和 Gradle 扩展。

使用 VS Code 打开本目录，并安装推荐的 Java 和 Gradle 扩展。将 `JAVA_HOME` 指向本机 JDK 21。可通过不纳入版本管理的 `.vscode/settings.json` 设置本机 JDK 路径；仓库不携带开发者的绝对路径。运行 `powershell -ExecutionPolicy Bypass -File tools/check-environment.ps1` 可检查本机 Java 与 Git。

项目默认语言为简体中文。中文资源在 `zh_cn.json` 中维护，英文资源在 `en_us.json` 中维护；英文文件保持英文。新开发客户端从整合包默认设置初始化中文，已有客户端保留玩家选择的语言。

正式工程位于 `mods/dbv-world`，使用随项目提供的 Gradle Wrapper。打开 `deep-blue-voyage.code-workspace` 后，运行 `powershell -NoProfile -ExecutionPolicy Bypass -File tools/dev.ps1 build` 构建，使用同一命令的 `run` 动作启动客户端。详见 [部署流程](docs/开发环境部署-development-setup.md) 和 [起始岛建设阶段方案](docs/game-mechanics/起始岛建设方案-starter-island-plan.md)。

临时建设设置集中在 `config/development-session.json`，不代表最终生存规则。当前只制作起始岛与简单近海，不预生成远海。

已启用机械动力柴油动力，起始岛东侧提供32×32无限原油区；油田随起始岛存档加载初始化，抽油及炼油设施由玩家搭建。位置和建井要求见[无限油田交接说明](docs/game-mechanics/起始岛无限油田-starter-oilfield.md)。

主潜艇系统已开始开发：当前支持三项潜艇资格检查、潜行右键船载电脑查看设备、管理员登记唯一主艇、独立身份档案和每秒船况观测，现已增加建设模式初始小艇生成及真实方块改装（物理船体拆解为世界方块，完成时检测并重新装配，失败／取消还原及方块阶段重启回滚），尚未开放生存改装及完整损毁恢复。操作见 [初始潜艇与改装流程](docs/development/初始潜艇与改装流程-starter-submarine-refit.md)。目录、命令、数据格式及验收记录见 [系统开发文档](docs/development/主潜艇系统开发文档-flagship-system-development.md)。

已完成源码构建、客户端加载及起始岛建筑与环境初版：废旧工厂、温馨生活区、自然地形和128×128船坞，机器工位留空。进入方式、地图快照和最新验收结果见 [起始岛建设记录](docs/game-mechanics/起始岛建筑与环境初版-starter-island-built-v1.md)；早期检查见 [开发工程检查记录](docs/开发验证记录-development-verification.md)。潜艇与机器玩法仍待开发。

## 目录用途

| 目录 | 用途 |
| --- | --- |
| `docs/` | 玩法设计、Mod 选型和测试标准 |
| `pack/overrides/` | 整合包配置、脚本与资源，作为打包输入 |
| `mods/` | 自研 Java Mod 工程，每个 Mod 单独建目录 |
| `assets/` | 原始纹理、模型、音效和宣传素材 |
| `runtime/client/` | 正式开发工程的客户端运行目录与本机存档 |
| `runtime/server/` | 服务端目录预留，尚未配置启动任务 |
| `runtime/backups/` | 测试存档备份 |
| `dist/` | 后续导出的整合包与发布文件 |
| `tools/` | 环境检查和后续构建辅助脚本 |

`runtime/` 和 `dist/` 不纳入版本管理。将需要发布的配置同步回 `pack/overrides/`，不要把测试存档、日志或账号信息混入发布包。

## 下一步

玩法整体方案见 [初版地图与科技路线](docs/game-mechanics/初版地图与科技路线-first-playable-design.md)：以海域限制组织探索，用母艇、子艇与离艇潜水分工，串联六个科技阶段；先制作一条约 2～4 小时的可玩样板路线。文中数值为待测试的设计目标，不代表地图生成或机制已实现。

当前选型以 [当前 Mod 清单](docs/当前Mod清单-current-mod-list.md) 为准：按用户提供的 111 项文件表划分确定加入、待定和备选区，记录授权、文件替换、依赖缺口与明确互斥。另有 [结构化清单](docs/current-mod-list.json)、[关键 JAR 静态证据](docs/current-mod-evidence.json) 和 [原清单 Excel（2026-10-13历史快照）](outputs/current-mod-list-20261013/深蓝远航-当前Mod清单.xlsx)。初筛候选不代表已同意加入。

已完成第一轮 [Mod 初筛表](docs/Mod初筛表-mod-candidates.md)：170 项候选，包含 161 个有 1.21.1 NeoForge 发布文件的模组项目和 9 个支持 1.21.1 的数据包。表内记录简介、版本、前置、来源及兼容性备注；[结构化目录](docs/mod-catalog.json) 与 [可筛选 Excel](outputs/mod-screening-20261013/深蓝远航-Mod初筛.xlsx) 同步保存。此阶段尚未进行整包运行验证。

目前已有可装配的[灯笼鱼型潜艇](docs/development/灯笼鱼型潜艇与操作说明-angler-submarine-and-operation.md)，支持柴油传动、四角压载、双桨、制氧和独立气闸布局；已验证装配、双桨换向和断岸供运行。旧存档须主动执行升级命令，不会自动覆盖主艇。另见[起始岛保护](docs/game-mechanics/起始岛保护-starter-island-protection.md)和[控制器深度与升降控制](docs/development/控制器深度与升降控制-controller-depth-and-ballast.md)。现已恢复Deep Seas原生算法、调整初始艇浮力与独立换向传动，加入管理面板“停泊”及中文驾驶指南，完成1～15米范围的分档升降与前后运行检查。见[原生控制与返航停泊](docs/development/原生潜艇控制与返航停泊-native-submarine-control-and-mooring.md)。下一步验证长途、负载变化及气闸水下往返，再扩展群系、Boss与成长路线。

2026-10-16：此前的潜艇全局调参已全部恢复为 Deep Seas 默认值（单舱升力75、浮筒升力25、升降速度上限2.0），启动不再强制覆盖。初始艇由用户设计；上文海试结果对应旧配置，本次未修改船体或世界存档。
