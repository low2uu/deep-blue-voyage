# 内容设计器字段目录

由 `node tools/content-designer/generate.cjs` 根据UI的同一字段目录生成。字段总数随目录更新；每项均导出状态、值、来源和备注。列表内子字段同时列出。

## 基本信息（identity）

先确定它是什么、叫什么、解决什么问题。注册ID一旦发布应保持稳定。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| identity.registryId | 注册ID | text | 是 |
| identity.nameZh | 中文名称 | text | 是 |
| identity.nameEn | 英文名称 | text | 是 |
| identity.kind | 内容类型 | enum：block / 普通方块；machine / 机器方块；controller / 控制器；decorative / 装饰方块；item / 普通物品；tool / 工具；equipment / 装备；monster / 怪物 / Boss | 是 |
| identity.category | 功能类别 | text | 是 |
| identity.summary | 功能描述（必填） | textarea | 是 |
| identity.motivation | 使用场景与设计目的 | textarea | 可标待定/不适用 |
| identity.keywords | 搜索关键词（每行一个） | lines | 可标待定/不适用 |
| identity.author | 设计者 | text | 可标待定/不适用 |
| identity.targetRelease | 计划加入版本 | text | 可标待定/不适用 |
| identity.creativeTabs | 创造分类ID（每行一个） | lines | 可标待定/不适用 |
| identity.tooltipsZh | 中文提示（每行一句） | lines | 可标待定/不适用 |
| identity.tooltipsEn | 英文提示（与中文对应） | lines | 可标待定/不适用 |

## 平台与依赖（platform）

使用准确版本和modId；这里填写的是要求，不代表工具已验证兼容。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| platform.minecraft | Minecraft版本 | text | 是 |
| platform.loader | 加载器 | enum：neoforge / NeoForge；forge / Forge；fabric / Fabric | 是 |
| platform.loaderVersion | 加载器版本/范围 | text | 是 |
| platform.java | Java主版本 | integer | 是 |
| platform.namespace | 所属Mod命名空间 | text | 是 |
| platform.dependencies | 依赖Mod | rows | 可标待定/不适用 |
| platform.installationSide | 安装端 | enum：both / 客户端与服务端；server / 仅服务端；client / 仅客户端 | 可标待定/不适用 |
| platform.compatibilityPolicy | 缺少依赖、版本不符时如何处理 | textarea | 可标待定/不适用 |

**依赖Mod每条记录的字段：**

- `modId`：modId（text）
- `version`：锁定版本/范围（text）
- `requirement`：关系（enum；required / 必需；optional / 可选）
- `side`：环境（enum；both / 两端；server / 服务端；client / 客户端）
- `purpose`：用途（textarea）

## 外观、音效与形状（appearance）

资源路径只用于描述，不会读取文件。模型和纹理需要后续制作。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| appearance.model | 模型 | enum：cube_all / 六面同材质立方体；cube / 六面独立材质；slab / 半砖；stairs / 楼梯；custom / 自定义；item / 物品模型 | 可标待定/不适用 |
| appearance.visualDescription | 外形与材质描述 | textarea | 可标待定/不适用 |
| appearance.textureAll | 共用材质资源ID | text | 可标待定/不适用 |
| appearance.faceTextures | 各面材质 | rows | 可标待定/不适用 |
| appearance.textureResolution | 纹理边长（像素） | integer | 可标待定/不适用 |
| appearance.renderLayer | 渲染层 | enum：solid / 不透明；cutout / 镂空；translucent / 半透明 | 可标待定/不适用 |
| appearance.mapColor | 地图颜色/参考方块 | text | 可标待定/不适用 |
| appearance.soundType | 音效类型/参考方块 | text | 可标待定/不适用 |
| appearance.lightLevel | 发光等级 | integer | 可标待定/不适用 |
| appearance.occludes | 遮挡光线 | boolean | 可标待定/不适用 |
| appearance.fullCube | 完整立方体 | boolean | 可标待定/不适用 |
| appearance.collisionShape | 碰撞体定义（坐标单位与各盒范围） | textarea | 可标待定/不适用 |
| appearance.outlineShape | 选中框形状 | textarea | 可标待定/不适用 |
| appearance.waterlogged | 支持含水 | boolean | 可标待定/不适用 |
| appearance.facing | 朝向 | enum：none / 无；horizontal / 水平四向；six / 六向；axis / 轴向；custom / 自定义 | 可标待定/不适用 |
| appearance.blockStates | 额外方块状态 | rows | 可标待定/不适用 |
| appearance.assetReferences | 素材路径/参考链接（每行一个） | lines | 可标待定/不适用 |
| appearance.animations | 动画、粒子、动态声音 | textarea | 可标待定/不适用 |

