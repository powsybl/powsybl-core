package com.powsybl.shortcircuit.interceptors;

import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.shortcircuit.FaultResult;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertEquals(10, faultResult.getThreePhaseFaultCurrent(), 0);
    }

    private static void assertLimitViolation(LimitViolation limitViolation) {
        assertNotNull(limitViolation);
        assertEquals(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, limitViolation.getLimitType());
    }

    private static void assertShortCircuitResult(ShortCircuitAnalysisResult shortCircuitAnalysisResult) {
        assertNotNull(shortCircuitAnalysisResult);
        assertEquals(1, shortCircuitAnalysisResult.getFaultResults().size());
        assertEquals(1, shortCircuitAnalysisResult.getLimitViolations().size());
    }
}
