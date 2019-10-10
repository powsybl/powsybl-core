/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
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
        return find(name, PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
    }

    public static Runner find() {
        return find(null);
    }

    public static Runner find(String name, List<DynamicSimulationProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);
        Objects.requireNonNull(platformConfig);

        if (providers.isEmpty()) {
            throw new PowsyblException("No dynamic simulation providers found");
        }

        // if no dynamic simulation implementation name is provided through the API we
        // look for information in platform configuration
        String dynamicSimulatorName = name != null ? name
            : platformConfig.getOptionalModuleConfig("dynamic-simulation")
                .flatMap(mc -> mc.getOptionalStringProperty("default"))
                .orElse(null);
        DynamicSimulationProvider provider;
        if (providers.size() == 1 && dynamicSimulatorName == null) {
            // no information to select the implementation but only one provider, so we can
            // use it by default (that is be the most common use case)
            provider = providers.get(0);
        } else {
            if (providers.size() > 1 && dynamicSimulatorName == null) {
                // several providers and no information to select which one to choose, we can
                // only throw an exception
                List<String> dynamicSimulatorNames = providers.stream().map(DynamicSimulationProvider::getName)
                    .collect(Collectors.toList());
                throw new PowsyblException("Several dynamic simulation implementations found (" + dynamicSimulatorNames
                    + "), you must add configuration to select the implementation");
            }
            provider = providers.stream()
                .filter(p -> p.getName().equals(dynamicSimulatorName))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Dynamic simulation '" + dynamicSimulatorName + "' not found"));
        }

        return new Runner(provider);
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