**各面材质每条记录的字段：**

- `face`：面（enum；up / 上；down / 下；north / 北；south / 南；east / 东；west / 西）
- `texture`：材质资源ID（text）
- `rotation`：旋转角度（enum；0 / 0°；90 / 90°；180 / 180°；270 / 270°）

**额外方块状态每条记录的字段：**

- `name`：属性名（text）
- `values`：允许值（逗号分隔）（text）
- `default`：初始值（text）
- `meaning`：含义与状态切换（textarea）

## 采集与掉落（harvesting）

采集硬度与爆炸抗性分别配置。硬度5不会自动得到铁块的所有行为。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| harvesting.referenceBlock | 采集参考方块ID | text | 可标待定/不适用 |
| harvesting.hardness | 挖掘硬度（-1表示不可挖掘） | number | 可标待定/不适用 |
| harvesting.tool | 有效工具 | enum：pickaxe / 镐；axe / 斧；shovel / 锹；hoe / 锄；shears / 剪刀；any / 任意；custom / 自定义 | 可标待定/不适用 |
| harvesting.minimumTier | 最低工具等级 | enum：none / 无；wood / 木级；stone / 石级；iron / 铁级；diamond / 钻石级；netherite / 下界合金级；custom / 自定义 | 可标待定/不适用 |
| harvesting.requiresCorrectTool | 正确工具才掉落 | boolean | 可标待定/不适用 |
| harvesting.dropsSelf | 掉落自身 | boolean | 可标待定/不适用 |
| harvesting.silkTouch | 精准采集行为 | textarea | 可标待定/不适用 |
| harvesting.fortune | 时运行为 | textarea | 可标待定/不适用 |
| harvesting.explosionLoot | 爆炸掉落行为 | textarea | 可标待定/不适用 |
| harvesting.drops | 额外/替代掉落 | rows | 可标待定/不适用 |
| harvesting.experience | 采集经验 | number | 可标待定/不适用 |
| harvesting.breakSideEffects | 破坏时设备、库存与船体数据处理 | textarea | 可标待定/不适用 |

**额外/替代掉落每条记录的字段：**

- `item`：物品ID（text）
- `min`：最小数量（integer）
- `max`：最大数量（integer）
- `chance`：概率0–1（number）
- `conditions`：条件（textarea）

## 原版防护与材料（protection）

特殊免疫应逐项写明，不自动复制所有参考方块标签。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| protection.referenceBlock | 防护参考方块ID | text | 可标待定/不适用 |
| protection.explosionResistance | 爆炸抗性 | number | 可标待定/不适用 |
| protection.pistonReaction | 活塞反应 | enum：normal / 可推动；block / 不可推动；destroy / 推动时破坏；push_only / 只允许推；ignore / 忽略 | 可标待定/不适用 |
| protection.dragonImmune | 末影龙破坏免疫 | boolean | 可标待定/不适用 |
| protection.witherImmune | 凋灵破坏免疫 | boolean | 可标待定/不适用 |
| protection.flammability | 可燃性 | integer | 可标待定/不适用 |
| protection.fireSpread | 火焰蔓延 | integer | 可标待定/不适用 |
| protection.slipperiness | 原版脚下摩擦系数 | number | 可标待定/不适用 |
| protection.speedFactor | 行走速度倍率 | number | 可标待定/不适用 |
| protection.jumpFactor | 跳跃倍率 | number | 可标待定/不适用 |
| protection.replaceable | 可被放置替换 | boolean | 可标待定/不适用 |
| protection.gravityAffected | 受原版重力下落 | boolean | 可标待定/不适用 |
| protection.blockTags | 方块标签 | lines | 可标待定/不适用 |
| protection.itemTags | 物品标签 | lines | 可标待定/不适用 |
| protection.specialInteractions | 活塞之外的搬运、粘连及特殊交互 | textarea | 可标待定/不适用 |

