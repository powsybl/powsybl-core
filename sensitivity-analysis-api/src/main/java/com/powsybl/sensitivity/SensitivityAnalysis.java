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
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.*;
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

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingStateId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager,
                                                Reporter reporter) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "Parameters should not be null");
            Objects.requireNonNull(factorReader, "Sensitivity factors reader should not be null");
            Objects.requireNonNull(valueWriter, "Sensitivity values writer should not be null");
            Objects.requireNonNull(contingencies, "Contingency list should not be null");
            Objects.requireNonNull(variableSets, "VariableSet list should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
            Objects.requireNonNull(reporter, "Reporter should not be null");

            return provider.run(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, computationManager, reporter);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingStateId,
                                                                     List<SensitivityFactor> sensitivityFactors,
                                                                     List<Contingency> contingencies,
                                                                     List<SensitivityVariableSet> variableSets,
                                                                     SensitivityAnalysisParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     Reporter reporter) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "Parameters should not be null");
            Objects.requireNonNull(sensitivityFactors, "Sensitivity factor list should not be null");
            Objects.requireNonNull(contingencies, "Contingency list should not be null");
            Objects.requireNonNull(variableSets, "VariableSet list should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
            Objects.requireNonNull(reporter, "Reporter should not be null");

            return CompletableFuture.supplyAsync(() -> {
                SensitivityFactorReader factorReader = new SensitivityFactorModelReader(sensitivityFactors, network);
                SensitivityValueModelWriter valueWriter = new SensitivityValueModelWriter();

                provider.run(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, computationManager, reporter);

                return new SensitivityAnalysisResult(valueWriter.getValues());
            });
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingStateId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager) {
            return runAsync(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, computationManager, Reporter.NO_OP);
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingStateId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorReader, valueWriter, contingencies, variableSets, parameters);
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets) {
            return runAsync(network, factorReader, valueWriter, contingencies, variableSets, SensitivityAnalysisParameters.load());
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingStateId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager) {
            return runAsync(network, workingStateId, factorReader, valueWriter, Collections.emptyList(), Collections.emptyList(), parameters, computationManager);
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingStateId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingStateId, factorReader, valueWriter, parameters, DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager());
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter,
                                                SensitivityAnalysisParameters parameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), factorReader, valueWriter, parameters);
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                SensitivityFactorReader factorReader,
                                                SensitivityValueWriter valueWriter) {
            return runAsync(network, factorReader, valueWriter, SensitivityAnalysisParameters.load());
        }

        public void run(Network network,
                        String workingStateId,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        List<Contingency> contingencies,
                        List<SensitivityVariableSet> variableSets,
                        SensitivityAnalysisParameters parameters,
                        ComputationManager computationManager) {
            runAsync(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, computationManager).join();
        }

        public void run(Network network,
                        String workingStateId,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        List<Contingency> contingencies,
                        List<SensitivityVariableSet> variableSets,
                        SensitivityAnalysisParameters parameters) {
            runAsync(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters).join();
        }

        public void run(Network network,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        List<Contingency> contingencies,
                        List<SensitivityVariableSet> variableSets,
                        SensitivityAnalysisParameters parameters) {
            runAsync(network, factorReader, valueWriter, contingencies, variableSets, parameters).join();
        }

        public void run(Network network,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        List<Contingency> contingencies,
                        List<SensitivityVariableSet> variableSets) {
            runAsync(network, factorReader, valueWriter, contingencies, variableSets).join();
        }

        public void run(Network network,
                        String workingStateId,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        SensitivityAnalysisParameters parameters,
                        ComputationManager computationManager) {
            runAsync(network, workingStateId, factorReader, valueWriter, parameters, computationManager).join();
        }

        public void run(Network network,
                        String workingStateId,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        SensitivityAnalysisParameters parameters) {
            runAsync(network, workingStateId, factorReader, valueWriter, parameters).join();
        }

        public void run(Network network,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter,
                        SensitivityAnalysisParameters parameters) {
            runAsync(network, factorReader, valueWriter, parameters).join();
        }

        public void run(Network network,
                        SensitivityFactorReader factorReader,
                        SensitivityValueWriter valueWriter) {
            runAsync(network, factorReader, valueWriter).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingStateId,
                                             List<SensitivityFactor> sensitivityFactors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             Reporter reporter) {
            return runAsync(network, workingStateId, sensitivityFactors, contingencies, variableSets, parameters, computationManager, reporter).join();
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

    public static CompletableFuture<Void> runAsync(Network network,
                                                   String workingStateId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   List<Contingency> contingencies,
                                                   List<SensitivityVariableSet> variableSets,
                                                   SensitivityAnalysisParameters parameters,
                                                   ComputationManager computationManager) {
        return find().runAsync(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, computationManager);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingStateId,
                                                                        List<SensitivityFactor> factors,
                                                                        List<Contingency> contingencies,
                                                                        List<SensitivityVariableSet> variableSets,
                                                                        SensitivityAnalysisParameters parameters,
                                                                        ComputationManager computationManager) {
        return find().runAsync(network, workingStateId, factors, contingencies, variableSets, parameters, computationManager, Reporter.NO_OP);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   String workingStateId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   List<Contingency> contingencies,
                                                   List<SensitivityVariableSet> variableSets,
                                                   SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   List<Contingency> contingencies,
                                                   List<SensitivityVariableSet> variableSets,
                                                   SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, factorReader, valueWriter, contingencies, variableSets, parameters);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   List<Contingency> contingencies,
                                                   List<SensitivityVariableSet> variableSets) {
        return find().runAsync(network, factorReader, valueWriter, contingencies, variableSets);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   String workingStateId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   SensitivityAnalysisParameters parameters,
                                                   ComputationManager computationManager) {
        return find().runAsync(network, workingStateId, factorReader, valueWriter, parameters, computationManager);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   String workingStateId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, workingStateId, factorReader, valueWriter, parameters);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter,
                                                   SensitivityAnalysisParameters parameters) {
        return find().runAsync(network, factorReader, valueWriter, parameters);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityValueWriter valueWriter) {
        return find().runAsync(network, factorReader, valueWriter);
    }

    public static void run(Network network,
                           String workingStateId,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           List<Contingency> contingencies,
                           List<SensitivityVariableSet> variableSets,
                           SensitivityAnalysisParameters parameters,
                           ComputationManager computationManager) {
        find().run(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters, computationManager);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingStateId,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager) {
        return find().run(network, workingStateId, factors, contingencies, variableSets, parameters, computationManager, Reporter.NO_OP);
    }

    public static void run(Network network,
                           String workingStateId,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           List<Contingency> contingencies,
                           List<SensitivityVariableSet> variableSets,
                           SensitivityAnalysisParameters parameters) {
        find().run(network, workingStateId, factorReader, valueWriter, contingencies, variableSets, parameters);
    }

    public static void run(Network network,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           List<Contingency> contingencies,
                           List<SensitivityVariableSet> variableSets,
                           SensitivityAnalysisParameters parameters) {
        find().run(network, factorReader, valueWriter, contingencies, variableSets, parameters);
    }

    public static void run(Network network,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           List<Contingency> contingencies,
                           List<SensitivityVariableSet> variableSets) {
        find().run(network, factorReader, valueWriter, contingencies, variableSets);
    }

    public static void run(Network network,
                           String workingStateId,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           SensitivityAnalysisParameters parameters,
                           ComputationManager computationManager) {
        find().run(network, workingStateId, factorReader, valueWriter, parameters, computationManager);
    }

    public static void run(Network network,
                           String workingStateId,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           SensitivityAnalysisParameters parameters) {
        find().run(network, workingStateId, factorReader, valueWriter, parameters);
    }

    public static void run(Network network,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter,
                           SensitivityAnalysisParameters parameters) {
        find().run(network, factorReader, valueWriter, parameters);
    }

    public static void run(Network network,
                           SensitivityFactorReader factorReader,
                           SensitivityValueWriter valueWriter) {
        find().run(network, factorReader, valueWriter);
    }
}
