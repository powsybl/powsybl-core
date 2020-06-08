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

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   ComputationManager computationManager, DynamicSimulationParameters parameters) {
            return provider.run(network, curvesSupplier, workingVariantId, computationManager, parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier, String workingVariantId,
                                                                   DynamicSimulationParameters parameters) {
            return runAsync(network, curvesSupplier, workingVariantId, LocalComputationManager.getDefault(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, curvesSupplier, network.getVariantManager().getWorkingVariantId(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicSimulationParameters parameters) {
            return runAsync(network, CurvesSupplier.empty(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier) {
            return runAsync(network, curvesSupplier, DynamicSimulationParameters.load());
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network) {
            return runAsync(network, DynamicSimulationParameters.load());
        }

        public DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager,
                                           DynamicSimulationParameters parameters) {
            return runAsync(network, curvesSupplier, workingVariantId, computationManager, parameters).join();
        }

        public DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier, String workingVariantId, DynamicSimulationParameters parameters) {
            return runAsync(network, curvesSupplier, workingVariantId, parameters).join();
        }

        public DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
            return runAsync(network, curvesSupplier, parameters).join();
        }

        public DynamicSimulationResult run(Network network, DynamicSimulationParameters parameters) {
            return runAsync(network, parameters).join();
        }

        public DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier) {
            return runAsync(network, curvesSupplier).join();
        }

        public DynamicSimulationResult run(Network network) {
            return runAsync(network).join();
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

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier, String workingVariantId,
                                                               ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().runAsync(network, curvesSupplier, workingVariantId, computationManager, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier, String workingVariantId,
                                                               DynamicSimulationParameters parameters) {
        return find().runAsync(network, curvesSupplier, workingVariantId, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
        return find().runAsync(network, curvesSupplier, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, DynamicSimulationParameters parameters) {
        return find().runAsync(network, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, CurvesSupplier curvesSupplier) {
        return find().runAsync(network, curvesSupplier);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network) {
        return find().runAsync(network);
    }

    public static DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier, String workingVariantId,
                                       ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().run(network, curvesSupplier, workingVariantId, computationManager, parameters);
    }

    public static DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier, String workingVariantId,
                                       DynamicSimulationParameters parameters) {
        return find().run(network, curvesSupplier, workingVariantId, parameters);
    }

    public static DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier, DynamicSimulationParameters parameters) {
        return find().run(network, curvesSupplier, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicSimulationParameters parameters) {
        return find().run(network, parameters);
    }

    public static DynamicSimulationResult run(Network network, CurvesSupplier curvesSupplier) {
        return find().run(network, curvesSupplier);
    }

    public static DynamicSimulationResult run(Network network) {
        return find().run(network);
    }
}
