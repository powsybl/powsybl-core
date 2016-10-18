/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.offline.MetricsDb;
import eu.itesla_project.modules.offline.OfflineDb;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.rules.RulesBuilder;
import eu.itesla_project.simulation.*;
import eu.itesla_project.modules.validation.ValidationDb;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static eu.itesla_project.modules.offline.OfflineTaskStatus.FAILED;
import static eu.itesla_project.modules.offline.OfflineTaskStatus.SUCCEED;
import static eu.itesla_project.modules.offline.OfflineTaskType.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimplifiedOfflineWorkflow extends AbstractOfflineWorkflow {

    static final int LOADFLOW_PRIORITY = 0;
    static final int STABILIZATION_PRIORITY = 1;
    static final int IMPACT_ANALYSIS_PRIORITY = 2;

    public SimplifiedOfflineWorkflow(String id, OfflineWorkflowCreationParameters creationParameters, ComputationManager computationManager,
                                     ContingenciesAndActionsDatabaseClientFactory cadbClientFactory,
                                     RulesBuilder rulesBuilder, OfflineDb offlineDb, CaseRepository caseRepository,
                                     LoadFlowFactory loadFlowFactory, SimulatorFactory simulatorFactory, MergeOptimizerFactory mergeOptimizerFactory,
                                     MetricsDb metricsDb, ValidationDb validationDb, ExecutorService executorService) throws IOException {
        super(id, creationParameters, computationManager, cadbClientFactory, rulesBuilder, offlineDb, caseRepository,
                loadFlowFactory, simulatorFactory, mergeOptimizerFactory, metricsDb, validationDb, executorService);
    }

    private static String getCaseId(int caseNum) {
        return "case-" + caseNum;
    }

    private CompletableFuture<Void> createTask(DateTime date, WorkflowStartContext startContext,
                                               SimulationParameters simulationParameters,  ContingenciesAndActionsDatabaseClient cadbClient) {
        class Context {
            Network network;
            LoadFlow loadFlow;
            Stabilization stabilization;
            ImpactAnalysis impactAnalysis;
        }

        class StopException extends RuntimeException {
        }

        Context context = new Context();

        int caseNum = offlineDb.createSample(id);

        LoadFlowParameters loadFlowParameters = createLoadFlowParameters();

        return CompletableFuture
                .runAsync(() -> {
                    context.network = loadAndMergeNetwork(date, LOADFLOW_PRIORITY);
                    context.loadFlow = loadFlowFactory.create(context.network, computationManager, LOADFLOW_PRIORITY);
                    context.stabilization = simulatorFactory.createStabilization(context.network, computationManager, STABILIZATION_PRIORITY);
                    context.impactAnalysis = simulatorFactory.createImpactAnalysis(context.network, computationManager, IMPACT_ANALYSIS_PRIORITY, cadbClient);

                    LOGGER.debug("Workflow {}, case {}: loaded {}", id, caseNum, context.network.getId());

                    if (caseNum == 0) {
                        LOGGER.info("Loadflow module: {} {}", context.loadFlow.getName(), Objects.toString(context.loadFlow.getVersion(), ""));
                        LOGGER.info("Stabilization module: {} {}", context.stabilization.getName(), Objects.toString(context.stabilization.getVersion(), ""));
                        LOGGER.info("Impact analysis module: {} {}", context.impactAnalysis.getName(), Objects.toString(context.impactAnalysis.getVersion(), ""));
                    }

                    // allow multi threads access of network states
                    context.network.getStateManager().allowStateMultiThreadAccess(true);
                    context.network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, getCaseId(caseNum));
                    context.network.getStateManager().setWorkingState(getCaseId(caseNum));

                }, executorService)
                .thenComposeAsync(aVoid -> {
                    LOGGER.debug("Workflow {}, case {}: loadflow started", id, caseNum);
                    return context.loadFlow.runAsync(getCaseId(caseNum), loadFlowParameters);
                }, executorService)
                .thenAcceptAsync(loadFlowResult -> {
                    context.network.getStateManager().setWorkingState(getCaseId(caseNum));

                    LOGGER.debug("Workflow {}, case {}: loadflow terminated (ok={})",
                            id, caseNum, loadFlowResult.isOk());

                    // to memorize le link case id network id
                    metricsDb.store(id, getCaseId(caseNum), "INFO", ImmutableMap.of("networkId", context.network.getId()));

                    offlineDb.storeTaskStatus(id, caseNum, LOAD_FLOW, loadFlowResult.isOk() ? SUCCEED : FAILED, null);

                    if (!loadFlowResult.isOk()) {
                        throw new StopException();
                    }

                    offlineDb.storeState(id, caseNum, context.network, creationParameters.getAttributesCountryFilter());

                    startContext.incrementProcessedSamples();

                    try {
                        validationDb.save(context.network, OfflineWorkflow.getValidationDir(id), getCaseId(caseNum));
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }

                    Map<String, Object> simulationInitContext = new HashMap<>();
                    try {
                        context.stabilization.init(simulationParameters, simulationInitContext);
                        context.impactAnalysis.init(simulationParameters, simulationInitContext);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    LOGGER.debug("Workflow {}, case {}: stabilization started", id, caseNum);
                }, executorService)
                .thenComposeAsync(aVoid -> context.stabilization.runAsync(getCaseId(caseNum)),executorService)
                .thenApplyAsync(stabilizationResult -> {

                    LOGGER.debug("Workflow {}, case {}: stabilization terminated (status={})",
                            id, caseNum, stabilizationResult.getStatus());

                    offlineDb.storeTaskStatus(id, caseNum, STABILIZATION, stabilizationResult.getStatus() == StabilizationStatus.COMPLETED ? SUCCEED : FAILED, null);

                    metricsDb.store(id, getCaseId(caseNum), STABILIZATION.name(), stabilizationResult.getMetrics());

                    if (stabilizationResult.getStatus() != StabilizationStatus.COMPLETED) {
                        throw new StopException();
                    }

                    return stabilizationResult;
                }, executorService)
                .thenComposeAsync(stabilizationResult -> {
                    LOGGER.debug("Workflow {}, case {}: impact analysis started", id, caseNum);
                    return context.impactAnalysis.runAsync(stabilizationResult.getState(), null, null);
                }, executorService)
                .thenAcceptAsync(impactAnalysisResult -> {
                    LOGGER.debug("Workflow {}, case {}: impact analysis terminated ({}%)",
                            id, caseNum, parseSuccessPercent(impactAnalysisResult.getMetrics()));

                    offlineDb.storeTaskStatus(id, caseNum, IMPACT_ANALYSIS, SUCCEED, null);

                    metricsDb.store(id, getCaseId(caseNum), IMPACT_ANALYSIS.name(), impactAnalysisResult.getMetrics());

                    // store the security indexes in the offline db
                    long startTime = System.currentTimeMillis();
                    offlineDb.storeSecurityIndexes(id, caseNum, impactAnalysisResult.getSecurityIndexes());

                    LOGGER.debug("Workflow {}, case {}: security indexes stored in {} ms",
                            id, caseNum, (System.currentTimeMillis() - startTime));
                }, executorService)
                .exceptionally(throwable -> {
                    if (!(throwable instanceof CompletionException && throwable.getCause() instanceof StopException)) {
                        LOGGER.error(throwable.toString(), throwable);
                    }
                    return null;
                });
    }

    @Override
    protected void startImpl(WorkflowStartContext startContext) throws Exception {
        Queue<DateTime> dates = new ArrayDeque<>(caseRepository.dataAvailable(CaseType.SN, creationParameters.getCountries(), creationParameters.getHistoInterval()));

        LOGGER.debug("{} cases in the queue", dates.size());

        ContingenciesAndActionsDatabaseClient cadbClient = cadbClientFactory.create();

        SimulationParameters simulationParameters = SimulationParameters.load();
        LOGGER.info(simulationParameters.toString());

        int n = startContext.getStartParameters().getStateQueueSize();

        List<CompletableFuture<Void>> tasks = new LinkedList<>();

        try {
            while (dates.size() > 0 && !isStopRequested(startContext)) {
                if (tasks.size() > 0) {
                    CompletableFuture.anyOf(tasks.toArray(new CompletableFuture[tasks.size()]))
                            .join();
                    // remove completed tasks
                    for (Iterator<CompletableFuture<Void>> it = tasks.iterator(); it.hasNext(); ) {
                        CompletableFuture<Void> task = it.next();
                        if (task.isDone()) {
                            it.remove();
                        }
                    }
                }
                // replace completed with new ones
                for (int i = tasks.size(); i < n; i++) {
                    DateTime date = dates.poll();
                    if (date != null) {
                        tasks.add(createTask(date, startContext, simulationParameters, cadbClient));
                    }
                }
            }
        } finally {
            if (isStopRequested(startContext)) {
                LOGGER.debug("Workflow {} stopped", id);
            } else {
                if (dates.isEmpty()) {
                    LOGGER.debug("Workflow {} complete", id);
                } else {
                    LOGGER.debug("Workflow {} aborted", id);
                }
            }

            try {
                offlineDb.flush(id);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }
}
