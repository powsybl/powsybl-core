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
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.CurrentLimitViolationInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisImpl extends AbstractSecurityAnalysis {

    private final ComputationManager computationManager;

    private final LoadFlowFactory loadFlowFactory;

    public SecurityAnalysisImpl(Network network, ComputationManager computationManager,
                                LoadFlowFactory loadFlowFactory) {
        this(network, new LimitViolationFilter(), computationManager, loadFlowFactory);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationFilter filter,
                                ComputationManager computationManager, LoadFlowFactory loadFlowFactory) {
        this(network, new DefaultLimitViolationDetector(), filter, computationManager, loadFlowFactory);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                ComputationManager computationManager, LoadFlowFactory loadFlowFactory) {
        super(network, detector, filter);

        this.computationManager = Objects.requireNonNull(computationManager);
        this.loadFlowFactory = Objects.requireNonNull(loadFlowFactory);

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

        LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = loadFlowParameters.copy().setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        return loadFlow.run(workingStateId, loadFlowParameters) // run base load flow
                .thenComposeAsync(loadFlowResult -> {
                    network.getVariantManager().setWorkingVariant(workingStateId);

                    SecurityAnalysisResultBuilder resultBuilder = createResultBuilder(workingStateId);

                    CompletableFuture<Void>[] futures;

                    if (loadFlowResult.isOk()) {

                        resultBuilder.preContingency()
                                .setComputationOk(true);
                        violationDetector.checkAll(network, resultBuilder::addViolation);
                        resultBuilder.endPreContingency();

                        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);

                        futures = new CompletableFuture[contingencies.size()];

                        String hash = UUID.randomUUID().toString();
                        for (int i = 0; i < contingencies.size(); i++) {
                            Contingency contingency = contingencies.get(i);

                            String postContStateId = hash + "_" + contingency.getId();

                            // run one loadflow per contingency
                            futures[i] = CompletableFuture
                                    .supplyAsync(() -> {
                                        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, postContStateId);
                                        network.getVariantManager().setWorkingVariant(postContStateId);

                                        // apply the contingency on the network
                                        contingency.toTask().modify(network, computationManager);

                                        return null;
                                    }, computationManager.getExecutor())
                                    .thenComposeAsync(aVoid -> loadFlow.run(postContStateId, postContParameters), computationManager.getExecutor())
                                    .handleAsync((lfResult, throwable) -> {
                                        network.getVariantManager().setWorkingVariant(postContStateId);

                                        resultBuilder.contingency(contingency)
                                                .setComputationOk(lfResult.isOk());
                                        violationDetector.checkAll(contingency, network, resultBuilder::addViolation);
                                        resultBuilder.endContingency();
                                        network.getVariantManager().removeVariant(postContStateId);

                                        return null;
                                    }, computationManager.getExecutor());
                        }
                    } else {
                        resultBuilder.preContingency()
                                .setComputationOk(false)
                                .endPreContingency()
                                .build();
                        futures = new CompletableFuture[0];
                    }

                    return CompletableFuture.allOf(futures)
                        .thenApplyAsync(aVoid -> {
                            network.getVariantManager().setWorkingVariant(workingStateId);
                            return resultBuilder.build();
                        });
                }, computationManager.getExecutor());
    }
}
