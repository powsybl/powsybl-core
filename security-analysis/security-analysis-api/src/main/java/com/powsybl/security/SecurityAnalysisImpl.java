/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.CurrentLimitViolationInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisImpl extends AbstractSecurityAnalysis {

    private final ComputationManager computationManager;

    public SecurityAnalysisImpl(Network network, ComputationManager computationManager) {
        this(network, new LimitViolationFilter(), computationManager);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationFilter filter,
                                ComputationManager computationManager) {
        this(network, new DefaultLimitViolationDetector(), filter, computationManager);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                ComputationManager computationManager) {
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
    public CompletableFuture<SecurityAnalysisResult> run(String workingVariantId, SecurityAnalysisParameters securityAnalysisParameters, ContingenciesProvider contingenciesProvider) {
        Objects.requireNonNull(workingVariantId);
        Objects.requireNonNull(securityAnalysisParameters);
        Objects.requireNonNull(contingenciesProvider);

        LoadFlowParameters loadFlowParameters = securityAnalysisParameters.getLoadFlowParameters();

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = loadFlowParameters.copy().setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        network.getVariantManager().allowVariantMultiThreadAccess(true);
        int contSize = contingenciesProvider.getContingencies(network).size();
        int count = Math.min(computationManager.getResourcesStatus().getAvailableCores(), contSize);
        SecurityAnalysisResult[] results = new SecurityAnalysisResult[count];
        LoadFlowContext loadFlowContext = new LoadFlowContext(network, postContParameters, computationManager, violationDetector, workingVariantId);
        // run base load flow
        CompletableFuture<LoadFlowResult> baseCf = LoadFlow.runAsync(network, workingVariantId, computationManager, loadFlowParameters);
        return baseCf.thenComposeAsync(loadFlowResult -> CompletableFuture.supplyAsync(() -> {
            SecurityAnalysisResultBuilder precontBuilder = createResultBuilder(workingVariantId);
            if (loadFlowResult.isOk()) {
                precontBuilder.preContingency()
                        .setComputationOk(true);
                network.getVariantManager().setWorkingVariant(workingVariantId);
                violationDetector.checkAll(network, precontBuilder::addViolation);
                precontBuilder.endPreContingency();
                return precontBuilder.build().getPreContingencyResult();
            } else {
                return precontBuilder.preContingency().setComputationOk(false).build().getPreContingencyResult();
            }
        }), computationManager.getExecutor()).thenComposeAsync(limitViolationsResult -> CompletableFuture.supplyAsync(() -> {
            // allocate variants
            if (limitViolationsResult.isComputationOk()) {
                List<String> varNames = IntStream.range(0, count).mapToObj(i -> "worker_" + i).collect(Collectors.toList());
                network.getVariantManager().cloneVariant(workingVariantId, varNames);
            }
            return limitViolationsResult;
        }), computationManager.getExecutor()).thenComposeAsync(limitViolationsResult -> CompletableFuture.supplyAsync(() -> {
            if (limitViolationsResult.isComputationOk()) {
                CompletableFuture[] futures = new CompletableFuture[count];
                List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
                AtomicInteger ai = new AtomicInteger(0);
                for (int k = 0; k < count; k++) {
                    int i = k;
                    futures[k] = CompletableFuture.supplyAsync(() -> {
                        LoadFlowWorker worker = new LoadFlowWorker(i, ai, contingencies, limitViolationsResult, loadFlowContext);
                        Thread workerPerThread = new Thread(worker);
                        workerPerThread.start();
                        try {
                            workerPerThread.join();
                            results[i] = worker.getResult();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    });
                }
                return CompletableFuture.allOf(futures)
                        .thenComposeAsync(Void -> CompletableFuture.supplyAsync(() -> SecurityAnalysisResultMerger.merge(results)), computationManager.getExecutor()).join();
            } else {
                return createResultBuilder(workingVariantId).preContingency(limitViolationsResult, workingVariantId).build();
            }
        }), computationManager.getExecutor());
    }

    private static final class LoadFlowContext {

        private final Network network;
        private final LoadFlowParameters postContParameters;
        private final ComputationManager computationManager;
        private final LimitViolationDetector violationDetector;
        private final String workingVariantId;

        LoadFlowContext(Network network, LoadFlowParameters postContParameters, ComputationManager computationManager,
                        LimitViolationDetector violationDetector, String workingVariantId) {
            this.network = network;
            this.postContParameters = postContParameters;
            this.computationManager = computationManager;
            this.violationDetector = violationDetector;
            this.workingVariantId = workingVariantId;
        }

        Network getNetwork() {
            return network;
        }

        LoadFlowParameters getPostContParameters() {
            return postContParameters;
        }

        ComputationManager getComputationManager() {
            return computationManager;
        }

        LimitViolationDetector getViolationDetector() {
            return violationDetector;
        }

        String getWorkingVariantId() {
            return workingVariantId;
        }
    }

    private class LoadFlowWorker implements Runnable {

        private final AtomicInteger ai;
        private final List<Contingency> contingencies;
        private final int size;
        // context
        private final Network network;
        private final LoadFlowParameters postContParameters;
        private final ComputationManager computationManager;
        private final LimitViolationDetector violationDetector;
        private final String workingVariantId;

        private final String variantId;

        private final SecurityAnalysisResultBuilder resultBuilder;
        SecurityAnalysisResult build;

        LoadFlowWorker(int i, AtomicInteger ai, List<Contingency> contingencies, LimitViolationsResult preResult, LoadFlowContext context) {
            variantId = "worker_" + i;
            this.ai = ai;
            this.contingencies = contingencies;
            size = contingencies.size();
            this.network = context.getNetwork();
            this.postContParameters = context.getPostContParameters();
            this.computationManager = context.getComputationManager();
            this.violationDetector = context.getViolationDetector();
            this.workingVariantId = context.getWorkingVariantId();
            resultBuilder = createResultBuilder(variantId);
            resultBuilder.preContingency(preResult, workingVariantId);
        }

        private void work() {
            int idx = ai.getAndIncrement();
            network.getVariantManager().setWorkingVariant(variantId);
            while (idx < size) {
                network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantId, true);
                Contingency contingency = contingencies.get(idx);
                // apply the contingency on the network
                contingency.toTask().modify(network, computationManager);

                LoadFlow.runAsync(network, variantId, computationManager, postContParameters)
                        .handleAsync((lfResult, exce) -> {
                            resultBuilder.contingency(contingency)
                                    .setComputationOk(lfResult.isOk());
                            violationDetector.checkAll(contingency, network, resultBuilder::addViolation);
                            resultBuilder.endContingency();
                            return null;
                        }, computationManager.getExecutor()).join();
                idx = ai.getAndIncrement();
            }
            build = resultBuilder.build();
        }

        private SecurityAnalysisResult getResult() {
            return build;
        }

        @Override
        public void run() {
            work();
        }
    }
}
