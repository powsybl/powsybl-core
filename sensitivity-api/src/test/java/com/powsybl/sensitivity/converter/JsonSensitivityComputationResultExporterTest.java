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

import static org.junit.Assert.*;

public class JsonSensitivityComputationResultExporterTest {

    @Test
    public void export() throws IOException {
        JsonSensitivityComputationResultExporter exporter = new JsonSensitivityComputationResultExporter();

        byte[] inputBytes = IOUtils.toByteArray(getClass().getResourceAsStream("/resultsExport.json"));
        SensitivityComputationResults results = SensitivityComputationResultJsonSerializer.read(new InputStreamReader(new ByteArrayInputStream(inputBytes)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.export(results, new OutputStreamWriter(baos));
        byte[] outputBytes = baos.toByteArray();

        assertArrayEquals(inputBytes, outputBytes);
    }
}
