/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.interceptors;

import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.shortcircuit.FaultResult;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitAnalysisInterceptorMock extends DefaultShortCircuitAnalysisInterceptor {

    private int onFaultResultCount = 0;

    private int onLimitViolationCount = 0;

    private int onShortCircuitResultCount = 0;

    @Override
    public void onFaultResult(Network network, FaultResult faultResult) {
        super.onFaultResult(network, faultResult);

        assertFaultResult(faultResult);
    }

    @Override
    public void onLimitViolation(Network network, LimitViolation limitViolation) {
        super.onLimitViolation(network, limitViolation);

        assertLimitViolation(limitViolation);
    }

    @Override
    public void onShortCircuitResult(Network network, ShortCircuitAnalysisResult shortCircuitAnalysisResult) {
        super.onShortCircuitResult(network, shortCircuitAnalysisResult);

        assertShortCircuitResult(shortCircuitAnalysisResult);
    }

    public int getOnFaultResultCount() {
        return onFaultResultCount;
    }

    public int getOnLimitViolationCount() {
        return onLimitViolationCount;
    }

    public int getOnShortCircuitResultCount() {
        return onShortCircuitResultCount;
    }

    private static void assertFaultResult(FaultResult faultResult) {
        assertNotNull(faultResult);
        assertNotNull(faultResult.getFeederResults());
        assertEquals(10, faultResult.getThreePhaseFaultCurrent(), 0);
        assertEquals(5, faultResult.getFeederResults().get(0).getFeederThreePhaseCurrent(), 0);
        assertEquals(5, faultResult.getFeederCurrent("GEN"), 0);
        assertEquals(Double.NaN, faultResult.getFeederCurrent("Unexpected"), 0);
    }

    private static void assertLimitViolation(LimitViolation limitViolation) {
        assertNotNull(limitViolation);
        assertEquals(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, limitViolation.getLimitType());
    }

    private static void assertShortCircuitResult(ShortCircuitAnalysisResult shortCircuitAnalysisResult) {
        assertNotNull(shortCircuitAnalysisResult);
        assertEquals(1, shortCircuitAnalysisResult.getFaultResults().size());
        assertEquals(1, shortCircuitAnalysisResult.getFaultResults().get(0).getLimitViolations().size());
    }
}
