# 深蓝远航：部署、启动地图与进入最新版本

更新日期：2026-10-16。适用于 Windows、Minecraft 1.21.1、NeoForge 21.1.259、Zulu JDK 21 和 VS Code。使用现有正式开发工程，当前版本为起始岛建设与潜艇改装原型。

## 1 已有环境：本机直接启动

项目根目录是 **`E:\mc\project\deep-blue-voyage`**，不是外层的 `project`，也不是里面的 `mods\dbv-world`。

在 VS Code 终端或桌面 PowerShell 中，逐条执行：

```powershell
Set-Location E:\mc\project\deep-blue-voyage
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 run
```

`build` 显示 `BUILD SUCCESSFUL` 后再执行 `run`。游戏会打开独立窗口，运行期间保留终端。正常启动使用 `run`；`debug` 会等待调试器连接，不能用来代替普通启动。

进入游戏后：

1. 在语言设置中选择“简体中文”。
2. 点击“单人游戏”。
3. 选择 **深蓝远航·联合开发**。
4. 点击“进入选中的世界”，等待地形和潜艇加载。

本机地图对应关系如下，游戏显示名称与磁盘文件夹名称不同：

| 本机文件夹 | 游戏中名称／用途 | 应如何选择 |
| --- | --- | --- |
| `runtime/client/saves/深蓝远航-联合开发` | **深蓝远航·联合开发** | 当前继续建设与体验潜艇管理 UI 用这个 |

如果没有联合开发存档，请按第 4 节新建世界，或按第 5 节接收完整存档。不要把旧存档简单改名为联合开发存档，改名不会增加潜艇。

海面为Y=128，世界范围Y=-256～319。v0.1.4 联合开发存档按用户授权重建，包含起始岛和新版初始潜艇；外海是被海水淹没的自然地形，中心10000格以外也会随探索生成区域与资源岛。矿种自动扫描，最近三岛为铁、铜、金。旧活动存档清理前保留完整压缩备份。详见[自动矿种与无限探索](development/自动矿种与无限探索-v0.1.4.md)。

## 2 每次更新：获取最新代码并重新启动

“最新代码”和“最新地图”是两件事：GitHub 同步源码、依赖版本和文档；`runtime/client/saves` 下的游戏进度不通过 Git 同步。

先在游戏里保存并退出，再完全关闭 Minecraft。若正在改装，先提交或取消；未结束的改装在下次加载时会自动还原。

### 2.1 备份正在玩的地图

在项目根目录执行以下命令，示例针对本机联合开发存档。使用其他地图时改为对应文件夹名。

```powershell
$worldPath = Join-Path (Get-Location) 'runtime\client\saves\深蓝远航-联合开发'
if (!(Test-Path -LiteralPath $worldPath)) { throw '未找到联合开发存档，请核对存档文件夹名。' }
$backupPath = Join-Path (Get-Location) ('runtime\backups\深蓝远航-联合开发-before-update-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Force .\runtime\backups | Out-Null
Copy-Item -LiteralPath $worldPath -Destination $backupPath -Recurse
```

必须退出游戏后再备份；复制整个世界文件夹，包含潜艇物理数据、登记档案和改装备份。

### 2.2 更新 main 分支

先检查工作区：

```powershell
git status --short
git branch --show-current
```

如果第一条有输出，说明有本地改动。先提交自己的工作，或由开发者确认如何保存这些改动，再继续；不要直接清空改动。普通体验者使用 `main` 分支，开发者若在功能分支上，不要盲目切换。

工作区干净、确认可以切换后，逐条执行：

```powershell
git switch main
git pull --ff-only origin main
git log -1 --oneline
git rev-parse HEAD
git rev-parse origin/main
```

最后两条输出一致，表示本机 HEAD 与本次拉取的 GitHub `main` 一致。`git pull` 报错时先处理错误，不要继续并认为已经更新成功；不要用强制重置覆盖自己的开发成果。

再执行第 1 节的 `build` 和 `run`，进入 v4 或自己当前的正式地图。Java 改动需要退出旧客户端后重新启动才生效。

**版本核对：**本文编写时，中文潜艇管理 UI 已在提交 `02bc281` 中发布。此提交用于追溯，不是让你切回该历史版本；日常使用最新 `main`。自研 Mod 显示版本目前仍为 `0.1.0`，地图名中的 `v4` 也不是代码版本号，判断更新应以 Git 提交和实际功能为准。

