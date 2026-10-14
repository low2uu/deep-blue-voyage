package io.github.low2uu.dbv.fleet;

import io.github.low2uu.dbv.fleet.domain.Qualification;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualificationTest {
    @Test void everyMissingConditionAndPendingStateDeniesAdmission() {
        for (int mask = 0; mask < 16; mask++) {
            var q = new Qualification((mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, (mask & 8) != 0);
            assertEquals(mask == 7, q.eligible(), "condition mask=" + mask);
            assertEquals(q.eligible(), q.reasons().isEmpty());
        }
    }
}
