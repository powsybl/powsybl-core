/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.contingency.Contingency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisImpl implements SecurityAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisImpl.class);

    private final Network network;
    private final ComputationManager computationManager;
    private final LoadFlowFactory loadFlowFactory;

    public SecurityAnalysisImpl(Network network, ComputationManager computationManager, LoadFlowFactory loadFlowFactory) {
        this.network = Objects.requireNonNull(network);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.loadFlowFactory = Objects.requireNonNull(loadFlowFactory);
    }

    private static List<LimitViolation> checkLimits(Network network) {
        return Security.checkLimits(network, Security.CurrentLimitType.TATL, 1f);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId, LoadFlowParameters parameters) {
        Objects.requireNonNull(contingenciesProvider);
        Objects.requireNonNull(workingStateId);
        Objects.requireNonNull(parameters);

        LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);

        final boolean[] preContingencyComputationOk = new boolean[1];
        final List<LimitViolation> preContingencyLimitViolations = new ArrayList<>();
        final List<PostContingencyResult> postContingencyResults = Collections.synchronizedList(new ArrayList<>());

        // start post contingency LF from pre-contingency state variables
        LoadFlowParameters postContParameters = parameters.clone().setVoltageInitMode(LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES);

        return loadFlow.runAsync(workingStateId, parameters) // run base load flow
                .thenComposeAsync(loadFlowResult -> {
                    network.getStateManager().setWorkingState(workingStateId);

                    preContingencyComputationOk[0] = loadFlowResult.isOk();
                    preContingencyLimitViolations.addAll(checkLimits(network));

                    CompletableFuture<Void>[] futures;

                    if (loadFlowResult.isOk()) {
                        List<Contingency> contingencies = contingenciesProvider.getContingencies(network);

                        futures = new CompletableFuture[contingencies.size()];

                        String hash = UUID.randomUUID().toString();
                        for (int i = 0; i < contingencies.size(); i++) {
                            Contingency contingency = contingencies.get(i);

                            String postContStateId = hash + "_" + contingency.getId();

                            // run one loadflow per contingency
                            futures[i] = CompletableFuture
                                    .supplyAsync(new Supplier<Void>() {
                                        @Override
                                        public Void get() {
                                            network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, postContStateId);
                                            network.getStateManager().setWorkingState(postContStateId);

                                            // apply the contingency on the network
                                            contingency.toTask().modify(network);

                                            return null;
                                        }
                                    }, computationManager.getExecutor())
                                    .thenComposeAsync(aVoid -> loadFlow.runAsync(postContStateId, postContParameters), computationManager.getExecutor())
                                    .handleAsync(new BiFunction<LoadFlowResult, Throwable, Void>() {
                                        @Override
                                        public Void apply(LoadFlowResult loadFlowResult, Throwable throwable) {
                                            network.getStateManager().setWorkingState(postContStateId);

                                            postContingencyResults.add(new PostContingencyResult(contingency,
                                                                                                    loadFlowResult.isOk(),
                                                                                                    checkLimits(network)));

                                            network.getStateManager().removeState(postContStateId);

                                            return null;
                                        }
                                    }, computationManager.getExecutor());
                        }
                    }
                    else {
                        futures = new CompletableFuture[0];
                    }

                    return CompletableFuture.allOf(futures)
                            .thenApplyAsync(aVoid -> new SecurityAnalysisResult(new PreContingencyResult(preContingencyComputationOk[0], preContingencyLimitViolations), postContingencyResults));
                }, computationManager.getExecutor());
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId) {
        return runAsync(contingenciesProvider, workingStateId, LoadFlowParameters.load());
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider) {
        return runAsync(contingenciesProvider, StateManager.INITIAL_STATE_ID);
    }

}
