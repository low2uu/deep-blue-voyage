# 可独立下载的工具

| 工具 | 下载 | 使用方式 |
| --- | --- | --- |
| [内容设计器](content-designer/README.md) | [便携ZIP v0.1.16](content-designer-v0.1.16.zip) · [SHA-256](content-designer-v0.1.16.zip.sha256) | 完整解压后双击`index.html`或Windows下的`start.cmd`，无需安装依赖 |

在GitHub打开ZIP后点击“Download raw file”即可下载，无需下载整个整合包仓库。

`tools/content-designer/`是源程序目录；`tool/content-designer/`是便携分发副本。开发时修改源码，再运行`tools/package-content-designer.ps1`打包；不要单独修改分发副本。

当前仅保留内容设计器v0.1.16便携版及其展开副本。旧设计器发行包可从Git历史恢复；`item-models`、`creature-models`为独立的历史资源交付，并非当前设计器源码。当前16×16素材以源码生成结果为准。
