/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Security analysis main API. It is a utility class (so with only static methods) used as an entry point for running
 * a security analysis allowing to choose either a specific implementation or just to rely on the default one.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class SecurityAnalysis {

    private SecurityAnalysis() {
        throw new AssertionError("Utility class should not been instantiated");
    }

    /**
     * A security analysis runner is responsible for providing convenient methods on top of {@link SecurityAnalysisProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final SecurityAnalysisProvider provider;

        private Runner(SecurityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                                                  String workingStateId,
                                                                  LimitViolationDetector detector,
                                                                  LimitViolationFilter filter,
                                                                  ComputationManager computationManager,
                                                                  SecurityAnalysisParameters parameters,
                                                                  ContingenciesProvider contingenciesProvider,
                                                                  List<SecurityAnalysisInterceptor> interceptors,
                                                                  List<StateMonitor> monitors) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            Objects.requireNonNull(detector, "LimitViolation detector should not be null");
            Objects.requireNonNull(filter, "LimitViolation filter should not be null");
            Objects.requireNonNull(computationManager, "ComputationManager should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            Objects.requireNonNull(parameters, "Security analysis parameters should not be null");
            Objects.requireNonNull(interceptors, "Interceptor list should not be null");
            return provider.run(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, monitors);
        }

        public CompletableFuture<SecurityAnalysisReport>  runAsync(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), new DefaultLimitViolationDetector(), filter, computationManager, parameters, contingenciesProvider, Collections.emptyList(), Collections.emptyList());
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager) {
            return runAsync(network, contingenciesProvider, parameters, computationManager, LimitViolationFilter.load());
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies, SecurityAnalysisParameters parameters) {
            return runAsync(network, n -> contingencies, parameters, LocalComputationManager.getDefault());
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies) {
            return runAsync(network, contingencies, SecurityAnalysisParameters.load());
        }

        public SecurityAnalysisReport run(Network network,
                                          String workingStateId,
                                          LimitViolationDetector detector,
                                          LimitViolationFilter filter,
                                          ComputationManager computationManager,
                                          SecurityAnalysisParameters parameters,
                                          ContingenciesProvider contingenciesProvider,
                                          List<SecurityAnalysisInterceptor> interceptors) {
            return runAsync(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, Collections.emptyList()).join();
        }

        public SecurityAnalysisReport run(Network network,
                                          String workingStateId,
                                          LimitViolationDetector detector,
                                          LimitViolationFilter filter,
                                          ComputationManager computationManager,
                                          SecurityAnalysisParameters parameters,
                                          ContingenciesProvider contingenciesProvider,
                                          List<SecurityAnalysisInterceptor> interceptors, List<StateMonitor> monitors) {
            return runAsync(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, monitors).join();
        }

        public SecurityAnalysisReport run(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
            return run(network, network.getVariantManager().getWorkingVariantId(), new DefaultLimitViolationDetector(), filter, computationManager, parameters, contingenciesProvider, Collections.emptyList());
        }

        public SecurityAnalysisReport run(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager) {
            return run(network, contingenciesProvider, parameters, computationManager, LimitViolationFilter.load());
        }

        public SecurityAnalysisReport run(Network network, List<Contingency> contingencies, SecurityAnalysisParameters parameters) {
            return run(network, n -> contingencies, parameters, LocalComputationManager.getDefault());
        }

        public SecurityAnalysisReport run(Network network, List<Contingency> contingencies) {
            return run(network, contingencies, SecurityAnalysisParameters.load());
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

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                                                     String workingStateId,
                                                                     LimitViolationDetector detector,
                                                                     LimitViolationFilter filter,
                                                                     ComputationManager computationManager,
                                                                     SecurityAnalysisParameters parameters,
                                                                     ContingenciesProvider contingenciesProvider,
                                                                     List<SecurityAnalysisInterceptor> interceptors) {
        return find().runAsync(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, Collections.emptyList());
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, ContingenciesProvider contingenciesProvider,
                                                                     SecurityAnalysisParameters parameters, ComputationManager computationManager,
                                                                     LimitViolationFilter filter) {
        return find().runAsync(network, contingenciesProvider, parameters, computationManager, filter);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, ContingenciesProvider contingenciesProvider,
                                                                     SecurityAnalysisParameters parameters, ComputationManager computationManager) {
        return find().runAsync(network, contingenciesProvider, parameters, computationManager);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies,
                                                                     SecurityAnalysisParameters parameters) {
        return find().runAsync(network, contingencies, parameters);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, List<Contingency> contingencies) {
        return find().runAsync(network, contingencies);
    }

    public static SecurityAnalysisReport run(Network network,
                                             String workingStateId,
                                             LimitViolationDetector detector,
                                             LimitViolationFilter filter,
                                             ComputationManager computationManager,
                                             SecurityAnalysisParameters parameters,
                                             ContingenciesProvider contingenciesProvider,
                                             List<SecurityAnalysisInterceptor> interceptors) {
        return find().run(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
    }

    public static SecurityAnalysisReport run(Network network,
                                             String workingStateId,
                                             LimitViolationDetector detector,
                                             LimitViolationFilter filter,
                                             ComputationManager computationManager,
                                             SecurityAnalysisParameters parameters,
                                             ContingenciesProvider contingenciesProvider,
                                             List<SecurityAnalysisInterceptor> interceptors, List<StateMonitor> monitors) {
        return find().run(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, monitors);
    }

    public static SecurityAnalysisReport run(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
        return find().run(network, contingenciesProvider, parameters, computationManager, filter);
    }

    public static SecurityAnalysisReport run(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager) {
        return find().run(network, contingenciesProvider, parameters, computationManager);
    }

    public static SecurityAnalysisReport run(Network network, List<Contingency> contingencies, SecurityAnalysisParameters parameters) {
        return find().run(network, contingencies, parameters);
    }

    public static SecurityAnalysisReport run(Network network, List<Contingency> contingencies) {
        return find().run(network, contingencies);
    }
}
