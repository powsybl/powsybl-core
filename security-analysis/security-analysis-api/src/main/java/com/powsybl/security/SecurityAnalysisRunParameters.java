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

    private LimitViolationDetector detector;
    private LimitViolationFilter filter;
    private ComputationManager computationManager;
    private SecurityAnalysisParameters securityAnalysisParameters;
    private List<SecurityAnalysisInterceptor> interceptors = Collections.emptyList();
    private List<OperatorStrategy> operatorStrategies = Collections.emptyList();
    private List<Action> actions = Collections.emptyList();
    private List<StateMonitor> monitors = Collections.emptyList();
    private List<LimitReduction> limitReductions = Collections.emptyList();

    public static SecurityAnalysisRunParameters getDefault() {
        return new SecurityAnalysisRunParameters()
                .setDetector(new DefaultLimitViolationDetector())
                .setFilter(LimitViolationFilter.load())
                .setSecurityAnalysisParameters(SecurityAnalysisParameters.load())
                .setComputationManager(LocalComputationManager.getDefault());
    }

    public LimitViolationDetector getDetector() {
        return Objects.requireNonNullElseGet(detector, () -> detector = new DefaultLimitViolationDetector());
    }

    public LimitViolationFilter getFilter() {
        return Objects.requireNonNullElseGet(filter, () -> filter = LimitViolationFilter.load());
    }

    public ComputationManager getComputationManager() {
        return Objects.requireNonNullElseGet(computationManager, () -> computationManager = LocalComputationManager.getDefault());
    }

    public SecurityAnalysisParameters getSecurityAnalysisParameters() {
        return Objects.requireNonNullElseGet(securityAnalysisParameters, () -> securityAnalysisParameters = SecurityAnalysisParameters.load());
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
        Objects.requireNonNull(detector, "LimitViolationDetector should not be null");
        this.detector = detector;
        return this;
    }

    public SecurityAnalysisRunParameters setFilter(LimitViolationFilter filter) {
        Objects.requireNonNull(filter, "LimitViolationFilter should not be null");
        this.filter = filter;
        return this;
    }

    public SecurityAnalysisRunParameters setComputationManager(ComputationManager computationManager) {
        Objects.requireNonNull(computationManager, "ComputationManager should not be null");
        this.computationManager = computationManager;
        return this;
    }

    public SecurityAnalysisRunParameters setSecurityAnalysisParameters(SecurityAnalysisParameters securityAnalysisParameters) {
        Objects.requireNonNull(securityAnalysisParameters, "Security analysis parameters should not be null");
        this.securityAnalysisParameters = securityAnalysisParameters;
        return this;
    }

    public SecurityAnalysisRunParameters setOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        Objects.requireNonNull(operatorStrategies, "OperatorStrategy list should not be null");
        this.operatorStrategies = operatorStrategies;
        return this;
    }

    public SecurityAnalysisRunParameters setInterceptors(List<SecurityAnalysisInterceptor> interceptors) {
        Objects.requireNonNull(interceptors, "Interceptor list should not be null");
        this.interceptors = interceptors;
        return this;
    }

    public SecurityAnalysisRunParameters setLimitReductions(List<LimitReduction> limitReductions) {
        Objects.requireNonNull(limitReductions, "LimitReductions list should not be null");
        this.limitReductions = limitReductions;
        return this;
    }

    public SecurityAnalysisRunParameters setMonitors(List<StateMonitor> monitors) {
        Objects.requireNonNull(monitors, "StateMonitor list should not be null");
        this.monitors = monitors;
        return this;
    }

    public SecurityAnalysisRunParameters setActions(List<Action> actions) {
        Objects.requireNonNull(actions, "Action list should not be null");
        this.actions = actions;
        return this;
    }
}
