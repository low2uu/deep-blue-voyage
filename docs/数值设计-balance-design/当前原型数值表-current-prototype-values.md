# 当前原型数值表

核对基线：v0.1.13，2026-10-22。以下为配置快照，不是实机测量或正式平衡定稿。完整来源：[设计规格](../../content/prototypes/README.md)、[运行目录数据](../../mods/dbv-world/src/main/resources/dbv_prototypes.json)。

## 怪物属性

移动速度是属性值，不是格/秒；攻击列是 ATTACK_DAMAGE，守卫者光束等继承行为不保证直接按该值扣血。碰撞宽高来自实体注册，模型缩放另列；装饰尾翼不自动成为命中部位。

| 怪物 / ID | AI基底 | 当前/目标生命 | 速度属性 | 攻击属性 | 护甲 | 索敌范围 | 抗击退 | 宽×高 | 模型缩放 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | --- | ---: |
| 缆绳鳗 / `cable_eel` | squid | 32/32 | 0.1 | 0 | 0 | 16 | 0 | 0.8×0.8 | 1 |
| 掠流鳐 / `current_ray` | squid | 48/48 | 0.1 | 0 | 0 | 16 | 0 | 0.8×0.8 | 1 |
| 附着蠕虫 / `hull_worm` | squid | 20/20 | 0.1 | 0 | 0 | 16 | 0 | 0.32×0.32 | 0.4 |
| 冰穴伏猎者 / `ice_cave_stalker` | guardian | 100/100 | 0.5 | 6 | 0 | 16 | 0 | 0.85×0.85 | 1 |
| 冰缝鳗 / `ice_fissure_eel` | squid | 52/52 | 0.1 | 0 | 0 | 16 | 0 | 0.8×0.8 | 1 |
| 灯囊诱猎者 / `lantern_lurker` | guardian | 72/72 | 0.5 | 6 | 0 | 16 | 0 | 0.85×0.85 | 1 |
| 镜甲虾 / `mirror_shrimp` | squid | 44/44 | 0.1 | 0 | 0 | 16 | 0 | 0.8×0.8 | 1 |
| 缠桨水母 / `propeller_jellyfish` | squid | 36/36 | 0.1 | 0 | 0 | 16 | 0 | 0.8×0.8 | 1 |
| 撞角鱼 / `ramfish` | guardian | 96/96 | 0.5 | 6 | 0 | 16 | 0 | 0.85×0.85 | 1 |
| 锈壳蟹 / `rust_shell_crab` | squid | 24/24 | 0.1 | 0 | 0 | 16 | 0 | 0.8×0.8 | 1 |
| 裂壳螯兽 / `split_shell_beast` | guardian | 120/120 | 0.5 | 6 | 0 | 16 | 0 | 0.85×0.85 | 1 |
| 沉钟守卫 / `sunken_bell_guardian` | guardian | 1024/2800 | 0.5 | 6 | 0 | 16 | 0 | 1.7×1.7 | 2 |
| 回旋巨鳐 / `vortex_ray` | squid | 1024/1800 | 0.1 | 0 | 0 | 16 | 0 | 1.6×1.6 | 2 |
| 白幕潜猎者 / `whiteout_hunter` | guardian | 1024/1200 | 0.5 | 6 | 0 | 16 | 0 | 1.7×1.7 | 2 |
| 残骸潜水者 / `wreck_diver` | drowned | 40/40 | 0.23 | 3 | 2 | 16 | 0 | 0.6×1.95 | 1 |

## 掉落快照

数量为区间均匀整数，特殊条目独立抽取，当前不受抢夺影响。Boss研究记录现阶段不是团队首杀奖励，不要把草案首杀规则当成已实现。自然刷新均未开启。

