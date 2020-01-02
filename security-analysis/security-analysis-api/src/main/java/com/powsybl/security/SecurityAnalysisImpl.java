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
import com.powsybl.loadflow.LoadFlow;
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

        network.getVariantManager().allowVariantMultiThreadAccess(true);

        return LoadFlow.runAsync(network, workingStateId, computationManager, loadFlowParameters) // run base load flow
                .thenComposeAsync(baseLfResult -> {
                    network.getVariantManager().setWorkingVariant(workingStateId);

                    List<Contingency> contingencies = contingenciesProvider.getContingencies(network);
                    SecurityAnalysisResultBuilder[] builders = new SecurityAnalysisResultBuilder[contingencies.size()];
                    for (int i = 0; i < contingencies.size(); i++) {
                        builders[i] = createResultBuilder(workingStateId);
                    }
                    CompletableFuture<Void>[] futures;

                    String hash = UUID.randomUUID().toString();
                    if (baseLfResult.isOk()) {
                        for (int i = 0; i < contingencies.size(); i++) {
                            Contingency contingency = contingencies.get(i);
                            SecurityAnalysisResultBuilder builderPerContingency = builders[i];
                            builderPerContingency.preContingency()
                                    .setComputationOk(true);
                            String postContStateId = hash + "_" + contingency.getId();
                            violationDetector.checkAll(network, builderPerContingency::addViolation);
                            builderPerContingency.endPreContingency();
                            network.getVariantManager().cloneVariant(workingStateId, postContStateId);
                            network.getVariantManager().setWorkingVariant(postContStateId);

                            // apply the contingency on the network
                            contingency.toTask().modify(network, computationManager);
                        }
                        futures = new CompletableFuture[contingencies.size()];

                        for (int i = 0; i < contingencies.size(); i++) {
                            Contingency contingency = contingencies.get(i);
                            SecurityAnalysisResultBuilder builderPerContingency = builders[i];
                            String postContStateId = hash + "_" + contingency.getId();

                            // run one loadflow per contingency
                            futures[i] = LoadFlow.runAsync(network, postContStateId, computationManager, postContParameters)
                                    .handleAsync((lfResultPerContingency, throwable) -> {
                                        network.getVariantManager().setWorkingVariant(postContStateId);
                                        builderPerContingency.contingency(contingency);
                                        builderPerContingency.setComputationOk(lfResultPerContingency.isOk());
                                        violationDetector.checkAll(contingency, network, builderPerContingency::addViolation);
                                        builderPerContingency.endContingency();
                                        network.getVariantManager().removeVariant(postContStateId);
                                        return null;
                                    }, computationManager.getExecutor());
                        }
                    } else {
                        futures = new CompletableFuture[0];
                    }

                    return CompletableFuture.allOf(futures)
                        .thenApplyAsync(aVoid -> {
                            network.getVariantManager().setWorkingVariant(workingStateId);
                            if (baseLfResult.isOk()) {
                                SecurityAnalysisResult[] securityAnalysisResults = new SecurityAnalysisResult[contingencies.size()];
                                for (int i = 0; i < contingencies.size(); i++) {
                                    SecurityAnalysisResultBuilder builderPerContingency = builders[i];
                                    securityAnalysisResults[i] = builderPerContingency.build();
                                }
                                return SecurityAnalysisResultMerger.merge(securityAnalysisResults);
                            }
                            return createResultBuilder(workingStateId).preContingency()
                                    .setComputationOk(false)
                                    .endPreContingency()
                                    .build();
                        });
                }, computationManager.getExecutor());
    }
}
