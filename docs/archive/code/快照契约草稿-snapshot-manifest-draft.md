# 快照契约草稿

此类原位于fleet/snapshot，2026-11-05审计确认没有调用、反射或生成器依赖。仅保存尚未接入的格式设想，从运行源码移除；不代表已经实现完整快照恢复。

```java
package io.github.low2uu.dbv.fleet.snapshot;

import java.util.Objects;
import java.util.UUID;

/** Future archive contract. No restore operation is enabled by this metadata alone. */
public record SnapshotManifest(int schemaVersion, UUID vesselId, long generation, UUID snapshotId,
        Kind kind, Coverage coverage, String minecraftVersion, String sableVersion) {
    public enum Kind { DESIGN, PRE_REFIT, VOYAGE, INCIDENT }
    public enum Coverage { METADATA_ONLY, BODY_ONLY, FULL_VALIDATED }
    public SnapshotManifest {
        if (schemaVersion != 1 || generation < 1) { throw new IllegalArgumentException("Unsupported archive identity"); }
        Objects.requireNonNull(vesselId); Objects.requireNonNull(snapshotId);
        Objects.requireNonNull(kind); Objects.requireNonNull(coverage);
        Objects.requireNonNull(minecraftVersion); Objects.requireNonNull(sableVersion);
    }
}

```
