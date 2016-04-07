/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.commons.Version;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modules.MergeOptimizerFactory;
import eu.itesla_project.modules.OptimizerFactory;
import eu.itesla_project.modules.cases.CaseRepository;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbClientFactory;
import eu.itesla_project.modules.offline.*;
import eu.itesla_project.modules.rules.*;
import eu.itesla_project.modules.sampling.SamplerFactory;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import eu.itesla_project.modules.simulation.SimulatorFactory;
import eu.itesla_project.modules.topo.TopologyMinerFactory;
import eu.itesla_project.modules.validation.ValidationDb;
import eu.itesla_project.offline.monitoring.BusyCoresSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static eu.itesla_project.offline.LocalOfflineApplicationMBean.Attribute.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalOfflineApplication extends NotificationBroadcasterSupport implements LocalOfflineApplicationMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalOfflineApplication.class);

    private final OfflineConfig config;

    private final ComputationManager computationManager;

    private final ScheduledExecutorService ses;

    private final ExecutorService es;

    private final OfflineDb offlineDb;

    private final HistoDbClientFactory histoDbClientFactory;

    private final DynamicDatabaseClientFactory ddbClientFactory;

    private final ContingenciesAndActionsDatabaseClientFactory cadbClientFactory;

    private final RulesDbClient rulesDb;

    private final RulesBuilder rulesBuilder;

    private final TopologyMinerFactory topologyMinerFactory;

    private final ValidationDb validationDb;

    private final CaseRepository caseRepository;

    private final SamplerFactory samplerFactory;

    private final LoadFlowFactory loadFlowFactory;

    private final OptimizerFactory optimizerFactory;

    private final SimulatorFactory simulatorFactory;

    private final MergeOptimizerFactory mergeOptimizerFactory;

    private final MetricsDb metricsDb;

    private final ScheduledFuture<?> scheduledFuture;

    private final Map<String, OfflineWorkflow> workflows = new LinkedHashMap<>();

    private final Lock workflowsLock = new ReentrantLock();

    private final BusyCoresSeries busyCoresSeries;

    private final Lock busyCoresSeriesLock = new ReentrantLock();

    private final List<OfflineApplicationListener> listeners = new CopyOnWriteArrayList<>();

    private final AtomicInteger notificationIndex = new AtomicInteger();

    public LocalOfflineApplication(OfflineConfig config,
                                   ComputationManager computationManager,
                                   String simulationDbName,
                                   String rulesDbName,
                                   String metricsDbName,
                                   ScheduledExecutorService ses,
                                   ExecutorService es)
            throws IllegalAccessException, InstantiationException, InstanceAlreadyExistsException,
                   MBeanRegistrationException, MalformedObjectNameException, NotCompliantMBeanException {
        this.config = config;
        this.computationManager = computationManager;
        this.ses = ses;
        this.es = es;

        LOGGER.info("Version: {}", Version.VERSION);

        offlineDb = config.getOfflineDbFactoryClass().newInstance().create(simulationDbName);
        histoDbClientFactory = config.getHistoDbClientFactoryClass().newInstance();
        ddbClientFactory = config.getDynamicDbClientFactoryClass().newInstance();
        cadbClientFactory = config.getContingencyDbClientFactoryClass().newInstance();
        rulesDb = config.getRulesDbClientFactoryClass().newInstance().create(rulesDbName);
        metricsDb = config.getMetricsDbFactoryClass().newInstance().create(metricsDbName);
        rulesBuilder = config.getRulesBuilderFactoryClass().newInstance().create(computationManager, offlineDb, metricsDb, rulesDb);
        topologyMinerFactory = config.getTopologyMinerFactoryClass().newInstance();
        validationDb = config.getValidationDbFactoryClass().newInstance().create();
        caseRepository = config.getCaseRepositoryFactoryClass().newInstance().create(computationManager);
        samplerFactory = config.getSamplerFactoryClass().newInstance();
        loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
        optimizerFactory = config.getOptimizerFactoryClass().newInstance();
        simulatorFactory = config.getSimulatorFactoryClass().newInstance();
        mergeOptimizerFactory = config.getMergeOptimizerFactoryClass().newInstance();

        busyCoresSeries = new BusyCoresSeries(computationManager.getResourcesStatus().getAvailableCores());

        // create and register offline application mbean
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName(BEAN_NAME));

        scheduledFuture = ses.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    busyCoresSeriesLock.lock();
                    try {
                        busyCoresSeries.addValue(new BusyCoresSeries.Value(LocalOfflineApplication.this.computationManager.getResourcesStatus().getBusyCores()));
                    } finally {
                        busyCoresSeriesLock.unlock();
                    }

                    notifyBusyCoresSeriesChange();

                } catch (Throwable t) {
                    LOGGER.error(t.toString(), t);
                }
            }
        }, 0, 2, TimeUnit.SECONDS);

        // load workflows from db
        for (String workflowId : offlineDb.listWorkflows()) {
            OfflineWorkflowCreationParameters parameters = offlineDb.getParameters(workflowId);
            try {
                addWorkflow(workflowId, parameters);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    @Override
    public void refreshAll() {
        workflowsLock.lock();
        try {
            Collection<OfflineWorkflowStatus> workflowStatus = new ArrayList<>();
            for(OfflineWorkflow workflow : workflows.values()) {
                workflowStatus.add(workflow.getStatus());
            }
            notifyWorkflowListChange(workflowStatus);
            for (OfflineWorkflow workflow : workflows.values()) {
                workflow.notifySynthesisListeners();
            }
        } finally {
            workflowsLock.unlock();
        }

        notifyBusyCoresSeriesChange();
    }

    @Override
    public void ping() {
        // nothing, used just to check JMX connection
    }

    private void notifyBusyCoresSeriesChange() {
        busyCoresSeriesLock.lock();
        try {
            sendNotification(new AttributeChangeNotification(this,
                                                             notificationIndex.getAndIncrement(),
                                                             System.currentTimeMillis(),
                                                             "Busy cores series has changed",
                                                             BUSY_CORES.toString(),
                                                             BusyCoresSeries.class.getName(),
                                                             null,
                                                             busyCoresSeries));

            for (OfflineApplicationListener l : listeners) {
                l.onBusyCoresUpdate(busyCoresSeries);
            }
        } finally {
            busyCoresSeriesLock.unlock();
        }
    }

    private void notifyWorkflowCreation(OfflineWorkflow workflow) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onWorkflowCreation(workflow.getStatus());
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

        sendNotification(
            new AttributeChangeNotification(
                this,
                notificationIndex.getAndIncrement(),
                System.currentTimeMillis(),
                "A workflow has been created",
                WORKFLOW_CREATION.toString(),
                String.class.getName(),
                null,
                workflow.getStatus()
            )
        );
    }

    private void notifyWorkflowRemoval(String workflowId) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onWorkflowRemoval(workflowId);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "A workflow has been removed",
                                                         WORKFLOW_REMOVAL.toString(),
                                                         String.class.getName(),
                                                         null,
                                                         workflowId));
    }

    private void notifyWorkflowListChange(Collection<OfflineWorkflowStatus> statuses) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onWorkflowListChange(statuses);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "Workflow list has changed",
                                                         WORKFLOW_LIST.toString(),
                                                         List.class.getName(),
                                                         null,
                                                         new ArrayList<>(statuses)
                                                         ));
    }

    private void notifyWorkflowStatusChange(OfflineWorkflowStatus status) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onWorkflowStatusChange(status);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "Workflow status has changed",
                                                         WORKFLOW_STATUS.toString(),
                                                         OfflineWorkflowStatus.class.getName(),
                                                         null,
                                                         status));
    }

    private void notifySampleSynthesisChange(String workflowId, Collection<SampleSynthesis> samples) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onSamplesChange(workflowId, samples);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "Samples have changed",
                                                         SAMPLES.toString(),
                                                         Object[].class.getName(),
                                                         null,
                                                         new Object[] {workflowId, new ArrayList<>(samples)})); // ArrayList copy is needed to be serializable
    }

    /**
     * Called at the end of the computing, and at the workflow selection
     * @param workflowId
     * @param ruleIds
     */
    private void notifySecurityRulesChange(String workflowId, Collection<RuleId> ruleIds) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onSecurityRulesChange(workflowId, ruleIds);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "Security rules have changed",
                                                         SECURITY_RULES.toString(),
                                                         Object[].class.getName(),
                                                         null,
                                                         new Object[] {workflowId, ruleIds}));
    }

    /**
     * Called at the end of the computing, and at the workflow selection
     * @param workflowId
     */
    private void notifySecurityRulesProgress(String workflowId, float percentProgress) {
        for (OfflineApplicationListener l : listeners) {
            try {
                l.onSecurityRulesProgress(workflowId, percentProgress);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "Security rules have changed",
                                                         SECURITY_RULES_PROGRESS.toString(),
                                                         Object[].class.getName(),
                                                         null,
                                                         new Object[] {workflowId, percentProgress}));
    }

    private void sendSecurityRule(String workflowId, SecurityRule securityRule) {
        sendNotification(new AttributeChangeNotification(this,
                                                         notificationIndex.getAndIncrement(),
                                                         System.currentTimeMillis(),
                                                         "Security rules descriptions",
                                                         SECURITY_RULE_DESCRIPTION.toString(),
                                                         Object[].class.getName(),
                                                         null,
                                                         new Object[] {workflowId, securityRule}));
    }

    @Override
    public Map<String, OfflineWorkflowStatus> listWorkflows() {
        workflowsLock.lock();
        try {
            Map<String, OfflineWorkflowStatus> statuses = new HashMap<>();
            for (OfflineWorkflow workflow : workflows.values()) {
                statuses.put(workflow.getId(), workflow.getStatus());
            }
            return statuses;
        } finally {
            workflowsLock.unlock();
        }
    }

    private void addWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) throws IOException {
        if (workflows.containsKey(workflowId)) {
            throw new RuntimeException("Workflow '" + workflowId + "' already exists");
        }

        // create a new workflow
        OfflineWorkflowFactory offlineWorkflowFactory = parameters.isSimplifiedWorkflow() ? new SimplifiedOfflineWorkflowFactory()
                                                                                          : new OfflineWorkflowFactoryImpl();
        final OfflineWorkflow workflow = offlineWorkflowFactory.create(workflowId,
                                                                     parameters,
                                                                     computationManager,
                                                                     ddbClientFactory,
                                                                     cadbClientFactory,
                                                                     histoDbClientFactory,
                                                                     topologyMinerFactory,
                                                                     rulesBuilder,
                                                                     offlineDb,
                                                                     validationDb,
                                                                     caseRepository,
                                                                     samplerFactory,
                                                                     loadFlowFactory,
                                                                     optimizerFactory,
                                                                     simulatorFactory,
                                                                     mergeOptimizerFactory,
                                                                     metricsDb,
                                                                     es);

        // create a workflow
        workflows.put(workflow.getId(), workflow);

        notifyWorkflowCreation(workflow);

        workflow.addListener(new OfflineWorkflowListener() {
            @Override
            public void onWorkflowStatusChange(OfflineWorkflowStatus status) {
                notifyWorkflowStatusChange(status);
            }

            @Override
            public void onSecurityRuleStorage(String workflowId, RuleId ruleId, boolean ok, float percentComplete) {
                notifySecurityRulesProgress(workflowId, percentComplete);
            }
        });

        workflow.addSynthesisListener(new OfflineWorkflowSynthesisListener() {
            @Override
            public void onSamplesChange(Collection<SampleSynthesis> samples) {
                notifySampleSynthesisChange(workflow.getId(), samples);
            }
        });
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        workflowsLock.lock();
        try {
            String workflowId2 = offlineDb.createWorkflow(workflowId, parameters);
            addWorkflow(workflowId2, parameters != null ? parameters : OfflineWorkflowCreationParameters.load());

            // clean validation db
            validationDb.init(OfflineWorkflow.getValidationDir(workflowId2));

            // prepare metrics db
            metricsDb.create(workflowId2);

            return workflowId2;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            workflowsLock.unlock();
        }
    }

    @Override
    public OfflineWorkflowCreationParameters getWorkflowParameters(String workflowId) {
        workflowsLock.lock();
        try {
            OfflineWorkflow workflow = getWorkflow(workflowId);
            return workflow.getCreationParameters();
        } finally {
            workflowsLock.unlock();
        }
    }

    private OfflineWorkflow getWorkflow(String workflowId) {
        OfflineWorkflow workflow = workflows.get(workflowId);
        if (workflow == null) {
            throw new RuntimeException("Workflow " + workflowId + " not found");
        }
        return workflow;
    }

    @Override
    public void removeWorkflow(String workflowId) {
        workflowsLock.lock();
        try {
            OfflineWorkflow workflow = getWorkflow(workflowId);

            if (workflow.getStatus().isRunning()) {
                throw new IllegalStateException("Cannot remove a running workflow");
            }

            offlineDb.deleteWorkflow(workflowId);

            metricsDb.remove(workflowId);

            workflows.remove(workflowId);

            notifyWorkflowRemoval(workflowId);

        } finally {
            workflowsLock.unlock();
        }
    }

    public void startWorkflowAndWait(String workflowId, OfflineWorkflowStartParameters startParameters) {
        try {
            OfflineWorkflow workflow;
            workflowsLock.lock();
            try {
                workflow = getWorkflow(workflowId);
            } finally {
                workflowsLock.unlock();
            }
            workflow.start(startParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startWorkflow(final String workflowId, final OfflineWorkflowStartParameters startParameters) {
        es.submit(() -> {
            try {
                startWorkflowAndWait(workflowId, startParameters);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        });
    }

    public void stopWorkflowAndWait(final String workflowId) {
        OfflineWorkflow workflow;
        workflowsLock.lock();
        try {
            workflow = getWorkflow(workflowId);
        } finally {
            workflowsLock.unlock();
        }
        workflow.stop();
    }

    @Override
    public void stopWorkflow(final String workflowId) {
        es.submit(() -> {
            try {
                stopWorkflowAndWait(workflowId);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        });
    }

    public void computeSecurityRulesAndWait(final String workflowId) throws Exception {
        OfflineWorkflow workflow;
        workflowsLock.lock();
        try {
            workflow = getWorkflow(workflowId);
        } finally {
            workflowsLock.unlock();
        }
        workflow.computeSecurityRules();
        notifySecurityRulesChange(workflowId, rulesDb.listRules(workflowId, null));
    }

    @Override
    public void computeSecurityRules(final String workflowId) {
        es.submit(() -> {
            try {
                computeSecurityRulesAndWait(workflowId);
            } catch (Throwable t) {
                LOGGER.error(t.toString(), t);
            }
        });
    }

    private final Lock stopLock = new ReentrantLock();
    private Condition stopCondition = stopLock.newCondition();

    public void await() throws InterruptedException {
        stopLock.lock();
        try {
            stopCondition.await();
        } finally {
            stopLock.unlock();
        }
    }

    @Override
    public void stopApplication() {
        stopLock.lock();
        try {
            stopCondition.signalAll();
        } finally {
            stopLock.unlock();
        }
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        return offlineDb.getSecurityIndexesSynthesis(workflowId);
    }

    @Override
    public void getSecurityRules(String workflowId) {
        notifySecurityRulesChange(workflowId, rulesDb.listRules(workflowId, null));
    }

    @Override
    public SecurityRuleExpression getSecurityRuleExpression(String workflowId, RuleId ruleId) {
        workflowId = Objects.requireNonNull(workflowId);
        ruleId = Objects.requireNonNull(ruleId);
        RuleAttributeSet attributeSet = Objects.requireNonNull(ruleId.getAttributeSet());
        SecurityIndexId securityIndexId = Objects.requireNonNull(ruleId.getSecurityIndexId());
        SecurityIndexType securityIndexType = Objects.requireNonNull(securityIndexId.getSecurityIndexType());
        String contingencyId = Objects.requireNonNull(securityIndexId.getContingencyId());
        SecurityRule securityRule = Objects.requireNonNull(rulesDb.getRules(workflowId, attributeSet, contingencyId, securityIndexType).get(0));
        return securityRule.toExpression();
    }

    @Override
    public void addListener(OfflineApplicationListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(OfflineApplicationListener l) {
        listeners.remove(l);
    }

    @Override
    public void close() throws Exception {
        scheduledFuture.cancel(true);

        try {
            offlineDb.close();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        try {
            metricsDb.close();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        try {
            rulesDb.close();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

        // unregister application mbean
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(BEAN_NAME));
    }

}
