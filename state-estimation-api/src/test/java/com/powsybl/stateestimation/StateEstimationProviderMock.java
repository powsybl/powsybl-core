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
 * A provider that returns fixed results and records the arguments it was called with, so that
 * the facade can be tested without an estimator.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
@AutoService(StateEstimationProvider.class)
public class StateEstimationProviderMock implements StateEstimationProvider {

    public static final String NAME = "StateEstimationMock";

    public static final String BUS_ID = "BUS";
    public static final String MEASUREMENT_ID = "M1";
    public static final String CRITICAL_MEASUREMENT_ID = "M2";
    public static final double ESTIMATED_V = 398.5;
    public static final double ESTIMATED_ANGLE = -2.75;

    private static Network lastNetwork;
    private static String lastWorkingVariantId;
    private static StateEstimationRunParameters lastRunParameters;
    private static StateEstimationResult lastEstimate;
    private static ObservabilityResult lastObservability;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<ObservabilityResult> analyseObservability(Network network, String workingVariantId,
                                                                       StateEstimationRunParameters runParameters) {
        lastNetwork = network;
        lastWorkingVariantId = workingVariantId;
        lastRunParameters = runParameters;
        return CompletableFuture.completedFuture(observability());
    }

    @Override
    public CompletableFuture<StateEstimationResult> estimate(Network network, String workingVariantId,
                                                             StateEstimationRunParameters runParameters) {
        lastNetwork = network;
        lastWorkingVariantId = workingVariantId;
        lastRunParameters = runParameters;
        return CompletableFuture.completedFuture(result());
    }

    @Override
    public CompletableFuture<BadDataResult> detectBadData(Network network, StateEstimationResult estimate,
                                                          ObservabilityResult observability,
                                                          StateEstimationRunParameters runParameters) {
        lastNetwork = network;
        lastEstimate = estimate;
        lastObservability = observability;
        lastRunParameters = runParameters;
        return CompletableFuture.completedFuture(badData());
    }

    /** Clears the recorded arguments between tests, since the provider instance is reused. */
    public static void forget() {
        lastNetwork = null;
        lastWorkingVariantId = null;
        lastRunParameters = null;
        lastEstimate = null;
        lastObservability = null;
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

    public static StateEstimationResult getLastEstimate() {
        return lastEstimate;
    }

    public static ObservabilityResult getLastObservability() {
        return lastObservability;
    }

    /** One component, one estimated bus, one measurement. */
    private static StateEstimationResult result() {
        StateEstimationResult.ComponentResult component = new StateEstimationResultImpl.ComponentResultImpl(
                0, 0, StateEstimationResult.ComponentResult.Status.ESTIMATED, "estimated",
                3, BUS_ID, 1.25, Map.of());

        StateEstimationResult.BusResult bus =
                new StateEstimationResultImpl.BusResultImpl(BUS_ID, ESTIMATED_V, ESTIMATED_ANGLE);

        StateEstimationResult.MeasurementResult measurement =
                new StateEstimationResultImpl.MeasurementResultImpl(MEASUREMENT_ID, 100.0, 99.5, 0.5, false);

        return new StateEstimationResultImpl(StateEstimationResult.Status.FULLY_ESTIMATED,
                Map.of("mock", "true"), "",
                List.of(component), List.of(bus), List.of(measurement));
    }

    /** One observable area containing the one bus, whose active power is redundant. */
    private static ObservabilityResult observability() {
        ObservabilityResult.Area area = new ObservabilityResultImpl.AreaImpl(
                0, ObservabilityResult.AreaStatus.OBSERVABLE, Set.of(BUS_ID));

        ObservabilityResult.Quality quality = new ObservabilityResultImpl.QualityImpl(
                ObservabilityResult.Quantity.ACTIVE_POWER, null, 0.5, true);

        ObservabilityResult.ElementObservability element =
                new ObservabilityResultImpl.ElementObservabilityImpl(BUS_ID, true, List.of(quality));

        return new ObservabilityResultImpl(List.of(area), List.of(element));
    }

    /** Nothing suspect, and one measurement that could not be tested. */
    private static BadDataResult badData() {
        return new BadDataResultImpl(BadDataResult.Status.NO_BAD_DATA, List.of(),
                Set.of(CRITICAL_MEASUREMENT_ID), Map.of("test", "chi2"));
    }
}
