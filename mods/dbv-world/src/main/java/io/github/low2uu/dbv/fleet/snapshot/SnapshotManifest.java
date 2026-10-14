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
