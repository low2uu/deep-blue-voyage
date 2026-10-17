# 当前 Mod 清单：深蓝远航

更新日期：2026-10-17。目标：**Minecraft 1.21.1 / NeoForge 21.1.259 / Java 21**。

## 本次整理规则

按用户最新澄清，将**已确定主路线的同类替代 Mod**移入备选，并非将所有功能 Mod 移入备选。前置库、必要兼容补丁和互补功能保留各自状态；历史同意记录保留，当前是否主选以所在分区为准。“确定加入”不等于全部已经安装。

2026-10-17新增用户确认的Dashpanels本体，实际运行依赖仍以`mods/dbv-world/build.gradle`为准。刚启用的JEI、JER、Xaero双地图、BetterF3、Jade Sable Compat及必需前置均保留；没有卸载现有Mod。核心Create、Aeronautics、Sable、Deep Seas、Power Grid和自研主艇系统保持主选。

| 功能 | 主选路线 | 同类备选 / 处理 |
| --- | --- | --- |
| 电力系统 | Power Grid | Crafts & Additions移入备选；柴油燃料和动力扩展不视为完全替代品 |
| 主艇中央控制 | Deep Seas船载电脑＋自研Mod | CC:Tweaked、Avionics、CC:Sable移入可编程控制备选；CC:Deep Seas仍在备选 |
| 配方与资源查询 | JEI＋JER | 两者互补，均保留；不把JER当作另一套JEI |
| 地图 | Xaero小地图＋世界地图 | 两者互补；FTB Chunks的领地与区块加载职责仍保留 |
| 领地与团队 | FTB Chunks＋FTB Teams＋AeroClaims | OPAC继续备选，不把功能重复写成硬冲突 |
| 声音物理 | Sound Physics: Aeronautics | Sound Physics Remastered因同modId和作者禁止同装而剔除 |
| 船内仓储 | Refined Storage＋Storage Drawers | 网络存储与大宗抽屉互补；Sophisticated Storage继续备选 |
| 地形、结构与其他玩法 | 原两类仍待定 | 地形替代项目继续备选；Deeper Oceans与Tectonic禁止同装，等方案定案后二选一 |
| 动态光源与远景 | 保留原选型及依赖条件 | 互斥分支和Distant Horizons继续备选，具体见条目 |

只有已确认与主选冲突的条目移出可选清单，原因和出处见[Mod剔除记录](Mod剔除记录-mod-exclusions.md)。版本待核、缺前置、未实测都不是“已证实不兼容”。Roxy作者明确支持特定1.21.11 Fabric Voxy作为1.21.1 NeoForge桥接输入，保留这一有官方依据的例外，不扩展到其他跨版本文件。

JEI当前锁定19.51.0.418，原版JAR已在正式开发环境启动、进世界及初始化GUI通过；历史元数据疑点保留但不再将其列为待定。Cloth Config已作为BetterF3前置单独启用；XaeroLib由地图内置，不重复添加。其他内置库按实际JAR依赖管理，避免重复安装。

## 数量与证据

| 区域 | 项数 |
| --- | ---: |
| 确定加入 | 100 |
| 待定 | 15 |
| 备选区 | 36 |
| 已剔除（只保留历史记录） | 1 |

原始Excel的111个编号均可追溯：110项保留在当前三分区，1项移入剔除记录；原Excel未改动。新增依赖、辅助工具和同类候选另行计数。全量字段、授权、是否直接启用及剔除历史见[结构化清单](current-mod-list.json)。

- [原文件静态核查](current-mod-evidence.json)：记录原始JAR元数据，不是当前安装锁定表。
- [辅助Mod启用记录](辅助Mod启用记录-auxiliary-mods.md)及[证据](auxiliary-mod-evidence.json)：最新辅助工具版本与运行验证。
- [开发工具记录](开发工具记录-development-tools.md)：WorldEdit、ProbeJS、KubeJS、spark、Jade等。
- [此前初筛池](Mod初筛表-mod-candidates.md)：历史研究候选，不自动进入当前主选。

## 确定加入

