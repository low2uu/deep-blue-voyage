# 当前 Mod 清单：深蓝远航

核对日期：2026-10-13。目标：**Minecraft 1.21.1 / NeoForge / Java 21**。

本清单以你提供的 Excel 111 项为基础。你的“除了世界生成与结构、玩法与其他，这俩先待定，其它都要”视为对其余类别的明确授权；不是对之前 170 项初筛候选的整体授权。新增项目及替代路线未获同意前均不进入确定区。

“确定加入”表示项目已获同意且已有目标平台发布证据，**不代表所有具体文件已完成安装验证**。安装条件列明确区分缺前置、主模组待定与整包待测。保留每个原表序号，原 Excel 和实际游戏文件均未改动。

发布页按目标版本与加载器核对；重点读取 38 个原文件的 JAR 元数据，并补核 G&G 稳定版、较新 JEI 和 Voxy World Gen。记录见 [静态证据](current-mod-evidence.json)，全量字段和官方文件 SHA-1 见 [结构化清单](current-mod-list.json)。源 Excel 没有文件哈希，所以“文件名匹配”不能证明用户电脑上的同名 JAR 字节相同。

## 分类规则与主要结论

- 原表两类共 11 项继续待定；世界生成基础库不因“服务于世界生成”而擅自推翻你的分类授权。Voxy World Gen 是预生成工具，按原表远景分类处理。
- 主选 FTB Chunks + FTB Teams + AeroClaims；OPAC 放备选。AeroClaims 0.9.4 已加入 FTB 集成，不以旧简介推断硬依赖 OPAC。Xaero 地图继续保留，重复界面后续配置。
- 主选 Sound Physics Aeronautics；Sound Physics Remastered 放备选，两者不可同时装。
- G&G 项目已获同意，原 Beta-1.1 文件被作者明确排除整包用途，改选同项目 1.2.10 release；不据此承诺通用潜艇自动驾驶已经实现。
- Voxy 0.2.16-beta 的 1.21.11 Fabric JAR 是 Roxy 官方支持的特定桥接输入，保留并明确标注；其余跨版本/加载器文件不享受自动例外。两个无法精确识别的服务端远景文件先待核，不能默认同装。
- JEI 原文件的发布标签和内部 Minecraft 版本范围矛盾，暂缓锁包，相关 RS–JEI 集成随之暂缓；没有运行证据就不把它写成已证实不兼容。
- Rechiseled: Create、Waystones: Sable 已获同意，但本体仍属用户明确待定项，因此暂缓安装。新增 Jade、Sodium Options API、Ironworks 只列批准申请。
- 不重复添加已内置的 Better Advanced Tooltips、playerAnimator、MezzConfig、Fusion、Cloth Config、Sable Companion 等。平台“必需”与 JAR 声明不一致时写明差异，不擅自引入光影链。
- 核心按 Create 6.0.10 + Aeronautics 1.3.2 + Sable 2.0.6 + Deep Seas 3.3.0 + Power Grid 0.6.2 对照。已读原文件中 JEI 要求 NeoForge >=21.1.238；其他已读文件最高下限至少 21.1.229。最终加载器构建仍需全量依赖锁定后确定。

## 数量

| 区域 | 项数 |
|---|---:|
| 确定加入 | 95 |
| 待定 | 17 |
| 备选区 | 33 |

原表 111 项在三类中各出现一次；新增项仅为 3 个待批准依赖与同类备选。初筛池其余项目仍见 [此前初筛表](mod-candidates.md)，没有默认为本次确认项。

## 确定加入

