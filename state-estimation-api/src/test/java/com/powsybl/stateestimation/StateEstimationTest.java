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
    void runsAgainstTheWorkingVariantByDefault() {
        StateEstimation.run(network);
        assertEquals(network.getVariantManager().getWorkingVariantId(), StateEstimationProviderMock.getLastWorkingVariantId());
        assertSame(network, StateEstimationProviderMock.getLastNetwork());
    }

    @Test
    void passesTheNamedVariantThrough() {
        network.getVariantManager().cloneVariant(
                network.getVariantManager().getWorkingVariantId(), "snapshot");

        StateEstimation.run(network, "snapshot", StateEstimationRunParameters.getDefault());

        assertEquals("snapshot", StateEstimationProviderMock.getLastWorkingVariantId());
    }

    @Test
    void passesParametersThrough() {
        StateEstimationParameters parameters = new StateEstimationParameters()
                .setZeroInjectionMode(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT)
                .setResidualFlaggingThreshold(2.5);

        StateEstimation.run(network, parameters);

        StateEstimationParameters seen = StateEstimationProviderMock.getLastRunParameters().getStateEstimationParameters();
        assertSame(parameters, seen);
        assertEquals(StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT, seen.getZeroInjectionMode());
        assertEquals(2.5, seen.getResidualFlaggingThreshold());
    }

    @Test
    void fillsInDefaultsWhenGivenNoParameters() {
        StateEstimation.run(network);

        StateEstimationRunParameters runParameters = StateEstimationProviderMock.getLastRunParameters();
        assertEquals(StateEstimationParameters.DEFAULT_ZERO_INJECTION_MODE,
                runParameters.getStateEstimationParameters().getZeroInjectionMode());
        assertEquals(StateEstimationParameters.DEFAULT_RESIDUAL_FLAGGING_THRESHOLD,
                runParameters.getStateEstimationParameters().getResidualFlaggingThreshold());
    }

    @Test
    void runsAsynchronously() throws InterruptedException, ExecutionException {
        StateEstimationResult result = StateEstimation.runAsync(network).get();
        assertTrue(result.isFullyEstimated());
    }

    @Test
    void returnsWhatTheProviderProduced() {
        StateEstimationResult result = StateEstimation.run(network);

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
        StateEstimationResult.ObservabilityResult observability =
                StateEstimation.run(network).getObservabilityResult();

        assertEquals(1, observability.getAreas().size());
        assertEquals(StateEstimationResult.ObservabilityResult.AreaStatus.OBSERVABLE,
                observability.getAreas().get(0).getStatus());
        assertTrue(observability.getArea(StateEstimationProviderMock.BUS_ID).isPresent());
        assertTrue(observability.getArea("NotEstimated").isEmpty());

        StateEstimationResult.ObservabilityResult.ElementObservability element =
                observability.getElementObservability(StateEstimationProviderMock.BUS_ID).orElseThrow();
        assertTrue(element.isObservable());
        assertEquals(1, element.getQualities().size());
        assertTrue(element.getQualities().get(0).isRedundant().orElseThrow());
        assertTrue(element.getQualities().get(0).getSide().isEmpty());
    }

    @Test
    void carriesAnImplementationsOwnResultsAsAnExtension() {
        StateEstimationResultImpl result = (StateEstimationResultImpl) StateEstimation.run(network);
        BadDataExtension badData = new BadDataExtension(21.4);

        result.addExtension(BadDataExtension.class, badData);

        assertSame(badData, result.getExtension(BadDataExtension.class));
        assertEquals(1, result.getExtensions().size());
    }
}
