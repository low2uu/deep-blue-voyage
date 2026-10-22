# 物品与怪物原型清单

内容基线：0.1.11；物品外观更新：0.1.12。依据[整体玩法与数值初案](整体玩法与数值初案-gameplay-and-balance-draft.md)，本次授权范围为初步创建。设计和平衡值继续标记待定。

## 如何在游戏里查看

双方使用相同版本的自研 Mod。开发客户端先运行 `tools/dev.ps1 build`，再运行 `tools/dev.ps1 run`；联机服按 `server/README.md` 的停服更新流程更新，并让客户端同步。不要在同一个 mods 目录保留两个 dbv-world 版本。

进入创造模式的“深蓝远航”分类，可以看到新增的 54 个物品和 15 个刷怪蛋；也可以使用创造物品搜索。怪物应放入水中，守卫者/溺尸原型测试使用普通难度，和平难度会移除敌对怪物。请在测试地图召唤，不建议向正式岛屿批量投放。

例如：

```mcfunction
/give @s dbv_world:standard_repair_kit
/give @s dbv_world:rust_shell_crab_spawn_egg
/summon dbv_world:rust_shell_crab ~ ~ ~
```

已有存档不需要重新生成。没有自然刷新和地图结构投放，不会自行在已有世界铺怪。

## 本版实现边界

| 内容 | 当前游戏实现 | 尚未实现 |
| --- | --- | --- |
| 54 个物品 | 独立注册 ID、中英文名称、独立三维物品模型、创造分类、堆叠与实际容器存储、原型提示 | 美术后续精修、加工配方、武器发射、维修效果、研究解锁 |
| 15 种怪物 | 独立实体 ID、刷怪蛋、临时原版模型和 AI、生命/属性、掉落表、原生世界实体保存 | 原创模型、专属技能、自然生态、地区门槛、攻击潜艇设备 |
| 3 个 Boss | 可召唤原型、奖励材料掉落 | Boss 血条/阶段、弱点系统、人数缩放、团队首杀研究奖励 |
| 设计器 | 怪物字段、新建怪物、JSON 导入编辑导出、v1 无损升级为 v2 | 任意 JSON 直接热加载为可运行代码 |

鱿鱼基底只游动/逃避，不会主动攻击；守卫者基底具有原版光束和反伤；溺尸基底使用原版近战、水陆寻路、日照和装备规则。当前名称表达目标角色，**不代表角色特有技能已经完成**。Boss 暂复用放大模型，目标生命 1800/2800/1200 保留在 JSON，当前生命统一为原版上限 1024。未调整原版全局上限和航空学物理。

掉落数量采用区间内均匀整数；特殊掉落为独立概率、数量 1（Boss 保证奖励除外），暂不受抢夺影响。当前普通死亡都能结算，没有首杀和团队限制，不作为正式战斗经济。

## JSON 与工具

规范文件位于 `content/prototypes/items/` 和 `content/prototypes/mobs/`，每个对象一份完整 JSON。便携工具的 `specs/` 同时携带这 69 份文件，完整解压后双击 `index.html`，选择“导入 JSON”，在左侧“怪物与 Boss”修改属性。编辑不会改变游戏；导出后交给开发者生成资源和实现代码。

v1 文档导入时仅补齐怪物字段并升级版本，旧字段原值、状态、来源和备注保留。未知字段和破损结构仍拒绝导入，避免静默丢失。导出文件为 v2，不能交给旧版工具编辑。

游戏使用 `dbv_prototypes.json` 的编译后子集，源规格在仓库，不写入主潜艇 JSON 档案。普通怪物世界实体仍沿用 Minecraft 实体存档；主潜艇独立备份机制没有改变。

## 验证记录

- Gradle 编译与既有单元测试通过。
- 设计器与原型一致性测试覆盖全部 69 份规格、旧版迁移、非法数值、翻译、模型、掉落引用。
- 独立临时服务端成功加载，逐一召唤 15 种实体并核对最大生命，54 个物品成功写入实际箱子，15 张掉落表能生成奖励。
- 重启服务端后，实体身份、锈壳蟹12点当前生命和两个箱子共54种物品保持正常。
- 客户端模型视觉与浏览器点击效果尚未实机验收；不要把编译和服务端测试理解为专属战斗玩法通过。

## 怪物目录

| 名称 | 注册 ID | 地区 | 游戏生命 / 目标生命 | 临时基底 |
| --- | --- | --- | ---: | --- |
| 缆绳鳗 | `dbv_world:cable_eel` | 普通海底 | 32 / 32 | squid |
| 掠流鳐 | `dbv_world:current_ray` | 急流 | 48 / 48 | squid |
| 附着蠕虫 | `dbv_world:hull_worm` | 深海 | 20 / 20 | squid |
| 冰穴伏猎者 | `dbv_world:ice_cave_stalker` | 冰川 | 100 / 100 | guardian |
| 冰缝鳗 | `dbv_world:ice_fissure_eel` | 冰川 | 52 / 52 | squid |
| 灯囊诱猎者 | `dbv_world:lantern_lurker` | 深海 | 72 / 72 | guardian |
| 镜甲虾 | `dbv_world:mirror_shrimp` | 冰川 | 44 / 44 | squid |
| 缠桨水母 | `dbv_world:propeller_jellyfish` | 急流 | 36 / 36 | squid |
| 撞角鱼 | `dbv_world:ramfish` | 急流 | 96 / 96 | guardian |
| 锈壳蟹 | `dbv_world:rust_shell_crab` | 普通海底 | 24 / 24 | squid |
| 裂壳螯兽 | `dbv_world:split_shell_beast` | 深海 | 120 / 120 | guardian |
| 沉钟守卫 | `dbv_world:sunken_bell_guardian` | 深海 | 1024 / 2800 | guardian |
| 回旋巨鳐 | `dbv_world:vortex_ray` | 急流 | 1024 / 1800 | squid |
| 白幕潜猎者 | `dbv_world:whiteout_hunter` | 冰川 | 1024 / 1200 | guardian |
| 残骸潜水者 | `dbv_world:wreck_diver` | 普通海底 | 40 / 40 | drowned |