| 编号 / 原分类 | Mod / 简介 | 选定版本 | 安装条件与处理原因 | 官方依据 |
|---|---|---|---|---|
| U001 · 核心与前置库 | **Create**<br>Create 机械动力核心，齿轮传动、机械装置 | 6.0.10+mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create/version/UjX6dr61) |
| U002 · 核心与前置库 | **Architectury API**<br>跨加载器 API 库（多 mod 依赖） | 13.0.11+neoforge | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/architectury-api/version/1IiqEQGl) |
| U003 · 核心与前置库 | **Balm**<br>实用工具库（Waystones 等依赖） | 21.0.66+neoforge-1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/balm/version/CquiaiDj) |
| U004 · 核心与前置库 | **Forgified Fabric API**<br>Fabric API 的 Forge/NeoForge 兼容层 | 0.116.15+2.3.5+1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/forgified-fabric-api/version/V9WdDUTx) |
| U005 · 核心与前置库 | **Kotlin for Forge**<br>Kotlin 语言运行支持 | 5.12.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/kotlin-for-forge/version/uhJhCT7X) |
| U006 · 核心与前置库 | **Rhino**<br>JavaScript 引擎（KubeJS 依赖） | 2101.2.8-build.91+Rhino-1.21 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/rhino/version/SqkDvOLG) |
| U007 · 核心与前置库 | **KubeJS**<br>用脚本自定义配方、物品、事件 | 2101.7.2-build.377 | **版本标签核对；整包待测**<br>Better Advanced Tooltips 已内置，不另开独立批准项。Rhino 原表版本满足 JAR 下限。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/kubejs/version/THIGFPwf) |
| U008 · 核心与前置库 | **SuperMartijn642's Core Lib**<br>通用核心库 | 1.1.25-neoforge-mc1.21 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/supermartijn642s-core-lib/version/OG6Edgmm) |
| U009 · 核心与前置库 | **SuperMartijn642's Config Lib**<br>配置文件库 | 1.1.8-neoforge-mc1.21 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/supermartijn642s-config-lib/version/qKL9jM75) |
| U010 · 核心与前置库 | **LDLib2**<br>客户端渲染/配置 UI 库 | 2.2.42 | **CurseForge 发布记录已核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://www.curseforge.com/minecraft/mc-mods/ldlib/files/9059591) |
| U011 · 核心与前置库 | **Geckolib**<br>实体/方块动画库 | 4.9.3 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/geckolib/version/Grwn5rUB) |
| U012 · 核心与前置库 | **Ponder for KubeJS**<br>Create 教学动画（Ponder）扩展 | 1.21.1-2.4.0+neoforge | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/ponder/version/BqhDf7W8) |
| U013 · 核心与前置库 | **Curios API**<br>饰品栏系统（额外装备槽） | 9.5.1+1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/curios/version/yohfFbgD) |
| U014 · 核心与前置库 | **Lithostitched**<br>结构/世界生成修补库 | 1.8.0-neoforge-21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/lithostitched/version/sPmtDq1X) |
| U015 · 核心与前置库 | **YUNG's API**<br>YUNG 系列 mod 的前置 API | 1.21.1-NeoForge-5.1.9 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/yungs-api/version/2prKITKh) |
| U016 · Create 机械动力扩展 | **Create Crafts & Additions**<br>电力扩展：发电机、电动机、电线 | neoforge-1.21.1-1.7.2 | **版本标签核对；整包待测**<br> 与其他动力模组存在功能交集但不等于硬冲突；保留用户已批准项，后续统一供电、燃料及进度配方。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/createaddition/version/hsjVajlP) |
| U017 · Create 机械动力扩展 | **Create: Connected**<br>连接增强：更便捷的动力传导 | 1.3.3-mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-connected/version/Xe7EqzfQ) |
| U018 · Create 机械动力扩展 | **Create Big Cannons**<br>大炮组装与发射系统 | 5.11.7 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-big-cannons/version/bOiDu0LS) |
| U019 · Create 机械动力扩展 | **CBC Military Supplement**<br>大炮军事补充（弹药/配件） | 2.1.4-1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/cbc-more-shells/version/lOHOokMh) |
| U020 · Create 机械动力扩展 | **CBC: Enhanced Shell Armor**<br>炮弹压缩装甲模块 | 1.0.0 | **暂缓锁包：需指定同名构建**<br>官方同名 1.0.0 文件有多个发布日期；项目支持目标版本，但安装前必须锁定具体文件 ID/哈希，不能只凭文件名。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://www.curseforge.com/minecraft/mc-mods/cbc-enhanced-shell-armor-create-big-cannons/files/all) |
| U021 · Create 机械动力扩展 | **Create: Diesel Generators**<br>柴油发电机与燃料系统 | 1.21.1-1.3.15 | **版本标签核对；整包待测**<br> 与其他动力模组存在功能交集但不等于硬冲突；保留用户已批准项，后续统一供电、燃料及进度配方。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-diesel-generators/version/UoPH8lO1) |
| U022 · Create 机械动力扩展 | **Create Ore Excavation**<br>机械钻头开采矿石 | 1.21.1-1.6.8 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-ore-excavation/version/tivxiPTo) |
| U023 · Create 机械动力扩展 | **Create: Enchantment Industry**<br>自动化附魔/经验工业 | 2.5.4 | **版本标签核对；整包待测**<br> 官方存在同名 Forge 1.20.1 文件；本清单仅指 NeoForge 1.21.1 发布记录，下载必须按版本链接/哈希。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-enchantment-industry/version/WJ2VPWAG) |
| U024 · Create 机械动力扩展 | **Create: Framed**<br>框架式结构方块 | 1.8.2+1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-framed/version/yioQUGiO) |
| U025 · Create 机械动力扩展 | **Create: Pattern Schematics**<br>图纸/蓝图复制结构 | 2.0.10 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-pattern-schematics/version/VSJhIkG2) |
| U026 · Create 机械动力扩展 | **Create: Power Loader**<br>动力区块加载器 | 2.0.5-mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-power-loader/version/3Y4r0ItR) |
| U027 · Create 机械动力扩展 | **Create: Hypertubes**<br>真空管道高速运输 | 0.6.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/hypertube/version/qYjOyvgW) |
| U028 · Create 机械动力扩展 | **Create: Radars**<br>雷达探测实体/载具 | 0.4.9.4-1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-radars/version/AntNFNAx) |
| U029 · Create 机械动力扩展 | **Create: Tweaked Controllers**<br>鼠标、键盘、手柄等输入控制 Create 机械；不是 CC:Tweaked 桥接前置。 | 1.21.1-1.2.7 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-tweaked-controllers/version/csCe2v7f) |
| U030 · Create 机械动力扩展 | **Create Jetpack**<br>Create 喷气背包 | 5.2.1 | **版本标签核对；整包待测**<br> 文件名带 forge，但该精确发布记录的加载器为 NeoForge，不因名字误删。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-jetpack/version/sCwiqLqq) |
| U031 · Create 机械动力扩展 | **Create: Curios Jetpack & Backtank**<br>喷气背包 Curios 兼容 | 1.2.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-curios-jetpack/version/JEmLrfe6) |
| U032 · Create 机械动力扩展 | **Create Deep Seas**<br>潜艇建造与驾驶 | 3.3.0 | **版本标签核对；整包待测**<br>JAR 核实只强制 Create/Ponder/Sable/Aeronautics/Simulated；内置 Fusion、Lithostitched。平台标必需的 Iris & Oculus Flywheel Compat 未出现在硬依赖中，不能因此自动加 Iris/光影链。Ponder 由 Create 提供，区别于 PonderJS。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-deep-seas/version/8CV1LRTl) |
| U033 · Create 机械动力扩展 | **Create: Advanced Optimization**<br>Create 性能优化 | 1.5 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-advanced-optimization/version/wBkaAnjm) |
| U034 · Create 机械动力扩展 | **Create: Dragons Plus**<br>批量染色、冷冻等加工功能与 Create 附属开发支持；是附魔工业前置，不是龙或飞行生物模组。 | 1.11.9 | **版本标签核对；整包待测**<br> 官方存在同名 Forge 1.20.1 文件；本清单仅指 NeoForge 1.21.1 发布记录，下载必须按版本链接/哈希。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-dragons-plus/version/b0u9vk8C) |
| U035 · Create 机械动力扩展 | **Create Sable Dynamic Lights**<br>机械动态光源（Sodium 兼容） | 2.3.1-sodium-sable | **暂缓启用：随 Sodium Dynamic Lights 前置补齐**<br>原文件属于 Create Sable Dynamic Lights（Sodium 分支），和 U077 是扩展与基础灯光引擎关系，应配套；不是两套重复引擎。不要再叠装同 modId 的其他 Dynamic Lights 分支。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-sable-dynamic-lights/version/sKw2ERw2) |
| U036 · Create 机械动力扩展 | **Rechiseled: Create**<br>Create 方块雕刻装饰 | 1.1.1-neoforge-mc1.21 | **暂缓安装：Rechiseled 本体待定**<br>附属已获同意，但 U109 Rechiseled 属用户明确待定类别；不自动提升本体为确定加入。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/rechiseled-create/version/VnOezhJR) |
| U037 · Create 机械动力扩展 | **Create: Copycats+**<br>可伪装成其他方块的复制方块 | 3.0.9+mc.1.21.1-neoforge | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/copycats/version/bPYeUWZx) |
| U038 · Create 机械动力扩展 | **Create: Interiors**<br>室内装饰家具 | 0.6.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/interiors/version/gBrfZy6S) |
| U039 · Create 机械动力扩展 | **Create: Bits 'n' Bobs**<br>Create 零件/装饰补充 | 2.3.5 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-bits-n-bobs/version/g9Lmjjc5) |
| U040 · Create 机械动力扩展 | **Create: Bells & Whistles**<br>Create 铃铛、汽笛等装饰 | v0.4.7-1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/bellsandwhistles/version/w0mifib8) |
| U041 · Create 机械动力扩展 | **Mechanicals Lib**<br>Mechanicals Lib 机械附属公共库；不应当成独立机械玩法。 | 1.21.1-1.1.6 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/mechanicals-lib/version/GBSlpwLT) |
| U042 · Create 机械动力扩展 | **Create: Power Grid**<br>电网传输系统 | 0.6.2 | **版本标签核对；整包待测**<br> 与其他动力模组存在功能交集但不等于硬冲突；保留用户已批准项，后续统一供电、燃料及进度配方。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/power-grid/version/jCkPHjmP) |
| U043 · Create 机械动力扩展 | **Create Aeronautics: Portable Engine Liquid Fuel**<br>便携引擎液体燃料 | 2.0.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-portable-engine-liquid-fuel/version/L6KPVFxI) |
| U044 · Create 机械动力扩展 | **Create Aeronautics: Dyeable Components**<br>可染色机械组件 | 1.0.4+mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/aeronautics-dyeable-components/version/kOW3VwPl) |
| U045 · Create 机械动力扩展 | **Create Aeronautics: Gadgets & Gizmos**<br>各类小工具/装置合集 | 1.2.10 | **版本标签核对；整包待测**<br>同一已获同意项目改选 1.2.10 release；原 2.0.0-Beta-1.1 被作者明确禁止用于整合包/重要世界。稳定分支的船舶自动导航也不等于通用潜艇 AI。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos/version/MgD655V5) |
| U046 · Create 机械动力扩展 | **CC: Tweaked**<br>ComputerCraft 编程电脑/机器人 | 1.120.2 | **版本标签核对；整包待测**<br> 文件名带 forge，但该精确发布记录的加载器为 NeoForge，不因名字误删。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/cc-tweaked/version/1ewzHZYg) |
| U047 · Create 机械动力扩展 | **Ritchie's Projectile Library**<br>弹射物物理库（大炮等依赖） | 2.1.2 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/rpl/version/hZ6B2Z0x) |
| U048 · Create 航空系列 | **Create Aeronautics**<br>Create 航空主体（飞艇/飞行器） | 1.3.2+mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics/version/44pLdPGg) |
| U049 · Create 航空系列 | **Create Aeronautics Lift Patch**<br>升力计算补丁 | 1.1.0 | **版本标签核对；整包待测**<br>修改升力/阻力计算；作者注明与 Wind Tunnel 不兼容。当前未选 Wind Tunnel；与 Sable 2.0.6 的具体物理行为待测。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-lift-patch/version/b5GqYXEI) |
| U050 · Create 航空系列 | **Create Aeronautics: Automated Logistics**<br>航空自动物流 | 0.6.2 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-automated-logistics/version/EgPi4wq9) |
| U051 · Create 航空系列 | **Create Aeronautics: Throwable Rope Connector**<br>投掷绳索连接器 | 0.4.3 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-throwable-rope-connector/version/EDzeTV17) |
| U052 · Create 航空系列 | **Create Aeronautics: Toolgun**<br>航空工具枪 | 0.3.6 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-toolgun/version/pcD0Yutw) |
| U053 · Create 航空系列 | **Create Aeronautics: Transmission & Linkage**<br>传动连杆机构 | 0.2.8 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-transmission-linkage/version/FCZZ3ED4) |
| U054 · Create 航空系列 | **Create Aeronautics: Gyro Stabilizers**<br>陀螺仪稳定器 | 1.2.2 | **版本标签核对；整包待测**<br>原文件名不带版本且多次发布复用；本清单选定官方 1.2.2，并在 JSON 记录官方 SHA-1。原表没有哈希，不能证明用户原文件就是该构建。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-gyroscope-stabilizers/version/9P9dNVUn) |
| U055 · Create 航空系列 | **Create Aeronautics Physics Gantry**<br>航空物理龙门架 | 1.0.3 | **版本标签核对；整包待测**<br>硬依赖 Sable Companion；Sable 2.0.6 等已内置 1.6.0。与 G&G 内相关机械功能有交集，不据此认定 modId 冲突。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-physics-gantry/version/QoeKSEzn) |
| U056 · Create 航空系列 | **Create: Avionics — ComputerCraft for Aeronautics**<br>航空电子/仪表 | 0.6.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-avionics/version/9siqVCcu) |
| U057 · Create 航空系列 | **Create Aeronautics: Compatibility**<br>航空兼容补丁 | 1.1.3 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-compatability/version/j10zlI9h) |
| U058 · Create 航空系列 | **Create Aeronautics: Encased Fluid Pipes**<br>航空封闭管道 | 1.0.7 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-encased-fluid-pipes/version/pKFEfjEB) |
| U059 · Create 航空系列 | **Create Aeronautics: Claims**<br>飞行器领地保护 | 0.9.4 | **版本标签核对；整包待测**<br>0.9.4 的 JAR 将 FTB Teams、FTB Chunks、OPAC 均列可选，更新日志已加入 FTB Chunks GUI 集成；不能只依据旧项目简介误判为强制 OPAC。主选用 FTB 后端。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/aeroclaims/version/dWMRSgEu) |
| U060 · Create 航空系列 | **Climbable Ropes for Create Aeronautics**<br>可攀爬绳索 | 2.1.4 | **版本标签核对；整包待测**<br>playerAnimator 已内置，不需要重复加入清单。攀爬跨船体及多人同步待测。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-climbable-rope/version/fk5DMIYY) |
| U061 · Create 航空系列 | **Strut Your Stuff (Struts)**<br>跨方块杆件/线缆结构库，供 Bits n Bobs 等附属使用。 | 1.3.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/strut-your-stuff/version/BOTN7zzN) |
| U062 · Create 航空系列 | **Drive-By-Wire with Sable**<br>线控驾驶/操控 | 0.3.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/drive-by-wire-sable/version/zdSwg9kS) |
| U063 · Create 航空系列 | **Azimuth API**<br>Azimuth API；Bits n Bobs 等项目的开发前置。 | 1.4.10 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/azimuth-api/version/dxtZVosY) |
| U064 · Create 航空系列 | **Create Smart Bounds**<br>优化 Create 方块实体渲染包围盒；不是潜艇通行体积锁。 | 1.0.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-smart-bounds/version/HXviiJ3I) |
| U065 · Sable 物理载具系列 | **Sable**<br>Sable 载具物理核心 | 2.0.6+mc1.21.1 | **版本标签核对；整包待测**<br>Sable 2.0.6 内置 Veil/Sable Companion 1.6.0；声明与更高的 Sable Companion、过旧 Sodium、ScalableLux 不兼容。原表 Sodium 0.8.13 满足下限；不另加 ScalableLux。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sable/version/fg9dTRz9) |
| U066 · Sable 物理载具系列 | **Sable Beyond**<br>Sable 扩展内容 | neoforge1.21.1+v0.5.0 | **版本标签核对；整包待测**<br>0.5.0 为 alpha；流体对船体施力、动态质量等需逐项配置和实测。Cloth Config 已内置，可供相容依赖共用。并非宏观洋流/速度锁的完整实现。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sable_beyond/version/BWQxgVOj) |
| U067 · Sable 物理载具系列 | **Sable: Physics Compat**<br>为其他模组方块补充 Sable 质量、浮力、摩擦等物理标签；不保证解决所有兼容问题。 | 1.3.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sablecompat/version/Ucq7afTi) |
| U068 · Sable 物理载具系列 | **Sable: Collision damage  (Create Aeronautics)**<br>碰撞伤害 | 1.0.8-Sable_2.0.0+ | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sable-create-aeronautics-collision-damage/version/snRIIWNz) |
| U069 · Sable 物理载具系列 | **Jade Sable Compat**<br>Jade 信息显示集成 | 1.3.0 | **暂缓启用：Jade 本体待批准**<br>JAR 将 Jade 标为可选，但没有 Jade 就没有这项 HUD 联动；Jade 新增申请列待定，不默认同意。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/jade-sable-compat/version/cxUohR7A) |
| U070 · Sable 物理载具系列 | **Sable: Mass view (Create Aeronautics)**<br>质量/重心显示 | 1.0.0 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-aeronautics-sable-mass-view/version/Dc1FsCO5) |
| U071 · Sable 物理载具系列 | **CC: Sable**<br>ComputerCraft 驾驶兼容 | 1.3.4 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/cc-sable/version/j0UWQoMG) |
| U072 · Sable 物理载具系列 | **Waystones: Sable (Create Aeronautics Addon)**<br>传送石兼容 Sable | 1.0.7 | **暂缓安装：Waystones 本体待定**<br>U110 Waystones 仍待定；本附属要求 Waystones >=21.1.39，原表 21.1.46 数值满足，但必须等本体批准。传送功能也需考虑航行体验。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/waystones-sable/version/Wot8Pf4C) |
| U073 · Sable 物理载具系列 | **Create: Deep Seas - Lava Fix**<br>Create Deep Seas 熔岩相关修复；不是通用潜艇修复包。 | 1.0.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/create-deep-seas-lava-fix/version/9LUKMhCi) |
| U074 · Sable 物理载具系列 | **Sable: Water Pressure**<br>水压模拟 | 1.0.0 | **暂缓完整玩法：Ironworks 待批准**<br>项目页/发布依赖要求 Ironworks，JAR 只强制 Create；铜潜水全套仍需要 Ironworks 胸甲/护腿。不能视为全功能依赖已齐。作用于玩家，密闭干燥潜艇内免压，不实现船壳材料压力锁。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sable-water-pressure/version/sqjz0BcW) |
| U075 · Sable 物理载具系列 | **Separate Sable Render Distance (Create Aeronautics Addon)**<br>单独调整 Sable 移动结构渲染距离；不替代 Voxy 的地形远景。 | 1.8.7 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/ssrd/version/iZflRKdV) |
| U076 · 性能优化 | **Sodium**<br>Sodium 渲染优化（大幅提升 FPS） | mc1.21.1-0.8.13-neoforge | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sodium/version/uMOpc5uV) |
| U077 · 性能优化 | **Sodium Dynamic Lights**<br>动态光源（火把等实时照明） | neoforge-1.21.1-1.0.10 | **暂缓锁包：Sodium Options API 待批准**<br>发布依赖要求 Sodium Options API，原表未列；JAR 的版本文字仍为 1.0.9（发布为 1.0.10），不要据此误删。待补前置并联调。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sodium-dynamic-lights/version/XI0WLXdn) |
| U078 · 性能优化 | **Lithium**<br>游戏逻辑/服务端优化 | mc1.21.1-0.15.4-neoforge | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/lithium/version/DDUrRVCA) |
| U079 · 性能优化 | **FerriteCore**<br>内存占用优化 | 7.0.3-neoforge | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/ferrite-core/version/x7kQWVju) |
| U080 · 性能优化 | **ModernFix**<br>综合性能/启动优化 | 5.27.24+mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/modernfix/version/5HLHxQ2F) |
| U081 · 性能优化 | **BadOptimizations**<br>杂项性能优化 | 2.4.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/badoptimizations/version/S2qthD5S) |
| U082 · Voxy 远景渲染 | **Voxy（Roxy 桥接用原版 JAR）**<br>远景 LOD 渲染（超远视距） | 0.2.16-beta | **版本标签核对；整包待测**<br>Roxy 官方明确要求该 0.2.16-beta 原版文件；这是有作者支持的桥接例外，不代表其他 Fabric/跨版本文件可直接用。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/voxy/version/H3w2nVdU) |
| U085 · Voxy 远景渲染 | **Voxy WorldGen**<br>Voxy 世界生成兼容 | 2.4.2 | **版本标签核对；整包待测**<br>这是远景区块后台生成工具，不是新增群系/结构模组，所以按原分类已获批准。平台列 Cloth Config/Better ModList 必需，但 JAR 仅将 Cloth Config 设可选；Cloth Config 由 Sable Beyond 内置，Better ModList 不作为强制新增项。控制预生成队列，避免与其他生成器重复运行。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/voxy-worldgen/version/PzNUZlWw) |
| U086 · Voxy 远景渲染 | **Roxy**<br>Voxy 相关辅助 | 0.4.0-neoforge+mc1.21.1 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/roxy/version/cDmwpuYr) |
| U094 · 存储与物流 | **Refined Storage**<br>精致存储：数字化物品网络 | 2.0.9 | **版本标签核对；整包待测**<br> RS 网络存储与 Storage Drawers 大宗抽屉可以互补，不因都属于存储就二选一；船体移动后交互需要联调。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/refined-storage/version/lHHiI26k) |
| U095 · 存储与物流 | **Refined Storage - JEI Integration**<br>精致存储 JEI 集成 | 1.0.0 | **暂缓安装：等 JEI 文件核实**<br>已同意该集成项目；依赖 JEI，随 U098 解决后安装。RS 版本约束使用裸版本字符串，不能误当成必须等于旧 beta。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/refined-storage-jei-integration/version/BYZmHubv) |
| U096 · 存储与物流 | **Storage Drawers**<br>储物抽屉（大量同类物品收纳） | 1.21.1-13.11.4 | **版本标签核对；整包待测**<br> RS 网络存储与 Storage Drawers 大宗抽屉可以互补，不因都属于存储就二选一；船体移动后交互需要联调。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/storagedrawers/version/px0CCB06) |
| U097 · 存储与物流 | **Lootr**<br>战利品箱独立刷新（每人独立） | 1.21.1-1.11.38.127 | **版本标签核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/lootr/version/SPOrIzX8) |
| U099 · 界面、工具与多人 | **Xaero's Minimap**<br>Xaero 小地图（实时小地图与路径点） | neoforge-1.21.1-26.6.0 | **版本标签核对；整包待测**<br> Xaero 小地图/世界地图保留；FTB Chunks 主要负责领地/加载，重复地图界面可在配置层处理。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/xaeros-minimap/version/irjR7dHk) |
| U100 · 界面、工具与多人 | **Xaero's World Map**<br>Xaero 世界地图（全图浏览与标记） | neoforge-1.21.1-1.47.0 | **版本标签核对；整包待测**<br> Xaero 小地图/世界地图保留；FTB Chunks 主要负责领地/加载，重复地图界面可在配置层处理。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/xaeros-world-map/version/SnQ4SoDe) |
| U101 · 界面、工具与多人 | **FTB Library**<br>FTB 系列基础库 | 2101.1.37 | **CurseForge 发布记录已核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://www.curseforge.com/minecraft/mc-mods/ftb-library-forge/files/all) |
| U102 · 界面、工具与多人 | **FTB Quests**<br>任务/进度系统 | 2101.1.36 | **CurseForge 发布记录已核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge/files/all) |
| U103 · 界面、工具与多人 | **FTB Chunks**<br>区块认领/加载 | 2101.1.22 | **CurseForge 发布记录已核对；整包待测**<br> Xaero 小地图/世界地图保留；FTB Chunks 主要负责领地/加载，重复地图界面可在配置层处理。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge/files/all) |
| U104 · 界面、工具与多人 | **FTB Teams**<br>队伍/权限管理 | 2101.1.11 | **CurseForge 发布记录已核对；整包待测**<br><br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-forge/files/all) |
| U107 · 音效 | **Sound Physics: Aeronautics**<br>航空载具音效支持 | 2.0.1 | **版本标签核对；整包待测**<br>独立替代分支，不是需要原版的附属；移除 U106 后再安装。JAR 要求 Create [6.0.10,6.0.11)，当前 6.0.10 满足。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sound-physics-aeronautics/version/MDdwFCrR) |

