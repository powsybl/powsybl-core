/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;

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
        throw new IllegalStateException("Utility class should not been instantiated");
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

        /**
         * @deprecated use {@link #runAsync(Network, String, SensitivityFactorReader, SensitivityResultWriter, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public CompletableFuture<Void> runAsync(Network network,
                                                String workingVariantId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityResultWriter resultWriter,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager,
                                                ReportNode reportNode) {
            return runAsync(network, workingVariantId, factorReader, resultWriter,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setVariableSets(variableSets)
                    .setParameters(parameters)
                    .setComputationManager(computationManager)
                    .setReportNode(reportNode));
        }

        /**
         * @deprecated use {@link #runAsync(Network, String, List, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     String workingVariantId,
                                                                     List<SensitivityFactor> factors,
                                                                     List<Contingency> contingencies,
                                                                     List<SensitivityVariableSet> variableSets,
                                                                     SensitivityAnalysisParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     ReportNode reportNode) {
            return runAsync(network, workingVariantId, factors, new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setVariableSets(variableSets)
                .setParameters(parameters)
                .setComputationManager(computationManager)
                .setReportNode(reportNode));
        }

        public CompletableFuture<Void> runAsync(Network network,
                                                String workingVariantId,
                                                SensitivityFactorReader factorReader,
                                                SensitivityResultWriter resultWriter,
                                                SensitivityAnalysisRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "Working variant ID should not be null");
            Objects.requireNonNull(factorReader, "Sensitivity factors reader should not be null");
            Objects.requireNonNull(resultWriter, "Sensitivity results writer should not be null");
            Objects.requireNonNull(runParameters, "Sensitivity analysis run parameters should not be null");

            return provider.run(network, workingVariantId, factorReader, resultWriter, runParameters);
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                String workingVariantId,
                                                List<SensitivityFactor> factors,
                                                SensitivityAnalysisRunParameters runParameters) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingVariantId, "Working variant ID should not be null");
            Objects.requireNonNull(factors, "Sensitivity factors reader should not be null");
            Objects.requireNonNull(runParameters, "Sensitivity analysis run parameters should not be null");

            SensitivityFactorReader factorReader = new SensitivityFactorModelReader(factors, network);
            SensitivityResultModelWriter resultWriter = new SensitivityResultModelWriter(runParameters.getContingencies());

            return provider.run(network, workingVariantId, factorReader, resultWriter, runParameters)
                .thenApply(unused -> new SensitivityAnalysisResult(factors, resultWriter.getContingencyStatuses(), resultWriter.getValues()));
        }

        public CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                     List<SensitivityFactor> factors) {

            return runAsync(network,
                VariantManagerConstants.INITIAL_VARIANT_ID,
                factors,
                SensitivityAnalysisRunParameters.getDefault());
        }

        /**
         * @deprecated use {@link #run(Network, String, SensitivityFactorReader, SensitivityResultWriter, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public void run(Network network,
                        String workingVariantId,
                        SensitivityFactorReader factorReader,
                        SensitivityResultWriter resultWriter,
                        List<Contingency> contingencies,
                        List<SensitivityVariableSet> variableSets,
                        SensitivityAnalysisParameters parameters,
                        ComputationManager computationManager,
                        ReportNode reportNode) {
            run(network, workingVariantId, factorReader, resultWriter,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setVariableSets(variableSets)
                    .setParameters(parameters)
                    .setComputationManager(computationManager)
                    .setReportNode(reportNode));
        }

        /**
         * @deprecated use {@link #run(Network, String, List, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public SensitivityAnalysisResult run(Network network,
                                             String workingVariantId,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             ReportNode reportNode) {
            return run(network, workingVariantId, factors,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setVariableSets(variableSets)
                    .setParameters(parameters)
                    .setComputationManager(computationManager)
                    .setReportNode(reportNode));
        }

        /**
         * @deprecated use {@link #run(Network, String, List, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public SensitivityAnalysisResult run(Network network,
                                             String workingVariantId,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, workingVariantId, factors,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setVariableSets(variableSets)
                    .setParameters(parameters));
        }

        /**
         * @deprecated use {@link #run(Network, List, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             List<SensitivityVariableSet> variableSets,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setVariableSets(variableSets)
                    .setParameters(parameters));
        }

        /**
         * @deprecated use {@link #run(Network, List, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setParameters(parameters));
        }

        /**
         * @deprecated use {@link #run(Network, List, SensitivityAnalysisRunParameters)} instead
         */
        @Deprecated(since = "6.9.0", forRemoval = true)
        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             List<Contingency> contingencies) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors,
                new SensitivityAnalysisRunParameters().setContingencies(contingencies));
        }

        public void run(Network network,
                        String workingVariantId,
                        SensitivityFactorReader factorReader,
                        SensitivityResultWriter resultWriter,
                        SensitivityAnalysisRunParameters runParameters) {
            runAsync(network, workingVariantId, factorReader, resultWriter, runParameters).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             String workingVariantId,
                                             List<SensitivityFactor> factors,
                                             SensitivityAnalysisRunParameters runParameters) {
            return runAsync(network, workingVariantId, factors, runParameters).join();
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             SensitivityAnalysisParameters parameters) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors,
                new SensitivityAnalysisRunParameters().setParameters(parameters));
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors,
                                             SensitivityAnalysisRunParameters runParameters) {
            return run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factors, runParameters);
        }

        public SensitivityAnalysisResult run(Network network,
                                             List<SensitivityFactor> factors) {
            return run(network, factors, SensitivityAnalysisRunParameters.getDefault());
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

    /**
     * @deprecated use {@link #runAsync(Network, String, SensitivityFactorReader, SensitivityResultWriter, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static CompletableFuture<Void> runAsync(Network network,
                                                   String workingVariantId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityResultWriter resultWriter,
                                                   List<Contingency> contingencies,
                                                   List<SensitivityVariableSet> variableSets,
                                                   SensitivityAnalysisParameters parameters,
                                                   ComputationManager computationManager,
                                                   ReportNode reportNode) {
        return find().runAsync(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reportNode);
    }

    /**
     * @deprecated use {@link #runAsync(Network, String, List, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingVariantId,
                                                                        List<SensitivityFactor> factors,
                                                                        List<Contingency> contingencies,
                                                                        List<SensitivityVariableSet> variableSets,
                                                                        SensitivityAnalysisParameters parameters,
                                                                        ComputationManager computationManager,
                                                                        ReportNode reportNode) {
        return find().runAsync(network, workingVariantId, factors, contingencies, variableSets, parameters, computationManager, reportNode);
    }

    public static CompletableFuture<Void> runAsync(Network network,
                                                   String workingVariantId,
                                                   SensitivityFactorReader factorReader,
                                                   SensitivityResultWriter resultWriter,
                                                   SensitivityAnalysisRunParameters runParameters) {
        return find().runAsync(network, workingVariantId, factorReader, resultWriter, runParameters);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        String workingVariantId,
                                                                        List<SensitivityFactor> factors,
                                                                        SensitivityAnalysisRunParameters runParameters) {
        return find().runAsync(network, workingVariantId, factors, runParameters);
    }

    public static CompletableFuture<SensitivityAnalysisResult> runAsync(Network network,
                                                                        List<SensitivityFactor> factors) {
        return find().runAsync(network, factors);
    }

    /**
     * @deprecated use {@link #run(Network, String, SensitivityFactorReader, SensitivityResultWriter, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static void run(Network network,
                           String workingVariantId,
                           SensitivityFactorReader factorReader,
                           SensitivityResultWriter resultWriter,
                           List<Contingency> contingencies,
                           List<SensitivityVariableSet> variableSets,
                           SensitivityAnalysisParameters parameters,
                           ComputationManager computationManager,
                           ReportNode reportNode) {
        find().run(network, workingVariantId, factorReader, resultWriter, contingencies, variableSets, parameters, computationManager, reportNode);
    }

    /**
     * @deprecated use {@link #run(Network, String, List, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static SensitivityAnalysisResult run(Network network,
                                                String workingVariantId,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters,
                                                ComputationManager computationManager,
                                                ReportNode reportNode) {
        return find().run(network, workingVariantId, factors, contingencies, variableSets, parameters, computationManager, reportNode);
    }

    public static void run(Network network,
                           String workingVariantId,
                           SensitivityFactorReader factorReader,
                           SensitivityResultWriter resultWriter,
                           SensitivityAnalysisRunParameters runParameters) {
        find().run(network, workingVariantId, factorReader, resultWriter, runParameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                String workingVariantId,
                                                List<SensitivityFactor> factors,
                                                SensitivityAnalysisRunParameters runParameters) {
        return find().run(network, workingVariantId, factors, runParameters);
    }

    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                SensitivityAnalysisRunParameters runParameters) {
        return find().run(network, factors, runParameters);
    }

    /**
     * @deprecated use {@link #run(Network, String, List, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static SensitivityAnalysisResult run(Network network,
                                                String workingVariantId,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, workingVariantId, factors, contingencies, variableSets, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, List, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                List<SensitivityVariableSet> variableSets,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factors, contingencies, variableSets, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, List, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
    public static SensitivityAnalysisResult run(Network network,
                                                List<SensitivityFactor> factors,
                                                List<Contingency> contingencies,
                                                SensitivityAnalysisParameters parameters) {
        return find().run(network, factors, contingencies, parameters);
    }

    /**
     * @deprecated use {@link #run(Network, List, SensitivityAnalysisRunParameters)} instead
     */
    @Deprecated(since = "6.9.0", forRemoval = true)
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