| 编号 / 类别 | Mod / 简介 | 选定版本 | 安装条件与处理原因 | 来源 |
| --- | --- | --- | --- | --- |
| A-DASHPANELS · 驾驶舱控制面板 | **Dashpanels**<br>模块化红石面板、电缆及Create联动 | 2.1+neoforge1.21.1 | **已加入运行依赖；船体操作与改装恢复待实测**<br>用户2026-10-17确认；NeoForge≥21.1.235，Create联动≥6.0.10，当前满足。未加入Extra。 | [依据](https://modrinth.com/mod/dashpanels/version/aH1Oe6Kk) |
| U001 · 核心与前置库 | **Create**<br>Create 机械动力核心，齿轮传动、机械装置 | 6.0.10+mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create/version/UjX6dr61) |
| U002 · 核心与前置库 | **Architectury API**<br>跨加载器 API 库（多 mod 依赖） | 13.0.11+neoforge | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/architectury-api/version/1IiqEQGl) |
| U003 · 核心与前置库 | **Balm**<br>实用工具库（Waystones 等依赖） | 21.0.66+neoforge-1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/balm/version/CquiaiDj) |
| U004 · 核心与前置库 | **Forgified Fabric API**<br>Fabric API 的 Forge/NeoForge 兼容层 | 0.116.15+2.3.5+1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/forgified-fabric-api/version/V9WdDUTx) |
| U005 · 核心与前置库 | **Kotlin for Forge**<br>Kotlin 语言运行支持 | 5.12.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/kotlin-for-forge/version/uhJhCT7X) |
| U006 · 核心与前置库 | **Rhino**<br>JavaScript 引擎（KubeJS 依赖） | 2101.2.8-build.91+Rhino-1.21 | **已启用；客户端加载通过，世界内功能待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/rhino/version/SqkDvOLG) |
| U007 · 核心与前置库 | **KubeJS**<br>用脚本自定义配方、物品、事件 | 2101.7.2-build.377 | **已启用；客户端加载通过，世界内功能待测**<br>Better Advanced Tooltips 已内置，不另开独立批准项。Rhino 原表版本满足 JAR 下限。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/kubejs/version/THIGFPwf) |
| U008 · 核心与前置库 | **SuperMartijn642's Core Lib**<br>通用核心库 | 1.1.25-neoforge-mc1.21 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/supermartijn642s-core-lib/version/OG6Edgmm) |
| U009 · 核心与前置库 | **SuperMartijn642's Config Lib**<br>配置文件库 | 1.1.8-neoforge-mc1.21 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/supermartijn642s-config-lib/version/qKL9jM75) |
| U010 · 核心与前置库 | **LDLib2**<br>客户端渲染/配置 UI 库 | 2.2.42 | **CurseForge 发布记录已核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://www.curseforge.com/minecraft/mc-mods/ldlib/files/9059591) |
| U011 · 核心与前置库 | **Geckolib**<br>实体/方块动画库 | 4.9.3 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/geckolib/version/Grwn5rUB) |
| U012 · 核心与前置库 | **Ponder for KubeJS**<br>Create 教学动画（Ponder）扩展 | 1.21.1-2.4.0+neoforge | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/ponder/version/BqhDf7W8) |
| U013 · 核心与前置库 | **Curios API**<br>饰品栏系统（额外装备槽） | 9.5.1+1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/curios/version/yohfFbgD) |
| U014 · 核心与前置库 | **Lithostitched**<br>结构/世界生成修补库 | 1.8.0-neoforge-21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/lithostitched/version/sPmtDq1X) |
| U015 · 核心与前置库 | **YUNG's API**<br>YUNG 系列 mod 的前置 API | 1.21.1-NeoForge-5.1.9 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/yungs-api/version/2prKITKh) |
| U017 · Create 机械动力扩展 | **Create: Connected**<br>连接增强：更便捷的动力传导 | 1.3.3-mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-connected/version/Xe7EqzfQ) |
| U018 · Create 机械动力扩展 | **Create Big Cannons**<br>大炮组装与发射系统 | 5.11.7 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-big-cannons/version/bOiDu0LS) |
| U019 · Create 机械动力扩展 | **CBC Military Supplement**<br>大炮军事补充（弹药/配件） | 2.1.4-1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/cbc-more-shells/version/lOHOokMh) |
| U020 · Create 机械动力扩展 | **CBC: Enhanced Shell Armor**<br>炮弹压缩装甲模块 | 1.0.0 | **暂缓锁包：需指定同名构建**<br>官方同名 1.0.0 文件有多个发布日期；项目支持目标版本，但安装前必须锁定具体文件 ID/哈希，不能只凭文件名。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://www.curseforge.com/minecraft/mc-mods/cbc-enhanced-shell-armor-create-big-cannons/files/all) |
| U021 · Create 机械动力扩展 | **Create: Diesel Generators**<br>柴油发电机与燃料系统 | 1.21.1-1.3.15 | **已启用；客户端加载与原生抽油接口通过**<br>2026-10-15再次明确批准，起始岛四个指定区块为无限原油；设施由玩家搭建。整条炼油线与移动潜艇燃油系统待测，未修改全局油量规则。见[油田说明](game-mechanics/起始岛无限油田-starter-oilfield.md)及[JAR证据](oilfield-mod-evidence.json)。<br><br>历史授权：原表除两类待定外其余都要；本次明确要求增加柴油动力与无限油田 | [依据](https://modrinth.com/mod/create-diesel-generators/version/UoPH8lO1) |
| U022 · Create 机械动力扩展 | **Create Ore Excavation**<br>机械钻头开采矿石 | 1.21.1-1.6.8 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-ore-excavation/version/tivxiPTo) |
| U023 · Create 机械动力扩展 | **Create: Enchantment Industry**<br>自动化附魔/经验工业 | 2.5.4 | **版本标签核对；整包待测**<br> 官方存在同名 Forge 1.20.1 文件；本清单仅指 NeoForge 1.21.1 发布记录，下载必须按版本链接/哈希。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-enchantment-industry/version/WJ2VPWAG) |
| U024 · Create 机械动力扩展 | **Create: Framed**<br>框架式结构方块 | 1.8.2+1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-framed/version/yioQUGiO) |
| U025 · Create 机械动力扩展 | **Create: Pattern Schematics**<br>图纸/蓝图复制结构 | 2.0.10 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-pattern-schematics/version/VSJhIkG2) |
| U026 · Create 机械动力扩展 | **Create: Power Loader**<br>动力区块加载器 | 2.0.5-mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-power-loader/version/3Y4r0ItR) |
| U027 · Create 机械动力扩展 | **Create: Hypertubes**<br>真空管道高速运输 | 0.6.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/hypertube/version/qYjOyvgW) |
| U028 · Create 机械动力扩展 | **Create: Radars**<br>雷达探测实体/载具 | 0.4.9.4-1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-radars/version/AntNFNAx) |
| U029 · Create 机械动力扩展 | **Create: Tweaked Controllers**<br>鼠标、键盘、手柄等输入控制 Create 机械；不是 CC:Tweaked 桥接前置。 | 1.21.1-1.2.7 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-tweaked-controllers/version/csCe2v7f) |
| U030 · Create 机械动力扩展 | **Create Jetpack**<br>Create 喷气背包 | 5.2.1 | **版本标签核对；整包待测**<br> 文件名带 forge，但该精确发布记录的加载器为 NeoForge，不因名字误删。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-jetpack/version/sCwiqLqq) |
| U031 · Create 机械动力扩展 | **Create: Curios Jetpack & Backtank**<br>喷气背包 Curios 兼容 | 1.2.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-curios-jetpack/version/JEmLrfe6) |
| U032 · Create 机械动力扩展 | **Create Deep Seas**<br>潜艇建造与驾驶 | 3.3.0 | **版本标签核对；整包待测**<br>JAR 核实只强制 Create/Ponder/Sable/Aeronautics/Simulated；内置 Fusion、Lithostitched。平台标必需的 Iris & Oculus Flywheel Compat 未出现在硬依赖中，不能因此自动加 Iris/光影链。Ponder 由 Create 提供，区别于 PonderJS。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-deep-seas/version/8CV1LRTl) |
| U033 · Create 机械动力扩展 | **Create: Advanced Optimization**<br>Create 性能优化 | 1.5 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-advanced-optimization/version/wBkaAnjm) |
| U034 · Create 机械动力扩展 | **Create: Dragons Plus**<br>批量染色、冷冻等加工功能与 Create 附属开发支持；是附魔工业前置，不是龙或飞行生物模组。 | 1.11.9 | **版本标签核对；整包待测**<br> 官方存在同名 Forge 1.20.1 文件；本清单仅指 NeoForge 1.21.1 发布记录，下载必须按版本链接/哈希。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-dragons-plus/version/b0u9vk8C) |
| U035 · Create 机械动力扩展 | **Create Sable Dynamic Lights**<br>机械动态光源（Sodium 兼容） | 2.3.1-sodium-sable | **暂缓启用：随 Sodium Dynamic Lights 前置补齐**<br>原文件属于 Create Sable Dynamic Lights（Sodium 分支），和 U077 是扩展与基础灯光引擎关系，应配套；不是两套重复引擎。不要再叠装同 modId 的其他 Dynamic Lights 分支。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-sable-dynamic-lights/version/sKw2ERw2) |
| U036 · Create 机械动力扩展 | **Rechiseled: Create**<br>Create 方块雕刻装饰 | 1.1.1-neoforge-mc1.21 | **暂缓安装：Rechiseled 本体待定**<br>附属已获同意，但 U109 Rechiseled 属用户明确待定类别；不自动提升本体为确定加入。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/rechiseled-create/version/VnOezhJR) |
| U037 · Create 机械动力扩展 | **Create: Copycats+**<br>可伪装成其他方块的复制方块 | 3.0.9+mc.1.21.1-neoforge | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/copycats/version/bPYeUWZx) |
| U038 · Create 机械动力扩展 | **Create: Interiors**<br>室内装饰家具 | 0.6.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/interiors/version/gBrfZy6S) |
| U039 · Create 机械动力扩展 | **Create: Bits 'n' Bobs**<br>Create 零件/装饰补充 | 2.3.5 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-bits-n-bobs/version/g9Lmjjc5) |
| U040 · Create 机械动力扩展 | **Create: Bells & Whistles**<br>Create 铃铛、汽笛等装饰 | v0.4.7-1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/bellsandwhistles/version/w0mifib8) |
| U041 · Create 机械动力扩展 | **Mechanicals Lib**<br>Mechanicals Lib 机械附属公共库；不应当成独立机械玩法。 | 1.21.1-1.1.6 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/mechanicals-lib/version/GBSlpwLT) |
| U042 · Create 机械动力扩展 | **Create: Power Grid**<br>电网传输系统 | 0.6.2 | **版本标签核对；整包待测**<br> 与其他动力模组存在功能交集但不等于硬冲突；保留用户已批准项，后续统一供电、燃料及进度配方。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/power-grid/version/jCkPHjmP) |
| U043 · Create 机械动力扩展 | **Create Aeronautics: Portable Engine Liquid Fuel**<br>便携引擎液体燃料 | 2.0.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-portable-engine-liquid-fuel/version/L6KPVFxI) |
| U044 · Create 机械动力扩展 | **Create Aeronautics: Dyeable Components**<br>可染色机械组件 | 1.0.4+mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/aeronautics-dyeable-components/version/kOW3VwPl) |
| U045 · Create 机械动力扩展 | **Create Aeronautics: Gadgets & Gizmos**<br>各类小工具/装置合集 | 1.2.10 | **版本标签核对；整包待测**<br>同一已获同意项目改选 1.2.10 release；原 2.0.0-Beta-1.1 被作者明确禁止用于整合包/重要世界。稳定分支的船舶自动导航也不等于通用潜艇 AI。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos/version/MgD655V5) |
| U047 · Create 机械动力扩展 | **Ritchie's Projectile Library**<br>弹射物物理库（大炮等依赖） | 2.1.2 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/rpl/version/hZ6B2Z0x) |
| U048 · Create 航空系列 | **Create Aeronautics**<br>Create 航空主体（飞艇/飞行器） | 1.3.2+mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics/version/44pLdPGg) |
| U049 · Create 航空系列 | **Create Aeronautics Lift Patch**<br>升力计算补丁 | 1.1.0 | **版本标签核对；整包待测**<br>修改升力/阻力计算；作者注明与 Wind Tunnel 不兼容。当前未选 Wind Tunnel；与 Sable 2.0.6 的具体物理行为待测。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-lift-patch/version/b5GqYXEI) |
| U050 · Create 航空系列 | **Create Aeronautics: Automated Logistics**<br>航空自动物流 | 0.6.2 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-automated-logistics/version/EgPi4wq9) |
| U051 · Create 航空系列 | **Create Aeronautics: Throwable Rope Connector**<br>投掷绳索连接器 | 0.4.3 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-throwable-rope-connector/version/EDzeTV17) |
| U052 · Create 航空系列 | **Create Aeronautics: Toolgun**<br>航空工具枪 | 0.3.6 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-toolgun/version/pcD0Yutw) |
| U053 · Create 航空系列 | **Create Aeronautics: Transmission & Linkage**<br>传动连杆机构 | 0.2.8 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-transmission-linkage/version/FCZZ3ED4) |
| U054 · Create 航空系列 | **Create Aeronautics: Gyro Stabilizers**<br>陀螺仪稳定器 | 1.2.2 | **版本标签核对；整包待测**<br>原文件名不带版本且多次发布复用；本清单选定官方 1.2.2，并在 JSON 记录官方 SHA-1。原表没有哈希，不能证明用户原文件就是该构建。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-gyroscope-stabilizers/version/9P9dNVUn) |
| U055 · Create 航空系列 | **Create Aeronautics Physics Gantry**<br>航空物理龙门架 | 1.0.3 | **版本标签核对；整包待测**<br>硬依赖 Sable Companion；Sable 2.0.6 等已内置 1.6.0。与 G&G 内相关机械功能有交集，不据此认定 modId 冲突。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-physics-gantry/version/QoeKSEzn) |
| U057 · Create 航空系列 | **Create Aeronautics: Compatibility**<br>航空兼容补丁 | 1.1.3 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-compatability/version/j10zlI9h) |
| U058 · Create 航空系列 | **Create Aeronautics: Encased Fluid Pipes**<br>航空封闭管道 | 1.0.7 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-encased-fluid-pipes/version/pKFEfjEB) |
| U059 · Create 航空系列 | **Create Aeronautics: Claims**<br>飞行器领地保护 | 0.9.4 | **版本标签核对；整包待测**<br>0.9.4 的 JAR 将 FTB Teams、FTB Chunks、OPAC 均列可选，更新日志已加入 FTB Chunks GUI 集成；不能只依据旧项目简介误判为强制 OPAC。主选用 FTB 后端。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/aeroclaims/version/dWMRSgEu) |
| U060 · Create 航空系列 | **Climbable Ropes for Create Aeronautics**<br>可攀爬绳索 | 2.1.4 | **版本标签核对；整包待测**<br>playerAnimator 已内置，不需要重复加入清单。攀爬跨船体及多人同步待测。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-climbable-rope/version/fk5DMIYY) |
| U061 · Create 航空系列 | **Strut Your Stuff (Struts)**<br>跨方块杆件/线缆结构库，供 Bits n Bobs 等附属使用。 | 1.3.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/strut-your-stuff/version/BOTN7zzN) |
| U062 · Create 航空系列 | **Drive-By-Wire with Sable**<br>线控驾驶/操控 | 0.3.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/drive-by-wire-sable/version/zdSwg9kS) |
| U063 · Create 航空系列 | **Azimuth API**<br>Azimuth API；Bits n Bobs 等项目的开发前置。 | 1.4.10 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/azimuth-api/version/dxtZVosY) |
| U064 · Create 航空系列 | **Create Smart Bounds**<br>优化 Create 方块实体渲染包围盒；不是潜艇通行体积锁。 | 1.0.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-smart-bounds/version/HXviiJ3I) |
| U065 · Sable 物理载具系列 | **Sable**<br>Sable 载具物理核心 | 2.0.6+mc1.21.1 | **版本标签核对；整包待测**<br>Sable 2.0.6 内置 Veil/Sable Companion 1.6.0；声明与更高的 Sable Companion、过旧 Sodium、ScalableLux 不兼容。原表 Sodium 0.8.13 满足下限；不另加 ScalableLux。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sable/version/fg9dTRz9) |
| U066 · Sable 物理载具系列 | **Sable Beyond**<br>Sable 扩展内容 | neoforge1.21.1+v0.5.0 | **版本标签核对；整包待测**<br>0.5.0 为 alpha；流体对船体施力、动态质量等需逐项配置和实测。Cloth Config 已内置，可供相容依赖共用。并非宏观洋流/速度锁的完整实现。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sable_beyond/version/BWQxgVOj) |
| U067 · Sable 物理载具系列 | **Sable: Physics Compat**<br>为其他模组方块补充 Sable 质量、浮力、摩擦等物理标签；不保证解决所有兼容问题。 | 1.3.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sablecompat/version/Ucq7afTi) |
| U068 · Sable 物理载具系列 | **Sable: Collision damage  (Create Aeronautics)**<br>碰撞伤害 | 1.0.8-Sable_2.0.0+ | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sable-create-aeronautics-collision-damage/version/snRIIWNz) |
| U069 · Sable 物理载具系列 | **Jade Sable Compat**<br>Jade 信息显示集成 | 1.3.0 | **已启用；正式客户端加载与进世界通过**<br>当前正式客户端加载并进入存档通过；具体交互和大型船体兼容仍需持续验证。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/jade-sable-compat/version/cxUohR7A) |
| U070 · Sable 物理载具系列 | **Sable: Mass view (Create Aeronautics)**<br>质量/重心显示 | 1.0.0 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-aeronautics-sable-mass-view/version/Dc1FsCO5) |
| U072 · Sable 物理载具系列 | **Waystones: Sable (Create Aeronautics Addon)**<br>传送石兼容 Sable | 1.0.7 | **暂缓安装：Waystones 本体待定**<br>U110 Waystones 仍待定；本附属要求 Waystones >=21.1.39，原表 21.1.46 数值满足，但必须等本体批准。传送功能也需考虑航行体验。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/waystones-sable/version/Wot8Pf4C) |
| U073 · Sable 物理载具系列 | **Create: Deep Seas - Lava Fix**<br>Create Deep Seas 熔岩相关修复；不是通用潜艇修复包。 | 1.0.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-deep-seas-lava-fix/version/9LUKMhCi) |
| U074 · Sable 物理载具系列 | **Sable: Water Pressure**<br>水压模拟 | 1.0.0 | **暂缓完整玩法：Ironworks 待批准**<br>项目页/发布依赖要求 Ironworks，JAR 只强制 Create；铜潜水全套仍需要 Ironworks 胸甲/护腿。不能视为全功能依赖已齐。作用于玩家，密闭干燥潜艇内免压，不实现船壳材料压力锁。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sable-water-pressure/version/sqjz0BcW) |
| U075 · Sable 物理载具系列 | **Separate Sable Render Distance (Create Aeronautics Addon)**<br>单独调整 Sable 移动结构渲染距离；不替代 Voxy 的地形远景。 | 1.8.7 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/ssrd/version/iZflRKdV) |
| U076 · 性能优化 | **Sodium**<br>Sodium 渲染优化（大幅提升 FPS） | mc1.21.1-0.8.13-neoforge | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sodium/version/uMOpc5uV) |
| U077 · 性能优化 | **Sodium Dynamic Lights**<br>动态光源（火把等实时照明） | neoforge-1.21.1-1.0.10 | **暂缓锁包：Sodium Options API 待批准**<br>发布依赖要求 Sodium Options API，原表未列；JAR 的版本文字仍为 1.0.9（发布为 1.0.10），不要据此误删。待补前置并联调。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sodium-dynamic-lights/version/XI0WLXdn) |
| U078 · 性能优化 | **Lithium**<br>游戏逻辑/服务端优化 | mc1.21.1-0.15.4-neoforge | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/lithium/version/DDUrRVCA) |
| U079 · 性能优化 | **FerriteCore**<br>内存占用优化 | 7.0.3-neoforge | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/ferrite-core/version/x7kQWVju) |
| U080 · 性能优化 | **ModernFix**<br>综合性能/启动优化 | 5.27.24+mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/modernfix/version/5HLHxQ2F) |
| U081 · 性能优化 | **BadOptimizations**<br>杂项性能优化 | 2.4.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/badoptimizations/version/S2qthD5S) |
| U082 · Voxy 远景渲染 | **Voxy（Roxy 桥接用原版 JAR）**<br>远景 LOD 渲染（超远视距） | 0.2.16-beta | **版本标签核对；整包待测**<br>Roxy 官方明确要求该 0.2.16-beta 原版文件；这是有作者支持的桥接例外，不代表其他 Fabric/跨版本文件可直接用。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/voxy/version/H3w2nVdU) |
| U085 · Voxy 远景渲染 | **Voxy WorldGen**<br>Voxy 世界生成兼容 | 2.4.2 | **版本标签核对；整包待测**<br>这是远景区块后台生成工具，不是新增群系/结构模组，所以按原分类已获批准。平台列 Cloth Config/Better ModList 必需，但 JAR 仅将 Cloth Config 设可选；Cloth Config 由 Sable Beyond 内置，Better ModList 不作为强制新增项。控制预生成队列，避免与其他生成器重复运行。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/voxy-worldgen/version/PzNUZlWw) |
| U086 · Voxy 远景渲染 | **Roxy**<br>Voxy 相关辅助 | 0.4.0-neoforge+mc1.21.1 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/roxy/version/cDmwpuYr) |
| U094 · 存储与物流 | **Refined Storage**<br>精致存储：数字化物品网络 | 2.0.9 | **版本标签核对；整包待测**<br> RS 网络存储与 Storage Drawers 大宗抽屉可以互补，不因都属于存储就二选一；船体移动后交互需要联调。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/refined-storage/version/lHHiI26k) |
| U095 · 存储与物流 | **Refined Storage - JEI Integration**<br>精致存储 JEI 集成 | 1.0.0 | **JEI已启用；RS及其集成尚未加入当前运行依赖**<br>本次只启用JEI，未额外安装Refined Storage或其JEI集成。后续按已批准清单分批联调。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/refined-storage-jei-integration/version/BYZmHubv) |
| U096 · 存储与物流 | **Storage Drawers**<br>储物抽屉（大量同类物品收纳） | 1.21.1-13.11.4 | **版本标签核对；整包待测**<br> RS 网络存储与 Storage Drawers 大宗抽屉可以互补，不因都属于存储就二选一；船体移动后交互需要联调。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/storagedrawers/version/px0CCB06) |
| U097 · 存储与物流 | **Lootr**<br>战利品箱独立刷新（每人独立） | 1.21.1-1.11.38.127 | **版本标签核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/lootr/version/SPOrIzX8) |
| U098 · 界面、工具与多人 | **Just Enough Items (JEI)**<br>JEI 物品/配方查询 | 19.51.0.418 | **已启用；正式客户端加载与进世界通过**<br>锁定19.51.0.418：原版JAR的Minecraft范围声明仍为[1.21,1.21.1)，但当前NeoForge 21.1.259正式开发环境实测加载、进世界及JEI GUI初始化通过；未修改JAR或依赖限制，不外推其他运行环境。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/jei/version/ufHUqt9b) |
| U099 · 界面、工具与多人 | **Xaero's Minimap**<br>Xaero 小地图（实时小地图与路径点） | neoforge-1.21.1-26.6.0 | **已启用；正式客户端加载与进世界通过**<br>当前正式客户端加载并进入存档通过；具体交互和大型船体兼容仍需持续验证。 XaeroLib 1.7.3已内置；在线更新检查超时不阻断加载。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/xaeros-minimap/version/irjR7dHk) |
| U100 · 界面、工具与多人 | **Xaero's World Map**<br>Xaero 世界地图（全图浏览与标记） | neoforge-1.21.1-1.47.0 | **已启用；正式客户端加载与进世界通过**<br>当前正式客户端加载并进入存档通过；具体交互和大型船体兼容仍需持续验证。 XaeroLib 1.7.3已内置；在线更新检查超时不阻断加载。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/xaeros-world-map/version/SnQ4SoDe) |
| U101 · 界面、工具与多人 | **FTB Library**<br>FTB 系列基础库 | 2101.1.37 | **CurseForge 发布记录已核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://www.curseforge.com/minecraft/mc-mods/ftb-library-forge/files/all) |
| U102 · 界面、工具与多人 | **FTB Quests**<br>任务/进度系统 | 2101.1.36 | **CurseForge 发布记录已核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge/files/all) |
| U103 · 界面、工具与多人 | **FTB Chunks**<br>区块认领/加载 | 2101.1.22 | **CurseForge 发布记录已核对；整包待测**<br> Xaero 小地图/世界地图保留；FTB Chunks 主要负责领地/加载，重复地图界面可在配置层处理。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge/files/all) |
| U104 · 界面、工具与多人 | **FTB Teams**<br>队伍/权限管理 | 2101.1.11 | **CurseForge 发布记录已核对；整包待测**<br><br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge/files/all) |
| U107 · 音效 | **Sound Physics: Aeronautics**<br>航空载具音效支持 | 2.0.1 | **版本标签核对；整包待测**<br>独立替代分支，不是需要原版的附属；移除 U106 后再安装。JAR 要求 Create [6.0.10,6.0.11)，当前 6.0.10 满足。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/sound-physics-aeronautics/version/MDdwFCrR) |
| D002 · 界面、工具与多人 | **Jade 🔍**<br>显示指向方块和实体的信息；Jade Sable Compat 的 HUD 主体 | 15.10.6+neoforge | **已启用；客户端加载通过，世界内功能待测**<br>15.10.6 改善航空学兼容性；本次批准并加入运行依赖。<br><br>历史授权：用户于 2026-10-14 明确同意增加推荐表前五项开发工具 | [依据](https://modrinth.com/mod/jade/version/eYz2YBGT) |
| D004 · 开发与建设工具 | **WorldEdit**<br>批量建图、建筑蓝图导入导出和地形编辑 | 7.3.8 | **已加入开发运行依赖；客户端加载通过，世界内功能待测**<br>开发阶段工具，不因本次启用自动列为玩家发布包必装。<br><br>历史授权：用户于 2026-10-14 明确同意增加推荐表前五项开发工具 | [依据](https://modrinth.com/mod/worldedit/version/WTAFvuRx) |
| D005 · 开发与建设工具 | **ProbeJS**<br>导出 KubeJS 类型与接口资料，辅助 VS Code 和脚本开发 | 8.0.3 | **已加入开发运行依赖；客户端加载通过，世界内功能待测**<br>开发阶段工具，不因本次启用自动列为玩家发布包必装。<br><br>历史授权：用户于 2026-10-14 明确同意增加推荐表前五项开发工具 | [依据](https://modrinth.com/mod/probejs/version/xC9KUA7R) |
| D006 · 开发与建设工具 | **spark**<br>CPU、内存及 tick 性能分析 | 1.10.124-neoforge-1.21.1 | **已加入开发运行依赖；客户端加载通过，世界内功能待测**<br>开发阶段工具，不因本次启用自动列为玩家发布包必装。<br><br>历史授权：用户于 2026-10-14 明确同意增加推荐表前五项开发工具 | [依据](https://modrinth.com/mod/spark/version/v5qtqRQi) |
| D007 · 界面、工具与多人 | **Just Enough Resources (JER)**<br>JEI资源与掉落信息 | 1.6.0.17 | **已启用；正式客户端加载与进世界通过**<br>当前正式客户端加载并进入存档通过；具体交互和大型船体兼容仍需持续验证。 已确认JER分类和配方注册完成。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/just-enough-resources-jer/version/TgNFki8j) |
| D008 · 界面、工具与多人 | **BetterF3**<br>更清晰的F3调试信息 | 11.0.3 | **已启用；正式客户端加载与进世界通过**<br>当前正式客户端加载并进入存档通过；具体交互和大型船体兼容仍需持续验证。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/betterf3/version/maXNB1dn) |
| D009 · 界面、工具与多人 | **Cloth Config API**<br>BetterF3必需配置界面前置 | 15.0.140+neoforge | **已启用；正式客户端加载与进世界通过**<br>当前正式客户端加载并进入存档通过；具体交互和大型船体兼容仍需持续验证。<br><br>历史授权：用户2026-10-14明确要求加入；Cloth Config为BetterF3必需前置 | [依据](https://modrinth.com/mod/cloth-config/version/izKINKFg) |

## 待定

| 编号 / 类别 | Mod / 简介 | 选定版本 | 安装条件与处理原因 | 来源 |
| --- | --- | --- | --- | --- |
| U083 · Voxy 远景渲染 | **Voxy 服务端组件（原文件身份待核）**<br>Voxy 服务端组件 | 未核实 | **暂缓安装：未核实文件**<br>与多种同名 Voxy Server Side / LOD Server Support 项目容易混淆。现有发布列表未精确匹配此无版本文件名；需原 JAR 或下载地址。不要与 U084 同时默认启用。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据]() |
| U084 · Voxy 远景渲染 | **Voxy Server LOD（原文件身份待核）**<br>服务端 LOD 生成 | 未核实 | **暂缓安装：未核实文件**<br>未找到可可靠匹配 1.1.4 的官方记录；不能用其他同名项目的兼容性代替。与 U083 属同类，待识别后择一；备选 B 项提供已核实路线。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据]() |
| U087 · 世界生成与结构 | **Terralith**<br>大型地形生成（山脉、生物群系） | 2.6.2 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/terralith/version/IY93YaEe) |
| U088 · 世界生成与结构 | **Tectonic**<br>地形/地层增强 | 3.0.28-neoforge-21.1 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/tectonic/version/n4iyW7aB) |
| U089 · 世界生成与结构 | **When Dungeons Arise**<br>大型地牢/建筑结构生成 | 2.1.68 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/when-dungeons-arise/version/XIRJSFQ0) |
| U090 · 世界生成与结构 | **When Dungeons Arise: Seven Seas**<br>七海海洋结构 | 1.0.4 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/when-dungeons-arise-seven-seas/version/erk04BGa) |
| U091 · 世界生成与结构 | **YUNG's Better Dungeons**<br>更好的地牢设计 | 1.21.1-NeoForge-5.1.4 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/yungs-better-dungeons/version/D6aZn0Em) |
| U092 · 世界生成与结构 | **YUNG's Better Ocean Monuments**<br>更好的海底神殿 | 1.21.1-NeoForge-4.1.2 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/yungs-better-ocean-monuments/version/yFjEcj2g) |
| U093 · 世界生成与结构 | **Create Aeronautics Structures**<br>额外结构生成 | 1.2 | **暂缓安装：等待用户选定**<br>已精确匹配 Create Aeronautics Structures 1.2；不是按通用文件名猜测。仍遵照世界生成与结构类别暂缓。<br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/aeronautics-structures/version/JWhmlx4p) |
| U108 · 玩法与其他 | **[UNOFFICIAL] TaCZ 1.21.1 NeoForge Port**<br>TACZ 现代枪械模组 | 1.1.8-hotfix-r7 | **暂缓安装：等待用户选定**<br>对应 MUKSC 非官方 1.21.1 NeoForge 移植；不是 TaCZ 原官方 1.20.1 文件。仍按玩法与其他类别待定。<br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/tacz-1.21.1/version/62q6mB5Q) |
| U109 · 玩法与其他 | **Rechiseled**<br>方块雕刻/纹理装饰 | 1.2.6-neoforge-mc1.21 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/rechiseled/version/6muGRqvu) |
| U110 · 玩法与其他 | **Waystones**<br>传送石（快速传送） | 21.1.46+neoforge-1.21.1 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/waystones/version/6Z6MQ6os) |
| U111 · 玩法与其他 | **Aquaculture 2**<br>水产养殖/钓鱼扩展 | 2.7.21 | **暂缓安装：等待用户选定**<br><br><br>历史授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [依据](https://modrinth.com/mod/aquaculture/version/5pbz0ETj) |
| D001 · 新增依赖申请 | **Sodium Options API**<br>U077 的发布依赖；未在用户原表，等待批准。 | neoforge-1.21.1-1.0.10 | **等待用户批准；批准后再锁包**<br>U077 的发布依赖；未在用户原表，等待批准。<br><br>历史授权：新增依赖/功能主体，未获用户同意 | [依据](https://modrinth.com/mod/sodium-options-api/version/lrsX3TMS) |
| D003 · 新增依赖申请 | **Create: Ironworks**<br>U074 铜潜水套装完整功能所需；会增加内容，等待用户同意。 | 4.0.3 | **等待用户批准；批准后再锁包**<br>U074 铜潜水套装完整功能所需；会增加内容，等待用户同意。<br><br>历史授权：新增依赖/功能主体，未获用户同意 | [依据](https://modrinth.com/mod/create-ironworks/version/91r9PehB) |

## 备选区

| 编号 / 类别 | Mod / 简介 | 选定版本 | 安装条件与处理原因 | 来源 |
| --- | --- | --- | --- | --- |
| U016 · Create 机械动力扩展 | **Create Crafts & Additions**<br>电力扩展：发电机、电动机、电线 | neoforge-1.21.1-1.7.2 | **同类路线备选；不在当前默认安装计划**<br>电力主选为Create: Power Grid；Crafts & Additions保留为同类电力路线备选。功能并不完全等价，并非硬性不兼容。 原兼容性说明： 与其他动力模组存在功能交集但不等于硬冲突；保留用户已批准项，后续统一供电、燃料及进度配方。<br>用户要求将已确定Mod的同类Mod移入备选；历史授权保留，不等于当前主选。<br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/createaddition/version/hsjVajlP) |
| U046 · Create 机械动力扩展 | **CC: Tweaked**<br>ComputerCraft 编程电脑/机器人 | 1.120.2 | **同类路线备选；不在当前默认安装计划**<br>主艇控制确定采用Deep Seas船载电脑及自研系统；CC:Tweaked保留为可编程电脑路线备选，不代表其机器人等独立功能已由主艇电脑替代。 原兼容性说明： 文件名带 forge，但该精确发布记录的加载器为 NeoForge，不因名字误删。<br>用户要求将已确定Mod的同类Mod移入备选；历史授权保留，不等于当前主选。<br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/cc-tweaked/version/1ewzHZYg) |
| U056 · Create 航空系列 | **Create: Avionics — ComputerCraft for Aeronautics**<br>航空电子/仪表 | 0.6.0 | **同类路线备选；不在当前默认安装计划**<br>随CC:Tweaked可编程船载控制路线移入备选；当前主艇使用Deep Seas船载电脑与自研适配。 原兼容性说明：<br>用户要求将已确定Mod的同类Mod移入备选；历史授权保留，不等于当前主选。<br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/create-avionics/version/9siqVCcu) |
| U071 · Sable 物理载具系列 | **CC: Sable**<br>ComputerCraft 驾驶兼容 | 1.3.4 | **同类路线备选；不在当前默认安装计划**<br>随CC:Tweaked可编程船载控制路线移入备选；与Avionics属于接口补充，不声明二者硬互斥。 原兼容性说明：<br>用户要求将已确定Mod的同类Mod移入备选；历史授权保留，不等于当前主选。<br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/cc-sable/version/j0UWQoMG) |
| U105 · 界面、工具与多人 | **Open Parties and Claims**<br>开放领地与队伍保护 | neoforge-1.21.1-0.32.7 | **不与主选同时启用领地功能**<br>同类替代 FTB Chunks 领地路线。主选 FTB Chunks + FTB Teams，服务 FTB Quests/物流；AeroClaims 0.9.4 更新日志和 JAR 均支持 FTB 集成。不是硬性不兼容；若换 OPAC，需同步改领地/团队配置。<br><br>历史授权：用户本次明确同意：原表除两类待定外其余都要 | [依据](https://modrinth.com/mod/open-parties-and-claims/version/qMR7tHTs) |
| B001 · 同类备选 / Tectonic / 海洋地形 | **Deeper Oceans**<br>加深海洋，为大型潜艇提供更大的垂直空间。 | 2.0.1-neoforge-21.1 | **备选，不进入当前安装表**<br>更深海底候选；地形方案整体待定。 作者明确不兼容 Tectonic；与其他海床生成器择一测试。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/deeper-oceans/version/rHJ7RgjW) |
| B002 · 同类备选 / Tectonic / 海洋比例 | **Continents**<br>将陆地分成相隔更远的大陆，形成宽阔海洋。 | 1.1.14 | **备选，不进入当前安装表**<br>大陆与海域分布路线；不是直接叠加全部地形模组。 可作为大洋布局候选；Large Biomes 与 Terralith 的组合有作者限制。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/continents/version/bVXaqvWr) |
| B003 · 同类备选 / Tectonic / 海洋世界 | **Oceanus**<br>离中心越远越趋向无尽海洋的世界生成方案。 | v2.1.0+mod | **备选，不进入当前安装表**<br>海洋化地形替代路线，需与深海维度区分。 默认内圈约 5000 格、过渡约 2000 格；并非出生点全海洋。项目停止维护。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/oceanus/version/EAKpVSjV) |
| B004 · 同类备选 / Terralith / 群系 | **Regions Unexplored**<br>扩展生态群系、植被与方块。 | 0.6.2-neoforge-21.1 | **备选，不进入当前安装表**<br>增加模组群系的另一条路线。 辅助海岛生态；会增加内容和群系分布调参工作。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/regions-unexplored/version/5A8LFnXX) |
| B005 · 同类备选 / Terralith / 群系 | **Biomes O' Plenty**<br>超多生物群系；增加群系、植物和建筑材料。 | 21.1.0.14 | **备选，不进入当前安装表**<br>与 Regions Unexplored 等对比选型，不能当成已批准。 辅助岛屿景观；与海洋主导生成规则联调。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/biomes-o-plenty/version/BtZKRp69) |
| B006 · 同类备选 / 海洋战斗内容 | **Aquamirae**<br>冰海、船墓地和海洋怪物，含 Captain Cornelia 战斗。 | 7.2.12 | **备选，不进入当前安装表**<br>此前主题初筛候选；补充海洋 Boss，待玩法方案批准。 狭窄冰迷宫更适合离艇探险；新版依赖 Fragmentum 与 GeckoLib。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/aquamirae/version/RSbxcVPE) |
| B007 · 同类备选 / 海洋战斗内容 | **L_Ender's Cataclysm**<br>灾变；大型地牢和 Boss，包含海洋探索内容。 | 3.33 | **备选，不进入当前安装表**<br>含海洋 Boss 的战斗内容候选；不是已确认加入。 也加入陆地与其他维度内容；战利品强度与船体战斗另测。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/l_enders-cataclysm/version/PsPYpoCC) |
| B008 · 同类备选 / Aquaculture / 海洋生物 | **Hybrid Aquatic**<br>鱼类、鲨鱼、甲壳类等水生生态和潜水内容。 | mc1.21.1-1.7.4-neoforge | **备选，不进入当前安装表**<br>扩充海洋生态，与钓鱼系统侧重点不同，可择需补充。 前置较多，含 Kotlin for Forge；与其他生态模组控制总刷怪量。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/hybrid-aquatic/version/jdOU7Pzu) |
| B009 · 同类备选 / Aquaculture / 钓鱼 | **Tide 2**<br>扩展钓鱼、鱼类与钓具进度。 | 2.1.1 | **备选，不进入当前安装表**<br>钓鱼生态替代/补充方案，玩法仍未定。 不应把所有钓获物都当作可见游动生物。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/tide/version/1ki6UyqV) |
| B010 · 同类备选 / YUNG 海底神殿 | **Dungeons and Taverns Ocean Monument Overhaul**<br>Dungeons and Taverns 的海底神殿重构。 | v1.2+mod | **备选，不进入当前安装表**<br>同一原版结构的重做路线，默认择一，不能无脑重叠。 与 YUNG 同类重做择一，不默认叠加。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/dungeons-and-taverns-ocean-monument-overhaul/version/SFXYDC6X) |
| B011 · 同类备选 / 世界生成与结构 | **Hopo Better Underwater Ruins**<br>扩展海底废墟、城市遗迹与考古探索。 | 1.2.1b | **备选，不进入当前安装表**<br>海底遗迹候选；与神殿不是硬互斥。 检查深海床生成高度、遗迹密度与战利品。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/hopo-better-underwater-ruins/version/cCUmdH6y) |
| B012 · 同类备选 / 世界生成与结构 | **Awesome Dungeon Ocean**<br>海洋地牢、船只和水下遗迹。 | 3.3.0 | **备选，不进入当前安装表**<br>海洋地牢候选，需控制结构密度。 结构船通常是静态遗迹，不能默认直接驾驶。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/awesome-dungeon-edition-ocean/version/4oZY86TA) |
| B013 · 同类备选 / 世界生成与结构 | **MOS - Moog's Ocean Structures**<br>大量海洋结构和探索地点。 | 1.1.1 | **备选，不进入当前安装表**<br>海洋结构候选，具体组合待定。 多结构包并用需降低生成密度，保留潜艇航道。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/mos-moogs-ocean-structures/version/QfMITqh9) |
| B014 · 同类备选 / 船体装饰 | **Create Deco**<br>工业建筑装饰，适合潜艇与港口。 | 2.1.3 | **备选，不进入当前安装表**<br>Create Framed / Interiors / Rechiseled 的同类补充，非硬互斥。 船壳装饰是否密封需按方块实测。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/create-deco/version/qrcMVoBD) |
| B015 · 同类备选 / 船体装饰 | **Create: Design n' Decor**<br>工业设计与装饰方块。 | 2.2b | **备选，不进入当前安装表**<br>同类装饰扩展，模型和 Sable 兼容另测。 运动船体上配套航空学兼容补丁候选。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/create-design-n-decor/version/uQsIRky8) |
| B016 · 同类备选 / 潜水装备 | **Create: Aquatic Ambitions**<br>海洋材料与机械动力加工联动，提供海洋资源循环。 | 1.21.1-6.0.10-2.0.4 | **备选，不进入当前安装表**<br>Create 潜水装备扩展，可与压力玩法比较。 2.x 要求 Create 至少 6.0.6；与 Upgrade Aquatic 可联动。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/create-aquatic-ambitions/version/DoI3PpXj) |
| B017 · 同类备选 / CC 船载控制 | **CC: Deep Seas**<br>Deep Seas 压载、推进及舱室数据的电脑外设。 | 1.1.1 | **备选，不进入当前安装表**<br>与 Avionics / CC:Sable 为同类接口补充，不等于开箱即用 AI。 适合潜艇仪表；自动驾驶仍需自行编程与测试。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/cc-deep-seas/version/qKkaeGSS) |
| B018 · 同类备选 / 船内控制布线 | **Create: Aero Cables**<br>运动结构之间连接电力和数据网络。 | 1.21-0.4.0 | **备选，不进入当前安装表**<br>与 Drive-By-Wire 的布线目标相近，具体信号类型不同。 作者覆盖 FE、AE2、RS、CC 等；不能据此推断支持全部交错电网导线。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/create-aero-cables/version/Wn81ihHo) |
| B019 · 同类备选 / 交错电网附属 | **Create: Power Components**<br>交错电网附加电力元件。 | 1.3.0 | **备选，不进入当前安装表**<br>同类电力组件补充；不是用户已批准的 Power Grid 本体。 核对 Power Grid 版本与移动船体连接。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/create-power-components/version/W3GV5Ldp) |
| B020 · 同类备选 / 载具操控 | **Aeronautics: Calibrated**<br>航空学动力、控制和导航相关设备。 | 1.4.0 | **备选，不进入当前安装表**<br>与 G&G / Gyro 等比较可配置控制功能。 多套推进方案并用需统一能耗与控制逻辑。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/aeronautics-calibrated/version/78Nl65uk) |
| B021 · 同类备选 / 船坞升降机构 | **Create: Lift n' Load ／  Simulated/Aeronautics Addon**<br>吊运、装卸及载具建造工具扩展。 | 1.0c | **备选，不进入当前安装表**<br>与 Physics Gantry / G&G 比较，非硬冲突。 适合港口补给与装配；部件运动行为需联调。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/create-lift-n-load/version/G7N1k717) |
| B022 · 同类备选 / 潜水装备 | **Simple Dive Gear**<br>独立潜水装备，提供离艇探索能力。 | 10.0.0 | **备选，不进入当前安装表**<br>可替代部分潜水装备体验，压力联动仍待设计。 调整持续时间与成本，避免跳过氧气进度。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/simple-dive-gear/version/4ZexmS2I) |
| B023 · 同类备选 / RS / 抽屉存储 | **Sophisticated Storage**<br>精妙存储；船内仓储和过滤。 | 1.21.1-1.6.5.2176 | **备选，不进入当前安装表**<br>升级箱体路线；不是 RS 网络的等价替换。 移动船体中方块实体、升级和自动化接口待测。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/sophisticated-storage/version/EvzAB4E6) |
| B024 · 同类备选 / PonderJS / 教程展示 | **Patchouli**<br>制作航海日志、潜艇建造手册与生物图鉴。 | 1.21.1-93-neoforge | **备选，不进入当前安装表**<br>书本教程路线，和场景教程可互补，但尚未批准。 手册内容需自行编写。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/patchouli/version/BIogJv2D) |
| B025 · 同类备选 / 载具 | **Small Ships**<br>小型可驾驶船只，作为初期航行或救援艇。 | 2.0.1-NeoForge | **备选，不进入当前安装表**<br>预制小船作为早期交通备选，不替代自建大型潜艇。 不是巨型自由拼装潜艇；Deep Sea 的船损规则需检查。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/small-ships/version/QNjKZdCr) |
| B026 · 同类备选 / 载具 | **Immersive Aircraft**<br>独立飞机类载具，提供空中运输备选。 | 1.6.0+1.21.1 | **备选，不进入当前安装表**<br>预制飞机路线；与自建载具定位不同，先备选。 可能绕过海上探索路线，属于可选内容。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/immersive-aircraft/version/ZCSI1mBS) |
| B027 · 同类备选 / Voxy + Roxy | **Distant Horizons**<br>远景 LOD，适合远洋与大型岛屿视野。 | 3.3.3-1.21.1 | **备选，不进入当前安装表**<br>另一套远景渲染路线；主客户端择一进行基准测试。 不保证运动船体进入远景；配合光影时单独测试。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/distanthorizons/version/9w34y8ai) |
| B028 · 同类备选 / Voxy World Gen V2 | **Chunky**<br>提前生成区块，降低远航途中生成卡顿。 | 1.4.23 | **备选，不进入当前安装表**<br>区块预生成工具替代；不直接替代 Voxy 渲染，勿并行重复生成。 确定最终地形后再预生成；消耗磁盘与生成时间。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/chunky/version/LuFhm4eU) |
| B029 · 同类备选 / 光影兼容 | **Iris & Oculus Flywheel Compat**<br>Iris/Oculus 与 Flywheel 渲染适配。 | 1.21.1+2.4.0-release | **备选，不进入当前安装表**<br>可选光影路线；Deep Seas JAR 未硬要求，未获同意不默认安装。 补丁不代表所有光影都能兼容 Deep Seas。<br><br>历史授权：此前初筛候选；未获用户同意 | [依据](https://modrinth.com/mod/iris-flw-compat/version/6ZQz5Ow2) |
| B030 · 同类备选 / 动态光照 | **Sable Dynamic Lights (Create: Dynamic Lights fork)**<br>另一套基于 LambDynamicLights 的分支；替换 Create Sable Dynamic Lights + Sodium Dynamic Lights 整条路线，不能叠加同名核心；新引擎前置也需批准。 | 2.0.1 | **备选，不进入当前安装表**<br>另一套基于 LambDynamicLights 的分支；替换 Create Sable Dynamic Lights + Sodium Dynamic Lights 整条路线，不能叠加同名核心；新引擎前置也需批准。<br><br>历史授权：新检索到的同类路线；未获同意 | [依据](https://modrinth.com/mod/sable-dynamic-lights/version/vjb0hAWB) |
| B031 · 同类备选 / 服务端远景传输 | **LOD Server Support**<br>已核实的 1.21.1 NeoForge 路线，官方说明支持 Roxy；可代替身份待核的 U083/U084，属于新项目批准，不能冒认为原文件。 | v0.18.0+neoforge+mc1.21.1 | **备选，不进入当前安装表**<br>已核实的 1.21.1 NeoForge 路线，官方说明支持 Roxy；可代替身份待核的 U083/U084，属于新项目批准，不能冒认为原文件。<br><br>历史授权：新检索到的同类路线；未获同意 | [依据](https://modrinth.com/mod/lod-server-support/version/4WpqxQiA) |

## 兼容性边界

尚未完成全清单任意组合实测。此前静态核查中的Create/Sable等版本要求与当前基线对照，未发现新的明确硬依赖冲突；这不能替代后续安装测试。压力玩家惩罚不等于船壳承压，渲染包围盒不等于通行体积锁；这些项目不因为关键词相同就视为同类替代品。

已启用模块的运行验证见开发记录；未启用的“确定加入”仍须分批安装验证。新项目只有获得用户同意才进入确定区；切换互斥备选路线时先移除对应主选并复核依赖。