更新 Mod 不会重刷已有建筑，也不会给旧世界自动补造初始艇。想体验新版世界生成，请另建起始岛预设世界，保留旧地图。

## 3 新电脑：从零部署

### 3.1 准备软件

安装 **Zulu JDK 21（64 位 JDK，不是 JRE）**、Git for Windows 和 VS Code。软件链接及安装细节见 [开发环境部署](开发环境部署-development-setup.md)。不需要另建 MDK，也不需要另装 Gradle。

安装后重新打开 PowerShell，检查：

```powershell
java -version
javac -version
git --version
```

Java 和 javac 应为 21。`JAVA_HOME` 应指向本机 JDK 根目录，不包含末尾的 `bin`。如尚未配置，可在当前终端设置，路径必须改为自己的实际安装位置：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe')
```

结果应为 `True`；此环境变量只对当前终端及其子进程有效。长期使用请配置系统环境变量，或按部署文档设置本地 `.vscode/settings.json`。启动脚本优先读取该文件中的 `java.jdt.ls.java.home`，不要留下别人的 JDK 路径。

### 3.2 克隆工程

以下以 `C:\mc` 为例，已克隆的电脑不要重复执行 `git clone`：

```powershell
New-Item -ItemType Directory -Force C:\mc | Out-Null
Set-Location C:\mc
git clone https://github.com/low2uu/deep-blue-voyage.git
Set-Location .\deep-blue-voyage
code .\deep-blue-voyage.code-workspace
```

在 VS Code 安装推荐的 Java 和 Gradle 扩展，等待导入。若 `code` 命令不可用，直接用 VS Code 打开仓库中的工作区文件。

### 3.3 准备、构建、运行

在仓库根目录逐条执行，前一条成功再继续：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 prepare
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 run
```

首次会下载依赖，需要网络和等待时间；以后通常只需要 `build`、`run`。所需 Mod 由工程按固定版本加载，不要再把同名 JAR 复制进 `runtime/client/mods`，也不要把自研 Mod 的构建 JAR 重复放进去。

## 4 新电脑没有地图：创建最新版起始岛

推荐用这个流程体验当前代码生成的岛屿和初始小艇。

1. 启动前检查 `config/development-session.json` 中 `constructionMode` 为 `true`。这是临时建设配置，不是最终生存规则。
2. 启动游戏，选择“单人游戏 → 创建新的世界”。
3. 输入新名称，例如“深蓝远航－当前建设”。选择**创造模式**，开启**允许命令／作弊**。
4. 在世界设置中切换“世界类型”，选择 **深蓝远航 起始岛**；英文为 **Deep Blue Voyage Starter Island**。不要选普通默认世界或超平坦。
5. 创建世界。如果出现自定义世界／实验设置确认，确认这是本项目的起始岛预设后继续。
6. 进入后等待初始艇生成、制氧启动和密闭扫描。主艇完成检查后自动登记，不需要手动再造第二艘。
7. 前往船坞查看小艇，并按第 6 节检查新界面。

初始小艇约在 `(64,136,157)`，出生广场约在 `(-84,134,-28)`。需要快速定位时，可在允许命令的创造世界中执行：

```text
/gamemode creative
/tp @s 64 142 157
```

传送点在小艇上方。通过顶部舱口进入后，关闭舱门，等待资格刷新。新世界的名字与存档目录可以自定，不必一定叫 `深蓝远航-联合开发`。

目前是船坞样艇和有限改装原型，尚未配备完整推进、武器或酒狐 AI，不能把它当作已完成的出海成品。

## 5 想使用同一张现成地图：交接完整存档

截至本文编写，GitHub 只有 `assets/worlds/DBV-Island-v1.zip` 这份旧建筑快照，**没有本机 v4 的完整存档**。克隆仓库后没看到 v4 是正常现象。

要继续维护者当前的 v4 地图：

