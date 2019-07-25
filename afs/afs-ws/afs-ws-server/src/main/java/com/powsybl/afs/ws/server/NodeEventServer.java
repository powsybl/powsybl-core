/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.afs.storage.events.NodeEventContainer;
import com.powsybl.afs.ws.server.utils.AppDataBean;
import com.powsybl.afs.ws.utils.AfsRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ServerEndpoint(value = "/messages/" + AfsRestApi.RESOURCE_ROOT + "/" + AfsRestApi.VERSION + "/node_events/{fileSystemName}",
                encoders = {NodeEventListEncoder.class},  decoders = {NodeEventContainerDecoder.class})
public class NodeEventServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventServer.class);

    @Inject
    private AppDataBean appDataBean;

    @Inject
    private WebSocketContext webSocketContext;

    @OnOpen
    public void onOpen(@PathParam("fileSystemName") String fileSystemName, Session session) {
        LOGGER.debug("WebSocket session '{}' opened for file system '{}'", session.getId(), fileSystemName);

        AppStorage storage = appDataBean.getStorage(fileSystemName);

        AppStorageListener listener = eventList -> {
            if (session.isOpen()) {
                RemoteEndpoint.Async remote = session.getAsyncRemote();
                remote.setSendTimeout(1000);
                remote.sendObject(eventList, result -> {
                    if (!result.isOK()) {
                        LOGGER.error(result.getException().toString(), result.getException());
                    }
                });
            } else {
                webSocketContext.removeSession(session);
            }
        };
        storage.getEventsBus().addListener(listener);
        session.getUserProperties().put("listener", listener); // to prevent weak listener from being garbage collected

        webSocketContext.addSession(session);
    }

    @OnMessage
    public void onMessage(Session session, NodeEventContainer nodeEventContainer) {
        LOGGER.trace("Node event websocket session '' of type '{}' id: ",
                session.getId());
        AppStorage storage = appDataBean.getStorage(nodeEventContainer.getFileSystemName());
        storage.getEventsBus().pushEvent(nodeEventContainer.getNodeEvent(), nodeEventContainer.getTopic());
    }

    private void removeSession(String fileSystemName, Session session) {
        AppStorage storage = appDataBean.getStorage(fileSystemName);

        AppStorageListener listener = (AppStorageListener) session.getUserProperties().get("listener");
        storage.getEventsBus().removeListener(listener);

        webSocketContext.removeSession(session);
    }

    @OnClose
    public void onClose(@PathParam("fileSystemName") String fileSystemName, Session session, CloseReason closeReason) {
        LOGGER.debug("WebSocket session '{}' closed ({}) for file system '{}'",
                session.getId(), closeReason, fileSystemName);

        removeSession(fileSystemName, session);
    }

    @OnError
    public void error(@PathParam("fileSystemName") String fileSystemName, Session session, Throwable t) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(t.toString(), t);
        }

        removeSession(fileSystemName, session);
    }
}
