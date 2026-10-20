# 双人联机服务器

伙伴开独立服务端，你们各自用登录正版账号的客户端进入。服务器负责保存世界与主潜艇；GitHub 保存代码、版本锁定和初始地图。**拉取更新不会重置你们正在玩的世界。**

当前目标：Windows 10/11、Zulu **JDK 21**、Minecraft **1.21.1**、NeoForge **21.1.259**。建议服务器分配 6 GB 内存；同时运行客户端的电脑建议至少 16 GB 总内存。性能取决于船体、机械和探索量。

## 1. 伙伴首次部署

安装 Git 和完整 Zulu JDK 21。以下路径仅为示例，替换成伙伴自己的目录：

```powershell
git clone --branch develop https://github.com/low2uu/deep-blue-voyage.git
cd deep-blue-voyage
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
powershell -NoProfile -ExecutionPolicy Bypass -File .\server\manage.ps1 prepare
```

已有仓库则进入项目根目录，先 `git pull --ff-only origin develop`，再执行 `prepare`。首次需要联网下载 Gradle、Mod 和官方 NeoForge 服务端，耗时取决于网络。不要同时运行两次维护命令。

脚本会构建并测试自研 Mod，按 Gradle 锁定版本下载依赖，分别生成服务端和客户端文件，校验官方安装器 SHA-256，安装 NeoForge，并校验、导入 `pack/worlds` 的初始地图为 `runtime/server/world`。服务器运行目录完全在 Git 忽略范围内。

部署完成后：

