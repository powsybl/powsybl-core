/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

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

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
            return provider.run(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, computationManager, parameters, reportNode);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   ComputationManager computationManager, DynamicSimulationParameters parameters) {
            return provider.run(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, computationManager, parameters, ReportNode.NO_OP);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, LocalComputationManager.getDefault(), parameters, ReportNode.NO_OP);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, network.getVariantManager().getWorkingVariantId(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, CurvesSupplier.empty(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, EventModelsSupplier.empty(), curvesSupplier, parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, CurvesSupplier.empty(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, DynamicSimulationParameters.load());
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier) {
            return runAsync(network, dynamicModelsSupplier, curvesSupplier, DynamicSimulationParameters.load());
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier, DynamicSimulationParameters.load());
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager,
                                           DynamicSimulationParameters parameters, ReportNode reportNode) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, computationManager, parameters, reportNode).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager,
                                           DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, computationManager, parameters, ReportNode.NO_OP).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, parameters).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, curvesSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier) {
            return runAsync(network, dynamicModelsSupplier, curvesSupplier).join();
        }

        public DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
            return runAsync(network, dynamicModelsSupplier).join();
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

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier,
                                                               String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, computationManager, parameters, reportNode);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier,
                                                               String workingVariantId, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier,
                                                               DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, curvesSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier) {
        return find().runAsync(network, dynamicModelsSupplier, curvesSupplier);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
        return find().runAsync(network, dynamicModelsSupplier);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier,
                                       String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, computationManager, parameters, reportNode);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier,
                                       String workingVariantId, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, workingVariantId, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier,
                                       DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, curvesSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, curvesSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, dynamicModelsSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier, CurvesSupplier curvesSupplier) {
        return find().run(network, dynamicModelsSupplier, curvesSupplier);
    }

    public static DynamicSimulationResult run(Network network, DynamicModelsSupplier dynamicModelsSupplier) {
        return find().run(network, dynamicModelsSupplier);
    }
}
