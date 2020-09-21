/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity analysis main API. It is a utility class (so with only static methods) used as an entry point for running
 * a sensitivity analysis allowing to choose either a specific implementation or just to rely on default one.
 *
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public final class SensitivityAnalysis {

    private SensitivityAnalysis() {
        throw new AssertionError("Utility class should not been instantiated");
    }

    /**
     * A sensitivity analysis runner is responsible for providing convenient methods on top of {@link SensitivityAnalysisProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final SensitivityAnalysisProvider provider;

        private Runner(SensitivityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        private void checkInputs(Network network,
                                 String workingStateId,
                                 SensitivityFactorsProvider factorsProvider,
                                 ContingenciesProvider contingenciesProvider,
                                 SensitivityAnalysisParameters parameters,
                                 ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
        }

        private void checkInputs(Network network,
                                 String workingStateId,
                                 SensitivityFactorsProvider factorsProvider,
                                 SensitivityAnalysisParameters parameters,
                                 ComputationManager computationManager) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "Parameters should not be null");
            Objects.requireNonNull(factorsProvider, "Sensitivity factors provider should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      String workingStateId,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      ContingenciesProvider contingenciesProvider,
                                                                      SensitivityAnalysisParameters parameters,
                                                                      ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      String workingStateId,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      ContingenciesProvider contingenciesProvider,
                                                                      SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, contingenciesProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      ContingenciesProvider contingenciesProvider,
                                                                      SensitivityAnalysisParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingenciesProvider, parameters);
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      ContingenciesProvider contingenciesProvider) {
            return runAsync(network, factorsProvider, contingenciesProvider, SensitivityAnalysisParameters.load());
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      String workingStateId,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      SensitivityAnalysisParameters parameters,
                                                                      ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, parameters, computationManager);
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      String workingStateId,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      SensitivityFactorsProvider factorsProvider,
                                                                      SensitivityAnalysisParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public CompletableFuture<SensitivityAnalysisResults> runAsync(Network network,
                                                                      SensitivityFactorsProvider factorsProvider) {
            return runAsync(network, factorsProvider, SensitivityAnalysisParameters.load());
        }

        public SensitivityAnalysisResults run(Network network,
                                              String workingStateId,
                                              SensitivityFactorsProvider factorsProvider,
                                              ContingenciesProvider contingenciesProvider,
                                              SensitivityAnalysisParameters parameters,
                                              ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, computationManager).join();
        }

        public SensitivityAnalysisResults run(Network network,
                                              String workingStateId,
                                              SensitivityFactorsProvider factorsProvider,
                                              ContingenciesProvider contingenciesProvider,
                                              SensitivityAnalysisParameters parameters) {
            return run(network, workingStateId, factorsProvider, contingenciesProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public SensitivityAnalysisResults run(Network network,
                                              SensitivityFactorsProvider factorsProvider,
                                              ContingenciesProvider contingenciesProvider,
                                              SensitivityAnalysisParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingenciesProvider, parameters);
        }

        public SensitivityAnalysisResults run(Network network,
                                              SensitivityFactorsProvider factorsProvider,
                                              ContingenciesProvider contingenciesProvider) {
            return run(network, factorsProvider, contingenciesProvider, SensitivityAnalysisParameters.load());
        }

        public SensitivityAnalysisResults run(Network network,
                                              String workingStateId,
                                              SensitivityFactorsProvider factorsProvider,
                                              SensitivityAnalysisParameters parameters,
                                              ComputationManager computationManager) {
            checkInputs(network, workingStateId, factorsProvider, parameters, computationManager);
            return provider.run(network, workingStateId, factorsProvider, parameters, computationManager).join();
        }

        public SensitivityAnalysisResults run(Network network,
                                              String workingStateId,
                                              SensitivityFactorsProvider factorsProvider,
                                              SensitivityAnalysisParameters parameters) {
            return run(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public SensitivityAnalysisResults run(Network network,
                                              SensitivityFactorsProvider factorsProvider,
                                              SensitivityAnalysisParameters parameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public SensitivityAnalysisResults run(Network network,
                                              SensitivityFactorsProvider factorsProvider) {
            return run(network, factorsProvider, SensitivityAnalysisParameters.load());
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
     * Get a runner for sensitivity analysis implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the sensitivity analysis implementation, null if we want to use default one
     * @return a runner for sensitivity analysis implementation named {@code name}
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
                .find(name, "sensitivity-analysis", SensitivityAnalysisProvider.class,
                        PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default sensitivity analysis implementation.
     *
     * @throws PowsyblException in case we cannot find a default implementation
     * @return a runner for default sensitivity analysis implementation
     */
    public static Runner find() {
        return find(null);
    }

}
