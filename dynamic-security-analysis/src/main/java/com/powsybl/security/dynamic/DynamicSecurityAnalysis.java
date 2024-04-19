/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.powsybl.action.Action;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic Security analysis main API.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public final class DynamicSecurityAnalysis {

    private DynamicSecurityAnalysis() {
        throw new IllegalStateException("Utility class should not been instantiated");
    }

    /**
     * A dynamic security analysis runner is responsible for providing convenient methods on top of {@link DynamicSecurityAnalysisProvider}:
     * several variants of synchronous and asynchronous run with default parameters.
     */
    public static final class Runner implements Versionable {

        private final DynamicSecurityAnalysisProvider provider;

        public Runner(DynamicSecurityAnalysisProvider provider) {
            this.provider = Objects.requireNonNull(provider);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                                                  DynamicModelsSupplier dynamicModelsSupplier,
                                                                  EventModelsSupplier eventModelsSupplier,
                                                                  String workingStateId,
                                                                  ContingenciesProvider contingenciesProvider,
                                                                  DynamicSecurityAnalysisParameters parameters,
                                                                  ComputationManager computationManager,
                                                                  LimitViolationFilter filter,
                                                                  LimitViolationDetector detector,
                                                                  List<SecurityAnalysisInterceptor> interceptors,
                                                                  List<OperatorStrategy> operatorStrategies,
                                                                  List<Action> actions,
                                                                  List<StateMonitor> monitors,
                                                                  ReportNode reportNode) {
            Objects.requireNonNull(network, "Network should not be null");
            Objects.requireNonNull(dynamicModelsSupplier, "Dynamic model supplier should not be null");
            Objects.requireNonNull(eventModelsSupplier, "Event models supplier should not be null");
            Objects.requireNonNull(workingStateId, "WorkingVariantId should not be null");
            Objects.requireNonNull(detector, "LimitViolation detector should not be null");
            Objects.requireNonNull(filter, "LimitViolation filter should not be null");
            Objects.requireNonNull(computationManager, "ComputationManager should not be null");
            Objects.requireNonNull(contingenciesProvider, "Contingencies provider should not be null");
            Objects.requireNonNull(parameters, "Security analysis parameters should not be null");
            Objects.requireNonNull(interceptors, "Interceptor list should not be null");
            Objects.requireNonNull(reportNode, "ReportNode should not be null");
            return provider.run(network, dynamicModelsSupplier, eventModelsSupplier, workingStateId, detector, filter, computationManager, parameters, contingenciesProvider, interceptors, operatorStrategies, actions, monitors, reportNode);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                          DynamicModelsSupplier dynamicModelsSupplier,
                                          String workingStateId,
                                          ContingenciesProvider contingenciesProvider,
                                          DynamicSecurityAnalysisParameters parameters,
                                          ComputationManager computationManager,
                                          LimitViolationFilter filter,
                                          LimitViolationDetector detector,
                                          List<SecurityAnalysisInterceptor> interceptors,
                                          List<OperatorStrategy> operatorStrategies,
                                          List<Action> actions) {
            return runAsync(network, dynamicModelsSupplier, EventModelsSupplier.empty(), workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions, Collections.emptyList(), ReportNode.NO_OP);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
            return runAsync(network, dynamicModelsSupplier, EventModelsSupplier.empty(), network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, new DefaultLimitViolationDetector(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ReportNode.NO_OP);
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager) {
            return runAsync(network, dynamicModelsSupplier, contingenciesProvider, parameters, computationManager, LimitViolationFilter.load());
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies, DynamicSecurityAnalysisParameters parameters) {
            return runAsync(network, dynamicModelsSupplier, n -> contingencies, parameters, LocalComputationManager.getDefault());
        }

        public CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies) {
            return runAsync(network, dynamicModelsSupplier, contingencies, DynamicSecurityAnalysisParameters.load());
        }

        public SecurityAnalysisReport run(Network network,
                                          DynamicModelsSupplier dynamicModelsSupplier,
                                          EventModelsSupplier eventModelsSupplier,
                                          String workingStateId,
                                          ContingenciesProvider contingenciesProvider,
                                          DynamicSecurityAnalysisParameters parameters,
                                          ComputationManager computationManager,
                                          LimitViolationFilter filter,
                                          LimitViolationDetector detector,
                                          List<SecurityAnalysisInterceptor> interceptors,
                                          List<OperatorStrategy> operatorStrategies,
                                          List<Action> actions,
                                          List<StateMonitor> monitors,
                                          ReportNode reportNode) {
            return runAsync(network, dynamicModelsSupplier, eventModelsSupplier, workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions, monitors, reportNode).join();
        }

        public SecurityAnalysisReport run(Network network,
                                          DynamicModelsSupplier dynamicModelsSupplier,
                                          String workingStateId,
                                          ContingenciesProvider contingenciesProvider,
                                          DynamicSecurityAnalysisParameters parameters,
                                          ComputationManager computationManager,
                                          LimitViolationFilter filter,
                                          LimitViolationDetector detector,
                                          List<SecurityAnalysisInterceptor> interceptors,
                                          List<OperatorStrategy> operatorStrategies,
                                          List<Action> actions) {
            return runAsync(network, dynamicModelsSupplier, EventModelsSupplier.empty(), workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions, Collections.emptyList(), ReportNode.NO_OP).join();
        }

        public SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
            return run(network, dynamicModelsSupplier, network.getVariantManager().getWorkingVariantId(), contingenciesProvider, parameters, computationManager, filter, new DefaultLimitViolationDetector(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        public SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager) {
            return run(network, dynamicModelsSupplier, contingenciesProvider, parameters, computationManager, LimitViolationFilter.load());
        }

        public SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies, DynamicSecurityAnalysisParameters parameters) {
            return run(network, dynamicModelsSupplier, n -> contingencies, parameters, LocalComputationManager.getDefault());
        }

        public SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies) {
            return run(network, dynamicModelsSupplier, contingencies, DynamicSecurityAnalysisParameters.load());
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
            .find(name, "dynamic-security-analysis", DynamicSecurityAnalysisProvider.class,
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
                                                                     DynamicModelsSupplier dynamicModelsSupplier,
                                                                     EventModelsSupplier eventModelsSupplier,
                                                                     String workingStateId,
                                                                     ContingenciesProvider contingenciesProvider,
                                                                     DynamicSecurityAnalysisParameters parameters,
                                                                     ComputationManager computationManager,
                                                                     LimitViolationFilter filter,
                                                                     LimitViolationDetector detector,
                                                                     List<SecurityAnalysisInterceptor> interceptors,
                                                                     List<OperatorStrategy> operatorStrategies,
                                                                     List<Action> actions,
                                                                     List<StateMonitor> monitors,
                                                                     ReportNode reportNode) {
        return find().runAsync(network, dynamicModelsSupplier, eventModelsSupplier, workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions, monitors, reportNode);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network,
                                             DynamicModelsSupplier dynamicModelsSupplier,
                                             String workingStateId,
                                             ContingenciesProvider contingenciesProvider,
                                             DynamicSecurityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             LimitViolationFilter filter,
                                             LimitViolationDetector detector,
                                             List<SecurityAnalysisInterceptor> interceptors,
                                             List<OperatorStrategy> operatorStrategies,
                                             List<Action> actions) {
        return find().runAsync(network, dynamicModelsSupplier, workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider,
                                                                     DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager,
                                                                     LimitViolationFilter filter) {
        return find().runAsync(network, dynamicModelsSupplier, contingenciesProvider, parameters, computationManager, filter);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider,
                                                                     DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager) {
        return find().runAsync(network, dynamicModelsSupplier, contingenciesProvider, parameters, computationManager);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier,
                                                                     List<Contingency> contingencies, DynamicSecurityAnalysisParameters parameters) {
        return find().runAsync(network, dynamicModelsSupplier, contingencies, parameters);
    }

    public static CompletableFuture<SecurityAnalysisReport> runAsync(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies) {
        return find().runAsync(network, dynamicModelsSupplier, contingencies);
    }

    public static SecurityAnalysisReport run(Network network,
                                             DynamicModelsSupplier dynamicModelsSupplier,
                                             EventModelsSupplier eventModelsSupplier,
                                             String workingStateId,
                                             ContingenciesProvider contingenciesProvider,
                                             DynamicSecurityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             LimitViolationFilter filter,
                                             LimitViolationDetector detector,
                                             List<SecurityAnalysisInterceptor> interceptors,
                                             List<OperatorStrategy> operatorStrategies,
                                             List<Action> actions,
                                             List<StateMonitor> monitors,
                                             ReportNode reportNode) {
        return find().run(network, dynamicModelsSupplier, eventModelsSupplier, workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions, monitors, reportNode);
    }

    public static SecurityAnalysisReport run(Network network,
                                             DynamicModelsSupplier dynamicModelsSupplier,
                                             String workingStateId,
                                             ContingenciesProvider contingenciesProvider,
                                             DynamicSecurityAnalysisParameters parameters,
                                             ComputationManager computationManager,
                                             LimitViolationFilter filter,
                                             LimitViolationDetector detector,
                                             List<SecurityAnalysisInterceptor> interceptors,
                                             List<OperatorStrategy> operatorStrategies,
                                             List<Action> actions) {
        return find().run(network, dynamicModelsSupplier, workingStateId, contingenciesProvider, parameters, computationManager, filter, detector, interceptors, operatorStrategies, actions);
    }

    public static SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager, LimitViolationFilter filter) {
        return find().run(network, dynamicModelsSupplier, contingenciesProvider, parameters, computationManager, filter);
    }

    public static SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, ContingenciesProvider contingenciesProvider, DynamicSecurityAnalysisParameters parameters, ComputationManager computationManager) {
        return find().run(network, dynamicModelsSupplier, contingenciesProvider, parameters, computationManager);
    }

    public static SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies, DynamicSecurityAnalysisParameters parameters) {
        return find().run(network, dynamicModelsSupplier, contingencies, parameters);
    }

    public static SecurityAnalysisReport run(Network network, DynamicModelsSupplier dynamicModelsSupplier, List<Contingency> contingencies) {
        return find().run(network, dynamicModelsSupplier, contingencies);
    }
}
