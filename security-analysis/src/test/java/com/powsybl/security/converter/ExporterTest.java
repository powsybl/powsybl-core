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
import com.powsybl.security.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ExporterTest extends AbstractConverterTest {

    private static final LimitViolationFilter NO_FILTER = new LimitViolationFilter();

    private SecurityAnalysisResult result;

    private static SecurityAnalysisResult create() {
        // Create a LimitViolation(CURRENT) to ensure backward compatibility works
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, "limit", 100f, 0.95f, 110f, Branch.Side.ONE);
        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, "20'", 100f, 1.0f, 110f, Branch.Side.TWO);
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

    @Before
    public void setUp() throws IOException {
        super.setUp();

        result = create();
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testExporters() throws IOException {
        writeTest(result, ExporterTest::writeAscii, AbstractConverterTest::compareTxt, "/SecurityAnalysisResult.txt");
        writeTest(result, ExporterTest::writeCsv, AbstractConverterTest::compareTxt, "/SecurityAnalysisResult.csv");
        writeTest(result, ExporterTest::writeJson, AbstractConverterTest::compareTxt, "/SecurityAnalysisResult.json");
    }

    private static void writeAscii(SecurityAnalysisResult result, Path path) {
        write(result, path, "ASCII");
    }

    private static void writeCsv(SecurityAnalysisResult result, Path path) {
        write(result, path, "CSV");
    }

    private static void writeJson(SecurityAnalysisResult result, Path path) {
        write(result, path, "JSON");
    }

    private static void write(SecurityAnalysisResult result, Path path, String format) {
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

            exporter.export(result, NO_FILTER, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
