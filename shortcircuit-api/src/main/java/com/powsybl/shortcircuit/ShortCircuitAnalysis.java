/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * API for elementary short-circuit computations.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public final class ShortCircuitAnalysis {

    private static final String NOT_NULL_COMPUTATION_MANAGER_MESSAGE = "ComputationManager should not be null";
    private static final String NOT_NULL_PARAMETERS_MESSAGE = "Short circuit parameters should not be null";
    private static final String NOT_NULL_NETWORK_MESSAGE = "Network should not be null";
    private static final String NOT_NULL_FAULT_MESSAGE = "Fault characteristics should not be null";

    private ShortCircuitAnalysis() {
        throw new IllegalStateException("Utility class should not been instantiated");
    }

    public static final class Runner implements Versionable {
        private final ShortCircuitAnalysisProvider provider;

        private Runner(ShortCircuitAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<ShortCircuitAnalysisResult> runAsync(Network network,
                                                                      List<Fault> faults,
                                                                      ShortCircuitParameters parameters,
                                                                      ComputationManager computationManager,
                                                                      List<FaultParameters> faultParameters,
                                                                      ReportNode reportNode) {
            Objects.requireNonNull(network, NOT_NULL_NETWORK_MESSAGE);
            Objects.requireNonNull(faults, NOT_NULL_FAULT_MESSAGE);
            Objects.requireNonNull(computationManager, NOT_NULL_COMPUTATION_MANAGER_MESSAGE);
            Objects.requireNonNull(parameters, NOT_NULL_PARAMETERS_MESSAGE);
            Objects.requireNonNull(reportNode, "ReportNode should not be null");
            validateParameters(parameters);
            return provider.run(network, faults, parameters, computationManager, faultParameters, reportNode);
        }

        public CompletableFuture<ShortCircuitAnalysisResult> runAsync(Network network,
                                                                      List<Fault> faults,
                                                                      ShortCircuitParameters parameters,
                                                                      ComputationManager computationManager,
                                                                      List<FaultParameters> faultParameters) {
            Objects.requireNonNull(network, NOT_NULL_NETWORK_MESSAGE);
            Objects.requireNonNull(faults, NOT_NULL_FAULT_MESSAGE);
            Objects.requireNonNull(computationManager, NOT_NULL_COMPUTATION_MANAGER_MESSAGE);
            Objects.requireNonNull(parameters, NOT_NULL_PARAMETERS_MESSAGE);
            validateParameters(parameters);
            return provider.run(network, faults, parameters, computationManager, faultParameters);
        }

        private void validateParameters(ShortCircuitParameters shortCircuitParameters) {
            shortCircuitParameters.validate();
        }

        public ShortCircuitAnalysisResult run(Network network, List<Fault> faults,
                                              ShortCircuitParameters parameters,
                                              ComputationManager computationManager,
                                              List<FaultParameters> faultParameters,
                                              ReportNode reportNode) {
            return runAsync(network, faults, parameters, computationManager, faultParameters, reportNode).join();
        }

        public ShortCircuitAnalysisResult run(Network network, List<Fault> faults,
                                              ShortCircuitParameters parameters,
                                              ComputationManager computationManager,
                                              List<FaultParameters> faultParameters) {
            return runAsync(network, faults, parameters, computationManager, faultParameters).join();
        }

        public ShortCircuitAnalysisResult run(Network network,
                                              List<Fault> faults,
                                              ShortCircuitParameters parameters,
                                              List<FaultParameters> faultParameters) {
            return run(network, faults, parameters, LocalComputationManager.getDefault(), faultParameters);
        }

        public ShortCircuitAnalysisResult run(Network network,
                                              List<Fault> faults,
                                              List<FaultParameters> faultParameters) {
            return run(network, faults, ShortCircuitParameters.load(), faultParameters);
        }

        public ShortCircuitAnalysisResult run(Network network,
                                              List<Fault> faults) {
            return run(network, faults, ShortCircuitParameters.load(), Collections.emptyList());
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
        return new Runner(PlatformConfigNamedProvider.Finder
                .find(name, "shortcircuits-analysis", ShortCircuitAnalysisProvider.class,
                        PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default short circuit analysis implementation.
     *
     * @return a runner for default short circuit analysis implementation
     * @throws PowsyblException in case we cannot find a default implementation
     */
    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<ShortCircuitAnalysisResult> runAsync(Network network,
                                                                         List<Fault> faults,
                                                                         ShortCircuitParameters parameters,
                                                                         ComputationManager computationManager,
                                                                         List<FaultParameters> faultParameters) {
        return find().runAsync(network, faults, parameters, computationManager, faultParameters);
    }

    public static CompletableFuture<ShortCircuitAnalysisResult> runAsync(Network network,
                                                                         List<Fault> faults,
                                                                         ShortCircuitParameters parameters,
                                                                         ComputationManager computationManager,
                                                                         List<FaultParameters> faultParameters,
                                                                         ReportNode reportNode) {
        return find().runAsync(network, faults, parameters, computationManager, faultParameters, reportNode);
    }

    public static ShortCircuitAnalysisResult run(Network network,
                                                 List<Fault> faults,
                                                 ShortCircuitParameters parameters,
                                                 ComputationManager computationManager,
                                                 List<FaultParameters> faultParameters,
                                                 ReportNode reportNode) {
        return find().run(network, faults, parameters, computationManager, faultParameters, reportNode);
    }

    public static ShortCircuitAnalysisResult run(Network network,
                                                 List<Fault> faults,
                                                 ShortCircuitParameters parameters,
                                                 ComputationManager computationManager,
                                                 List<FaultParameters> faultParameters) {
        return find().run(network, faults, parameters, computationManager, faultParameters);
    }

    public static ShortCircuitAnalysisResult run(Network network, List<Fault> faults, ShortCircuitParameters parameters,
                                                 List<FaultParameters> faultParameters) {
        return find().run(network, faults, parameters, faultParameters);
    }

    public static ShortCircuitAnalysisResult run(Network network,
                                                 List<Fault> faults,
                                                 List<FaultParameters> faultParameters) {
        return find().run(network, faults, faultParameters);
    }

    public static ShortCircuitAnalysisResult run(Network network,
                                                 List<Fault> faults) {
        return find().run(network, faults, Collections.emptyList());
    }
}
