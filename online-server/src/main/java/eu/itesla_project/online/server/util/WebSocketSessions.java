/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.server.util;

import eu.itesla_project.online.server.message.Message;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class WebSocketSessions {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketSessions.class);

    private final Set<Session> sessions = new HashSet<Session>();

    public synchronized void add(Session session) {
        sessions.add(session);
    }

    public synchronized void remove(Session session) {
        sessions.remove(session);
    }

    public synchronized <T> void send(Message<T> message) {
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

    public synchronized void close() {
        for (Session session : sessions) {
            try {
                session.close();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

}
