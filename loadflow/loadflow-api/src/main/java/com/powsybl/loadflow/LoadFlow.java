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
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * LoadFlow main API. It is a utility class (so with only static methods) used as an entry point for running
 * a loadflow allowing to choose either a specific find implementation or just to rely on default one.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class LoadFlow {

    private LoadFlow() {
    }

    private static final Supplier<List<LoadFlowProvider>> PROVIDERS_SUPPLIERS
            = Suppliers.memoize(() -> new ServiceLoaderCache<>(LoadFlowProvider.class).getServices());

    /**
     * A loadflow runner is responsible for providing convenient methods on top of {@link LoadFlowProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static class Runner implements Versionable {

        private final LoadFlowProvider provider;

        Runner(LoadFlowProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
            Objects.requireNonNull(workingStateId);
            Objects.requireNonNull(parameters);
            return provider.run(network, computationManager, workingStateId, parameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
            return runAsync(network, LocalComputationManager.getDefault(), parameters);
        }

        public CompletableFuture<LoadFlowResult> runAsync(Network network) {
            return runAsync(network, LoadFlowParameters.load());
        }

        public LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
            Objects.requireNonNull(workingStateId);
            Objects.requireNonNull(parameters);
            return provider.run(network, computationManager, workingStateId, parameters).join();
        }

        public LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), computationManager, parameters);
        }

        public LoadFlowResult run(Network network, LoadFlowParameters parameters) {
            return run(network, LocalComputationManager.getDefault(), parameters);
        }

        public LoadFlowResult run(Network network) {
            return run(network, LoadFlowParameters.load());
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
        return find(name, PROVIDERS_SUPPLIERS.get(), PlatformConfig.defaultConfig());
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
     * A variant of {@link LoadFlow#find(String)} intended to be used for unit testing that allow passing
     * an explicit provider list instead of relying on service loader and an explicit {@link PlatformConfig}
     * instead of global one.
     *
     * @param name name of the load flow implementation, null if we want to use default one
     * @param providers loadflow provider list
     * @param platformConfig platform config to look for default loadflow implementation name
     * @return a runner for loadflow implementation named {@code name}
     */
    public static Runner find(String name, List<LoadFlowProvider> providers, PlatformConfig platformConfig) {
        Objects.requireNonNull(providers);
        Objects.requireNonNull(platformConfig);

        if (providers.isEmpty()) {
            throw new PowsyblException("No loadflow providers found");
        }

        // if no loadflow implementation name is provided through the API we look for information
        // in platform configuration
        String loadFlowName = name != null ? name : platformConfig.getOptionalModuleConfig("load-flow")
                                                                  .flatMap(mc -> mc.getOptionalStringProperty("default"))
                                                                  .orElse(null);
        LoadFlowProvider provider;
        if (providers.size() == 1 && loadFlowName == null) {
            // no information to select the implementation but only one provider, so we can use it by default
            // (that is be the most common use case)
            provider = providers.get(0);
        } else {
            if (providers.size() > 1 && loadFlowName == null) {
                // several providers and no information to select which one to choose, we can only throw
                // an exception
                throw new PowsyblException("Loadflow configuration not found");
            }
            provider = providers.stream()
                    .filter(p -> p.getName().equals(loadFlowName))
                    .findFirst()
                    .orElseThrow(() -> new PowsyblException("Loadflow '" + loadFlowName + "' not found"));
        }

        return new Runner(provider);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().runAsync(network, workingStateId, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().runAsync(network, computationManager, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network, LoadFlowParameters parameters) {
        return find().runAsync(network, parameters);
    }

    public static CompletableFuture<LoadFlowResult> runAsync(Network network) {
        return find().runAsync(network);
    }

    public static LoadFlowResult run(Network network, String workingStateId, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().run(network, workingStateId, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network, ComputationManager computationManager, LoadFlowParameters parameters) {
        return find().run(network, computationManager, parameters);
    }

    public static LoadFlowResult run(Network network, LoadFlowParameters parameters) {
        return find().run(network, parameters);
    }

    public static LoadFlowResult run(Network network) {
        return find().run(network);
    }
}
