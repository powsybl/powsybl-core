/*
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Sensitivity computation main API. It is a utility class (so with only static methods) used as an entry point for running
 * a sensitivity computation allowing to choose either a specific implementation or just to rely on default one.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class SensitivityComputation {

    private SensitivityComputation() {
        throw new AssertionError("Utility class should not been instantiated");
    }

    private static final Supplier<List<SensitivityComputationProvider>> SENSITIVITY_COMPUTATION_PROVIDERS
        = Suppliers.memoize(() -> new ServiceLoaderCache<>(SensitivityComputationProvider.class).getServices());

    /**
     * A sensitivity computation runner is responsible for providing convenient methods on top of {@link SensitivityComputationProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final SensitivityComputationProvider provider;

        private Runner(SensitivityComputationProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        private void checkInputs(Network network,
                                 String workingStateId,
                                 SensitivityFactorsProvider factorsProvider,
                                 ContingenciesProvider contingenciesProvider,
                                 SensitivityComputationParameters parameters,
                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
        }

        private void checkInputs(Network network,
                                 String workingStateId,
                                 SensitivityFactorsProvider factorsProvider,
                                 SensitivityComputationParameters parameters,
                                 ComputationManager computationManager) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "Parameters should not be null");
            Objects.requireNonNull(factorsProvider, "Sensitivity factors provider should not be null");
            Objects.requireNonNull(parameters, "Sensitivity computation parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider,
                                                                         SensitivityComputationParameters parameters,
                                                                         ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, contingenciesProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingenciesProvider, parameters);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         ContingenciesProvider contingenciesProvider) {
            return runAsync(network, factorsProvider, contingenciesProvider, SensitivityComputationParameters.load());
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         SensitivityComputationParameters parameters,
                                                                         ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, parameters, computationManager);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         String workingStateId,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider,
                                                                         SensitivityComputationParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public CompletableFuture<SensitivityComputationResults> runAsync(Network network,
                                                                         SensitivityFactorsProvider factorsProvider) {
            return runAsync(network, factorsProvider, SensitivityComputationParameters.load());
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider,
                                                 SensitivityComputationParameters parameters,
                                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager).join();
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingenciesProvider, parameters);
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 ContingenciesProvider contingenciesProvider) {
            return run(network, factorsProvider, contingenciesProvider, SensitivityComputationParameters.load());
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 SensitivityComputationParameters parameters,
                                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, parameters, computationManager).join();
        }

        public SensitivityComputationResults run(Network network,
                                                 String workingStateId,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider,
                                                 SensitivityComputationParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public SensitivityComputationResults run(Network network,
                                                 SensitivityFactorsProvider factorsProvider) {
            return run(network, factorsProvider, SensitivityComputationParameters.load());
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
     * Get a runner for sensitivity computation implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the sensitivity computation implementation, null if we want to use default one
     * @return a runner for sensitivity computation implementation named {@code name}
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
                .find(name, "sensitivity-computation", SensitivityComputationProvider.class,
                        PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default sensitivity computation implementation.
     *
     * @throws PowsyblException in case we cannot find a default implementation
     * @return a runner for default sensitivity computation implementation
     */
    public static Runner find() {
        return find(null);
    }

}
