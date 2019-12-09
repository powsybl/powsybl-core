/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import com.powsybl.afs.Project;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.TaskListener;
import com.powsybl.afs.TaskMonitor;
import com.powsybl.afs.ws.client.utils.ClientUtils;
import com.powsybl.afs.ws.client.utils.UncheckedDeploymentException;
import com.powsybl.afs.ws.utils.AfsRestApi;
import com.powsybl.afs.ws.utils.JsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.powsybl.afs.ws.client.utils.ClientUtils.checkOk;
import static com.powsybl.afs.ws.client.utils.ClientUtils.readEntityIfOk;
import static com.powsybl.afs.ws.storage.RemoteAppStorage.getWebTarget;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteTaskMonitor implements TaskMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteTaskMonitor.class);
    public static final String FILE_SYSTEM_NAME = "fileSystemName";

    private final String fileSystemName;

    private final URI restUri;

    private final String token;

    private final Map<TaskListener, Session> sessions = new HashMap<>();

    private final Client client;

    private final WebTarget webTarget;

    public RemoteTaskMonitor(String fileSystemName, URI restUri, String token) {
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.restUri = Objects.requireNonNull(restUri);
        this.token = token;

        client = ClientUtils.createClient()
                .register(new JsonProvider());

        webTarget = getWebTarget(client, restUri);
    }

    @Override
    public Task startTask(ProjectFile projectFile) {
        Objects.requireNonNull(projectFile);

        LOGGER.debug("startTask(fileSystemName={}, projectFile={})", fileSystemName, projectFile.getId());

        Response response = webTarget.path("fileSystems/{fileSystemName}/tasks")
                .resolveTemplate(FILE_SYSTEM_NAME, fileSystemName)
                .queryParam("projectFileId", projectFile.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .put(Entity.text(""));
        try {
            return readEntityIfOk(response, TaskMonitor.Task.class);
        } finally {
            response.close();
        }
    }

    @Override
    public Task startTask(String name, Project project) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(project);

        LOGGER.debug("startTask(fileSystemName={}, name={}, project={})", fileSystemName, name, project.getId());

        Response response = webTarget.path("fileSystems/{fileSystemName}/tasks")
                .resolveTemplate(FILE_SYSTEM_NAME, fileSystemName)
                .queryParam("name", name)
                .queryParam("projectId", project.getId())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .put(Entity.text(""));
        try {
            return readEntityIfOk(response, TaskMonitor.Task.class);
        } finally {
            response.close();
        }
    }

    @Override
    public void stopTask(UUID id) {
        LOGGER.debug("stopTask(fileSystemName={}, id={})", fileSystemName, id);

        Response response = webTarget.path("fileSystems/{fileSystemName}/tasks/{taskId}")
                .resolveTemplate(FILE_SYSTEM_NAME, fileSystemName)
                .resolveTemplate("taskId", id)
                .request()
                .header(HttpHeaders.AUTHORIZATION, token)
                .delete();
        try {
            checkOk(response);
        } finally {
            response.close();
        }
    }

    @Override
    public void updateTaskMessage(UUID id, String message) {
        LOGGER.debug("updateTaskMessage(fileSystemName={}, id={})", fileSystemName, id);

        Response response = webTarget.path("fileSystems/{fileSystemName}/tasks/{taskId}")
                .resolveTemplate(FILE_SYSTEM_NAME, fileSystemName)
                .resolveTemplate("taskId", id)
                .request()
                .header(HttpHeaders.AUTHORIZATION, token)
                .post(Entity.text(message));
        try {
            checkOk(response);
        } finally {
            response.close();
        }
    }

    @Override
    public Snapshot takeSnapshot(String projectId) {
        LOGGER.debug("takeSnapshot(fileSystemName={}, projectId={})", fileSystemName, projectId);

        Response response = webTarget.path("fileSystems/{fileSystemName}/tasks")
                .resolveTemplate(FILE_SYSTEM_NAME, fileSystemName)
                .queryParam("projectId", projectId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .get();
        try {
            return readEntityIfOk(response, Snapshot.class);
        } finally {
            response.close();
        }
    }

    @Override
    public boolean cancelTaskComputation(UUID id) {
        LOGGER.debug("cancel(fileSystemName={}, id={})", fileSystemName, id);

        Response response = webTarget.path("fileSystems/{fileSystemName}/tasks/{taskId}/_cancel")
                .resolveTemplate(FILE_SYSTEM_NAME, fileSystemName)
                .resolveTemplate("taskId", id)
                .request()
                .header(HttpHeaders.AUTHORIZATION, token)
                .put(null);
        try {
            return readEntityIfOk(response, Boolean.class);
        } finally {
            response.close();
        }
    }

    @Override
    public void addListener(TaskListener listener) {
        Objects.requireNonNull(listener);

        URI wsUri = SocketsUtils.getWebSocketUri(restUri);
        URI endPointUri = URI.create(wsUri + "/messages/" + AfsRestApi.RESOURCE_ROOT + "/" +
                AfsRestApi.VERSION + "/task_events/" + fileSystemName + "/" + listener.getProjectId());

        LOGGER.debug("Connecting to task event websocket for file system {} at {}", fileSystemName, endPointUri);

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            Session session = container.connectToServer(new TaskEventClient(listener), endPointUri);
            sessions.put(listener, session);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (DeploymentException e) {
            throw new UncheckedDeploymentException(e);
        }
    }

    @Override
    public void removeListener(TaskListener listener) {
        Objects.requireNonNull(listener);

        Session session = sessions.remove(listener);
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void updateTaskFuture(UUID taskId, Future future) throws NotACancellableTaskMonitor {
        throw new NotACancellableTaskMonitor("Cannot update task future from remote");
    }

    @Override
    public void close() {
        for (Session session : sessions.values()) {
            try {
                session.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        client.close();
    }
}
