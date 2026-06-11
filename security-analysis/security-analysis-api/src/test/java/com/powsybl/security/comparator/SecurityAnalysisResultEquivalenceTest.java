/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.NetworkResult;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.results.PostContingencyResult;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class SecurityAnalysisResultEquivalenceTest {

    @Test
    void equivalent() {
        SecurityAnalysisResultEquivalence resultEquivalence = new SecurityAnalysisResultEquivalence(0.1, NullWriter.INSTANCE);

        LimitViolation line1Violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100)
            .side1()
            .build();
        LimitViolation similarLine1Violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100.09)
            .side(TwoSides.ONE)
            .build();
        LimitViolation differentLine1Violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1101)
            .side(TwoSides.ONE)
            .build();
        LimitViolation smallLine1Violation1 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(950.09)
            .side1()
            .build();

        LimitViolation line1Violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100)
            .side2()
            .build();
        LimitViolation similarLine1Violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100.09)
            .side(TwoSides.TWO)
            .build();
        LimitViolation differentLine1Violation2 = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1101)
            .side(TwoSides.TWO)
            .build();

        LimitViolation line2Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100)
            .side(TwoSides.ONE)
            .build();
        LimitViolation similarLine2Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100.09)
            .side1()
            .build();
        LimitViolation smallLine2Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(950.09)
            .side1()
            .build();
        LimitViolation line3Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_3")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100)
            .side1()
            .build();
        LimitViolation similarLine3Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_3")
            .type(LimitViolationType.CURRENT)
            .limit(1000)
            .reduction(0.95)
            .value(1100.09)
            .side1()
            .build();

        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        Contingency contingency2 = Mockito.mock(Contingency.class);
        Mockito.when(contingency2.getId()).thenReturn("contingency2");
        Contingency contingency3 = Mockito.mock(Contingency.class);
        Mockito.when(contingency3.getId()).thenReturn("contingency3");

        // similar pre and post contingency results
        LimitViolationsResult preContingencyResult1 = new LimitViolationsResult(Arrays.asList(line1Violation1));
        PostContingencyResult postContingencyResult11 = new PostContingencyResult(contingency1, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(line1Violation1, line1Violation2)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        PostContingencyResult postContingencyResult12 = new PostContingencyResult(contingency2, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(line1Violation1, line2Violation)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        SecurityAnalysisResult result1 = new SecurityAnalysisResult(preContingencyResult1, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult11, postContingencyResult12));

        LimitViolationsResult preContingencyResult2 = new LimitViolationsResult(Arrays.asList(similarLine1Violation1));
        PostContingencyResult postContingencyResult21 = new PostContingencyResult(contingency1, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(similarLine1Violation1, similarLine1Violation2)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        PostContingencyResult postContingencyResult22 = new PostContingencyResult(contingency2, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(similarLine1Violation1, similarLine2Violation)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        SecurityAnalysisResult result2 = new SecurityAnalysisResult(preContingencyResult2, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertTrue(resultEquivalence.equivalent(result1, result2));

        // different pre contingency results, similar post contingency results
        preContingencyResult2 = new LimitViolationsResult(Arrays.asList(differentLine1Violation1));
        result2 = new SecurityAnalysisResult(preContingencyResult2, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results
        preContingencyResult2 = new LimitViolationsResult(Arrays.asList(similarLine1Violation1));
        postContingencyResult21 = new PostContingencyResult(contingency1, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(similarLine1Violation1, differentLine1Violation2)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        result2 = new SecurityAnalysisResult(preContingencyResult2, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies at the end of result2
        postContingencyResult21 = new PostContingencyResult(contingency1, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(similarLine1Violation1, similarLine1Violation2)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        PostContingencyResult postContingencyResult23 = new PostContingencyResult(contingency3, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(similarLine1Violation1, similarLine3Violation)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        result2 = new SecurityAnalysisResult(preContingencyResult2, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult22, postContingencyResult21, postContingencyResult23));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies in result2
        PostContingencyResult postContingencyResult13 = new PostContingencyResult(contingency3, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(line1Violation1, line3Violation)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        result1 = new SecurityAnalysisResult(preContingencyResult1, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult13, postContingencyResult12));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies at the end of result1
        result1 = new SecurityAnalysisResult(preContingencyResult1, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult13, postContingencyResult11, postContingencyResult12));
        result2 = new SecurityAnalysisResult(preContingencyResult2, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult22, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, different post contingency results: more contingencies in result1
        result2 = new SecurityAnalysisResult(preContingencyResult2, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult23, postContingencyResult21));

        assertFalse(resultEquivalence.equivalent(result1, result2));

        // similar pre contingency results, similar post contingency results: more contingencies in result1, but small
        postContingencyResult12 = new PostContingencyResult(contingency2, PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(Arrays.asList(smallLine1Violation1, smallLine2Violation)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN);
        result1 = new SecurityAnalysisResult(preContingencyResult1, LoadFlowResult.ComponentResult.Status.CONVERGED, Arrays.asList(postContingencyResult13, postContingencyResult11, postContingencyResult12));

        assertTrue(resultEquivalence.equivalent(result1, result2));
    }

}
