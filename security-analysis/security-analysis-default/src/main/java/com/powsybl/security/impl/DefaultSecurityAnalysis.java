/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.security.detectors.LimitViolationDetector;
import com.powsybl.security.detectors.LoadingLimitType;
import com.powsybl.security.interceptors.CurrentLimitViolationInterceptor;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.monitor.StateMonitorIndex;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResult;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class DefaultSecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSecurityAnalysis.class);

    /**
     * This executor is used to create the variants of the network, submit the tasks
     * for computing contingency loadflows and submit the tasks for checking for the
     * violations. Submitting tasks itself is blocking because we can only run a
     * limited number of loadflows in parallel because we need the memory for the
     * variant, and we don't want to submit tasks that would immediately block to
     * get an available variant (they hurt the performance of the executor who
     * excutes them)
     */
    private static final ExecutorService SCHEDULER_EXECUTOR = createThreadPool(getOptionalIntProperty("default-security-analysis", "scheduler-pool-size", 10));

    private static final int MAX_VARIANTS_PER_ANALYSIS = getOptionalIntProperty("default-security-analysis", "max-variants-per-analysis", 10);

    /**
     * Return the value of the property or the default value if the module or the property doesn't exist in the configuration.
     *
     * @param moduleName   The name of the module
     * @param propertyName The name of the property
     * @param defaultValue The default value
     * @return The value of the property if it exists, the default value otherwise
     */
    private static int getOptionalIntProperty(String moduleName, String propertyName, int defaultValue) {
        return PlatformConfig.defaultConfig()
            .getOptionalModuleConfig(moduleName)
            .map(m -> m.getOptionalIntProperty(propertyName).orElse(defaultValue))
            .orElse(defaultValue);
    }

    private static ExecutorService createThreadPool(int poolSize) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, poolSize, 1L, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }

    private final ComputationManager computationManager;
    private final Network network;
    private final LimitViolationDetector violationDetector;
    private final LimitViolationFilter violationFilter;
    private final List<SecurityAnalysisInterceptor> interceptors;
    private final StateMonitorIndex monitorIndex;
    private final ReportNode reportNode;

    public DefaultSecurityAnalysis(Network network, LimitViolationFilter filter, ComputationManager computationManager,
                                   List<StateMonitor> monitors, ReportNode reportNode) {
        this(network, null, filter, computationManager, monitors, reportNode);
    }

    public DefaultSecurityAnalysis(Network network, @Nullable LimitViolationDetector detector,
                                   LimitViolationFilter filter, ComputationManager computationManager,
                                   List<StateMonitor> monitors, ReportNode reportNode) {
        this.network = Objects.requireNonNull(network);
        this.violationDetector = detector;
        this.violationFilter = Objects.requireNonNull(filter);
        this.interceptors = new ArrayList<>();
        this.computationManager = Objects.requireNonNull(computationManager);
        this.monitorIndex = new StateMonitorIndex(monitors);
        this.reportNode = Objects.requireNonNull(reportNode);
        interceptors.add(new CurrentLimitViolationInterceptor());
    }

    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
    }

    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    private SecurityAnalysisResultBuilder createResultBuilder(String initialWorkingStateId) {
        return new SecurityAnalysisResultBuilder(violationFilter, new RunningContext(network, initialWorkingStateId), interceptors);
    }

    public CompletableFuture<SecurityAnalysisReport> run(String workingVariantId,
                                                         SecurityAnalysisParameters securityAnalysisParameters, ContingenciesProvider contingenciesProvider) {
        Objects.requireNonNull(workingVariantId);
        Objects.requireNonNull(securityAnalysisParameters);
        Objects.requireNonNull(contingenciesProvider);

        LoadFlowParameters loadFlowParameters = securityAnalysisParameters.getLoadFlowParameters();

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = loadFlowParameters.copy()
            .setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        SecurityAnalysisResultBuilder resultBuilder = createResultBuilder(workingVariantId);

        return LoadFlow
            .runAsync(network, workingVariantId, computationManager, loadFlowParameters, reportNode)
            .thenCompose(loadFlowResult -> {
                if (loadFlowResult.isOk()) {
                    return CompletableFuture
                        .runAsync(() -> {
                            network.getVariantManager().setWorkingVariant(workingVariantId);
                            setPreContingencyOkAndCheckViolations(resultBuilder);
                        }, computationManager.getExecutor())
                        .thenComposeAsync(aVoid ->
                                submitAllLoadFlows(workingVariantId, contingenciesProvider, postContParameters, resultBuilder),
                            SCHEDULER_EXECUTOR);
                } else {
                    return setPreContingencyKo(resultBuilder);
                }
            })
            .thenApply(aVoid -> new SecurityAnalysisReport(resultBuilder.build()));
    }

    private void setPreContingencyOkAndCheckViolations(SecurityAnalysisResultBuilder resultBuilder) {
        SecurityAnalysisResultBuilder.PreContingencyResultBuilder builder =
                resultBuilder.preContingency()
                        .setStatus(LoadFlowResult.ComponentResult.Status.CONVERGED);
        checkPreContingencyViolations(network, builder::addViolation);
        addMonitorInfos(network, monitorIndex.getAllStateMonitor(), builder::addBranchResult, builder::addBusResult, builder::addThreeWindingsTransformerResult);
        addMonitorInfos(network, monitorIndex.getNoneStateMonitor(), builder::addBranchResult, builder::addBusResult, builder::addThreeWindingsTransformerResult);
        builder.endPreContingency();
    }

    private CompletableFuture<Void> setPreContingencyKo(SecurityAnalysisResultBuilder resultBuilder) {
        resultBuilder.preContingency().setStatus(LoadFlowResult.ComponentResult.Status.FAILED).endPreContingency();
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> submitAllLoadFlows(String workingVariantId,
                                                       ContingenciesProvider contingenciesProvider, LoadFlowParameters postContParameters,
                                                       SecurityAnalysisResultBuilder resultBuilder) {

        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
        int workerCount = Math.min(MAX_VARIANTS_PER_ANALYSIS, Math.min(computationManager.getResourcesStatus().getAvailableCores(), contingencies.isEmpty() ? 1 : contingencies.size()));
        List<String> variantIds = makeWorkingVariantsNames(workerCount);
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(workerCount, false, variantIds);

        network.getVariantManager().allowVariantMultiThreadAccess(true);
        network.getVariantManager().cloneVariant(workingVariantId, variantIds);

        return CompletableFuture
            .allOf(contingencies.stream()
                .map(contingency -> submitOneLoadFlow(workingVariantId, contingency, postContParameters, resultBuilder, queue))
                .toArray(CompletableFuture[]::new))
            .whenComplete((aVoid, throwable) -> variantIds.forEach(network.getVariantManager()::removeVariant));
    }

    private static List<String> makeWorkingVariantsNames(int workerCount) {
        String hash = UUID.randomUUID().toString();
        return IntStream.range(0, workerCount).mapToObj(i -> hash + "_" + i).collect(Collectors.toList());
    }

    // Block for an available variant, then submit a loadflow on this variant, then
    // make the variant available again
    private CompletableFuture<Void> submitOneLoadFlow(String workingVariantId, Contingency contingency, LoadFlowParameters postContParameters,
                                                      SecurityAnalysisResultBuilder resultBuilder, BlockingQueue<String> queue) {
        return CompletableFuture.completedFuture(null).thenCompose(aaVoid -> {
            String postContVariantId = getVariantId(queue);
            return runOneLoadFlowAsync(workingVariantId, postContVariantId, postContParameters, resultBuilder, contingency)
                .whenComplete((aVoid, throwable) -> queue.add(postContVariantId));
        });
    }

    private static String getVariantId(BlockingQueue<String> queue) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptedException(e);
        }
    }

    private CompletableFuture<Void> runOneLoadFlowAsync(String workingVariantId, String postContVariantId, LoadFlowParameters postContParameters,
                                                        SecurityAnalysisResultBuilder resultBuilder, Contingency contingency) {
        return CompletableFuture
            .runAsync(() -> {
                LOGGER.debug("Worker {} run loadflow for contingency '{}'.", postContVariantId, contingency.getId());
                applyContingency(workingVariantId, postContVariantId, contingency);
            }, computationManager.getExecutor())
            .thenCompose(aVoid ->
                LoadFlow.runAsync(network, postContVariantId, computationManager, postContParameters, reportNode)
            )
            .thenApplyAsync(lfResult -> {
                setContingencyOkAndCheckViolations(postContVariantId, resultBuilder, contingency, lfResult);
                return null;
            }, computationManager.getExecutor());
    }

    private void setContingencyOkAndCheckViolations(String postContVariantId, SecurityAnalysisResultBuilder resultBuilder,
                                                    Contingency contingency, LoadFlowResult lfResult) {
        network.getVariantManager().setWorkingVariant(postContVariantId);
        SecurityAnalysisResultBuilder.PostContingencyResultBuilder builder =
                resultBuilder.contingency(contingency)
                        .setStatus(lfResult.isOk() ? PostContingencyComputationStatus.CONVERGED : PostContingencyComputationStatus.FAILED)
                        .setConnectivityResult(new ConnectivityResult(0, 0, 0.0, 0.0, Collections.emptySet()));
        if (lfResult.isOk()) {
            checkPostContingencyViolations(contingency, network, builder::addViolation);
            addMonitorInfos(network, monitorIndex.getAllStateMonitor(), builder::addBranchResult, builder::addBusResult, builder::addThreeWindingsTransformerResult);
            StateMonitor stateMonitor = monitorIndex.getSpecificStateMonitors().get(contingency.getId());
            if (stateMonitor != null) {
                addMonitorInfos(network, stateMonitor, builder::addBranchResult, builder::addBusResult, builder::addThreeWindingsTransformerResult);
            }
        }
        builder.endContingency();
    }

    private void applyContingency(String workingVariantId, String postContVariantId, Contingency contingency) {
        network.getVariantManager().cloneVariant(workingVariantId, postContVariantId, true);
        network.getVariantManager().setWorkingVariant(postContVariantId);
        contingency.toModification().apply(network, computationManager);
    }

    private void addMonitorInfos(Network network, StateMonitor monitor, Consumer<BranchResult> branchResultConsumer,
                                 Consumer<BusResult> busResultsConsumer, Consumer<ThreeWindingsTransformerResult> threeWindingsTransformerResultConsumer) {
        monitor.getBranchIds().forEach(branchId -> {
            Branch branch = network.getBranch(branchId);
            if (branch != null) {
                branchResultConsumer.accept(createBranchResult(network.getBranch(branchId)));
            }
        });
        monitor.getVoltageLevelIds().forEach(voltageLevelId -> {
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel != null) {
                voltageLevel.getBusView().getBuses().forEach(bus ->
                        busResultsConsumer.accept(createBusResult(bus, voltageLevelId)));
            }
        });
        monitor.getThreeWindingsTransformerIds().forEach(threeWindingsTransformerId -> {
            ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(threeWindingsTransformerId);
            if (twt != null) {
                threeWindingsTransformerResultConsumer
                        .accept(createThreeWindingsTransformerResult(twt));
            }
        });
    }

    private BranchResult createBranchResult(Branch branch) {
        return new BranchResult(branch.getId(), branch.getTerminal1().getP(), branch.getTerminal1().getQ(), branch.getTerminal1().getI(),
            branch.getTerminal2().getP(), branch.getTerminal2().getQ(), branch.getTerminal2().getI(), 0.0);
    }

    private BusResult createBusResult(Bus bus, String voltageLevelId) {
        return new BusResult(voltageLevelId, bus.getId(), bus.getV(), bus.getAngle());
    }

    private ThreeWindingsTransformerResult createThreeWindingsTransformerResult(ThreeWindingsTransformer threeWindingsTransformer) {
        return new ThreeWindingsTransformerResult(threeWindingsTransformer.getId(), threeWindingsTransformer.getLeg1().getTerminal().getP(),
                threeWindingsTransformer.getLeg1().getTerminal().getQ(), threeWindingsTransformer.getLeg1().getTerminal().getI(),
                threeWindingsTransformer.getLeg2().getTerminal().getP(), threeWindingsTransformer.getLeg2().getTerminal().getQ(), threeWindingsTransformer.getLeg2().getTerminal().getI(),
                threeWindingsTransformer.getLeg3().getTerminal().getP(), threeWindingsTransformer.getLeg3().getTerminal().getQ(), threeWindingsTransformer.getLeg3().getTerminal().getI());
    }

    private void checkPreContingencyViolations(Network network, Consumer<LimitViolation> consumer) {
        if (violationDetector != null) {
            violationDetector.checkAll(network, consumer);
        } else {
            LimitViolationDetection.checkAll(network, EnumSet.allOf(LoadingLimitType.class), 1., consumer);
        }
    }

    protected void checkPostContingencyViolations(Contingency contingency, Network network, Consumer<LimitViolation> consumer) {
        if (violationDetector != null) {
            violationDetector.checkAll(contingency, network, consumer);
        } else {
            // TODO For now, contingencies are ignored in the default security analysis
            //  (same behavior as using the default LimitViolationDetector)
            //  This should change to fully support LimitReductions
            LimitViolationDetection.checkAll(network, EnumSet.allOf(LoadingLimitType.class), 1., consumer);
        }
    }
}
