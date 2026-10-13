# 游戏机制与现有模组实现分工

核查日期：2026-10-13。目标平台严格限定为 **Minecraft 1.21.1 NeoForge**。依据为作者项目说明、具体版本文件、发布记录及部分公开源码；尚未进行整包启动和多人实测。版本存在只证明有候选文件，不证明这些文件能直接一起运行。

结论是：造船、潜浮、基础耐压、供氧、声呐和部分辅助驾驶可以复用现有功能。我们重点编写海域规则、任务和控制程序，并开发完整航线规划、战斗决策、Boss 与运动船体交互等缺口。不要先重写一套潜艇物理或所有传感器。

覆盖范围对应[功能架构](functional-architecture.md)、[海域准入机制](ocean-access-gates.md)和[艇载 AI 与协同战斗](onboard-ai-and-cooperative-combat.md)。

## 实现类别

| 标记 | 含义 | 我们需要做什么 |
| --- | --- | --- |
| A 现成功能 | 作者已发布对应基础功能 | 安装、建造、配置和验证，不预设需要重写代码 |
| B 配置与内容 | 模组提供机制，但没有我们的海域、配方和成长内容 | 配置、数据包、结构制作、任务和数值设计 |
| C 自写脚本 | 已有读写接口可搭建原型，但完整行为需要编程 | 编写 Lua 或整合脚本；接口不足时转 Java 桥接 |
| D 自研扩展 | 当前证据未覆盖我们的完整规则 | 优先做独立 Java 附属，复用物理、设备和事件 |
| 待验证 | 版本或功能存在，但关键接口、兼容或行为没有确认 | 先制作最小测试，失败后才决定适配或替换 |

一项机制可以包含多个类别。D 是基于当前需求与已查候选的工程判断，不代表互联网上绝对没有同类项目。C 也属于开发工作，但通常不需要新建完整 Java 模组。数据包作为 1.21.1 内容制作方式使用，不引入其他加载器。

## 三种核心限制与环境机制

