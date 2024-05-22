package com.powsybl.security;

import com.powsybl.action.Action;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
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

    private static final Supplier<LimitViolationDetector> DEFAULT_DETECTOR_SUPPLIER = DefaultLimitViolationDetector::new;
    private static final Supplier<LimitViolationFilter> DEFAULT_FILTER_SUPPLIER = LimitViolationFilter::load;
    private static final Supplier<SecurityAnalysisParameters> DEFAULT_SA_PARAMETERS_SUPPLIER = SecurityAnalysisParameters::load;
    private static final Supplier<ComputationManager> DEFAULT_COMPUTATION_MANAGER_SUPPLIER = LocalComputationManager::getDefault;

    private LimitViolationDetector detector;
    private LimitViolationFilter filter;
    /**
     * Handle command execution
     */
    private ComputationManager computationManager;
    /**
     * Specific security analysis parameters
     */
    private SecurityAnalysisParameters securityAnalysisParameters;
    /**
     * List of interceptors to notify at specific steps of the security analysis (see {@link SecurityAnalysisInterceptor} API)
     */
    private List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();
    /**
     * List of strategies to apply to solve violations occurring after a contingency
     */
    private List<OperatorStrategy> operatorStrategies = new ArrayList<>();
    /**
     * List of actions referenced in {@link #operatorStrategies}
     */
    private List<Action> actions = new ArrayList<>();
    /**
     * List of StateMonitors that define the branches, buses and threeWindingsTransformers about which information will be written after security analysis
     */
    private List<StateMonitor> monitors = new ArrayList<>();
    /**
     * List of the limit reductions to apply
     */
    private List<LimitReduction> limitReductions = new ArrayList<>();
    /**
     * The reportNode used for functional logs
     */
    private ReportNode reportNode = ReportNode.NO_OP;

    /**
     * Returns a {@link SecurityAnalysisRunParameters} instance with default value on each field
     * @return the SecurityAnalysisRunParameters instance
     */
    public static SecurityAnalysisRunParameters getDefault() {
        return new SecurityAnalysisRunParameters()
                .setDetector(DEFAULT_DETECTOR_SUPPLIER.get())
                .setFilter(DEFAULT_FILTER_SUPPLIER.get())
                .setSecurityAnalysisParameters(DEFAULT_SA_PARAMETERS_SUPPLIER.get())
                .setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
    }

    /**
     * {@link LimitViolationDetector} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_DETECTOR_SUPPLIER} before returning it
     */
    public LimitViolationDetector getDetector() {
        if (detector == null) {
            setDetector(DEFAULT_DETECTOR_SUPPLIER.get());
        }
        return detector;
    }

    /**
     * {@link LimitViolationFilter} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_FILTER_SUPPLIER} before returning it
     */
    public LimitViolationFilter getFilter() {
        if (filter == null) {
            setFilter(DEFAULT_FILTER_SUPPLIER.get());
        }
        return filter;
    }

    /**
     * {@link ComputationManager} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_COMPUTATION_MANAGER_SUPPLIER} before returning it
     */
    public ComputationManager getComputationManager() {
        if (computationManager == null) {
            setComputationManager(DEFAULT_COMPUTATION_MANAGER_SUPPLIER.get());
        }
        return computationManager;
    }

    /**
     * {@link SecurityAnalysisParameters} getter<br>
     * If null, sets the field to its default value with {@link #DEFAULT_SA_PARAMETERS_SUPPLIER} before returning it
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

    public SecurityAnalysisRunParameters setReportNode(ReportNode reportNode) {
        Objects.requireNonNull(reportNode, "ReportNode should not be null");
        this.reportNode = reportNode;
        return this;
    }
}
