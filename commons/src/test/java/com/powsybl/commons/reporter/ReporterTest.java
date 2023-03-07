package com.powsybl.commons.reporter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.commons.reporter.TypedValue.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReporterTest {

    @Test
    void testReporterIsEnabled() {
        ReporterModel reporter = new ReporterModel("reporterTest", "Reporter Test");

        // Check that isEnabled works for all combinations of severities and thresholds
        List<TypedValue> severities = List.of(TRACE_SEVERITY, DEBUG_SEVERITY, INFO_SEVERITY, WARN_SEVERITY);
        for (TypedValue severityThreshold : severities) {
            reporter.setSeverityThreshold(severityThreshold);
            for (TypedValue severity : severities) {
                if (severity == severityThreshold) {
                    assertTrue(reporter.isEnabled(severity));
                } else {
                    assertFalse(reporter.isEnabled(severity));
                }
            }
        }

        // Check if an unknown severity is set
        TypedValue unknownSeverity = new TypedValue("unknownSeverity", TypedValue.SEVERITY);
        assertThrows(IllegalArgumentException.class, () -> reporter.setSeverityThreshold(unknownSeverity));
    }
}
