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

        /**
         * Wraps the given implementation.
         *
         * @param provider the implementation to wrap
         */
        public Runner(StateEstimationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        /**
         * Which parts of the network its measurements determine, for the topology of the given variant.
         *
         * @param network the network
         * @param workingVariantId the identifier of the network variant to run against
         * @param runParameters the run parameters
         * @return a {@link CompletableFuture} on the observability result
         */
        public CompletableFuture<ObservabilityResult> analyseObservabilityAsync(Network network, String workingVariantId,
                                                                                 StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.analyseObservability(network, workingVariantId, runParameters);
        }

        /**
         * Which parts of the network its measurements determine, for the topology of the given variant.
         *
         * @param network the network
         * @param workingVariantId the identifier of the network variant to run against
         * @param runParameters the run parameters
         * @return which parts of the network its measurements determine
         */
        public ObservabilityResult analyseObservability(Network network, String workingVariantId,
                                                        StateEstimationRunParameters runParameters) {
            return analyseObservabilityAsync(network, workingVariantId, runParameters).join();
        }

        /**
         * Which parts of the network its measurements determine, for the topology of the working
         * variant, with default run parameters.
         *
         * @param network the network
         * @return which parts of the network its measurements determine
         */
        public ObservabilityResult analyseObservability(Network network) {
            return analyseObservability(network, network.getVariantManager().getWorkingVariantId(),
                    StateEstimationRunParameters.getDefault());
        }

        /**
         * The state of the given variant that best fits its measurements.
         *
         * @param network the network
         * @param workingVariantId the identifier of the network variant to run against
         * @param runParameters the run parameters
         * @return a {@link CompletableFuture} on the estimation result
         */
        public CompletableFuture<StateEstimationResult> estimateAsync(Network network, String workingVariantId,
                                                                      StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.estimate(network, workingVariantId, runParameters);
        }

        /**
         * The state of the given variant that best fits its measurements.
         *
         * @param network the network
         * @param workingVariantId the identifier of the network variant to run against
         * @param runParameters the run parameters
         * @return the estimated state
         */
        public StateEstimationResult estimate(Network network, String workingVariantId,
                                              StateEstimationRunParameters runParameters) {
            return estimateAsync(network, workingVariantId, runParameters).join();
        }

        /**
         * The state of the working variant that best fits its measurements, under the given settings.
         *
         * @param network the network
         * @param parameters the estimation settings
         * @return the estimated state
         */
        public StateEstimationResult estimate(Network network, StateEstimationParameters parameters) {
            return estimate(network, network.getVariantManager().getWorkingVariantId(),
                    new StateEstimationRunParameters().setParameters(parameters));
        }

        /**
         * The state of the working variant that best fits its measurements, with default run parameters.
         *
         * @param network the network
         * @return the estimated state
         */
        public StateEstimationResult estimate(Network network) {
            return estimate(network, network.getVariantManager().getWorkingVariantId(),
                    StateEstimationRunParameters.getDefault());
        }

        /**
         * Tests an estimate for measurements that disagree with it. The estimate is used as given and is not recomputed.
         *
         * @param network the network
         * @param estimate the estimate to test
         * @param observability the observability result, telling which measurements can be tested
         * @param runParameters the run parameters
         * @return a {@link CompletableFuture} on the bad data result
         */
        public CompletableFuture<BadDataResult> detectBadDataAsync(Network network, StateEstimationResult estimate,
                                                                   ObservabilityResult observability,
                                                                   StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(estimate, "StateEstimationResult should not be null");
            Objects.requireNonNull(observability, "ObservabilityResult should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.detectBadData(network, estimate, observability, runParameters);
        }

        /**
         * Tests an estimate for measurements that disagree with it. The estimate is used as given and is not recomputed.
         *
         * @param network the network
         * @param estimate the estimate to test
         * @param observability the observability result, telling which measurements can be tested
         * @param runParameters the run parameters
         * @return the outcome of the test
         */
        public BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                           ObservabilityResult observability,
                                           StateEstimationRunParameters runParameters) {
            return detectBadDataAsync(network, estimate, observability, runParameters).join();
        }

        /**
         * Tests an estimate for measurements that disagree with it. The estimate is used as given and is not recomputed.
         * Uses default run parameters.
         *
         * @param network the network
         * @param estimate the estimate to test
         * @param observability the observability result, telling which measurements can be tested
         * @return the outcome of the test
         */
        public BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                           ObservabilityResult observability) {
            return detectBadData(network, estimate, observability, StateEstimationRunParameters.getDefault());
        }

        /**
         * Writes a result back into the network, under the variant it was computed against.
         *
         * <p>Separate from {@link StateEstimationParameters#isWriteResultsToNetwork()}, which asks
         * the implementation to do this at the end of a run. Calling it directly lets a caller hold
         * several results, compare them, and write the one it settles on.</p>
         *
         * @param network the network to write into
         * @param workingVariantId the identifier of the variant the result was computed against
         * @param result the result to write
         */
        public void writeResultsToNetwork(Network network, String workingVariantId, StateEstimationResult result) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(result, "StateEstimationResult should not be null");
            provider.writeResultsToNetwork(network, workingVariantId, result);
        }

        /**
         * Writes a result back into the network, under its working variant.
         *
         * @param network the network to write into
         * @param result the result to write
         */
        public void writeResultsToNetwork(Network network, StateEstimationResult result) {
            Objects.requireNonNull(network, "Network should not be null");
            writeResultsToNetwork(network, network.getVariantManager().getWorkingVariantId(), result);
        }

        /**
         * Whether the implementation can honor the given parameters.
         *
         * @param runParameters the run parameters
         * @return true if the implementation can honor the parameters, false otherwise
         */
        public boolean checkParameters(StateEstimationRunParameters runParameters) {
            return provider.checkParameters(runParameters);
        }

        /**
         * Whether the implementation can honor the default parameters, reporting what it cannot to
         * {@code reportNode}.
         *
         * @param reportNode where the implementation writes its functional logs
         * @return true if the implementation can honor the default parameters, false otherwise
         */
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
     */
    public static Runner find() {
        return find(null);
    }

    /**
     * Runs {@link Runner#analyseObservability(Network)} on the default implementation.
     *
     * @param network the network
     * @return which parts of the network its measurements determine
     */
    public static ObservabilityResult analyseObservability(Network network) {
        return find().analyseObservability(network);
    }

    /**
     * Runs {@link Runner#analyseObservability(Network, String, StateEstimationRunParameters)} on the default implementation.
     *
     * @param network the network
     * @param workingVariantId the identifier of the network variant to run against
     * @param runParameters the run parameters
     * @return which parts of the network its measurements determine
     */
    public static ObservabilityResult analyseObservability(Network network, String workingVariantId,
                                                           StateEstimationRunParameters runParameters) {
        return find().analyseObservability(network, workingVariantId, runParameters);
    }

    /**
     * Runs {@link Runner#estimate(Network)} on the default implementation.
     *
     * @param network the network
     * @return the estimated state
     */
    public static StateEstimationResult estimate(Network network) {
        return find().estimate(network);
    }

    /**
     * Runs {@link Runner#estimate(Network, StateEstimationParameters)} on the default implementation.
     *
     * @param network the network
     * @param parameters the estimation settings
     * @return the estimated state
     */
    public static StateEstimationResult estimate(Network network, StateEstimationParameters parameters) {
        return find().estimate(network, parameters);
    }

    /**
     * Runs {@link Runner#estimate(Network, String, StateEstimationRunParameters)} on the default implementation.
     *
     * @param network the network
     * @param workingVariantId the identifier of the network variant to run against
     * @param runParameters the run parameters
     * @return the estimated state
     */
    public static StateEstimationResult estimate(Network network, String workingVariantId,
                                                 StateEstimationRunParameters runParameters) {
        return find().estimate(network, workingVariantId, runParameters);
    }

    /**
     * Runs {@link Runner#detectBadData(Network, StateEstimationResult, ObservabilityResult)} on the default implementation.
     *
     * @param network the network
     * @param estimate the estimate to test
     * @param observability the observability result, telling which measurements can be tested
     * @return the outcome of the test
     */
    public static BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                              ObservabilityResult observability) {
        return find().detectBadData(network, estimate, observability);
    }

    /**
     * Runs {@link Runner#detectBadData(Network, StateEstimationResult, ObservabilityResult, StateEstimationRunParameters)}
     * on the default implementation.
     *
     * @param network the network
     * @param estimate the estimate to test
     * @param observability the observability result, telling which measurements can be tested
     * @param runParameters the run parameters
     * @return the outcome of the test
     */
    public static BadDataResult detectBadData(Network network, StateEstimationResult estimate,
                                              ObservabilityResult observability,
                                              StateEstimationRunParameters runParameters) {
        return find().detectBadData(network, estimate, observability, runParameters);
    }

    /**
     * Runs {@link Runner#writeResultsToNetwork(Network, StateEstimationResult)} on the default implementation.
     *
     * @param network the network to write into
     * @param result the result to write
     */
    public static void writeResultsToNetwork(Network network, StateEstimationResult result) {
        find().writeResultsToNetwork(network, result);
    }

    /**
     * Runs {@link Runner#writeResultsToNetwork(Network, String, StateEstimationResult)} on the default implementation.
     *
     * @param network the network to write into
     * @param workingVariantId the identifier of the variant the result was computed against
     * @param result the result to write
     */
    public static void writeResultsToNetwork(Network network, String workingVariantId, StateEstimationResult result) {
        find().writeResultsToNetwork(network, workingVariantId, result);
    }

    /**
     * Runs {@link Runner#checkParameters(StateEstimationRunParameters)} on the default implementation.
     *
     * @param runParameters the run parameters
     * @return true if the implementation can honor the parameters, false otherwise
     */
    public static boolean checkParameters(StateEstimationRunParameters runParameters) {
        return find().checkParameters(runParameters);
    }

    /**
     * Runs {@link Runner#checkDefaultParameters(ReportNode)} on the default implementation.
     *
     * @param reportNode where the implementation writes its functional logs
     * @return true if the implementation can honor the default parameters, false otherwise
     */
    public static boolean checkDefaultParameters(ReportNode reportNode) {
        return find().checkDefaultParameters(reportNode);
    }
}
