/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.shortcircuit.FaultResult;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    private static ShortCircuitAnalysisResult createResult() {
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult = new FaultResult("faultResultID", 1);
        faultResults.add(faultResult);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        float limitReduction = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation);
        return new ShortCircuitAnalysisResult(faultResults, limitViolations);
    }

    private static ShortCircuitAnalysisResult createResultWithExtension() {
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult = new FaultResult("faultResultID", 1);
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
        ShortCircuitAnalysisResult shortCircuitAnalysisResult =  new ShortCircuitAnalysisResult(faultResults, limitViolations);
        shortCircuitAnalysisResult.addExtension(DummyShortCircuitAnalysisResultExtension.class, new DummyShortCircuitAnalysisResultExtension());
        return shortCircuitAnalysisResult;
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
