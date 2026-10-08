/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class StateEstimationTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
        StateEstimationProviderMock.forget();
    }

    @Test
    void findsTheDefaultProvider() {
        StateEstimation.Runner runner = StateEstimation.find();
        assertEquals(StateEstimationProviderMock.NAME, runner.getName());
        assertEquals("1.0", runner.getVersion());
    }

    @Test
    void findsAProviderByName() {
        assertEquals(StateEstimationProviderMock.NAME,
                StateEstimation.find(StateEstimationProviderMock.NAME).getName());
    }

    @Test
    void refusesAnUnknownProvider() {
        PowsyblException thrown = assertThrows(PowsyblException.class,
                () -> StateEstimation.find("NoSuchEstimator"));
        assertTrue(thrown.getMessage().contains("NoSuchEstimator"),
                "the message should name the implementation that was asked for: " + thrown.getMessage());
    }

    @Test
    void oneProviderServesAllThreeOperations() {
        StateEstimation.Runner runner = StateEstimation.find(StateEstimationProviderMock.NAME);

        ObservabilityResult observability = runner.analyseObservability(network);
        StateEstimationResult estimate = runner.estimate(network);
        BadDataResult badData = runner.detectBadData(network, estimate, observability);

        assertEquals(ObservabilityResult.AreaStatus.OBSERVABLE, observability.getAreas().get(0).getStatus());
        assertTrue(estimate.isFullyEstimated());
        assertEquals(BadDataResult.Status.NO_BAD_DATA, badData.getStatus());
    }

    @Test
    void estimatesAgainstTheWorkingVariantByDefault() {
        StateEstimation.estimate(network);
        assertEquals(network.getVariantManager().getWorkingVariantId(), StateEstimationProviderMock.getLastWorkingVariantId());
        assertSame(network, StateEstimationProviderMock.getLastNetwork());
    }

    @Test
    void passesTheNamedVariantThrough() {
        network.getVariantManager().cloneVariant(
                network.getVariantManager().getWorkingVariantId(), "snapshot");

        StateEstimation.estimate(network, "snapshot", StateEstimationRunParameters.getDefault());
        assertEquals("snapshot", StateEstimationProviderMock.getLastWorkingVariantId());

        StateEstimation.analyseObservability(network, "snapshot", StateEstimationRunParameters.getDefault());
        assertEquals("snapshot", StateEstimationProviderMock.getLastWorkingVariantId());
    }

    @Test
    void passesParametersThrough() {
        StateEstimationParameters parameters = new StateEstimationParameters()
                .setZeroInjectionMode(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT)
                .setResidualFlaggingThreshold(2.5);

        StateEstimation.estimate(network, parameters);

        StateEstimationParameters seen = StateEstimationProviderMock.getLastRunParameters().getStateEstimationParameters();
        assertSame(parameters, seen);
        assertEquals(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT, seen.getZeroInjectionMode());
        assertEquals(2.5, seen.getResidualFlaggingThreshold());
    }

    @Test
    void fillsInDefaultsWhenGivenNoParameters() {
        StateEstimation.estimate(network);

        StateEstimationRunParameters runParameters = StateEstimationProviderMock.getLastRunParameters();
        assertEquals(StateEstimationParameters.DEFAULT_ZERO_INJECTION_MODE,
                runParameters.getStateEstimationParameters().getZeroInjectionMode());
        assertEquals(StateEstimationParameters.DEFAULT_RESIDUAL_FLAGGING_THRESHOLD,
                runParameters.getStateEstimationParameters().getResidualFlaggingThreshold());
        assertEquals(StateEstimationParameters.BusInjectionPolicy.REQUIRE_ALL_METERED,
                runParameters.getStateEstimationParameters().getBusInjectionPolicy());
    }

    @Test
    void passesTheBusInjectionPolicyThrough() {
        StateEstimationParameters parameters = new StateEstimationParameters()
                .setBusInjectionPolicy(StateEstimationParameters.BusInjectionPolicy.SUM_METERED);

        StateEstimation.estimate(network, parameters);

        assertEquals(StateEstimationParameters.BusInjectionPolicy.SUM_METERED,
                StateEstimationProviderMock.getLastRunParameters().getStateEstimationParameters().getBusInjectionPolicy());
    }

    @Test
    void estimatesAsynchronously() throws InterruptedException, ExecutionException {
        StateEstimationResult result = StateEstimation.find()
                .estimateAsync(network, network.getVariantManager().getWorkingVariantId(),
                        StateEstimationRunParameters.getDefault())
                .get();
        assertTrue(result.isFullyEstimated());
    }

    @Test
    void returnsWhatTheProviderEstimated() {
        StateEstimationResult result = StateEstimation.estimate(network);

        assertEquals(StateEstimationResult.Status.FULLY_ESTIMATED, result.getStatus());
        assertTrue(result.isFullyEstimated());

        assertEquals(1, result.getComponentResults().size());
        StateEstimationResult.ComponentResult component = result.getComponentResults().get(0);
        assertEquals(StateEstimationResult.ComponentResult.Status.ESTIMATED, component.getStatus());
        assertEquals(3, component.getIterationCount());
        assertEquals(StateEstimationProviderMock.BUS_ID, component.getReferenceBusId());

        assertEquals(1, result.getBusResults().size());
        assertEquals(StateEstimationProviderMock.ESTIMATED_V, result.getBusResults().get(0).getV());
        assertEquals(StateEstimationProviderMock.ESTIMATED_ANGLE, result.getBusResults().get(0).getAngle());

        assertEquals(1, result.getMeasurementResults().size());
        assertEquals(0.5, result.getMeasurementResults().get(0).getResidual(), 1e-12);
    }

    @Test
    void reportsObservabilityInTheVocabularyOfTheIidmExtensions() {
        ObservabilityResult observability = StateEstimation.analyseObservability(network);

        assertEquals(1, observability.getAreas().size());
        assertEquals(ObservabilityResult.AreaStatus.OBSERVABLE, observability.getAreas().get(0).getStatus());
        assertTrue(observability.getArea(StateEstimationProviderMock.BUS_ID).isPresent());
        assertTrue(observability.getArea("NotEstimated").isEmpty());

        ObservabilityResult.ElementObservability element =
                observability.getElementObservability(StateEstimationProviderMock.BUS_ID).orElseThrow();
        assertTrue(element.isObservable());
        assertEquals(1, element.getQualities().size());
        assertTrue(element.getQualities().get(0).isRedundant().orElseThrow());
        assertTrue(element.getQualities().get(0).getSide().isEmpty());
    }

    @Test
    void detectBadDataWorksFromAnEstimateItDidNotCompute() {
        ObservabilityResult observability = StateEstimation.analyseObservability(network);
        StateEstimationResult estimate = StateEstimation.estimate(network);
        StateEstimationProviderMock.forget();

        BadDataResult badData = StateEstimation.detectBadData(network, estimate, observability);

        assertSame(estimate, StateEstimationProviderMock.getLastEstimate());
        assertSame(observability, StateEstimationProviderMock.getLastObservability());
        assertEquals(BadDataResult.Status.NO_BAD_DATA, badData.getStatus());
        assertTrue(badData.getSuspectMeasurements().isEmpty());
    }

    @Test
    void reportsTheMeasurementsNoTestCanClear() {
        ObservabilityResult observability = StateEstimation.analyseObservability(network);
        StateEstimationResult estimate = StateEstimation.estimate(network);

        BadDataResult badData = StateEstimation.detectBadData(network, estimate, observability);

        assertEquals(1, badData.getUncheckableMeasurementIds().size());
        assertTrue(badData.getUncheckableMeasurementIds()
                .contains(StateEstimationProviderMock.CRITICAL_MEASUREMENT_ID));
    }

    @Test
    void carriesAnImplementationsOwnResultsAsAnExtension() {
        StateEstimationResultImpl result = (StateEstimationResultImpl) StateEstimation.estimate(network);
        ChiSquareExtension statistic = new ChiSquareExtension(21.4);

        result.addExtension(ChiSquareExtension.class, statistic);

        assertSame(statistic, result.getExtension(ChiSquareExtension.class));
        assertEquals(1, result.getExtensions().size());
    }
}
