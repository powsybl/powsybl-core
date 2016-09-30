/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.merge.MergeUtil;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.offline.MetricsDb;
import eu.itesla_project.modules.offline.OfflineDb;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.rules.RuleAttributeSet;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.RulesBuildListener;
import eu.itesla_project.modules.rules.RulesBuilder;
import eu.itesla_project.simulation.SimulatorFactory;
import eu.itesla_project.modules.validation.ValidationDb;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
 */
abstract class AbstractOfflineWorkflow implements OfflineWorkflow {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractOfflineWorkflow.class);

    protected  final String id;

    protected  final OfflineWorkflowCreationParameters creationParameters;

    protected final ComputationManager computationManager;

    protected final ContingenciesAndActionsDatabaseClientFactory cadbClientFactory;

    protected final RulesBuilder rulesBuilder;

    protected final OfflineDb offlineDb;

    protected final CaseRepository caseRepository;

    protected final LoadFlowFactory loadFlowFactory;

    protected final SimulatorFactory simulatorFactory;

    protected final MergeOptimizerFactory mergeOptimizerFactory;

    protected final MetricsDb metricsDb;

    protected final ValidationDb validationDb;

    protected final ExecutorService executorService;

    protected final AtomicReference<OfflineWorkflowStatus> status;

    protected final ReentrantLock runLock = new ReentrantLock();

    protected final AtomicBoolean stopRequested = new AtomicBoolean(false);

    protected AbstractOfflineWorkflow(String id, OfflineWorkflowCreationParameters creationParameters, ComputationManager computationManager,
                                      ContingenciesAndActionsDatabaseClientFactory cadbClientFactory,
                                      RulesBuilder rulesBuilder, OfflineDb offlineDb, CaseRepository caseRepository,
                                      LoadFlowFactory loadFlowFactory,  SimulatorFactory simulatorFactory, MergeOptimizerFactory mergeOptimizerFactory,
                                      MetricsDb metricsDb, ValidationDb validationDb, ExecutorService executorService) throws IOException {
        this.id = Objects.requireNonNull(id);
        this.creationParameters = Objects.requireNonNull(creationParameters);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.cadbClientFactory = Objects.requireNonNull(cadbClientFactory);
        this.rulesBuilder = Objects.requireNonNull(rulesBuilder);
        this.offlineDb = Objects.requireNonNull(offlineDb);
        this.caseRepository = Objects.requireNonNull(caseRepository);
        this.loadFlowFactory = Objects.requireNonNull(loadFlowFactory);
        this.simulatorFactory = Objects.requireNonNull(simulatorFactory);
        this.mergeOptimizerFactory = Objects.requireNonNull(mergeOptimizerFactory);
        this.metricsDb = Objects.requireNonNull(metricsDb);
        this.validationDb = Objects.requireNonNull(validationDb);
        this.executorService = Objects.requireNonNull(executorService);
        this.status = new AtomicReference<>(new OfflineWorkflowStatus(id, OfflineWorkflowStep.IDLE, creationParameters));
        LOGGER.info("Creation parameters: {}", creationParameters);
    }

    protected static int parseSuccessPercent(Map<String, String> metrics) {
        String value = metrics.get("successPercent");
        return value != null ? Math.round(Float.parseFloat(value)) : 0;
    }

    @Override
    public String getId() {
        return id;
    }

    protected boolean isStopRequested(WorkflowStartContext context) {
        return stopRequested.get() || isTimedOut(context) || isTerminated(context);
    }

    private static boolean isTimedOut(WorkflowStartContext context) {
        return context.getStartParameters().getDuration() != -1 &&
                (System.currentTimeMillis() - context.getStartMs()) >= TimeUnit.MINUTES.toMillis(context.getStartParameters().getDuration());
    }

    private static boolean isTerminated(WorkflowStartContext context) {
        return context.getStartParameters().getMaxProcessedSamples() != -1
                && context.getProcessedSamples() > context.getStartParameters().getMaxProcessedSamples();
    }

    protected Network loadAndMergeNetwork(DateTime date, int loadFlowPriority) {
        return MergeUtil.merge(caseRepository, date, CaseType.SN, creationParameters.getCountries(), loadFlowFactory,
                loadFlowPriority, mergeOptimizerFactory, computationManager,
                creationParameters.isMergeOptimized());
    }

    protected LoadFlowParameters createLoadFlowParameters() {
        return new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.DC_VALUES,
                                      creationParameters.isLoadFlowTransformerVoltageControlOn());
    }

    protected void notifyStatusChange(OfflineWorkflowStatus status) {
    }

    protected void changeWorkflowStatus(OfflineWorkflowStatus status) {
        this.status.set(status);
        notifyStatusChange(status);
    }

    protected abstract void startImpl(WorkflowStartContext startContext) throws Exception;

    @Override
    public void start(final OfflineWorkflowStartParameters startParameters) throws Exception {
        if (runLock.tryLock()) {
            try {
                LOGGER.info("Starting offline workflow {}", id);

                changeWorkflowStatus(new OfflineWorkflowStatus(id, OfflineWorkflowStep.INITIALIZATION, creationParameters, startParameters));
                stopRequested.set(false);

                LOGGER.info("Computation manager: {}", computationManager.getVersion());

                LOGGER.info("Start parameters: {}", startParameters);

                startImpl(new WorkflowStartContext(startParameters));
            } finally {
                changeWorkflowStatus(new OfflineWorkflowStatus(id, OfflineWorkflowStep.IDLE, creationParameters));
                runLock.unlock();
                LOGGER.info("Offline workflow {} terminated", id);
            }
        }
    }

    /**
     * Stop the workflow.
     */
    @Override
    public void stop() {
        if (isRunning()) {
            LOGGER.info("Stopping offline workflow {}", id);
            stopRequested.set(true);
        }
    }

    protected void notifyRuleStorage(String workflowId, RuleId ruleId, boolean ok, float percentComplete) {
    }

    @Override
    public void computeSecurityRules() throws Exception {
        if (runLock.tryLock()) {
            try {
                LOGGER.info("Rules builder module {} {}", rulesBuilder.getName(), Objects.toString(rulesBuilder.getVersion(), ""));

                changeWorkflowStatus(new OfflineWorkflowStatus(id, OfflineWorkflowStep.SECURITY_RULES_COMPUTATION, creationParameters));

                rulesBuilder.build(id, EnumSet.of(RuleAttributeSet.MONTE_CARLO, RuleAttributeSet.WORST_CASE), new RulesBuildListener() {
                    @Override
                    public void onRule(String workflowId, RuleId ruleId, boolean ok, float percentComplete) {
                        notifyRuleStorage(workflowId, ruleId, ok, percentComplete);
                    }
                });
            } finally {
                changeWorkflowStatus(new OfflineWorkflowStatus(id, OfflineWorkflowStep.IDLE, creationParameters));
                runLock.unlock();
            }
        }
    }

    private boolean isRunning() {
        return runLock.isLocked();
    }

    @Override
    public OfflineWorkflowStatus getStatus() {
        return status.get();
    }

    @Override
    public OfflineWorkflowCreationParameters getCreationParameters() {
        return creationParameters;
    }

    @Override
    public void addListener(OfflineWorkflowListener listener) {
    }

    @Override
    public void removeListener(OfflineWorkflowListener listener) {
    }

    @Override
    public void addSynthesisListener(OfflineWorkflowSynthesisListener listener) {
    }

    @Override
    public void removeSynthesisListener(OfflineWorkflowSynthesisListener listener) {
    }

    @Override
    public void notifySynthesisListeners() {
    }
}
