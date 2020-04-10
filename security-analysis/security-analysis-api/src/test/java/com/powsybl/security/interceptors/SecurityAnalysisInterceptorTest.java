/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class SecurityAnalysisInterceptorTest {

    @Test
    public void test() {
        assertEquals(Collections.singleton("SecurityAnalysisInterceptorMock"), SecurityAnalysisInterceptors.getExtensionNames());

        SecurityAnalysisInterceptor interceptor = SecurityAnalysisInterceptors.createInterceptor("SecurityAnalysisInterceptorMock");
        assertNotNull(interceptor);
        assertEquals(SecurityAnalysisInterceptorMock.class, interceptor.getClass());

        try {
            interceptor = SecurityAnalysisInterceptors.createInterceptor(null);
            fail();
        } catch (NullPointerException e) {
            // Nothing to do
        }

        try {
            interceptor = SecurityAnalysisInterceptors.createInterceptor("unknown-security-analysis-interceptor");
            fail();
        } catch (IllegalArgumentException e) {
            // Nothing to do
        }
    }

    @Test
    public void testDefaultNewApi() {
        RunningContext runningContext = mock(RunningContext.class);
        LimitViolationsResult preRes = mock(LimitViolationsResult.class);
        PostContingencyResult postRes = mock(PostContingencyResult.class);
        SecurityAnalysisResult allRes = mock(SecurityAnalysisResult.class);
        OldInterceptor oldInterceptor = new OldInterceptor(runningContext, preRes, postRes, allRes);
        oldInterceptor.onPreContingencyResult(preRes, runningContext);
        oldInterceptor.onPostContingencyResult(postRes, runningContext);
        oldInterceptor.onSecurityAnalysisResult(allRes, runningContext);

        for (int i = 0; i < 3; i++) {
            assertTrue(oldInterceptor.allMethodsCalled[i]);
        }
    }

    static class OldInterceptor implements SecurityAnalysisInterceptor {

        private final RunningContext runningContext;
        private final LimitViolationsResult preRes;
        private final PostContingencyResult postRes;
        private final SecurityAnalysisResult allRes;

        private boolean[] allMethodsCalled = new boolean[3];

        public OldInterceptor(RunningContext runningContext, LimitViolationsResult preRes, PostContingencyResult postRes, SecurityAnalysisResult allRes) {
            this.runningContext = runningContext;
            this.preRes = preRes;
            this.postRes = postRes;
            this.allRes = allRes;
        }

        @Override
        public void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult) {
            assertSame(runningContext, context);
            assertSame(preRes, preContingencyResult);
            allMethodsCalled[0] = true;
        }

        @Override
        public void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult) {
            assertSame(runningContext, context);
            assertSame(postRes, postContingencyResult);
            allMethodsCalled[1] = true;
        }

        @Override
        public void onSecurityAnalysisResult(RunningContext context, SecurityAnalysisResult result) {
            assertSame(runningContext, context);
            assertSame(allRes, result);
            allMethodsCalled[2] = true;
        }
    }

}
