package io.github.low2uu.dbv.fleet.domain;

import java.util.Objects;
import java.util.UUID;

/** Persistent campaign identity, independent of the current physics body. */
public record VesselIdentity(UUID vesselId, UUID bodyId, String dimension, String name, long generation) {
    public VesselIdentity {
        Objects.requireNonNull(vesselId);
        Objects.requireNonNull(bodyId);
        Objects.requireNonNull(dimension);
        Objects.requireNonNull(name);
        if (!dimension.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("Invalid dimension identifier");
        }
        name = name.strip();
        if (name.isEmpty() || name.length() > 64 || name.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Ship name must contain 1–64 printable characters");
        }
        if (generation < 1) { throw new IllegalArgumentException("Invalid vessel generation"); }
    }
}
