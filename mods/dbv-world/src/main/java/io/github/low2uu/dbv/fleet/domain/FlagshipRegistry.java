package io.github.low2uu.dbv.fleet.domain;

import java.util.Optional;
import java.util.UUID;

/** Immutable single-slot registry. No delete/rebind operation until recovery transactions exist. */
public record FlagshipRegistry(Optional<VesselIdentity> flagship) {
    public FlagshipRegistry { java.util.Objects.requireNonNull(flagship); }

    public static FlagshipRegistry empty() { return new FlagshipRegistry(Optional.empty()); }

    public FlagshipRegistry register(UUID bodyId, String dimension, String name) {
        if (flagship.isPresent()) { throw new IllegalStateException("This world already has a flagship"); }
        return new FlagshipRegistry(Optional.of(new VesselIdentity(UUID.randomUUID(), bodyId, dimension, name, 1)));
    }
}
