# 可独立下载的工具

| 工具 | 下载 | 使用方式 |
| --- | --- | --- |
| [内容设计器](content-designer/README.md) | [便携ZIP v0.1.7](content-designer-v0.1.7.zip) · [SHA-256](content-designer-v0.1.7.zip.sha256) | 完整解压后双击`index.html`或Windows下的`start.cmd`，无需安装依赖 |

在GitHub打开ZIP后点击“Download raw file”即可下载，无需下载整个整合包仓库。

`tools/content-designer/`是源程序目录；`tool/content-designer/`是便携分发副本。开发时修改源码，再运行`tools/package-content-designer.ps1`打包；不要单独修改分发副本。
