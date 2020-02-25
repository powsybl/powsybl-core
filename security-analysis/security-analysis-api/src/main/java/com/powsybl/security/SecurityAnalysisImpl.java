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
                    .getOptionalModuleConfig("default-security-analysis")
                    .flatMap(m -> m.getOptionalStringProperty("scheduler-pool-size"))
                    .orElse("10")));

    private static final int MAX_VARIANTS_PER_ANALYSIS = Integer.parseInt(PlatformConfig
            .defaultConfig().getOptionalModuleConfig("default-security-analysis")
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
    public CompletableFuture<SecurityAnalysisResult> run(String workingVariantId,
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
            .runAsync(network, workingVariantId, computationManager, loadFlowParameters)
            .thenCompose(loadFlowResult -> {
                if (loadFlowResult.isOk()) {
                    return CompletableFuture
                        .runAsync(() -> {
                            network.getVariantManager().setWorkingVariant(workingVariantId);
                            setPreContigencyOkAndCheckViolations(resultBuilder);
                        }, computationManager.getExecutor())
                        .thenComposeAsync(aVoid ->
                            submitAllLoadFlows(workingVariantId, contingenciesProvider, postContParameters, resultBuilder),
                        SCHEDULER_EXECUTOR);
                } else {
                    return setPreContingencyKo(resultBuilder);
                }
            })
            .thenApply(aVoid -> resultBuilder.build());
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

    private CompletableFuture<Void> submitAllLoadFlows(String workingVariantId,
            ContingenciesProvider contingenciesProvider, LoadFlowParameters postContParameters,
            SecurityAnalysisResultBuilder resultBuilder) {

        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
        int workerCount = Math.min(MAX_VARIANTS_PER_ANALYSIS, Math.min(computationManager.getResourcesStatus().getAvailableCores(), contingencies.size()));
        List<String> variantIds = makeWorkingVariantsNames(workerCount);
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(workerCount, false, variantIds);

        network.getVariantManager().allowVariantMultiThreadAccess(true);
        network.getVariantManager().cloneVariant(workingVariantId, variantIds);

        return CompletableFuture
                .allOf(contingencies.stream()
                        .map(contingency -> submitOneLoadFlow(workingVariantId, contingency, postContParameters, resultBuilder, queue))
                        .toArray(CompletableFuture[]::new))
                .whenComplete((aVoid, throwable) -> variantIds.stream().forEach(network.getVariantManager()::removeVariant));
    }

    private static List<String> makeWorkingVariantsNames(int workerCount) {
        String hash = UUID.randomUUID().toString();
        return IntStream.range(0, workerCount).mapToObj(i -> hash + "_" + i).collect(Collectors.toList());
    }

    // Block for an available variant, then submit a loadflow on this variant, then
    // make the variant available again
    private CompletableFuture<Void> submitOneLoadFlow(String workingVariantId, Contingency contingency,
            LoadFlowParameters postContParameters, SecurityAnalysisResultBuilder resultBuilder,
            BlockingQueue<String> queue) {
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

    private CompletableFuture<Void> runOneLoadFlowAsync(String workingVariantId, String postContVariantId,
            LoadFlowParameters postContParameters, SecurityAnalysisResultBuilder resultBuilder,
            Contingency contingency) {
        return CompletableFuture
                .runAsync(() ->
                    applyContingency(workingVariantId, postContVariantId, contingency),
                computationManager.getExecutor())
                .thenCompose(aVoid -> {
                    return LoadFlow.runAsync(network, postContVariantId, computationManager, postContParameters);
                })
                .thenApplyAsync(lfResult -> {
                    setContingencyOkAndCheckViolations(postContVariantId, resultBuilder, contingency, lfResult);
                    return null;
                }, computationManager.getExecutor());
    }

    private void setContingencyOkAndCheckViolations(String postContVariantId, SecurityAnalysisResultBuilder resultBuilder,
            Contingency contingency, LoadFlowResult lfResult) {
        network.getVariantManager().setWorkingVariant(postContVariantId);
        synchronized (resultBuilder) {
            resultBuilder.contingency(contingency).setComputationOk(lfResult.isOk());
            violationDetector.checkAll(contingency, network, resultBuilder::addViolation);
            resultBuilder.endContingency();
        }
    }

    private void applyContingency(String workingVariantId, String postContVariantId, Contingency contingency) {
        network.getVariantManager().cloneVariant(workingVariantId, postContVariantId, true);
        network.getVariantManager().setWorkingVariant(postContVariantId);
        contingency.toTask().modify(network, computationManager);
    }

}
