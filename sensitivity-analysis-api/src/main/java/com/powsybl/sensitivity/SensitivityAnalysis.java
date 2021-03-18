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
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity analysis main API. It is a utility class (so with only static methods) used as an entry point for running
 * a sensitivity analysis allowing to choose either a specific implementation or just to rely on the default one.
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

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingStateId,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     List<Contingency> contingencies,
                                                                     SensitivityAnalysisParameters parameters,
                                                                     ComputationManager computationManager) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "Parameters should not be null");
            Objects.requireNonNull(factorsProvider, "Sensitivity factors provider should not be null");
            Objects.requireNonNull(contingencies, "Contingency list should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
            return provider.run(network, workingStateId, factorsProvider, contingencies, parameters, computationManager);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingStateId,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     List<Contingency> contingencies,
                                                                     SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, contingencies, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     List<Contingency> contingencies,
                                                                     SensitivityAnalysisParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, contingencies, parameters);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     List<Contingency> contingencies) {
            return runAsync(network, factorsProvider, contingencies, SensitivityAnalysisParameters.load());
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingStateId,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     SensitivityAnalysisParameters parameters,
                                                                     ComputationManager computationManager) {
            return runAsync(network, workingStateId, factorsProvider, Collections.emptyList(), parameters, computationManager);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingStateId,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, parameters, DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager());
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     SensitivityFactorsProvider factorsProvider,
                                                                     SensitivityAnalysisParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorsProvider, parameters);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     SensitivityFactorsProvider factorsProvider) {
            return runAsync(network, factorsProvider, SensitivityAnalysisParameters.load());
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingStateId,
                                             SensitivityFactorsProvider factorsProvider,
                                             List<Contingency> contingencies,
                                             SensitivityAnalysisParameters parameters,
                                             ComputationManager computationManager) {
            return runAsync(network, workingStateId, factorsProvider, contingencies, parameters, computationManager).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingStateId,
                                             SensitivityFactorsProvider factorsProvider,
                                             List<Contingency> contingencies,
                                             SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, contingencies, parameters).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             SensitivityFactorsProvider factorsProvider,
                                             List<Contingency> contingencies,
                                             SensitivityAnalysisParameters parameters) {
            return runAsync(network, factorsProvider, contingencies, parameters).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             SensitivityFactorsProvider factorsProvider,
                                             List<Contingency> contingencies) {
            return runAsync(network, factorsProvider, contingencies).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingStateId,
                                             SensitivityFactorsProvider factorsProvider,
                                             SensitivityAnalysisParameters parameters,
                                             ComputationManager computationManager) {
            return runAsync(network, workingStateId, factorsProvider, parameters, computationManager).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingStateId,
                                             SensitivityFactorsProvider factorsProvider,
                                             SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorsProvider, parameters).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             SensitivityFactorsProvider factorsProvider,
                                             SensitivityAnalysisParameters parameters) {
            return runAsync(network, factorsProvider, parameters).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             SensitivityFactorsProvider factorsProvider) {
            return runAsync(network, factorsProvider).join();
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

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingStateId,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        List<Contingency> contingencies,
                                                                        SensitivityAnalysisParameters parameters,
                                                                        ComputationManager computationManager) {
        return find().runAsync(network, workingStateId, factorsProvider, contingencies, parameters, computationManager);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingStateId,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        List<Contingency> contingencies,
                                                                        SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, workingStateId, factorsProvider, contingencies, parameters);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        List<Contingency> contingencies,
                                                                        SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, factorsProvider, contingencies, parameters);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        List<Contingency> contingencies) {
        return find().runAsync(network, factorsProvider, contingencies);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingStateId,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        SensitivityAnalysisParameters parameters,
                                                                        ComputationManager computationManager) {
        return find().runAsync(network, workingStateId, factorsProvider, parameters, computationManager);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingStateId,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, workingStateId, factorsProvider, parameters);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        SensitivityFactorsProvider factorsProvider,
                                                                        SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, factorsProvider, parameters);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        SensitivityFactorsProvider factorsProvider) {
        return find().runAsync(network, factorsProvider);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingStateId,
                                                SensitivityFactorsProvider factorsProvider,
                                                List<Contingency> contingencies,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager) {
        return find().run(network, workingStateId, factorsProvider, contingencies, parameters, computationManager);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingStateId,
                                                SensitivityFactorsProvider factorsProvider,
                                                List<Contingency> contingencies,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, workingStateId, factorsProvider, contingencies, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                SensitivityFactorsProvider factorsProvider,
                                                List<Contingency> contingencies,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factorsProvider, contingencies, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                SensitivityFactorsProvider factorsProvider,
                                                List<Contingency> contingencies) {
        return find().run(network, factorsProvider, contingencies);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingStateId,
                                                SensitivityFactorsProvider factorsProvider,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager) {
        return find().run(network, workingStateId, factorsProvider, parameters, computationManager);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingStateId,
                                                SensitivityFactorsProvider factorsProvider,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, workingStateId, factorsProvider, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                SensitivityFactorsProvider factorsProvider,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factorsProvider, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                SensitivityFactorsProvider factorsProvider) {
        return find().run(network, factorsProvider);
    }
}
