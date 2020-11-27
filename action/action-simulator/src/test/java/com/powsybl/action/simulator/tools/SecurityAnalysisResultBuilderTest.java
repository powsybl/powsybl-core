/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.tools;

import com.powsybl.action.simulator.loadflow.RunningContext;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

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
        AbstractSecurityAnalysisResultBuilder builder = new AbstractSecurityAnalysisResultBuilder() {
            @Override
            public void onFinalStateResult(SecurityAnalysisResult result) {

                testLimitViolation(result.getPreContingencyResult(), convergent, Collections.singletonList("line1"), Collections.singletonList("pre-action"));

                List<PostContingencyResult> postContingencyResults = result.getPostContingencyResults();
                assertEquals(1, postContingencyResults.size());

                PostContingencyResult postContingencyResult = postContingencyResults.get(0);
                assertEquals("contingency", postContingencyResult.getContingency().getId());
                assertEquals(0, postContingencyResult.getContingency().getElements().size());

                LimitViolationsResult postContingencyLimitViolationsResult = postContingencyResult.getLimitViolationsResult();
                testLimitViolation(postContingencyLimitViolationsResult, convergent, Collections.singletonList("line2"), Arrays.asList("post-action1", "post-action2"));
            }
        };

        builder.beforePreContingencyAnalysis(null);
        builder.afterAction(null, "pre-action");
        RunningContext runningContext = new RunningContext(null, null);
        runningContext.setRound(0);
        if (convergent) {
            builder.loadFlowConverged(runningContext, createPreContingencyViolations());
        } else {
            builder.loadFlowDiverged(runningContext);
        }
        builder.afterPreContingencyAnalysis();

        Contingency contingency = createContingency();
        RunningContext runningContext1 = new RunningContext(null, contingency);
        runningContext1.setRound(0);
        builder.afterAction(runningContext1, "post-action1");
        builder.afterAction(runningContext1, "post-action2");
        if (convergent) {
            builder.loadFlowConverged(runningContext1, createPostContingencyViolations());
        } else {
            builder.loadFlowDiverged(runningContext1);
        }

        builder.afterPostContingencyAnalysis();
    }

    @Test
    public void testSARBuilder() {
        testSARBuilder(true);
        testSARBuilder(false);
    }
}
