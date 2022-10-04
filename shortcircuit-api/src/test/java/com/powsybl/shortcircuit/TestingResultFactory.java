/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public final class TestingResultFactory {

    private TestingResultFactory() {
    }

    public static ShortCircuitAnalysisResult createResult() {
        List<FaultResult> faultResults = new ArrayList<>();
        FaultResult faultResult1 = createFaultResult("Fault_ID_1", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 2500, 2000);
        FaultResult faultResult2 = createFaultResult("Fault_ID_2", LimitViolationType.LOW_SHORT_CIRCUIT_CURRENT, 2501, 2001);
        Fault fault = new BranchFault("Fault_ID_3", "BranchId", 0.0, 0.0, 12.0);
        List<ShortCircuitBusResults> busResults = new ArrayList<>();
        busResults.add(new ShortCircuitBusResults("VLGEN", "busId", new FortescueValue(2004, 2005)));
        FaultResult faultResult3 = new FaultResult(fault, 1.0, Collections.emptyList(),
                Collections.emptyList(), new FortescueValue(2002, 2003), null, busResults, null);
        faultResults.add(faultResult1);
        faultResults.add(faultResult2);
        faultResults.add(faultResult3);
        return new ShortCircuitAnalysisResult(faultResults);
    }

    public static FaultResult createFaultResult(String faultId, LimitViolationType limitType, float limit, float value) {
        Fault fault = new BusFault(faultId, "BusId", 0.0, 0.0);
        List<LimitViolation> limitViolations = new ArrayList<>();
        float limitReduction = 1;
        LimitViolation limitViolation1 = new LimitViolation("VLGEN", limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation1);
        LimitViolation limitViolation2 = new LimitViolation("VLGEN", limitType, limit, limitReduction, value);
        limitViolations.add(limitViolation2);
        return new FaultResult(fault, 1.0, Collections.emptyList(), limitViolations, new FortescueValue(value));
    }

    public static ShortCircuitAnalysisResult createWithFeederResults() {
        Fault fault = new BusFault("id", "BusId", 0.0, 0.0);
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
        FaultResult faultResult = new FaultResult(fault, 0.1, Collections.singletonList(feederResult), limitViolations,
                new FortescueValue(1.0), new FortescueValue(2.0), Collections.emptyList(), Duration.ofSeconds(1));
        faultResults.add(faultResult);
        return new ShortCircuitAnalysisResult(faultResults);
    }
}
