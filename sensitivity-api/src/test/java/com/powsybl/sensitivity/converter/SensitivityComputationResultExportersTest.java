package com.powsybl.sensitivity.converter;

import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

public class SensitivityComputationResultExportersTest {

    @Test
    public void testExporterForFormat() {
        SensitivityComputationResultExporter exporter = SensitivityComputationResultExporters.getExporter("JSON");
        assertEquals(JsonSensitivityComputationResultExporter.class, exporter.getClass());
    }

    @Test
    public void testExporterForUnavailableFormat() {
        SensitivityComputationResultExporter exporter = SensitivityComputationResultExporters.getExporter("Invalid");
        assertTrue(Objects.isNull(exporter));
    }
}