| 编号 | 机制 | 可复用模组与已知覆盖 | 分工 | 仍需实现或验证 |
| --- | --- | --- | --- | --- |
| G01 | 大洋布局与深海空间 | [Continents][continents] 提供大陆布局；[Deeper Oceans][deeper-oceans] 或 [Tectonic][tectonic] 提供深海地形 | B | 调整海陆、航道、出生资源与遗迹分布；后两者不能直接叠加。以巨艇尺寸验地形 |
| G02 | 浅海到深渊的资源与危险分层 | 上述地形与 [In Control][in-control] 可作为生成和刷怪基础 | B，特殊规则 D | 编写区域资源、敌人、奖励和阶段内容；精确深度条件需要核对事件能力 |
| G03 | 洋流推动潜艇 | [Sablewaves][sablewaves] 作者明确描述洋流作用于 Sable 船只和潜艇，含潮流、环流与深层反流 | A＋待验证 | 先测 Sable 2.0.6 与 Deep Seas 3.3.0 的受力、深水效果和同步；有流场不等于已有指定关卡 |
| G04 | 指定海峡的速度锁 | Sablewaves 有流速强度与上限配置 | B 原型，D 区域扩展 | 全局流速可先调；精确区域、方向、缓流时段、路线绕行与保证可返航的关卡规则未确认现成接口。需要时扩展流场，不重复叠加第二套受力 |
| G05 | 流速仪表与返航判断 | Sablewaves 提供流场 HUD 与 Current Locator | A 仪表，C／D 预测 | 仪表不是续航规划器；预计逆流速度、能源储备及返航风险需结合本艇数据计算 |
| G06 | 材料耐压与深度锁 | [Deep Seas][create-deep-seas] 已有逐方块最大水深、失效概率、弱点判定；支持 `submarine_hull.json` | A＋B | 配置材料等级与配方即可先做压力成长；检查方块复制材质、门窗、接口和装饰是否正确参与判定，见[源码证据](#关键证据与适用边界) |
| G07 | 超深预警、弱点定位与修补 | Deep Seas 已有警报、船壳应力表现、诊断及裂纹维修基础 | A，额外损管 D | 先复用；预警到破损时间、维修物资成本、升级建议提示需要按目标补齐 |
| G08 | 舱跨度、加强筋、多层壳与疲劳寿命 | 现有逐方块耐压不能证明已有这些结构规则 | D | 若保留这些设计，开发舱室结构评估与耐压修正；首版可先采用材料等级，避免过早做复杂受力模拟 |
| G09 | 硬度、质量、耐热与耐腐蚀差异 | [Sable][sable] 提供物理基础；[载荷扩展][create-aeronautics-weight]、[Simulated Copycats][simulated-copycats] 可补充部分物理属性 | B 质量配置，D 环境耐久 | 不把硬度直接当耐压；完整耐腐蚀、耐热、设备磨损和维修难度体系仍需规则与代码 |
| G10 | 洞口宽高与转弯空间锁 | Sable／[航空学][create-aeronautics] 的载具碰撞，加上人工设计的洞口与遗迹 | A＋B | 自然几何限制即可形成尺寸锁，无需先写“方块总数门禁”；细小碰撞形状、旋转和外设必须实测 |
| G11 | 通行尺寸扫描与预警 | Deep Seas 声呐可显示周围地形与船底净空 | D 高级扫描 | “能看到地形”不等于“能保证整艇通过”；要写全艇包络、转弯路径与突出部件的通行评估 |
| G12 | 动力、燃料与续航限制 | [交错电网][power-grid]、[柴油发电机][create-diesel-generators]、[Calibrated][aeronautics-calibrated] 提供不同能源和推进设备 | A＋B | 选定一条真正消耗资源的动力链，统一 FE、电路、旋转动力与设备消耗；不是叠加模组就自动获得续航锁 |
| G13 | 剩余航程和应急能源预算 | [CC:Tweaked][cc-tweaked] 与设备外设可读取部分状态 | C，缺接口 D | 计算船速、流速、耗能与载荷影响；保留返航及生命保障能源，数据不足时显示不确定而非虚假精确值 |
| G14 | 潜水员压力、装备与深度雾 | [Crush Depth][crush-depth] 有玩家压伤、装备深潜进度、深度雾与相关配置 | A＋B＋待验证 | 与 Deep Seas 同步测试；其玩家防护不提高船壳强度。不要与其他玩家水压模组重复叠伤 |
| G15 | 机动性与精确操控 | 航空学控制部件、[陀螺稳定器][create-aeronautics-gyroscope-stabilizers]、[Avionics][create-avionics] | A 设备，B／C 调校 | 建造与调节侧推、重心和转向；稳定倾斜不等于保持位置或完成复杂洞穴导航 |
| G16 | 噪声锁、诱饵与静音航行 | 已查候选不足以证明存在完整“本艇声源—传播—海怪响应”体系 | D | 定义发动机、声呐、采矿的噪声事件与敌人感知；仅增加环境音不能形成玩法限制 |
| G17 | 高温与腐蚀海域 | 可以制作对应海域和资源，但未确认通用的 Sable 船体环境损伤方案 | B 场景，D 规则 | 防护材料、冷却、腐蚀进度、警报与损伤联动需要定制 |
| G18 | 打捞、切割、钻探与牵引 | [Create][create] 提供机械作业基础；航空学绳索、[传动连接][create-aeronautics-transmission-linkage] 可搭工程设备 | A＋B，特殊目标 C／D | 移动艇上工具作业、权限与掉落要测；任务专用残骸、回收重量和目标识别另做内容或桥接 |

## 潜艇与母子艇系统

| 编号 | 机制 | 可复用模组与已知覆盖 | 分工 | 仍需实现或验证 |
| --- | --- | --- | --- | --- |
| V01 | 方块造船、装配、物理运动 | Create＋Sable＋航空学 | A | 不重写物理引擎；测试巨艇方块实体、跨区块与多人同步 |
| V02 | 水下推进、压载与潜浮 | Deep Seas 的潜艇推进和压载设备 | A＋B | 确定船型、压载容量与推进配置，验证不同载荷与洋流中的操控 |
| V03 | 密封、供氧、气闸与排水 | Deep Seas 已有封闭舱室、氧气、减压舱与泵系统 | A＋待验证 | 逐步进水是实验选项且默认关闭；默认破口可能导致整间进水，不能直接承诺缓慢渗漏与完整损管 |
| V04 | 独立舱段损管和自动隔离 | Deep Seas 提供舱室和破损基础 | C／D | 验证门与舱段边界后，再接自动关门、分区排水、备用供氧与故障策略；不要另算一套互相矛盾的密封状态 |
| V05 | 基础船况诊断 | Deep Seas 诊断页；[CC: Deep Seas][cc-deep-seas] 暴露部分内部信息 | A，整合面板 C／D | CC 扩展的舱室查询在说明中关联创造氧气机，不能默认已有生存可用的完整船况接口 |
| V06 | 子艇停泊、供电和转运 | [Gadgets & Gizmos][create-aeronautics-gadgets-and-gizmos] 有 Ship Dock；[Aero Cables][create-aero-cables] 支持运动结构的 FE／数据连接 | A 部件＋待验证，完整流程 C／D | 测相对运动、物理约束、充电与拆离；现有码头和线缆不等于完整自动母子艇系统 |
| V07 | 碰撞伤害与维修 | [Sable 碰撞伤害][sable-create-aeronautics-collision-damage]、Deep Seas 裂纹维修 | A＋B | 调整撞礁代价；击毁部件、维修材料、Boss 伤害需统一，避免多套伤害重复作用 |
| V08 | 船内仓储、饮食与加工 | [精妙仓储][sophisticated-storage]、[精妙背包][sophisticated-backpacks]、[农夫乐事][farmers-delight]、Create | A＋B | 现成生存内容；船上设备、装卸与质量变化仍需联调 |
| V09 | 救援、失事定位与回收任务 | 航空学牵引和现有导航可作为工具 | B 场景，C／D 管理 | 求救记录、失事船身份、回收目标、权限与损失结算需要编写 |

## 艇载 AI 与协同战斗

| 编号 | 机制 | 可复用模组与已知覆盖 | 分工 | 仍需实现或验证 |
| --- | --- | --- | --- | --- |
| A01 | 自动定深与潜浮控制 | Deep Seas 的 Onboard Computer＋Pump Controller 已有目标深度控制 | A＋待验证 | 本体已有 WIP 功能，先验证，不从零写 PID；仍需测洋流和大载荷下的表现 |
| A02 | 基础海床避障 | Deep Seas 的 Autopilot 根据前方海床调整深度并限制安全潜深 | A 局部能力 | 只覆盖局部纵向避障，不是三维目的地寻路；洞顶、侧壁、死路和未知区块仍是缺口 |
| A03 | 定向、姿态与简单值守 | Avionics 提供航向、速度、姿态和执行器接口；陀螺部件可稳定倾斜 | A 部件＋C 控制 | 自写定向和位置控制；与本体定深功能明确控制权，避免两套控制器争抢压载 |
| A04 | 航点自动巡航与返航 | Avionics 可写控制程序；G&G 有航程表和寻路系统但作者限制其推进类型与船型 | C 原型，D 完整规划 | 水下三维路径、整艇尺寸、转弯制动、流场、续航约束和重规划要定制；不能把飞艇调度直接认作潜艇导航 |
| A05 | 玩家跟随、离艇值守与接应 | 现有控制接口可执行运动；基础位置通信可搭原型 | C／D | 玩家目标跟踪、停在洞口、接应点选择、失联与死亡策略需要编程 |
| A06 | 手动接管、分岗权限与状态 UI | [Aeroclaims][aeroclaims] 提供载具保护方向；CC:CBC 可切换电脑火控 | C／D | 领地保护不等于驾驶／火控／指挥权限；统一命令仲裁、即时接管与可见状态需要我们做 |
| A07 | 海床测深与基础目标显示 | Deep Seas 内置声呐；[Echo Radars][echo-radars] 有前视、测深、侧扫及成像声呐 | A＋待验证 | Echo Radars 作者标注 Sable 支持；检测到目标不意味着可直接作为服务端 AI 输入，接口需核对 |
| A08 | 自定义探测锁与未知生物识别 | 现有声呐显示提供基础 | B 参数，C／D 数据接口 | 探测距离、遮挡、发现记录、未知目标解锁与服务器可信目标数据需整合；不能直接把客户端显示当火控真值 |
| A09 | 炮塔、装填、瞄准与鱼雷 | [Create Big Cannons][create-big-cannons] 提供火炮；[CBC Military Supplement][cbcms] 提供鱼雷；[CC:CBC][cc-cbc] 提供角度、装配与开火接口 | A 武器＋C 控制＋待验证 | 验证水下弹道、运动炮架坐标、后坐力和碰撞；Echo Radars 标注的鱼雷联动范围为 CBCMS 2.1.4–2.1.x，不能直接替换为 3.0.0 |
| A10 | 自动防御与普通怪物托管 | [Create Radars][create-radars] 有雷达与炮控，Echo Radars 补充水下目标；CC:CBC 驱动炮架 | A／C 原型，D 完整规则 | 敌我过滤、普通动物保护、区域清剿、弹药保留与停止追击需定制；先确认雷达识别所选海怪 |
| A11 | 友军避让、本艇遮挡与爆炸安全 | 当前接口能发射，不证明已覆盖我们的安全火控要求 | D | 统一世界坐标与艇内坐标，做射线、提前量、射界及爆炸范围评估，判断失败就停火 |
| A12 | 玩家标记与战术指令 | CC 显示与控制接口可作原型终端 | C 原型，D 正式交互 | 集火、保护、撤退、接应快捷操作，目标同步、权限和反馈需要开发 |
| A13 | 玩家与潜艇共同打现有 Boss | [Aquamirae][aquamirae]、[Cataclysm][l_enders-cataclysm] 有现成 Boss；已有船载武器 | A 内容＋待验证，D 适配 | 先测 Boss 是否被炮弹伤害、是否识别船体、能否反击；同时打同一敌人不等于完成协同机制 |
| A14 | 破甲、弱点、装置和阶段协同 | 现有 Boss 可复用模型与基础战斗，但不承诺暴露所有阶段接口 | B／C 简单场景，D 深度修改 | 需要按 Boss 编写适配或自制 Boss；阶段事件、船艇仇恨、部位伤害与玩家专属交互不能仅靠加血量完成 |
| A15 | 战斗撤退、故障降级与离线策略 | 已有读数、控制和武器接口 | C／D | 统一状态机管理低电、漏水、失联、重载和未加载区块；停止射击与控制权恢复要服务端执行 |

## 内容与运行支持

| 编号 | 机制 | 可复用模组与已知覆盖 | 分工 | 仍需实现或验证 |
| --- | --- | --- | --- | --- |
| S01 | 海洋生物与普通遭遇 | [Hybrid Aquatic][hybrid-aquatic] 等提供水生生态 | A＋B | 海域分布、生成密度、稀有度及资源产出由整合包设计 |
| S02 | 港口、遗迹、前哨和停泊区 | 原版结构／数据包与 Create 建筑机械可制作 | B | 设计建筑与生成规则，预留母艇停靠空间；无须为了固定遗迹单独写 Java 模组 |
| S03 | 资源闭环与升级配方 | [KubeJS][kubejs]、原版配方和标签 | B／C | 确保海洋开局资源可得、材料升级无死循环；复杂船况条件需要附属事件桥接 |
| S04 | 任务、手册、图鉴与日志 | [FTB Quests][ftb-quests]、[Patchouli][patchouli] | B，动态船况目标 C／D | 编写任务与手册；若要检测安全潜深、返航或母子艇对接，需暴露相应事件。FTB 与 KubeJS 联动还要核对 XMod Compat |
| S05 | 多人战利品与奖励 | [Lootr][lootr] 与任务奖励系统 | A＋B | 统一战利品、多人参与判定；团队协同奖励不是安装即成 |
| S06 | 深海气氛与风险提示 | Deep Seas 警报、Crush Depth 雾效、Sablewaves 海面表现 | A＋B | 避免重复雾效和渲染冲突；自定义机制的报警与声音另接入 |
| S07 | 优化与持续运行 | [Sodium][sodium]、[FerriteCore][ferrite-core]、[Chunky][chunky]、[spark][spark] | A 工具＋B 调优 | 优化模组不能保证巨艇稳定；物理、AI、声呐扫描、加载与多人同步要实测 |
| S08 | 备份、权限与存档恢复 | 服务端备份流程、Aeroclaims 和原有存档机制 | B 运维，D 自研状态恢复 | 设计 AI 命令、海域规则和船艇身份的保存与恢复；备份本身不需要重写游戏物理 |

## 关键证据与适用边界

### Deep Seas 已覆盖的部分比旧简介更多

项目简介仍将部分内容写为未来计划，但 [3.3.0 所在 Release 发布记录][ds-release]包含本体电脑、声呐、基础 Autopilot 和诊断。此次以版本发布记录与 Release 源码交叉核查，不能继续按早期简介把它们全部算成自研。

- [HullStrengthConfig][ds-hull] 存储逐方块 `maxWaterDepth` 和 `implosionChance`，配置文件为 `submarine_hull.json`。
- [SubmarinePressureSystem][ds-pressure] 与 [HullDiagnostic][ds-diagnostic] 提供耐压弱点、风险及漏点诊断基础；完整的舱跨度、多层壳计算仍未确认。
- [CommandSubBlockEntity][ds-computer] 的 `followFloor` 沿运动方向采样海床并调整目标深度。它没有证明具备我们的全艇三维路线规划、洞顶检测和战斗 AI。
- [SonarReadings][ds-sonar] 是客户端目标显示逻辑。要供服务端自动开火使用，必须核对或补充服务端传感器接口。
- [SubmarineConfig][ds-config] 中 `progressiveFlooding` 默认关闭；发布记录明确提到逐步进水存在门和部分方块边界问题。因此 V03/V04 不能标为完整损管已解决。

引用源码固定在 Release 所指提交 `3c287c548d0e247880e5fb59fb4f9e756394c35a`，不以未发布开发分支的功能作为承诺。源码存在也不替代实际 JAR 的运行验证。

### 新增候选与版本组合风险

| 候选 | 本次确认的价值 | 需要优先验证的边界 |
| --- | --- | --- |
| Sablewaves | 洋流可作用于 Sable 载具，提供流向和流速观察工具 | 作者说明文件使用过工具链外 API 签名编译；先测实际链接与受力。潮汐调整有效浮力水位，不等于移动原版水方块；需测 Deep Seas 水面与压力采样是否一致 |
| Echo Radars | 专门的水下地形和目标声呐，作者标注 Sable 联动 | 需要 Create ≥6.0.10、Radars ≥0.4.9.4；客户端还列 YACL ≥3.8.2。与本体声呐择需使用，避免重复扫描负担 |
| CBC Military Supplement | 现成鱼雷管与鱼雷，减少武器从零开发量 | 2.1.4 是 Echo Radars 宣称支持范围内的候选；3.0.0 已发布但超出该范围且增加 Shaolib 前置，不能混称已兼容 |
| CC:CBC | 炮架角度、开火和状态接口 | 这是执行器接口，不附带完整敌我识别、目标选择和安全射击策略 |
| Crush Depth | 玩家水压与装备成长，补足离艇危险 | 与船壳压力分工；不要把其配置当作所有结构算法。与其他玩家水压效果择一或明确关闭重叠部分 |
| Gadgets & Gizmos | 有控制器、码头与调度方向，可借用部分设备 | 本次优先列 release 1.2.10。2.0.0-Beta-3.1 的作者明确警告不用于整合包或重要存档；其新增特性不作为发布方案。项目说明仍提示部分船型和推进类型受限 |

## 建议的自研范围

正式自研采用 Java 21，先在一个附属工程里划分内部模块，避免过早拆成多个彼此依赖的模组。表中的 Lua 是使用现有电脑外设快速验证控制逻辑的可选路径，不是必须采用的正式架构；服务端状态、权限、UI 和性能关键逻辑优先由 Java 附属统一管理。

| 内部模块 | 应负责的缺口 | 优先复用的内容 |
| --- | --- | --- |
| 海域规则 | 指定区域流场规则、准入提示、资源与危险分层事件 | Sablewaves 受力、既有地形和 Deep Seas 压力 |
| 载具适配 | 统一船艇身份、坐标转换、船况读数、设备控制与状态事件 | Sable／Deep Seas／Avionics／CC:CBC |
| 航行控制 | 航点、全艇路径、跟随接应、返航、接管与故障状态机 | 本体定深与基础避障，现有推进设备 |
| 战术控制 | 目标选择、敌我关系、安全射击、玩家标记、资源预算 | 现成声呐、雷达、炮架和鱼雷 |
| Boss 协同 | 运动船体仇恨与反击、阶段交互、玩家与艇的任务分工 | 现有 Boss 中能稳定适配的部分；必要时自制专属 Boss |
| 扩展损管 | 确认保留后的跨度、多层壳、腐蚀、热损伤和维修成本 | 现有密封、耐压、裂纹、警报与维修能力 |

首版不需要自制所有生物、物理引擎、仓储、食物、任务编辑器或声呐画面。先验证现有基础，再围绕我们特有的协同玩法补缺口。

## 实施顺序与判断条件

1. **基础潜艇**：Create＋Sable＋航空学＋Deep Seas，验证巨艇密封、潜浮、耐压、诊断、定深及声呐。检查文件内的实际依赖，不能只依赖项目首页。
2. **三种限制**：配置材料压力等级、制作窄洞和子艇路线、单独加入 Sablewaves 验证洋流。基础流场不足以做关卡时再编写区域扩展。
3. **控制原型**：用 Avionics／CC 接口验证定向、航点和接管。先确认关键传感器能在服务端读取，再决定正式 Java 接口。
4. **武器原型**：固定 CBC、Radars、Echo Radars、CBCMS、CC:CBC 的兼容版本，只测一种武器和一种敌人，核对水下命中与船体反击。
5. **协同原型**：制作一个有明确玩家动作、潜艇动作和撤退阶段的 Boss 场景。成功后再扩展巡航、区域清剿和更多 Boss。

最小测试需覆盖单人、专用服务器、重载、玩家离线、低能源、船体破损与跨区块。某个现成部件失败时，应先缩小范围定位接口问题，再决定适配或替换；不据此直接重写整个系统。

## 版本依据

下表的版本均有 1.21.1 NeoForge 文件；它是本次判断的证据表，不是已经通过启动验证的安装锁定表。前置库及其精确版本需要在装配时补齐。项目名链接指向作者说明，版本链接指向具体文件或列有该文件的作者发布列表。

| 模组 | 1.21.1 NeoForge 文件 | 发布类型 | 备注 |
| --- | --- | --- | --- |
| [Continents][continents] | [1.1.14](https://modrinth.com/mod/continents/version/bVXaqvWr) | release | 发布于 2026-07-08 |
| [Deeper Oceans][deeper-oceans] | [2.0.1-neoforge-21.1](https://modrinth.com/mod/deeper-oceans/version/rHJ7RgjW) | release | 发布于 2026-06-28 |
| [Tectonic][tectonic] | [3.0.28-neoforge-21.1](https://modrinth.com/mod/tectonic/version/n4iyW7aB) | release | 发布于 2026-09-11 |
| [In Control!][in-control] | [1.21-10.3.0](https://modrinth.com/mod/in-control/version/L0dof2WQ) | beta | 发布于 2026-09-02 |
| [Sablewaves][sablewaves] | [2.28.0](https://www.curseforge.com/minecraft/mc-mods/sablewaves/files/9140250) | release | 洋流作用于潜艇为作者说明；先测实际受力及链接 |
| [Create Deep Seas][create-deep-seas] | [3.3.0](https://modrinth.com/mod/create-deep-seas/version/8CV1LRTl) | release | 发布于 2026-10-02 |
| [Sable][sable] | [2.0.6+mc1.21.1](https://modrinth.com/mod/sable/version/fg9dTRz9) | release | 发布于 2026-10-03 |
| [Create Aeronautics: Weight][create-aeronautics-weight] | [1.2.0](https://modrinth.com/mod/create-aeronautics-weight/version/DNiXWANa) | release | 发布于 2026-08-05 |
| [Aeronautics: simulated copycats][simulated-copycats] | [1.3.2](https://modrinth.com/mod/simulated-copycats/version/6zbD44Qe) | release | 发布于 2026-08-24 |
| [Create Aeronautics][create-aeronautics] | [1.3.2+mc1.21.1](https://modrinth.com/mod/create-aeronautics/version/44pLdPGg) | release | 发布于 2026-08-29 |
| [Create: Power Grid][power-grid] | [0.6.2](https://modrinth.com/mod/power-grid/version/jCkPHjmP) | release | 发布于 2026-09-14 |
| [Create: Diesel Generators][create-diesel-generators] | [1.21.1-1.3.15](https://modrinth.com/mod/create-diesel-generators/version/UoPH8lO1) | release | 发布于 2026-07-30 |
| [Aeronautics: Calibrated][aeronautics-calibrated] | [1.4.0](https://modrinth.com/mod/aeronautics-calibrated/version/78Nl65uk) | beta | 发布于 2026-07-22 |
| [CC: Tweaked][cc-tweaked] | [1.120.2](https://modrinth.com/mod/cc-tweaked/version/1ewzHZYg) | alpha | 发布于 2026-08-08 |
| [Create Deep Seas: Crush Depth][crush-depth] | [1.11.1](https://www.curseforge.com/minecraft/mc-mods/crush-depth/files/8679814) | release | 玩家水压，需 Create 与 Deep Seas |
| [Create Aeronautics: Gyro Stabilizers][create-aeronautics-gyroscope-stabilizers] | [1.2.2](https://modrinth.com/mod/create-aeronautics-gyroscope-stabilizers/version/9P9dNVUn) | release | 发布于 2026-04-28 |
| [Create: Avionics — ComputerCraft for Aeronautics][create-avionics] | [0.6.0](https://modrinth.com/mod/create-avionics/version/9siqVCcu) | alpha | 发布于 2026-09-01 |
| [Create][create] | [6.0.10+mc1.21.1](https://modrinth.com/mod/create/version/UjX6dr61) | release | 发布于 2026-04-21 |
| [Create Aeronautics: Transmission & Linkage][create-aeronautics-transmission-linkage] | [0.2.8](https://modrinth.com/mod/create-aeronautics-transmission-linkage/version/FCZZ3ED4) | release | 发布于 2026-09-02 |
| [CC: Deep Seas][cc-deep-seas] | [1.1.1](https://modrinth.com/mod/cc-deep-seas/version/qKkaeGSS) | release | 发布于 2026-05-18 |
| [Create Aeronautics: Gadgets & Gizmos][create-aeronautics-gadgets-and-gizmos] | [1.2.10](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos/version/MgD655V5) | release | 发布于 2026-10-12；选择 release，不采用 2.0 Beta |
| [Create: Aero Cables][create-aero-cables] | [1.21-0.4.0](https://modrinth.com/mod/create-aero-cables/version/Wn81ihHo) | beta | 发布于 2026-06-18 |
| [Sable: Collision damage  (Create Aeronautics)][sable-create-aeronautics-collision-damage] | [1.0.8-Sable_2.0.0+](https://modrinth.com/mod/sable-create-aeronautics-collision-damage/version/snRIIWNz) | beta | 发布于 2026-06-15 |
| [Sophisticated Storage][sophisticated-storage] | [1.21.1-1.6.5.2176](https://modrinth.com/mod/sophisticated-storage/version/EvzAB4E6) | release | 发布于 2026-10-13 |
| [Sophisticated Backpacks][sophisticated-backpacks] | [1.21.1-3.26.12.2208](https://modrinth.com/mod/sophisticated-backpacks/version/QkVLEgt1) | release | 发布于 2026-10-13 |
| [Farmer's Delight][farmers-delight] | [1.21.1-1.3.4](https://modrinth.com/mod/farmers-delight/version/XTVZDOol) | release | 发布于 2026-08-29 |
| [Create Aeronautics: Claims][aeroclaims] | [0.9.4](https://modrinth.com/mod/aeroclaims/version/dWMRSgEu) | beta | 发布于 2026-09-08 |
| [Create: Echo Radars][echo-radars] | [1.1.1](https://www.curseforge.com/minecraft/mc-mods/create-echo-radars/files) | release | 文件名 create_echo_radars-1.1.1.jar；2026-10-04 |
| [Create Big Cannons][create-big-cannons] | [5.11.7](https://modrinth.com/mod/create-big-cannons/version/bOiDu0LS) | release | 发布于 2026-06-22 |
| [CBC Military Supplement][cbcms] | [2.1.4-1.21.1](https://modrinth.com/mod/cbc-more-shells/version/lOHOokMh) | beta | 发布于 2026-08-21；用于核对 Echo Radars 所述兼容范围 |
| [CC:CBC][cc-cbc] | [1.1.1](https://modrinth.com/mod/cc-cbc/version/naLCoIB4) | release | 发布于 2026-05-21 |
| [Create: Radars][create-radars] | [0.4.9.4-1.21.1](https://modrinth.com/mod/create-radars/version/AntNFNAx) | release | 发布于 2026-06-10 |
| [Aquamirae][aquamirae] | [7.2.12](https://modrinth.com/mod/aquamirae/version/RSbxcVPE) | release | 发布于 2026-10-11 |
| [L_Ender's Cataclysm][l_enders-cataclysm] | [3.33](https://modrinth.com/mod/l_enders-cataclysm/version/PsPYpoCC) | release | 发布于 2026-09-20 |
| [Hybrid Aquatic][hybrid-aquatic] | [mc1.21.1-1.7.4-neoforge](https://modrinth.com/mod/hybrid-aquatic/version/jdOU7Pzu) | release | 发布于 2026-10-04 |
| [KubeJS][kubejs] | [2101.7.2-build.377](https://modrinth.com/mod/kubejs/version/THIGFPwf) | release | 发布于 2026-09-09 |
| [FTB Quests][ftb-quests] | [2101.1.36](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge/files) | release | 选择 1.21.1 NeoForge 分支；2026-09-15 |
| [Patchouli][patchouli] | [1.21.1-93-neoforge](https://modrinth.com/mod/patchouli/version/BIogJv2D) | release | 发布于 2026-03-08 |
| [Lootr][lootr] | [1.21.1-1.11.38.127](https://modrinth.com/mod/lootr/version/SPOrIzX8) | release | 发布于 2026-09-25 |
| [Sodium][sodium] | [mc1.21.1-0.8.13-neoforge](https://modrinth.com/mod/sodium/version/uMOpc5uV) | release | 发布于 2026-08-28 |
| [FerriteCore][ferrite-core] | [7.0.3-neoforge](https://modrinth.com/mod/ferrite-core/version/x7kQWVju) | release | 发布于 2026-01-25 |
| [Chunky][chunky] | [1.4.23](https://modrinth.com/mod/chunky/version/LuFhm4eU) | release | 发布于 2025-04-04 |
| [spark][spark] | [1.10.124-neoforge-1.21.1](https://modrinth.com/mod/spark/version/v5qtqRQi) | release | 发布于 2025-02-23 |

[continents]: https://modrinth.com/mod/continents
[deeper-oceans]: https://modrinth.com/mod/deeper-oceans
[tectonic]: https://modrinth.com/mod/tectonic
[in-control]: https://modrinth.com/mod/in-control
[sablewaves]: https://www.curseforge.com/minecraft/mc-mods/sablewaves
[create-deep-seas]: https://modrinth.com/mod/create-deep-seas
[sable]: https://modrinth.com/mod/sable
[create-aeronautics-weight]: https://modrinth.com/mod/create-aeronautics-weight
[simulated-copycats]: https://modrinth.com/mod/simulated-copycats
[create-aeronautics]: https://modrinth.com/mod/create-aeronautics
[power-grid]: https://modrinth.com/mod/power-grid
[create-diesel-generators]: https://modrinth.com/mod/create-diesel-generators
[aeronautics-calibrated]: https://modrinth.com/mod/aeronautics-calibrated
[cc-tweaked]: https://modrinth.com/mod/cc-tweaked
[crush-depth]: https://www.curseforge.com/minecraft/mc-mods/crush-depth
[create-aeronautics-gyroscope-stabilizers]: https://modrinth.com/mod/create-aeronautics-gyroscope-stabilizers
[create-avionics]: https://modrinth.com/mod/create-avionics
[create]: https://modrinth.com/mod/create
[create-aeronautics-transmission-linkage]: https://modrinth.com/mod/create-aeronautics-transmission-linkage
[cc-deep-seas]: https://modrinth.com/mod/cc-deep-seas
[create-aeronautics-gadgets-and-gizmos]: https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos
[create-aero-cables]: https://modrinth.com/mod/create-aero-cables
[sable-create-aeronautics-collision-damage]: https://modrinth.com/mod/sable-create-aeronautics-collision-damage
[sophisticated-storage]: https://modrinth.com/mod/sophisticated-storage
[sophisticated-backpacks]: https://modrinth.com/mod/sophisticated-backpacks
[farmers-delight]: https://modrinth.com/mod/farmers-delight
[aeroclaims]: https://modrinth.com/mod/aeroclaims
[echo-radars]: https://www.curseforge.com/minecraft/mc-mods/create-echo-radars
[create-big-cannons]: https://modrinth.com/mod/create-big-cannons
[cbcms]: https://modrinth.com/mod/cbc-more-shells
[cc-cbc]: https://modrinth.com/mod/cc-cbc
[create-radars]: https://modrinth.com/mod/create-radars
[aquamirae]: https://modrinth.com/mod/aquamirae
[l_enders-cataclysm]: https://modrinth.com/mod/l_enders-cataclysm
[hybrid-aquatic]: https://modrinth.com/mod/hybrid-aquatic
[kubejs]: https://modrinth.com/mod/kubejs
[ftb-quests]: https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge
[patchouli]: https://modrinth.com/mod/patchouli
[lootr]: https://modrinth.com/mod/lootr
[sodium]: https://modrinth.com/mod/sodium
[ferrite-core]: https://modrinth.com/mod/ferrite-core
[chunky]: https://modrinth.com/mod/chunky
[spark]: https://modrinth.com/mod/spark
[ds-release]: https://github.com/MaxCreateMC/Create-Deep-Seas/releases/tag/Release
[ds-hull]: https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/config/HullStrengthConfig.java
[ds-pressure]: https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/system/SubmarinePressureSystem.java
[ds-diagnostic]: https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/system/HullDiagnostic.java
[ds-computer]: https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/block/entity/CommandSubBlockEntity.java
[ds-sonar]: https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/client/SonarReadings.java
[ds-config]: https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/config/SubmarineConfig.java
