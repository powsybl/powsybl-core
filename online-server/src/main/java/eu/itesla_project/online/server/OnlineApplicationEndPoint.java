/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.online.server.message.encoder.BusyCoresMessageEncoder;
import eu.itesla_project.online.server.message.encoder.ConnectionMessageEncoder;
import eu.itesla_project.online.server.message.encoder.RunningMessageEncoder;
import eu.itesla_project.online.server.message.encoder.StatesWithActionsSynthesisMessageEncoder;
import eu.itesla_project.online.server.message.encoder.StatesWithIndexesSynthesisMessageEncoder;
import eu.itesla_project.online.server.message.encoder.StatesWithSecurityRulesResultSynthesisMessageEncoder;
import eu.itesla_project.online.server.message.encoder.StatusMessageEncoder;
import eu.itesla_project.online.server.message.encoder.WcaContingenciesMessageEncoder;
import eu.itesla_project.online.server.message.encoder.WcaRunningMessageEncoder;
import eu.itesla_project.online.server.message.encoder.WorkStatusMessageEncoder;
import eu.itesla_project.online.server.message.encoder.WorkflowListMessageEncoder;


@ServerEndpoint(value="/messages/online/workflow", encoders={StatusMessageEncoder.class,RunningMessageEncoder.class,WcaRunningMessageEncoder.class,
															 BusyCoresMessageEncoder.class,StatesWithActionsSynthesisMessageEncoder.class,
															 StatesWithIndexesSynthesisMessageEncoder.class, WorkStatusMessageEncoder.class, 
															 //StableContingenciesMessageEncoder.class, 
															 WorkflowListMessageEncoder.class,ConnectionMessageEncoder.class, 
															 StatesWithSecurityRulesResultSynthesisMessageEncoder.class ,
															 //unstableContingenciesMessageEncoder.class
															 WcaContingenciesMessageEncoder.class})
/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineApplicationEndPoint {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(OnlineApplicationEndPoint.class);

    @Inject
    private OnlineApplicationBean bean;

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Session {} opened", session.getId());
        bean.getSessions().add(session);
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("Session {} closed", session.getId());
        bean.getSessions().remove(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        LOGGER.error(t.toString(), t);
    }

}
