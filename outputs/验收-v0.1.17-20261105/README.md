# v0.1.17 主艇完整恢复验收

日期：2026-11-05。Minecraft 1.21.1 / NeoForge 21.1.259 / Zulu Java 21。

**结果：90 项 Java 测试及四次独立无界面服务端启动验收全部通过。** 使用世界 5 的真实用户设计艇和锁定的 Mod 组合，正式客户端、正式服务器及玩家存档未被修改。当前测试不包含玩家 GUI 点击或双人界面操作。

## 整体验收

| 阶段 | 实际操作及断言 | 结果文件 |
| --- | --- | --- |
| recover | 原设计艇加连接支架及真实设备样本，保存 JSON 检查点后删除原船，重新部署；核对永久 ID、方块、设备、带组件物品、柴油、水、氧气、FE、轴承活动结构与系泊 | [recover.json](recover.json) |
| restart | 同一临时世界重新启动，确认身份、设备与资源保存；再次开始恢复并在登记提交前保存停服，留下真实中断事务 | [restart.json](restart.json) |
| resume | 再次启动，清理未提交候选，使用原恢复点重试；核对新船和物品，永久 ID 保留 | [resume.json](resume.json) |
| splitrollback | 故意构造一个不连接的载物木桶，触发原生分裂及内容核对失败；验证根候选与拆分子船均从内存和磁盘清除，无掉落物副本，登记代次与恢复点不变 | [splitrollback.json](splitrollback.json) |

测试船含支架和样本共 **1693 个静态方块、321 个方块实体、1 个轴承活动结构**。样本包含命名钻石、自定义组件/大整数数据、带耐久的工具、柴油储罐、电解器和氧气释放器。第一次恢复以精确的 1234 FE 等非整千资源值验收；正常重启后岸供可继续工作，探针记录当时实际值再验证下一次恢复，不把合法生产/补给误判为丢失。

负向测试确实产生了一个额外拆分载体；报告记录两个候选全部移除、`cargoCopiesRemaining=0`、`identityUnchanged=true`、`checkpointUnchanged=true`。该阶段报告中的设备缺失提示是预期拒绝原因，`passed=true` 表示回滚与清理验证通过。

## 单元与文件检查

- 90 项 Java 测试，0 失败、0 错误、0 跳过；按测试类统计见 [verification.json](verification.json)。
- 包括 typed JSON 无损数值/数组、旧 NBT 迁移、损伤冻结、子船/运动结构损失、状态文件先写残骸时断电、当前镜像损坏、登记 pending、事务重读、资源回放和语言资源。
- 26 份原游戏机制和数值资料逐文件 SHA-256 保持不变。
- 实际服务器加载的自研 JAR SHA-256：`2fec8f804733e02ef688294f08c652daa0d0607a71595bb4614448b87ddd573b`；与最终本机构建产物一致。所有测试 Mod 的哈希见 [runtime-manifest.json](runtime-manifest.json)。

## 复现与证据边界

从仓库根目录执行 `tools/test-whole-vessel-recovery.ps1 prepare`，再执行 `run`。它们只在 `.local/acceptance/whole-vessel-recovery/` 创建的专用世界中运行；探针不打入正常发行包。测试会主动生成、销毁和恢复船体，应使用脚本管理的临时目录。

本机最终批次：`.local/acceptance/whole-vessel-recovery/20261105-121611-dc02376c`。完整日志、测试世界和事务留在本机，Git 仅保存小型验收报告和依赖清单。

本报告证明当前依赖组合和这些设备样本的恢复与回滚。新增第三方设备的全局外部数据、额外实体类型及未来 Mod 版本仍需按其保存方式适配和验证。使用步骤见[主艇完整恢复与 JSON 档案](../../docs/development/主艇完整恢复与JSON档案-whole-vessel-recovery-and-json.md)。