| 怪物 | 物品 / ID | 数量 | 概率 | 条件 |
| --- | --- | --- | ---: | --- |
| 缆绳鳗 | 韧性纤维 / `dbv_world:tough_fiber` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 缆绳鳗 | 导电黏液 / `dbv_world:conductive_mucus` | 1～1 | 20% | 独立概率，不受抢夺影响 |
| 掠流鳐 | 导流鳍片 / `dbv_world:flow_fin` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 掠流鳐 | 流纹软骨 / `dbv_world:flow_cartilage` | 1～1 | 20% | 独立概率，不受抢夺影响 |
| 附着蠕虫 | 密封黏质 / `dbv_world:sealing_mucus` | 1～1 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 附着蠕虫 | 活性黏质 / `dbv_world:active_mucus` | 1～1 | 15% | 独立概率，不受抢夺影响 |
| 冰穴伏猎者 | 轻质骨架 / `dbv_world:light_skeleton` | 2～3 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 冰穴伏猎者 | 完整听觉器官 / `dbv_world:auditory_organ` | 1～1 | 25% | 独立概率，不受抢夺影响 |
| 冰缝鳗 | 柔韧筋束 / `dbv_world:flexible_tendon` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 冰缝鳗 | 感知须 / `dbv_world:sensory_barbel` | 1～1 | 20% | 独立概率，不受抢夺影响 |
| 灯囊诱猎者 | 发光囊 / `dbv_world:luminous_sac` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 灯囊诱猎者 | 透镜组织 / `dbv_world:lens_tissue` | 1～1 | 20% | 独立概率，不受抢夺影响 |
| 镜甲虾 | 镜质甲片 / `dbv_world:mirror_carapace` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 镜甲虾 | 清澈晶片 / `dbv_world:clear_crystal` | 1～1 | 25% | 独立概率，不受抢夺影响 |
| 缠桨水母 | 绝缘胶质 / `dbv_world:insulating_gel` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 缠桨水母 | 完整触须束 / `dbv_world:intact_tentacles` | 1～1 | 25% | 独立概率，不受抢夺影响 |
| 撞角鱼 | 硬质角片 / `dbv_world:hard_horn` | 2～4 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 撞角鱼 | 冲击腺体 / `dbv_world:impact_gland` | 1～1 | 25% | 独立概率，不受抢夺影响 |
| 锈壳蟹 | 壳片 / `dbv_world:shell_fragment` | 1～2 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 锈壳蟹 | 回收金属片 / `dbv_world:reclaimed_metal` | 1～1 | 25% | 独立概率，不受抢夺影响 |
| 裂壳螯兽 | 致密甲片 / `dbv_world:dense_carapace` | 2～4 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 裂壳螯兽 | 完整关节膜 / `dbv_world:joint_membrane` | 1～1 | 25% | 独立概率，不受抢夺影响 |
| 沉钟守卫 | 致密材料 / `dbv_world:dense_material` | 8～12 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 沉钟守卫 | 高压密封材料 / `dbv_world:high_pressure_sealant` | 4～4 | 100% | 独立概率，不受抢夺影响 |
| 回旋巨鳐 | 高品质导流材料 / `dbv_world:premium_flow_material` | 6～8 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 回旋巨鳐 | 冲击腺体 / `dbv_world:impact_gland` | 2～2 | 100% | 独立概率，不受抢夺影响 |
| 白幕潜猎者 | 声学晶体 / `dbv_world:acoustic_crystal` | 6～8 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 白幕潜猎者 | 轻质结构材料 / `dbv_world:light_structural_material` | 4～4 | 100% | 独立概率，不受抢夺影响 |
| 残骸潜水者 | 废旧零件 / `dbv_world:scrap_parts` | 1～3 | 100% | 死亡结算；均匀整数数量，不受抢夺影响 |
| 残骸潜水者 | 维修工具残件 / `dbv_world:repair_tool_fragment` | 1～1 | 15% | 独立概率，不受抢夺影响 |

## 物品堆叠

以下54项已注册；不代表武器伤害、维修效果或配方已经实现。