## 航空学 / Sable物理（physics）

独立于原版属性。质量使用引擎单位；整船升力和航速不能直接当成单块属性。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| physics.referenceBlock | 物理参考方块ID | text | 可标待定/不适用 |
| physics.bindingPolicy | 参考值策略 | enum：snapshot / 锁定核对时的值；follow_runtime / 跟随参考方块运行时值；custom / 独立指定 | 可标待定/不适用 |
| physics.mass | 单块质量 | number | 可标待定/不适用 |
| physics.volume | 物理体积 | number | 可标待定/不适用 |
| physics.friction | 物理碰撞摩擦 | number | 可标待定/不适用 |
| physics.restitution | 碰撞回弹 | number | 可标待定/不适用 |
| physics.fragile | 易碎标记 | boolean | 可标待定/不适用 |
| physics.inertia | 惯性 | enum：engine / 由引擎按质量和几何计算；custom / 自定义，需说明 | 可标待定/不适用 |
| physics.customInertia | 自定义惯性/质心说明 | textarea | 可标待定/不适用 |
| physics.floatingMaterial | 特殊漂浮材质ID（无则设不适用） | text | 可标待定/不适用 |
| physics.floatingScale | 特殊漂浮缩放 | number | 可标待定/不适用 |
| physics.assemblyAllowed | 允许装配为物理船 | boolean | 可标待定/不适用 |
| physics.connections | 胶水、轴承、关节及连接识别 | textarea | 可标待定/不适用 |
| physics.damageBehavior | 碰撞损坏/脱落行为 | textarea | 可标待定/不适用 |
| physics.overridePolicy | 标签、数据包与运行时覆盖优先级 | textarea | 可标待定/不适用 |

## 深海学耐压与密闭（pressure）

耐深是材料参数，不是整船安全下潜承诺。概率参数不是每秒损坏概率。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| pressure.referenceBlock | 耐压参考方块ID | text | 可标待定/不适用 |
| pressure.bindingPolicy | 耐压参考策略 | enum：snapshot / 锁定值；follow_runtime / 跟随黑曜石等参考块；custom / 独立指定 | 可标待定/不适用 |
| pressure.maxWaterDepth | 材料耐深（格） | integer | 可标待定/不适用 |
| pressure.implosionChance | 压溃概率参数 | number | 可标待定/不适用 |
| pressure.useGlobalMultipliers | 遵循上游全局倍率 | boolean | 可标待定/不适用 |
| pressure.sealsCompartment | 作为密闭舱壁 | boolean | 可标待定/不适用 |
| pressure.leakBehavior | 损坏、开门、含水时的漏水行为 | textarea | 可标待定/不适用 |
| pressure.oxygenBehavior | 制氧、供氧、耗氧或无作用 | textarea | 可标待定/不适用 |
| pressure.configurationMigration | 已有配置、缓存和客户端同步策略 | textarea | 可标待定/不适用 |

## 物品、工具与装备（item）