1. 阅读 [Minecraft EULA](https://www.minecraft.net/eula)，同意后自行将 `runtime/server/eula.txt` 中的 `eula=false` 改为 `eula=true`。
2. 检查 `runtime/server/user_jvm_args.txt`。默认 `-Xms2G -Xmx6G`；不要超过电脑可用内存。`-Ddbv.construction=true` 用于当前建设测试阶段，会启用项目建设模式规则；正式冒险时再改成 `false`。
3. 执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\server\manage.ps1 start
```

看到 `Done (...)!` 后，在这个服务器窗口输入（没有 `/`）：

```text
whitelist add 你的正版游戏名
whitelist add 伙伴的正版游戏名
op 你的正版游戏名
op 伙伴的正版游戏名
```

测试改装、WorldEdit 等功能时，可在游戏内使用 `/gamemode creative`。仅给可信伙伴 OP。关服在服务器窗口输入 `stop`，等待保存完成并回到 PowerShell 提示符；不要直接关窗口。

## 2. 双方安装一致的客户端

`prepare` 成功时会打印 `dist/coop/<时间与编号>/client-pack.zip`。伙伴把这份包发给你，**双方使用同一份**。也可以在相同 Git 提交下执行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\server\manage.ps1 client-pack
```

在支持 NeoForge 的启动器里创建独立实例：Minecraft 1.21.1 + NeoForge 21.1.259，选择 Java 21，登录各自正版账号。关闭游戏后解压客户端包到该实例的游戏目录：

- 首次放入 `mods`、`config`、`defaultconfigs`、资源包等目录。
- 更新时先备份实例，把旧 `mods` 整个移到实例之外，再放入新 `mods`；不要将新旧版本 Jar 混在一起。
- `config` 的已有个人设置先保留，对照包中的配置合并。`resourcepacks` 可更新，游戏内启用 `dbv-zh-cn` 中文资源包。
- 保留 `options.txt`、地图标记和个人存档；加入服务器不需要导入服务器地图。
- 保留 `pack-version.json`，双方用其中的 Git 提交、NeoForge 版本及 Mod 哈希核对。

**不要使用开发入口 `tools/dev.ps1 run` 联机**：它使用固定的开发用户名，不能代替正版启动器登录。服务端保持 `online-mode=true`。

## 3. 连到伙伴的电脑

- 同一局域网：伙伴在 `ipconfig` 中查看局域网 IPv4，你连接 `伙伴IP:25565`；伙伴本机连接 `localhost:25565`。
- 不同网络：可用双方已配置好的组网 VPN，连接伙伴的虚拟局域网 IP；或由伙伴配置路由器端口转发。
- Minecraft 使用 **TCP 25565**；当前 Sable 的物理同步也使用同端口 UDP，已在本机验证。因此同一端口同时允许 **TCP/UDP**，并检查服务器日志和实际 UDP 监听。仅支持 TCP 的内网穿透可能导致物理同步性能下降，不能当作完整验收。
- Windows 防火墙仅对所使用的网络放行 Java/相应端口。不要开放 RCON、不要关闭整个防火墙。示例配置已关闭 RCON 和 query。
- `server-ip=` 保持空白。公网入口由路由器/组网软件负责，不填写公网 IP 到这里。

白名单已默认启用。在白名单加人之前，普通玩家被拒绝连接是预期行为。

## 4. 从 GitHub 更新

先通知双方退出游戏，在服务端输入 `stop`。确认服务器进程已退出后，在伙伴仓库执行：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
powershell -NoProfile -ExecutionPolicy Bypass -File .\server\manage.ps1 update
```

更新顺序：检查 `develop` 和干净工作区 → 完整压缩备份 → `git pull --ff-only origin develop` → 构建和测试 → 安装所需 NeoForge → 替换服务端 Mod → 输出新的客户端包。构建失败不会替换原有 Mod；后续步骤失败则按回退说明恢复，**不要继续启动半更新状态**。

更新保留服务器世界、白名单、OP、服务器配置、JVM 配置和独立潜艇数据。已有配置不会被模板自动覆盖；遇到配置结构变化，按版本记录人工迁移。已存在 `world` 时绝不重新导入 GitHub 初始地图。不要删除它来“更新版本”。

将新客户端包发给双方，按第 2 节更新，然后执行 `start`。自动更新不会强行丢弃你们的代码修改或合并冲突；工作区有修改时会停止，先自行处理。

可以随时拉取和准备新版，**不能在服务器运行中热替换 Mod**。同一台电脑的 `start`、`prepare`、`backup`、`update` 有互斥锁；始终通过本脚本启动，否则无法提供这项进程保护。

## 5. 备份、回退与数据位置

停服后手动备份：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\server\manage.ps1 backup
powershell -NoProfile -ExecutionPolicy Bypass -File .\server\manage.ps1 status
```

| 内容 | 仓库内相对路径 | 是否上传 Git |
| --- | --- | --- |
| 服务器运行文件、设置和 Mod | `runtime/server/` | 否 |
| 正在玩的世界及世界内潜艇独立数据 | `runtime/server/world/` | 否 |
| 完整备份 ZIP（包含整个运行目录） | `runtime/server-backups/` | 否 |
| 客户端更新包、版本与哈希 | `dist/coop/` | 否 |
| 部署脚本及说明 | `server/` | 是 |
| 初次部署用的地图 | `pack/worlds/` | 是 |

回退步骤：停服 → 将当前 `runtime/server` 改名留存 → 把某次完整备份解压成新的 `runtime/server`（`server.properties` 应直接位于该目录）→ 双方换回该次对应客户端包 → 执行 `start`。启动使用备份内记录的 NeoForge 版本，不会因为当前 Git 是新版而强制升级。

不要只恢复 `region`：潜艇物理船体、维度、玩家、独立 JSON 档案、Mod 配置可能分散在多个目录。完整服务端备份把它们一起保存；这是停服一致性备份。每个完整备份另有 `.sha256` 校验文件，可用 `Get-FileHash -Algorithm SHA256` 比对；`.partial` 表示未完成的备份，不用于恢复。备份不会自动清理，定期复制到另一块磁盘并检查剩余空间。

## 6. 故障排查

- 找不到 Java：设置 `JAVA_HOME`，或给命令追加 `-JavaHome '实际JDK路径'`，需要 JDK 21。
- 下载失败：检查官方 Maven/Modrinth 网络访问；重试 `prepare`，不要跳过校验。
- 拒绝连接：确认 `Done`、白名单、IP、端口和防火墙。跨网再检查路由/VPN。
- 身份验证失败：双方使用启动器正版登录，不能共用开发用户名。
- Mod 版本不匹配：双方核对本次包的 `pack-version.json`，清理旧 Mod 混装。
- 更新失败：保存控制台输出和 `runtime/server/logs/latest.log`，按完整备份回退；不通过删除世界解决。
- 无法启动或潜艇异常：收集 `logs/latest.log`、`crash-reports/` 与版本清单，发给开发者。不要公开含个人地址等信息的完整配置。

部署方式依据 [NeoForge 官方服务端安装说明](https://docs.neoforged.net/user/docs/server/)。具体本次验证结果见[联机验证记录](联机验证记录-coop-validation.md)。
