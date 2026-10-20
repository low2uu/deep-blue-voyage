# 铁质潜艇壳

版本：0.1.5；Minecraft 1.21.1 / NeoForge。

新增独立方块 `dbv_world:iron_submarine_hull`，中文名“铁质潜艇壳”，英文名“Iron Submarine Hull”。可在“深蓝远航”创造模式分类中找到，或执行 `/give @s dbv_world:iron_submarine_hull 64`。本阶段未增加合成配方。

使用 `BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)` 复制黑曜石属性，包括硬度50、爆炸抗性1200、需要正确工具等。额外补齐钻石级镐挖掘标签、活塞不可推动与末影龙免疫；正确工具挖掘掉落自身。它是独立的完整实心方块，不是给原版铁块换贴图。

第三方 Mod 若按方块ID、标签或专门配置计算质量/耐压，不能仅凭复制黑曜石属性保证得到完全相同结果。本次未覆盖航空学、深海学的计算，也未替换已有潜艇外壳。

六面和物品模型共用 `assets/dbv_world/textures/block/iron_submarine_hull.png`，以 `cube_all` 映射。贴图为灰色平滑金属板，四边有拼接缝、四角有螺丝，64×64 PNG。

材质使用内置 imagegen 生成，随后仅缩放为游戏纹理尺寸。生成提示词：

> Create a single square seamless Minecraft block texture for an iron submarine hull. Flat front-on orthographic 2D texture ONLY, entire square filled edge to edge, no perspective, no rendered cube, no background, no text. Gray smooth metal plate, restrained neutral gray tones, large clean smooth central area with extremely subtle metal shading. Thin dark joining seams exactly along the four edges, small clearly readable slotted screw heads near each of four corners. Minecraft pixel-art texture aesthetic, logical 32x32 pixel grid enlarged sharply, consistent pixel sizes, no noisy grunge, no rust, no logos. Identical texture will be used on all six block faces. Square PNG opaque.

验证范围：构建与自动测试、方块资源引用/语言键/标签/掉落表和JAR打包检查、贴图视觉检查。本次未启动游戏进行放置与挖掘实测。
