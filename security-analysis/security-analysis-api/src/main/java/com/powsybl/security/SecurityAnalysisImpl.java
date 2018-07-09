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
import com.powsybl.iidm.network.StateManagerConstants;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.interceptors.CurrentLimitViolationInterceptor;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisImpl implements SecurityAnalysis {

    private final Network network;

    private final LimitViolationFilter filter;

    private final ComputationManager computationManager;

    private final LoadFlowFactory loadFlowFactory;

    private final List<SecurityAnalysisInterceptor> interceptors = new ArrayList<>();

    public SecurityAnalysisImpl(Network network, ComputationManager computationManager, LoadFlowFactory loadFlowFactory) {
        this(network, new LimitViolationFilter(), computationManager, loadFlowFactory);
    }

    public SecurityAnalysisImpl(Network network, LimitViolationFilter filter, ComputationManager computationManager, LoadFlowFactory loadFlowFactory) {
        this.network = Objects.requireNonNull(network);
        this.filter = Objects.requireNonNull(filter);
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

    private List<LimitViolation> checkLimits(Network network) {
        List<LimitViolation> violations = Security.checkLimits(network, 1f);

        return filter.apply(violations, network);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters securityAnalysisParameters, ContingenciesProvider contingenciesProvider) {
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(securityAnalysisParameters);
        Objects.requireNonNull(contingenciesProvider);

        LoadFlowParameters loadFlowParameters = securityAnalysisParameters.getLoadFlowParameters();

        RunningContext context = new RunningContext(network, workingStateId);

        LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);

        final LimitViolationsResult[] limitViolationsResults = new LimitViolationsResult[1];
        final List<PostContingencyResult> postContingencyResults = Collections.synchronizedList(new ArrayList<>());

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = loadFlowParameters.copy().setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        return loadFlow.run(workingStateId, loadFlowParameters) // run base load flow
                .thenComposeAsync(loadFlowResult -> {
                    network.getStateManager().setWorkingState(workingStateId);

                    limitViolationsResults[0] = new LimitViolationsResult(loadFlowResult.isOk(), new ArrayList<>());

                    CompletableFuture<Void>[] futures;

                    if (loadFlowResult.isOk()) {
                        limitViolationsResults[0].getLimitViolations().addAll(checkLimits(network));

                        interceptors.forEach(o -> o.onPreContingencyResult(context, limitViolationsResults[0]));

                        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);

                        futures = new CompletableFuture[contingencies.size()];

                        String hash = UUID.randomUUID().toString();
                        for (int i = 0; i < contingencies.size(); i++) {
                            Contingency contingency = contingencies.get(i);

                            String postContStateId = hash + "_" + contingency.getId();

                            // run one loadflow per contingency
                            futures[i] = CompletableFuture
                                    .supplyAsync(() -> {
                                        network.getStateManager().cloneState(StateManagerConstants.INITIAL_STATE_ID, postContStateId);
                                        network.getStateManager().setWorkingState(postContStateId);

                                        // apply the contingency on the network
                                        contingency.toTask().modify(network, computationManager);

                                        return null;
                                    }, computationManager.getExecutor())
                                    .thenComposeAsync(aVoid -> loadFlow.run(postContStateId, postContParameters), computationManager.getExecutor())
                                    .handleAsync((lfResult, throwable) -> {
                                        network.getStateManager().setWorkingState(postContStateId);

                                        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, lfResult.isOk(), checkLimits(network));
                                        postContingencyResults.add(postContingencyResult);

                                        interceptors.forEach(o -> o.onPostContingencyResult(context, postContingencyResult));

                                        network.getStateManager().removeState(postContStateId);

                                        return null;
                                    }, computationManager.getExecutor());
                        }
                    } else {
                        interceptors.forEach(o -> o.onPreContingencyResult(context, limitViolationsResults[0]));

                        futures = new CompletableFuture[0];
                    }

                    return CompletableFuture.allOf(futures)
                        .thenApplyAsync(aVoid -> {
                            SecurityAnalysisResult result = new SecurityAnalysisResult(limitViolationsResults[0], postContingencyResults);
                            result.setNetworkMetadata(new NetworkMetadata(network));

                            interceptors.forEach(o -> o.onSecurityAnalysisResult(context, result));

                            return result;
                        });
                }, computationManager.getExecutor());
    }
}
