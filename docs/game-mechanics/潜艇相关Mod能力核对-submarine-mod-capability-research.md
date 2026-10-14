# 主潜艇相关 Mod 能力核对

核对日期：2026-10-14。范围是当前工程已启用的 Minecraft 1.21.1 NeoForge 组合。以下区分发布说明、当前 JAR 内实际存在的类/资源、源码实现及仍需游戏验证的部分；没有安装额外 Mod，没有修改游戏规则。对应方案见 [唯一主潜艇与酒狐船长](主潜艇与酒狐实现方案-flagship-submarine-plan.md)。

## 核对方法与版本

读取 `mods/dbv-world/build.gradle`、本地已下载 JAR 的元数据、内嵌组件、语言与 Ponder 资源，并用 JDK `javap -public` 核对关键签名。源码按固定引用阅读；航空学主仓库未找到1.3.2版本标签，主分支源码只作结构参考，当前可用符号以1.3.2 JAR为准。本次没有编写或运行潜艇集成原型，因此“存在接口”不等于“组合恢复已通过”。

| 项目 | 工程锁定版本 | 核对依据 |
| --- | --- | --- |
| Create | 6.0.10 | 工程依赖和现有启动记录 |
| Create Aeronautics | 1.3.2，Modrinth `44pLdPGg` | [官方发布页](https://modrinth.com/mod/create-aeronautics/version/44pLdPGg)、本地JAR内嵌组件 |
| Sable | 2.0.6，`fg9dTRz9` | 本地JAR与 [2.0.6源码标签](https://github.com/ryanhcode/sable/tree/mc1.21.1-2.0.6-neoforge)，解析提交 `ad8b4d377741fbd923f4d863b2cf9e462daff9a6` |
| Deep Seas | 3.3.0，`8CV1LRTl` | 本地JAR、[官方发布记录](https://github.com/MaxCreateMC/Create-Deep-Seas/releases)、3.3.0发布提交 `3c287c548d0e247880e5fb59fb4f9e756394c35a` |
| Power Grid | 0.6.2，`jCkPHjmP` | 本地JAR、[项目源码](https://github.com/patryk3211/PowerGrid)、`v0.6.2`标签；具体NeoForge兼容行为以安装包和实测为准 |

## 航空学实际包括什么

当前下载的 Aeronautics 文件是组合包，内嵌 Simulated、Aeronautics、Offroad 三个组件。官方下载说明亦将其分为装配/控制、航空、地面载具三部分。[官方项目说明](https://modrinth.com/mod/create-aeronautics)

| 部分 | 当前包可核对的功能 | 主潜艇如何使用 |
| --- | --- | --- |
| Simulated | 物理装配器、操纵与红石元件、速度/高度/姿态传感器、导航桌、对接连接器 | 复用装配、状态读数与机械操控；导航桌和对接元件不能直接当作完整AI或改装事务系统 |
| Aeronautics | 螺旋桨轴承、陀螺螺旋桨轴承、热气系统、浮空材料等 | 按需要使用相关机械；潜航推进优先使用Deep Seas对应设备，不假定空气推进装置在水下等效 |
| Offroad | 地面车辆与轮系相关内容 | 已随包存在，主潜艇首版不依赖轮系玩法 |
| Sable前置 | 可运动的方块子空间、碰撞、质量与运动基础 | 船体适配与实时仪表直接对接这一层 |

1.3.2包内存在 `PhysicsAssemblerBlockEntity`、`SimAssemblyHelper`、`VelocitySensorBlockEntity`、`GimbalSensorBlockEntity`、`AltitudeSensorBlockEntity` 和对接/导航相关资源。源码的装配路径还处理Create活动结构，因此自研不能绕过全部装配过程后假定行为相同。[航空学源码仓库](https://github.com/Creators-of-Aeronautics/Simulated-Project)

## Sable 可复用的具体入口

| 需求 | 当前2.0.6中核实的入口 | 边界 |
| --- | --- | --- |
| 找到船体 | `SubLevelContainer.getContainer`、`getSubLevel(UUID)` | 需要维度与加载状态，不能用船名作为唯一键 |
| 物理身份和姿态 | `getUniqueId()`、`logicalPose()`、`boundingBox()` | 区分局部plot坐标与世界坐标 |
| 质量和重心 | `ServerSubLevel.getMassTracker()`、`getSelfMassTracker()`；`MassData` | 合并质量可能已经包含活动部件，不能重复求和；源码质量单位标为kpg，不直接标真实吨数 |
| 线速度和角速度 | `RigidBodyHandle.of()`、`getLinearVelocity()`、`getAngularVelocity()` | 在安全物理阶段读取；需核对单位及服务端时序，不跨线程随意访问句柄 |
| 自研海流等力 | 刚体冲量及排队力组接口 | 只用于有明确物理含义的力；不能把AI开船做成无限推力 |
| 生命周期监控 | `SubLevelContainer.addObserver()`、`SubLevelObserver` | 有增加、移除前通知、tick；移除原因包括UNLOADED和REMOVED，后者也不能直接等价战斗损毁 |
| 自研身份标签 | `getUserDataTag()`、`setUserDataTag()` | 在命名空间子标签内合并数据，不覆盖其他附属的标签；拷贝蓝图时仍要服务器校验身份 |
| 船体保存/加载 | `SubLevelSerializer.toData/fromData/fullyLoad` | 是底层工具，不是完整的跨Mod事务备份API |
| 蓝图坐标映射 | `SubLevelSchematicSerializationContext` | 包含UUID和位置转换；部分入口标注Internal，适配必须锁版本 |
| 船体分裂 | `getSplitFromSubLevel()` 等 | 可辅助追踪碎片；不能把临时字段当永久主艇登记表 |

依据：[ServerSubLevel](https://github.com/ryanhcode/sable/blob/ad8b4d377741fbd923f4d863b2cf9e462daff9a6/common/src/main/java/dev/ryanhcode/sable/sublevel/ServerSubLevel.java)、[RigidBodyHandle](https://github.com/ryanhcode/sable/blob/ad8b4d377741fbd923f4d863b2cf9e462daff9a6/common/src/main/java/dev/ryanhcode/sable/api/physics/handle/RigidBodyHandle.java)、[Observer](https://github.com/ryanhcode/sable/blob/ad8b4d377741fbd923f4d863b2cf9e462daff9a6/common/src/main/java/dev/ryanhcode/sable/api/sublevel/SubLevelObserver.java)。

序列化源码保存UUID、位置、plot、速度、加载依赖和用户标签。plot保存方块状态、方块实体及部分调度/光照/附件数据；普通实体不在这个方法返回的同一份标签中。加载还涉及plot位置分配，不能只修改外层UUID就保证整艇可复制到另一处。因此原位置精确撤销、船坞异地重建、跨世界导入是三个不同难度的任务；首版仅支持同世界船坞恢复。[序列化实现](https://github.com/ryanhcode/sable/blob/ad8b4d377741fbd923f4d863b2cf9e462daff9a6/common/src/main/java/dev/ryanhcode/sable/sublevel/storage/serialization/SubLevelSerializer.java)、[plot保存实现](https://github.com/ryanhcode/sable/blob/ad8b4d377741fbd923f4d863b2cf9e462daff9a6/common/src/main/java/dev/ryanhcode/sable/sublevel/plot/ServerLevelPlot.java)

## Deep Seas 比基础潜航多出的能力

3.3.0安装包内有潜艇推进器、水推进器、压载舱与阀门、电解设备、氧气扩散器、耐压玻璃、压力表、艇载电脑、声呐、泵控制器及诊断界面。包内还有High Seas和Abyss相关资源；本方案不自动启用新的世界生成内容。

| 能力 | 已核对内容 | 对方案的影响 |
| --- | --- | --- |
| 材料耐压 | `HullStrengthConfig` 使用 `config/submarine_hull.json`，属性含最大水深和内爆概率 | 现有材料压力锁可先从配置开始，具体材料值与科技路线统一 |
| 裂纹与破舱 | `SubmarinePressureSystem` 提供深度、裂纹数、破舱、最弱耐压部位等查询 | 可作为监控与报警数据；裂纹逐块状态和缓存需要单独验证恢复 |
| 舱室与进水 | `CompartmentTracker`、`FloodSystem` 可查询舱室、结构版本及进水单元 | 复用扫描，不重新每tick全艇洪泛；内存缓存不等于持久存档 |
| 危急内爆 | `ImplosionSequence.buildUp` 和进行中查询 | 是可接入的事故触发点；未见通用可取消的“所有损毁前备份”事件 |
| 定深 | `CommandSubBlockEntity` 的目标深度、泵命令和更新入口 | 酒狐可通过适配器使用现有控制器；变更必须服务端校验并发送正常同步 |
| 海床跟随 | 电脑的autopilot根据海床及前方采样调整深度目标 | 不能宣传为自主水平导航或复杂洞穴避障；其速度档位影响垂直控制，不是通用航速设置 |
| 诊断 | `HullDiagnostic`、`DiagnosticPayload` 和电脑面板 | 可借用结果和规则，补充船坞改装的材料、连接、唯一性检查 |

依据：[艇载电脑](https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/block/entity/CommandSubBlockEntity.java)、[压力系统](https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/system/SubmarinePressureSystem.java)、[船壳配置](https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/config/HullStrengthConfig.java)、[进水系统](https://github.com/MaxCreateMC/Create-Deep-Seas/blob/3c287c548d0e247880e5fb59fb4f9e756394c35a/src/main/java/com/maxenonyme/createsubmarine/submarine/compartment/FloodSystem.java)。上述关键查询与电脑控制签名也已在本地3.3.0 JAR核对。

压力测深源码存在有限扫描范围，原版世界高度亦有限。未来数百格以上的深海、特殊维度海平面和大艇姿态下的水深定义需要一起调整，不能只把目标深度输入框改成大数字。

## 其他已装 Mod 的角色

| Mod | 对主艇的作用 | 不应假定的能力 |
| --- | --- | --- |
| Create | 传动、机械装置、容器、活动结构和蓝图工具 | 蓝图等于所有库存、实体与外部连接的完整回档 |
| Power Grid 0.6.2 | 电力网络、设备和电气行为；本地包存在Sable装配兼容Mixin | 电线自动跨坐标恢复、任意端点都支持移动船体；需分别测船内线与岸电 |
| Architectury | 跨平台基础依赖 | 主艇唯一性或备份业务能力 |
| WorldEdit | 开发阶段建造、选区与结构处理 | 原生undo能撤销物理载体、酒狐与跨Mod资源账 |
| KubeJS、Rhino、ProbeJS | 配方、轻量任务脚本、类型资料 | 核心恢复事务应交给Java；脚本热重载不能影响唯一主艇登记 |
| Jade | 展示主艇终端或设备信息的扩展入口 | 已自动做出整艇仪表和报警系统 |
| spark | 测量监控、扫描、快照和恢复的性能 | 自动解决大艇性能瓶颈 |
| 自研DBV World | 当前仅有岛屿与临时建设规则等基础 | 尚未具有主潜艇档案或酒狐控制 |

Power Grid 官方将其定位为基于物理的电路系统，不等同于统一FE电池系统；面板应读取实际设备和电网数据，不虚构全艇总电量。[Power Grid项目](https://github.com/patryk3211/PowerGrid)

## 额外候选与需要验证的边界

发现社区 [Sable Schematic API](https://github.com/Rew1nd-dev/sable-schematic-api) 提供载体蓝图与多种连接映射相关实现，可进一步评估为导入导出候选。它不是Sable官方接口，也未在本项目安装或获新增授权；其文档列有旧航空学兼容测试包，不能据此承诺当前1.3.2组合可用。首版方案不把它当作必须安装项。

优先做五项验证：完整船体快照的实体/连接边界；异地重建后的UUID与plot坐标重映射；Deep Seas裂纹与进水恢复；Power Grid电线与设备状态；资源跨船转移及残骸打捞的防重复结算。在这些项目通过前，只能称为可实现的架构方案，不能宣称一键完整恢复已经可用。