普通方块也有对应物品；没有耐久的方块物品请将耐久设为不适用。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| item.maxStackSize | 最大堆叠 | integer | 可标待定/不适用 |
| item.durability | 耐久 | integer | 可标待定/不适用 |
| item.fireResistant | 物品实体耐火 | boolean | 可标待定/不适用 |
| item.rarity | 稀有度 | enum：common / 普通；uncommon / 罕见；rare / 稀有；epic / 史诗 | 可标待定/不适用 |
| item.equipmentSlot | 装备槽 | text | 可标待定/不适用 |
| item.attackDamage | 攻击伤害 | number | 可标待定/不适用 |
| item.attackSpeed | 攻击速度 | number | 可标待定/不适用 |
| item.armor | 护甲 | number | 可标待定/不适用 |
| item.toughness | 韧性 | number | 可标待定/不适用 |
| item.knockbackResistance | 击退抗性 | number | 可标待定/不适用 |
| item.repairIngredients | 修复材料ID/标签 | lines | 可标待定/不适用 |
| item.enchantments | 允许/禁止附魔说明 | lines | 可标待定/不适用 |
| item.useBehavior | 物品使用、冷却与消耗行为 | textarea | 可标待定/不适用 |

## 功能、操作与界面（interaction）

功能应写清触发、条件、结果与失败情况，避免只有一句模糊描述。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| interaction.actions | 功能规则 | rows | 可标待定/不适用 |
| interaction.ui | 界面布局、页签、按钮、数据显示与刷新 | textarea | 可标待定/不适用 |
| interaction.permissions | 玩家/队伍/管理员权限 | textarea | 可标待定/不适用 |
| interaction.multiplayer | 多人同时操作、距离与并发规则 | textarea | 可标待定/不适用 |
| interaction.redstone | 红石输入、输出、比较器 | textarea | 可标待定/不适用 |
| interaction.automation | 漏斗、机械动力、管线等自动化 | textarea | 可标待定/不适用 |
| interaction.accessibility | 提示语言、音效/闪烁开关、色盲/键盘操作 | textarea | 可标待定/不适用 |

**功能规则每条记录的字段：**

- `name`：功能名（text）
- `trigger`：触发：右键/潜行右键/红石等（text）
- `conditions`：前置条件（textarea）
- `steps`：执行步骤（textarea）
- `success`：成功结果（textarea）
- `failure`：失败结果与回滚（textarea）
- `feedbackZh`：中文反馈（text）
- `cooldownTicks`：冷却tick（integer）
- `executionSide`：执行端（enum；server / 服务端权威；client / 客户端显示；both / 两端协作）

## 设备接口与运行（machine）

未涉及的接口设为不适用。每个接口应说明单位、方向、容量和规则。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| machine.ports | 接口 | rows | 可标待定/不适用 |
| machine.inventorySlots | 内部物品槽数 | integer | 可标待定/不适用 |
| machine.tankCount | 内部流体罐数 | integer | 可标待定/不适用 |
| machine.energyPerTick | 运行能耗 FE/t | number | 可标待定/不适用 |
| machine.stressImpact | 应力消耗系数 | number | 可标待定/不适用 |
| machine.stressCapacity | 应力供给系数 | number | 可标待定/不适用 |
| machine.minimumRpm | 最低转速 | number | 可标待定/不适用 |
| machine.maximumRpm | 最高转速 | number | 可标待定/不适用 |
| machine.operatingStates | 停止、运行、缺料、满载、故障等状态 | textarea | 可标待定/不适用 |
| machine.multiblock | 多方块结构与控制器规则 | textarea | 可标待定/不适用 |
| machine.failureModes | 过载、断电、爆炸及安全行为 | textarea | 可标待定/不适用 |

**接口每条记录的字段：**

- `name`：接口名（text）
- `resource`：资源类型（enum；item / 物品；fluid / 流体；energy / FE能量；kinetic / 旋转应力；oxygen / 氧气；signal / 信号）
- `direction`：流向（enum；input / 输入；output / 输出；both / 双向）
- `faces`：连接面（text）
- `capacity`：容量（number）
- `unit`：单位（text）
- `ratePerTick`：每tick速率（number）
- `filters`：过滤/允许内容（textarea）
- `rules`：连接与运行规则（textarea）

## 配方、获取与平衡（acquisition）

