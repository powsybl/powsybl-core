/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.shortcircuit.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitAnalysisResultExportersTest extends AbstractConverterTest {

    @Test
    public void testGetFormats() {
        assertEquals("[ASCII, CSV, JSON]", ShortCircuitAnalysisResultExporters.getFormats().toString());
    }

    @Test
    public void testGetExporter() {
        assertEquals("ASCII", ShortCircuitAnalysisResultExporters.getExporter("ASCII").getFormat());
        assertEquals("CSV", ShortCircuitAnalysisResultExporters.getExporter("CSV").getFormat());
        assertEquals("JSON", ShortCircuitAnalysisResultExporters.getExporter("JSON").getFormat());
    }

    @Test
    public void testComment() {
        assertEquals("Export a result in ASCII tables", ShortCircuitAnalysisResultExporters.getExporter("ASCII").getComment());
        assertEquals("Export a result in a CSV-like format", ShortCircuitAnalysisResultExporters.getExporter("CSV").getComment());
        assertEquals("Export a result in JSON format", ShortCircuitAnalysisResultExporters.getExporter("JSON").getComment());
    }

    private static ShortCircuitAnalysisResult createResult() {
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult1 = createFaultResult("ID_1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 2500, 2000);
        FaultResult faultResult2 = createFaultResult("ID_2", LimitViolationType.LOW_SHORT_CIRCUIT_CURRENT, 2501, 2001);
        Fault fault = new BranchFault("ID_3", 0.0, 0.0, 12.0);
        FaultResult faultResult3 = new FaultResult(fault,  new FortescueValues(2002, 2003));
        faultResults.add(faultResult1);
        faultResults.add(faultResult2);
        faultResults.add(faultResult3);
        return new ShortCircuitAnalysisResult(faultResults);
    }

    private static FaultResult createFaultResult(String faultId, LimitViolationType limitType, float limit, float value) {
        Fault fault = new BusFault(faultId, 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "VLGEN";
        float limitReduction = 1;
        LimitViolation limitViolation1 = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation1);
        LimitViolation limitViolation2 = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation2);
        return new FaultResult(fault, new FortescueValues(value), limitViolations);
    }

    private static ShortCircuitAnalysisResult createResultWithExtension() {
        Fault fault = new BusFault("id", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolation.addExtension(DummyLimitViolationExtension.class, new DummyLimitViolationExtension());
        limitViolations.add(limitViolation);
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult = new FaultResult(fault, new FortescueValues(1.0), limitViolations);
        faultResult.addExtension(DummyFaultResultExtension.class, new DummyFaultResultExtension());
        faultResults.add(faultResult);
        ShortCircuitAnalysisResult shortCircuitAnalysisResult =  new ShortCircuitAnalysisResult(faultResults);
        shortCircuitAnalysisResult.addExtension(DummyShortCircuitAnalysisResultExtension.class, new DummyShortCircuitAnalysisResultExtension());
        return shortCircuitAnalysisResult;
    }

    private static ShortCircuitAnalysisResult createWithFeederResults() {
        Fault fault = new BusFault("id", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation);
        List<FaultResult> faultResults = new ArrayList<>();
        FeederResult feederResult = new FeederResult("connectableId", 1);
        FaultResult faultResult = new FaultResult(fault, 0.1, Collections.singletonList(feederResult), limitViolations, new FortescueValues(1.0), new FortescueValues(2.0), 1);
        faultResults.add(faultResult);
        return new ShortCircuitAnalysisResult(faultResults);
    }

    public void writeJson(ShortCircuitAnalysisResult results, Path path) {
        ShortCircuitAnalysisResultExporters.export(results, path, "JSON", null);
    }

    @Test
    public void testWriteJson() throws IOException {
        ShortCircuitAnalysisResult result = createResultWithExtension();
        writeTest(result, this::writeJson, AbstractConverterTest::compareTxt, "/shortcircuit-with-extensions-results.json");
    }

    @Test
    public void roundTripJson() throws IOException {
        ShortCircuitAnalysisResult result = createResultWithExtension();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-with-extensions-results.json");
    }

    @Test
    public void testJsonWithFeederResult() throws IOException {
        ShortCircuitAnalysisResult result = createWithFeederResults();
        writeTest(result, this::writeJson, AbstractConverterTest::compareTxt, "/shortcircuit-results-with-feeder-result.json");
    }

    @Test
    public void roundTripJsonWithFeederResult() throws IOException {
        ShortCircuitAnalysisResult result = createWithFeederResults();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-results-with-feeder-result.json");
    }

    public void writeCsv(ShortCircuitAnalysisResult result, Path path) {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitAnalysisResultExporters.export(result, path, "CSV", network);
    }

    @Test
    public void testWriteCsv() throws IOException {
        ShortCircuitAnalysisResult result = createResult();
        writeTest(result, this::writeCsv, AbstractConverterTest::compareTxt, "/shortcircuit-results.csv");
    }

    static class DummyFaultResultExtension extends AbstractExtension<FaultResult> {

        @Override
        public String getName() {
            return "DummyFaultResultExtension";
        }
    }

    static class DummyLimitViolationExtension extends AbstractExtension<LimitViolation> {

        @Override
        public String getName() {
            return "DummyLimitViolationExtension";
        }
    }

    static class DummyShortCircuitAnalysisResultExtension extends AbstractExtension<ShortCircuitAnalysisResult> {

        @Override
        public String getName() {
            return "DummyShortCircuitAnalysisResultExtension";
        }
    }

}
