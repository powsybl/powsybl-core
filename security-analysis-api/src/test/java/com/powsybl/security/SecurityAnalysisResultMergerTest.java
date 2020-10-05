/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class SecurityAnalysisResultMergerTest {

    private SecurityAnalysisResult result1;
    private SecurityAnalysisResult result2;
    private SecurityAnalysisResult failedResult;

    private LimitViolationsResult preContingencyResult;
    private LimitViolationsResult failedPreContingencyResult;
    private PostContingencyResult postContingencyResult;
    private PostContingencyResult postContingencyResult2;

    @Before
    public void setup() {
        // create pre-contingency results, just one violation on line1
        LimitViolation line1Violation = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000f, 0.95f, 1100, Branch.Side.ONE);
        preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(line1Violation), Collections.singletonList("action1"));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        LimitViolation line2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 900f, 0.95f, 950, Branch.Side.ONE);
        postContingencyResult = new PostContingencyResult(contingency1, true, Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2"));

        Contingency contingency2 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency2");
        postContingencyResult2 = new PostContingencyResult(contingency2, true, Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action3"));

        result1 = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
        result2 = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult2));

        failedPreContingencyResult = new LimitViolationsResult(false, Collections.emptyList());
        failedResult = new SecurityAnalysisResult(failedPreContingencyResult, Collections.emptyList());
    }

    @Test
    public void testMerge() {
        SecurityAnalysisResult[] results = new SecurityAnalysisResult[]{
            result1, result2
        };
        SecurityAnalysisResult mergedResult = SecurityAnalysisResultMerger.merge(results);
        assertEquals(preContingencyResult, mergedResult.getPreContingencyResult());
        assertEquals(Arrays.asList(postContingencyResult, postContingencyResult2), mergedResult.getPostContingencyResults());
    }

    @Test
    public void testFailedResultsMerge() {
        SecurityAnalysisResult[] results = new SecurityAnalysisResult[]{
            failedResult, result2
        };
        SecurityAnalysisResult mergedResult = SecurityAnalysisResultMerger.merge(results);
        assertEquals(SecurityAnalysisResultMerger.FAILED_SECURITY_ANALYSIS_RESULT, mergedResult);
    }
}
