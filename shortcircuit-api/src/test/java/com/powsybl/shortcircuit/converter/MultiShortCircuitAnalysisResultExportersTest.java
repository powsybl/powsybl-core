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
public class MultiShortCircuitAnalysisResultExportersTest extends AbstractConverterTest {

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

    private static ShortCircuitAnalysisMultiResult createResult() {
        Fault fault = new BusFault("faultResultID", 0.0, 0.0, Fault.ConnectionType.SERIES);
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult = new FaultResult(fault, 1);
        faultResults.add(faultResult);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation);
        return new ShortCircuitAnalysisMultiResult(faultResults, limitViolations);
    }

    private static ShortCircuitAnalysisMultiResult createResultWithExtension() {
        Fault fault = new BusFault("faultResultID", 0.0, 0.0, Fault.ConnectionType.SERIES);
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult = new FaultResult(fault, 1);
        faultResult.addExtension(DummyFaultResultExtension.class, new DummyFaultResultExtension());
        faultResults.add(faultResult);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolation.addExtension(DummyLimitViolationExtension.class, new DummyLimitViolationExtension());
        limitViolations.add(limitViolation);
        ShortCircuitAnalysisMultiResult shortCircuitAnalysisResult =  new ShortCircuitAnalysisMultiResult(faultResults, limitViolations);
        shortCircuitAnalysisResult.addExtension(DummyShortCircuitAnalysisResultExtension.class, new DummyShortCircuitAnalysisResultExtension());
        return shortCircuitAnalysisResult;
    }

    private static ShortCircuitAnalysisMultiResult createWithFeederResults() {
        Fault fault = new BusFault("faultResultID", 0.0, 0.0, Fault.ConnectionType.SERIES);
        List<FaultResult> faultResults = new ArrayList<>();
        FeederResult feederResult = new FeederResult("connectableId", 1);
        FaultResult faultResult = new FaultResult(fault, 1, Collections.singletonList(feederResult), 1);
        faultResults.add(faultResult);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation);
        return new ShortCircuitAnalysisMultiResult(faultResults, limitViolations);
    }

    public void writeJson(ShortCircuitAnalysisMultiResult results, Path path) {
        ShortCircuitAnalysisResultExporters.export(results, path, "JSON", null);
    }

    @Test
    public void testWriteJson() throws IOException {
        ShortCircuitAnalysisMultiResult result = createResultWithExtension();
        writeTest(result, this::writeJson, AbstractConverterTest::compareTxt, "/shortcircuit-with-extensions-results.json");
    }

    @Test
    public void roundTripJson() throws IOException {
        ShortCircuitAnalysisMultiResult result = createResultWithExtension();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-with-extensions-results.json");
    }

    @Test
    public void testJsonWithFeederResult() throws IOException {
        ShortCircuitAnalysisMultiResult result = createWithFeederResults();
        writeTest(result, this::writeJson, AbstractConverterTest::compareTxt, "/shortcircuit-results-with-feeder-result.json");
    }

    @Test
    public void roundTripJsonWithFeederResult() throws IOException {
        ShortCircuitAnalysisMultiResult result = createWithFeederResults();
        roundTripTest(result, this::writeJson, ShortCircuitAnalysisResultDeserializer::read, "/shortcircuit-results-with-feeder-result.json");
    }

    public void writeCsv(ShortCircuitAnalysisMultiResult result, Path path) {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitAnalysisResultExporters.export(result, path, "CSV", network);
    }

    @Test
    public void testWriteCsv() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        ShortCircuitAnalysisMultiResult result = createResult(network);
        writeTest(result, this::writeCsv, AbstractConverterTest::compareTxt, "/shortcircuit-results.csv");
    }

    public ShortCircuitAnalysisMultiResult createResult(Network network) {
        Fault fault = new BusFault("VLGEN", 0.0, 0.0, Fault.ConnectionType.SERIES);
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult = new FaultResult(fault, 2500);
        faultResults.add(faultResult);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "VLGEN";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation);
        return new ShortCircuitAnalysisMultiResult(faultResults, limitViolations);
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

    static class DummyShortCircuitAnalysisResultExtension extends AbstractExtension<ShortCircuitAnalysisMultiResult> {

        @Override
        public String getName() {
            return "DummyShortCircuitAnalysisResultExtension";
        }
    }

}