配方是设计需求。输入请逐行写清ID/标签和数量；不会在此自动转换为游戏配方。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| acquisition.recipes | 配方 | rows | 可标待定/不适用 |
| acquisition.otherSources | 战利品、交易、任务、生成等获取方式 | textarea | 可标待定/不适用 |
| acquisition.progression | 科技阶段与解锁条件 | textarea | 可标待定/不适用 |
| acquisition.balance | 成本、优势、短板与限制 | textarea | 可标待定/不适用 |
| acquisition.recipeViewer | JEI/JER展示说明 | textarea | 可标待定/不适用 |

**配方每条记录的字段：**

- `id`：配方ID（text）
- `type`：配方类型ID（text）
- `ingredients`：材料/数量/标签，每行一项（textarea）
- `pattern`：有序配方排列/步骤（textarea）
- `result`：产物ID（text）
- `count`：产物数量（integer）
- `durationTicks`：耗时tick（integer）
- `energy`：总能耗（number）
- `byproducts`：副产物、概率与返还容器（textarea）
- `conditions`：解锁/转速/温度/前置条件（textarea）

## 数据、独立备份与恢复（persistence）

本项目船内业务数据归入主艇独立JSON；Minecraft内部序列化与独立档案是不同层次。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| persistence.hasBlockEntity | 需要方块实体 | boolean | 可标待定/不适用 |
| persistence.vesselOwned | 数据归属主潜艇 | boolean | 可标待定/不适用 |
| persistence.jsonModule | 主艇JSON模块名 | text | 可标待定/不适用 |
| persistence.dataVersion | 数据结构版本 | integer | 可标待定/不适用 |
| persistence.fields | 持久化字段 | rows | 可标待定/不适用 |
| persistence.authoritativeSource | 权威数据源（实际容器/独立档案等） | textarea | 可标待定/不适用 |
| persistence.backupTriggers | 备份触发与频率 | textarea | 可标待定/不适用 |
| persistence.restorePolicy | 恢复顺序、位置映射、缺失资源与回滚 | textarea | 可标待定/不适用 |
| persistence.breakMoveRefit | 拆除、物理化、改装、船体损毁时数据生命周期 | textarea | 可标待定/不适用 |
| persistence.migration | 旧版本数据迁移 | textarea | 可标待定/不适用 |
| persistence.duplicationProtection | 防止复制物品/重复部署/身份冲突 | textarea | 可标待定/不适用 |

**持久化字段每条记录的字段：**

- `key`：字段键（text）
- `type`：类型（enum；string / 文本；number / 数值；boolean / 布尔；uuid / UUID；list / 列表；object / 对象）
- `initialValue`：默认值说明（text）
- `meaning`：含义/单位/范围（textarea）
- `backup`：纳入独立备份（boolean）
- `sync`：同步客户端（boolean）

## 跨Mod兼容与实现边界（integration）

只借用需要的能力；不要全量继承黑曜石配方或传送门身份。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| integration.adapters | 兼容适配 | rows | 可标待定/不适用 |
| integration.excludedBehaviors | 明确不继承的行为（每行一项） | lines | 可标待定/不适用 |
| integration.performance | 扫描范围、tick预算、缓存与批处理要求 | textarea | 可标待定/不适用 |
| integration.network | 同步频率、包大小、校验与服务端权限 | textarea | 可标待定/不适用 |
| integration.knownConflicts | 已知冲突与待核对项 | textarea | 可标待定/不适用 |

**兼容适配每条记录的字段：**

- `modId`：目标modId（text）
- `version`：核对版本（text）
- `capability`：要兼容的属性/接口（text）
- `reference`：参考方块/设备（text）
- `mechanism`：标签/JSON/接口/事件/待研究（textarea）
- `fallback`：依赖缺失时行为（textarea）
- `evidence`：源码/资源路径/实测证据（textarea）

## 验收与开放问题（acceptance）

