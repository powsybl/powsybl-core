/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import eu.itesla_project.offline.server.message.LoginMessage;
import eu.itesla_project.offline.server.message.encoder.*;
import java.io.IOException;
import java.io.StringReader;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ServerEndpoint(value="/messages/offline", encoders={
    BusyCoresSeriesMessageEncoder.class,
    LoginMessageEncoder.class,
    SamplesSynthesisMessageEncoder.class,
    SecurityIndexesSynthesisMessageEncoder.class,
    SecurityRuleDescriptionMessageEncoder.class,
    SecurityRulesChangeMessageEncoder.class,
    SecurityRulesProgressMessageEncoder.class,
    WorkflowCreationMessageEncoder.class,
    WorkflowListMessageEncoder.class,
    WorkflowRemovalMessageEncoder.class,
    WorkflowStatusMessageEncoder.class,
})
public class OfflineApplicationEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineApplicationEndPoint.class);

    @Inject
    private OfflineApplicationBean bean;

    @Inject
    private WebSocketContext webSocketContext;

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("WebSocket session {} opened", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String username;
        String password;
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject obj = reader.readObject();
            username = obj.getString("username");
            password = obj.getString("password");
        }

        if (OfflineApplicationSecurity.check(username, password)) {
            LOGGER.info("WebSocket session {} authenticated", session.getId());

            if (bean.getApplication().isConnected()) {
                // confirm login to the client
                try {
                    session.getBasicRemote().sendObject(new LoginMessage());
                } catch (IOException|EncodeException e) {
                    LOGGER.error(e.toString(), e);
                }

                // store the session for later updates
                webSocketContext.addSession(session);

                // full refresh of all clients
                bean.getApplication().refreshAll();
            } else {
                try {
                    session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Login denied because JMX connection to workflow manager has failed."));
                } catch (IOException e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        } else {
            try {
                session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Login denied because your username or password is invalid."));
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.info("WebSocket session {} closed", session.getId());
        webSocketContext.removeSession(session);
    }

}
