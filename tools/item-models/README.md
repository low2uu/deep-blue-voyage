# 三维物品模型资源

`generate.py` 是54件原型物品的原生立方体几何源文件。每件分别定义形状，使用128×128共享材质调色板；每个色块保留16×16像素，减少多级纹理下的串色。不依赖额外模型加载器。没有修改怪物、刷怪蛋、方块或物品功能。

```powershell
python tools/item-models/generate.py
node tools/content-designer/build-prototypes.cjs
```

Python需要 Pillow 和 NumPy，游戏运行不需要。前者生成模型、材质、规格外观字段和离线预览；后者连接物品入口模型。生成器不启动Minecraft。

输出：

- `mods/dbv-world/src/main/resources/assets/dbv_world/models/item/prototypes/`：独立模型。
- `mods/dbv-world/src/main/resources/assets/dbv_world/models/item/`：物品入口引用。
- `mods/dbv-world/src/main/resources/assets/dbv_world/textures/item/prototype_palette.png`：共享材质。
- `docs/art/item-models/`：离线几何总览和中文结构说明。
- `content/prototypes/items/`：同步更新的设计器JSON。

模型覆盖背包、左右手、地面和展示框上下文。层叠饰件使用微小几何间距避免共面闪烁。预览使用同一份几何和深度缓冲生成，不是游戏截图；客户端亮度、模型角度、持握位置仍需以后实机验收。

这次只制作外观。炮、鱼雷、切割器和维修包并未因为有模型而新增发射、放置或使用效果。材料发光部分为明亮配色，没有增加动态光源。