1. 发送方保存并完全退出游戏，复制或压缩整个 `runtime/client/saves/深蓝远航-联合开发` 文件夹。
2. 接收方更新代码、构建成功并关闭游戏。
3. 接收方如有同名地图，先备份并保留，给接收的地图使用不同文件夹名，避免合并覆盖。
4. 将完整存档放入本机 `runtime/client/saves/`。确认 `level.dat` 直接位于该地图文件夹下，例如 `saves/深蓝远航-联合开发/level.dat`，不要多套一层同名文件夹。
5. 重新 `run`，在单人游戏列表选择 **深蓝远航·联合开发**。只改磁盘文件夹名通常不会改变游戏内显示名称。

不要只复制 `region` 或 `level.dat`；主艇登记、Sable 物理船体和船坞快照也需要一起交接。不要把两个人分别施工的世界直接覆盖合并。此流程是存档交接，不是联机服务器部署；当前两个默认开发客户端都使用 `DBVDeveloper`，共用服务器和玩家身份仍需另行配置。

## 6 进入后确认新版本功能

1. 靠近船载电脑，按住 **Shift＋右键**。
2. 应打开独立中文“潜艇管理”窗口，而不是只在聊天栏输出信息。
3. 能看到“船况与改装”“艇内设备”，以及“开始改装／重新检查／完成改装／取消还原”。
4. 在船坞中以创造管理员开始改装，先备份并拆为世界方块；此时不要求三项符合。点击完成改装时才检测三项，通过后恢复为物理船体。
5. 开始后关闭窗口即可建造；关闭窗口不会取消改装。完成后再次打开，提交或取消还原；取消需要二次确认。

本版编辑范围是原船包围盒，尚不支持扩容和任意设备。详细白名单、恢复边界及命令见 [初始潜艇与改装流程](development/初始潜艇与改装流程-starter-submarine-refit.md)。

## 7 常见问题

| 情况 | 处理 |
| --- | --- |
| 构建成功，但没有游戏窗口 | `build` 只编译；还要执行 `run`。保留终端，等待启动。 |
| 用 debug 一直没有主菜单 | 正在等调试器；正常开启改用 `run`。 |
| 本机 v4 不在列表 | 检查启动目录和 `saves` 路径；新电脑按第 4 节建图或第 5 节接收存档。 |
| 创建世界没有“深蓝远航 起始岛” | 核对从本仓库 `run` 启动、构建成功且自研 Mod 已加载，不能从缺少本 Mod 的普通启动器创建。 |
| 更新后还是聊天提示，没有面板 | 完全退出旧游戏，核对 Git 提交，重新构建并运行。确认 Shift＋右键的目标是船载电脑。 |
| 控制器仍显示英文 | 切换为简体中文，核对代码已更新，并重新启动客户端。 |
| 开始改装按钮灰色 | 查看三条件与提示；开始只检查身份、创造模式、命令权限、船坞、系泊与泊位占用；三项资格只限制完成改装。长说明可悬停查看。 |
| 权限不足 | 新建开发世界时开启命令。已有单人世界可通过“对局域网开放”临时允许命令；此操作会开放局域网访问，退出后不再持续。 |
| 存档被占用／session.lock 错误 | 先保存并完全退出另一个客户端，不要同时打开同一存档，也不要手动删除锁文件绕过。 |
| 下载失败或崩溃 | 保留 `runtime/client/logs/latest.log` 及存在时的 `crash-reports`，附上 `git log -1 --oneline` 和复现步骤。 |

正常退出使用“保存并退回到标题画面”，然后退出游戏；等终端中的运行任务结束再关终端。

当前协作交付为 `pack/worlds/` 的联合开发存档分卷；使用下文的 `tools/restore-world.ps1` 自动校验并导入。已有同名存档先另行备份，禁止合并覆盖。


## v0.1.4 存档交付与新建世界

当前交付采用 `pack/worlds/深蓝远航-联合开发.zip.001`，本版仅一卷，卷数以 world-manifest.json 为准。拉取完整仓库后，运行 `powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\restore-world.ps1`，会校验各卷与完整 SHA-256 后导入；已有同名存档则拒绝覆盖。请先另行保存自己的进度，不要手工合并存档。详情见 [世界存档交付](../pack/worlds/README.md)。

不使用交付快照也可以新建：仍须在“世界类型”选择 **深蓝远航 起始岛**。起始岛、随机区域占位和每岛分配一种额外矿物的资源岛都属于该预设的正式生成器，按新种子及探索生成；不是只对交付地图有效的一次性脚本。普通默认世界仍是原版世界类型，不会自动变成本整合包地图。
