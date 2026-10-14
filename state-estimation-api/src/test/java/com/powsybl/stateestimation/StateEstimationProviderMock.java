/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;
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
    public static final double MEASURED_VALUE = 100.0;
    public static final double ESTIMATED_VALUE = 99.5;
    public static final double NORMALIZED_RESIDUAL = 2.5;

    public static final String MODULE_CONFIG_NAME = "StateEstimationMock-state-estimation-parameters";
    public static final String TOLERANCE = "tolerance";
    public static final String MAX_ITERATIONS = "maxIterations";

    public static final List<Parameter> PARAMETERS = List.of(
            new Parameter(TOLERANCE, ParameterType.DOUBLE, "convergence tolerance", MockSpecificParameters.DEFAULT_TOLERANCE),
            new Parameter(MAX_ITERATIONS, ParameterType.INTEGER, "iteration cap", MockSpecificParameters.DEFAULT_MAX_ITERATIONS));

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
        runParameters.getReportNode().newReportNode()
                .withStrictMode(false)
                .withMessageTemplate("testStateEstimation")
                .withUntypedValue("variantId", workingVariantId)
                .add();
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
        return CompletableFuture.completedFuture(badData(estimate, runParameters));
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
                StateEstimationResult.ComponentResult.Status.ESTIMATED, "estimated", 3, BUS_ID, 1.25);

        StateEstimationResult.BusResult bus =
                new StateEstimationResultImpl.BusResultImpl(BUS_ID, ESTIMATED_V, ESTIMATED_ANGLE);

        StateEstimationResult.MeasurementResult measurement = new StateEstimationResultImpl.MeasurementResultImpl(
                MEASUREMENT_ID, MEASURED_VALUE, ESTIMATED_VALUE, NORMALIZED_RESIDUAL, false);

        return new StateEstimationResultImpl(StateEstimationResult.Status.FULLY_ESTIMATED,
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

        return new ObservabilityResultImpl(ObservabilityResult.Status.COMPLETED, "",
                List.of(area), List.of(element));
    }

    /**
     * Suspects whatever measurement of the estimate sits further from it than the configured
     * threshold, and reports one measurement nothing else determines as untestable.
     */
    private static BadDataResult badData(StateEstimationResult estimate, StateEstimationRunParameters runParameters) {
        double threshold = runParameters.getStateEstimationParameters().getResidualFlaggingThreshold();

        List<BadDataResult.SuspectMeasurement> suspects = estimate.getMeasurementResults().stream()
                .filter(measurement -> Math.abs(measurement.getNormalizedResidual()) > threshold)
                .map(measurement -> (BadDataResult.SuspectMeasurement) new BadDataResultImpl.SuspectMeasurementImpl(
                        measurement.getMeasurementId(), measurement.getNormalizedResidual()))
                .toList();

        BadDataResult.Status status = suspects.isEmpty()
                ? BadDataResult.Status.NO_BAD_DATA
                : BadDataResult.Status.BAD_DATA_DETECTED;

        return new BadDataResultImpl(status, "", suspects, Set.of(CRITICAL_MEASUREMENT_ID));
    }

    @Override
    public Optional<Extension<StateEstimationParameters>> loadSpecificParameters(PlatformConfig config) {
        MockSpecificParameters parameters = new MockSpecificParameters();
        updateSpecificParameters(parameters, config);
        return Optional.of(parameters);
    }

    @Override
    public void updateSpecificParameters(Extension<StateEstimationParameters> extension, PlatformConfig config) {
        MockSpecificParameters parameters = (MockSpecificParameters) extension;
        getModuleConfig(config).ifPresent(moduleConfig -> {
            moduleConfig.getOptionalDoubleProperty(TOLERANCE).ifPresent(parameters::setTolerance);
            moduleConfig.getOptionalIntProperty(MAX_ITERATIONS).ifPresent(parameters::setMaxIterations);
        });
    }

    @Override
    public List<Parameter> getRawSpecificParameters() {
        return PARAMETERS;
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(PlatformConfig config) {
        return config.getOptionalModuleConfig(MODULE_CONFIG_NAME);
    }

    /**
     * Refuses the two settings this stand-in has no way of honoring, writing what it refused to the
     * report node.
     */
    @Override
    public boolean checkParameters(StateEstimationRunParameters runParameters) {
        StateEstimationParameters parameters = runParameters.getStateEstimationParameters();

        if (parameters.getZeroInjectionMode() == StateEstimationParameters.ZeroInjectionMode.EQUALITY_CONSTRAINT) {
            refuse(runParameters, "zeroInjectionMode", parameters.getZeroInjectionMode().toString());
            return false;
        }
        if (parameters.getComponentMode() == StateEstimationParameters.ComponentMode.ALL) {
            refuse(runParameters, "componentMode", parameters.getComponentMode().toString());
            return false;
        }
        return true;
    }

    private static void refuse(StateEstimationRunParameters runParameters, String name, String value) {
        runParameters.getReportNode().newReportNode()
                .withStrictMode(false)
                .withMessageTemplate("testParameterNotSupported")
                .withUntypedValue("parameterName", name)
                .withUntypedValue("parameterValue", value)
                .add();
    }
}
