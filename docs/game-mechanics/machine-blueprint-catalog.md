# 起始岛机器蓝图库

更新于 2026-10-14。目标为 Minecraft 1.21.1、NeoForge 21.1.259、Create 6.0.10。共整理 41 个作品页面；按同功能比较后，选择当前阶段优先 6 项、按需追加 4 项，其余留作替代或后续参考。

**优先候选已经核对作者标注平台、网页尺寸、具体下载项和部署注意事项，但尚未在本项目逐台导入运行。清单不把“可能跨版本”写成“已验证可用”。** 下载文件本次未缓存；锅炉下载请求超时，网页数据可正常读取。后续应先下载并检查 NBT，再在正式开发环境验收。

## 尺寸和版本如何读

- 本体统一写 X×Y×Z，Y 是高度；不能直接把标题里的三个数字当作长宽高。用地写 X×Z。所有用地均为设计预算，包含维护、接线、局部缓存，仍需运行范围验证。
- 页面标注0的尺寸视为缺失；多文件作品的整页尺寸不保证适用于每个文件。单片、主体、外置供料和旋转扫掠范围分别计算。
- 页面“可用／不可用”投票按钮及其版本列表不是验证结果；有同意/反对计数的记录也只是站点用户反馈。
- Mod只采用当前批准清单。旧Forge/Fabric蓝图若只使用目标环境同名方块和同样机制，仍可迁移；必须检查方块状态、物品组件、过滤、流体、移动结构及配方。不能仅凭加载器标签排除，也不能仅凭能打印认定能生产。
- 下载项通过完整路径区分：detail/1413是树场页面，download/1413是齿轮厂的自储存文件。

## 当前选择

| 功能 | 当前首选 | 同功能对照 | 选择理由 |
| --- | --- | --- | --- |
| 动力 | CMS-967 四级锅炉 | CMS-218 旧锅炉 | 967有1.21.1声明及6.0.8站点反馈；先核负载再启动 |
| 木材 | CMS-1413 云杉竹木树场 | CMS-649 纯云杉；CMS-113旋转树场 | 1413作者明确列6.0.10，满箱停机；不需要竹子则649 |
| 铁料 | CMS-993 石磨刷铁机 | CMS-695 集成刷铁机 | 993体积及产量适合当前；695作为需求扩大后的替换 |
| 安山合金 | CMS-840 无黄铜主体 | CMS-588 岩浆版 | 已区分1.21主体和扩容片；前期不承担高应力高产路线 |
| 通用加工 | CMS-1025 的1.21.1文件1470 | CMS-631/651/656/989等分立单元 | 先共用加工能力；变体尺寸仍待复量，失败则换分立单元 |
| 仓储 | CMS-1003 V3 | CMS-372双层仓库 | 锁定修正版和库存重绑步骤，先小规模收发 |
| 后续当前需求 | CMS-1005、1093、700、983 | 表内对应同类 | 仅当工作站或供应瓶颈确实需要时分别加黄铜、精密构件、机壳、齿轮 |

## 首批优先蓝图

