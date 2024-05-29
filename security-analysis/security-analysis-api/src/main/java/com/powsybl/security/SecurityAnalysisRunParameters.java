/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.action.Action;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Parameters used in {@link SecurityAnalysisProvider#run} called in {@link SecurityAnalysis} API
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisRunParameters {

    private static final Supplier<LimitViolationFilter> DEFAULT_FILTER_SUPPLIER = LimitViolationFilter::load;
    private static final Supplier<SecurityAnalysisParameters> DEFAULT_SA_PARAMETERS_SUPPLIER = SecurityAnalysisParameters::load;
    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;

    private LimitViolationFilter filter;
    private ComputationManager computationManager;
    private SecurityAnalysisParameters securityAnalysisParameters;
    private List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
    private List<OperatorStrategy> operatorStrategies = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private List<StateMonitor> monitors = new ArrayList<>();
    private List<LimitReduction> limitReductions = new ArrayList<>();
    private ReportNode reportNode = ReportNode.NO_OP;

    /**
     * Returns a {@link SecurityAnalysisRunParameters} instance with default value on each field.
     * @return the SecurityAnalysisRunParameters instance.
     */
    public static SecurityAnalysisRunParameters getDefault() {
        return new SecurityAnalysisRunParameters()
                .setFilter(DEFAULT_FILTER_SUPPLIER.get())
                .setSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get())
                .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /**
     * {@link LimitViolationFilter} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_FILTER_SUPPLIER} before returning it.
     */
    public LimitViolationFilter getFilter() {
        if (filter == null) {
            setFilter(DEFAULT_FILTER_SUPPLIER.get());
        }
        return filter;
    }

    /**
     * {@link ComputationManager} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_COMPUTATION_MANAGER_SUPPLIER} before returning it.
     */
    public ComputationManager getComputationManager() {
        if (computationManager == null) {
            setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
        }
        return computationManager;
    }

    /**
     * {@link SecurityAnalysisParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_SA_PARAMETERS_SUPPLIER} before returning it.
     */
    public SecurityAnalysisParameters getSecurityAnalysisParameters() {
        if (securityAnalysisParameters == null) {
            setSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get());
        }
        return securityAnalysisParameters;
    }

    public List<SecurityAnalysisInterceptor> getInterceptors() {
        return interceptors;
    }

    public List<OperatorStrategy> getOperatorStrategies() {
        return operatorStrategies;
    }

    public List<Action> getActions() {
        return actions;
    }

    public List<StateMonitor> getMonitors() {
        return monitors;
    }

    public List<LimitReduction> getLimitReductions() {
        return limitReductions;
    }

    public ReportNode getReportNode() {
        return reportNode;
    }

    public SecurityAnalysisRunParameters setFilter(LimitViolationFilter filter) {
        Objects.requireNonNull(filter, "LimitViolationFilter should not be null");
        this.filter = filter;
        return this;
    }

    /**
     * Sets the computationManager handling command execution.
     */
    public SecurityAnalysisRunParameters setComputationManager(ComputationManager computationManager) {
        Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        this.computationManager = computationManager;
        return this;
    }

    /**
     * Sets the security analysis parameters, see {@link SecurityAnalysisParameters}.
     */
    public SecurityAnalysisRunParameters setSecurityAnalysisParameters(SecurityAnalysisParameters securityAnalysisParameters) {
        Objects.requireNonNull(securityAnalysisParameters, "Security analysis parameters should not be null");
        this.securityAnalysisParameters = securityAnalysisParameters;
        return this;
    }

    /**
     * Sets the list of operator strategies to apply to solve limit violations occurring after a contingency,
     * see {@link OperatorStrategy}.
     */
    public SecurityAnalysisRunParameters setOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        Objects.requireNonNull(operatorStrategies, "OperatorStrategy list should not be null");
        this.operatorStrategies = operatorStrategies;
        return this;
    }

    /**
     * Sets the list of interceptors to notify at specific steps of the security analysis,
     * see {@link SecurityAnalysisInterceptor}.
     */
    public SecurityAnalysisRunParameters setInterceptors(List<SecurityAnalysisInterceptor> interceptors) {
        Objects.requireNonNull(interceptors, "Interceptor list should not be null");
        this.interceptors = interceptors;
        return this;
    }

    /**
     * Sets the list of the limit reductions to apply, see {@link LimitReduction}
     */
    public SecurityAnalysisRunParameters setLimitReductions(List<LimitReduction> limitReductions) {
        Objects.requireNonNull(limitReductions, "LimitReductions list should not be null");
        this.limitReductions = limitReductions;
        return this;
    }

    /**
     * Sets the list of state monitors, see  {@link StateMonitor}
     */
    public SecurityAnalysisRunParameters setMonitors(List<StateMonitor> monitors) {
        Objects.requireNonNull(monitors, "StateMonitor list should not be null");
        this.monitors = monitors;
        return this;
    }

    /**
     * Sets the list of actions referenced in {@link OperatorStrategy}
     */
    public SecurityAnalysisRunParameters setActions(List<Action> actions) {
        Objects.requireNonNull(actions, "Action list should not be null");
        this.actions = actions;
        return this;
    }

    /**
     * Sets the reportNode used for functional logs, see {@link ReportNode}
     */
    public SecurityAnalysisRunParameters setReportNode(ReportNode reportNode) {
        Objects.requireNonNull(reportNode, "ReportNode should not be null");
        this.reportNode = reportNode;
        return this;
    }

    public SecurityAnalysisRunParameters addOperatorStrategy(OperatorStrategy operatorStrategy) {
        Objects.requireNonNull(operatorStrategy, "OperatorStrategy should not be null");
        operatorStrategies.add(operatorStrategy);
        return this;
    }

    public SecurityAnalysisRunParameters addInterceptor(SecurityAnalysisInterceptor interceptor) {
        Objects.requireNonNull(interceptor, "Interceptor should not be null");
        interceptors.add(interceptor);
        return this;
    }

    public SecurityAnalysisRunParameters addLimitReduction(LimitReduction limitReduction) {
        Objects.requireNonNull(limitReduction, "LimitReduction should not be null");
        limitReductions.add(limitReduction);
        return this;
    }

    public SecurityAnalysisRunParameters addMonitor(StateMonitor monitor) {
        Objects.requireNonNull(monitor, "StateMonitor should not be null");
        monitors.add(monitor);
        return this;
    }

    public SecurityAnalysisRunParameters addAction(Action action) {
        Objects.requireNonNull(action, "Action should not be null");
        actions.add(action);
        return this;
    }
}
