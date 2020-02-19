/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.CurrentLimitViolationInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters securityAnalysisParameters, ContingenciesProvider contingenciesProvider) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(securityAnalysisParameters);
        Objects.requireNonNull(contingenciesProvider);

        LoadFlowParameters loadFlowParameters = securityAnalysisParameters.getLoadFlowParameters();

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = loadFlowParameters.copy().setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        return LoadFlow.runAsync(network, workingStateId, computationManager, loadFlowParameters) // run base load flow
                .thenComposeAsync(loadFlowResult -> {
                    network.getVariantManager().setWorkingVariant(workingStateId);

                    SecurityAnalysisResultBuilder resultBuilder = createResultBuilder(workingStateId);

                    CompletableFuture<Void> future;

                    if (loadFlowResult.isOk()) {

                        resultBuilder.preContingency()
                                .setComputationOk(true);
                        violationDetector.checkAll(network, resultBuilder::addViolation);
                        resultBuilder.endPreContingency();

                        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);

                        CompletableFuture<Void>[] futures = new CompletableFuture[contingencies.size()];

                        String hash = UUID.randomUUID().toString();
                        int workerCount = Math.min(computationManager.getResourcesStatus().getAvailableCores(), contingencies.size());
                        List<String> variantIds = IntStream.range(0, workerCount).mapToObj(i -> hash + "_" + i).collect(Collectors.toList());
                        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantIds);

                        BlockingQueue<String> queue = new ArrayBlockingQueue<>(variantIds.size(), false, variantIds);
                        boolean previousMultiThreadAcces = network.getVariantManager().isVariantMultiThreadAccessAllowed();
                        network.getVariantManager().allowVariantMultiThreadAccess(true);
                        for (int i = 0; i < contingencies.size(); i++) {
                            Contingency contingency = contingencies.get(i);

                            // run one loadflow per contingency
                            futures[i] = CompletableFuture
                                    .supplyAsync(() -> {
                                        try {
                                            return queue.take();
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                            throw new UncheckedInterruptedException(e);
                                        }
                                    }, computationManager.getExecutor())
                                    .thenCompose(postContStateId -> {
                                        return CompletableFuture
                                                .runAsync(() -> {
                                                    network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, postContStateId, true);
                                                    network.getVariantManager().setWorkingVariant(postContStateId);

                                                    // apply the contingency on the network
                                                    contingency.toTask().modify(network, computationManager);
                                                }, computationManager.getExecutor())
                                                .thenCompose(aVoid -> LoadFlow.runAsync(network, postContStateId, computationManager, postContParameters))
                                                .handleAsync((lfResult, throwable) -> {
                                                    network.getVariantManager().setWorkingVariant(postContStateId);
                                                    synchronized (resultBuilder) {
                                                        resultBuilder.contingency(contingency)
                                                                .setComputationOk(lfResult.isOk());
                                                        violationDetector.checkAll(contingency, network, resultBuilder::addViolation);
                                                        resultBuilder.endContingency();
                                                    }
                                                    queue.add(postContStateId); //Will always work because we are putting back in the queue the id we took
                                                    return null;
                                                }, computationManager.getExecutor());
                                    });
                        }
                        future = CompletableFuture.allOf(futures).whenComplete((aVoid, throwable) -> {
                            //We clean up after the computation.
                            //Note that this is only executed when all the futures complete (normally or exceptionally),
                            //so it will fail if the exception is generated in the few lines outside of the future.
                            variantIds.stream().forEach(network.getVariantManager()::removeVariant);
                            network.getVariantManager().allowVariantMultiThreadAccess(previousMultiThreadAcces);
                        });
                    } else {
                        resultBuilder.preContingency()
                                .setComputationOk(false)
                                .endPreContingency();
                        future = CompletableFuture.completedFuture(null);
                    }

                    return future
                        .thenApply(aVoid -> {
                            network.getVariantManager().setWorkingVariant(workingStateId);
                            return resultBuilder.build();
                        });
                }, computationManager.getExecutor());
    }
}