| 物品 | ID | 最大堆叠 |
| --- | --- | ---: |
| 声学晶体 | `dbv_world:acoustic_crystal` | 64 |
| 活性黏质 | `dbv_world:active_mucus` | 64 |
| 人工导流替代料 | `dbv_world:artificial_flow_material` | 64 |
| 完整听觉器官 | `dbv_world:auditory_organ` | 64 |
| 基础推进药 | `dbv_world:base_propellant` | 64 |
| 基础胶料 | `dbv_world:base_rubber` | 64 |
| 清澈晶片 | `dbv_world:clear_crystal` | 64 |
| 导电黏液 | `dbv_world:conductive_mucus` | 64 |
| 铜制零件 | `dbv_world:copper_part` | 64 |
| 损管专精研究记录 | `dbv_world:damage_control_research` | 64 |
| 致密甲片 | `dbv_world:dense_carapace` | 64 |
| 致密材料 | `dbv_world:dense_material` | 64 |
| 探测专精研究记录 | `dbv_world:detection_research` | 64 |
| 电子组件 | `dbv_world:electronic_component` | 64 |
| 柔韧筋束 | `dbv_world:flexible_tendon` | 64 |
| 导流组件 | `dbv_world:flow_assembly` | 64 |
| 流纹软骨 | `dbv_world:flow_cartilage` | 64 |
| 导流鳍片 | `dbv_world:flow_fin` | 64 |
| 手持鱼叉器 | `dbv_world:hand_harpoon` | 1 |
| 硬质角片 | `dbv_world:hard_horn` | 64 |
| 高压密封材料 | `dbv_world:high_pressure_sealant` | 64 |
| 冲击腺体 | `dbv_world:impact_gland` | 64 |
| 绝缘胶质 | `dbv_world:insulating_gel` | 64 |
| 完整触须束 | `dbv_world:intact_tentacles` | 64 |
| 完整关节膜 | `dbv_world:joint_membrane` | 64 |
| 透镜组织 | `dbv_world:lens_tissue` | 64 |
| 轻炮弹 | `dbv_world:light_cannon_round` | 64 |
| 潜艇轻型射钉炮 | `dbv_world:light_nail_cannon` | 1 |
| 轻质骨架 | `dbv_world:light_skeleton` | 64 |
| 轻质结构材料 | `dbv_world:light_structural_material` | 64 |
| 轻量结构件 | `dbv_world:light_structure` | 64 |
| 发光囊 | `dbv_world:luminous_sac` | 64 |
| 金属片 | `dbv_world:metal_sheet` | 64 |
| 镜质甲片 | `dbv_world:mirror_carapace` | 64 |
| 纤维 | `dbv_world:plant_fiber` | 64 |
| 精密声学组件 | `dbv_world:precision_acoustic_assembly` | 64 |
| 高品质导流材料 | `dbv_world:premium_flow_material` | 64 |
| 耐压复合板 | `dbv_world:pressure_composite_plate` | 64 |
| 处理后的致密材料 | `dbv_world:processed_dense_material` | 64 |
| 推进专精研究记录 | `dbv_world:propulsion_research` | 64 |
| 回收金属片 | `dbv_world:reclaimed_metal` | 64 |
| 增强纤维束 | `dbv_world:reinforced_fiber` | 64 |
| 维修工具残件 | `dbv_world:repair_tool_fragment` | 64 |
| 废旧零件 | `dbv_world:scrap_parts` | 64 |
| 密封黏质 | `dbv_world:sealing_mucus` | 64 |
| 感知须 | `dbv_world:sensory_barbel` | 64 |
| 壳片 | `dbv_world:shell_fragment` | 64 |
| 标准机械件 | `dbv_world:standard_mechanical_part` | 64 |
| 标准维修包 | `dbv_world:standard_repair_kit` | 1 |
| 标准鱼雷 | `dbv_world:standard_torpedo` | 64 |
| 韧性纤维 | `dbv_world:tough_fiber` | 64 |
| 通用密封件 | `dbv_world:universal_seal` | 64 |
| 升级轻炮 | `dbv_world:upgraded_light_cannon` | 1 |
| 作业切割器 | `dbv_world:work_cutter` | 1 |