写成能验证的结果，覆盖失败场景和存档重进。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| acceptance.tests | 验收用例 | rows | 可标待定/不适用 |
| acceptance.openQuestions | 未决问题 | lines | 可标待定/不适用 |
| acceptance.nonGoals | 本次不做的内容 | lines | 可标待定/不适用 |
| acceptance.completionCriteria | 完成定义 | textarea | 可标待定/不适用 |
| acceptance.notes | 补充说明 | textarea | 可标待定/不适用 |

**验收用例每条记录的字段：**

- `name`：用例名（text）
- `setup`：前提/环境（textarea）
- `steps`：操作步骤（textarea）
- `expected`：期望结果（textarea）
- `priority`：优先级（enum；required / 必须通过；recommended / 建议；later / 后续）

## 怪物与 Boss（monster）

区分当前原型属性与待定目标。自然刷新、技能和阶段需要单独开发；填写规格不会自动实现。

| 字段键 | 中文名 | 类型/选项 | 必填 |
| --- | --- | --- | --- |
| monster.stage | 制作状态 | enum：prototype / 原型（数值待定）；approved / 已确认设计 | 可标待定/不适用 |
| monster.base | 当前原型行为与模型 | enum：squid / 鱿鱼（游动、逃避）；guardian / 守卫者（游动、光束攻击）；drowned / 溺尸（水陆寻路、近战）；custom / 自定义，需开发 | 可标待定/不适用 |
| monster.region | 目标地区 | text | 可标待定/不适用 |
| monster.boss | 是否 Boss | boolean | 可标待定/不适用 |
| monster.plannedHealth | 方案目标生命点 | number | 可标待定/不适用 |
| monster.health | 当前游戏生命点 | number | 可标待定/不适用 |
| monster.width | 碰撞宽度（格） | number | 可标待定/不适用 |
| monster.height | 碰撞高度（格） | number | 可标待定/不适用 |
| monster.scale | 模型缩放 | number | 可标待定/不适用 |
| monster.speed | 移动速度属性（非格每秒） | number | 可标待定/不适用 |
| monster.attackDamage | 基础攻击伤害（不含技能） | number | 可标待定/不适用 |
| monster.armor | 护甲点 | number | 可标待定/不适用 |
| monster.followRange | 索敌距离（格） | number | 可标待定/不适用 |
| monster.knockbackResistance | 击退抗性 | number | 可标待定/不适用 |
| monster.naturalSpawn | 自然生成 | boolean | 可标待定/不适用 |
| monster.spawnRules | 群系、深度、光照、数量、刷新和脱战 | textarea | 可标待定/不适用 |
| monster.behavior | 目标行为与攻击预警 | textarea | 可标待定/不适用 |
| monster.implementedBehavior | 当前已实现行为及限制 | textarea | 可标待定/不适用 |
| monster.skills | 技能、触发、冷却、射程、伤害与反制 | textarea | 可标待定/不适用 |
| monster.phases | Boss 阶段与切换条件 | textarea | 可标待定/不适用 |
| monster.targetPolicy | 目标选择、船体和玩家交互 | textarea | 可标待定/不适用 |
| monster.resistances | 弱点、免疫与抗性 | textarea | 可标待定/不适用 |
| monster.teamScaling | 多人缩放与归属 | textarea | 可标待定/不适用 |
| monster.firstReward | 首次奖励及防重复 | textarea | 可标待定/不适用 |
| monster.drops | 击败掉落 | rows | 可标待定/不适用 |
| monster.eggPrimary | 刷怪蛋主色（六位十六进制） | text | 可标待定/不适用 |
| monster.eggSecondary | 刷怪蛋副色（六位十六进制） | text | 可标待定/不适用 |
| monster.sounds | 环境、受击、死亡和预警声音 | textarea | 可标待定/不适用 |
| monster.persistence | 保存、卸载、重载与防重复 | textarea | 可标待定/不适用 |
| monster.acceptance | 测试步骤与预期结果 | textarea | 可标待定/不适用 |

**击败掉落每条记录的字段：**

- `item`：物品ID（text）
- `min`：最小数量（integer）
- `max`：最大数量（integer）
- `chance`：概率（number）
- `conditions`：条件（textarea）
