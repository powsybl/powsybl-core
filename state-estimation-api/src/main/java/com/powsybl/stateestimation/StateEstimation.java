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
 * State estimation main API. Entry point for running an estimation, either against a named
 * implementation or against the default one.
 *
 * <p>A state estimation finds the network state that best explains a set of noisy and redundant
 * meter readings. It takes the network alone, because the readings travel with it in the IIDM
 * measurement extensions.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public final class StateEstimation {

    private StateEstimation() {
    }

    /**
     * Convenience on top of {@link StateEstimationProvider}: synchronous and asynchronous runs
     * with defaults filled in.
     */
    public static class Runner implements Versionable {

        private final StateEstimationProvider provider;

        public Runner(StateEstimationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<StateEstimationResult> runAsync(Network network, String workingVariantId, StateEstimationRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "WorkingVariantId should not be null");
            Objects.requireNonNull(runParameters, "StateEstimationRunParameters should not be null");
            return provider.run(network, workingVariantId, runParameters);
        }

        public CompletableFuture<StateEstimationResult> runAsync(Network network, StateEstimationRunParameters runParameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), runParameters);
        }

        public CompletableFuture<StateEstimationResult> runAsync(Network network, StateEstimationParameters parameters) {
            return runAsync(network, new StateEstimationRunParameters().setParameters(parameters));
        }

        public CompletableFuture<StateEstimationResult> runAsync(Network network) {
            return runAsync(network, StateEstimationRunParameters.getDefault());
        }

        public StateEstimationResult run(Network network, String workingVariantId, StateEstimationRunParameters runParameters) {
            return runAsync(network, workingVariantId, runParameters).join();
        }

        public StateEstimationResult run(Network network, StateEstimationRunParameters runParameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), runParameters);
        }

        public StateEstimationResult run(Network network, StateEstimationParameters parameters) {
            return run(network, new StateEstimationRunParameters().setParameters(parameters));
        }

        public StateEstimationResult run(Network network) {
            return run(network, StateEstimationRunParameters.getDefault());
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

    public static CompletableFuture<StateEstimationResult> runAsync(Network network, String workingVariantId, StateEstimationRunParameters runParameters) {
        return find().runAsync(network, workingVariantId, runParameters);
    }

    public static CompletableFuture<StateEstimationResult> runAsync(Network network, StateEstimationRunParameters runParameters) {
        return find().runAsync(network, runParameters);
    }

    public static CompletableFuture<StateEstimationResult> runAsync(Network network, StateEstimationParameters parameters) {
        return find().runAsync(network, parameters);
    }

    public static CompletableFuture<StateEstimationResult> runAsync(Network network) {
        return find().runAsync(network);
    }

    public static StateEstimationResult run(Network network, String workingVariantId, StateEstimationRunParameters runParameters) {
        return find().run(network, workingVariantId, runParameters);
    }

    public static StateEstimationResult run(Network network, StateEstimationRunParameters runParameters) {
        return find().run(network, runParameters);
    }

    public static StateEstimationResult run(Network network, StateEstimationParameters parameters) {
        return find().run(network, parameters);
    }

    public static StateEstimationResult run(Network network) {
        return find().run(network);
    }

    public static boolean checkParameters(StateEstimationRunParameters runParameters) {
        return find().checkParameters(runParameters);
    }

    public static boolean checkDefaultParameters(ReportNode reportNode) {
        return find().checkDefaultParameters(reportNode);
    }
}
