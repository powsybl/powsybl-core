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
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Sensitivity analysis main API. It is a utility class used as an entry point for running
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

        public Runner(SensitivityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingVariantId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityResultWriter resultWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager,
                                                Reporter reporter) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "Working variant ID should not be null");
            Objects.requireNonNull(factorReader, "Sensitivity factors reader should not be null");
            Objects.requireNonNull(resultWriter, "Sensitivity results writer should not be null");
            Objects.requireNonNull(contingencies, "Contingency list should not be null");
            Objects.requireNonNull(variableSets, "VariableSet list should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
            Objects.requireNonNull(reporter, "Reporter should not be null");

            return provider.run(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reporter);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingVariantId,
                                                                     List<SensitivityFactor> factors,
                                                                     List<Contingency> contingencies,
                                                                     List<SensitivityVariableSet> variableSets,
                                                                     SensitivityAnalysisParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     Reporter reporter) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "Working variant ID should not be null");
            Objects.requireNonNull(factors, "Sensitivity factor list should not be null");
            Objects.requireNonNull(contingencies, "Contingency list should not be null");
            Objects.requireNonNull(variableSets, "VariableSet list should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(computationManager, "Computation manager should not be null");
            Objects.requireNonNull(reporter, "Reporter should not be null");

            SensitivityFactorReader factorReader = new SensitivityFactorModelReader(factors, network);
            SensitivityResultModelWriter resultWriter = new SensitivityResultModelWriter();

            return provider.run(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reporter)
                    .thenApply(unused -> new SensitivityAnalysisResult(factors, contingencies, resultWriter.getContingencyStatuses(), resultWriter.getValues()));
        }

        public void run(Network network,
                        String workingVariantId,
                        SensitivityFactorReader factorReader,
                        SensitivityResultWriter resultWriter,
                        List<Contingency> contingencies,
                        List<SensitivityVariableSet> variableSets,
                        SensitivityAnalysisParameters parameters,
                        ComputationManager computationManager,
                        Reporter reporter) {
            runAsync(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reporter).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingVariantId,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             Reporter reporter) {
            return runAsync(network, workingVariantId, factors, contingencies, variableSets, parameters, computationManager, reporter).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingVariantId,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters) {
            return runAsync(network, workingVariantId, factors, contingencies, variableSets, parameters, LocalComputationManager.getDefault(), Reporter.NO_OP).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors, contingencies, variableSets, parameters);
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors, contingencies, Collections.emptyList(), parameters);
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies) {
            return run(network, factors, contingencies, Collections.emptyList(), SensitivityAnalysisParameters.load());
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors) {
            return run(network, factors, Collections.emptyList());
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, factors, Collections.emptyList(), parameters);
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
                                                   String workingVariantId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityResultWriter resultWriter,
                                                   List<Contingency> contingencies,
                                                   List<SensitivityVariableSet> variableSets,
                                                   SensitivityAnalysisParameters parameters,
                                                   ComputationManager computationManager,
                                                   Reporter reporter) {
        return find().runAsync(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reporter);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingVariantId,
                                                                        List<SensitivityFactor> factors,
                                                                        List<Contingency> contingencies,
                                                                        List<SensitivityVariableSet> variableSets,
                                                                        SensitivityAnalysisParameters parameters,
                                                                        ComputationManager computationManager,
                                                                        Reporter reporter) {
        return find().runAsync(network, workingVariantId, factors, contingencies, variableSets, parameters, computationManager, reporter);
    }

    public static void run(Network network,
                           String workingVariantId,
                           SensitivityFactorReader factorReader,
                           SensitivityResultWriter resultWriter,
                           List<Contingency> contingencies,
                           List<SensitivityVariableSet> variableSets,
                           SensitivityAnalysisParameters parameters,
                           ComputationManager computationManager,
                           Reporter reporter) {
        find().run(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reporter);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingVariantId,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager,
                                                Reporter reporter) {
        return find().run(network, workingVariantId, factors, contingencies, variableSets, parameters, computationManager, reporter);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingVariantId,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, workingVariantId, factors, contingencies, variableSets, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factors, contingencies, variableSets, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factors, contingencies, parameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies) {
        return find().run(network, factors, contingencies);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors) {
        return find().run(network, factors);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factors, parameters);
    }
}
