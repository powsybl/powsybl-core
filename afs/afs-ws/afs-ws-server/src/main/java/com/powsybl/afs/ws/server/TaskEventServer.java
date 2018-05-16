/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.TaskEvent;
import com.powsybl.afs.TaskListener;
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
@ServerEndpoint(value = "/messages/" + AfsRestApi.RESOURCE_ROOT + "/" + AfsRestApi.VERSION + "/task_events/{fileSystemName}/{projectId}",
                encoders = {NodeEventListEncoder.class})
public class TaskEventServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskEventServer.class);

    @Inject
    private AppDataBean appDataBean;

    @Inject
    private WebSocketContext webSocketContext;

    @OnOpen
    public void onOpen(@PathParam("fileSystemName") String fileSystemName, @PathParam("projectId") String projectId, Session session) {
        LOGGER.debug("Task events webSocket session '{}' opened for file system {} filtering on project {}",
                session.getId(), fileSystemName, projectId);

        AppFileSystem fileSystem = appDataBean.getFileSystem(fileSystemName);

        TaskListener listener = new TaskListener() {

            @Override
            public String getProjectId() {
                return projectId;
            }

            @Override
            public void onEvent(TaskEvent event) {
                if (session.isOpen()) {
                    RemoteEndpoint.Async remote = session.getAsyncRemote();
                    remote.setSendTimeout(1000);
                    remote.sendObject(event, result -> {
                        if (!result.isOK()) {
                            LOGGER.error(result.getException().toString(), result.getException());
                        }
                    });
                } else {
                    webSocketContext.removeSession(session);
                }
            }
        };
        session.getUserProperties().put("listener", listener);
        fileSystem.getTaskMonitor().addListener(listener);

        webSocketContext.addSession(session);
    }

    private void removeSession(String fileSystemName, Session session) {
        AppFileSystem fileSystem = appDataBean.getFileSystem(fileSystemName);

        TaskListener listener = (TaskListener) session.getUserProperties().get("listener");
        fileSystem.getTaskMonitor().removeListener(listener);

        webSocketContext.removeSession(session);
    }

    @OnClose
    public void onClose(@PathParam("fileSystemName") String fileSystemName, Session session, CloseReason closeReason) {
        LOGGER.debug("Task events webSocket session '{}' closed ({}) for file system '{}'",
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
