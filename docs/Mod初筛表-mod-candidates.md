# 深蓝远航：Mod 初筛表

核对日期：2026-10-13。目标：**Minecraft 1.21.1 + NeoForge + Java 21**。共 **170 项**：161 个模组项目、9 个数据包。

按最后要求，不列旧 MC 版本、仅 Fabric/Forge 的项目或尚未发布目标版本的计划。模组以具体文件同时标注 1.21.1 和 NeoForge 为准；数据包独立标明，属于世界生成/内容补充。此表是尽量广泛的候选池，不是建议全部安装的模组清单。

**版本文件存在不等于整包兼容。** 尚未下载 JAR 验证、启动游戏或测试多人服务器。备注中的“待测/建议”是整合判断，不代表已知冲突。依赖列只记录文件页列出的直接必需项，不保证列全传递依赖或版本范围。发布日期和 release/beta/alpha 取所列文件，而非整个项目的最新版本。

## 先关注的结论

- 潜艇主线：Create → Sable / Create Aeronautics → Create Deep Seas；交错电网负责船载电力候选。Deep Seas 仍在测试阶段，其 The Abyss 群系是未来计划，海洋生态需要另补。
- Deep Sea 与 Create Deep Seas 是不同项目：前者限制普通船的远洋生存，后者才是当前潜艇方案。
- 大洋布局与水深需要分开解决。Continents / Oceanus / Oceanographic 是布局候选；Deeper Oceans 与 Tectonic 明确不能一起使用。Abyssia 能扩展更深层，但文档与文件版本矛盾，优先验包再判断。
- 大型海怪存在不代表能与 Sable 船壳正确战斗；需验证寻路、命中、碰撞、爆炸、船体损伤和多人同步。
- 首轮潜艇验证建议关闭光影。航空学与 Deep Seas 都有 Iris 相关说明，兼容桥不能保证所有光影正常。

