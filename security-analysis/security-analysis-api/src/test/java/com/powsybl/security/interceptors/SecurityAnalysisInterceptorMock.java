/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.security.*;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class SecurityAnalysisInterceptorMock extends DefaultSecurityAnalysisInterceptor {

    private boolean onPrecontingencyResultCount = false;

    private boolean onPostContingencyResultCount = false;

    private boolean onSecurityAnalysisResultCount = false;

    @Override
    public void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult) {
        super.onPreContingencyResult(context, preContingencyResult);

        assertRunningContext(context);
        assertPreContingencyResult(preContingencyResult);
    }

    @Override
    public void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult) {
        super.onPostContingencyResult(context, postContingencyResult);

        assertRunningContext(context);
        assertPostContingencyResult(postContingencyResult);


    }

    @Override
    public void onSecurityAnalysisResult(RunningContext context, SecurityAnalysisResult result) {
        super.onSecurityAnalysisResult(context, result);

        assertRunningContext(context);
        assertNotNull(result);
        assertPreContingencyResult(result.getPreContingencyResult());
        result.getPostContingencyResults().forEach(SecurityAnalysisInterceptorMock::assertPostContingencyResult);
    }

    private static void assertRunningContext(RunningContext context) {
        assertNotNull(context);
        assertNotNull(context.getNetwork());
        assertEquals("sim1", context.getNetwork().getId());
        assertEquals("test", context.getNetwork().getSourceFormat());
    }

    private static void assertPreContingencyResult(LimitViolationsResult preContingencyResult) {
        assertNotNull(preContingencyResult);
        assertTrue(preContingencyResult.isComputationOk());
        assertEquals(0, preContingencyResult.getLimitViolations().size());
    }

    private static void assertPostContingencyResult(PostContingencyResult postContingencyResult) {
        assertNotNull(postContingencyResult);
        assertTrue(postContingencyResult.getLimitViolationsResult().isComputationOk());
        assertEquals(1, postContingencyResult.getLimitViolationsResult().getLimitViolations().size());
        LimitViolation violation = postContingencyResult.getLimitViolationsResult().getLimitViolations().get(0);
        assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
        assertEquals("NHV1_NHV2_1", violation.getSubjectId());
    }
}
