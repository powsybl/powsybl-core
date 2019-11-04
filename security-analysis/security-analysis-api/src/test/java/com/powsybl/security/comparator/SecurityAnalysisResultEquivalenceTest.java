/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.io.output.NullWriter;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SecurityAnalysisResultEquivalenceTest {

    @Test
    public void equivalent() {
        SecurityAnalysisResultEquivalence resultEquivalence = new SecurityAnalysisResultEquivalence(0.1, NullWriter.NULL_WRITER);

        LimitViolation line1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.ONE);
        LimitViolation similarLine1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.09, Branch.Side.ONE);
        LimitViolation differentLine1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1101.0, Branch.Side.ONE);
        LimitViolation smallLine1Violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 950.09, Branch.Side.ONE);

        LimitViolation line1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.TWO);
        LimitViolation similarLine1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.09, Branch.Side.TWO);
        LimitViolation differentLine1Violation2 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1101.0, Branch.Side.TWO);

        LimitViolation line2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.ONE);
        LimitViolation similarLine2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.09, Branch.Side.ONE);
        LimitViolation smallLine2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 950.09, Branch.Side.ONE);
        LimitViolation line3Violation = new LimitViolation("NHV1_NHV2_3", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.ONE);
        LimitViolation similarLine3Violation = new LimitViolation("NHV1_NHV2_3", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.09, Branch.Side.ONE);

        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        Contingency contingency2 = Mockito.mock(Contingency.class);
        Mockito.when(contingency2.getId()).thenReturn("contingency2");
        Contingency contingency3 = Mockito.mock(Contingency.class);
        Mockito.when(contingency3.getId()).thenReturn("contingency3");

        // similar pre and post contingency results
        LimitViolationsResult preContingencyResult1 = new LimitViolationsResult(true, Arrays.asList(line1Violation1));
        PostContingencyResult postContingencyResult11 = new PostContingencyResult(contingency1, new LimitViolationsResult(true, Arrays.asList(line1Violation1, line1Violation2)));
        PostContingencyResult postContingencyResult12 = new PostContingencyResult(contingency2, new LimitViolationsResult(true, Arrays.asList(line1Violation1, line2Violation)));
        SecurityAnalysisResult result1 = new SecurityAnalysisResult(preContingencyResult1, Arrays.asList(postContingencyResult11, postContingencyResult12));

        LimitViolationsResult preContingencyResult2 = new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1));
        PostContingencyResult postContingencyResult21 = new PostContingencyResult(contingency1, new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1, similarLine1Violation2)));
        PostContingencyResult postContingencyResult22 = new PostContingencyResult(contingency2, new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1, similarLine2Violation)));
        SecurityAnalysisResult result2 = new SecurityAnalysisResult(preContingencyResult2, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertTrue(resultEquivalence.equivalent(result1, result2));

        // different pre contingency results, similar post contingency results
        preContingencyResult2 = new LimitViolationsResult(true, Arrays.asList(differentLine1Violation1));
        result2 = new SecurityAnalysisResult(preContingencyResult2, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results
        preContingencyResult2 = new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1));
        postContingencyResult21 = new PostContingencyResult(contingency1, new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1, differentLine1Violation2)));
        result2 = new SecurityAnalysisResult(preContingencyResult2, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies at the end of result2
        postContingencyResult21 = new PostContingencyResult(contingency1, new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1, similarLine1Violation2)));
        PostContingencyResult postContingencyResult23 = new PostContingencyResult(contingency3, new LimitViolationsResult(true, Arrays.asList(similarLine1Violation1, similarLine3Violation)));
        result2 = new SecurityAnalysisResult(preContingencyResult2, Arrays.asList(postContingencyResult22, postContingencyResult21, postContingencyResult23));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies in result2
        PostContingencyResult postContingencyResult13 = new PostContingencyResult(contingency3, new LimitViolationsResult(true, Arrays.asList(line1Violation1, line3Violation)));
        result1 = new SecurityAnalysisResult(preContingencyResult1, Arrays.asList(postContingencyResult13, postContingencyResult12));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies at the end of result1
        result1 = new SecurityAnalysisResult(preContingencyResult1, Arrays.asList(postContingencyResult13, postContingencyResult11, postContingencyResult12));
        result2 = new SecurityAnalysisResult(preContingencyResult2, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies in result1
        result2 = new SecurityAnalysisResult(preContingencyResult2, Arrays.asList(postContingencyResult23, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, similar post contingency results: more contingencies in result1, but small
        postContingencyResult12 = new PostContingencyResult(contingency2, new LimitViolationsResult(true, Arrays.asList(smallLine1Violation1, smallLine2Violation)));
        result1 = new SecurityAnalysisResult(preContingencyResult1, Arrays.asList(postContingencyResult13, postContingencyResult11, postContingencyResult12));

        assertTrue(resultEquivalence.equivalent(result1, result2));
    }

}
