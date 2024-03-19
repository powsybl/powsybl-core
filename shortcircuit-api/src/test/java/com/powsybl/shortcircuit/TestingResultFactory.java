/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.powsybl.shortcircuit.FaultResult.Status.SUCCESS;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class TestingResultFactory {

    private TestingResultFactory() {
    }

    public static ShortCircuitAnalysisResult createResult() {
        List<FaultResult> faultResults = new ArrayList<>();
        FortescueFaultResult faultResult1 = createFaultResult("Fault_ID_1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 2500, 2000);
        FortescueFaultResult faultResult2 = createFaultResult("Fault_ID_2", LimitViolationType.LOW_SHORT_CIRCUIT_CURRENT, 2501, 2001);
        Fault fault = new BranchFault("Fault_ID_3", "BranchId", 0.0, 0.0, 12.0);
        List<ShortCircuitBusResults> busResults = new ArrayList<>();
        busResults.add(new FortescueShortCircuitBusResults("VLGEN", "busId", 2800, new FortescueValue(2004, 2005), 70));
        FortescueFaultResult faultResult3 = new FortescueFaultResult(fault, 1.0, Collections.emptyList(),
                Collections.emptyList(), new FortescueValue(2002, 2003), null, busResults, null, FaultResult.Status.SUCCESS);
        faultResults.add(faultResult1);
        faultResults.add(faultResult2);
        faultResults.add(faultResult3);
        return new ShortCircuitAnalysisResult(faultResults);
    }

    public static FortescueFaultResult createFaultResult(String faultId, LimitViolationType limitType, float limit, float value) {
        Fault fault = new BusFault(faultId, "BusId", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        double limitReductionValue = 1;
        LimitViolation limitViolation1 = new LimitViolation("VLGEN", limitType, limit, limitReductionValue, value);
        limitViolations.add(limitViolation1);
        LimitViolation limitViolation2 = new LimitViolation("VLGEN", limitType, limit, limitReductionValue, value);
        limitViolations.add(limitViolation2);
        return new FortescueFaultResult(fault, 1.0, Collections.emptyList(), limitViolations, new FortescueValue(value), FortescueFaultResult.Status.SUCCESS);
    }

    public static ShortCircuitAnalysisResult createWithFeederResults() {
        Fault fault = new BusFault("id", "BusId", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "id";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        double limitReductionValue = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReductionValue, value);
        limitViolations.add(limitViolation);
        List<FaultResult> faultResults = new ArrayList<>();
        MagnitudeFeederResult feederResult = new MagnitudeFeederResult("connectableId", 1);
        MagnitudeFaultResult faultResult = new MagnitudeFaultResult(fault, 0.1, Collections.singletonList(feederResult), limitViolations,
                1.0, Collections.emptyList(), Duration.ofSeconds(1), FortescueFaultResult.Status.SUCCESS);
        faultResults.add(faultResult);
        return new ShortCircuitAnalysisResult(faultResults);
    }

    public static ShortCircuitAnalysisResult createResultWithExtension() {
        Fault fault = new BusFault("id", "busId", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "vlId";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        double limitReductionValue = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReductionValue, value);
        limitViolation.addExtension(DummyLimitViolationExtension.class, new DummyLimitViolationExtension());
        limitViolations.add(limitViolation);
        List<ShortCircuitBusResults> busResults = new ArrayList<>();
        busResults.add(new FortescueShortCircuitBusResults(subjectId, "busId", 4800, new FortescueValue(2004, 2005), 70));
        List<FaultResult> faultResults = new ArrayList<>();
        MagnitudeFaultResult faultResult = new MagnitudeFaultResult(fault, 1.0, Collections.emptyList(), limitViolations, 1.0, busResults, null, SUCCESS);
        faultResult.addExtension(DummyFaultResultExtension.class, new DummyFaultResultExtension());
        faultResults.add(faultResult);
        ShortCircuitAnalysisResult shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults);
        shortCircuitAnalysisResult.addExtension(DummyShortCircuitAnalysisResultExtension.class, new DummyShortCircuitAnalysisResultExtension());
        return shortCircuitAnalysisResult;
    }

    public static ShortCircuitAnalysisResult createResultWithTwoFaultResults() {
        Fault fault1 = new BusFault("id1", "busId", 0.0, 0.0);
        Fault fault2 = new BusFault("id2", "busId2", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "vlId";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        double limitReductionValue = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReductionValue, value);
        limitViolation.addExtension(DummyLimitViolationExtension.class, new DummyLimitViolationExtension());
        limitViolations.add(limitViolation);
        List<ShortCircuitBusResults> busResults = new ArrayList<>();
        busResults.add(new FortescueShortCircuitBusResults(subjectId, "busId", 400, new FortescueValue(2004, 2005), 50));
        List<FaultResult> faultResults = new ArrayList<>();
        FortescueFaultResult faultResult1 = new FortescueFaultResult(fault1, 1.0, Collections.emptyList(), limitViolations, new FortescueValue(1.0, 10), null, busResults, null, SUCCESS);
        faultResults.add(faultResult1);
        MagnitudeFaultResult faultResult2 = new MagnitudeFaultResult(fault2, 1.0, Collections.emptyList(), Collections.emptyList(), 10, null, SUCCESS);
        faultResults.add(faultResult2);
        return new ShortCircuitAnalysisResult(faultResults);
    }

    public static ShortCircuitAnalysisResult createMagnitudeResult() {
        Fault fault = new BusFault("id", "busId", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "vlId";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        double limitReductionValue = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReductionValue, value);
        limitViolation.addExtension(DummyLimitViolationExtension.class, new DummyLimitViolationExtension());
        limitViolations.add(limitViolation);
        List<ShortCircuitBusResults> busResults = new ArrayList<>();
        busResults.add(new MagnitudeShortCircuitBusResults(subjectId, "busId", 2000, 2004, 100));
        List<FeederResult> feederResults = new ArrayList<>();
        MagnitudeFeederResult feederResult = new MagnitudeFeederResult("id2", 1.0);
        feederResults.add(feederResult);
        List<FaultResult> faultResults = new ArrayList<>();
        MagnitudeFaultResult faultResult = new MagnitudeFaultResult(fault, 1.0, feederResults, limitViolations, 1.0, busResults, Duration.ofSeconds(1), SUCCESS);
        faultResult.addExtension(DummyFaultResultExtension.class, new DummyFaultResultExtension());
        faultResults.add(faultResult);
        ShortCircuitAnalysisResult shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults);
        shortCircuitAnalysisResult.addExtension(DummyShortCircuitAnalysisResultExtension.class, new DummyShortCircuitAnalysisResultExtension());
        return shortCircuitAnalysisResult;
    }

    public static ShortCircuitAnalysisResult createFortescueResult() {
        Fault fault = new BusFault("id", "busId", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        String subjectId = "vlId";
        LimitViolationType limitType = LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT;
        float limit = 2000;
        double limitReductionValue = 1;
        float value = 2500;
        LimitViolation limitViolation = new LimitViolation(subjectId, limitType, limit, limitReductionValue, value);
        limitViolation.addExtension(DummyLimitViolationExtension.class, new DummyLimitViolationExtension());
        limitViolations.add(limitViolation);
        List<ShortCircuitBusResults> busResults = new ArrayList<>();
        busResults.add(new FortescueShortCircuitBusResults(subjectId, "busId", 2000, new FortescueValue(2004, 200), 100));
        List<FeederResult> feederResults = new ArrayList<>();
        FortescueFeederResult feederResult = new FortescueFeederResult("id2", new FortescueValue(1.0, 10));
        feederResults.add(feederResult);
        List<FaultResult> faultResults = new ArrayList<>();
        FortescueFaultResult faultResult = new FortescueFaultResult(fault, 1.0, feederResults, limitViolations, new FortescueValue(1.0, 10), new FortescueValue(50, 50), busResults, Duration.ofSeconds(1), SUCCESS);
        faultResult.addExtension(DummyFaultResultExtension.class, new DummyFaultResultExtension());
        faultResults.add(faultResult);
        ShortCircuitAnalysisResult shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults);
        shortCircuitAnalysisResult.addExtension(DummyShortCircuitAnalysisResultExtension.class, new DummyShortCircuitAnalysisResultExtension());
        return shortCircuitAnalysisResult;
    }

    public static class DummyFaultResultExtension extends AbstractExtension<FaultResult> {

        @Override
        public String getName() {
            return "DummyFaultResultExtension";
        }
    }

    public static class DummyLimitViolationExtension extends AbstractExtension<LimitViolation> {

        @Override
        public String getName() {
            return "DummyLimitViolationExtension";
        }
    }

    public static class DummyShortCircuitAnalysisResultExtension extends AbstractExtension<ShortCircuitAnalysisResult> {

        @Override
        public String getName() {
            return "DummyShortCircuitAnalysisResultExtension";
        }
    }
}
