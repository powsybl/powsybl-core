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
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class SecurityAnalysisInterceptorMock extends DefaultSecurityAnalysisInterceptor {

    private int onPreContingencyResultCount = 0;

    private int onPostContingencyResultCount = 0;

    private int onSecurityAnalysisResultCount = 0;

    @Override
    public void onPreContingencyResult(LimitViolationsResult preContingencyResult, SecurityAnalysisResultContext context) {
        super.onPreContingencyResult(preContingencyResult, context);

        assertRunningContext(context);
        assertPreContingencyResult(preContingencyResult);
        onPreContingencyResultCount++;
    }

    @Override
    public void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
        super.onPostContingencyResult(postContingencyResult, context);

        assertRunningContext(context);
        assertPostContingencyResult(postContingencyResult);
        onPostContingencyResultCount++;
    }

    @Override
    public void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context) {
        super.onSecurityAnalysisResult(result, context);

        assertRunningContext(context);
        assertNotNull(result);
        assertPreContingencyResult(result.getPreContingencyResult());
        result.getPostContingencyResults().forEach(SecurityAnalysisInterceptorMock::assertPostContingencyResult);
        onSecurityAnalysisResultCount++;
    }

    public int getOnPreContingencyResultCount() {
        return onPreContingencyResultCount;
    }

    public int getOnPostContingencyResultCount() {
        return onPostContingencyResultCount;
    }

    public int getOnSecurityAnalysisResultCount() {
        return onSecurityAnalysisResultCount;
    }

    private static void assertRunningContext(SecurityAnalysisResultContext context) {
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
