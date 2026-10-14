# Mod剔除与受限文件记录

2026-10-14。这里保留移出当前清单的依据，不是待安装候选。当前三分区见[Mod清单](当前Mod清单-current-mod-list.md)。

| 项目 | 处理 | 原因与来源 |
| --- | --- | --- |
| U106 Sound Physics Remastered 1.5.1 | 从备选区移除；原记录保存在JSON的excluded_rows | 已选Sound Physics: Aeronautics为独立分支，两者同modId且作者禁止同装。[作者说明](https://modrinth.com/mod/sound-physics-aeronautics)；不是“不支持1.21.1 NeoForge”。 |
| U045 G&G原2.0.0-Beta-1.1文件 | 继续排除该旧文件，项目保留选定的1.2.10 release | 作者不允许该Beta用于整合包/重要世界，这是文件适用性限制。[原文件说明](https://modrinth.com/mod/create-aeronautics-gadgets-and-gizmos/version/9ci2hEic) |

## 不误删的情况

- Deeper Oceans与Tectonic互斥，但地形主选尚未确定，保留二选一标注，不据此判定任一个普遍不兼容。[作者说明](https://modrinth.com/mod/deeper-oceans)
- Roxy官方明确支持特定Voxy 0.2.16-beta（原生1.21.11 Fabric）桥接到1.21.1 NeoForge；保留指定组合，未实际启用或验证。[作者说明](https://modrinth.com/mod/roxy)
- JEI原版19.51.0.418已在当前正式环境加载、进世界并初始化GUI；不按旧的范围声明疑点误删，亦不推断其他运行环境必然兼容。[运行证据](辅助Mod启用记录-auxiliary-mods.md)
- 两个Voxy服务端身份待核文件仍为待定，不能以未知冒充不兼容。
- ScalableLux、过旧Sodium、过高Sable Companion及Bits n Bobs声明冲突的旧TFMG等并不在当前选定文件内，无须虚构删除操作。相关条件保留在[静态证据](current-mod-evidence.json)。

本次没有从运行目录卸载JAR；被剔除的Sound Physics Remastered原本就未启用。