## 待定

| 编号 / 原分类 | Mod / 简介 | 选定版本 | 安装条件与处理原因 | 官方依据 |
|---|---|---|---|---|
| U083 · Voxy 远景渲染 | **Voxy 服务端组件（原文件身份待核）**<br>Voxy 服务端组件 | 未核实 | **暂缓安装：未核实文件**<br>与多种同名 Voxy Server Side / LOD Server Support 项目容易混淆。现有发布列表未精确匹配此无版本文件名；需原 JAR 或下载地址。不要与 U084 同时默认启用。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | 原表；发布来源待核 |
| U084 · Voxy 远景渲染 | **Voxy Server LOD（原文件身份待核）**<br>服务端 LOD 生成 | 未核实 | **暂缓安装：未核实文件**<br>未找到可可靠匹配 1.1.4 的官方记录；不能用其他同名项目的兼容性代替。与 U083 属同类，待识别后择一；备选 B 项提供已核实路线。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | 原表；发布来源待核 |
| U087 · 世界生成与结构 | **Terralith**<br>大型地形生成（山脉、生物群系） | 2.6.2 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/terralith/version/IY93YaEe) |
| U088 · 世界生成与结构 | **Tectonic**<br>地形/地层增强 | 3.0.28-neoforge-21.1 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/tectonic/version/n4iyW7aB) |
| U089 · 世界生成与结构 | **When Dungeons Arise**<br>大型地牢/建筑结构生成 | 2.1.68 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/when-dungeons-arise/version/XIRJSFQ0) |
| U090 · 世界生成与结构 | **When Dungeons Arise: Seven Seas**<br>七海海洋结构 | 1.0.4 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/when-dungeons-arise-seven-seas/version/erk04BGa) |
| U091 · 世界生成与结构 | **YUNG's Better Dungeons**<br>更好的地牢设计 | 1.21.1-NeoForge-5.1.4 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/yungs-better-dungeons/version/D6aZn0Em) |
| U092 · 世界生成与结构 | **YUNG's Better Ocean Monuments**<br>更好的海底神殿 | 1.21.1-NeoForge-4.1.2 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/yungs-better-ocean-monuments/version/yFjEcj2g) |
| U093 · 世界生成与结构 | **Create Aeronautics Structures**<br>额外结构生成 | 1.2 | **暂缓安装：等待用户选定**<br>已精确匹配 Create Aeronautics Structures 1.2；不是按通用文件名猜测。仍遵照世界生成与结构类别暂缓。<br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/aeronautics-structures/version/JWhmlx4p) |
| U098 · 界面、工具与多人 | **Just Enough Items (JEI)**<br>JEI 物品/配方查询 | 19.57.0.451 | **暂缓锁包：发布标签与内部范围矛盾**<br>项目已获用户同意，但原 19.57.0.451 的 JAR 将 minecraft 声明为 [1.21, 1.21.1)，与发布页 1.21.1 标签矛盾；19.64.0.453 也相同。未验证启动，不能直接断言不可运行或擅自放宽范围。保留待核而非当作已证实不兼容删除。MezzConfig 已内置。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/jei/version/RI8WCow6) |
| U108 · 玩法与其他 | **[UNOFFICIAL] TaCZ 1.21.1 NeoForge Port**<br>TACZ 现代枪械模组 | 1.1.8-hotfix-r7 | **暂缓安装：等待用户选定**<br>对应 MUKSC 非官方 1.21.1 NeoForge 移植；不是 TaCZ 原官方 1.20.1 文件。仍按玩法与其他类别待定。<br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/tacz-1.21.1/version/62q6mB5Q) |
| U109 · 玩法与其他 | **Rechiseled**<br>方块雕刻/纹理装饰 | 1.2.6-neoforge-mc1.21 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/rechiseled/version/6muGRqvu) |
| U110 · 玩法与其他 | **Waystones**<br>传送石（快速传送） | 21.1.46+neoforge-1.21.1 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/waystones/version/6Z6MQ6os) |
| U111 · 玩法与其他 | **Aquaculture 2**<br>水产养殖/钓鱼扩展 | 2.7.21 | **暂缓安装：等待用户选定**<br><br>授权：用户明确暂缓：世界生成与结构 / 玩法与其他 | [发布记录](https://modrinth.com/mod/aquaculture/version/5pbz0ETj) |
| D001 · 新增依赖申请 | **Sodium Options API**<br>U077 的发布依赖；未在用户原表，等待批准。 | neoforge-1.21.1-1.0.10 | **等待用户批准；批准后再锁包**<br>U077 的发布依赖；未在用户原表，等待批准。<br>授权：新增依赖/功能主体，未获用户同意 | [发布记录](https://modrinth.com/mod/sodium-options-api/version/lrsX3TMS) |
| D002 · 新增依赖申请 | **Jade 🔍**<br>U069 的 HUD 主体；不是已获同意的独立项目。 | 15.10.6+neoforge | **等待用户批准；批准后再锁包**<br>U069 的 HUD 主体；不是已获同意的独立项目。<br>授权：新增依赖/功能主体，未获用户同意 | [发布记录](https://modrinth.com/mod/jade/version/eYz2YBGT) |
| D003 · 新增依赖申请 | **Create: Ironworks**<br>U074 铜潜水套装完整功能所需；会增加内容，等待用户同意。 | 4.0.3 | **等待用户批准；批准后再锁包**<br>U074 铜潜水套装完整功能所需；会增加内容，等待用户同意。<br>授权：新增依赖/功能主体，未获用户同意 | [发布记录](https://modrinth.com/mod/create-ironworks/version/91r9PehB) |

## 备选区

| 编号 / 原分类 | Mod / 简介 | 选定版本 | 安装条件与处理原因 | 官方依据 |
|---|---|---|---|---|
| U105 · 界面、工具与多人 | **Open Parties and Claims**<br>开放领地与队伍保护 | neoforge-1.21.1-0.32.7 | **不与主选同时启用领地功能**<br>同类替代 FTB Chunks 领地路线。主选 FTB Chunks + FTB Teams，服务 FTB Quests/物流；AeroClaims 0.9.4 更新日志和 JAR 均支持 FTB 集成。不是硬性不兼容；若换 OPAC，需同步改领地/团队配置。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/open-parties-and-claims/version/qMR7tHTs) |
| U106 · 音效 | **Sound Physics Remastered**<br>真实声音传播/回声 | neoforge-1.21.1-1.5.1 | **互斥：不可与 U107 同装**<br>主选 Sound Physics Aeronautics；两者为原版与分支，使用相同 modId sound_physics_remastered，作者明确禁止同时安装。<br>授权：用户本次明确同意：原表除两类待定外其余都要 | [发布记录](https://modrinth.com/mod/sound-physics-remastered/version/Dd2tmpsk) |
| B001 · 同类备选 / Tectonic / 海洋地形 | **Deeper Oceans**<br>加深海洋，为大型潜艇提供更大的垂直空间。 | 2.0.1-neoforge-21.1 | **备选，不进入当前安装表**<br>更深海底候选；地形方案整体待定。 作者明确不兼容 Tectonic；与其他海床生成器择一测试。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/deeper-oceans/version/rHJ7RgjW) |
| B002 · 同类备选 / Tectonic / 海洋比例 | **Continents**<br>将陆地分成相隔更远的大陆，形成宽阔海洋。 | 1.1.14 | **备选，不进入当前安装表**<br>大陆与海域分布路线；不是直接叠加全部地形模组。 可作为大洋布局候选；Large Biomes 与 Terralith 的组合有作者限制。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/continents/version/bVXaqvWr) |
| B003 · 同类备选 / Tectonic / 海洋世界 | **Oceanus**<br>离中心越远越趋向无尽海洋的世界生成方案。 | v2.1.0+mod | **备选，不进入当前安装表**<br>海洋化地形替代路线，需与深海维度区分。 默认内圈约 5000 格、过渡约 2000 格；并非出生点全海洋。项目停止维护。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/oceanus/version/EAKpVSjV) |
| B004 · 同类备选 / Terralith / 群系 | **Regions Unexplored**<br>扩展生态群系、植被与方块。 | 0.6.2-neoforge-21.1 | **备选，不进入当前安装表**<br>增加模组群系的另一条路线。 辅助海岛生态；会增加内容和群系分布调参工作。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/regions-unexplored/version/5A8LFnXX) |
| B005 · 同类备选 / Terralith / 群系 | **Biomes O' Plenty**<br>超多生物群系；增加群系、植物和建筑材料。 | 21.1.0.14 | **备选，不进入当前安装表**<br>与 Regions Unexplored 等对比选型，不能当成已批准。 辅助岛屿景观；与海洋主导生成规则联调。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/biomes-o-plenty/version/BtZKRp69) |
| B006 · 同类备选 / 海洋战斗内容 | **Aquamirae**<br>冰海、船墓地和海洋怪物，含 Captain Cornelia 战斗。 | 7.2.12 | **备选，不进入当前安装表**<br>此前主题初筛候选；补充海洋 Boss，待玩法方案批准。 狭窄冰迷宫更适合离艇探险；新版依赖 Fragmentum 与 GeckoLib。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/aquamirae/version/RSbxcVPE) |
| B007 · 同类备选 / 海洋战斗内容 | **L_Ender's Cataclysm**<br>灾变；大型地牢和 Boss，包含海洋探索内容。 | 3.33 | **备选，不进入当前安装表**<br>含海洋 Boss 的战斗内容候选；不是已确认加入。 也加入陆地与其他维度内容；战利品强度与船体战斗另测。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/l_enders-cataclysm/version/PsPYpoCC) |
| B008 · 同类备选 / Aquaculture / 海洋生物 | **Hybrid Aquatic**<br>鱼类、鲨鱼、甲壳类等水生生态和潜水内容。 | mc1.21.1-1.7.4-neoforge | **备选，不进入当前安装表**<br>扩充海洋生态，与钓鱼系统侧重点不同，可择需补充。 前置较多，含 Kotlin for Forge；与其他生态模组控制总刷怪量。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/hybrid-aquatic/version/jdOU7Pzu) |
| B009 · 同类备选 / Aquaculture / 钓鱼 | **Tide 2**<br>扩展钓鱼、鱼类与钓具进度。 | 2.1.1 | **备选，不进入当前安装表**<br>钓鱼生态替代/补充方案，玩法仍未定。 不应把所有钓获物都当作可见游动生物。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/tide/version/1ki6UyqV) |
| B010 · 同类备选 / YUNG 海底神殿 | **Dungeons and Taverns Ocean Monument Overhaul**<br>Dungeons and Taverns 的海底神殿重构。 | v1.2+mod | **备选，不进入当前安装表**<br>同一原版结构的重做路线，默认择一，不能无脑重叠。 与 YUNG 同类重做择一，不默认叠加。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/dungeons-and-taverns-ocean-monument-overhaul/version/SFXYDC6X) |
| B011 · 同类备选 / 世界生成与结构 | **Hopo Better Underwater Ruins**<br>扩展海底废墟、城市遗迹与考古探索。 | 1.2.1b | **备选，不进入当前安装表**<br>海底遗迹候选；与神殿不是硬互斥。 检查深海床生成高度、遗迹密度与战利品。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/hopo-better-underwater-ruins/version/cCUmdH6y) |
| B012 · 同类备选 / 世界生成与结构 | **Awesome Dungeon Ocean**<br>海洋地牢、船只和水下遗迹。 | 3.3.0 | **备选，不进入当前安装表**<br>海洋地牢候选，需控制结构密度。 结构船通常是静态遗迹，不能默认直接驾驶。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/awesome-dungeon-edition-ocean/version/4oZY86TA) |
| B013 · 同类备选 / 世界生成与结构 | **MOS - Moog's Ocean Structures**<br>大量海洋结构和探索地点。 | 1.1.1 | **备选，不进入当前安装表**<br>海洋结构候选，具体组合待定。 多结构包并用需降低生成密度，保留潜艇航道。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/mos-moogs-ocean-structures/version/QfMITqh9) |
| B014 · 同类备选 / 船体装饰 | **Create Deco**<br>工业建筑装饰，适合潜艇与港口。 | 2.1.3 | **备选，不进入当前安装表**<br>Create Framed / Interiors / Rechiseled 的同类补充，非硬互斥。 船壳装饰是否密封需按方块实测。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/create-deco/version/qrcMVoBD) |
| B015 · 同类备选 / 船体装饰 | **Create: Design n' Decor**<br>工业设计与装饰方块。 | 2.2b | **备选，不进入当前安装表**<br>同类装饰扩展，模型和 Sable 兼容另测。 运动船体上配套航空学兼容补丁候选。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/create-design-n-decor/version/uQsIRky8) |
| B016 · 同类备选 / 潜水装备 | **Create: Aquatic Ambitions**<br>海洋材料与机械动力加工联动，提供海洋资源循环。 | 1.21.1-6.0.10-2.0.4 | **备选，不进入当前安装表**<br>Create 潜水装备扩展，可与压力玩法比较。 2.x 要求 Create 至少 6.0.6；与 Upgrade Aquatic 可联动。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/create-aquatic-ambitions/version/DoI3PpXj) |
| B017 · 同类备选 / CC 船载控制 | **CC: Deep Seas**<br>Deep Seas 压载、推进及舱室数据的电脑外设。 | 1.1.1 | **备选，不进入当前安装表**<br>与 Avionics / CC:Sable 为同类接口补充，不等于开箱即用 AI。 适合潜艇仪表；自动驾驶仍需自行编程与测试。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/cc-deep-seas/version/qKkaeGSS) |
| B018 · 同类备选 / 船内控制布线 | **Create: Aero Cables**<br>运动结构之间连接电力和数据网络。 | 1.21-0.4.0 | **备选，不进入当前安装表**<br>与 Drive-By-Wire 的布线目标相近，具体信号类型不同。 作者覆盖 FE、AE2、RS、CC 等；不能据此推断支持全部交错电网导线。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/create-aero-cables/version/Wn81ihHo) |
| B019 · 同类备选 / 交错电网附属 | **Create: Power Components**<br>交错电网附加电力元件。 | 1.3.0 | **备选，不进入当前安装表**<br>同类电力组件补充；不是用户已批准的 Power Grid 本体。 核对 Power Grid 版本与移动船体连接。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/create-power-components/version/W3GV5Ldp) |
| B020 · 同类备选 / 载具操控 | **Aeronautics: Calibrated**<br>航空学动力、控制和导航相关设备。 | 1.4.0 | **备选，不进入当前安装表**<br>与 G&G / Gyro 等比较可配置控制功能。 多套推进方案并用需统一能耗与控制逻辑。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/aeronautics-calibrated/version/78Nl65uk) |
| B021 · 同类备选 / 船坞升降机构 | **Create: Lift n' Load ／  Simulated/Aeronautics Addon**<br>吊运、装卸及载具建造工具扩展。 | 1.0c | **备选，不进入当前安装表**<br>与 Physics Gantry / G&G 比较，非硬冲突。 适合港口补给与装配；部件运动行为需联调。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/create-lift-n-load/version/G7N1k717) |
| B022 · 同类备选 / 潜水装备 | **Simple Dive Gear**<br>独立潜水装备，提供离艇探索能力。 | 10.0.0 | **备选，不进入当前安装表**<br>可替代部分潜水装备体验，压力联动仍待设计。 调整持续时间与成本，避免跳过氧气进度。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/simple-dive-gear/version/4ZexmS2I) |
| B023 · 同类备选 / RS / 抽屉存储 | **Sophisticated Storage**<br>精妙存储；船内仓储和过滤。 | 1.21.1-1.6.5.2176 | **备选，不进入当前安装表**<br>升级箱体路线；不是 RS 网络的等价替换。 移动船体中方块实体、升级和自动化接口待测。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/sophisticated-storage/version/EvzAB4E6) |
| B024 · 同类备选 / PonderJS / 教程展示 | **Patchouli**<br>制作航海日志、潜艇建造手册与生物图鉴。 | 1.21.1-93-neoforge | **备选，不进入当前安装表**<br>书本教程路线，和场景教程可互补，但尚未批准。 手册内容需自行编写。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/patchouli/version/BIogJv2D) |
| B025 · 同类备选 / 载具 | **Small Ships**<br>小型可驾驶船只，作为初期航行或救援艇。 | 2.0.1-NeoForge | **备选，不进入当前安装表**<br>预制小船作为早期交通备选，不替代自建大型潜艇。 不是巨型自由拼装潜艇；Deep Sea 的船损规则需检查。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/small-ships/version/QNjKZdCr) |
| B026 · 同类备选 / 载具 | **Immersive Aircraft**<br>独立飞机类载具，提供空中运输备选。 | 1.6.0+1.21.1 | **备选，不进入当前安装表**<br>预制飞机路线；与自建载具定位不同，先备选。 可能绕过海上探索路线，属于可选内容。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/immersive-aircraft/version/ZCSI1mBS) |
| B027 · 同类备选 / Voxy + Roxy | **Distant Horizons**<br>远景 LOD，适合远洋与大型岛屿视野。 | 3.3.3-1.21.1 | **备选，不进入当前安装表**<br>另一套远景渲染路线；主客户端择一进行基准测试。 不保证运动船体进入远景；配合光影时单独测试。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/distanthorizons/version/9w34y8ai) |
| B028 · 同类备选 / Voxy World Gen V2 | **Chunky**<br>提前生成区块，降低远航途中生成卡顿。 | 1.4.23 | **备选，不进入当前安装表**<br>区块预生成工具替代；不直接替代 Voxy 渲染，勿并行重复生成。 确定最终地形后再预生成；消耗磁盘与生成时间。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/chunky/version/LuFhm4eU) |
| B029 · 同类备选 / 光影兼容 | **Iris & Oculus Flywheel Compat**<br>Iris/Oculus 与 Flywheel 渲染适配。 | 1.21.1+2.4.0-release | **备选，不进入当前安装表**<br>可选光影路线；Deep Seas JAR 未硬要求，未获同意不默认安装。 补丁不代表所有光影都能兼容 Deep Seas。<br>授权：此前初筛候选；未获用户同意 | [发布记录](https://modrinth.com/mod/iris-flw-compat/version/6ZQz5Ow2) |
| B030 · 同类备选 / 动态光照 | **Sable Dynamic Lights (Create: Dynamic Lights fork)**<br>另一套基于 LambDynamicLights 的分支；替换 Create Sable Dynamic Lights + Sodium Dynamic Lights 整条路线，不能叠加同名核心；新引擎前置也需批准。 | 2.0.1 | **备选，不进入当前安装表**<br>另一套基于 LambDynamicLights 的分支；替换 Create Sable Dynamic Lights + Sodium Dynamic Lights 整条路线，不能叠加同名核心；新引擎前置也需批准。<br>授权：新检索到的同类路线；未获同意 | [发布记录](https://modrinth.com/mod/sable-dynamic-lights/version/vjb0hAWB) |
| B031 · 同类备选 / 服务端远景传输 | **LOD Server Support**<br>已核实的 1.21.1 NeoForge 路线，官方说明支持 Roxy；可代替身份待核的 U083/U084，属于新项目批准，不能冒认为原文件。 | v0.18.0+neoforge+mc1.21.1 | **备选，不进入当前安装表**<br>已核实的 1.21.1 NeoForge 路线，官方说明支持 Roxy；可代替身份待核的 U083/U084，属于新项目批准，不能冒认为原文件。<br>授权：新检索到的同类路线；未获同意 | [发布记录](https://modrinth.com/mod/lod-server-support/version/4WpqxQiA) |

## 剔除与调整记录

| 项目 | 操作 | 原因 | 依据 |
|---|---|---|---|
| U106 / Sound Physics Remastered 1.5.1 | 从主选移入备选 | 与主选 Sound Physics Aeronautics 同 modId，作者明确禁止同装。不是不支持 1.21.1 NeoForge。 | [来源](https://modrinth.com/mod/sound-physics-aeronautics) |
| U045 / G&G 2.0.0-Beta-1.1 | 剔除原文件；同项目改选 1.2.10 release | 作者明确标记此 Beta 不应放进整合包或重要世界。属于整包适用性限制，不是 MC/加载器错误。 | [来源](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos/version/9ci2hEic) |
| U105 / Open Parties and Claims | 移入备选 | 与 FTB Chunks 领地功能重复，保留 FTB 主路线。不是证实的运行冲突。 | [来源](https://modrinth.com/mod/aeroclaims/version/dWMRSgEu) |
| U098 / JEI 19.57.0.451 | 待定核查；不作为已证实不兼容删除 | 发布标签与 JAR 内 minecraft 范围矛盾；不擅自改文件或依赖范围。 | [来源](https://modrinth.com/mod/jei/version/RI8WCow6) |
| U083、U084 / 两个 Voxy 服务端文件 | 待定核查；暂停选择具体文件 | 未锁定官方对应文件；未知不等于不兼容。需要原 JAR/下载来源后判断同类关系。 | 原表文件名；无可靠精确发布匹配 |

## 同类选择的边界

存储网络与抽屉、不同加工/发电附属、控制器与电脑接口、地图与领地工具不因功能相近就全部互相替代。表内只有作者明确禁止同装或相同 modId 的情况标为硬互斥；“备选”也可以是尚未批准的同类补充，不等于不兼容。

当前压力模组控制的是玩家深潜惩罚，不是潜艇外壳承压；动态光照、渲染包围盒、物理标签也不等于洋流速度锁、通道体积锁。自研机制仍按 [游戏机制文档](game-mechanics/README.md) 单独规划。

此轮未安装游戏实例或运行联调，不能承诺任意组合都能启动。下一步先处理表内待核文件和新增依赖批准，再验证核心装配、潜航、移动船内存储、炮击、多人领地、声音和远景性能；未经同意的新增 Mod 不会自动转入确定区。
