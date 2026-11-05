# 本地来源 Mod

用户于2026-10-22提供“超视距.zip”，明确要求加入这两个文件。原始JAR不作二次打包或修改；文件名、内部版本、SHA-256见manifest.json。两个文件均声明MIT；原JAR内的元数据与许可证保留。

Gradle的packLocalRuntime读取此目录中的两个指定文件；开发启动和客户端/服务端导出均包含它们。协作者需拉取本目录，不能只复制build.gradle。

voxy_server_lod文件名为1.1.4，内部版本是1.0.0；Voxy Server Side内部版本是0.16.0（ID为lss）。两者都声明NeoForge1.21.1，但前者说明面向旧社区Voxy移植；与Roxy及另一套LOD传输的兼容、负载和实际效果未进游戏验证。不自动执行预生成命令。
