/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.CurrentLimitViolationInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisImpl extends AbstractSecurityAnalysis {

    /**
     * This executor is used to create the variants of the network, submit the tasks
     * for computing contingency loadflows and submit the tasks for checking for the
     * violations. Submitting tasks itself is blocking because we can only run a
     * limited number of loadflows in parallel because we need the memory for the
     * variant, and we don't want to submit tasks that would immediately block to
     * get an available variant (they hurt the performance of the executor who
     * excutes them)
     */
    private static final ExecutorService SCHEDULER_EXECUTOR = Executors
            .newFixedThreadPool(Integer.parseInt(PlatformConfig.defaultConfig()
                    .getOptionalModuleConfig("security-analysis-impl")
                    .flatMap(m -> m.getOptionalStringProperty("scheduler-pool-size"))
                    .orElse("10")));

    private static final int MAX_VARIANTS_PER_ANALYSIS = Integer.parseInt(PlatformConfig
            .defaultConfig().getOptionalModuleConfig("security-analysis-impl")
            .flatMap(m -> m.getOptionalStringProperty("max-variants-per-analysis"))
            .orElse("10"));

    private final ComputationManager computationManager;

    public SecurityAnalysisImpl(Network network, ComputationManager computationManager) {
        this(network, new LimitViolationFilter(), computationManager);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationFilter filter,
                                ComputationManager computationManager) {
        this(network, new DefaultLimitViolationDetector(), filter, computationManager);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationDetector detector,
                                LimitViolationFilter filter, ComputationManager computationManager) {
        super(network, detector, filter);

        this.computationManager = Objects.requireNonNull(computationManager);

        interceptors.add(new CurrentLimitViolationInterceptor());
    }

    @Override
    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
    }

    @Override
    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters securityAnalysisParameters, ContingenciesProvider contingenciesProvider) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(securityAnalysisParameters);
        Objects.requireNonNull(contingenciesProvider);

        LoadFlowParameters loadFlowParameters = securityAnalysisParameters.getLoadFlowParameters();

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = loadFlowParameters.copy().setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        SecurityAnalysisResultBuilder resultBuilder = createResultBuilder(workingStateId);

        return LoadFlow
                .runAsync(network, workingStateId, computationManager, loadFlowParameters)
                .thenCompose(loadFlowResult -> {
                    if (loadFlowResult.isOk()) {
                        return CompletableFuture
                                .runAsync(() -> {
                                    network.getVariantManager().setWorkingVariant(workingStateId);
                                    setPreContigencyOkAndCheckViolations(resultBuilder);
                                }, computationManager.getExecutor())
                                .thenComposeAsync(
                                    aVoid -> submitAllLoadFlows(workingStateId, contingenciesProvider, postContParameters, resultBuilder),
                                    SCHEDULER_EXECUTOR);
                    } else {
                        return setPreContingencyKo(resultBuilder);
                    }
                }).thenApply(aVoid -> resultBuilder.build());
    }

    private void setPreContigencyOkAndCheckViolations(SecurityAnalysisResultBuilder resultBuilder) {
        resultBuilder.preContingency().setComputationOk(true);
        violationDetector.checkAll(network, resultBuilder::addViolation);
        resultBuilder.endPreContingency();
    }

    private CompletableFuture<Void> setPreContingencyKo(SecurityAnalysisResultBuilder resultBuilder) {
        resultBuilder.preContingency().setComputationOk(false).endPreContingency();
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> submitAllLoadFlows(String workingStateId,
            ContingenciesProvider contingenciesProvider,
            LoadFlowParameters postContParameters,
            SecurityAnalysisResultBuilder resultBuilder) {
        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
        List<String> variantIds = makeWorkingVariantsNames(contingencies.size());
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(variantIds.size(), false, variantIds);
        network.getVariantManager().cloneVariant(workingStateId, variantIds);
        return CompletableFuture.completedFuture(null).thenCompose(aaVoid -> {
            boolean previousMultiThreadAcces = network.getVariantManager()
                    .isVariantMultiThreadAccessAllowed();
            network.getVariantManager().allowVariantMultiThreadAccess(true);
            return CompletableFuture
                    .allOf(contingencies.stream()
                            .map(contingency -> submitOneLoadFlow(workingStateId,
                                    contingency, postContParameters, resultBuilder,
                                    queue))
                            .toArray(CompletableFuture[]::new))
                    .whenComplete((aVoid, throwable) -> network.getVariantManager()
                            .allowVariantMultiThreadAccess(previousMultiThreadAcces));
        }).whenComplete((aVoid, throwable) ->
            variantIds.stream().forEach(network.getVariantManager()::removeVariant));
    }

    private List<String> makeWorkingVariantsNames(int contingencySize) {
        String hash = UUID.randomUUID().toString();
        int workerCount = Math.min(MAX_VARIANTS_PER_ANALYSIS,
                Math.min(computationManager.getResourcesStatus().getAvailableCores(),
                        contingencySize));
        return IntStream.range(0, workerCount)
                .mapToObj(i -> hash + "_" + i).collect(Collectors.toList());
    }

    // Block for an available variant, then submit a loadflow on this variant, then
    // make the variant available again
    private CompletableFuture<Void> submitOneLoadFlow(String workingStateId,
            Contingency contingency, LoadFlowParameters postContParameters,
            SecurityAnalysisResultBuilder resultBuilder, BlockingQueue<String> queue) {
        return CompletableFuture.completedFuture(null).thenCompose(aaVoid -> {
            String postContStateId;
            try {
                postContStateId = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedInterruptedException(e);
            }
            return runOneLoadFlowAsync(workingStateId, postContStateId, postContParameters, resultBuilder, contingency)
                    .whenComplete((aVoid, throwable) -> queue.add(postContStateId));
        });
    }

    private CompletableFuture<Void> runOneLoadFlowAsync(String workingStateId,
            String postContStateId, LoadFlowParameters postContParameters,
            SecurityAnalysisResultBuilder resultBuilder, Contingency contingency) {
        return CompletableFuture
                .runAsync(() -> applyContingency(workingStateId, postContStateId,
                        contingency), computationManager.getExecutor())
                .thenCompose(aVoid -> LoadFlow.runAsync(network, postContStateId,
                        computationManager, postContParameters))
                .thenApplyAsync(lfResult -> {
                    setContingencyOkAndCheckViolations(postContStateId, resultBuilder,
                            contingency, lfResult);
                    return null;
                }, computationManager.getExecutor());
    }

    private void setContingencyOkAndCheckViolations(String postContStateId,
            SecurityAnalysisResultBuilder resultBuilder, Contingency contingency,
            LoadFlowResult lfResult) {
        network.getVariantManager().setWorkingVariant(postContStateId);
        synchronized (resultBuilder) {
            resultBuilder.contingency(contingency)
                    .setComputationOk(lfResult.isOk());
            violationDetector.checkAll(contingency, network, resultBuilder::addViolation);
            resultBuilder.endContingency();
        }
    }

    private void applyContingency(String workingStateId, String postContStateId,
            Contingency contingency) {
        network.getVariantManager().cloneVariant(workingStateId, postContStateId, true);
        network.getVariantManager().setWorkingVariant(postContStateId);
        contingency.toTask().modify(network, computationManager);
    }

}
