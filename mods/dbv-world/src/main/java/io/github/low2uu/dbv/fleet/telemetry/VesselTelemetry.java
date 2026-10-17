package io.github.low2uu.dbv.fleet.telemetry;

/** Snapshot of observations only. Never used as a recoverable ship backup. */
public record VesselTelemetry(State state, long observedTick, String detail,
        double mass, double speed, Integer depth, Integer cracks, Boolean breached) {
    public enum State {
        UNREGISTERED, ACTIVE, REFITTING, NOT_LOADED, IDENTITY_CONFLICT, ADAPTER_ERROR;

        public String displayName() {
            return switch (this) {
                case UNREGISTERED -> "尚未登记";
                case ACTIVE -> "正常运行";
                case REFITTING -> "正在改装";
                case NOT_LOADED -> "船体尚未加载";
                case IDENTITY_CONFLICT -> "主艇身份冲突";
                case ADAPTER_ERROR -> "船况读取异常";
            };
        }
    }

    public static VesselTelemetry unavailable(State state, long tick, String detail) {
        return new VesselTelemetry(state, tick, detail, Double.NaN, Double.NaN, null, null, null);
    }
}
