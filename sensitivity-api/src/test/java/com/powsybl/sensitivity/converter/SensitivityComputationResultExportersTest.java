package com.powsybl.sensitivity.converter;

import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.json.SensitivityComputationResultJsonSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    @Test
    public void testExportWithFormat() throws IOException {

        byte[] inputBytes = IOUtils.toByteArray(getClass().getResourceAsStream("/resultsExport.json"));
        SensitivityComputationResults results = SensitivityComputationResultJsonSerializer.read(new InputStreamReader(new ByteArrayInputStream(inputBytes)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SensitivityComputationResultExporters.export(results, new OutputStreamWriter(baos), "JSON");
        byte[] outputBytes = baos.toByteArray();
        assertArrayEquals(inputBytes, outputBytes);
    }
}
