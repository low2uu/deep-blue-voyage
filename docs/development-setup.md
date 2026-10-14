# 深蓝远航 Windows 协作开发环境部署流程

适用于第一次参与项目的开发者。统一使用 Minecraft 1.21.1、NeoForge、Zulu JDK 21 和 VS Code；每人在自己的电脑克隆同一仓库，通过 GitHub 合作。代码编辑、构建和游戏运行使用同一个正式工程。

编写日期：2026-10-14。

部署前提：截至本文编写时，正式开发工程和启动脚本仍在维护者本地，尚未提交并推送到 GitHub。维护者需要先完成工程构建和启动验收，再发布这些文件；协作者现在可以先完成软件安装。克隆后若没有 `tools/dev.ps1`、`mods/dbv-world/gradlew.bat`、`deep-blue-voyage.code-workspace`，说明还没有拿到开发工程，应联系维护者，不要自行另建 MDK 项目。

## 1 安装软件

| 软件 | 选择与用途 |
| --- | --- |
| [Zulu JDK](https://www.azul.com/downloads/?os=windows&package=jdk&version=java-21-lts) | Java 21 LTS、Windows、x86 64 位、JDK、普通非 JavaFX 版本；优先 MSI 安装包。不要下载成 JRE。 |
| [Git for Windows](https://git-scm.com/install/windows) | 用于克隆、提交和同步。安装时允许命令行及第三方软件使用 Git。 |
| [VS Code](https://code.visualstudio.com/download) | 用于 Java 开发；安装时勾选加入 PATH。 |
| [GitHub](https://github.com/) 账号 | 公共仓库可以直接克隆；向项目推送需要协作者权限，或通过自己的 Fork 提交 PR。 |

建议使用 Windows 10/11 64 位、SSD、16 GB 以上内存，并预留约 20 GB 可用空间。这是开发配置建议，不是已测得的最低要求。

Zulu 安装时勾选 `Add to PATH` 和 `Set JAVA_HOME variable`。`JAVA_HOME` 指向 JDK 根目录，例如 `C:\Program Files\Zulu\zulu-21`，不要指向 `bin`。不用照搬其他成员的安装路径。[Zulu 官方安装说明](https://docs.azul.com/core/install/windows)

已有其他 Java 的电脑，可以保留其他软件需要的版本，但本项目必须选中 Java 21。NeoForge 1.21.1 要求 Java 21 和 64 位 JVM。[NeoForge 官方说明](https://docs.neoforged.net/docs/1.21.1/gettingstarted/)

无需单独安装 Gradle、Maven、IntelliJ IDEA，也无需为源码运行另装启动器或 NeoForge 安装器；仓库自带 Gradle Wrapper，会下载工程所需组件。

## 2 检查安装结果

安装完关闭并重新打开 PowerShell 和 VS Code，然后执行：

```powershell
java -version
javac -version
$env:JAVA_HOME
where.exe java
git --version
code --version
```

`java` 和 `javac` 应显示 21，Java 应为 Zulu/OpenJDK 64 位。如果 `where.exe java` 列出多个位置，检查最前面的是否为预期版本。`code` 无法识别时，可先从开始菜单打开 VS Code。

## 3 获取项目

以下命令使用 `C:\mc` 作为示例目录，可以换成自己电脑上的目录：

```powershell
New-Item -ItemType Directory -Force C:\mc
Set-Location C:\mc
git clone https://github.com/low2uu/deep-blue-voyage.git
Set-Location .\deep-blue-voyage
git config user.name "你的 GitHub 用户名或署名"
git config user.email "你的 GitHub 提交邮箱"
Test-Path .\tools\dev.ps1
Test-Path .\mods\dbv-world\gradlew.bat
Test-Path .\deep-blue-voyage.code-workspace
code .\deep-blue-voyage.code-workspace
```

三个 `Test-Path` 都应返回 `True`。邮箱可以使用 GitHub 邮箱设置页提供的 noreply 地址。这里的 Git 身份设置只针对当前仓库，不负责 GitHub 登录。

## 4 配置 VS Code

在扩展面板安装：

- `Extension Pack for Java`，扩展 ID：`vscjava.vscode-java-pack`。
- `Gradle for Java`，扩展 ID：`vscjava.vscode-gradle`。

Java 扩展的安装与使用可参考 [VS Code 官方说明](https://code.visualstudio.com/docs/java/java-tutorial)。打开工程后等待 Java/Gradle 导入完成；第一次导入需要联网下载依赖。

如果 JAVA_HOME 已正确设置，先使用默认配置。只有 VS Code 或脚本选错 JDK 时，才新建或修改项目的 `.vscode/settings.json`，将以下路径替换为自己的实际路径：

```json
{
  "java.jdt.ls.java.home": "C:/Program Files/Zulu/zulu-21",
  "java.import.gradle.java.home": "C:/Program Files/Zulu/zulu-21",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "C:/Program Files/Zulu/zulu-21",
      "default": true
    }
  ]
}
```

已有设置时合并字段，不要直接覆盖其他配置。这是各自电脑的本地文件，已被 Git 忽略。启动脚本会优先使用其中的 `java.jdt.ls.java.home`，因此过期路径需要一起修正。

## 5 首次构建与运行

在 VS Code 的 PowerShell 终端执行。工作目录必须是包含 `tools` 文件夹的仓库根目录：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 prepare
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 run
```

逐条执行，前一条成功后再执行下一条。`prepare` 下载并准备 Minecraft/NeoForge 开发资源；`build` 编译打包；`run` 启动同一工程的 Minecraft 客户端。这里的执行策略参数仅用于本次 PowerShell 进程。

首次准备依赖和源码可能花费较长时间，后续会使用缓存。网络需要能访问 GitHub、Gradle、NeoForge、Mojang 和 Modrinth 的相关服务。下载失败时保留报错，检查网络后重试原命令，不必先清空全部缓存。

当前工程配置为 NeoForge `21.1.259`、Gradle `9.2.1`、ModDevGradle `2.0.148`。协作者以仓库版本为准，不要各自升级。当前首批运行依赖是 Create、Architectury API、Sable、Create Aeronautics、Create Deep Seas、Power Grid，由 Gradle 按指定版本获取，不要再往运行目录重复放一份。

验收时确认 `build` 出现 `BUILD SUCCESSFUL`，`run` 能加载本项目 Mod，并能创建或进入世界。2026-10-14 已实际创建起始岛存档，完成出生点、海床采样与建设规则检查；潜艇操控和全部工具功能仍待验证，详见 [检查记录](development-verification.md)。

当前采用临时低负载建设配置，集中在 `config/development-session.json`，详细设置和恢复方式见 [起始岛建设阶段方案](game-mechanics/starter-island-plan.md)。它使用同一套正式工程和本机存档。

## 6 日常开发

修改 Java 后，先退出游戏，再执行 `build` 和 `run` 验证。日常通常不必重新执行 `prepare`。

也可以使用 VS Code 的 `终端 → 运行任务`：`DBV: Build` 构建，`DBV: Run Minecraft` 启动；`Ctrl+Shift+B` 使用默认构建任务。

需要断点时，先选择 `DBV: Start Minecraft for Debugger`，待 JVM 等待连接，再在运行和调试面板选择 `DBV: Attach to Minecraft`。它连接本机 5005 端口，使用同一套源码和 `runtime/client`，不会另建一套游戏环境。

以下路径均相对于仓库根目录：

| 内容 | 位置 |
| --- | --- |
| 自研 Mod 的 Java 源码 | `mods/dbv-world/src/main/java` |
| 数据包、语言及 Mod 资源 | `mods/dbv-world/src/main/resources` |
| 依赖和构建配置 | `mods/dbv-world/build.gradle` |
| 游戏机制文档 | `docs/game-mechanics` |
| 打包后的自研 Mod | `mods/dbv-world/build/libs` |
| 本机游戏存档 | `runtime/client/saves` |
| 本机游戏日志 | `runtime/client/logs/latest.log` |
| 下载缓存 | `.local/gradle-home` |

构建出的 JAR 是自研 Mod，不是整个整合包。`runtime`、缓存及构建输出不会通过 Git 同步。运行目录中的配置修改如需分享，应整理到仓库约定的位置并说明部署方法。

## 7 两个人如何合作

维护者在 GitHub 仓库设置中邀请协作者，对方接受后才能向原仓库推送。没有写权限时，可 Fork 仓库后提 PR。每人每项任务使用独立分支，合并前先构建并验证；新增 Mod 仍需项目负责人确认。

下面以具有原仓库写权限的成员新增起始岛工作为例。从干净的工作区开始执行，分支名称可以替换：

```powershell
git status
git switch main
git pull --ff-only
git switch -c feature/starter-island-layout
```

完成修改后执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
git diff
```

确认修改范围后，在 VS Code 的源代码管理面板只暂存本次任务涉及的文件，然后执行：

```powershell
git diff --cached
git commit -m "调整起始岛布局"
git push -u origin feature/starter-island-layout
```

首次推送按 Git 的提示用自己的 GitHub 账号登录。随后在 GitHub 发起 Pull Request，说明修改内容、构建结果和游戏内验证情况，由另一人检查后合并。如果工作区有未完成改动，先提交或暂存自己的工作，再切分支和更新；不要为解决同步问题直接强制覆盖文件。

## 8 共同建图和代码协作的区别

两人克隆同一仓库，只会得到同一套代码，不会自动进入同一个世界。目前工程只配置客户端运行，尚未配置共用专用服务器；开发启动名称也固定为 `DBVDeveloper`，因此不能直接把两个默认开发客户端当作现成联机方案。

要一起在岛上施工，应另行确定共用服务器及客户端身份认证方案，并由服务器保存唯一的权威地图。不要通过 Git 合并两个人分别修改过的世界存档。交接整个存档时应先退出世界或停止服务器，再备份和传输；可复用建筑后续导出为结构或蓝图纳入项目。

## 9 常见问题

| 问题 | 处理方法 |
| --- | --- |
| 没有 dev.ps1 或 gradlew.bat | 先确认正式工程已上传，并且自己克隆了正确分支；不要另建空项目。 |
| java 不是 21，或找不到 javac | 检查安装的是 JDK 21，修正 JAVA_HOME 和 PATH，重开终端。 |
| 提示 Configure a Java 21 JDK | 检查 JAVA_HOME；若存在本地 settings.json，检查其中是否残留别人的路径。 |
| VS Code 不识别 Java 工程 | 确认打开仓库工作区、扩展启用，等待导入；必要时执行 Java: Import Java Projects。 |
| 下载超时 | 检查报错指向的域名和网络/代理设置，然后重试；不要关闭证书校验。 |
| 游戏崩溃 | 提供 latest.log、存在时的 crash-reports 文件，以及复现步骤；不要只截取最后一行。 |
| git push 被拒绝 | 确认账号和协作者权限，或改用 Fork + PR；不要用强制推送绕过。 |
| 调试启动后没有游戏窗口 | 可能正在等待 VS Code 连接 5005 端口；正常游玩请使用 run。 |

部署成功后发给维护者：`java -version` 输出、`git rev-parse --short HEAD` 输出、构建结果，以及客户端能否进入世界。
