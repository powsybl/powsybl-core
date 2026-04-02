package com.powsybl.mixed.security.analysis.criteria;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.mixed.security.analysis.parameters.MixedModeParametersExtension;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PostContingencyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisSwitchCriteriaTest {
    private MixedModeParametersExtension extension;
    private AnalysisSwitchCriteria criteria;
    private PostContingencyResult result;
    private Contingency contingency;

    @BeforeEach
    void setUp() {
        extension = new MixedModeParametersExtension();
        extension.setStaticSimulator("load-flow");
        extension.setDynamicSimulator("dynaflow");
        contingency = Mockito.mock(Contingency.class);
        when(contingency.getId()).thenReturn("contingency-1");
        criteria = new AnalysisSwitchCriteria(extension);
    }

    @Test
    void testNullExtensionThrows() {
        assertThrows(NullPointerException.class, () -> new AnalysisSwitchCriteria(null));
    }

    @Test
    void testNullResultThrows() {
        assertThrows(NullPointerException.class, () -> criteria.evaluate(null));
    }

    @Test
    void testNoCriteriaDefinedReturnsNoSwitch() {
        extension.setSwitchCriteria(null);
        result = createPostContingencyResult(true, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertFalse(decision.shouldSwitch());
        assertEquals("No criteria defined", decision.getReason());
    }

    @Test
    void testEmptyCriteriaReturnsNoSwitch() {
        extension.setSwitchCriteria(Collections.emptyList());
        result = createPostContingencyResult(true, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertFalse(decision.shouldSwitch());
        assertEquals("No criteria defined", decision.getReason());
    }

    @Test
    void testFailedCriteriaTriggersSwitch() {
        extension.setSwitchCriteria(Collections.singletonList("FAILED"));
        result = createPostContingencyResult(false, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertTrue(decision.shouldSwitch());
        assertTrue(decision.getReason().contains("FAILED"));
    }

    @Test
    void testFailedCriteriaNoSwitchWhenConverged() {
        extension.setSwitchCriteria(Collections.singletonList("FAILED"));
        result = createPostContingencyResult(true, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertFalse(decision.shouldSwitch());
        assertEquals("No criteria met", decision.getReason());
    }

    @Test
    void testLimitViolationsCriteriaTriggersSwitch() {
        extension.setSwitchCriteria(Collections.singletonList("LIMIT_VIOLATIONS"));
        result = createPostContingencyResult(true, 1);
        SwitchDecision decision = criteria.evaluate(result);
        assertTrue(decision.shouldSwitch());
        assertTrue(decision.getReason().contains("LIMIT_VIOLATIONS"));
    }

    @Test
    void testLimitViolationsCriteriaNoSwitchWithoutViolations() {
        extension.setSwitchCriteria(Collections.singletonList("LIMIT_VIOLATIONS"));
        result = createPostContingencyResult(true, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertFalse(decision.shouldSwitch());
        assertEquals("No criteria met", decision.getReason());
    }

    @Test
    void testMultipleCriteria() {
        extension.setSwitchCriteria(Arrays.asList("FAILED", "LIMIT_VIOLATIONS"));
        result = createPostContingencyResult(true, 1);
        SwitchDecision decision = criteria.evaluate(result);
        assertTrue(decision.shouldSwitch());
    }

    @Test
    void testUnknownCriteriaIgnored() {
        extension.setSwitchCriteria(Collections.singletonList("UNKNOWN_CRITERION"));
        result = createPostContingencyResult(true, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertFalse(decision.shouldSwitch());
        assertEquals("No criteria met", decision.getReason());
    }

    @Test
    void testSpsTriggerCriteriaNotImplemented() {
        extension.setSwitchCriteria(Collections.singletonList("SPS_TRIGGERED"));
        result = createPostContingencyResult(true, 0);
        SwitchDecision decision = criteria.evaluate(result);
        assertFalse(decision.shouldSwitch());
    }

    private PostContingencyResult createPostContingencyResult(boolean converged, int violationCount) {
        PostContingencyComputationStatus status = converged
            ? PostContingencyComputationStatus.CONVERGED
            : PostContingencyComputationStatus.FAILED;
        LimitViolationsResult limitViolations = Mockito.mock(LimitViolationsResult.class);
        when(limitViolations.getLimitViolations()).thenReturn(
                Collections.nCopies(violationCount, mock(LimitViolation.class))
        );
        return new PostContingencyResult(
            contingency,
            status,
            limitViolations,
            NetworkResult.empty(),
            ConnectivityResult.empty(),
            Double.NaN
        );
    }
}