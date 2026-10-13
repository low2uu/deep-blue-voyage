# 深蓝远航

以《海底两万里》式潜艇探险为主题的 Minecraft 整合包。玩家建造大型潜艇，在广阔、连通的海洋中远航，探索不同海洋群系、海底遗迹与海洋 Boss。

英文工作名为 Deep Blue Voyage，目录名为 `deep-blue-voyage`。

## 开发环境

- Minecraft：1.21.1。
- 加载器：NeoForge，具体构建版本在候选 Mod 兼容性确认后锁定。
- 自研 Mod 语言：Java 21。
- 本机 JDK：Zulu 21.0.3。
- 编辑器：VS Code，已配置 Java 和 Gradle 扩展。

使用 VS Code 打开本目录，并安装推荐的 Java 和 Gradle 扩展。将 `JAVA_HOME` 指向本机 JDK 21。可通过不纳入版本管理的 `.vscode/settings.json` 设置本机 JDK 路径；仓库不携带开发者的绝对路径。运行 `powershell -ExecutionPolicy Bypass -File tools/check-environment.ps1` 可检查本机 Java 与 Git。

当前完成目录和环境配置；尚未下载 Mod、创建 NeoForge MDK 或安装测试实例。各 Mod 工程后续使用自己的 Gradle Wrapper。

## 目录用途

| 目录 | 用途 |
| --- | --- |
| `docs/` | 玩法设计、Mod 选型和测试标准 |
| `pack/overrides/` | 整合包配置、脚本与资源，作为打包输入 |
| `mods/` | 自研 Java Mod 工程，每个 Mod 单独建目录 |
| `assets/` | 原始纹理、模型、音效和宣传素材 |
| `runtime/client/` | 独立客户端测试实例 |
| `runtime/server/` | 独立服务端测试实例 |
| `runtime/backups/` | 测试存档备份 |
| `dist/` | 后续导出的整合包与发布文件 |
| `tools/` | 环境检查和后续构建辅助脚本 |

`runtime/` 和 `dist/` 不纳入版本管理。将需要发布的配置同步回 `pack/overrides/`，不要把测试存档、日志或账号信息混入发布包。

## 下一步

先核实航空学、deepsea、交错电网的准确项目名称、1.21.1 NeoForge 支持情况及依赖，再确定潜艇实现方案。随后制作一个可航行的大型潜艇原型，用它验证海洋地形尺度，再扩展群系、Boss 与成长路线。
