package com.powsybl.sensitivity.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.json.SensitivityComputationResultJsonSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class JsonSensitivityComputationResultExporterTest extends AbstractConverterTest {

    private static SensitivityComputationResults create() throws IOException {
        byte[] inputBytes = IOUtils.toByteArray(JsonSensitivityComputationResultExporterTest.class.getResourceAsStream("/resultsExport.json"));
        return SensitivityComputationResultJsonSerializer.read(new InputStreamReader(new ByteArrayInputStream(inputBytes)));
    }

    private static SensitivityComputationResults read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            return SensitivityComputationResultJsonSerializer.read(new InputStreamReader(is));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(SensitivityComputationResults results, Path jsonFile) {
        Objects.requireNonNull(results);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            SensitivityComputationResultExporter exporter = new JsonSensitivityComputationResultExporter();
            exporter.export(results, new OutputStreamWriter(os));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeViaExporters(SensitivityComputationResults results, Path jsonFile) {
        Objects.requireNonNull(results);
        Objects.requireNonNull(jsonFile);
        SensitivityComputationResultExporters.export(results, jsonFile, "JSON");
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), JsonSensitivityComputationResultExporterTest::write, JsonSensitivityComputationResultExporterTest::read, "/resultsExport.json");
    }

    @Test
    public void roundTripViaExportersTest() throws IOException {
        roundTripTest(create(), JsonSensitivityComputationResultExporterTest::writeViaExporters, JsonSensitivityComputationResultExporterTest::read, "/resultsExport.json");
    }
}
