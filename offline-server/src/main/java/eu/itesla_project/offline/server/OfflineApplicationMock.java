/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import eu.itesla_project.modules.histo.HistoDbAttr;
import eu.itesla_project.modules.histo.HistoDbMetaAttributeId;
import eu.itesla_project.modules.histo.HistoDbMetaAttributeType;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.modules.offline.OfflineTaskStatus;
import eu.itesla_project.modules.offline.OfflineTaskType;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.rules.*;
import eu.itesla_project.modules.rules.expr.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import eu.itesla_project.offline.*;
import eu.itesla_project.offline.monitoring.BusyCoresSeries;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineApplicationMock implements RemoteOfflineApplication {

    private class Notifier implements OfflineApplicationListener {
        
        public final Collection<OfflineApplicationListener> listeners = Collections.synchronizedCollection(new ArrayList<OfflineApplicationListener>());

        public void addListener(OfflineApplicationListener l) {
            listeners.add(l);
        }

        public void removeListener(OfflineApplicationListener l) {
            listeners.remove(l);
        }

        @Override
        public void onWorkflowCreation(OfflineWorkflowStatus status) {
            for (OfflineApplicationListener l : listeners) {
                l.onWorkflowCreation(status);
            }
        }

        @Override
        public void onWorkflowRemoval(String workflowId) {
            for (OfflineApplicationListener l : listeners) {
                l.onWorkflowRemoval(workflowId);
            }
        }

        @Override
        public void onBusyCoresUpdate(BusyCoresSeries busyCoresSeries) {
            for (OfflineApplicationListener l : listeners) {
                l.onBusyCoresUpdate(busyCoresSeries);
            }
        }

        @Override
        public void onWorkflowStatusChange(OfflineWorkflowStatus status) {
            for (OfflineApplicationListener l : listeners) {
                l.onWorkflowStatusChange(status);
            }
        }

        @Override
        public void onWorkflowListChange(Collection<OfflineWorkflowStatus> statuses) {
            for (OfflineApplicationListener l : listeners) {
                l.onWorkflowListChange(statuses);
            }
        }

        @Override
        public void onSamplesChange(String workflowId, Collection<SampleSynthesis> samples) {
            for (OfflineApplicationListener l : listeners) {
                l.onSamplesChange(workflowId, samples);
            }
        }

        @Override
        public void onSecurityIndexesChange(String workflowId, SecurityIndexSynthesis synthesis) {
            for (OfflineApplicationListener l : listeners) {
                l.onSecurityIndexesChange(workflowId, synthesis);
            }
        }

        @Override
        public void onSecurityRulesChange(String workflowId, Collection<RuleId> ruleIds) {
            for (OfflineApplicationListener l : listeners) {
                l.onSecurityRulesChange(workflowId, ruleIds);
            }
        }

        @Override
        public void onSecurityRulesProgress(String workflowId, Float progress) {
            for (OfflineApplicationListener l : listeners) {
                l.onSecurityRulesProgress(workflowId, progress);
            }
        }

        @Override
        public void onSecurityRuleDescription(String workflowId, SecurityRule rule) {
            for (OfflineApplicationListener l : listeners) {
                l.onSecurityRuleDescription(workflowId, rule);
            }
        }
    };
    
    private final Notifier notify = new Notifier();

    private class OfflineWorkflowRandom extends Random {
        private SecurityIndexType nextSecurityIndexType() {
            return SecurityIndexType.values()[random.nextInt(SecurityIndexType.values().length)];
        }

        private RuleAttributeSet nextRuleAttributeSet() {
            return RuleAttributeSet.values()[random.nextInt(RuleAttributeSet.values().length)];
        }

        private HistoDbAttr nextHistoDbAttr() {
            return HistoDbAttr.values()[random.nextInt(HistoDbAttr.values().length)];
        }
        
        private HistoDbNetworkAttributeId nextHistoDbNetworkAttributeId() {
            return new HistoDbNetworkAttributeId("equipment-" + nextInt(), nextHistoDbAttr());
        }
        
        private ComparisonOperator.Type nextComparisonOperatorType() {
            return ComparisonOperator.Type.values()[random.nextInt(ComparisonOperator.Type.values().length)];
        }
    }

    private final static int AVAILABLE_CORES = 48;

    private class OfflineWorkflowMock implements OfflineWorkflow {

        private final String id;
        
        private final OfflineWorkflowCreationParameters creationParameters;

        private final SecurityIndexSynthesis securityIndexSynthesis = new SecurityIndexSynthesis();

        private final Map<RuleId, SecurityRuleExpression> securityRules = new HashMap<>();

        private OfflineWorkflowStartParameters startParameters;

        private long endsTime;

        private boolean running = false;

        private boolean computing = false;
        
        private float computingProgress = 0;

        private int sampleIdx = -1;

        private int taskIdx = 0;
        
        private OfflineWorkflowMock(String id, OfflineWorkflowCreationParameters creationParameters) {
            this.id = id;
            this.creationParameters = creationParameters;
            securityIndexSynthesis.addSecurityIndex(new SecurityIndexId("LINE1", SecurityIndexType.OVERLOAD), true);
            securityIndexSynthesis.addSecurityIndex(new SecurityIndexId("LINE1", SecurityIndexType.OVERLOAD), true);
            securityIndexSynthesis.addSecurityIndex(new SecurityIndexId("LINE1", SecurityIndexType.OVERLOAD), false);
            securityIndexSynthesis.addSecurityIndex(new SecurityIndexId("LINE1", SecurityIndexType.TSO_FREQUENCY), true);
            computeSecurityRules(100);
        }

        private void change() {
            boolean notifyStatusChange = false;
            if (running) {
                if (System.currentTimeMillis() > endsTime) {
                    running = false;
                }
                if (sampleIdx == -1) {
                    sampleIdx = 0;
                }
                taskIdx++;
                if (taskIdx >= OfflineTaskType.values().length) {
                    sampleIdx++;
                    taskIdx = 0;
                }
                notifySampleSynthesisChange();
                notifySecurityIndexesSynthesisChange();
                notifyStatusChange = true;
            }
            if(computing) {
                if (computingProgress >= 1) {
                    computing = false;
                    notify.onSecurityRulesChange(id, securityRules.keySet());
                } else {
                    computeSecurityRules(10);
                }
                computingProgress += .1f;
                notify.onSecurityRulesProgress(id, computingProgress);
                notifyStatusChange = true;
            }
            if (notifyStatusChange) notify.onWorkflowStatusChange(this.getStatus());
        }

        private void notifySampleSynthesisChange() {
            if (sampleIdx >= 0) {
                SampleSynthesis ss = new SampleSynthesis(sampleIdx, new OfflineTaskEvent(OfflineTaskType.values()[taskIdx], OfflineTaskStatus.SUCCEED, null));
                notify.onSamplesChange(id, Collections.singleton(ss));
            }
        }

        private void notifySecurityIndexesSynthesisChange() {
            if (securityIndexSynthesis != null) {
                notify.onSecurityIndexesChange(id, securityIndexSynthesis);
            }
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void start(OfflineWorkflowStartParameters startParameters) throws Exception {
            this.startParameters = startParameters;
            endsTime = System.currentTimeMillis() + startParameters.getDuration() * 60 * 1000;
            running = true;
        }

        @Override
        public void stop() {
            running = false;
        }

        @Override
        public void computeSecurityRules() throws Exception {
            computingProgress = 0;
            computing = true;
        }

        @Override
        public OfflineWorkflowStatus getStatus() {
            if(running) {
                return new OfflineWorkflowStatus(id, OfflineWorkflowStep.SAMPLING, creationParameters, startParameters);
            } else if (computing) {
                return new OfflineWorkflowStatus(id, OfflineWorkflowStep.SECURITY_RULES_COMPUTATION, creationParameters);
            } else {
                return new OfflineWorkflowStatus(id, OfflineWorkflowStep.IDLE, creationParameters);
            }
        }

        @Override
        public OfflineWorkflowCreationParameters getCreationParameters() {
            return creationParameters;
        }

        @Override
        public void addListener(OfflineWorkflowListener listener) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeListener(OfflineWorkflowListener listener) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void addSynthesisListener(OfflineWorkflowSynthesisListener listener) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeSynthesisListener(OfflineWorkflowSynthesisListener listener) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        private void computeSecurityRules(int count) {
            int size = securityRules.size();
            for (int i = size; i < (size + count); i++) {
                String contingencyId = "contingency-" + i;
                SecurityIndexType securityIndexType = random.nextSecurityIndexType();
                SecurityIndexId securityIndexId = new SecurityIndexId(contingencyId, securityIndexType);
                RuleAttributeSet ruleAttributeSet = random.nextRuleAttributeSet();
                RuleId ruleId = new RuleId(ruleAttributeSet, securityIndexId);

                SecurityRuleStatus securityRuleStatus = SecurityRuleStatus.values()[random.nextInt(SecurityRuleStatus.values().length)];
                ExpressionNode condition = null;
                switch (securityRuleStatus) {
                    case SECURE_IF: {
                        ComparisonOperator condition1 = new ComparisonOperator(
                                new Attribute(new HistoDbMetaAttributeId(HistoDbMetaAttributeType.values()[random.nextInt(HistoDbMetaAttributeType.values().length)])),
                                new Litteral(random.nextDouble()),
                                random.nextComparisonOperatorType());
                        ComparisonOperator condition2 = new ComparisonOperator(
                                new Attribute(new HistoDbMetaAttributeId(HistoDbMetaAttributeType.values()[random.nextInt(HistoDbMetaAttributeType.values().length)])),
                                new Litteral(random.nextDouble()),
                                random.nextComparisonOperatorType());
                        ComparisonOperator condition3 = new ComparisonOperator(
                                new Attribute(random.nextHistoDbNetworkAttributeId()),
                                new Litteral(random.nextDouble()),
                                random.nextComparisonOperatorType());
                        AndOperator and = new AndOperator(condition1, condition2);
                        condition = new OrOperator(and, condition3);
                        break;
                    }
                    case ALWAYS_SECURE:
                    case ALWAYS_UNSECURE:
                    default:
                        break;
                }
                SecurityRuleExpression securityRuleExpression = new SecurityRuleExpression(ruleId, securityRuleStatus, condition);
                securityRules.put(ruleId, securityRuleExpression);
            }
        }

        @Override
        public void notifySynthesisListeners() {
        }
    }

    private final Map<String, OfflineWorkflowMock> workflows = Collections.synchronizedMap(new LinkedHashMap<String, OfflineWorkflowMock>());

    private final ScheduledFuture<?> future;

    private final BusyCoresSeries busyCores = new BusyCoresSeries(AVAILABLE_CORES);

    private final OfflineWorkflowRandom random = new OfflineWorkflowRandom();

    private final AtomicInteger workflowIndex = new AtomicInteger();

    public OfflineApplicationMock(ScheduledExecutorService ses) {
        future = ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int value;
                if (busyCores.getValues().isEmpty()) {
                    value = 0;
                } else {
                    int inc = (int) (random.nextDouble() * 10) - 5;
                    value = busyCores.getValues().get(busyCores.getValues().size()-1).getBusyCores() + inc;
                    if (value < 0) {
                        value = 0;
                    }
                    if (value > AVAILABLE_CORES) {
                        value = AVAILABLE_CORES;
                    }
                }
                busyCores.addValue(new BusyCoresSeries.Value(value));
                
                notify.onBusyCoresUpdate(busyCores);

                for (OfflineWorkflowMock workflow : workflows.values()) {
                    workflow.change();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public Map<String, OfflineWorkflowStatus> listWorkflows() {
        Map<String, OfflineWorkflowStatus> statuses = new HashMap<>();
        for (OfflineWorkflowMock workflow : workflows.values()) {
            statuses.put(workflow.id, new OfflineWorkflowStatus(workflow.id, OfflineWorkflowStep.SAMPLING, new OfflineWorkflowCreationParameters(new HashSet(), new DateTime(), new Interval(0, 100000), false, false)));
        }
        return statuses;
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        String workflowId2 = workflowId != null ? workflowId : "test-" + workflowIndex.getAndIncrement();
        OfflineWorkflowMock workflow = new OfflineWorkflowMock(workflowId2, parameters);
        workflows.put(workflowId2, workflow);
        notify.onWorkflowCreation(workflow.getStatus());
        return workflowId2;
    }

    @Override
    public OfflineWorkflowCreationParameters getWorkflowParameters(String workflowId) {
        return OfflineWorkflowCreationParameters.load();
    }

    @Override
    public void removeWorkflow(String workflowId) {
        workflows.remove(workflowId);
        notify.onWorkflowRemoval(workflowId);
    }

    @Override
    public void startWorkflow(String workflowId, OfflineWorkflowStartParameters startParameters) {
        OfflineWorkflowMock workflow = workflows.get(workflowId);
        try {
            workflow.start(startParameters);
            notify.onWorkflowStatusChange(workflow.getStatus());
        } catch (Exception ex) {
            Logger.getLogger(OfflineApplicationMock.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stopWorkflow(String workflowId) {
        OfflineWorkflowMock workflow = workflows.get(workflowId);
        workflow.stop();
        notify.onWorkflowStatusChange(workflow.getStatus());
    }

    @Override
    public void computeSecurityRules(String workflowId) {
        OfflineWorkflowMock workflow = workflows.get(workflowId);
        workflow.securityRules.clear();
        workflow.computing = true;
        notify.onWorkflowStatusChange(workflow.getStatus());
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        OfflineWorkflowMock workflow = workflows.get(workflowId);
        return workflow.securityIndexSynthesis;
    }

    @Override
    public void addListener(OfflineApplicationListener l) {
        notify.addListener(l);
    }

    @Override
    public void removeListener(OfflineApplicationListener l) {
        notify.removeListener(l);
    }

    @Override
    public void refreshAll() {
        List<OfflineWorkflowStatus> workflowStatuses = new ArrayList<>(workflows.size());
        for (Map.Entry<String, OfflineWorkflowMock> entry : workflows.entrySet()) {
            OfflineWorkflowMock workflowMock = entry.getValue();
            workflowStatuses.add(workflowMock.getStatus());
        }
        notify.onWorkflowListChange(workflowStatuses);
        for (Map.Entry<String, OfflineWorkflowMock> entry : workflows.entrySet()) {
            OfflineWorkflowMock workflow = entry.getValue();
            notify.onWorkflowStatusChange(workflow.getStatus());
            workflow.notifySampleSynthesisChange();
            workflow.notifySecurityIndexesSynthesisChange();
            getSecurityRules(workflow.getId());
        }
    }

    @Override
    public void stopApplication() {
    }

    @Override
    public void close() throws Exception {
        future.cancel(true);
    }

    @Override
    public void getSecurityRules(String workflowId) {
        OfflineWorkflowMock workflow = workflows.get(workflowId);
        notify.onSecurityRulesChange(workflow.getId(), workflow.securityRules.keySet());
    }

    @Override
    public SecurityRuleExpression getSecurityRuleExpression(String workflowId, RuleId ruleId) {
        OfflineWorkflowMock workflow = workflows.get(workflowId);
        return workflow.securityRules.get(ruleId);
    }
}
