/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * API for multi short-circuit computations.
 *
 * @author Boubakeur Brahimi
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class MultiShortCircuitAnalysis {

    private static final String NOT_NULL_COMPUTATION_MANAGER_MESSAGE = "ComputationManager should not be null";
    private static final String NOT_NULL_PARAMETERS_MESSAGE = "Short circuit parameters should not be null";
    private static final String NOT_NULL_NETWORK_MESSAGE = "Network should not be null";

    private MultiShortCircuitAnalysis() {
        throw new AssertionError("Utility class should not been instantiated");
    }

    public static final class Runner implements Versionable {
        private final MultiShortCircuitAnalysisProvider provider;

        private Runner(MultiShortCircuitAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<ShortCircuitAnalysisMultiResult> runAsync(Network network,
                                                                      ShortCircuitParameters parameters,
                                                                      ComputationManager computationManager,
                                                                      Reporter reporter) {
            Objects.requireNonNull(network, NOT_NULL_NETWORK_MESSAGE);
            Objects.requireNonNull(computationManager, NOT_NULL_COMPUTATION_MANAGER_MESSAGE);
            Objects.requireNonNull(parameters, NOT_NULL_PARAMETERS_MESSAGE);
            Objects.requireNonNull(reporter, "Reporter should not be null");
            return provider.run(network, parameters, computationManager, reporter);
        }

        public CompletableFuture<ShortCircuitAnalysisMultiResult> runAsync(Network network,
                                                                      ShortCircuitParameters parameters,
                                                                      ComputationManager computationManager) {
            Objects.requireNonNull(network, NOT_NULL_NETWORK_MESSAGE);
            Objects.requireNonNull(computationManager, NOT_NULL_COMPUTATION_MANAGER_MESSAGE);
            Objects.requireNonNull(parameters, NOT_NULL_PARAMETERS_MESSAGE);
            return provider.run(network, parameters, computationManager);
        }

        public ShortCircuitAnalysisMultiResult run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager,
                                                   Reporter reporter) {
            return runAsync(network, parameters, computationManager, reporter).join();
        }

        public ShortCircuitAnalysisMultiResult run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager) {
            return runAsync(network, parameters, computationManager).join();
        }

        public ShortCircuitAnalysisMultiResult run(Network network, ShortCircuitParameters parameters) {
            return run(network, parameters, LocalComputationManager.getDefault());
        }

        public ShortCircuitAnalysisMultiResult run(Network network) {
            return run(network, ShortCircuitParameters.load());
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
                .find(name, "multi-shortcircuits-analysis", MultiShortCircuitAnalysisProvider.class,
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

    public static CompletableFuture<ShortCircuitAnalysisMultiResult> runAsync(Network network, ShortCircuitParameters parameters,
                                                                              ComputationManager computationManager) {
        return find().runAsync(network, parameters, computationManager);
    }

    public static CompletableFuture<ShortCircuitAnalysisMultiResult> runAsync(Network network, ShortCircuitParameters parameters,
                                                                              ComputationManager computationManager, Reporter reporter) {
        return find().runAsync(network, parameters, computationManager, reporter);
    }

    public static ShortCircuitAnalysisMultiResult run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager, Reporter reporter) {
        return find().run(network, parameters, computationManager, reporter);
    }

    public static ShortCircuitAnalysisMultiResult run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager) {
        return find().run(network, parameters, computationManager);
    }

    public static ShortCircuitAnalysisMultiResult run(Network network, ShortCircuitParameters parameters) {
        return find().run(network, parameters);
    }

    public static ShortCircuitAnalysisMultiResult run(Network network) {
        return find().run(network);
    }
}
