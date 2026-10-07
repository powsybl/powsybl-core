/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class DynamicSimulation {

    private DynamicSimulation() {
    }

    public static class Runner implements Versionable {

        private final DynamicSimulationProvider provider;

        public Runner(DynamicSimulationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        /**
         * @deprecated use {@link #runAsync(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId,
                                                                   ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
            return provider.run(network, workingVariantId, dynamicModelsSupplier, new DynamicSimulationRunParameters()
                    .setEventModelsSupplier(eventModelsSupplier)
                    .setOutputVariablesSupplier(outputVariablesSupplier)
                    .setParameters(parameters)
                    .setComputationManager(computationManager)
                    .setReportNode(reportNode));
        }

        /**
         * @deprecated use {@link #runAsync(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId,
                                                                   ComputationManager computationManager, DynamicSimulationParameters parameters) {
            return provider.run(network, workingVariantId, dynamicModelsSupplier, new DynamicSimulationRunParameters()
                    .setEventModelsSupplier(eventModelsSupplier)
                    .setOutputVariablesSupplier(outputVariablesSupplier)
                    .setParameters(parameters)
                    .setComputationManager(computationManager));
        }

        /**
         * @deprecated use {@link #runAsync(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, String workingVariantId,
                                                                   DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId,
                LocalComputationManager.getDefault(), parameters);
        }

        /**
         * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier,
                network.getVariantManager().getWorkingVariantId(), parameters);
        }

        /**
         * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, OutputVariablesSupplier.empty(), parameters);
        }

        /**
         * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   OutputVariablesSupplier outputVariablesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, EventModelsSupplier.empty(), outputVariablesSupplier, parameters);
        }

        /**
         * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, OutputVariablesSupplier.empty(), parameters);
        }

        /**
         * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   EventModelsSupplier eventModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, DynamicSimulationParameters.load());
        }

        /**
         * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   OutputVariablesSupplier outputVariablesSupplier) {
            return runAsync(network, dynamicModelsSupplier, outputVariablesSupplier, DynamicSimulationParameters.load());
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), dynamicModelsSupplier, DynamicSimulationRunParameters.getDefault());
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                   DynamicSimulationRunParameters runParameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), dynamicModelsSupplier, runParameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, String workingVariantId,
                                                                   DynamicModelsSupplier dynamicModelsSupplier,
                                                                   DynamicSimulationRunParameters runParameters) {
            return provider.run(network, workingVariantId, dynamicModelsSupplier, runParameters);
        }

        /**
         * @deprecated use {@link #run(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                           OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager,
                                           DynamicSimulationParameters parameters, ReportNode reportNode) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId,
                computationManager, parameters, reportNode).join();
        }

        /**
         * @deprecated use {@link #run(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                           OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager,
                                           DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId,
                computationManager, parameters).join();
        }

        /**
         * @deprecated use {@link #run(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                           OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId, parameters).join();
        }

        /**
         * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                           OutputVariablesSupplier outputVariablesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, parameters).join();
        }

        /**
         * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                           DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, parameters).join();
        }

        /**
         * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                           OutputVariablesSupplier outputVariablesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, outputVariablesSupplier, parameters).join();
        }

        /**
         * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, parameters).join();
        }

        /**
         * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier).join();
        }

        /**
         * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
         */
        @Deprecated(since = "7.4.0", forRemoval = true)
        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, OutputVariablesSupplier outputVariablesSupplier) {
            return runAsync(network, dynamicModelsSupplier, outputVariablesSupplier).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                           DynamicSimulationRunParameters runParameters) {
            return runAsync(network, dynamicModelsSupplier, runParameters).join();
        }

        public DynamicSimulationResult run(Network network, String workingVariantId,
                                           DynamicModelsSupplier dynamicModelsSupplier,
                                           DynamicSimulationRunParameters runParameters) {
            return runAsync(network, workingVariantId, dynamicModelsSupplier, runParameters).join();
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

    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder.find(name,
                "dynamic-simulation", DynamicSimulationProvider.class,
                PlatformConfig.defaultConfig()));
    }

    public static Runner find() {
        return find(null);
    }

    /**
     * @deprecated use {@link #runAsync(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                                                      String workingVariantId, ComputationManager computationManager,
                                                                      DynamicSimulationParameters parameters, ReportNode reportNode) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId,
            computationManager, parameters, reportNode);
    }

    /**
     * @deprecated use {@link #runAsync(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                                               String workingVariantId, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId, parameters);
    }

    /**
     * @deprecated use {@link #runAsync(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                                               DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, parameters);
    }

    /**
     * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, parameters);
    }

    /**
     * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      OutputVariablesSupplier outputVariablesSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, outputVariablesSupplier, parameters);
    }

    /**
     * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, parameters);
    }

    /**
     * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      EventModelsSupplier eventModelsSupplier) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier);
    }

    /**
     * @deprecated use {@link #runAsync(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                      OutputVariablesSupplier outputVariablesSupplier) {
        return find().runAsync(network, dynamicModelsSupplier, outputVariablesSupplier);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
        return find().runAsync(network, dynamicModelsSupplier);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                               DynamicSimulationRunParameters runParameters) {
        return find().runAsync(network, dynamicModelsSupplier, runParameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, String workingVariantId,
                                                               DynamicModelsSupplier dynamicModelsSupplier,
                                                               DynamicSimulationRunParameters runParameters) {
        return find().runAsync(network, workingVariantId, dynamicModelsSupplier, runParameters);
    }

    /**
     * @deprecated use {@link #run(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                       String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId, computationManager, parameters, reportNode);
    }

    /**
     * @deprecated use {@link #run(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                       String workingVariantId, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, workingVariantId, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, String, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, OutputVariablesSupplier outputVariablesSupplier,
                                       DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, outputVariablesSupplier, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, OutputVariablesSupplier outputVariablesSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, outputVariablesSupplier, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier);
    }

    /**
     * @deprecated use {@link #run(Network, DynamicModelsSupplier, DynamicSimulationRunParameters)} instead
     */
    @Deprecated(since = "7.4.0", forRemoval = true)
    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, OutputVariablesSupplier outputVariablesSupplier) {
        return find().run(network, dynamicModelsSupplier, outputVariablesSupplier);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
        return find().run(network, dynamicModelsSupplier);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                       DynamicSimulationRunParameters runParameters) {
        return find().run(network, dynamicModelsSupplier, runParameters);
    }

    public static DynamicSimulationResult run(Network network, String workingVariantId,
                                       DynamicModelsSupplier dynamicModelsSupplier,
                                       DynamicSimulationRunParameters runParameters) {
        return find().run(network, workingVariantId, dynamicModelsSupplier, runParameters);
    }
}
