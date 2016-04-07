/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.modules.rules.SecurityRuleExpression;
import eu.itesla_project.offline.monitoring.BusyCoresSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteOfflineApplicationImpl implements RemoteOfflineApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteOfflineApplicationImpl.class);

    private static class Listeners implements NotificationListener {

        private final List<OfflineApplicationListener> listeners = new CopyOnWriteArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public void handleNotification(Notification notification, Object handback) {
            AttributeChangeNotification changeNotification = (AttributeChangeNotification) notification;
            if (!changeNotification.getAttributeName().equals(LocalOfflineApplicationMBean.Attribute.BUSY_CORES.toString())) {
                LOGGER.info(changeNotification.getAttributeName());
            }
            switch (LocalOfflineApplicationMBean.Attribute.valueOf(changeNotification.getAttributeName())) {
                case BUSY_CORES:
                    for (OfflineApplicationListener l : listeners) {
                        l.onBusyCoresUpdate((BusyCoresSeries) changeNotification.getNewValue());
                    }
                    break;
                case WORKFLOW_CREATION: {
                    OfflineWorkflowStatus newWorkflowStatus = (OfflineWorkflowStatus) changeNotification.getNewValue();
                    LOGGER.info(newWorkflowStatus.toString());
                    for (OfflineApplicationListener l : listeners) {
                        l.onWorkflowCreation(newWorkflowStatus);
                    }
                    break;
                }
                case WORKFLOW_REMOVAL: {
                    for (OfflineApplicationListener l : listeners) {
                        l.onWorkflowRemoval((String) changeNotification.getNewValue());
                    }
                    break;
                }
                case WORKFLOW_LIST: {
                    List<OfflineWorkflowStatus> statuses = (List<OfflineWorkflowStatus>) changeNotification.getNewValue();
                    for (OfflineApplicationListener l : listeners) {
                        l.onWorkflowListChange(statuses);
                    }
                }
                break;
                case WORKFLOW_STATUS: {
                    OfflineWorkflowStatus status = (OfflineWorkflowStatus) changeNotification.getNewValue();
                    for (OfflineApplicationListener l : listeners) {
                        l.onWorkflowStatusChange(status);
                    }
                }
                break;
                case SAMPLES: {
                    Object[] newValue = (Object[]) changeNotification.getNewValue();
                    for (OfflineApplicationListener l : listeners) {
                        l.onSamplesChange((String) newValue[0], (Collection<SampleSynthesis>) newValue[1]);
                    }
                }
                break;
                case SECURITY_RULE_DESCRIPTION: {
                    Object[] newValue = (Object[]) changeNotification.getNewValue();
                    String workflowId = (String) newValue[0];
                    SecurityRule securityRule = (SecurityRule) newValue[1];
                    LOGGER.info("JMX Notification received: SECURITY_RULE_DESCRIPTION: " + workflowId + ", " + securityRule);
                    for (OfflineApplicationListener l : listeners) {
                        l.onSecurityRuleDescription(workflowId, securityRule);
                    }
                }
                break;
                case SECURITY_RULES: {
                    Object[] newValue = (Object[]) changeNotification.getNewValue();
                    String workflowId = (String) newValue[0];
                    Collection<RuleId> ruleIds = (Collection<RuleId>) newValue[1];
                    LOGGER.info("JMX Notification received: SECURITY_RULES: " + workflowId + ", " + ruleIds.size());
                    for (OfflineApplicationListener l : listeners) {
                        l.onSecurityRulesChange(workflowId, ruleIds);
                    }
                }
                break;
                case SECURITY_RULES_PROGRESS: {
                    Object[] newValue = (Object[]) changeNotification.getNewValue();
                    String workflowId = (String) newValue[0];
                    Float progress = (Float) newValue[1];
                    LOGGER.info("JMX Notification received: SECURITY_RULES_PROGRESS: " + workflowId + ", " + progress);
                    for (OfflineApplicationListener l : listeners) {
                        l.onSecurityRulesProgress(workflowId, progress);
                    }
                }
                break;
                default:
                    throw new AssertionError();
            }
        }

        private void add(OfflineApplicationListener l) {
            listeners.add(l);
        }

        private void remove(OfflineApplicationListener l) {
            listeners.remove(l);
        }
    }

    private final Listeners listeners = new Listeners();

    private final JMXServiceURL url;

    private final Map<String, String> env;

    private final ScheduledFuture<?> future;

    private static class JMXContext implements AutoCloseable {

        private final NotificationListener listener;

        private final JMXConnector connector;

        private final MBeanServerConnection mbsc;

        private final ObjectName beanName;

        private final LocalOfflineApplicationMBean application;

        private JMXContext(JMXServiceURL url, Map<String, String> env, NotificationListener listener)
                throws IOException, MalformedObjectNameException, InstanceNotFoundException {
            this.listener = listener;
            connector = JMXConnectorFactory.connect(url, env);
            mbsc = connector.getMBeanServerConnection();
            beanName = new ObjectName(LocalOfflineApplicationMBean.BEAN_NAME);
            application = MBeanServerInvocationHandler.newProxyInstance(mbsc, beanName, LocalOfflineApplicationMBean.class, false);
            mbsc.addNotificationListener(beanName, listener, null, null);
        }

        private LocalOfflineApplicationMBean getApplication() {
            return application;
        }

        @Override
        public void close() throws Exception {
            mbsc.removeNotificationListener(beanName, listener);
            connector.close();
        }

    }

    private final Lock contextLock = new ReentrantLock();

    private JMXContext context;

    public RemoteOfflineApplicationImpl(JMXServiceURL url, Map<String, String> env, ScheduledExecutorService scheduledExecutorService) {
        this.url = url;
        this.env = env;
        // automatic JMX reconnection (5s)
        future = scheduledExecutorService != null ? scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getJmxContext().getApplication().ping();
                } catch (IOException | JMException | UndeclaredThrowableException e) {
                    discardJmxContext();
                }
            }
        }, 5, 5, TimeUnit.SECONDS) : null;
    }

    public RemoteOfflineApplicationImpl(String host, int port, Map<String, String> env, ScheduledExecutorService scheduledExecutorService) throws MalformedURLException {
        this(new JMXServiceURL("service:jmx:rmi://" + host + "/jndi/rmi://" + host + ":" + port + "/jmxrmi"),
                Collections.<String, String>emptyMap(), scheduledExecutorService);
    }

    public RemoteOfflineApplicationImpl(OfflineUIConfig config, Map<String, String> env, ScheduledExecutorService scheduledExecutorService) throws MalformedURLException {
        this(config.getJmxHost(), config.getJmxPort(), env, scheduledExecutorService);
    }

    public RemoteOfflineApplicationImpl(Map<String, String> env, ScheduledExecutorService scheduledExecutorService) throws MalformedURLException {
        this(OfflineUIConfig.load(), env, scheduledExecutorService);
    }

    public RemoteOfflineApplicationImpl(ScheduledExecutorService scheduledExecutorService) throws MalformedURLException {
        this(Collections.<String, String>emptyMap(), scheduledExecutorService);
    }

    public RemoteOfflineApplicationImpl() throws MalformedURLException {
        this(null);
    }

    private JMXContext getJmxContext() throws IOException, JMException {
        contextLock.lock();
        try {
            if (context == null) {
                JMXContext newContext = new JMXContext(url, env, listeners);

                // immediately check the connection
                newContext.getApplication().ping();

                LOGGER.info("JMX connection to {}", url);

                context = newContext;

                try {
                    onConnection();
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        } finally {
            contextLock.unlock();
        }
        return context;
    }

    @Override
    public boolean isConnected() {
        contextLock.lock();
        try {
            return context != null;
        } finally {
            contextLock.unlock();
        }
    }

    protected void onDisconnection() {
        // TO OVERRIDE
    }

    protected void onConnection() {
        // TO OVERRIDE
    }

    private void discardJmxContext() {
        LOGGER.error("JMX connection error: {}", url);
        contextLock.lock();
        try {
            context = null;

            try {
                onDisconnection();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        } finally {
            contextLock.unlock();
        }
    }

    @Override
    public Map<String, OfflineWorkflowStatus> listWorkflows() {
        try {
            return getJmxContext().getApplication().listWorkflows();
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
            return Collections.emptyMap();
        }
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        try {
            return getJmxContext().getApplication().createWorkflow(workflowId, parameters);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
            return null;
        }
    }

    @Override
    public OfflineWorkflowCreationParameters getWorkflowParameters(String workflowId) {
        try {
            return getJmxContext().getApplication().getWorkflowParameters(workflowId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
            return null;
        }
    }

    @Override
    public void removeWorkflow(String workflowId) {
        try {
            getJmxContext().getApplication().removeWorkflow(workflowId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
    }

    @Override
    public void startWorkflow(String workflowId, OfflineWorkflowStartParameters startParameters) {
        try {
            getJmxContext().getApplication().startWorkflow(workflowId, startParameters);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
    }

    @Override
    public void stopWorkflow(String workflowId) {
        try {
            getJmxContext().getApplication().stopWorkflow(workflowId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
    }

    @Override
    public void computeSecurityRules(String workflowId) {
        try {
            getJmxContext().getApplication().computeSecurityRules(workflowId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        try {
            return getJmxContext().getApplication().getSecurityIndexesSynthesis(workflowId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
            return null;
        }
    }

    @Override
    public void getSecurityRules(String workflowId) {
        try {
            getJmxContext().getApplication().getSecurityRules(workflowId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
    }

    @Override
    public SecurityRuleExpression getSecurityRuleExpression(String workflowId, RuleId ruleId) {
        try {
            return getJmxContext().getApplication().getSecurityRuleExpression(workflowId, ruleId);
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
        return null;
    }

    @Override
    public void refreshAll() {
        try {
            getJmxContext().getApplication().refreshAll();
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
    }

    @Override
    public void stopApplication() {
        try {
            getJmxContext().getApplication().stopApplication();
        } catch (IOException | JMException | UndeclaredThrowableException e) {
            discardJmxContext();
        }
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
        if (future != null) {
            future.cancel(true);
        }
        contextLock.lock();
        try {
            if (context != null) {
                context.close();
            }
        } finally {
            contextLock.unlock();
        }
    }

}
