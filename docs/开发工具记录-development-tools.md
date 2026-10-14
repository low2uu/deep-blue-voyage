# 开发工具启用记录

2026-10-14，用户明确要求增加推荐表前五项。以下工具已加入同一正式工程的 Gradle 运行依赖，通过原来的 `tools/dev.ps1 run` 启动，不另建客户端或独立调试项目。

| 工具 | 锁定版本 | 用途 | 发布记录 |
| --- | --- | --- | --- |
| WorldEdit | 7.3.8 | 批量建图和建筑蓝图 | [WTAFvuRx](https://modrinth.com/mod/worldedit/version/WTAFvuRx) |
| ProbeJS | 8.0.3 | 导出 KubeJS 类型与接口资料 | [xC9KUA7R](https://modrinth.com/mod/probejs/version/xC9KUA7R) |
| KubeJS | 2101.7.2-build.377 | 配方、标签和事件脚本 | [THIGFPwf](https://modrinth.com/mod/kubejs/version/THIGFPwf) |
| spark | 1.10.124 | 性能采样与诊断 | [v5qtqRQi](https://modrinth.com/mod/spark/version/v5qtqRQi) |
| Jade | 15.10.6 | 方块、实体和设备信息显示 | [eYz2YBGT](https://modrinth.com/mod/jade/version/eYz2YBGT) |
| Rhino 必需前置 | 2101.2.8-build.91 | KubeJS JavaScript 引擎，沿用此前已批准版本 | [SqkDvOLG](https://modrinth.com/mod/rhino/version/SqkDvOLG) |

所有文件均匹配 Minecraft 1.21.1 NeoForge 发布标签，下载后已核对 SHA-1 并保存 JAR 元数据证据：[development-mod-evidence.json](development-mod-evidence.json)。Better Advanced Tooltips 已由 KubeJS 内置，不重复安装。Observable 和 NBTEdit 未加入；Jade Sable Compat 保持此前批准状态，本次未额外启用。

客户端启动日志已识别五项工具和 Rhino，WorldEdit 平台、Jade 插件和 KubeJS 客户端脚本初始化完成，纹理与着色器加载结束，未出现阻断启动的新依赖错误。交错电网此前的本机加速库提示仍在；本次另有 Mojang 公钥请求失败的网络错误，未阻断资源加载，联机认证尚未验证。世界内命令、ProbeJS 导出和机器交互仍需实际建档检查。

本机 KubeJS 数据位于 `runtime/client/kubejs`，不受 Git 跟踪。正式脚本应整理到 `pack/overrides/kubejs` 并部署至运行目录；当前没有添加玩法脚本，也没有配置自动同步。ProbeJS 的导出数据属于本机开发资料，不默认进入发布包。当前无需长期开启 spark 采样。

本次批准用于开发与建设，玩家发布包的工具取舍后续单独确定。现有清单 Excel 属于 2026-10-13 的历史快照；本次授权与启用状态以更新后的 Markdown、JSON 和 Gradle 配置为准。