依据：[Deep Seas](https://modrinth.com/mod/create-deep-seas)、[航空学](https://modrinth.com/mod/create-aeronautics)、[Deeper Oceans](https://modrinth.com/mod/deeper-oceans)、[交错电网更新记录](https://modrinth.com/mod/power-grid/versions)。

## 分类索引

| 分类 | 数量 |
| --- | ---: |
| 核心潜艇 | 5 |
| 海洋地形 | 16 |
| 海洋维度 | 3 |
| 海洋Boss | 7 |
| 海洋生态 | 18 |
| 钓鱼食物 | 6 |
| 海上遗迹 | 20 |
| 船体建造 | 9 |
| 动力工业 | 6 |
| 船载控制 | 23 |
| 兼容与调试 | 9 |
| 潜水生存 | 12 |
| 后勤与任务 | 14 |
| 其他载具 | 3 |
| 画面与氛围 | 10 |
| 性能与光影 | 9 |

## 核心潜艇

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 1 | [Create](https://modrinth.com/mod/create)（模组） | 机械动力；动力、加工、自动化与船内机械基础。 | [6.0.10+mc1.21.1](https://modrinth.com/mod/create/version/UjX6dr61) · 1.21.1 / NeoForge · release · 2026-04-21 | 文件页未列必需项；装配时核对 | 核心；附属仍需核对 Create 6.x 的具体要求。 |
| 2 | [Create Aeronautics](https://modrinth.com/mod/create-aeronautics)（模组） | 航空学；让方块建造物成为可运动、可交互的物理载具。 | [1.3.2+mc1.21.1](https://modrinth.com/mod/create-aeronautics/version/44pLdPGg) · 1.21.1 / NeoForge · release · 2026-08-29 | Sable；Create | 核心；作者提示 Iris 存在视觉问题，大船需测服务器性能。 |
| 3 | [Sable](https://modrinth.com/mod/sable)（模组） | 航空学所用的运动子世界与物理框架。 | [2.0.6+mc1.21.1](https://modrinth.com/mod/sable/version/fg9dTRz9) · 1.21.1 / NeoForge · release · 2026-10-03 | 文件页未列必需项；装配时核对 | 核心前置；侵入性改动较多，其他模组的方块实体需逐类测试。 |
| 4 | [Create Deep Seas](https://modrinth.com/mod/create-deep-seas)（模组） | 深海潜航；潜艇、压载、推进、密封舱和水下生存机制。 | [3.3.0](https://modrinth.com/mod/create-deep-seas/version/8CV1LRTl) · 1.21.1 / NeoForge · release · 2026-10-02 | Create；Sable；Iris & Oculus Flywheel Compat；Create Aeronautics | 核心；项目仍属 BETA。The Abyss 是未来计划，不能当作已发布群系。Iris/Veil 有渲染问题；版本依赖页列出 Iris & Oculus Flywheel Compat，装配时核对。 |
| 5 | [Create: Power Grid](https://modrinth.com/mod/power-grid)（模组） | 交错电网；导线、电路、电力设备与机械动力联动。 | [0.6.2](https://modrinth.com/mod/power-grid/version/jCkPHjmP) · 1.21.1 / NeoForge · release · 2026-09-14 | Architectury API；Create | 核心；0.5.5 起加入 Sable 兼容，0.6.1 修复运动结构相关崩溃；仍测装配、拆船和跨船导线。 |

## 海洋地形

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 6 | [Deeper Oceans](https://modrinth.com/mod/deeper-oceans)（模组） | 加深海洋，为大型潜艇提供更大的垂直空间。 | [2.0.1-neoforge-21.1](https://modrinth.com/mod/deeper-oceans/version/rHJ7RgjW) · 1.21.1 / NeoForge · release · 2026-06-28 | Lithostitched | 作者明确不兼容 Tectonic；与其他海床生成器择一测试。 |
| 7 | [Tectonic](https://modrinth.com/mod/tectonic)（模组） | 大尺度大陆、岛屿和更深海洋，也改变陆地山谷。 | [3.0.28-neoforge-21.1](https://modrinth.com/mod/tectonic/version/n4iyW7aB) · 1.21.1 / NeoForge · release · 2026-09-11 | Lithostitched | 不等于海洋世界；与 Deeper Oceans 二选一，海陆比例另调。 |
| 8 | [Continents](https://modrinth.com/mod/continents)（模组） | 将陆地分成相隔更远的大陆，形成宽阔海洋。 | [1.1.14](https://modrinth.com/mod/continents/version/bVXaqvWr) · 1.21.1 / NeoForge · release · 2026-07-08 | 文件页未列必需项；装配时核对 | 可作为大洋布局候选；Large Biomes 与 Terralith 的组合有作者限制。 |
| 9 | [Oceanographic](https://modrinth.com/datapack/oceanographic)（数据包） | 海洋与陡峭高岛地形数据包，面向航行探索。 | [1.1.3](https://modrinth.com/datapack/oceanographic/version/mHr854qx) · 1.21.1 / 数据包 · release · 2026-05-05 | 文件页未列必需项；装配时核对 | 数据包；高度范围 -64 至 352，不是海底深至 -352。新存档测试。 |
| 10 | [Oceanographic: Fractured](https://modrinth.com/datapack/oceanographic-fractured)（数据包） | Oceanographic 的海洋裂隙变体，强调巨大海沟。 | [1.1.2](https://modrinth.com/datapack/oceanographic-fractured/version/q0FM91QR) · 1.21.1 / 数据包 · release · 2026-05-05 | 文件页未列必需项；装配时核对 | 数据包；作为原版 Oceanographic 的替代方案，不默认叠加。 |
| 11 | [Oceanus](https://modrinth.com/mod/oceanus)（模组） | 离中心越远越趋向无尽海洋的世界生成方案。 | [v2.1.0+mod](https://modrinth.com/mod/oceanus/version/EAKpVSjV) · 1.21.1 / NeoForge · release · 2025-08-15 | More Density Functions | 默认内圈约 5000 格、过渡约 2000 格；并非出生点全海洋。项目停止维护。 |
| 12 | [Oceanus+Continents](https://modrinth.com/datapack/oceanus-pluscontinents)（数据包） | Oceanus 与 Continents 的组合地形方案。 | [v1.0.0](https://modrinth.com/datapack/oceanus-pluscontinents/version/JCjW8ALg) · 1.21.1 / 数据包 · release · 2025-08-11 | 文件页未列必需项；装配时核对 | 数据包；与独立版的覆盖顺序和重复配置需核对。 |
| 13 | [[World Generator] Ocean Only Water World!](https://modrinth.com/datapack/world-generator-ocean-only-water-world)（数据包） | 全海洋世界生成数据包，提供海上生存背景。 | [1.21.1](https://modrinth.com/datapack/world-generator-ocean-only-water-world/version/IQ0aJjL5) · 1.21.1 / 数据包 · release · 2024-10-19 | 文件页未列必需项；装配时核对 | 数据包；检查出生点安全、木材及前期矿物来源。 |
| 14 | [Water World (Only Ocean)](https://modrinth.com/datapack/water-world-data-pack)（数据包） | 以水世界替换大面积陆地的生成数据包。 | [1.0.0](https://modrinth.com/datapack/water-world-data-pack/version/3juD6Rb5) · 1.21.1 / 数据包 · release · 2026-04-25 | 文件页未列必需项；装配时核对 | 数据包；与其他主世界生成器择一；需设计开局资源。 |
| 15 | [Ocean World Dimension](https://modrinth.com/mod/ocean-world-dimension)（模组） | 增加独立海洋维度，保留主世界作为补给地。 | [2.0.0-1.21.1](https://modrinth.com/mod/ocean-world-dimension/version/lkyuZrVk) · 1.21.1 / NeoForge · release · 2025-11-28 | Custom Portal API Reforged | 需要门户前置；整艘 Sable 潜艇跨维度能力未确认。 |
| 16 | [Terralith](https://modrinth.com/mod/terralith)（模组） | 扩展原版风格地貌和群系，丰富岛屿与海岸。 | [2.6.2](https://modrinth.com/mod/terralith/version/IY93YaEe) · 1.21.1 / NeoForge · release · 2026-06-09 | Lithostitched | 辅助地形；不会自动使海洋主导，结构落点需测试。 |
| 17 | [Regions Unexplored](https://modrinth.com/mod/regions-unexplored)（模组） | 扩展生态群系、植被与方块。 | [0.6.2-neoforge-21.1](https://modrinth.com/mod/regions-unexplored/version/5A8LFnXX) · 1.21.1 / NeoForge · release · 2026-07-12 | Lithostitched | 辅助海岛生态；会增加内容和群系分布调参工作。 |
| 18 | [Biomes O' Plenty](https://modrinth.com/mod/biomes-o-plenty)（模组） | 超多生物群系；增加群系、植物和建筑材料。 | [21.1.0.14](https://modrinth.com/mod/biomes-o-plenty/version/BtZKRp69) · 1.21.1 / NeoForge · beta · 2026-06-20 | GlitchCore；TerraBlender | 辅助岛屿景观；与海洋主导生成规则联调。 |
| 19 | [William Wythers' Overhauled Overworld](https://modrinth.com/mod/wwoo)（模组） | William Wythers 的主世界地貌重构。 | [2.6.7](https://modrinth.com/mod/wwoo/version/sU7oaRZv) · 1.21.1 / NeoForge · release · 2026-05-28 | Cristel Lib | 地形备选；不要默认与多种密度函数重写同时启用。 |
| 20 | [Abyssal Ocean](https://www.curseforge.com/minecraft/mc-mods/abyssal-ocean)（模组） | 海沟延伸至 Y=-56，增加深海地形空间。 | [2.2.0](https://www.curseforge.com/minecraft/mc-mods/abyssal-ocean/files) · 1.21.1 / NeoForge · release · 2026-09-18 | 页面称无额外前置 | 作者声称支持 Tectonic、Terralith 等，尚未整包实测；与其他海床改造共同测试。 |
| 21 | [Abyssia](https://www.curseforge.com/minecraft/mc-mods/abyssia)（模组） | 增加 Y=-64 至 -368 的连续深海层、17 种深海群系及水生生态。 | [1.0.0](https://www.curseforge.com/minecraft/mc-mods/abyssia/files) · 1.21.1 / NeoForge · release · 2026-10-06 | 页面说明不一致；以 JAR 元数据复核 | 文件明确为 1.21.1 NeoForge，但介绍要求误写 1.20.1 Forge，需重点验包。必须新存档；裂隙能否容纳巨艇、水压与 Deep Seas 叠加待测。 |

## 海洋维度

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 22 | [Beyond the Ocean](https://modrinth.com/mod/beyond-the-ocean)（模组） | 受深海迷航启发的海洋星球、探索与 Boss 进度。 | [1.1.0](https://modrinth.com/mod/beyond-the-ocean/version/KiTxGHYs) · 1.21.1 / NeoForge · release · 2026-06-21 | 文件页未列必需项；装配时核对 | 偏末地之后的独立探索线；测试潜艇跨维度与进度整合。 |
| 23 | [Abyssal Planet](https://www.curseforge.com/minecraft/mc-mods/abyssal-planet)（模组） | 独立海洋维度、深海区域、克拉肯和深海蛇等怪物。 | [0.2.7](https://www.curseforge.com/minecraft/mc-mods/abyssal-planet/files) · 1.21.1 / NeoForge · release · 2026-09-06 | 以文件关系页为准 | 核对潜艇跨维度、水压及海流叠加；不会直接把主世界变成海洋。 |
| 24 | [Alex’s Caves (Unofficial Port)](https://www.curseforge.com/minecraft/mc-mods/alexs-caves-unofficial-port)（模组） | Alex 洞穴的社区移植，包含深海洞穴生态与探索内容。 | [1.21.1-2.0.10](https://www.curseforge.com/minecraft/mc-mods/alexs-caves-unofficial-port/files) · 1.21.1 / NeoForge · release · 2026-04-25 | Citadel (Unofficial Port) | 非官方移植；不是连续大洋生成器。航空学兼容补丁涉及其寻路问题，需配套测试。 |

## 海洋Boss

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 25 | [Aquamirae](https://modrinth.com/mod/aquamirae)（模组） | 冰海、船墓地和海洋怪物，含 Captain Cornelia 战斗。 | [7.2.12](https://modrinth.com/mod/aquamirae/version/RSbxcVPE) · 1.21.1 / NeoForge · release · 2026-10-11 | Geckolib；Fragmentum | 狭窄冰迷宫更适合离艇探险；新版依赖 Fragmentum 与 GeckoLib。 |
| 26 | [L_Ender's Cataclysm](https://modrinth.com/mod/l_enders-cataclysm)（模组） | 灾变；大型地牢和 Boss，包含海洋探索内容。 | [3.33](https://modrinth.com/mod/l_enders-cataclysm/version/PsPYpoCC) · 1.21.1 / NeoForge · release · 2026-09-20 | Lionfish-API；Curios API | 也加入陆地与其他维度内容；战利品强度与船体战斗另测。 |
| 27 | [Myths of The Sea](https://modrinth.com/mod/myths-of-the-sea)（模组） | 利维坦、克拉肯等海洋神话生物与坐骑。 | [1.3.0](https://modrinth.com/mod/myths-of-the-sea/version/veASg7rX) · 1.21.1 / NeoForge · release · 2026-07-13 | 文件页未列必需项；装配时核对 | 与其他巨型海怪的刷怪密度、战利品及难度需要统一。 |
| 28 | [Kraken Mod](https://modrinth.com/mod/kraken-mod)（模组） | 克拉肯 Boss 与海底宝库探索。 | [1.0.0](https://modrinth.com/mod/kraken-mod/version/wZzWxe4L) · 1.21.1 / NeoForge · release · 2024-12-09 | 文件页未列必需项；装配时核对 | 检查召唤、钥匙和奖励进度；不默认能攻击物理船体。 |
| 29 | [Krakentilus](https://modrinth.com/mod/krakentilus)（模组） | 海床伪装的巨型鹦鹉螺 Boss、坐骑及武器。 | [1.108](https://modrinth.com/mod/krakentilus/version/A640sCnx) · 1.21.1 / NeoForge · release · 2025-11-11 | 文件页未列必需项；装配时核对 | 适合作为海床遭遇；测命中箱、生成空间和水下战斗。 |
| 30 | [Bosses'Rise](https://modrinth.com/mod/bossesrise)（模组） | Bosses'Rise；包含深海诅咒船与 Nerakyss 等 Boss。 | [2.1.2](https://modrinth.com/mod/bossesrise/version/lE9PF6Wp) · 1.21.1 / NeoForge · release · 2026-05-21 | Geckolib | 不只海洋 Boss；可按群系标签限制相关结构分布。 |
| 31 | [Sea Myths: Legendary Ocean Monsters & Bosses Creatures](https://www.curseforge.com/minecraft/mc-mods/sea-myths)（模组） | Sea Eater、El Gran Maja 等巨型海怪和强力装备。 | [2.0.0+mc1.21.1](https://www.curseforge.com/minecraft/mc-mods/sea-myths/files) · 1.21.1 / NeoForge · release · 2026-10-08 | 以文件关系页为准 | 与 Myths of The Sea 不同项目；巨型实体体积、数值和多人服性能需重点测试。页面环境标注 Client，服务端适用性需验包。 |

## 海洋生态

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 32 | [Hybrid Aquatic](https://modrinth.com/mod/hybrid-aquatic)（模组） | 鱼类、鲨鱼、甲壳类等水生生态和潜水内容。 | [mc1.21.1-1.7.4-neoforge](https://modrinth.com/mod/hybrid-aquatic/version/jdOU7Pzu) · 1.21.1 / NeoForge · release · 2026-10-04 | Geckolib；Hybrid API [HAPI]；Lithostitched；Kotlin for Forge | 前置较多，含 Kotlin for Forge；与其他生态模组控制总刷怪量。 |
| 33 | [Upgrade Aquatic](https://modrinth.com/mod/upgrade-aquatic)（模组） | 水域升级；水生物、珊瑚、材料和水下生态扩展。 | [7.0.2](https://modrinth.com/mod/upgrade-aquatic/version/9zf6y4e4) · 1.21.1 / NeoForge · release · 2026-09-16 | Blueprint | Blueprint 前置；可与 Aquatic Ambitions 的资源加工联动。 |
| 34 | [Unusual Fish](https://modrinth.com/mod/unusual-fish)（模组） | 奇幻鱼类、海洋生物及相关装备和食物。 | [1.21.1-1.1.13](https://modrinth.com/mod/unusual-fish/version/zyAcoKq7) · 1.21.1 / NeoForge · release · 2026-06-26 | Geckolib | 适合奇幻海底；刷怪与掉落配方需统一。 |
| 35 | [Aquatic Frontiers](https://modrinth.com/mod/aquatic-frontiers)（模组） | 增加乌贼、珊瑚相关生物等水下生态。 | [1.7](https://modrinth.com/mod/aquatic-frontiers/version/Gb36Ydxp) · 1.21.1 / NeoForge · release · 2024-11-12 | Geckolib | 需 GeckoLib；与类似大型乌贼内容存在主题重叠。 |
| 36 | [Aquatic Fauna Expansion](https://modrinth.com/mod/aquatic-fauna-expansion)（模组） | 新增水生动物，补充海洋生物种类。 | [1.0.0](https://modrinth.com/mod/aquatic-fauna-expansion/version/KTefLTt3) · 1.21.1 / NeoForge · beta · 2025-11-20 | 文件页未列必需项；装配时核对 | 介绍信息较少；物种行为、服务器运行与生成密度待测。 |
| 37 | [Oceanlife](https://modrinth.com/mod/oceanlife)（模组） | 鱼类和鲨鱼等海洋动物。 | [1.0.0](https://modrinth.com/mod/oceanlife/version/OOxkTFjc) · 1.21.1 / NeoForge · release · 2026-06-30 | 文件页未列必需项；装配时核对 | 与其他鲨鱼模组存在内容重叠；联调刷怪权重。 |
| 38 | [Sea Life](https://modrinth.com/mod/new-sea-life)（模组） | 海洋鱼类、捕捞、养殖及相关食物。 | [21.1.0](https://modrinth.com/mod/new-sea-life/version/dSNJ1iNb) · 1.21.1 / NeoForge · release · 2026-05-21 | Puzzles Lib | 与钓鱼和食物系统统一配方、标签和产量。 |
| 39 | [Aures - Sea Dwellers](https://modrinth.com/mod/aures-sea-dwellers)（模组） | 水下居民、交易与海底村落，提供探索互动。 | [1.0.0](https://modrinth.com/mod/aures-sea-dwellers/version/fHmrQr8A) · 1.21.1 / NeoForge · release · 2026-05-18 | Geckolib | 与水下村庄结构共同测试密度；注意项目转载与署名条款。 |
| 40 | [Sea Creeps](https://modrinth.com/mod/sea-creeps)（模组） | 水下苦力怕类敌人及相关掉落。 | [1.2.5](https://modrinth.com/mod/sea-creeps/version/vwGFGtec) · 1.21.1 / NeoForge · release · 2025-02-13 | Geckolib | 测试爆炸是否伤及船体、密封舱与海底基地。 |
| 41 | [Aquatic Creepers](https://modrinth.com/mod/aquatic-creepers)（模组） | 另一套水生苦力怕敌人。 | [1.0.0](https://modrinth.com/mod/aquatic-creepers/version/2Smosh3T) · 1.21.1 / NeoForge · release · 2025-08-23 | Architectury API；oωo (owo-lib) | 与 Sea Creeps 内容重叠；建议择一或区分生物群系。 |
| 42 | [Aquatic Breeding](https://modrinth.com/mod/aquatic-breeding)（模组） | 扩展水生动物繁殖玩法。 | [1.0.2+neoforge](https://modrinth.com/mod/aquatic-breeding/version/LJl1ctuu) · 1.21.1 / NeoForge · release · 2025-12-13 | 文件页未列必需项；装配时核对 | 测试对新增水生物的实际覆盖范围。 |
| 43 | [Animal Garden - Bull Shark](https://modrinth.com/mod/animal-garden-bull-shark)（模组） | 牛鲨生态。 | [1.1.0-neoforge-1.21.1-21.1.210](https://modrinth.com/mod/animal-garden-bull-shark/version/Of71g6MR) · 1.21.1 / NeoForge · release · 2025-11-16 | 文件页未列必需项；装配时核对 | 与其他鲨鱼来源统一生成和攻击强度。 |
| 44 | [Animal Garden - Narwhal](https://modrinth.com/mod/animal-garden-narwhal)（模组） | 独角鲸，丰富寒冷海域。 | [1.0.0-neoforge-1.21.1-21.1.219](https://modrinth.com/mod/animal-garden-narwhal/version/DwyLYZD4) · 1.21.1 / NeoForge · release · 2026-03-14 | Aquarius Libs | 寒冷群系标签与其他地形包联调。 |
| 45 | [Animal Garden - Harp Seal](https://modrinth.com/mod/animal-garden-harp-seal)（模组） | 竖琴海豹，丰富冰海和海岸。 | [1.0.0-neoforge-1.21.1-21.1.219](https://modrinth.com/mod/animal-garden-harp-seal/version/xpZ6BIIW) · 1.21.1 / NeoForge · release · 2026-02-26 | Aquarius Libs | 需要适合的海岸与陆地活动空间。 |
| 46 | [Animal Garden - Sea Otter](https://modrinth.com/mod/animal-garden-sea-otter)（模组） | 海獭生态。 | [1.1.0-neoforge-1.21.1-21.1.213](https://modrinth.com/mod/animal-garden-sea-otter/version/Oo6zhzrW) · 1.21.1 / NeoForge · release · 2025-11-16 | 文件页未列必需项；装配时核对 | 检查岸边生成和互动行为。 |
| 47 | [Naturalist](https://modrinth.com/mod/naturalist)（模组） | 自然动物生态，补充海岛和近岸环境。 | [2.0.5+1.21.1-neoforge](https://modrinth.com/mod/naturalist/version/ETgOvHHS) · 1.21.1 / NeoForge · release · 2026-09-28 | 文件页未列必需项；装配时核对 | 包含较多陆地内容，属于辅助候选。 |
| 48 | [Alex’s Mobs (Unofficial Port)](https://www.curseforge.com/minecraft/mc-mods/alexs-mobs-1-21-1-port)（模组） | Alex 生物的社区移植，补充陆地与水生动物。 | [1.21.1-1.22.17](https://www.curseforge.com/minecraft/mc-mods/alexs-mobs-1-21-1-port/files) · 1.21.1 / NeoForge · release · 2026-01-26 | Citadel (Unofficial Port) | 非官方移植；航空学相关寻路适配需测试，多生态包统一生成权重。 |
| 49 | [FTB Ocean Mobs](https://www.curseforge.com/minecraft/mc-mods/ftb-ocean-mobs)（模组） | 为危险海洋生存增加水生/两栖敌人。 | [21.1.4](https://www.curseforge.com/minecraft/mc-mods/ftb-ocean-mobs/files) · 1.21.1 / NeoForge · release · 2025-05-17 | GeckoLib | 默认没有自然生成规则和掉落表，需要整合包作者自行设计；不是一套纯 Boss 模组。 |

## 钓鱼食物

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 50 | [Fish of Thieves](https://modrinth.com/mod/fish-of-thieves)（模组） | 盗贼之海风格鱼类与收集玩法。 | [21.1.2.2-neoforge](https://modrinth.com/mod/fish-of-thieves/version/ttj6ROuG) · 1.21.1 / NeoForge · release · 2026-09-30 | Cloth Config API | 与 Tide、Aquaculture 统一钓鱼战利品。 |
| 51 | [Tide 2](https://modrinth.com/mod/tide)（模组） | 扩展钓鱼、鱼类与钓具进度。 | [2.1.1](https://modrinth.com/mod/tide/version/1ki6UyqV) · 1.21.1 / NeoForge · release · 2026-08-03 | Cloth Config API | 不应把所有钓获物都当作可见游动生物。 |
| 52 | [Aquaculture 2](https://modrinth.com/mod/aquaculture)（模组） | 水产养殖；钓具、鱼类和相关装备。 | [2.7.21](https://modrinth.com/mod/aquaculture/version/5pbz0ETj) · 1.21.1 / NeoForge · release · 2026-04-09 | 文件页未列必需项；装配时核对 | 与其他钓鱼模组奖励重叠，需整合战利品表。 |
| 53 | [Farmer's Delight](https://modrinth.com/mod/farmers-delight)（模组） | 农夫乐事；船上厨房与长期航行饮食。 | [1.21.1-1.3.4](https://modrinth.com/mod/farmers-delight/version/XTVZDOol) · 1.21.1 / NeoForge · release · 2026-08-29 | 文件页未列必需项；装配时核对 | 基础食物体系；岛屿农业资源需要可获得。 |
| 54 | [Ocean's Delight](https://modrinth.com/mod/oceans-delight)（模组） | 海洋乐事；海产食谱扩展。 | [1.0.4](https://modrinth.com/mod/oceans-delight/version/ja5Qg1xw) · 1.21.1 / NeoForge · release · 2026-04-17 | 文件页未列必需项；装配时核对 | 需农夫乐事；适合作为海洋资源消费端。 |
| 55 | [Crabber's Delight](https://modrinth.com/mod/crabbers-delight)（模组） | 蟹类捕捞及海鲜料理。 | [1.3.1](https://modrinth.com/mod/crabbers-delight/version/j5Nzh8kh) · 1.21.1 / NeoForge · release · 2026-09-28 | 文件页未列必需项；装配时核对 | 检查海产生成与其他捕捞系统的配方标签。 |

## 海上遗迹

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 56 | [YUNG's Better Ocean Monuments](https://modrinth.com/mod/yungs-better-ocean-monuments)（模组） | 重做海底神殿，扩大探索空间。 | [1.21.1-NeoForge-4.1.2](https://modrinth.com/mod/yungs-better-ocean-monuments/version/yFjEcj2g) · 1.21.1 / NeoForge · release · 2024-11-21 | YUNG's API | 与其他神殿重做方案择一；海床高度与结构埋藏待测。 |
| 57 | [Dungeons and Taverns Ocean Monument Overhaul](https://modrinth.com/mod/dungeons-and-taverns-ocean-monument-overhaul)（模组） | Dungeons and Taverns 的海底神殿重构。 | [v1.2+mod](https://modrinth.com/mod/dungeons-and-taverns-ocean-monument-overhaul/version/SFXYDC6X) · 1.21.1 / NeoForge · release · 2024-09-15 | 文件页未列必需项；装配时核对 | 与 YUNG 同类重做择一，不默认叠加。 |
| 58 | [Hopo Better Underwater Ruins](https://modrinth.com/mod/hopo-better-underwater-ruins)（模组） | 扩展海底废墟、城市遗迹与考古探索。 | [1.2.1b](https://modrinth.com/mod/hopo-better-underwater-ruins/version/cCUmdH6y) · 1.21.1 / NeoForge · release · 2025-04-13 | 文件页未列必需项；装配时核对 | 检查深海床生成高度、遗迹密度与战利品。 |
| 59 | [Awesome Dungeon Ocean](https://modrinth.com/mod/awesome-dungeon-edition-ocean)（模组） | 海洋地牢、船只和水下遗迹。 | [3.3.0](https://modrinth.com/mod/awesome-dungeon-edition-ocean/version/4oZY86TA) · 1.21.1 / NeoForge · release · 2025-09-25 | Library Ferret | 结构船通常是静态遗迹，不能默认直接驾驶。 |
| 60 | [MOS - Moog's Ocean Structures](https://modrinth.com/mod/mos-moogs-ocean-structures)（模组） | 大量海洋结构和探索地点。 | [1.1.1](https://modrinth.com/mod/mos-moogs-ocean-structures/version/QfMITqh9) · 1.21.1 / NeoForge · release · 2026-09-17 | Moog's Structure Lib (moogs_structures) | 多结构包并用需降低生成密度，保留潜艇航道。 |
| 61 | [Underwater Village](https://modrinth.com/mod/underwater-village)（模组） | 水下村庄，增加海底补给与探索点。 | [1.0.2](https://modrinth.com/mod/underwater-village/version/ZCmcmtOR) · 1.21.1 / NeoForge · release · 2025-05-08 | 文件页未列必需项；装配时核对 | 测试村民生存、交易与深海地形落点。 |
| 62 | [When Dungeons Arise: Seven Seas](https://modrinth.com/mod/when-dungeons-arise-seven-seas)（模组） | 海上大型船只和地牢。 | [1.0.4](https://modrinth.com/mod/when-dungeons-arise-seven-seas/version/erk04BGa) · 1.21.1 / NeoForge · release · 2025-10-24 | 文件页未列必需项；装配时核对 | 独立海洋结构包；船为遗迹而非自动可驾驶载具。 |
| 63 | [Integrated Seven Seas](https://modrinth.com/mod/integrated-seven-seas)（模组） | 将七海结构与机械动力等模组内容整合。 | [1.1.0](https://modrinth.com/mod/integrated-seven-seas/version/rkThfF1I) · 1.21.1 / NeoForge · release · 2026-06-06 | Integrated API；Create；Farmer's Delight；Supplementaries；Quark | 核对整合前置；避免与原版结构包重复生成。 |
| 64 | [Lios Seafaring Dungeons](https://modrinth.com/mod/lios-seafaring-dungeons)（模组） | 海洋及地下航海风格地牢。 | [0.0.7](https://modrinth.com/mod/lios-seafaring-dungeons/version/Z8RxjdbK) · 1.21.1 / NeoForge · release · 2026-10-03 | 文件页未列必需项；装配时核对 | 需检查与加深海洋后的结构定位。 |
| 65 | [Unnamed Sea](https://modrinth.com/mod/unnamed-sea)（模组） | 灯塔、岛屿、木筏和海上船只。 | [1.2.5](https://modrinth.com/mod/unnamed-sea/version/vEr4Tzbr) · 1.21.1 / NeoForge · release · 2026-02-01 | 文件页未列必需项；装配时核对 | 适合航行补给点；控制岛屿密度。 |
| 66 | [Ships](https://modrinth.com/datapack/ships)（数据包） | 海盗船、舰队等海上遭遇数据包。 | [3.0.3](https://modrinth.com/datapack/ships/version/xmbECuEZ) · 1.21.1 / 数据包 · release · 2024-07-15 | 文件页未列必需项；装配时核对 | 数据包；舰队实体有性能成本；作者对 Modrinth 之外整合包发布另有留言/链接要求。 |
| 67 | [Ocean Lily Pad Village](https://modrinth.com/mod/ocean-lily-pad-village)（模组） | 海上睡莲村落，提供漂浮定居点。 | [1.0.1](https://modrinth.com/mod/ocean-lily-pad-village/version/LeimYgNR) · 1.21.1 / NeoForge · release · 2025-12-01 | 文件页未列必需项；装配时核对 | 检查海平面和其他结构的间距。 |
| 68 | [Antique Trading Ship](https://modrinth.com/mod/antique-trading-ship)（模组） | 古典商船探索点。 | [1.3.0](https://modrinth.com/mod/antique-trading-ship/version/HP5scbsD) · 1.21.1 / NeoForge · release · 2024-09-26 | 文件页未列必需项；装配时核对 | 静态结构；与其他贸易地点统一物资收益。 |
| 69 | [Skeleton Ghost Ship](https://modrinth.com/mod/skeleton-ghost-ship)（模组） | 骷髅幽灵船遭遇。 | [1.0.0](https://modrinth.com/mod/skeleton-ghost-ship/version/wGxrB8at) · 1.21.1 / NeoForge · release · 2026-02-10 | 文件页未列必需项；装配时核对 | 检查敌人密度和早期航行难度。 |
| 70 | [Illager War Trireme](https://modrinth.com/mod/illager-war-trireme)（模组） | 灾厄村民战船。 | [1.0.1](https://modrinth.com/mod/illager-war-trireme/version/lgM1VBEc) · 1.21.1 / NeoForge · release · 2025-08-28 | 文件页未列必需项；装配时核对 | 检查远程攻击、战利品与海上难度曲线。 |
| 71 | [Ocean Villager Trader](https://modrinth.com/mod/ocean-villager-trader)（模组） | 海洋商人、灯塔等交易探索内容。 | [2.1.0](https://modrinth.com/mod/ocean-villager-trader/version/mFaLh2HT) · 1.21.1 / NeoForge · release · 2026-06-06 | 文件页未列必需项；装配时核对 | 检查交易资源是否跳过潜艇制造进度。 |
| 72 | [Ocean Mermaid Statue](https://modrinth.com/mod/ocean-mermaid-statue)（模组） | 暖海的大型美人鱼雕像遗迹。 | [1.0.0](https://modrinth.com/mod/ocean-mermaid-statue/version/eGoWQttk) · 1.21.1 / NeoForge · release · 2026-03-15 | 文件页未列必需项；装配时核对 | 作者标注 Lootr 支持；检查深海地形落点。 |
| 73 | [Repurposed Structures - Neoforge/Forge](https://modrinth.com/mod/repurposed-structures-forge)（模组） | 在更多群系生成结构变体。 | [7.5.22+1.21.1-neoforge](https://modrinth.com/mod/repurposed-structures-forge/version/8duZuWci) · 1.21.1 / NeoForge · release · 2026-08-20 | 文件页未列必需项；装配时核对 | 属于广域补充；海洋主导时需限制陆地结构比例。 |
| 74 | [Repurposed Structures - Better Ocean Monuments Compat](https://modrinth.com/datapack/repurposed-structures-better-ocean-monuments-compat)（数据包） | Repurposed Structures 与 YUNG 神殿整合包。 | [7](https://modrinth.com/datapack/repurposed-structures-better-ocean-monuments-compat/version/EU6PZcdv) · 1.21.1 / 数据包 · release · 2024-10-13 | YUNG's Better Ocean Monuments | 数据包；仅在两者共同启用时加入并核对版本。 |
| 75 | [Create: Hangars (Aeronautics)](https://modrinth.com/mod/create-hangars)（模组） | 机械动力主题机库结构与探索物资。 | [V1.0.0+mod](https://modrinth.com/mod/create-hangars/version/3IYW39g2) · 1.21.1 / NeoForge · release · 2026-04-27 | Create Aeronautics；Sable；Create | 通常需要陆地；海洋主导世界应保留足够岛屿。 |

## 船体建造

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 76 | [Create: Copycats+](https://modrinth.com/mod/copycats)（模组） | 百变怪方块；用不同材料塑造精细船壳。 | [3.0.9+mc.1.21.1-neoforge](https://modrinth.com/mod/copycats/version/bPYeUWZx) · 1.21.1 / NeoForge · release · 2026-09-06 | Create | Deep Seas 作者列为支持；测试密封判定及材料质量。 |
| 77 | [Copycats+ aeronautics weight](https://modrinth.com/mod/copycats-plus-aeronautics-weight)（模组） | 为 Copycats 与航空学提供重量相关适配。 | [1.1.1](https://modrinth.com/mod/copycats-plus-aeronautics-weight/version/wsXjRa7l) · 1.21.1 / NeoForge · release · 2026-05-07 | Sable；Create: Copycats+ | 先检查核心版本是否已包含同类修复。 |
| 78 | [Aeronautics: simulated copycats](https://modrinth.com/mod/simulated-copycats)（模组） | 为复制材质方块增加物理属性相关功能。 | [1.3.2](https://modrinth.com/mod/simulated-copycats/version/6zbD44Qe) · 1.21.1 / NeoForge · release · 2026-08-24 | 文件页未列必需项；装配时核对 | 影响船体质量等参数，与其他重量修正共同测试。 |
| 79 | [Create: Connected](https://modrinth.com/mod/create-connected)（模组） | 机械动力连接件与实用机械扩展。 | [1.3.3-mc1.21.1](https://modrinth.com/mod/create-connected/version/Xe7EqzfQ) · 1.21.1 / NeoForge · release · 2026-08-31 | Create | 测试移动子世界内的方块实体与机械连接。 |
| 80 | [Create Deco](https://modrinth.com/mod/create-deco)（模组） | 工业建筑装饰，适合潜艇与港口。 | [2.1.3](https://modrinth.com/mod/create-deco/version/qrcMVoBD) · 1.21.1 / NeoForge · release · 2026-04-18 | 文件页未列必需项；装配时核对 | 船壳装饰是否密封需按方块实测。 |
| 81 | [Create: Design n' Decor](https://modrinth.com/mod/create-design-n-decor)（模组） | 工业设计与装饰方块。 | [2.2b](https://modrinth.com/mod/create-design-n-decor/version/uQsIRky8) · 1.21.1 / NeoForge · release · 2026-05-28 | Create | 运动船体上配套航空学兼容补丁候选。 |
| 82 | [Create: Interiors](https://modrinth.com/mod/interiors)（模组） | 机械动力内饰与座椅，丰富艇内生活区。 | [0.6.1](https://modrinth.com/mod/interiors/version/gBrfZy6S) · 1.21.1 / NeoForge · release · 2026-01-29 | 文件页未列必需项；装配时核对 | 测试乘坐、上下艇和运动中的交互。 |
| 83 | [Create: Design n' Decor - Aeronautics Compat](https://modrinth.com/mod/create-design-n-decor-aeronautics-compat)（模组） | Design n' Decor 与航空学适配。 | [1.0.0](https://modrinth.com/mod/create-design-n-decor-aeronautics-compat/version/Jhg0FhSv) · 1.21.1 / NeoForge · release · 2026-07-03 | Create: Design n' Decor；Create Aeronautics | 仅随对应装饰模组加入，核对补丁适用版本。 |
| 84 | [Create Aeronautics: Rechiseled Compatibility](https://modrinth.com/mod/create-aeronautics-rechiseled-compatibility)（模组） | Rechiseled 建筑方块与航空学适配。 | [1.2.2](https://modrinth.com/mod/create-aeronautics-rechiseled-compatibility/version/b6O6XLL6) · 1.21.1 / NeoForge · release · 2026-05-01 | Create Aeronautics；Rechiseled；Sable | 主体 Rechiseled 及其前置另核对；不作为独立装饰内容。 |

## 动力工业

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 85 | [Create: Aquatic Ambitions](https://modrinth.com/mod/create-aquatic-ambitions)（模组） | 海洋材料与机械动力加工联动，提供海洋资源循环。 | [1.21.1-6.0.10-2.0.4](https://modrinth.com/mod/create-aquatic-ambitions/version/DoI3PpXj) · 1.21.1 / NeoForge · release · 2026-07-19 | 文件页未列必需项；装配时核对 | 2.x 要求 Create 至少 6.0.6；与 Upgrade Aquatic 可联动。 |
| 86 | [Create: Diesel Generators](https://modrinth.com/mod/create-diesel-generators)（模组） | 柴油发电机、燃料与发动机体系。 | [1.21.1-1.3.15](https://modrinth.com/mod/create-diesel-generators/version/UoPH8lO1) · 1.21.1 / NeoForge · release · 2026-07-30 | Create | 与 TFMG 统一油品、配方和动力平衡。 |
| 87 | [Create: The Factory Must Grow](https://modrinth.com/mod/create-tfmg)（模组） | 重工业、石油与加工生产链。 | [1.2.0](https://modrinth.com/mod/create-tfmg/version/uDi14nbt) · 1.21.1 / NeoForge · release · 2026-01-07 | Create | 内容较重；设计适合潜艇空间的生产与补给链。 |
| 88 | [Create Crafts & Additions](https://modrinth.com/mod/createaddition)（模组） | Crafts & Additions；电力与旋转动力转换。 | [neoforge-1.21.1-1.7.2](https://modrinth.com/mod/createaddition/version/hsjVajlP) · 1.21.1 / NeoForge · beta · 2026-10-01 | 文件页未列必需项；装配时核对 | 与交错电网存在功能交集；FE 与其电路并非天然同一种网络。 |
| 89 | [Create: Ironworks](https://modrinth.com/mod/create-ironworks)（模组） | 金属材料、合金与装备扩展。 | [4.0.3](https://modrinth.com/mod/create-ironworks/version/91r9PehB) · 1.21.1 / NeoForge · release · 2026-03-14 | Create | Sable Water Pressure 关联前置；矿物来源需适配海洋世界。 |
| 90 | [Create: Enchantment Industry](https://modrinth.com/mod/create-enchantment-industry)（模组） | 机械动力附魔自动化。 | [2.5.4b](https://modrinth.com/mod/create-enchantment-industry/version/L6VSDdC4) · 1.21.1 / NeoForge · beta · 2026-10-09 | Create: Dragons Plus；Create | 可能加快装备进度；与 Boss 奖励平衡。 |

## 船载控制

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 91 | [CC: Tweaked](https://modrinth.com/mod/cc-tweaked)（模组） | 可编程电脑与外设，编写船内监控和控制逻辑。 | [1.120.2](https://modrinth.com/mod/cc-tweaked/version/1ewzHZYg) · 1.21.1 / NeoForge · alpha · 2026-08-08 | 文件页未列必需项；装配时核对 | 使用 Lua；只安装电脑不会自动获得潜艇导航。 |
| 92 | [Create: Avionics — ComputerCraft for Aeronautics](https://modrinth.com/mod/create-avionics)（模组） | 为航空学载具提供计算机控制相关接口。 | [0.6.0](https://modrinth.com/mod/create-avionics/version/9siqVCcu) · 1.21.1 / NeoForge · alpha · 2026-09-01 | Sable；Create；CC: Tweaked | 与 CC:Tweaked 联调坐标、姿态及运动控制。 |
| 93 | [CC: Deep Seas](https://modrinth.com/mod/cc-deep-seas)（模组） | Deep Seas 压载、推进及舱室数据的电脑外设。 | [1.1.1](https://modrinth.com/mod/cc-deep-seas/version/qKkaeGSS) · 1.21.1 / NeoForge · release · 2026-05-18 | CC: Tweaked；Create Deep Seas | 适合潜艇仪表；自动驾驶仍需自行编程与测试。 |
| 94 | [Create: Aero Cables](https://modrinth.com/mod/create-aero-cables)（模组） | 运动结构之间连接电力和数据网络。 | [1.21-0.4.0](https://modrinth.com/mod/create-aero-cables/version/Wn81ihHo) · 1.21.1 / NeoForge · beta · 2026-06-18 | Create Aeronautics；Create | 作者覆盖 FE、AE2、RS、CC 等；不能据此推断支持全部交错电网导线。 |
| 95 | [CC;CPG: Pinout](https://modrinth.com/mod/cccpg-pinout)（模组） | CC 与交错电网的引脚控制接口。 | [0.2.1](https://modrinth.com/mod/cccpg-pinout/version/UJtJCpBi) · 1.21.1 / NeoForge · release · 2026-07-30 | 文件页未列必需项；装配时核对 | 适合配电控制；核对双方 API 与信号方向。 |
| 96 | [Power Grid Dynamic Motor w/ CC](https://modrinth.com/mod/power-grid-dynamic-motor-w-cc)（模组） | 交错电网可调电机与电脑控制。 | [1.0](https://modrinth.com/mod/power-grid-dynamic-motor-w-cc/version/yVKwCQG7) · 1.21.1 / NeoForge · release · 2026-08-14 | 文件页未列必需项；装配时核对 | 与其他电机控制方案避免重复，检查细分转速行为。 |
| 97 | [Create: Power Components](https://modrinth.com/mod/create-power-components)（模组） | 交错电网附加电力元件。 | [1.3.0](https://modrinth.com/mod/create-power-components/version/W3GV5Ldp) · 1.21.1 / NeoForge · release · 2026-10-01 | 文件页未列必需项；装配时核对 | 核对 Power Grid 版本与移动船体连接。 |
| 98 | [Create: Power chip](https://modrinth.com/mod/create-power-chip)（模组） | 交错电网芯片/控制扩展。 | [powerchip-vdf952](https://modrinth.com/mod/create-power-chip/version/sQ0nPlqx) · 1.21.1 / NeoForge · release · 2026-09-23 | Create: Power Grid | 项目版本命名特殊；先做最小电路验证。 |
| 99 | [More Generators](https://modrinth.com/mod/more-generators)（模组） | 交错电网发电设备扩展。 | [0.0.2](https://modrinth.com/mod/more-generators/version/MC01tqz4) · 1.21.1 / NeoForge · release · 2026-06-17 | Create: Power Grid | 早期版本；能源产出和稳定性待测。 |
| 100 | [Create Aeronautics: Gyro Stabilizers](https://modrinth.com/mod/create-aeronautics-gyroscope-stabilizers)（模组） | 陀螺仪稳定装置，辅助载具姿态保持。 | [1.2.2](https://modrinth.com/mod/create-aeronautics-gyroscope-stabilizers/version/9P9dNVUn) · 1.21.1 / NeoForge · release · 2026-04-28 | Create Aeronautics；Create | 测试水下浮力、推进与稳定器是否互相振荡。 |
| 101 | [Create Aeronautics: Gadgets & Gizmos](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos)（模组） | 航空学推进、控制与对接等机械扩展。 | [2.0.0-Beta-3.1](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos/version/JF0rSDEY) · 1.21.1 / NeoForge · beta · 2026-10-12 | Create；Create Aeronautics | Beta 版本；与 Deep Seas 推进功能逐项对比。 |
| 102 | [Aeronautics: Calibrated](https://modrinth.com/mod/aeronautics-calibrated)（模组） | 航空学动力、控制和导航相关设备。 | [1.4.0](https://modrinth.com/mod/aeronautics-calibrated/version/78Nl65uk) · 1.21.1 / NeoForge · beta · 2026-07-22 | Sable；Create Aeronautics；Create | 多套推进方案并用需统一能耗与控制逻辑。 |
| 103 | [Create Aeronautics: Transmission & Linkage](https://modrinth.com/mod/create-aeronautics-transmission-linkage)（模组） | 运动结构之间的传动、关节和连杆。 | [0.2.8](https://modrinth.com/mod/create-aeronautics-transmission-linkage/version/FCZZ3ED4) · 1.21.1 / NeoForge · release · 2026-09-02 | 文件页未列必需项；装配时核对 | 适合机械臂/对接；大船运动约束稳定性待测。 |
| 104 | [Create Aeronautics Physics Gantry](https://modrinth.com/mod/create-aeronautics-physics-gantry)（模组） | 航空学物理龙门机构。 | [1.0.3](https://modrinth.com/mod/create-aeronautics-physics-gantry/version/QoeKSEzn) · 1.21.1 / NeoForge · release · 2026-07-07 | 文件页未列必需项；装配时核对 | 可作船坞设备；测试连接船体时的受力。 |
| 105 | [Create: Lift n' Load /  Simulated/Aeronautics Addon](https://modrinth.com/mod/create-lift-n-load)（模组） | 吊运、装卸及载具建造工具扩展。 | [1.0c](https://modrinth.com/mod/create-lift-n-load/version/G7N1k717) · 1.21.1 / NeoForge · beta · 2026-06-24 | Sable；Create Aeronautics；Create | 适合港口补给与装配；部件运动行为需联调。 |
| 106 | [Climbable Ropes for Create Aeronautics](https://modrinth.com/mod/create-aeronautics-climbable-rope)（模组） | 可攀爬绳索，方便上下艇。 | [2.1.4](https://modrinth.com/mod/create-aeronautics-climbable-rope/version/fk5DMIYY) · 1.21.1 / NeoForge · release · 2026-09-26 | Create；playerAnimator；Sable | 测试运动中攀爬、碰撞与多人同步。 |
| 107 | [Create Aeronautics: Throwable Rope Connector](https://modrinth.com/mod/create-aeronautics-throwable-rope-connector)（模组） | 投掷式绳索连接工具。 | [0.4.3](https://modrinth.com/mod/create-aeronautics-throwable-rope-connector/version/EDzeTV17) · 1.21.1 / NeoForge · release · 2026-08-10 | Create；Create Aeronautics | 适合牵引和对接；检查连接距离与跨船受力。 |
| 108 | [Create Aeronautics: Toolgun](https://modrinth.com/mod/create-aeronautics-toolgun)（模组） | 航空学结构操作工具，辅助复制、生成和牵引。 | [0.3.6](https://modrinth.com/mod/create-aeronautics-toolgun/version/pcD0Yutw) · 1.21.1 / NeoForge · release · 2026-08-21 | 文件页未列必需项；装配时核对 | 部分功能偏创造/管理用途；生存配方与权限需限制。 |
| 109 | [Create Aeronautics: Encased Fluid Pipes](https://modrinth.com/mod/create-aeronautics-encased-fluid-pipes)（模组） | 航空学配套流体管道。 | [1.0.7](https://modrinth.com/mod/create-aeronautics-encased-fluid-pipes/version/pKFEfjEB) · 1.21.1 / NeoForge · release · 2026-06-07 | Create Aeronautics；Create | 重点测移动子世界之间流体连接与存档。 |
| 110 | [Create Aeronautics: Portable Engine Liquid Fuel](https://modrinth.com/mod/create-aeronautics-portable-engine-liquid-fuel)（模组） | 便携发动机液体燃料支持。 | [2.0.0](https://modrinth.com/mod/create-aeronautics-portable-engine-liquid-fuel/version/L6KPVFxI) · 1.21.1 / NeoForge · release · 2026-04-28 | 文件页未列必需项；装配时核对 | 燃料标签与 Diesel/TFMG 联调。 |
| 111 | [Create Aeronautics: Weight](https://modrinth.com/mod/create-aeronautics-weight)（模组） | 玩家、物品和流体等载荷相关质量扩展。 | [1.2.0](https://modrinth.com/mod/create-aeronautics-weight/version/DNiXWANa) · 1.21.1 / NeoForge · release · 2026-08-05 | 文件页未列必需项；装配时核对 | 大仓库载荷会影响浮力和操控，需要平衡。 |
| 112 | [Create Big Cannons](https://modrinth.com/mod/create-big-cannons)（模组） | 大型火炮与舰载武器候选。 | [5.11.7](https://modrinth.com/mod/create-big-cannons/version/bOiDu0LS) · 1.21.1 / NeoForge · release · 2026-06-22 | Ritchie's Projectile Library；Create | 尚未确认水下弹道或潜艇鱼雷能力；需实测 Sable 船体交互。 |
| 113 | [Create: Radars](https://modrinth.com/mod/create-radars)（模组） | 机械动力雷达和目标探测候选。 | [0.4.9.4-1.21.1](https://modrinth.com/mod/create-radars/version/AntNFNAx) · 1.21.1 / NeoForge · release · 2026-06-10 | 文件页未列必需项；装配时核对 | 不能直接视为水下声呐；目标类别与探测方式待测。 |

## 兼容与调试

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 114 | [Create Aeronautics: Compatibility](https://modrinth.com/mod/create-aeronautics-compatability)（模组） | 航空学针对其他模组的兼容修复集合。 | [1.1.3](https://modrinth.com/mod/create-aeronautics-compatability/version/j10zlI9h) · 1.21.1 / NeoForge · release · 2026-07-10 | Sable | 有明确适配对象，非万能补丁；含 Alex 非官方移植的寻路修复。 |
| 115 | [Carry On + Create Aeronautics Compat](https://modrinth.com/mod/carryon-aeronautics-compat)（模组） | Carry On 与航空学的交互适配。 | [1.1.1](https://modrinth.com/mod/carryon-aeronautics-compat/version/z7xbAdje) · 1.21.1 / NeoForge · release · 2026-05-17 | Carry On | 搬运移动船体方块前需验证权限、复制漏洞与数据保存。 |
| 116 | [Waystones: Sable (Create Aeronautics Addon)](https://modrinth.com/mod/waystones-sable)（模组） | 传送石与 Sable 运动结构适配。 | [1.0.7](https://modrinth.com/mod/waystones-sable/version/Wot8Pf4C) · 1.21.1 / NeoForge · release · 2026-08-12 | 文件页未列必需项；装配时核对 | 可能绕过航行补给，需要设计传送成本。 |
| 117 | [Create Aeronautics: Claims](https://modrinth.com/mod/aeroclaims)（模组） | 航空学载具保护与多人权限。 | [0.9.4](https://modrinth.com/mod/aeroclaims/version/dWMRSgEu) · 1.21.1 / NeoForge · beta · 2026-09-08 | 文件页未列必需项；装配时核对 | 多人服候选；验证领地、交互和船体所有权。 |
| 118 | [Sable: Collision damage  (Create Aeronautics)](https://modrinth.com/mod/sable-create-aeronautics-collision-damage)（模组） | Sable 载具碰撞伤害。 | [1.0.8-Sable_2.0.0+](https://modrinth.com/mod/sable-create-aeronautics-collision-damage/version/snRIIWNz) · 1.21.1 / NeoForge · beta · 2026-06-15 | Sable | 要求 Sable 2.0.0+；谨慎调整搁浅、撞礁和港口损伤。 |
| 119 | [Sable: Mass view (Create Aeronautics)](https://modrinth.com/mod/create-aeronautics-sable-mass-view)（模组） | 查看载具质量，辅助设计平衡。 | [1.0.0](https://modrinth.com/mod/create-aeronautics-sable-mass-view/version/Dc1FsCO5) · 1.21.1 / NeoForge · release · 2026-04-19 | Sable | 建造调试工具；可限制管理权限。 |
| 120 | [Separate Sable Render Distance (Create Aeronautics Addon)](https://modrinth.com/mod/ssrd)（模组） | 单独调整 Sable 结构渲染距离。 | [1.8.7](https://modrinth.com/mod/ssrd/version/iZflRKdV) · 1.21.1 / NeoForge · beta · 2026-09-21 | Sodium；Sable | 与普通视距分开调节，实测大潜艇远景负载。 |
| 121 | [Aeronautics Camera Sync](https://modrinth.com/mod/aero_cam_sync)（模组） | 航空学载具相机同步与视觉体验改进。 | [1.4.0](https://modrinth.com/mod/aero_cam_sync/version/UbgVQ0qd) · 1.21.1 / NeoForge · release · 2026-08-30 | Sable | 测试第一人称、座椅和其他相机模组。 |
| 122 | [Create: Power Loader](https://modrinth.com/mod/create-power-loader)（模组） | 机械动力区块加载器。 | [2.0.5-mc1.21.1](https://modrinth.com/mod/create-power-loader/version/3Y4r0ItR) · 1.21.1 / NeoForge · release · 2026-05-22 | Create | 不能默认保证运动子世界全部持续加载；服务器边界需实测。 |

## 潜水生存

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 123 | [Sable: Water Pressure](https://modrinth.com/mod/sable-water-pressure)（模组） | 深度水压伤害，鼓励升级潜水装备与使用密封潜艇。 | [1.0.0](https://modrinth.com/mod/sable-water-pressure/version/sqjz0BcW) · 1.21.1 / NeoForge · beta · 2026-07-21 | Sable；Create: Ironworks；Create | 与 Deep Seas 水压机制重叠；关联 Ironworks。默认海平面/深度常量与定制地形需核对。 |
| 124 | [Simple Dive Gear](https://modrinth.com/mod/simple-dive-gear)（模组） | 独立潜水装备，提供离艇探索能力。 | [10.0.0](https://modrinth.com/mod/simple-dive-gear/version/4ZexmS2I) · 1.21.1 / NeoForge · release · 2025-01-09 | 文件页未列必需项；装配时核对 | 调整持续时间与成本，避免跳过氧气进度。 |
| 125 | [Deep Diving Gear](https://modrinth.com/mod/deep-diving-gear)（模组） | 深潜装备和强化材料进度。 | [1.2.1](https://modrinth.com/mod/deep-diving-gear/version/Kd3fDLCy) · 1.21.1 / NeoForge · release · 2026-08-31 | Deeper and Darker；Create | 涉及额外材料模组；检查资源在海洋世界可得性。 |
| 126 | [Aquatic Additions](https://modrinth.com/mod/aquaticadditions)（模组） | 水域装备和海龟护甲等生存扩展。 | [4.3](https://modrinth.com/mod/aquaticadditions/version/BFQtGYVi) · 1.21.1 / NeoForge · release · 2025-11-04 | 文件页未列必需项；装配时核对 | 完整装备的长效水下呼吸可能绕过氧气挑战。 |
| 127 | [Thick Air](https://modrinth.com/mod/thick-air)（模组） | 按高度施加氧气限制的环境生存机制。 | [1.1.2](https://modrinth.com/mod/thick-air/version/XZ93w1xP) · 1.21.1 / NeoForge · release · 2025-12-29 | Create；Curios API | 与 Deep Seas 重复；密封舱供氧、维度白名单必须联调。 |
| 128 | [Deep Sea](https://modrinth.com/mod/deepsea)（模组） | Deep Sea：让普通船在海洋环境受损，限制远洋小船。 | [4.0.0](https://modrinth.com/mod/deepsea/version/pSSxzf86) · 1.21.1 / NeoForge · release · 2026-07-20 | 文件页未列必需项；装配时核对 | 与 Create Deep Seas 是不同项目；用作进度门槛会影响救生艇。 |
| 129 | [Ocean Breaths](https://modrinth.com/datapack/ocean-breaths)（数据包） | 海底氧气泡、通气点等探索补给数据包。 | [1.3.2](https://modrinth.com/datapack/ocean-breaths/version/tU7xmu5P) · 1.21.1 / 数据包 · release · 2024-07-04 | 文件页未列必需项；装配时核对 | 数据包；可能降低氧气挑战，需调密度和出现条件。 |
| 130 | [Permanent Sponges](https://modrinth.com/mod/permanent-sponges)（模组） | 持续清理周围水体的海绵工具。 | [v21.1.0-1.21.1-NeoForge](https://modrinth.com/mod/permanent-sponges/version/9EBHr4tP) · 1.21.1 / NeoForge · release · 2024-09-18 | Puzzles Lib | 适合静态基地；不是运动潜艇的密封模拟替代品。 |
| 131 | [Glow Sticks](https://modrinth.com/mod/glow-sticks)（模组） | 投掷荧光棒，为水下探索提供照明。 | [7.5.1](https://modrinth.com/mod/glow-sticks/version/1dP9DxPC) · 1.21.1 / NeoForge · release · 2026-09-19 | 文件页未列必需项；装配时核对 | 检验水下可见度与动态光源性能。 |
| 132 | [Better Tridents](https://modrinth.com/mod/better-tridents)（模组） | 优化三叉戟使用体验。 | [v21.1.0-1.21.1-NeoForge](https://modrinth.com/mod/better-tridents/version/UVSMPjiS) · 1.21.1 / NeoForge · release · 2024-09-17 | Puzzles Lib | 与其他三叉戟修改模组一起测试附魔和回收。 |
| 133 | [Harpooned](https://modrinth.com/mod/harpooned)（模组） | 调整三叉戟，使其更接近鱼叉的使用方式。 | [1.21.1-1.1.0](https://modrinth.com/mod/harpooned/version/z1FtfIAP) · 1.21.1 / NeoForge · release · 2026-08-30 | 文件页未列必需项；装配时核对 | 并非单独新增一把鱼叉；与 Better Tridents 可能功能重叠。 |
| 134 | [Subnautica After Zero](https://modrinth.com/mod/subnautica-after-zero)（模组） | 深海迷航风格剧情内容。 | [Act_1e](https://modrinth.com/mod/subnautica-after-zero/version/TRo7XXIi) · 1.21.1 / NeoForge · release · 2026-03-09 | 文件页未列必需项；装配时核对 | 偏配套地图与未完成剧情项目，不默认适用于普通沙盒整合包。 |

## 后勤与任务

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 135 | [Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks)（模组） | 精妙背包；离艇探险储物与升级。 | [1.21.1-3.26.12.2208](https://modrinth.com/mod/sophisticated-backpacks/version/QkVLEgt1) · 1.21.1 / NeoForge · release · 2026-10-13 | Sophisticated Core | 空间与自动收集能力会影响补给需求。 |
| 136 | [Sophisticated Storage](https://modrinth.com/mod/sophisticated-storage)（模组） | 精妙存储；船内仓储和过滤。 | [1.21.1-1.6.5.2176](https://modrinth.com/mod/sophisticated-storage/version/EvzAB4E6) · 1.21.1 / NeoForge · release · 2026-10-13 | Sophisticated Core | 移动船体中方块实体、升级和自动化接口待测。 |
| 137 | [Storage Drawers](https://modrinth.com/mod/storagedrawers)（模组） | 储物抽屉；批量保存船内材料。 | [1.21.1-13.11.4](https://modrinth.com/mod/storagedrawers/version/px0CCB06) · 1.21.1 / NeoForge · release · 2025-09-14 | 文件页未列必需项；装配时核对 | 航空学兼容补丁有相关修复，但移动结构视觉问题仍需验证。 |
| 138 | [Lootr](https://modrinth.com/mod/lootr)（模组） | 多人独立战利品容器。 | [1.21.1-1.11.38.127](https://modrinth.com/mod/lootr/version/SPOrIzX8) · 1.21.1 / NeoForge · release · 2026-09-25 | 文件页未列必需项；装配时核对 | 测试结构箱替换及搬上船后的容器身份。 |
| 139 | [KubeJS](https://modrinth.com/mod/kubejs)（模组） | 用脚本统一配方、标签与整合包进度。 | [2101.7.2-build.377](https://modrinth.com/mod/kubejs/version/THIGFPwf) · 1.21.1 / NeoForge · release · 2026-09-09 | Rhino；Better Advanced Tooltips | 具体事件/API 依赖当前版本；可作为自定义整合基础。 |
| 140 | [Patchouli](https://modrinth.com/mod/patchouli)（模组） | 制作航海日志、潜艇建造手册与生物图鉴。 | [1.21.1-93-neoforge](https://modrinth.com/mod/patchouli/version/BIogJv2D) · 1.21.1 / NeoForge · release · 2026-03-08 | 文件页未列必需项；装配时核对 | 手册内容需自行编写。 |
| 141 | [In Control!](https://modrinth.com/mod/in-control)（模组） | 控制刷怪与掉落规则。 | [1.21-10.3.0](https://modrinth.com/mod/in-control/version/L0dof2WQ) · 1.21.1 / NeoForge · beta · 2026-09-02 | 文件页未列必需项；装配时核对 | 用于统一海怪密度；新增维度与生物标签需逐项配置。 |
| 142 | [Nature's Compass](https://modrinth.com/mod/natures-compass)（模组） | 定位群系，辅助寻找特殊海域。 | [1.21.1-3.4.0-neoforge](https://modrinth.com/mod/natures-compass/version/nFniEtJV) · 1.21.1 / NeoForge · release · 2026-04-08 | 文件页未列必需项；装配时核对 | 可能降低探索难度；可调整成本与范围。 |
| 143 | [Explorer's Compass](https://modrinth.com/mod/explorers-compass)（模组） | 定位结构，辅助寻找海底遗迹。 | [1.21.1-3.4.0-neoforge](https://modrinth.com/mod/explorers-compass/version/hIJ2Ev1Q) · 1.21.1 / NeoForge · release · 2026-04-08 | 文件页未列必需项；装配时核对 | 不等于声呐；可限制可搜索结构与使用阶段。 |
| 144 | [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map)（模组） | 航行地图与探索记录。 | [neoforge-1.21.1-1.47.0](https://modrinth.com/mod/xaeros-world-map/version/SnQ4SoDe) · 1.21.1 / NeoForge · release · 2026-10-05 | 文件页未列必需项；装配时核对 | 不是实时海底声呐；水下地形显示方式待测。 |
| 145 | [Jade 🔍](https://modrinth.com/mod/jade)（模组） | 查看方块和实体信息，辅助船内设备维护。 | [15.10.6+neoforge](https://modrinth.com/mod/jade/version/eYz2YBGT) · 1.21.1 / NeoForge · release · 2026-08-06 | 文件页未列必需项；装配时核对 | 测试瞄准运动子世界的识别。 |
| 146 | [Just Enough Items (JEI)](https://modrinth.com/mod/jei)（模组） | 查询配方与用途，支撑复杂造船进度。 | [19.64.0.453](https://modrinth.com/mod/jei/version/OZIoQboh) · 1.21.1 / NeoForge · beta · 2026-10-12 | MezzConfig | 检查附属自定义配方是否正确显示。 |
| 147 | [Waystones](https://modrinth.com/mod/waystones)（模组） | 港口间传送和后期回程。 | [21.1.46+neoforge-1.21.1](https://modrinth.com/mod/waystones/version/6Z6MQ6os) · 1.21.1 / NeoForge · release · 2026-09-25 | Balm | 会削弱远航主题，适合作为后期或受限系统。 |
| 148 | [FTB Quests (NeoForge)](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge)（模组） | 编写潜艇建造、海域探索与 Boss 挑战任务线。 | [2101.1.36](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge/files) · 1.21.1 / NeoForge · release · 2026-09-15 | FTB Library / FTB Teams 等以文件关系页为准 | 任务内容需自行制作；锁定 1.21.1 文件，勿下载首页其他 MC 版本的最新文件。 |

## 其他载具

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 149 | [Small Ships](https://modrinth.com/mod/small-ships)（模组） | 小型可驾驶船只，作为初期航行或救援艇。 | [2.0.1-NeoForge](https://modrinth.com/mod/small-ships/version/QNjKZdCr) · 1.21.1 / NeoForge · release · 2026-10-12 | 文件页未列必需项；装配时核对 | 不是巨型自由拼装潜艇；Deep Sea 的船损规则需检查。 |
| 150 | [Boatload](https://modrinth.com/mod/boatload)（模组） | 扩展原版船种类与载物能力。 | [6.0.2](https://modrinth.com/mod/boatload/version/cizR5egt) · 1.21.1 / NeoForge · release · 2025-10-17 | Blueprint | 适合前期；与船损限制模组联调。 |
| 151 | [Immersive Aircraft](https://modrinth.com/mod/immersive-aircraft)（模组） | 独立飞机类载具，提供空中运输备选。 | [1.6.0+1.21.1](https://modrinth.com/mod/immersive-aircraft/version/ZCSI1mBS) · 1.21.1 / NeoForge · release · 2026-10-08 | 文件页未列必需项；装配时核对 | 可能绕过海上探索路线，属于可选内容。 |

## 画面与氛围

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 152 | [Wakes Reforged](https://modrinth.com/mod/wakes-reforged)（模组） | NeoForge 航迹、水面涟漪效果。 | [1.4.1](https://modrinth.com/mod/wakes-reforged/version/Z3Ic8n5N) · 1.21.1 / NeoForge · release · 2026-08-15 | 文件页未列必需项；装配时核对 | Deep Seas 作者列为支持；大船水面效果仍需测负载。 |
| 153 | [Water Depth Fog](https://modrinth.com/mod/water-depth-fog)（模组） | 随深度改变水下雾效，强化深海压迫感。 | [1.0.0](https://modrinth.com/mod/water-depth-fog/version/T9kiVlEf) · 1.21.1 / NeoForge · beta · 2026-07-21 | 文件页未列必需项；装配时核对 | 与 Clear Water 的清晰化目标相反，建议分视觉预设。 |
| 154 | [Clear Water](https://modrinth.com/mod/clear-water)（模组） | 提高水下可见度。 | [3.0.0](https://modrinth.com/mod/clear-water/version/CCKZIPGa) · 1.21.1 / NeoForge · release · 2024-12-05 | Resourceful Config | 可能削弱探索与照明需求，和深度雾效择一调校。 |
| 155 | [Bathymetry](https://modrinth.com/mod/bathymetry)（模组） | 依据水下深度改善水色表现。 | [1.0.4+1.21.1+neoforge](https://modrinth.com/mod/bathymetry/version/Yo2fTC7v) · 1.21.1 / NeoForge · release · 2026-09-29 | MossyLib | 视觉深度信息，不是实际探测海床的声呐设备。 |
| 156 | [AmbientSounds](https://modrinth.com/mod/ambientsounds)（模组） | 环境声景，增强海岸和探索氛围。 | [6.3.10](https://modrinth.com/mod/ambientsounds/version/P48d3fOf) · 1.21.1 / NeoForge · release · 2026-10-08 | CreativeCore | 舱内/舱外声音切换与潜艇位置识别待测。 |
| 157 | [Sound Physics Remastered](https://modrinth.com/mod/sound-physics-remastered)（模组） | 声音遮挡与混响，强化密闭舱室感。 | [neoforge-1.21.1-1.5.1](https://modrinth.com/mod/sound-physics-remastered/version/Dd2tmpsk) · 1.21.1 / NeoForge · alpha · 2025-09-25 | 文件页未列必需项；装配时核对 | 运动子世界内的声学遮挡不保证正确，需实测。 |
| 158 | [Make Bubbles Pop](https://modrinth.com/mod/make_bubbles_pop)（模组） | 水下气泡视觉效果。 | [0.4.0-beta.2](https://modrinth.com/mod/make_bubbles_pop/version/OkGl4Kh9) · 1.21.1 / NeoForge · beta · 2026-09-05 | 文件页未列必需项；装配时核对 | Beta 文件；大量气泡时测试粒子性能。 |
| 159 | [Chest Bubbles](https://modrinth.com/datapack/chest-bubbles)（数据包） | 打开水下箱子产生气泡的数据包。 | [1.3.0](https://modrinth.com/datapack/chest-bubbles/version/dW6jo5ZD) · 1.21.1 / 数据包 · release · 2024-09-14 | 文件页未列必需项；装配时核对 | 数据包；检查 Lootr 箱子和新增容器是否生效。 |
| 160 | [Distant Horizons](https://modrinth.com/mod/distanthorizons)（模组） | 远景 LOD，适合远洋与大型岛屿视野。 | [3.3.3-1.21.1](https://modrinth.com/mod/distanthorizons/version/9w34y8ai) · 1.21.1 / NeoForge · release · 2026-09-29 | 文件页未列必需项；装配时核对 | 不保证运动船体进入远景；配合光影时单独测试。 |
| 161 | [Physics Mod](https://modrinth.com/mod/physicsmod)（模组） | 视觉物理效果。 | [3.0.34](https://modrinth.com/mod/physicsmod/version/ZhWEOiAq) · 1.21.1 / NeoForge · release · 2026-09-17 | 文件页未列必需项；装配时核对 | 不是 Sable 载具物理替代品；具体免费/付费特性及负载需核对。 |

## 性能与光影

| # | Mod / 数据包 | 简介与主题用途 | 1.21.1 文件 / 平台 | 直接前置 | 兼容性与选型备注 |
| ---: | --- | --- | --- | --- | --- |
| 162 | [Sodium](https://modrinth.com/mod/sodium)（模组） | 客户端渲染优化。 | [mc1.21.1-0.8.13-neoforge](https://modrinth.com/mod/sodium/version/uMOpc5uV) · 1.21.1 / NeoForge · release · 2026-08-28 | 文件页未列必需项；装配时核对 | Deep Seas 有对应修复/支持记录；仍需与附属渲染联调。 |
| 163 | [FerriteCore](https://modrinth.com/mod/ferrite-core)（模组） | 降低内存占用。 | [7.0.3-neoforge](https://modrinth.com/mod/ferrite-core/version/x7kQWVju) · 1.21.1 / NeoForge · release · 2026-01-25 | 文件页未列必需项；装配时核对 | 大型整合包候选；安装后测实际内存收益。 |
| 164 | [ModernFix](https://modrinth.com/mod/modernfix)（模组） | 加载、内存及其他性能修复。 | [5.27.26+mc1.21.1](https://modrinth.com/mod/modernfix/version/7Jy58lnT) · 1.21.1 / NeoForge · release · 2026-10-08 | 文件页未列必需项；装配时核对 | Sable 改动较深；根据日志决定是否关闭个别优化。 |
| 165 | [Lithium](https://modrinth.com/mod/lithium)（模组） | 游戏逻辑性能优化。 | [mc1.21.1-0.15.4-neoforge](https://modrinth.com/mod/lithium/version/DDUrRVCA) · 1.21.1 / NeoForge · release · 2026-06-27 | 文件页未列必需项；装配时核对 | 对大船/大量实体的收益需实际测量。 |
| 166 | [Chunky](https://modrinth.com/mod/chunky)（模组） | 提前生成区块，降低远航途中生成卡顿。 | [1.4.23](https://modrinth.com/mod/chunky/version/LuFhm4eU) · 1.21.1 / NeoForge · release · 2025-04-04 | 文件页未列必需项；装配时核对 | 确定最终地形后再预生成；消耗磁盘与生成时间。 |
| 167 | [spark](https://modrinth.com/mod/spark)（模组） | 性能分析与卡顿诊断工具。 | [1.10.124-neoforge-1.21.1](https://modrinth.com/mod/spark/version/v5qtqRQi) · 1.21.1 / NeoForge · release · 2025-02-23 | 文件页未列必需项；装配时核对 | 用于测大型潜艇物理、海怪 AI 和区块生成耗时。 |
| 168 | [Iris Shaders](https://modrinth.com/mod/iris)（模组） | 光影加载器。 | [1.8.14-beta.1+1.21.1-neoforge](https://modrinth.com/mod/iris/version/KduFYu4t) · 1.21.1 / NeoForge · beta · 2026-06-13 | 文件页未列必需项；装配时核对 | 存在航空学视觉问题、Deep Seas/Veil 冲突提示；仅实验候选，不作为首轮基线。 |
| 169 | [Iris & Oculus Flywheel Compat](https://modrinth.com/mod/iris-flw-compat)（模组） | Iris/Oculus 与 Flywheel 渲染适配。 | [1.21.1+2.4.0-release](https://modrinth.com/mod/iris-flw-compat/version/6ZQz5Ow2) · 1.21.1 / NeoForge · release · 2026-05-06 | Iris Shaders；Sodium | 补丁不代表所有光影都能兼容 Deep Seas。 |
| 170 | [Iris Veil Compat](https://modrinth.com/mod/iris-veil-compat)（模组） | Iris 与 Veil 渲染适配。 | [1.21.1+0.4.0-alpha](https://modrinth.com/mod/iris-veil-compat/version/hWItkPzn) · 1.21.1 / NeoForge · alpha · 2026-10-04 | Iris Shaders | Alpha 文件；与上一个补丁解决的层次不同，仍需逐光影测试。 |

## 下一轮验证顺序

1. 用纯核心与必要前置建立最小测试实例，完成大型船壳密封、潜浮、推进、存档重载和远距离航行。
2. 地形候选分别新建测试世界，测连续可航水域、最窄通道、海床净空、岛屿间距与结构落点；不能只看截图决定“大”。
3. 加入生态、海怪和遗迹，测试实体总量、船体攻击与多人 TPS，再调整刷怪及战利品。
4. 最后接入电力/电脑外设、仓储、任务和视觉模组，固定可复现的版本组合。

研究入口包含 Modrinth、CurseForge、项目 GitHub、Planet Minecraft 与 MC 百科。技术判断优先采用作者说明、具体版本文件和更新日志；每行均保留项目/文件来源。此表不声称穷尽所有网站上的项目，也未把所有通用前置库重复算作主题内容。
