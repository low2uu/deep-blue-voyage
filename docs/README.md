# 深蓝远航文档索引

新增说明文档使用中文文件名、中文标题和中文正文；各级 `README.md` 保持原名。现有文档改名时同步更新引用；JSON 清单、程序源码、运行目录和第三方依赖不因本规则改名。

## 开发与验证

| 文档 | 内容 |
| --- | --- |
| [系统开发文档](development/README.md) | 自研模块架构、代码目录、数据格式和开发验收 |
| [开发环境部署](开发环境部署-development-setup.md) | Windows、Zulu Java 21、VS Code 与协作开发流程 |
| [部署、启动地图与版本更新](部署启动与版本更新-deployment-launch-update.md) | 本机启动、新电脑部署、进入 v2、新建起始岛及更新代码 |
| [开发工具记录](开发工具记录-development-tools.md) | 已批准的建设、脚本和性能工具 |
| [开发验证记录](开发验证记录-development-verification.md) | 客户端、起始岛与工程验证记录 |
| [世界存档与汉化资源推送](世界存档与汉化资源推送-world4-and-l10n.md) | 「新的世界4」酒狐号存档与中文资源包交付 |
| [世界存档推送（新的世界5）](世界存档推送-新的世界5-world5.md) | 重制潜艇物理结构 `66d01998-…` 与设计快照交付 |

## 玩法与 Mod 选型

| 文档 | 内容 |
| --- | --- |
| [玩法设计](玩法设计-design.md) | 潜艇远航主题和整体方向 |
| [当前 Mod 清单](当前Mod清单-current-mod-list.md) | 确定、待定、备选及安装状态 |
| [Mod 初筛表](Mod初筛表-mod-candidates.md) | 海洋主题候选池及版本信息 |
| [游戏机制索引](game-mechanics/README.md) | 功能架构、海域限制、AI、地图与建设方案 |

## 当前建设与主潜艇

| 文档 | 内容 |
| --- | --- |
| [初始潜艇与改装流程](development/初始潜艇与改装流程-starter-submarine-refit.md) | 建设小艇、中文管理界面、备份与回滚边界 |
| [主艇独立数据与舱内面板](development/主艇独立数据与舱内面板-vessel-data-and-cabin-panel.md) | 独立 JSON 档案、容器汇总、信息面板与存储恢复 |
| [起始岛建筑与环境初版](game-mechanics/起始岛建筑与环境初版-starter-island-built-v1.md) | 已建成地图、船坞、机器预留空间与验收 |
| [起始岛工厂与蓝图方案](game-mechanics/起始岛工厂与蓝图方案-starter-factory-blueprint-plan.md) | 首期工厂规模与机器选型 |
| [机器蓝图库](game-mechanics/机器蓝图库-machine-blueprint-catalog.md) | 尺寸、版本、来源与使用说明 |
| [主潜艇与酒狐实现方案](game-mechanics/主潜艇与酒狐实现方案-flagship-submarine-plan.md) | 唯一主艇、改装、备份、监控和恢复方案 |
| [潜艇相关 Mod 能力核对](game-mechanics/潜艇相关Mod能力核对-submarine-mod-capability-research.md) | 航空学、Sable、Deep Seas等能力与接口依据 |

文档中的“设计、候选、待验证”不代表功能已经实现；以各文档的状态和实际验收记录为准。

辅助工具新增与加载验证：[辅助Mod启用记录](辅助Mod启用记录-auxiliary-mods.md)。

当前清单已按主选/同类备选整理；明确冲突的项目见[Mod剔除记录](Mod剔除记录-mod-exclusions.md)。
