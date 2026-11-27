/**
 * Copyright (c) 2026, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.interceptors;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.results.PreContingencyResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 */
class CurrentLimitViolationInterceptorTest {

    private static final String POST_VARIANT_ID = "post";

    private final CurrentLimitViolationInterceptor interceptor = new CurrentLimitViolationInterceptor();

    private static Stream<Arguments> nonBranchSubjects() {
        return Stream.of(
            Arguments.of((Supplier<Network>) DcDetailedNetworkFactory::createVscSymmetricalMonopole, "dcLinePos", null),
            Arguments.of((Supplier<Network>) DcDetailedNetworkFactory::createVscSymmetricalMonopole, "dcLinePos", ThreeSides.ONE),
            Arguments.of((Supplier<Network>) ThreeWindingsTransformerNetworkFactory::create, "3WT", ThreeSides.THREE),
            Arguments.of((Supplier<Network>) BoundaryLineNetworkFactory::create, "BL", ThreeSides.ONE)
        );
    }

    @ParameterizedTest
    @MethodSource("nonBranchSubjects")
    void nonBranchSubjectsAreIgnored(Supplier<Network> networkSupplier, String subjectId, ThreeSides side) {
        // A CURRENT violation whose subject is not a Branch (DC line, three-winding transformer, boundary line)
        // must not make the interceptor throw, and must be left without any extension, before or after contingency.
        Network network = networkSupplier.get();

        LimitViolation preViolation = currentViolation(subjectId, side);
        assertDoesNotThrow(() -> interceptor.onPreContingencyResult(preResult(preViolation),
            new DefaultSecurityAnalysisResultContext(network)));
        assertTrue(preViolation.getExtensions().isEmpty());

        LimitViolation postViolation = currentViolation(subjectId, side);
        assertDoesNotThrow(() -> interceptor.onPostContingencyResult(postResult(postViolation),
            new RunningContext(network, VariantManagerConstants.INITIAL_VARIANT_ID)));
        assertTrue(postViolation.getExtensions().isEmpty());
    }

    @Test
    void branchViolationIsEnrichedAndWorkingVariantRestored() {
        // Nominal case: a CURRENT violation on a branch is enriched with the active power read in the right variant
        // (P differs between the initial and post-contingency variants), and the working variant is restored afterwards.
        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1().setP(100.);
        double initialCurrent = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1().getI();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, POST_VARIANT_ID);
        network.getVariantManager().setWorkingVariant(POST_VARIANT_ID);
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1().setP(150.);

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        LimitViolation preViolation = currentViolation(EurostagTutorialExample1Factory.NHV1_NHV2_1, ThreeSides.ONE);
        interceptor.onPreContingencyResult(preResult(preViolation), new DefaultSecurityAnalysisResultContext(network));
        ActivePowerExtension preExtension = preViolation.getExtension(ActivePowerExtension.class);
        assertNotNull(preExtension);
        assertEquals(100., preExtension.getPreContingencyValue());

        network.getVariantManager().setWorkingVariant(POST_VARIANT_ID);
        LimitViolation postViolation = currentViolation(EurostagTutorialExample1Factory.NHV1_NHV2_1, ThreeSides.ONE);
        interceptor.onPostContingencyResult(postResult(postViolation),
            new RunningContext(network, VariantManagerConstants.INITIAL_VARIANT_ID));
        ActivePowerExtension postExtension = postViolation.getExtension(ActivePowerExtension.class);
        assertNotNull(postExtension);
        assertEquals(100., postExtension.getPreContingencyValue());
        assertEquals(150., postExtension.getPostContingencyValue());
        CurrentExtension currentExtension = postViolation.getExtension(CurrentExtension.class);
        assertNotNull(currentExtension);
        assertEquals(initialCurrent, currentExtension.getPreContingencyValue());
        assertEquals(POST_VARIANT_ID, network.getVariantManager().getWorkingVariantId());
    }

    @Test
    void undefinedPreContingencyCurrentIsNotReported() {
        // Without load flow results in the initial variant, the current is undefined (NaN): the interceptor adds
        // no CurrentExtension instead of throwing, and still reports the active power.
        Network network = EurostagTutorialExample1Factory.create();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1().setP(100.);
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, POST_VARIANT_ID);
        network.getVariantManager().setWorkingVariant(POST_VARIANT_ID);
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1().setP(150.);

        LimitViolation violation = currentViolation(EurostagTutorialExample1Factory.NHV1_NHV2_1, ThreeSides.ONE);
        RunningContext context = new RunningContext(network, VariantManagerConstants.INITIAL_VARIANT_ID);
        assertDoesNotThrow(() -> interceptor.onPostContingencyResult(postResult(violation), context));
        assertNull(violation.getExtension(CurrentExtension.class));
        ActivePowerExtension activePowerExtension = violation.getExtension(ActivePowerExtension.class);
        assertNotNull(activePowerExtension);
        assertEquals(100., activePowerExtension.getPreContingencyValue());
        assertEquals(150., activePowerExtension.getPostContingencyValue());
        assertEquals(POST_VARIANT_ID, network.getVariantManager().getWorkingVariantId());
    }

    @Test
    void workingVariantRestoredWhenPostContingencyProcessingThrows() {
        // The working variant must be restored even when reading the initial variant fails: here the violation
        // has no side, so the interceptor throws while it has switched to the initial variant.
        Network network = EurostagTutorialExample1Factory.create();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, POST_VARIANT_ID);
        network.getVariantManager().setWorkingVariant(POST_VARIANT_ID);

        LimitViolation violation = currentViolation(EurostagTutorialExample1Factory.NHV1_NHV2_1, null);
        PostContingencyResult result = postResult(violation);
        RunningContext context = new RunningContext(network, VariantManagerConstants.INITIAL_VARIANT_ID);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> interceptor.onPostContingencyResult(result, context));
        // Pin the source of the exception: it must come from the missing side, read after switching to the initial variant
        assertEquals("Violation on 'NHV1_NHV2_1' has no side.", exception.getMessage());
        assertEquals(POST_VARIANT_ID, network.getVariantManager().getWorkingVariantId());
    }

    private static LimitViolation currentViolation(String subjectId, ThreeSides side) {
        return new LimitViolation(subjectId, null, "", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 100.0, 1.0, 110.0, side, null);
    }

    private static PreContingencyResult preResult(LimitViolation... violations) {
        return new PreContingencyResult(LoadFlowResult.ComponentResult.Status.CONVERGED,
            new LimitViolationsResult(List.of(violations)), NetworkResult.empty(), Double.NaN);
    }

    private static PostContingencyResult postResult(LimitViolation... violations) {
        return new PostContingencyResult(Mockito.mock(Contingency.class), PostContingencyComputationStatus.CONVERGED,
            new LimitViolationsResult(List.of(violations)), NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN,
            Collections.emptyList());
    }
}
