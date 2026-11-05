# 远景与动态光源接入

2026-10-22，Minecraft 1.21.1 NeoForge。仅静态核查和构建，不启动游戏或服务器。

## 默认配置

用户选择优先Voxy/Roxy，因此Gradle默认`dbvGraphics=lod`，包括Roxy0.4.0、原版Voxy0.2.16-beta+1.21.11、Sodium0.8.12。FFAPI0.116.15+2.3.5+1.21.1与VoxyWorldGen2.4.2同时接入。

[Roxy作者说明](https://modrinth.com/mod/roxy)明确要求该Voxy文件，属于有意跨版本桥接。NeoForge仍为21.1.259，没有将整包迁移到1.21.11。Voxy原文件通过官方源下载，不在Git中分发。

开发启动仍使用`powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 run`；Gradle自动解析固定版本。服务器导出逻辑排除Roxy、Voxy、Sodium及动态光源等客户端渲染库，只保留公共部分；服务器LOD传输扩展由pack/local-mods中的两个原始JAR提供。

## 暂缓项目

- Sodium Dynamic Lights1.0.10的DynamicLightingPage继承旧`net.caffeinemc.mods.sodium.client.gui.options.OptionPage`；Sodium0.8.12 JAR已不存在此类。用户同意暂缓该Mod及依赖它的Create Dynamic Lights2.3.1-sodium-sable。
- 保留`dbvGraphics=lights`候选组合（旧Sodium0.6.13及相关选项依赖），用于后续继续核查；尚未实机验收，不与LOD组合混装。`none`可不加载两套渲染组合，公共依赖仍保留。
- Better ModList的Modrinth关系表列为WorldGen依赖，但实际WorldGen JAR未强制依赖；Better ModList21.1.4/21.1.5自身写有排除1.21.1的范围`[1.21,1.21.1)`，未贸然加入。
- 用户随后提供项目父目录的“超视距.zip”。已接入其中的Voxy Auto LOD（文件1.1.4、内部1.0.0）与Voxy Server Side0.16.0；原件及哈希见pack/local-mods。两个不同Mod ID可以被加载器分别识别，但这不证明两套LOD处理与Roxy协同工作。实际联动及重复工作负载待实测。

## 验收边界

Java测试、资源校验和依赖下载均不等于渲染兼容性实测。仍待用户启动后确认LOD渲染、物理艇透明玻璃、门体开闭及多人同步。没有运行WorldGen预生成任务，也没有修改存档。

精确下载地址、哈希与用户选择见[依赖证据](../lod-mod-evidence.json)。

2026-10-22离线结果：44项Java测试通过；104个状态资源、透明窗几何及全部自研贴图16×16检查通过；初次导出客户端29个JAR、服务端20个JAR（加入用户原件前），默认新增5项的文件哈希均与Modrinth一致，服务端未混入Voxy/Roxy/Sodium。未启动游戏。

用户原件补充验证：离线exportCoopPack成功，客户端31个JAR、服务端22个JAR，两端的两个新增文件SHA-256均与压缩包原件一致。未启动游戏或服务器。
