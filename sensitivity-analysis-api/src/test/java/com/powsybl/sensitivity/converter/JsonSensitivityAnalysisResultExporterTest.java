/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.json.SensitivityAnalysisResultJsonSerializer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class JsonSensitivityAnalysisResultExporterTest extends AbstractConverterTest {

    private static SensitivityAnalysisResult create() throws IOException {
        byte[] inputBytes = IOUtils.toByteArray(JsonSensitivityAnalysisResultExporterTest.class.getResourceAsStream("/resultsExport.json"));
        return SensitivityAnalysisResultJsonSerializer.read(new InputStreamReader(new ByteArrayInputStream(inputBytes)));
    }

    private static SensitivityAnalysisResult read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            return SensitivityAnalysisResultJsonSerializer.read(new InputStreamReader(is));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(SensitivityAnalysisResult results, Path jsonFile) {
        Objects.requireNonNull(results);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            SensitivityAnalysisResultExporter exporter = new JsonSensitivityAnalysisResultExporter();
            exporter.export(results, new OutputStreamWriter(os));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeViaExporters(SensitivityAnalysisResult results, Path jsonFile) {
        Objects.requireNonNull(results);
        Objects.requireNonNull(jsonFile);
        SensitivityAnalysisResultExporters.export(results, jsonFile, "JSON");
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), JsonSensitivityAnalysisResultExporterTest::write, JsonSensitivityAnalysisResultExporterTest::read, "/resultsExport.json");
    }

    @Test
    public void roundTripViaExportersTest() throws IOException {
        roundTripTest(create(), JsonSensitivityAnalysisResultExporterTest::writeViaExporters, JsonSensitivityAnalysisResultExporterTest::read, "/resultsExport.json");
    }
}
