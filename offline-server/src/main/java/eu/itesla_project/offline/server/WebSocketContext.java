/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server;

import eu.itesla_project.offline.server.message.Message;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Singleton
public class WebSocketContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketContext.class);

    private final Set<Session> sessions = new HashSet<>();

    public synchronized void addSession(Session session) {
        sessions.add(session);
    }

    public synchronized void removeSession(Session session) {
        sessions.remove(session);
    }

    public synchronized int getSessionCount() {
        return sessions.size();
    }

    public synchronized void send(Message message) {
        for (Iterator<Session> it =  sessions.iterator(); it.hasNext();) {
            Session session = it.next();
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendObject(message);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            } else {
                it.remove();
            }
        }
    }

    @PreDestroy
    public synchronized void closeAndRemoveAllSessions() {
        for (Session session : sessions) {
            try {
                session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "You have been logout because JMX connection to workflow manager has been lost."));
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        sessions.clear();
    }

}
