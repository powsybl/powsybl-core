/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class SecurityAnalysisResultBuilderTest {

    private Contingency createContingency() {
        return new Contingency("contingency");
    }

    private List<LimitViolation> createPreContingencyViolations() {
        return Collections.singletonList(new LimitViolation("line1", LimitViolationType.CURRENT, "IST", Integer.MAX_VALUE, 0.0, 100f, 101, Branch.Side.ONE));
    }

    private List<LimitViolation> createPostContingencyViolations() {
        return Collections.singletonList(new LimitViolation("line2", LimitViolationType.CURRENT, "IST", Integer.MAX_VALUE, 0.0, 100f, 110, Branch.Side.ONE));
    }

    private void testLimitViolation(LimitViolationsResult result, boolean convergent, List<String> equipmentsId, List<String> actionsId) {
        assertEquals(convergent, result.isComputationOk());
        assertEquals(actionsId, result.getActionsTaken());
        if (convergent) {
            assertEquals(1, result.getLimitViolations().size());
            assertEquals(equipmentsId, result.getLimitViolations().stream().map(LimitViolation::getSubjectId).collect(Collectors.toList()));
        }
    }

    private void testSARBuilder(final boolean convergent) {
        SecurityAnalysisResultBuilder builder = new SecurityAnalysisResultBuilder();
        builder.addResultHandler(result -> {
            testLimitViolation(result.getPreContingencyResult(), convergent, Collections.singletonList("line1"), Collections.singletonList("pre-action"));

            List<PostContingencyResult> postContingencyResults = result.getPostContingencyResults();
            assertEquals(1, postContingencyResults.size());

            PostContingencyResult postContingencyResult = postContingencyResults.get(0);
            assertEquals("contingency", postContingencyResult.getContingency().getId());
            assertEquals(0, postContingencyResult.getContingency().getElements().size());

            LimitViolationsResult postContingencyLimitViolationsResult = postContingencyResult.getLimitViolationsResult();
            testLimitViolation(postContingencyLimitViolationsResult, convergent, Collections.singletonList("line2"), Arrays.asList("post-action1", "post-action2"));

        });

        LoadFlowActionSimulatorObserver observer = builder.createObserver();

        observer.beforePreContingencyAnalysis(null);
        observer.afterAction(null, "pre-action");
        RunningContext runningContext = new RunningContext(null, null);
        runningContext.setRound(0);
        if (convergent) {
            observer.loadFlowConverged(runningContext, createPreContingencyViolations());
        } else {
            observer.loadFlowDiverged(runningContext);
        }
        observer.afterPreContingencyAnalysis();

        Contingency contingency = createContingency();
        RunningContext runningContext1 = new RunningContext(null, contingency);
        runningContext1.setRound(0);
        observer.afterAction(runningContext1, "post-action1");
        observer.afterAction(runningContext1, "post-action2");
        if (convergent) {
            observer.loadFlowConverged(runningContext1, createPostContingencyViolations());
        } else {
            observer.loadFlowDiverged(runningContext1);
        }

        observer.afterPostContingencyAnalysis();
    }

    @Test
    public void testSARBuilder() {
        testSARBuilder(true);
        testSARBuilder(false);
    }

    @Test
    public void testFindBuilder() {
        LoadFlowActionSimulatorResultBuilder builder = LoadFlowActionSimulatorResultBuilder.find("security-analysis-result");
        assertNotNull(builder);
        assertEquals("security-analysis-result", builder.getName());
    }
}
