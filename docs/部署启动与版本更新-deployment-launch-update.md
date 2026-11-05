# 部署、启动与更新

整理日期：2026-11-05。项目根目录是`deep-blue-voyage`；统一使用`develop`分支、Minecraft 1.21.1、NeoForge 21.1.259及Zulu JDK 21。源码开发版本为0.1.16，是否已提交/同步以`git status`和Git提交为准。

## 已有环境：启动开发客户端

在项目根目录执行，前一步成功后再继续：

```powershell
Set-Location E:\mc\project\deep-blue-voyage
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 run
```

`build`只构建和运行单元测试；`run`才启动游戏。`debug`会等待调试器，不是普通启动命令。运行依赖由Gradle加载，不要在`runtime/client/mods`再放同名Mod或自研JAR。

单人游戏选择“深蓝远航·联合开发”，对应`runtime/client/saves/深蓝远航-联合开发`。该目录是本机游玩进度，Git不管理它。

## 新电脑：安装、克隆、构建

安装完整的Zulu JDK 21、Git和VS Code；安装路径与Java配置详见[开发环境部署](开发环境部署-development-setup.md)。项目携带Gradle Wrapper，不另建MDK。

```powershell
git clone --branch develop https://github.com/low2uu/deep-blue-voyage.git
Set-Location .\deep-blue-voyage
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
java -version
javac -version
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 prepare
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
```

将JAVA_HOME示例替换为本机真实路径；`java`和`javac`都应为21。`tools/dev.ps1`优先使用本机`.vscode/settings.json`的`java.jdt.ls.java.home`（不纳入Git），其次使用JAVA_HOME。VS Code打开根目录或`deep-blue-voyage.code-workspace`。

首次下载依赖需要网络。原件依赖在`pack/local-mods`，应随仓库完整拉取。当前渲染默认采用Voxy/Roxy，动态光源暂缓；状态见[接入说明](development/远景与动态光源接入-lod-and-dynamic-lights.md)。构建成功不等于所有Mod已通过实机联动验收。

## 地图：交付快照和当前进度

仓库交付入口只有`pack/worlds/world-manifest.json`及其列出的分卷。当前快照版本为0.1.4；它与源码版本是两个独立版本号，不表示代码应退回0.1.4。

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\restore-world.ps1 -VerifyOnly
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\restore-world.ps1
```

脚本校验分卷与完整SHA-256，再导入`runtime/client/saves`。已有同名世界不会覆盖。保留正在玩的存档，不能通过删除它来“更新代码”。需要伙伴的最新建设进度时，在对方保存退出后交接整个世界目录，包括`sublevels`及`dbv/fleet`，不能只复制`region`或`level.dat`。完整说明见[世界交付](../pack/worlds/README.md)。

新建世界时选择“深蓝远航 起始岛”预设；普通默认世界不会自动获得本项目地形。开发建设使用创造模式和允许命令。`config/development-session.json`管理临时建设模式、视距和帧率，不能把建设设置当作最终生存规则。地图与潜艇设计进展见[系统开发索引](development/README.md)。

## 获取最新代码

先保存退出Minecraft和服务端。检查本地改动，并自行提交需要保留的开发成果后再拉取；不要重置或清空工作区来跳过冲突。

```powershell
git status --short
git branch --show-current
git pull --ff-only origin develop
git log -1 --oneline
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\dev.ps1 build
```

确认当前已经在`develop`再执行拉取；其他分支不要直接照抄。拉取失败时先处理错误。重新执行`run`才能载入新Java代码，旧游戏进程不会热更新。拉代码不更新本机世界进度，也不自动覆盖玩家潜艇。

改装操作、JSON备份和控制器使用分别见[改装流程](development/初始潜艇与改装流程-starter-submarine-refit.md)、[独立数据与面板](development/主艇独立数据与舱内面板-vessel-data-and-cabin-panel.md)、[舱门](development/舱内设备与模块化舱门-cabin-equipment-and-modular-hatches.md)。这些功能仍有开发阶段限制，尚不是完整生存冒险成品。

## 双人服务器与排错

离线昵称、IPv6、服务端部署与更新统一看[server/README.md](../server/README.md)，不在本页维护第二套流程。服务器世界在`runtime/server/world`，不是单人世界目录；联机双方应使用不同固定昵称和同版本客户端。

| 问题 | 检查 |
| --- | --- |
| build成功但无窗口 | 执行run；build不会启动游戏 |
| debug一直等待 | 改用run，或按VS Code配置连接调试器 |
| 地图不在列表 | 核对运行目录与saves路径；按manifest导入交付快照 |
| 修改后仍是旧界面 | 完全退出旧进程，核对分支、提交和版本，重新build/run |
| 存档被占用 | 保存退出占用它的游戏；不要删除session.lock绕过 |
| 缺依赖或崩溃 | 保留`runtime/client/logs/latest.log`及crash-reports，附Git提交和复现步骤 |
| 下载慢 | 保留Gradle缓存，确认网络；不要删缓存后不断重试 |

旧世界4/世界5、v2/v3/v4、初期参数及验收报告见[历史归档](archive/README.md)，不再作为当前部署入口。
