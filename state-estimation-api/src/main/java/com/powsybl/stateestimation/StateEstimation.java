/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * State estimation main API. Entry point for the three state estimation operations, either
 * against a named implementation or against the default one.
 *
 * <p>A state estimation finds the network state that best explains a set of noisy and redundant
 * meter readings. Every entry point takes the network alone, because the measurements are held
 * in the IIDM measurement extensions of its equipment.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public final class StateEstimation {

    private StateEstimation() {
    }

    /**
     * A state estimation runner is responsible for providing convenient methods on top of
     * {@link StateEstimationProvider}: synchronous and asynchronous forms of each operation, with
     * default parameters.
     */
    public static class Runner implements Versionable {

        private final StateEstimationProvider provider;

        public Runner(StateEstimationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<ObservabilityResult> analyseObservabilityAsync(Network network, String workingVariantId,
                                                                                 StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.analyseObservability(network, workingVariantId, runParameters);
        }

        public ObservabilityResult analyseObservability(Network network, String workingVariantId,
                                                        StateEstimationRunParameters runParameters) {
            return analyseObservabilityAsync(network, workingVariantId, runParameters).join();
        }

        public ObservabilityResult analyseObservability(Network network) {
            return analyseObservability(network, network.getVariantManager().getWorkingVariantId(),
                    StateEstimationRunParameters.getDefault());
        }

        public CompletableFuture<StateEstimationResult> estimateAsync(Network network, String workingVariantId,
                                                                      StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.estimate(network, workingVariantId, runParameters);
        }

        public StateEstimationResult estimate(Network network, String workingVariantId,
                                              StateEstimationRunParameters runParameters) {
            return estimateAsync(network, workingVariantId, runParameters).join();
        }

        public StateEstimationResult estimate(Network network, StateEstimationParameters parameters) {
            return estimate(network, network.getVariantManager().getWorkingVariantId(),
                    new StateEstimationRunParameters().setParameters(parameters));
        }

        public StateEstimationResult estimate(Network network) {
            return estimate(network, network.getVariantManager().getWorkingVariantId(),
                    StateEstimationRunParameters.getDefault());
        }

        public CompletableFuture<BadDataResult> detectBadDataAsync(Network network, StateEstimationResult estimate,
                                                                   ObservabilityResult observability,
                                                                   StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(estimate, "StateEstimationResult should not be null");
            Objects.requireNonNull(observability, "ObservabilityResult should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.detectBadData(network, estimate, observability, runParameters);
        }

        public BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                           ObservabilityResult observability,
                                           StateEstimationRunParameters runParameters) {
            return detectBadDataAsync(network, estimate, observability, runParameters).join();
        }

        public BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                           ObservabilityResult observability) {
            return detectBadData(network, estimate, observability, StateEstimationRunParameters.getDefault());
        }

        public boolean checkParameters(StateEstimationRunParameters runParameters) {
            return provider.checkParameters(runParameters);
        }

        public boolean checkDefaultParameters(ReportNode reportNode) {
            return checkParameters(StateEstimationRunParameters.getDefault().setReportNode(reportNode));
        }

        @Override
        public String getName() {
            return provider.getName();
        }

        @Override
        public String getVersion() {
            return provider.getVersion();
        }
    }

    /**
     * Get a runner for the state estimation implementation named {@code name}, or for the default
     * one when {@code name} is null.
     *
     * @param name name of the implementation, null to use the default one
     * @return a runner for that implementation
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
                .find(name, "state-estimation", StateEstimationProvider.class, PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for the default state estimation implementation.
     *
     * @throws PowsyblException if no default implementation can be found
     * @return a runner for the default implementation
     */
    public static Runner find() {
        return find(null);
    }

    public static ObservabilityResult analyseObservability(Network network) {
        return find().analyseObservability(network);
    }

    public static ObservabilityResult analyseObservability(Network network, String workingVariantId,
                                                           StateEstimationRunParameters runParameters) {
        return find().analyseObservability(network, workingVariantId, runParameters);
    }

    public static StateEstimationResult estimate(Network network) {
        return find().estimate(network);
    }

    public static StateEstimationResult estimate(Network network, StateEstimationParameters parameters) {
        return find().estimate(network, parameters);
    }

    public static StateEstimationResult estimate(Network network, String workingVariantId,
                                                 StateEstimationRunParameters runParameters) {
        return find().estimate(network, workingVariantId, runParameters);
    }

    public static BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                              ObservabilityResult observability) {
        return find().detectBadData(network, estimate, observability);
    }

    public static BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                              ObservabilityResult observability,
                                              StateEstimationRunParameters runParameters) {
        return find().detectBadData(network, estimate, observability, runParameters);
    }

    public static boolean checkParameters(StateEstimationRunParameters runParameters) {
        return find().checkParameters(runParameters);
    }

    public static boolean checkDefaultParameters(ReportNode reportNode) {
        return find().checkDefaultParameters(reportNode);
    }
}
