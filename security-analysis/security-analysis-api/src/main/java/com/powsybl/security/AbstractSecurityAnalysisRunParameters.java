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
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisRunParameters<T extends AbstractSecurityAnalysisRunParameters<T>> {

    protected static final Supplier<LimitViolationDetector> DEFAULT_DETECTOR_SUPPLIER = DefaultLimitViolationDetector::new;
    protected static final Supplier<LimitViolationFilter> DEFAULT_FILTER_SUPPLIER = LimitViolationFilter::load;
    protected static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;

    private LimitViolationDetector detector;
    private LimitViolationFilter filter;
    private ComputationManager computationManager;
    private List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
    private List<OperatorStrategy> operatorStrategies = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private List<StateMonitor> monitors = new ArrayList<>();
    private ReportNode reportNode = ReportNode.NO_OP;

    /**
     * {@link LimitViolationDetector} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_DETECTOR_SUPPLIER} before returning it.
     */
    public LimitViolationDetector getDetector() {
        if (detector == null) {
            setDetector(DEFAULT_DETECTOR_SUPPLIER.get());
        }
        return detector;
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

    public ReportNode getReportNode() {
        return reportNode;
    }

    public T setDetector(LimitViolationDetector detector) {
        Objects.requireNonNull(detector, "LimitViolationDetector should not be null");
        this.detector = detector;
        return self();
    }

    public T setFilter(LimitViolationFilter filter) {
        Objects.requireNonNull(filter, "LimitViolationFilter should not be null");
        this.filter = filter;
        return self();
    }

    /**
     * Sets the computationManager handling command execution.
     */
    public T setComputationManager(ComputationManager computationManager) {
        Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        this.computationManager = computationManager;
        return self();
    }

    /**
     * Sets the list of operator strategies to apply to solve limit violations occurring after a contingency,
     * see {@link OperatorStrategy}.
     */
    public T setOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        Objects.requireNonNull(operatorStrategies, "OperatorStrategy list should not be null");
        this.operatorStrategies = operatorStrategies;
        return self();
    }

    /**
     * Sets the list of interceptors to notify at specific steps of the security analysis,
     * see {@link SecurityAnalysisInterceptor}.
     */
    public T setInterceptors(List<SecurityAnalysisInterceptor> interceptors) {
        Objects.requireNonNull(interceptors, "Interceptor list should not be null");
        this.interceptors = interceptors;
        return self();
    }

    /**
     * Sets the list of state monitors, see  {@link StateMonitor}
     */
    public T setMonitors(List<StateMonitor> monitors) {
        Objects.requireNonNull(monitors, "StateMonitor list should not be null");
        this.monitors = monitors;
        return self();
    }

    /**
     * Sets the list of actions referenced in {@link OperatorStrategy}
     */
    public T setActions(List<Action> actions) {
        Objects.requireNonNull(actions, "Action list should not be null");
        this.actions = actions;
        return self();
    }

    /**
     * Sets the reportNode used for functional logs, see {@link ReportNode}
     */
    public T setReportNode(ReportNode reportNode) {
        Objects.requireNonNull(reportNode, "ReportNode should not be null");
        this.reportNode = reportNode;
        return self();
    }

    public T addOperatorStrategy(OperatorStrategy operatorStrategy) {
        Objects.requireNonNull(operatorStrategy, "OperatorStrategy should not be null");
        operatorStrategies.add(operatorStrategy);
        return self();
    }

    public T addInterceptor(SecurityAnalysisInterceptor interceptor) {
        Objects.requireNonNull(interceptor, "Interceptor should not be null");
        interceptors.add(interceptor);
        return self();
    }

    public T addMonitor(StateMonitor monitor) {
        Objects.requireNonNull(monitor, "StateMonitor should not be null");
        monitors.add(monitor);
        return self();
    }

    public T addAction(Action action) {
        Objects.requireNonNull(action, "Action should not be null");
        actions.add(action);
        return self();
    }

    protected abstract T self();
}
