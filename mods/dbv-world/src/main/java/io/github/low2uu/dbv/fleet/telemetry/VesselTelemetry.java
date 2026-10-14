package io.github.low2uu.dbv.fleet.telemetry;

/** Snapshot of observations only. Never used as a recoverable ship backup. */
public record VesselTelemetry(State state, long observedTick, String detail,
        double mass, double speed, Integer depth, Integer cracks, Boolean breached) {
    public enum State { UNREGISTERED, ACTIVE, NOT_LOADED, IDENTITY_CONFLICT, ADAPTER_ERROR }

    public static VesselTelemetry unavailable(State state, long tick, String detail) {
        return new VesselTelemetry(state, tick, detail, Double.NaN, Double.NaN, null, null, null);
    }
}
