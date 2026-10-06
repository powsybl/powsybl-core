/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A provider that estimates nothing and records what it was asked to do, so the facade can be
 * tested without an estimator behind it. Doubling as the first consumer of the API, it is also
 * the simplest answer to how much an implementation has to write: three methods.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
@AutoService(StateEstimationProvider.class)
public class StateEstimationProviderMock implements StateEstimationProvider {

    public static final String NAME = "StateEstimationMock";

    public static final String BUS_ID = "BUS";
    public static final double ESTIMATED_V = 398.5;
    public static final double ESTIMATED_ANGLE = -2.75;

    private static Network lastNetwork;
    private static String lastWorkingVariantId;
    private static StateEstimationRunParameters lastRunParameters;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<StateEstimationResult> run(Network network, String workingVariantId,
                                                        StateEstimationRunParameters runParameters) {
        lastNetwork = network;
        lastWorkingVariantId = workingVariantId;
        lastRunParameters = runParameters;
        return CompletableFuture.completedFuture(result());
    }

    /** Discarded between tests, since the facade and {@code findAll} hand back different instances. */
    public static void forget() {
        lastNetwork = null;
        lastWorkingVariantId = null;
        lastRunParameters = null;
    }

    public static Network getLastNetwork() {
        return lastNetwork;
    }

    public static String getLastWorkingVariantId() {
        return lastWorkingVariantId;
    }

    public static StateEstimationRunParameters getLastRunParameters() {
        return lastRunParameters;
    }

    /** One estimated bus, one component, one measurement, one observable area. */
    private static StateEstimationResult result() {
        StateEstimationResult.ComponentResult component = new StateEstimationResultImpl.ComponentResultImpl(
                0, 0, StateEstimationResult.ComponentResult.Status.ESTIMATED, "estimated",
                3, BUS_ID, 1.25, Map.of());

        StateEstimationResult.BusResult bus =
                new StateEstimationResultImpl.BusResultImpl(BUS_ID, ESTIMATED_V, ESTIMATED_ANGLE);

        StateEstimationResult.MeasurementResult measurement =
                new StateEstimationResultImpl.MeasurementResultImpl("M1", 100.0, 99.5, 0.5, false);

        StateEstimationResult.ObservabilityResult.Area area = new StateEstimationResultImpl.AreaImpl(
                0, StateEstimationResult.ObservabilityResult.AreaStatus.OBSERVABLE, Set.of(BUS_ID));

        StateEstimationResult.ObservabilityResult.Quality quality = new StateEstimationResultImpl.QualityImpl(
                StateEstimationResult.ObservabilityResult.Quantity.ACTIVE_POWER, null, 0.5, true);

        StateEstimationResult.ObservabilityResult.ElementObservability element =
                new StateEstimationResultImpl.ElementObservabilityImpl(BUS_ID, true, List.of(quality));

        return new StateEstimationResultImpl(StateEstimationResult.Status.FULLY_ESTIMATED,
                Map.of("mock", "true"), "",
                List.of(component), List.of(bus), List.of(measurement),
                new StateEstimationResultImpl.ObservabilityResultImpl(List.of(area), List.of(element)));
    }
}
