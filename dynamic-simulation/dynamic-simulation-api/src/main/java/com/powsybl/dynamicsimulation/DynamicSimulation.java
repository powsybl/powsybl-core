/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class DynamicSimulation {

    private DynamicSimulation() {
    }

    public static class Runner implements Versionable {

        private final DynamicSimulationProvider provider;

        public Runner(DynamicSimulationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   ComputationManager computationManager, DynamicSimulationParameters parameters) {
            return provider.run(network, mappingSupplier, curvesSupplier, workingVariantId, computationManager, parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, curvesSupplier, workingVariantId, LocalComputationManager.getDefault(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, curvesSupplier, network.getVariantManager().getWorkingVariantId(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, CurvesSupplier.empty(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier) {
            return runAsync(network, mappingSupplier, curvesSupplier, DynamicSimulationParameters.load());
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier) {
            return runAsync(network, mappingSupplier, DynamicSimulationParameters.load());
        }

        public DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager,
                                           DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, curvesSupplier, workingVariantId, computationManager, parameters).join();
        }

        public DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId, DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, curvesSupplier, workingVariantId, parameters).join();
        }

        public DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, curvesSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, mappingSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier) {
            return runAsync(network, mappingSupplier, curvesSupplier).join();
        }

        public DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier) {
            return runAsync(network, mappingSupplier).join();
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
        return new Runner(PlatformConfigNamedProvider.Finder.findBackwardsCompatible(name,
                "dynamic-simulation", DynamicSimulationProvider.class,
                PlatformConfig.defaultConfig()));
    }

    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                               ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().runAsync(network, mappingSupplier, curvesSupplier, workingVariantId, computationManager, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                                               DynamicSimulationParameters parameters) {
        return find().runAsync(network, mappingSupplier, curvesSupplier, workingVariantId, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, mappingSupplier, curvesSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, mappingSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier) {
        return find().runAsync(network, mappingSupplier, curvesSupplier);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, MappingSupplier mappingSupplier) {
        return find().runAsync(network, mappingSupplier);
    }

    public static DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                       ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().run(network, mappingSupplier, curvesSupplier, workingVariantId, computationManager, parameters);
    }

    public static DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, String workingVariantId,
                                       DynamicSimulationParameters parameters) {
        return find().run(network, mappingSupplier, curvesSupplier, workingVariantId, parameters);
    }

    public static DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, mappingSupplier, curvesSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, mappingSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier, CurvesSupplier curvesSupplier) {
        return find().run(network, mappingSupplier, curvesSupplier);
    }

    public static DynamicSimulationResult run(Network network, MappingSupplier mappingSupplier) {
        return find().run(network, mappingSupplier);
    }
}
