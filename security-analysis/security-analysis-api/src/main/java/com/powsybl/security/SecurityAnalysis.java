/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Security analysis main API. It is a utility class (so with only static methods) used as an entry point for running
 * a security analysis allowing to choose either a specific implementation or just to rely on the default one.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public final class SecurityAnalysis {

    private SecurityAnalysis() {
        throw new IllegalStateException("Utility class should not been instantiated");
    }

    /**
     * A security analysis runner is responsible for providing convenient methods on top of {@link SecurityAnalysisProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final SecurityAnalysisProvider provider;

        public Runner(SecurityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                                                  String workingStateId,
                                                                  ContingenciesProvider contingenciesProvider,
                                                                  SecurityAnalysisRunParameters runParameters,
                                                                  ReportNode reportNode) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            //TODO fix
//            Objects.requireNonNull(detector, "LimitViolation detector should not be null");
//            Objects.requireNonNull(filter, "LimitViolation filter should not be null");
//            Objects.requireNonNull(computationManager, "ComputationManager should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            Objects.requireNonNull(runParameters, "SecurityAnalysisRunParameters should not be null");
            Objects.requireNonNull(reportNode, "ReportNode should not be null");
            return provider.run(network, workingStateId, contingenciesProvider, runParameters, reportNode);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies, SecurityAnalysisRunParameters runParameters) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), n -> contingencies, runParameters, ReportNode.NO_OP);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies) {
            return runAsync(network, contingencies, SecurityAnalysisRunParameters.getDefault());
        }

        public SecurityAnalysisReport run(Network network,
                                          String workingStateId,
                                          ContingenciesProvider contingenciesProvider,
                                          SecurityAnalysisRunParameters runParameters,
                                          ReportNode reportNode) {
            return runAsync(network, workingStateId, contingenciesProvider, runParameters, reportNode).join();
        }

        public SecurityAnalysisReport run(Network network, List<Contingency> contingencies, SecurityAnalysisRunParameters runParameters) {
            return run(network, network.getVariantManager().getWorkingVariantId(), n -> contingencies, runParameters, ReportNode.NO_OP);
        }

        public SecurityAnalysisReport run(Network network, List<Contingency> contingencies) {
            return run(network, contingencies, SecurityAnalysisRunParameters.getDefault());
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
     * Get a runner for security analysis implementation named {@code name}. In the case of a null {@code name}, default
     * implementation is used.
     *
     * @param name name of the security analysis implementation, null if we want to use default one
     * @return a runner for security analysis implementation named {@code name}
     */
    public static Runner find(String name) {
        return new Runner(PlatformConfigNamedProvider.Finder
            .find(name, "security-analysis", SecurityAnalysisProvider.class,
                PlatformConfig.defaultConfig()));
    }

    /**
     * Get a runner for default security analysis implementation.
     *
     * @return a runner for default security analysis implementation
     * @throws PowsyblException in case we cannot find a default implementation
     */
    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, String workingStateId,
                                      ContingenciesProvider contingenciesProvider,
                                      SecurityAnalysisRunParameters runParameters,
                                      ReportNode reportNode) {
        return find().runAsync(network, workingStateId, contingenciesProvider, runParameters, reportNode);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies,
                                                                     SecurityAnalysisRunParameters runParameters) {
        return find().runAsync(network, contingencies, runParameters);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies) {
        return find().runAsync(network, contingencies);
    }

    public static SecurityAnalysisReport run(Network network, String workingStateId,
                                             ContingenciesProvider contingenciesProvider,
                                             SecurityAnalysisRunParameters runParameters,
                                             ReportNode reportNode) {
        return find().run(network, workingStateId, contingenciesProvider, runParameters, reportNode);
    }

    public static SecurityAnalysisReport run(Network network, List<Contingency> contingencies, SecurityAnalysisRunParameters runParameters) {
        return find().run(network, contingencies, runParameters);
    }

    public static SecurityAnalysisReport run(Network network, List<Contingency> contingencies) {
        return find().run(network, contingencies);
    }
}