| 编号和来源 | 功能与作者 | 本体 X×Y×Z | 作者标注版本 | 建议用地 X×Z | 具体下载 | 使用说明与兼容事项 |
| --- | --- | --- | --- | --- | --- | --- |
| [CMS-967 3x3x5 6W5应力 地板型四级锅炉 V1.0](https://www.creativemechanicserver.com/detail/967/) | 动力；熊楚心 | 3×5×3 | 1.21.1 NeoForge / Create 6.0.8 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1450/) | 页面有 6.0.8 同意1/反对0记录，非本项目实测。补水、燃料；转速控制器调10后手摇启动。作者说明动力臂配置已修正，仍复核取料点。标称65536 SU，不代表整个工厂够用。 |
| [CMS-1413 无骨粉云杉竹木树场](https://www.creativemechanicserver.com/detail/1413/) | 林业；LETIT | 11×6×11 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.10（另标Fabric） | 32×32 | [下载项](https://www.creativemechanicserver.com/download/2119/) | 含满箱停机、双库存。补树苗与竹子，设置库存及物流网络；树冠高度、树木生长空间和机械扫掠范围另测，11×6×11不是完整运行空间。 |
| [CMS-993 前期可用的石磨刷铁机](https://www.creativemechanicserver.com/detail/993/) | 铁料；4022sun | 8×8×5 | 1.21.1 NeoForge / Create 6.0.8 | 16×16 | [下载项](https://www.creativemechanicserver.com/download/1426/) | 无黄铜，作者要求Create 6.0以上的刷石机制。核对水/岩浆、转向和铁粒收集，先单台低产运行。作者标称1800铁粒/小时，不能写成1800铁锭。 |
| [CMS-840 B1_D_7x7无黄铜安山合金机](https://www.creativemechanicserver.com/detail/840/) | 安山合金；夭夭 | 7×7×1 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.4 | 16×16 | [下载项](https://www.creativemechanicserver.com/download/1178/) | 选1.21主体 download/1178，主体文件名7×7×2；网页7×7×1描述单片。扩容片1179；1180/1181是1.20.1文件。先装主体，输入圆石，核对流体和风车启用。 |
| [CMS-1025 多功能多倍速地面式工作站](https://www.creativemechanicserver.com/detail/1025/) | 综合加工；multitouches | 12×9×14 | 1.21.1 NeoForge 等 / 6.0.0–6.0.9 与0.5.1系列 | 24×24 | [下载项](https://www.creativemechanicserver.com/download/1470/) | 选作者注释“这个是1.21.1的”的去设施化6.0文件1470，不能默认首个1467就是目标文件。12×9×14是整页尺寸，1470变体未单独量取，24×24为暂估。逐项调好过滤、转向、加热与流体，只开放当前要用工位。 |
| [CMS-1003 物流式简约仓库 V3](https://www.creativemechanicserver.com/detail/1003/) | 仓储；LETIT | 9×11×3 | 1.21.1 NeoForge、1.20.1 / 6.0.0–6.0.8 | 16×16 | [下载项](https://www.creativemechanicserver.com/download/1480/) | 选V3文件1480；1437/V1和1446/V2作者不推荐。重新勾选显示链接器并绑定本世界库存网络；旋转后调转速控制器及鼓风机方向。尺寸是整页标注，V3需复量。 |

## 当前按需追加

| 编号和来源 | 功能与作者 | 本体 X×Y×Z | 作者标注版本 | 建议用地 X×Z | 具体下载 | 使用说明与兼容事项 |
| --- | --- | --- | --- | --- | --- | --- |
| [CMS-1005 物流式六倍速黄铜机 V2](https://www.creativemechanicserver.com/detail/1005/) | 黄铜；LETIT | 3×4×5 | 1.21.1 NeoForge、1.20.1 / 6.0.0–6.0.8 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1439/) | 输入铜、锌、燃料；按视频配置物流订货与加热。建设世界不产铜锌，施工验收使用明确标记的测试原料；基础工作站能处理时不重复建。 |
| [CMS-1093 5x4x3精密构件机](https://www.creativemechanicserver.com/detail/1093/) | 精密构件；World工作室 | 4×3×5 | 1.21.1 NeoForge / Create 6.0.6 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1541/) | 作者说明无附属；输入原木、安山合金、铁、金。核对各盆与机械手分配、失败副产物出口和满箱停机。网页“内置”应力不等于已证明有自持动力源。 |
| [CMS-700 物流式极简四机壳机 新版](https://www.creativemechanicserver.com/detail/700/) | 机壳；LETIT | 2×6×4 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.6 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1360/) | 选新版1360，旧文件1032。背后漏斗的去皮原木过滤改为本岛木材；供应合金、铜、黄铜、坚固板，后者未供应时只启用能生产的机壳。 |
| [CMS-983 物流式大小齿轮厂 V2](https://www.creativemechanicserver.com/detail/983/) | 齿轮；LETIT | 4×4×5 | 1.21.1 NeoForge、1.20.1 / 6.0.0–6.0.8 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1413/) | 当前选自储存文件1413，后续物流版1411/定量发包1412。木板过滤必须与工厂仪表一致，输入木板和传动杆。注意download/1413与detail/1413不是同一作品编号。 |

## 同功能替代与跨版本候选

| 编号和来源 | 功能与作者 | 本体 X×Y×Z | 作者标注版本 | 建议用地 X×Z | 具体下载 | 使用说明与兼容事项 |
| --- | --- | --- | --- | --- | --- | --- |
| [CMS-218 335蒸汽引擎 5w应力](https://www.creativemechanicserver.com/detail/218/) | 动力；涵涵子 | 3×5×3 | 1.18.2–1.20.1 Forge / 0.5.1i、j | 12×12 | [下载项](https://www.creativemechanicserver.com/download/217/) | 旧版锅炉结构参考；另有手摇版 download/218。迁移后核对水车、泵、供料和动力臂。不因旧标签直接判死刑，也不认定已适配。 |
| [CMS-649 物流式无骨粉云杉树场](https://www.creativemechanicserver.com/detail/649/) | 林业；LETIT | 11×11×6 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 32×32 | [下载项](https://www.creativemechanicserver.com/download/869/) | 不需要竹子时选此原版；补树苗并检查满箱停机和移动式接口。与1413二选一，避免重复常驻。 |
| [CMS-113 旋转树场豪华版，可收集树叶](https://www.creativemechanicserver.com/detail/113/) | 林业；柠喵喵LemoMew | 未填 | 1.19.2 Forge / 0.5.1c | 32×32 | [下载项](https://www.creativemechanicserver.com/download/101/) | 作者明确说明结构可用于多数版本，细节需调整。页面尺寸0视为未填。需重设树苗过滤；移动接口位置有防误收甘蔗用途，不能随意居中。旋转范围待量。 |
| [CMS-695 6.0懒人刷铁机 打印即用](https://www.creativemechanicserver.com/detail/695/) | 铁料；屿秋 | 16×5×16 | 1.21.1 NeoForge / Create 6.0.0–6.0.4 | 24×24 | [下载项](https://www.creativemechanicserver.com/download/951/) | 水车、钻头、树叶补水；圆石相邻处及炼药锅补岩浆，按作者顺序开关离合器。标称927铁锭/小时；带溢出销毁，实装改成满箱停机。体积与水车数较大，不作首选。 |
| [CMS-588 B4_5x5x5 岩浆版安山合金机](https://www.creativemechanicserver.com/detail/588/) | 安山合金；夭夭 | 5×5×5 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.3 | 16×16 | [下载项](https://www.creativemechanicserver.com/download/755/) | 选755；756明确是1.20.1版。需要岩浆和大量圆石、标称44544 SU；当前低负载阶段优先840。 |
| [CMS-372 高速双层可扩展仓库 v1.2](https://www.creativemechanicserver.com/detail/372/) | 仓储；blac | 11×8×15 | 1.21.1 NeoForge / Create 6.0.1 | 24×24 | 到原页选择，变体未锁定 | 支持物品/包裹提取；选择页面v1.2文件并查剪贴板说明，复核合包和蛙港遮挡。多个下载版本未逐一锁定，先用1003/V3。 |
| [CMS-650 物流式简易6倍速加热搅拌黄铜机](https://www.creativemechanicserver.com/detail/650/) | 黄铜；LETIT | 3×3×4 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/870/) | 双素材等量搅拌、自动停机；作者建议订单产出设3的倍数减少积料。优先新版本1005，配方份量以项目为准。 |
| [CMS-591 单机械手精密构件机](https://www.creativemechanicserver.com/detail/591/) | 精密构件；路过的魔理沙 | 3×5×1 | 1.21.1 NeoForge / Create 6.0.6 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1061/) | 投掷器由上至下放铁粒、大齿轮、小齿轮；还须供金板。作者对其他版本仅表示猜测，不能列为跨版本已验证。 |
| [CMS-664 物流式精密构件机](https://www.creativemechanicserver.com/detail/664/) | 精密构件；LETIT | 2×6×3 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/893/) | 输入金板、铁粒、大小齿轮；需搭配6.0物流。已有原料厂后可替换1093，不能单凭2×6×3认定整条供应链只占这点空间。 |
| [CMS-644 物流式简易大小齿轮厂](https://www.creativemechanicserver.com/detail/644/) | 齿轮；LETIT | 4×4×4 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/862/) | 旧版易积存原料；优先983。配齐木材与轴原料后检查连续合成和返料。 |
| [CMS-631 物流式简易6倍速切割站](https://www.creativemechanicserver.com/detail/631/) | 分立加工；LETIT | 3×3×5 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/850/) | 需要指定锯切过滤，防止切成别的建筑方块；综合工作站不足时再独立部署。 |
| [CMS-638 物流式简易压板机](https://www.creativemechanicserver.com/detail/638/) | 分立加工；LETIT | 3×6×3 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/856/) | 接料与出料链、设置订单；优先新版651或综合工作站，不重复建。 |
| [CMS-651 物流式简易4倍速压板机 V2](https://www.creativemechanicserver.com/detail/651/) | 分立加工；LETIT | 3×5×3 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/871/) | 标题写4倍，文中视频写6倍；不按宣传倍数保证产量。接6.0物流，实测压板与回传。 |
| [CMS-647 物流式简易压铸机](https://www.creativemechanicserver.com/detail/647/) | 分立加工；LETIT | 5×5×3 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/868/) | 这里是工作盆冲压类机械，不应当成CBC炮管铸造。作者提示会囤材料，需限制订单与缓存。 |
| [CMS-656 物流式极简可拓展粉碎机](https://www.creativemechanicserver.com/detail/656/) | 分立加工；LETIT | 3×5×3 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/880/) | 确认粉碎轮转向、成品过滤及包裹收发；扩容前核对吞吐，不增加无需求的轮组。 |
| [CMS-989 物流式四合一鼓风机组 V2](https://www.creativemechanicserver.com/detail/989/) | 分立加工；LETIT | 5×4×6 | 1.21.1 NeoForge、1.20.1 / 6.0.0–6.0.8 | 16×16 | [下载项](https://www.creativemechanicserver.com/download/1461/) | 选V2外接动力1461，V2自带动力1462。检查洗涤/熔炼/烟熏/缠魂各介质、风向和订单；原水车版作者要求打印后重放红石块。 |
| [CMS-1012 物流式含水搅拌机](https://www.creativemechanicserver.com/detail/1012/) | 分立加工；LETIT | 3×5×4 | 1.21.1 NeoForge、1.20.1 / 6.0.0–6.0.8 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1449/) | 供水并配置订单，可用于面团/纸浆等实际存在的配方。网页列出的所有产物不代表我们的配方均已具备。 |
| [CMS-175 5×3×5电子管机](https://www.creativemechanicserver.com/detail/175/) | 电子管；Raptor猛龙 | 5×3×5 | 1.18.2–1.20.1 Forge/Fabric / 0.5.0–0.5.1j | 12×12 | [下载项](https://www.creativemechanicserver.com/download/362/) | 作者称0.5.0及以上；保留跨版本候选，未证明6.0.10即用。准备沙、甘蔗、石英、铁和红石并核对砂纸磨制及配方。 |
| [CMS-160 坚固板机](https://www.creativemechanicserver.com/detail/160/) | 板材与轨道；柠喵喵LemoMew | 4×6×8 | 1.18.2–1.20.1 Forge / 0.5.0–0.5.1j | 16×16 | [下载项](https://www.creativemechanicserver.com/download/157/) | 有满箱检测；供熔岩，核对黑曜石生成、粉碎和两次冲压链。旧版过滤/流体NBT待迁移，产量只作作者标称。 |

## 后续参考暂不实装

| 编号和来源 | 功能与作者 | 本体 X×Y×Z | 作者标注版本 | 建议用地 X×Z | 具体下载 | 使用说明与兼容事项 |
| --- | --- | --- | --- | --- | --- | --- |
| [CMS-1007 物流式简约动力合成组](https://www.creativemechanicserver.com/detail/1007/) | 分立加工；LETIT | 11×4×7 | 1.21.1 NeoForge、1.20.1 / 6.0.0–6.0.8 | 20×16 | [下载项](https://www.creativemechanicserver.com/download/1444/) | 作者要求打印后重新放保险库与红石块更新打包/理包状态；标称41472 SU。9核并发对当前多余，现阶段工作站先覆盖基本合成。 |
| [CMS-659 简易堆叠式复合农场](https://www.creativemechanicserver.com/detail/659/) | 农业；LETIT | 14×13×13 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 24×24 | [下载项](https://www.creativemechanicserver.com/download/885/) | 作者已知边角漏收；打印后重放轴承、重设库存启停阈值。当前先保留普通田地，修好漏收后才考虑机械化；不以现状标为即用。 |
| [CMS-583 堆叠起重机农场](https://www.creativemechanicserver.com/detail/583/) | 农业；WhiteElephant | 22×56×23 | 1.21.1 NeoForge / Create 6.0.0–6.0.4 | 32×32 | [下载项](https://www.creativemechanicserver.com/download/752/) | 页面6.0.4有同意2/反对0；高56格。补水，调整卸货，收割机/犁按教程切换。作者未验证旋转，当前阶段不选高塔。 |
| [CMS-1287 5x4x4电子管机4速](https://www.creativemechanicserver.com/detail/1287/) | 电子管；摸鱼搭子 | 5×4×4 | 1.21.1 NeoForge / Create 6.0.9 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1967/) | 额外依赖附魔工业（已批准但未启用）；接蛙港和铁/石英/红石。磨制出现掉落物时按作者说明反转动力。当前不为此额外启用Mod。 |
| [CMS-751 334坚固板·列车机壳·轨道机](https://www.creativemechanicserver.com/detail/751/) | 板材与轨道；涵涵子 | 3×3×4 | 1.21.1 NeoForge / Create 6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/1036/) | 作者提醒跨版本过滤可能丢失，需手动补回；主要作为坚固板单元参考，供黑曜石和熔岩。机壳/轨道部分需另验。 |
| [CMS-673 物流式简易轨道机](https://www.creativemechanicserver.com/detail/673/) | 板材与轨道；LETIT | 4×3×7 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.4 | 12×16 | [下载项](https://www.creativemechanicserver.com/download/911/) | 供铁粒、石材，绑定物流与装配；起始岛尚无铁路需求，不安装。 |
| [CMS-757 物流式四合一熔岩工厂](https://www.creativemechanicserver.com/detail/757/) | 流体；LETIT | 8×9×4 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.6 | 24×24 | [下载项](https://www.creativemechanicserver.com/download/1043/) | 还需收集模块1044，且必须放箱子矿车。网页尺寸不能代表两个文件及外部熔岩源的总范围；所谓无限熔岩必须另核源池/配置条件。当前不建大型熔岩池。 |
| [CMS-699 物流式简易6倍速榨油机](https://www.creativemechanicserver.com/detail/699/) | 燃料；LETIT | 3×4×7 | 1.21.1 NeoForge 与 1.20.1 Forge / Create 6.0.0–6.0.6 | 12×16 | [下载项](https://www.creativemechanicserver.com/download/953/) | 作者明确要求对应榨油配方；本体方块能放置不代表能产油。待已批准燃料Mod启用并确认配方后采用。 |
| [CMS-906 农场物语植物油发动机](https://www.creativemechanicserver.com/detail/906/) | 燃料；北白蛇神社 | 11×12×12 | 1.20.1 Forge / 6.0.8 | 24×24 | [下载项](https://www.creativemechanicserver.com/download/1308/) | 葡香果园红葡萄与柴油动力路线，有整合包配方依赖；不能当作我们的1.21.1即用模板，当前不采用。 |
| [CMS-694 354机枪弹药装配机](https://www.creativemechanicserver.com/detail/694/) | 军工；摸鱼de酒精灯 | 3×5×4 | 1.20.1 Forge / 6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/949/) | 需要CBC；禁止向原料口输入机炮弹药冲压版。核对目标CBC 5.11.7物品ID与序列配方，当前不安装。 |
| [CMS-689 334机枪弹药装配机](https://www.creativemechanicserver.com/detail/689/) | 军工；未署名 | 3×3×4 | 1.20.1 Forge / 6.0.4 | 12×12 | [下载项](https://www.creativemechanicserver.com/download/943/) | 需要CBC，输入铜粒、火药小份、空机枪弹药；同功能低成本候选，尚未证明目标版本配方一致。 |
| [CMS-322 大炮工厂](https://www.creativemechanicserver.com/detail/322/) | 军工；BZL_rd | 26×73×55 | 1.18.2–1.20.1 Forge/Fabric / 0.5.1系列 | 40×72 | [下载项](https://www.creativemechanicserver.com/download/345/) | 需CBC；作者说明部署有问题。高73格，另有模板346；仅保留铸造流程参考，不据此扩大起始岛。 |

## 下次实装流程

1. 从本表的具体下载项取得文件，保存原文件名、作者、SHA-256、下载日期；原文件放本地蓝图缓存，不默认把第三方作品再分发到GitHub。
2. 读NBT的DataVersion、size、palette及方块实体，列出缺失Mod方块、创造马达/创造容器、过滤与流体配置。读取后的尺寸与网页尺寸分别保留。
3. 备份施工存档，在已预留的正式厂房位置试装单台；先核本体、检修通道、树冠/旋转/移动范围，再做建筑外壳。
4. 补水、岩浆、燃料、树苗、过滤、机械臂目标、黏胶及物流地址；同名库存网络必须重新绑定，测试蓝图旋转会否破坏转向。
5. 在目标版本检查启停、真实产物、连续供料、缺料恢复、满箱停机及保存重载。记录应力、转速、吞吐和游戏耗时，禁用自动销毁有用资源。
6. 完成后把JSON中的local_validation更新为“项目实测通过”，附证据与本地改造文件路径；失败保留原始记录，启用同功能备选。

蓝图库与[当前工厂方案](starter-factory-blueprint-plan.md)配套。