## 物品目录

| 名称 | 注册 ID | 类别 |
| --- | --- | --- |
| 声学晶体 | `dbv_world:acoustic_crystal` | 材料 |
| 活性黏质 | `dbv_world:active_mucus` | 材料 |
| 人工导流替代料 | `dbv_world:artificial_flow_material` | 材料 |
| 完整听觉器官 | `dbv_world:auditory_organ` | 材料 |
| 基础推进药 | `dbv_world:base_propellant` | 材料 |
| 基础胶料 | `dbv_world:base_rubber` | 材料 |
| 清澈晶片 | `dbv_world:clear_crystal` | 材料 |
| 导电黏液 | `dbv_world:conductive_mucus` | 材料 |
| 铜制零件 | `dbv_world:copper_part` | 材料 |
| 损管专精研究记录 | `dbv_world:damage_control_research` | 研究 |
| 致密甲片 | `dbv_world:dense_carapace` | 材料 |
| 致密材料 | `dbv_world:dense_material` | 材料 |
| 探测专精研究记录 | `dbv_world:detection_research` | 研究 |
| 电子组件 | `dbv_world:electronic_component` | 材料 |
| 柔韧筋束 | `dbv_world:flexible_tendon` | 材料 |
| 导流组件 | `dbv_world:flow_assembly` | 材料 |
| 流纹软骨 | `dbv_world:flow_cartilage` | 材料 |
| 导流鳍片 | `dbv_world:flow_fin` | 材料 |
| 手持鱼叉器 | `dbv_world:hand_harpoon` | 武器 |
| 硬质角片 | `dbv_world:hard_horn` | 材料 |
| 高压密封材料 | `dbv_world:high_pressure_sealant` | 材料 |
| 冲击腺体 | `dbv_world:impact_gland` | 材料 |
| 绝缘胶质 | `dbv_world:insulating_gel` | 材料 |
| 完整触须束 | `dbv_world:intact_tentacles` | 材料 |
| 完整关节膜 | `dbv_world:joint_membrane` | 材料 |
| 透镜组织 | `dbv_world:lens_tissue` | 材料 |
| 轻炮弹 | `dbv_world:light_cannon_round` | 弹药 |
| 潜艇轻型射钉炮 | `dbv_world:light_nail_cannon` | 武器 |
| 轻质骨架 | `dbv_world:light_skeleton` | 材料 |
| 轻质结构材料 | `dbv_world:light_structural_material` | 材料 |
| 轻量结构件 | `dbv_world:light_structure` | 材料 |
| 发光囊 | `dbv_world:luminous_sac` | 材料 |
| 金属片 | `dbv_world:metal_sheet` | 材料 |
| 镜质甲片 | `dbv_world:mirror_carapace` | 材料 |
| 纤维 | `dbv_world:plant_fiber` | 材料 |
| 精密声学组件 | `dbv_world:precision_acoustic_assembly` | 材料 |
| 高品质导流材料 | `dbv_world:premium_flow_material` | 材料 |
| 耐压复合板 | `dbv_world:pressure_composite_plate` | 材料 |
| 处理后的致密材料 | `dbv_world:processed_dense_material` | 材料 |
| 推进专精研究记录 | `dbv_world:propulsion_research` | 研究 |
| 回收金属片 | `dbv_world:reclaimed_metal` | 材料 |
| 增强纤维束 | `dbv_world:reinforced_fiber` | 材料 |
| 维修工具残件 | `dbv_world:repair_tool_fragment` | 材料 |
| 废旧零件 | `dbv_world:scrap_parts` | 材料 |
| 密封黏质 | `dbv_world:sealing_mucus` | 材料 |
| 感知须 | `dbv_world:sensory_barbel` | 材料 |
| 壳片 | `dbv_world:shell_fragment` | 材料 |
| 标准机械件 | `dbv_world:standard_mechanical_part` | 材料 |
| 标准维修包 | `dbv_world:standard_repair_kit` | 工具 |
| 标准鱼雷 | `dbv_world:standard_torpedo` | 弹药 |
| 韧性纤维 | `dbv_world:tough_fiber` | 材料 |
| 通用密封件 | `dbv_world:universal_seal` | 材料 |
| 升级轻炮 | `dbv_world:upgraded_light_cannon` | 武器 |
| 作业切割器 | `dbv_world:work_cutter` | 工具 |

## 0.1.12 物品外观更新

54个物品已换成原生三维模型，模型源文件、离线总览和逐项说明见[物品模型目录](../art/item-models/物品模型目录-item-model-catalog.md)。怪物和刷怪蛋保持原样。本次不启动游戏或测试服务器，资源包和手持外观尚未实机验收。上述0.1.11测试结果不覆盖本次新模型。
