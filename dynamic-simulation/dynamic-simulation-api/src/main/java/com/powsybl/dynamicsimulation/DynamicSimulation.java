/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public final class DynamicSimulation {

    private DynamicSimulation() {
    }

    private static final Supplier<List<DynamicSimulationProvider>> PROVIDERS_SUPPLIERS = Suppliers
        .memoize(() -> new ServiceLoaderCache<>(DynamicSimulationProvider.class).getServices());

    public static class Runner implements Versionable {

        private final DynamicSimulationProvider provider;

        public Runner(DynamicSimulationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network, String workingStateId,
            ComputationManager computationManager, DynamicSimulationParameters parameters) {
            Objects.requireNonNull(workingStateId);
            Objects.requireNonNull(parameters);
            return provider.run(network, computationManager, workingStateId, parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network,
            ComputationManager computationManager, DynamicSimulationParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network,
            DynamicSimulationParameters parameters) {
            return runAsync(network, LocalComputationManager.getDefault(), parameters);
        }

        public CompletableFuture<DynamicSimulationResult> runAsync(Network network) {
            return runAsync(network, DynamicSimulationParameters.load());
        }

        public DynamicSimulationResult run(Network network, String workingStateId,
            ComputationManager computationManager, DynamicSimulationParameters parameters) {
            Objects.requireNonNull(workingStateId);
            Objects.requireNonNull(parameters);
            return provider.run(network, computationManager, workingStateId, parameters).join();
        }

        public DynamicSimulationResult run(Network network, ComputationManager computationManager,
            DynamicSimulationParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
        }

        public DynamicSimulationResult run(Network network, DynamicSimulationParameters parameters) {
            return run(network, LocalComputationManager.getDefault(), parameters);
        }

        public DynamicSimulationResult run(Network network) {
            return run(network, DynamicSimulationParameters.load());
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
                "dynamic-simulation", PROVIDERS_SUPPLIERS.get(),
                PlatformConfig.defaultConfig()));
    }

    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network, String workingStateId,
        ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().runAsync(network, workingStateId, computationManager, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network,
        ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().runAsync(network, computationManager, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network,
        DynamicSimulationParameters parameters) {
        return find().runAsync(network, parameters);
    }

    public static CompletableFuture<DynamicSimulationResult> runAsync(Network network) {
        return find().runAsync(network);
    }

    public static DynamicSimulationResult run(Network network, String workingStateId,
        ComputationManager computationManager, DynamicSimulationParameters parameters) {
        return find().run(network, workingStateId, computationManager, parameters);
    }

    public static DynamicSimulationResult run(Network network, ComputationManager computationManager,
        DynamicSimulationParameters parameters) {
        return find().run(network, computationManager, parameters);
    }

    public static DynamicSimulationResult run(Network network, DynamicSimulationParameters parameters) {
        return find().run(network, parameters);
    }

    public static DynamicSimulationResult run(Network network) {
        return find().run(network);
    }
}
