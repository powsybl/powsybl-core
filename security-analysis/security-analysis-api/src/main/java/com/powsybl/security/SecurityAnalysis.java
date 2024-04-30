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
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.action.Action;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.Collections;
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
                                                                  SecurityAnalysisParameters parameters,
                                                                  ComputationManager computationManager,
                                                                  LimitViolationFilter filter,
                                                                  List<SecurityAnalysisInterceptor> interceptors,
                                                                  List<OperatorStrategy> operatorStrategies,
                                                                  List<Action> actions,
                                                                  List<StateMonitor> monitors,
                                                                  List<LimitReduction> limitReductions,
                                                                  ReportNode reportNode) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            Objects.requireNonNull(filter, "LimitViolation filter should not be null");
            Objects.requireNonNull(computationManager, "ComputationManager should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            Objects.requireNonNull(parameters, "Security analysis parameters should not be null");
            Objects.requireNonNull(interceptors, "Interceptor list should not be null");
            Objects.requireNonNull(reportNode, "ReportNode should not be null");
            return provider.run(network, workingStateId, filter, computationManager, parameters, contingenciesProvider, interceptors, operatorStrategies, actions, monitors, limitReductions, reportNode);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
            return runAsync(network, network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ReportNode.NO_OP);
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
                                          ContingenciesProvider contingenciesProvider,
                                          SecurityAnalysisParameters parameters,
                                          ComputationManager computationManager,
                                          LimitViolationFilter filter,
                                          List<SecurityAnalysisInterceptor> interceptors,
                                          List<OperatorStrategy> operatorStrategies,
                                          List<Action> actions,
                                          List<StateMonitor> monitors,
                                          List<LimitReduction> limitReductions,
                                          ReportNode reportNode) {
            return runAsync(network, workingStateId, contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, monitors, limitReductions, reportNode).join();
        }

        public SecurityAnalysisReport run(Network network,
                                          String workingStateId,
                                          ContingenciesProvider contingenciesProvider,
                                          SecurityAnalysisParameters parameters,
                                          ComputationManager computationManager,
                                          LimitViolationFilter filter,
                                          List<SecurityAnalysisInterceptor> interceptors,
                                          List<OperatorStrategy> operatorStrategies,
                                          List<Action> actions,
                                          List<StateMonitor> monitors,
                                          ReportNode reportNode) {
            return runAsync(network, workingStateId, contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, monitors, Collections.emptyList(), reportNode).join();
        }

        public SecurityAnalysisReport run(Network network,
                                          String workingStateId,
                                          ContingenciesProvider contingenciesProvider,
                                          SecurityAnalysisParameters parameters,
                                          ComputationManager computationManager,
                                          LimitViolationFilter filter,
                                          List<SecurityAnalysisInterceptor> interceptors,
                                          List<OperatorStrategy> operatorStrategies,
                                          List<Action> actions) {
            return runAsync(network, workingStateId, contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, Collections.emptyList(), Collections.emptyList(), ReportNode.NO_OP).join();
        }

        public SecurityAnalysisReport run(Network network, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
            return run(network, network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
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
                                                                     ContingenciesProvider contingenciesProvider,
                                                                     SecurityAnalysisParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     LimitViolationFilter filter,
                                                                     List<SecurityAnalysisInterceptor> interceptors,
                                                                     List<OperatorStrategy> operatorStrategies,
                                                                     List<Action> actions,
                                                                     ReportNode reportNode) {
        return find().runAsync(network, workingStateId, contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, Collections.emptyList(), Collections.emptyList(), reportNode);
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
                                             ContingenciesProvider contingenciesProvider,
                                             SecurityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             LimitViolationFilter filter,
                                             List<SecurityAnalysisInterceptor> interceptors,
                                             List<OperatorStrategy> operatorStrategies,
                                             List<Action> actions) {
        return find().run(network, workingStateId, contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions);
    }

    public static SecurityAnalysisReport run(Network network,
                                             String workingStateId,
                                             ContingenciesProvider contingenciesProvider,
                                             SecurityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             LimitViolationFilter filter,
                                             List<SecurityAnalysisInterceptor> interceptors,
                                             List<OperatorStrategy> operatorStrategies,
                                             List<Action> actions,
                                             List<StateMonitor> monitors,
                                             ReportNode reportNode) {
        return find().run(network, workingStateId, contingenciesProvider, parameters, computationManager, filter, interceptors, operatorStrategies, actions, monitors, reportNode);
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
