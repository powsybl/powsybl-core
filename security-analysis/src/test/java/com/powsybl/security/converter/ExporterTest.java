/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.contingency.*;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ExporterTest extends AbstractConverterTest {

    private static final Network NETWORK = TestingNetworkFactory.createFromEurostag();

    private static SecurityAnalysisResult create() {
        // Create a LimitViolation(CURRENT) to ensure backward compatibility works
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, "limit", 100f, 0.95f, 110f, 110f * 0.225f, Branch.Side.ONE, Float.NaN, Float.NaN, 0);
        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, "20'", 100f, 1.0f, 110f, 110f * 0.225f, Branch.Side.TWO, Float.NaN, Float.NaN, Integer.MAX_VALUE);
        LimitViolation violation3 = new LimitViolation("GEN", LimitViolationType.HIGH_VOLTAGE, 100f, 0.9f, 110f);
        LimitViolation violation4 = new LimitViolation("GEN2", LimitViolationType.LOW_VOLTAGE, 100f, 0.7f, 115f);

        List<ContingencyElement> elements = Arrays.asList(
            new BranchContingency("NHV1_NHV2_2", "VLNHV1"),
            new BranchContingency("NHV1_NHV2_1"),
            new GeneratorContingency("GEN"),
            new BusbarSectionContingency("BBS1")
        );
        Contingency contingency = new ContingencyImpl("contingency", elements);

        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(violation1));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, true, Arrays.asList(violation2, violation3, violation4), Arrays.asList("action1", "action2"));

        return new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
    }

    @Test
    public void testExporters() throws IOException {
        SecurityAnalysisResult result = create();

        writeTest(result, ExporterTest::writeAscii, AbstractConverterTest::compareTxt, "/SecurityAnalysisResult.txt");
        writeTest(result, ExporterTest::writeCsv, AbstractConverterTest::compareTxt, "/SecurityAnalysisResult.csv");
        writeTest(result, ExporterTest::writeJson, AbstractConverterTest::compareTxt, "/SecurityAnalysisResult.json");
    }

    @Test
    public void roundTripJson() throws IOException {
        SecurityAnalysisResult result = create();

        roundTripTest(result, ExporterTest::writeJson, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");

        BiConsumer<SecurityAnalysisResult, Path> exporter = (res, path) -> { SecurityAnalysisResultExporters.export(res, NETWORK, path, "JSON"); };
        roundTripTest(result, exporter, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");
    }

    private static void writeAscii(SecurityAnalysisResult result, Path path) {
        write(result, NETWORK, path, "ASCII");
    }

    private static void writeCsv(SecurityAnalysisResult result, Path path) {
        write(result, NETWORK, path, "CSV");
    }

    private static void writeJson(SecurityAnalysisResult result, Path path) {
        write(result, NETWORK, path, "JSON");
    }

    private static void write(SecurityAnalysisResult result, Network network, Path path, String format) {
        try (OutputStream stream = Files.newOutputStream(path)) {
            Writer writer = new OutputStreamWriter(stream) {
                @Override
                public void close() throws IOException {
                    flush();
                }
            };

            SecurityAnalysisResultExporter exporter = SecurityAnalysisResultExporters.getExporter(format);
            if (exporter instanceof AbstractTableSecurityAnalysisResultExporter) {
                exporter = new TableSecurityAnalysisResultExporterAdapter((AbstractTableSecurityAnalysisResultExporter) exporter);
            }

            assertNotNull(exporter);
            assertEquals(format, exporter.getFormat());

            exporter.export(result, network, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
