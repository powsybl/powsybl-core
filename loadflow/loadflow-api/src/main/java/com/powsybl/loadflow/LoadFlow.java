/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * LoadFlow main API. It is a utility class (so with only static methods) used as an entry point for running
 * a loadflow allowing to choose either a specific named implementation or just to rely on default one.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class LoadFlow {

    private LoadFlow() {
    }

    private static final Supplier<List<LoadFlowProvider>> PROVIDERS_SUPPLIERS
            = Suppliers.memoize(() -> new ServiceLoaderCache<>(LoadFlowProvider.class).getServices());

    /**
     * Get a loadflow runner for loadflow implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the load flow implementation, null if we want to use default implementation
     * @return a loadflow runner for implementation named {@code name}
     */
    public static LoadFlowRunner named(String name) {
        return named(name, PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
    }

    /**
     * A variant of {@link LoadFlow#named(String)} intended to be used for unit testing that allow passing
     * an explicit provider list instead of relying on service loader.
     *
     * @param name name of the load flow implementation, null if we want to use default implementation
     * @param providers a loadflow provider list that will be used to search for an implementation
     * @param platformConfig platform config to use for default implementation name search
     * @return a loadflow runner for implementation named {@code name}
     */
    public static LoadFlowRunner named(String name, List<LoadFlowProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);

        if (name == null) {
            return defaultRunner(providers, platformConfig);
        }

        LoadFlowProvider provider = providers.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Loadflow '" + name + "' not found"));

        return new LoadFlowRunner(provider);
    }

    /**
     * Get default loadflow runner.
     *
     * @throws PowsyblException in case we cannot find a default implementation
     * @return default loadflow runner.
     */
    public static LoadFlowRunner defaultRunner() {
        return defaultRunner(PlatformConfig.defaultConfig());
    }

    public static LoadFlowRunner defaultRunner(PlatformConfig platformConfig) {
        return defaultRunner(PROVIDERS_SUPPLIERS.get(), platformConfig);
    }

    static LoadFlowRunner defaultRunner(List<LoadFlowProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);
        Objects.requireNonNull(platformConfig);

        if (providers.isEmpty()) {
            throw new PowsyblException("No loadflow providers found");
        }
        String name = platformConfig.getOptionalModuleConfig("load-flow")
                .flatMap(mc -> mc.getOptionalStringProperty("default"))
                .orElse(null);
        if (providers.size() == 1) {
            LoadFlowProvider provider = providers.get(0);
            if (name != null && !provider.getName().equals(name)) {
                throw new PowsyblException("Loadflow '" + name + "' not found");
            }
            return new LoadFlowRunner(provider);
        } else {
            // try to find a loadflow config to known which implementation has to be chosen
            if (name == null) {
                throw new PowsyblException("Loadflow configuration not found");
            }
            return named(name, providers, platformConfig);
        }
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return defaultRunner().runAsync(network, workingStateId, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return defaultRunner().runAsync(network, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
        return defaultRunner().runAsync(network, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network) {
        return defaultRunner().runAsync(network);
    }

    public static LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return defaultRunner().run(network, workingStateId, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return defaultRunner().run(network, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network, LoadFlowParameters parameters) {
        return defaultRunner().run(network, parameters);
    }

    public static LoadFlowResult run(Network network) {
        return defaultRunner().run(network);
    }
}
