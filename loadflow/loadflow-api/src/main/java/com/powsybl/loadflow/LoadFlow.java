/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * LoadFlow main API. It is a utility class (so with only static methods) used as an entry point for running
 * a loadflow allowing to choose either a specific find implementation or just to rely on default one.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class LoadFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlow.class);

    /**
     * We suppose until 10% of gap no warning message
     * Connections between 380 kV and 400 KV (5%) no warning
     */
    private static final double NOMINAL_VOLTAGE_MISMATCH_WARNING_THRESHOLD = 0.10;

    private LoadFlow() {
    }

    /**
     * A loadflow runner is responsible for providing convenient methods on top of {@link LoadFlowProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static class Runner implements Versionable {

        private final LoadFlowProvider provider;

        public Runner(LoadFlowProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        /**
         * @deprecated use {@link #runAsync(Network, String, LoadFlowRunParameters)} instead
         */
        @Deprecated(since = "7.0.0", forRemoval = true)
        public CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
            return runAsync(network,
                workingStateId,
                new LoadFlowRunParameters()
                    .setComputationManager(computationManager)
                    .setParameters(parameters)
                    .setReportNode(reportNode));
        }

        /**
         * @deprecated use {@link #runAsync(Network, String, LoadFlowRunParameters)} instead
         */
        @Deprecated(since = "7.0.0", forRemoval = true)
        public CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
            return runAsync(network,
                workingStateId,
                new LoadFlowRunParameters()
                    .setComputationManager(computationManager)
                    .setParameters(parameters));
        }

        /**
         * @deprecated use {@link #runAsync(Network, LoadFlowRunParameters)} instead
         */
        @Deprecated(since = "7.0.0", forRemoval = true)
        public CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
            return runAsync(network,
                network.getVariantManager().getWorkingVariantId(),
                new LoadFlowRunParameters()
                    .setComputationManager(computationManager)
                    .setParameters(parameters));
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network,
                                                          String workingStateId,
                                                          LoadFlowRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            Objects.requireNonNull(runParameters, "LoadFlowRunParameters should not be null");
            logIfLineNominalVoltagesDifferent(network);
            return provider.run(network, workingStateId, runParameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowRunParameters runParameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), runParameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), new LoadFlowRunParameters().setParameters(parameters));
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network) {
            return runAsync(network, LoadFlowRunParameters.getDefault());
        }

        /**
         * @deprecated use {@link #run(Network, String, LoadFlowRunParameters)} instead
         */
        @Deprecated(since = "7.0.0", forRemoval = true)
        public LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
            return run(network,
                workingStateId,
                new LoadFlowRunParameters()
                    .setComputationManager(computationManager)
                    .setParameters(parameters)
                    .setReportNode(reportNode));
        }

        /**
         * @deprecated use {@link #run(Network, String, LoadFlowRunParameters)} instead
         */
        @Deprecated(since = "7.0.0", forRemoval = true)
        public LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
            return run(network,
                workingStateId,
                new LoadFlowRunParameters()
                    .setComputationManager(computationManager)
                    .setParameters(parameters));
        }

        /**
         * @deprecated use {@link #run(Network, LoadFlowRunParameters)} instead
         */
        @Deprecated(since = "7.0.0", forRemoval = true)
        public LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
            return run(network,
                network.getVariantManager().getWorkingVariantId(),
                new LoadFlowRunParameters()
                    .setComputationManager(computationManager)
                    .setParameters(parameters));
        }

        public LoadFlowResult run(Network network, String workingStateId, LoadFlowRunParameters runParameters) {
            return runAsync(network, workingStateId, runParameters).join();
        }

        public LoadFlowResult run(Network network, LoadFlowRunParameters runParameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), runParameters);
        }

        public LoadFlowResult run(Network network, LoadFlowParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), new LoadFlowRunParameters().setParameters(parameters));
        }

        public LoadFlowResult run(Network network) {
            return run(network, LoadFlowRunParameters.getDefault());
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
     * Get a runner for loadflow implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the load flow implementation, null if we want to use default one
     * @return a runner for loadflow implementation named {@code name}
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
                .find(name, "load-flow", LoadFlowProvider.class,
                PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default loadflow implementation.
     *
     * @throws PowsyblException in case we cannot find a default implementation
     * @return a runner for default loadflow implementation
     */
    public static Runner find() {
        return find(null);
    }

    /**
     * @deprecated use {@link #runAsync(Network, String, LoadFlowRunParameters)} instead
     */
    @Deprecated(since = "7.0.0", forRemoval = true)
    public static CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
        return find().runAsync(network, workingStateId, computationManager, parameters, reportNode);
    }

    /**
     * @deprecated use {@link #runAsync(Network, String, LoadFlowRunParameters)} instead
     */
    @Deprecated(since = "7.0.0", forRemoval = true)
    public static CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().runAsync(network, workingStateId, computationManager, parameters);
    }

    /**
     * @deprecated use {@link #runAsync(Network, LoadFlowRunParameters)} instead
     */
    @Deprecated(since = "7.0.0", forRemoval = true)
    public static CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().runAsync(network, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network,
                                                             String workingStateId,
                                                             LoadFlowRunParameters runParameters) {
        return find().runAsync(network, workingStateId, runParameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowRunParameters runParameters) {
        return find().runAsync(network, runParameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
        return find().runAsync(network, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network) {
        return find().runAsync(network);
    }


    /**
     * @deprecated use {@link #run(Network, String, LoadFlowRunParameters)} instead
     */
    @Deprecated(since = "7.0.0", forRemoval = true)
    public static LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters, ReportNode reportNode) {
        return find().run(network, workingStateId, computationManager, parameters, reportNode);
    }


    /**
     * @deprecated use {@link #run(Network, String, LoadFlowRunParameters)} instead
     */
    @Deprecated(since = "7.0.0", forRemoval = true)
    public static LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().run(network, workingStateId, computationManager, parameters);
    }


    /**
     * @deprecated use {@link #runAsync(Network, LoadFlowRunParameters)} instead
     */
    @Deprecated(since = "7.0.0", forRemoval = true)
    public static LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().run(network, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network,
                                     String workingStateId,
                                     LoadFlowRunParameters runParameters) {
        return find().run(network, workingStateId, runParameters);
    }

    public static LoadFlowResult run(Network network, LoadFlowRunParameters runParameters) {
        return find().run(network, runParameters);
    }

    public static LoadFlowResult run(Network network, LoadFlowParameters parameters) {
        return find().run(network, parameters);
    }

    public static LoadFlowResult run(Network network) {
        return find().run(network);
    }

    private static void logIfLineNominalVoltagesDifferent(Network network) {
        network.getLineStream().forEach(line -> {
            VoltageLevel vl1 = line.getTerminal1().getVoltageLevel();
            VoltageLevel vl2 = line.getTerminal2().getVoltageLevel();
            double nominalV1 = vl1.getNominalV();
            double nominalV2 = vl2.getNominalV();
            double maxNominalV = Math.max(nominalV1, nominalV2);
            double gap = Math.abs(nominalV1 - nominalV2) / maxNominalV;
            if (gap > NOMINAL_VOLTAGE_MISMATCH_WARNING_THRESHOLD) {
                double gapPercent = Math.round(gap * 1000.0) / 10.0;
                LOGGER.warn("Line '{}' connect voltage level '{}' ({} KV) and '{}' ({} KV): nominal voltage gap={}%", line.getId(), vl1.getId(), nominalV1, vl2.getId(), nominalV2, gapPercent);
            }
        });
    }
}
