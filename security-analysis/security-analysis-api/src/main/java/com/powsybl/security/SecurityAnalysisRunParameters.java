package com.powsybl.security;

import com.powsybl.action.Action;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisRunParameters {

    //TODO replace with default
    private LimitViolationDetector detector = new DefaultLimitViolationDetector();
    private LimitViolationFilter filter = LimitViolationFilter.load();
    private ComputationManager computationManager = LocalComputationManager.getDefault();
    private SecurityAnalysisParameters securityAnalysisParameters = SecurityAnalysisParameters.load();
    private List<SecurityAnalysisInterceptor> interceptors = Collections.emptyList();
    private List<OperatorStrategy> operatorStrategies = Collections.emptyList();
    private List<Action> actions = Collections.emptyList();
    private List<StateMonitor> monitors = Collections.emptyList();
    private List<LimitReduction> limitReductions = Collections.emptyList();

    public static SecurityAnalysisRunParameters getDefault() {
        return new SecurityAnalysisRunParameters();
    }

    public LimitViolationDetector getDetector() {
        return detector;
    }

    public LimitViolationFilter getFilter() {
        return filter;
    }

    public ComputationManager getComputationManager() {
        return computationManager;
    }

    public SecurityAnalysisParameters getSecurityAnalysisParameters() {
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

    public SecurityAnalysisRunParameters setDetector(LimitViolationDetector detector) {
        this.detector = detector;
        return this;
    }

    public SecurityAnalysisRunParameters setFilter(LimitViolationFilter filter) {
        this.filter = filter;
        return this;
    }

    public SecurityAnalysisRunParameters setComputationManager(ComputationManager computationManager) {
        this.computationManager = computationManager;
        return this;
    }

    public SecurityAnalysisRunParameters setSecurityAnalysisParameters(SecurityAnalysisParameters securityAnalysisParameters) {
        Objects.requireNonNull(securityAnalysisParameters, "Security analysis parameters should not be null");
        this.securityAnalysisParameters = securityAnalysisParameters;
        return this;
    }

    public SecurityAnalysisRunParameters setOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        this.operatorStrategies = operatorStrategies;
        return this;
    }

    public SecurityAnalysisRunParameters setInterceptors(List<SecurityAnalysisInterceptor> interceptors) {
        Objects.requireNonNull(interceptors, "Interceptor list should not be null");
        this.interceptors = interceptors;
        return this;
    }

    public SecurityAnalysisRunParameters setLimitReductions(List<LimitReduction> limitReductions) {
        this.limitReductions = limitReductions;
        return this;
    }

    public SecurityAnalysisRunParameters setMonitors(List<StateMonitor> monitors) {
        this.monitors = monitors;
        return this;
    }

    public SecurityAnalysisRunParameters setActions(List<Action> actions) {
        this.actions = actions;
        return this;
    }
}
