/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.computation.local.LocalComputationManagerFactory;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.EmptyContingencyListProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * Sensitivity analysis main API. It is a utility class (so with only static methods) used as an entry point for running
 * a sensitivity analysis allowing to choose either a specific implementation or just to rely on the default one.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public final class SecurityAnalysis2 {

    private SecurityAnalysis2() {
        throw new AssertionError("Utility class should not been instantiated");
    }

    /**
     * A sensitivity analysis runner is responsible for providing convenient methods on top of {@link SecurityAnalysisProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final SecurityAnalysisProvider provider;

        private Runner(SecurityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<SecurityAnalysisResult> runAsync(Network network,
                                                                  String workingStateId,
                                                                  LimitViolationDetector detector,
                                                                  LimitViolationFilter filter,
                                                                  ComputationManager computationManager,
                                                                  SecurityAnalysisParameters parameters,
                                                                  ContingenciesProvider contingenciesProvider,
                                                                  List<SecurityAnalysisInterceptor> interceptors) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            Objects.requireNonNull(detector, "LimitViolation detector should not be null");
            Objects.requireNonNull(filter, "LimitViolation filter should not be null");
            Objects.requireNonNull(computationManager, "ComputationManager should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(interceptors, "Interceptor list should not be null");
            return provider.run(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
        }

        public CompletableFuture<SecurityAnalysisResult> runAsync(Network network, LimitViolationFilter filter,
                                                                  ComputationManager computationManager) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), new DefaultLimitViolationDetector(), filter, computationManager, SecurityAnalysisParameters.load(), new EmptyContingencyListProvider(), Collections.emptyList());
        }

        public CompletableFuture<SecurityAnalysisResult> runAsync(Network network, ComputationManager computationManager) {
            return runAsync(network, LimitViolationFilter.load(), computationManager);
        }

        public CompletableFuture<SecurityAnalysisResult> runAsync(Network network) {
            return runAsync(network, new LocalComputationManagerFactory().create());
        }

        public CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network,
                                                                                String workingStateId,
                                                                                LimitViolationDetector detector,
                                                                                LimitViolationFilter filter,
                                                                                ComputationManager computationManager,
                                                                                SecurityAnalysisParameters parameters,
                                                                                ContingenciesProvider contingenciesProvider,
                                                                                List<SecurityAnalysisInterceptor> interceptors) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            Objects.requireNonNull(detector, "LimitViolation detector should not be null");
            Objects.requireNonNull(filter, "LimitViolation filter should not be null");
            Objects.requireNonNull(computationManager, "ComputationManager should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            Objects.requireNonNull(parameters, "Sensitivity analysis parameters should not be null");
            Objects.requireNonNull(interceptors, "Interceptor list should not be null");
            return provider.runWithLog(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
        }

        public CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network, LimitViolationFilter filter,
                                                                                ComputationManager computationManager) {
            return runAsyncWithLog(network, network.getVariantManager().getWorkingVariantId(), new DefaultLimitViolationDetector(), filter, computationManager, SecurityAnalysisParameters.load(), new EmptyContingencyListProvider(), Collections.emptyList());
        }

        public CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network, ComputationManager computationManager) {
            return runAsyncWithLog(network, LimitViolationFilter.load(), computationManager);
        }

        public CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network) {
            return runAsyncWithLog(network, new LocalComputationManagerFactory().create());
        }

        public SecurityAnalysisResult run(Network network,
                                          String workingStateId,
                                          LimitViolationDetector detector,
                                          LimitViolationFilter filter,
                                          ComputationManager computationManager,
                                          SecurityAnalysisParameters parameters,
                                          ContingenciesProvider contingenciesProvider,
                                          List<SecurityAnalysisInterceptor> interceptors) {
            return runAsync(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors).join();
        }

        public SecurityAnalysisResult run(Network network, LimitViolationFilter filter,
                                          ComputationManager computationManager) {
            return run(network, network.getVariantManager().getWorkingVariantId(), new DefaultLimitViolationDetector(), filter, computationManager, SecurityAnalysisParameters.load(), new EmptyContingencyListProvider(), Collections.emptyList());
        }

        public SecurityAnalysisResult run(Network network, ComputationManager computationManager) {
            return run(network, LimitViolationFilter.load(), computationManager);
        }

        public SecurityAnalysisResult run(Network network) {
            return run(network, new LocalComputationManagerFactory().create());
        }

        public SecurityAnalysisResultWithLog runWithLog(Network network,
                                                        String workingStateId,
                                                        LimitViolationDetector detector,
                                                        LimitViolationFilter filter,
                                                        ComputationManager computationManager,
                                                        SecurityAnalysisParameters parameters,
                                                        ContingenciesProvider contingenciesProvider,
                                                        List<SecurityAnalysisInterceptor> interceptors) {
            return runAsyncWithLog(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors).join();
        }

        public SecurityAnalysisResultWithLog runWithLog(Network network, LimitViolationFilter filter,
                                                        ComputationManager computationManager) {
            return runWithLog(network, network.getVariantManager().getWorkingVariantId(), new DefaultLimitViolationDetector(), filter, computationManager, SecurityAnalysisParameters.load(), new EmptyContingencyListProvider(), Collections.emptyList());
        }

        public SecurityAnalysisResultWithLog runWithLog(Network network, ComputationManager computationManager) {
            return runWithLog(network, LimitViolationFilter.load(), computationManager);
        }

        public SecurityAnalysisResultWithLog runWithLog(Network network) {
            return runWithLog(network, new LocalComputationManagerFactory().create());
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
     * @throws PowsyblException in case we cannot find a default implementation
     * @return a runner for default security analysis implementation
     */
    public static Runner find() {
        return find(null);
    }

    public static CompletableFuture<SecurityAnalysisResult> runAsync(Network network,
                                                                     String workingStateId,
                                                                     LimitViolationDetector detector,
                                                                     LimitViolationFilter filter,
                                                                     ComputationManager computationManager,
                                                                     SecurityAnalysisParameters parameters,
                                                                     ContingenciesProvider contingenciesProvider,
                                                                     List<SecurityAnalysisInterceptor> interceptors) {
        return find().runAsync(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
    }

    public static CompletableFuture<SecurityAnalysisResult> runAsync(Network network, LimitViolationFilter filter,
                                                                     ComputationManager computationManager) {
        return find().runAsync(network, filter, computationManager);
    }

    public static CompletableFuture<SecurityAnalysisResult> runAsync(Network network, ComputationManager computationManager) {
        return find().runAsync(network, computationManager);
    }

    public static CompletableFuture<SecurityAnalysisResult> runAsync(Network network) {
        return find().runAsync(network);
    }

    public static CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network,
                                                                                   String workingStateId,
                                                                                   LimitViolationDetector detector,
                                                                                   LimitViolationFilter filter,
                                                                                   ComputationManager computationManager,
                                                                                   SecurityAnalysisParameters parameters,
                                                                                   ContingenciesProvider contingenciesProvider,
                                                                                   List<SecurityAnalysisInterceptor> interceptors) {
        return find().runAsyncWithLog(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
    }

    public static CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network, LimitViolationFilter filter,
                                                                            ComputationManager computationManager) {
        return find().runAsyncWithLog(network, filter, computationManager);
    }

    public static CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network, ComputationManager computationManager) {
        return find().runAsyncWithLog(network, computationManager);
    }

    public static CompletableFuture<SecurityAnalysisResultWithLog> runAsyncWithLog(Network network) {
        return find().runAsyncWithLog(network);
    }

    public static SecurityAnalysisResult run(Network network,
                                             String workingStateId,
                                             LimitViolationDetector detector,
                                             LimitViolationFilter filter,
                                             ComputationManager computationManager,
                                             SecurityAnalysisParameters parameters,
                                             ContingenciesProvider contingenciesProvider,
                                             List<SecurityAnalysisInterceptor> interceptors) {
        return find().run(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
    }

    public static SecurityAnalysisResult run(Network network, LimitViolationFilter filter,
                                             ComputationManager computationManager) {
        return find().run(network, filter, computationManager);
    }

    public static SecurityAnalysisResult run(Network network, ComputationManager computationManager) {
        return find().run(network, computationManager);
    }

    public static SecurityAnalysisResult run(Network network) {
        return find().run(network);
    }

    public static SecurityAnalysisResultWithLog runWithLog(Network network,
                                                           String workingStateId,
                                                           LimitViolationDetector detector,
                                                           LimitViolationFilter filter,
                                                           ComputationManager computationManager,
                                                           SecurityAnalysisParameters parameters,
                                                           ContingenciesProvider contingenciesProvider,
                                                           List<SecurityAnalysisInterceptor> interceptors) {
        return find().runWithLog(network, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors);
    }

    public static SecurityAnalysisResultWithLog runWithLog(Network network, LimitViolationFilter filter,
                                                           ComputationManager computationManager) {
        return find().runWithLog(network, filter, computationManager);
    }

    public static SecurityAnalysisResultWithLog runWithLog(Network network, ComputationManager computationManager) {
        return find().runWithLog(network, computationManager);
    }

    public static SecurityAnalysisResultWithLog runWithLog(Network network) {
        return find().runWithLog(network);
    }
}
