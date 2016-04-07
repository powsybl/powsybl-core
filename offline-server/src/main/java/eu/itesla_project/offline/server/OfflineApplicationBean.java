/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import eu.itesla_project.modules.offline.SecurityIndexSynthesis;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.offline.*;
import eu.itesla_project.offline.monitoring.BusyCoresSeries;
import eu.itesla_project.offline.server.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Singleton
public class OfflineApplicationBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineApplicationBean.class);

    private RemoteOfflineApplication application;

    @Inject
    private WebSocketContext webSocketContext;

    @Resource
    private ManagedScheduledExecutorService scheduledExecutorService;

    private final OfflineApplicationListener computationResourcesListener = new OfflineApplicationListener() {

        @Override
        public void onWorkflowCreation(OfflineWorkflowStatus status) {
            webSocketContext.send(new WorkflowCreationMessage(status));
        }

        @Override
        public void onWorkflowRemoval(String workflowId) {
            webSocketContext.send(new WorkflowRemovalMessage(workflowId));
        }

        @Override
        public void onWorkflowListChange(Collection<OfflineWorkflowStatus> workflowStatus) {
            webSocketContext.send(new WorkflowListMessage(workflowStatus));
        }

        @Override
        public void onBusyCoresUpdate(BusyCoresSeries busyCores) {
            webSocketContext.send(new BusyCoresSeriesMessage(busyCores));
        }

        @Override
        public void onWorkflowStatusChange(OfflineWorkflowStatus status) {
            webSocketContext.send(new WorkflowStatusMessage(status));
        }

        @Override
        public void onSamplesChange(String workflowId, Collection<SampleSynthesis> samples) {
            webSocketContext.send(new SamplesSynthesisMessage(samples, workflowId));
        }

        @Override
        public void onSecurityIndexesChange(String workflowId, SecurityIndexSynthesis synthesis) {
            webSocketContext.send(new SecurityIndexesSynthesisMessage(synthesis, workflowId));
        }

        @Override
        public void onSecurityRulesChange(String workflowId, Collection<RuleId> ruleIds) {
            webSocketContext.send(new SecurityRulesChangeMessage(workflowId, ruleIds));
        }

        @Override
        public void onSecurityRuleDescription(String workflowId, SecurityRule securityRule) {
            webSocketContext.send(new SecurityRuleDescriptionMessage(workflowId, securityRule));
        }

        @Override
        public void onSecurityRulesProgress(String workflowId, Float progress) {
            webSocketContext.send(new SecurityRulesProgressMessage(workflowId, progress));
        }

    };

    RemoteOfflineApplication getApplication() {
        return application;
    }

    private RemoteOfflineApplication createApplication() {
        try {
            Map<String, String> env = new HashMap<>();
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, RMIContextFactory.class.getName());
            return new RemoteOfflineApplicationImpl(env, scheduledExecutorService) {

                @Override
                protected void onDisconnection() {
                    // to force clients to reconnect
                    webSocketContext.closeAndRemoveAllSessions();
                }

            };
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private RemoteOfflineApplication createMockApplication() {
        return new OfflineApplicationMock(scheduledExecutorService);
    }

    @PostConstruct
    void init() {
        LOGGER.info("Initializing offline workflow");
        application = createApplication();
        //application = createMockApplication();
        application.addListener(computationResourcesListener);
    }

    @PreDestroy
    void terminate() {
        LOGGER.info("Terminating offline workflow");
        application.removeListener(computationResourcesListener);
        try {
            application.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
