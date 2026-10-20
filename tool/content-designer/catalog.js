(function (root) {
  "use strict";
  const f = (id, label, type = "text", extra = {}) => ({ id, label, type, ...extra });
  const n = (id, label, min = 0, max = undefined) => f(id, label, "number", { min, max });
  const i = (id, label, min = 0, max = undefined) => f(id, label, "integer", { min, max });
  const b = (id, label) => f(id, label, "boolean");
  const t = (id, label) => f(id, label, "textarea");
  const l = (id, label) => f(id, label, "lines");
  const e = (id, label, options) => f(id, label, "enum", { options });
  const rows = (id, label, columns) => f(id, label, "rows", { columns });
  const groups = [
    { id: "identity", title: "基本信息", help: "先确定它是什么、叫什么、解决什么问题。注册ID一旦发布应保持稳定。", fields: [
      f("registryId", "注册ID", "text", { essential: true, placeholder: "dbv_world:iron_submarine_hull", pattern: "^[a-z0-9_.-]+:[a-z0-9/._-]+$" }),
      f("nameZh", "中文名称", "text", { essential: true }), f("nameEn", "英文名称", "text", { essential: true }),
      e("kind", "内容类型", ["block|普通方块", "machine|机器方块", "controller|控制器", "decorative|装饰方块", "item|普通物品", "tool|工具", "equipment|装备"]),
      f("category", "功能类别", "text", { essential: true, placeholder: "潜艇外壳 / 动力 / 存储 / 装饰" }),
      t("summary", "功能描述（必填）"), t("motivation", "使用场景与设计目的"),
      l("keywords", "搜索关键词（每行一个）"), f("author", "设计者"), f("targetRelease", "计划加入版本"),
      l("creativeTabs", "创造分类ID（每行一个）"), l("tooltipsZh", "中文提示（每行一句）"), l("tooltipsEn", "英文提示（与中文对应）")
    ] },
    { id: "platform", title: "平台与依赖", help: "使用准确版本和modId；这里填写的是要求，不代表工具已验证兼容。", fields: [
      f("minecraft", "Minecraft版本"), e("loader", "加载器", ["neoforge|NeoForge", "forge|Forge", "fabric|Fabric"]),
      f("loaderVersion", "加载器版本/范围"), i("java", "Java主版本", 8, 99), f("namespace", "所属Mod命名空间"),
      rows("dependencies", "依赖Mod", [f("modId", "modId"), f("version", "锁定版本/范围"), e("requirement", "关系", ["required|必需", "optional|可选"]), e("side", "环境", ["both|两端", "server|服务端", "client|客户端"]), t("purpose", "用途")]),
      e("installationSide", "安装端", ["both|客户端与服务端", "server|仅服务端", "client|仅客户端"]),
      t("compatibilityPolicy", "缺少依赖、版本不符时如何处理")
    ] },
    { id: "appearance", title: "外观、音效与形状", help: "资源路径只用于描述，不会读取文件。模型和纹理需要后续制作。", fields: [
      e("model", "模型", ["cube_all|六面同材质立方体", "cube|六面独立材质", "slab|半砖", "stairs|楼梯", "custom|自定义", "item|物品模型"]),
      t("visualDescription", "外形与材质描述"), f("textureAll", "共用材质资源ID"),
      rows("faceTextures", "各面材质", [e("face", "面", ["up|上", "down|下", "north|北", "south|南", "east|东", "west|西"]), f("texture", "材质资源ID"), e("rotation", "旋转角度", ["0|0°", "90|90°", "180|180°", "270|270°"])]),
      i("textureResolution", "纹理边长（像素）", 16, 2048),
      e("renderLayer", "渲染层", ["solid|不透明", "cutout|镂空", "translucent|半透明"]),
      f("mapColor", "地图颜色/参考方块"), f("soundType", "音效类型/参考方块"),
      i("lightLevel", "发光等级", 0, 15), b("occludes", "遮挡光线"), b("fullCube", "完整立方体"),
      t("collisionShape", "碰撞体定义（坐标单位与各盒范围）"), t("outlineShape", "选中框形状"),
      b("waterlogged", "支持含水"), e("facing", "朝向", ["none|无", "horizontal|水平四向", "six|六向", "axis|轴向", "custom|自定义"]),
      rows("blockStates", "额外方块状态", [f("name", "属性名"), f("values", "允许值（逗号分隔）"), f("default", "初始值"), t("meaning", "含义与状态切换")]),
      l("assetReferences", "素材路径/参考链接（每行一个）"), t("animations", "动画、粒子、动态声音")
    ] },
    { id: "harvesting", title: "采集与掉落", help: "采集硬度与爆炸抗性分别配置。硬度5不会自动得到铁块的所有行为。", fields: [
      f("referenceBlock", "采集参考方块ID"), n("hardness", "挖掘硬度（-1表示不可挖掘）", -1),
      e("tool", "有效工具", ["pickaxe|镐", "axe|斧", "shovel|锹", "hoe|锄", "shears|剪刀", "any|任意", "custom|自定义"]),
      e("minimumTier", "最低工具等级", ["none|无", "wood|木级", "stone|石级", "iron|铁级", "diamond|钻石级", "netherite|下界合金级", "custom|自定义"]),
      b("requiresCorrectTool", "正确工具才掉落"), b("dropsSelf", "掉落自身"),
      t("silkTouch", "精准采集行为"), t("fortune", "时运行为"), t("explosionLoot", "爆炸掉落行为"),
      rows("drops", "额外/替代掉落", [f("item", "物品ID"), i("min", "最小数量"), i("max", "最大数量"), n("chance", "概率0–1", 0, 1), t("conditions", "条件")]),
      n("experience", "采集经验"), t("breakSideEffects", "破坏时设备、库存与船体数据处理")
    ] },
    { id: "protection", title: "原版防护与材料", help: "特殊免疫应逐项写明，不自动复制所有参考方块标签。", fields: [
      f("referenceBlock", "防护参考方块ID"), n("explosionResistance", "爆炸抗性"),
      e("pistonReaction", "活塞反应", ["normal|可推动", "block|不可推动", "destroy|推动时破坏", "push_only|只允许推", "ignore|忽略"]),
      b("dragonImmune", "末影龙破坏免疫"), b("witherImmune", "凋灵破坏免疫"),
      i("flammability", "可燃性", 0), i("fireSpread", "火焰蔓延", 0),
      n("slipperiness", "原版脚下摩擦系数"), n("speedFactor", "行走速度倍率"), n("jumpFactor", "跳跃倍率"),
      b("replaceable", "可被放置替换"), b("gravityAffected", "受原版重力下落"),
      l("blockTags", "方块标签"), l("itemTags", "物品标签"), t("specialInteractions", "活塞之外的搬运、粘连及特殊交互")
    ] },
    { id: "physics", title: "航空学 / Sable物理", help: "独立于原版属性。质量使用引擎单位；整船升力和航速不能直接当成单块属性。", fields: [
      f("referenceBlock", "物理参考方块ID"), e("bindingPolicy", "参考值策略", ["snapshot|锁定核对时的值", "follow_runtime|跟随参考方块运行时值", "custom|独立指定"]),
      n("mass", "单块质量"), n("volume", "物理体积"), n("friction", "物理碰撞摩擦"),
      n("restitution", "碰撞回弹", 0, 1), b("fragile", "易碎标记"),
      e("inertia", "惯性", ["engine|由引擎按质量和几何计算", "custom|自定义，需说明"]),
      t("customInertia", "自定义惯性/质心说明"), f("floatingMaterial", "特殊漂浮材质ID（无则设不适用）"),
      n("floatingScale", "特殊漂浮缩放"), b("assemblyAllowed", "允许装配为物理船"),
      t("connections", "胶水、轴承、关节及连接识别"), t("damageBehavior", "碰撞损坏/脱落行为"),
      t("overridePolicy", "标签、数据包与运行时覆盖优先级")
    ] },
    { id: "pressure", title: "深海学耐压与密闭", help: "耐深是材料参数，不是整船安全下潜承诺。概率参数不是每秒损坏概率。", fields: [
      f("referenceBlock", "耐压参考方块ID"), e("bindingPolicy", "耐压参考策略", ["snapshot|锁定值", "follow_runtime|跟随黑曜石等参考块", "custom|独立指定"]),
      i("maxWaterDepth", "材料耐深（格）", 1), n("implosionChance", "压溃概率参数", 0, 1),
      b("useGlobalMultipliers", "遵循上游全局倍率"), b("sealsCompartment", "作为密闭舱壁"),
      t("leakBehavior", "损坏、开门、含水时的漏水行为"), t("oxygenBehavior", "制氧、供氧、耗氧或无作用"),
      t("configurationMigration", "已有配置、缓存和客户端同步策略")
    ] },
    { id: "item", title: "物品、工具与装备", help: "普通方块也有对应物品；没有耐久的方块物品请将耐久设为不适用。", fields: [
      i("maxStackSize", "最大堆叠", 1, 99), i("durability", "耐久", 1),
      b("fireResistant", "物品实体耐火"), e("rarity", "稀有度", ["common|普通", "uncommon|罕见", "rare|稀有", "epic|史诗"]),
      f("equipmentSlot", "装备槽"), n("attackDamage", "攻击伤害"), n("attackSpeed", "攻击速度", -4),
      n("armor", "护甲"), n("toughness", "韧性"), n("knockbackResistance", "击退抗性", 0, 1),
      l("repairIngredients", "修复材料ID/标签"), l("enchantments", "允许/禁止附魔说明"), t("useBehavior", "物品使用、冷却与消耗行为")
    ] },
    { id: "interaction", title: "功能、操作与界面", help: "功能应写清触发、条件、结果与失败情况，避免只有一句模糊描述。", fields: [
      rows("actions", "功能规则", [f("name", "功能名"), f("trigger", "触发：右键/潜行右键/红石等"), t("conditions", "前置条件"), t("steps", "执行步骤"), t("success", "成功结果"), t("failure", "失败结果与回滚"), f("feedbackZh", "中文反馈"), i("cooldownTicks", "冷却tick"), e("executionSide", "执行端", ["server|服务端权威", "client|客户端显示", "both|两端协作"])]),
      t("ui", "界面布局、页签、按钮、数据显示与刷新"),
      t("permissions", "玩家/队伍/管理员权限"), t("multiplayer", "多人同时操作、距离与并发规则"),
      t("redstone", "红石输入、输出、比较器"), t("automation", "漏斗、机械动力、管线等自动化"),
      t("accessibility", "提示语言、音效/闪烁开关、色盲/键盘操作")
    ] },
    { id: "machine", title: "设备接口与运行", help: "未涉及的接口设为不适用。每个接口应说明单位、方向、容量和规则。", fields: [
      rows("ports", "接口", [f("name", "接口名"), e("resource", "资源类型", ["item|物品", "fluid|流体", "energy|FE能量", "kinetic|旋转应力", "oxygen|氧气", "signal|信号"]), e("direction", "流向", ["input|输入", "output|输出", "both|双向"]), f("faces", "连接面"), n("capacity", "容量"), f("unit", "单位"), n("ratePerTick", "每tick速率"), t("filters", "过滤/允许内容"), t("rules", "连接与运行规则")]),
      i("inventorySlots", "内部物品槽数"), i("tankCount", "内部流体罐数"),
      n("energyPerTick", "运行能耗 FE/t"), n("stressImpact", "应力消耗系数"),
      n("stressCapacity", "应力供给系数"), n("minimumRpm", "最低转速"), n("maximumRpm", "最高转速"),
      t("operatingStates", "停止、运行、缺料、满载、故障等状态"),
      t("multiblock", "多方块结构与控制器规则"), t("failureModes", "过载、断电、爆炸及安全行为")
    ] },
    { id: "acquisition", title: "配方、获取与平衡", help: "配方是设计需求。输入请逐行写清ID/标签和数量；不会在此自动转换为游戏配方。", fields: [
      rows("recipes", "配方", [f("id", "配方ID"), f("type", "配方类型ID"), t("ingredients", "材料/数量/标签，每行一项"), t("pattern", "有序配方排列/步骤"), f("result", "产物ID"), i("count", "产物数量", 1), i("durationTicks", "耗时tick"), n("energy", "总能耗"), t("byproducts", "副产物、概率与返还容器"), t("conditions", "解锁/转速/温度/前置条件")]),
      t("otherSources", "战利品、交易、任务、生成等获取方式"),
      t("progression", "科技阶段与解锁条件"), t("balance", "成本、优势、短板与限制"),
      t("recipeViewer", "JEI/JER展示说明")
    ] },
    { id: "persistence", title: "数据、独立备份与恢复", help: "本项目船内业务数据归入主艇独立JSON；Minecraft内部序列化与独立档案是不同层次。", fields: [
      b("hasBlockEntity", "需要方块实体"), b("vesselOwned", "数据归属主潜艇"),
      f("jsonModule", "主艇JSON模块名"), i("dataVersion", "数据结构版本", 1),
      rows("fields", "持久化字段", [f("key", "字段键"), e("type", "类型", ["string|文本", "number|数值", "boolean|布尔", "uuid|UUID", "list|列表", "object|对象"]), f("initialValue", "默认值说明"), t("meaning", "含义/单位/范围"), b("backup", "纳入独立备份"), b("sync", "同步客户端")]),
      t("authoritativeSource", "权威数据源（实际容器/独立档案等）"),
      t("backupTriggers", "备份触发与频率"), t("restorePolicy", "恢复顺序、位置映射、缺失资源与回滚"),
      t("breakMoveRefit", "拆除、物理化、改装、船体损毁时数据生命周期"),
      t("migration", "旧版本数据迁移"), t("duplicationProtection", "防止复制物品/重复部署/身份冲突")
    ] },
    { id: "integration", title: "跨Mod兼容与实现边界", help: "只借用需要的能力；不要全量继承黑曜石配方或传送门身份。", fields: [
      rows("adapters", "兼容适配", [f("modId", "目标modId"), f("version", "核对版本"), f("capability", "要兼容的属性/接口"), f("reference", "参考方块/设备"), t("mechanism", "标签/JSON/接口/事件/待研究"), t("fallback", "依赖缺失时行为"), t("evidence", "源码/资源路径/实测证据")]),
      l("excludedBehaviors", "明确不继承的行为（每行一项）"),
      t("performance", "扫描范围、tick预算、缓存与批处理要求"),
      t("network", "同步频率、包大小、校验与服务端权限"),
      t("knownConflicts", "已知冲突与待核对项")
    ] },
    { id: "acceptance", title: "验收与开放问题", help: "写成能验证的结果，覆盖失败场景和存档重进。", fields: [
      rows("tests", "验收用例", [f("name", "用例名"), t("setup", "前提/环境"), t("steps", "操作步骤"), t("expected", "期望结果"), e("priority", "优先级", ["required|必须通过", "recommended|建议", "later|后续"])]),
      l("openQuestions", "未决问题"), l("nonGoals", "本次不做的内容"),
      t("completionCriteria", "完成定义"), t("notes", "补充说明")
    ] }
  ];
  groups[0].fields.find(x => x.id === "kind").essential = true;
  groups[0].fields.find(x => x.id === "summary").essential = true;
  for (const key of ["minecraft", "loader", "loaderVersion", "java", "namespace"])
    groups[1].fields.find(x => x.id === key).essential = true;
  const api = { version: 1, groups };
  root.ContentCatalog = api;
  if (typeof module !== "undefined") module.exports = api;
})(typeof globalThis !== "undefined" ? globalThis : this);
