/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.afs.storage.events.NodeEventList;
import com.powsybl.afs.ws.client.utils.RemoteServiceConfig;
import com.powsybl.afs.ws.utils.AfsRestApi;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import com.powsybl.commons.util.WeakListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import static java.lang.Thread.sleep;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ClientEndpoint(decoders = {NodeEventListDecoder.class})
public class NodeEventClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventClient.class);

    private final String fileSystemName;

    private final WeakListenerList<AppStorageListener> listeners;

    private final RemoteServiceConfig config;

    private int reconnectionInterval;

    public NodeEventClient(String fileSystemName, WeakListenerList<AppStorageListener> listeners, RemoteServiceConfig config) {
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.listeners = Objects.requireNonNull(listeners);
        this.config = Objects.requireNonNull(config);
        this.reconnectionInterval = config.getReconnectionInitialInterval();
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.trace("Node event websocket session '{}' opened for file system '{}'", session.getId(), fileSystemName);
    }

    @OnMessage
    public void onMessage(Session session, NodeEventList nodeEventList) {
        LOGGER.trace("Node event websocket session '{}' of file system '{}' received an event list: {}",
                session.getId(), fileSystemName, nodeEventList);
        listeners.log();
        listeners.notify(l -> l.onEvents(nodeEventList));
    }

    @OnError
    public void onError(Throwable t) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(t.toString(), t);
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (config.isAutoreconnectEnabled()) {
            toReconnectInAfs(session);
        } else {
            LOGGER.warn("Node event websocket session '{}' closed for file system '{}'", session.getId(), fileSystemName);
        }
    }

    private void toReconnectInAfs(Session session) {
        int counter = 0;
        boolean toReconnection = true;
        long dateInitial = Instant.now().getEpochSecond();
        while (toReconnection) {

            long dateReconnection = Instant.now().getEpochSecond();
            if (dateReconnection - dateInitial > config.getReconnectionMax()) {
                LOGGER.warn("Node event websocket session '{}' closed for file system '{}'", session.getId(), fileSystemName);
                return;
            }
            updateReconnectionInterval(counter);
            try {
                URI wsUri = RemoteListenableAppStorage.getWebSocketUri(config.getRestUri());
                URI endPointUri = URI.create(wsUri + "/messages/" + AfsRestApi.RESOURCE_ROOT + "/" +
                        AfsRestApi.VERSION + "/node_events/" + fileSystemName);
                LOGGER.debug("Connecting to node event websocket at {}", endPointUri);
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, endPointUri);
                toReconnection = false;

            } catch (DeploymentException e) {
                if (e.getCause().getCause() instanceof ConnectException) {
                    try {
                        LOGGER.error(e.getCause().getCause().getMessage(), e);
                        counter = counter + 1;
                        sleep(this.reconnectionInterval * 1000L);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        throw new UncheckedInterruptedException(e1);
                    }

                } else {
                    LOGGER.warn("Node event websocket session '{}' closed for file system '{}'", session.getId(), fileSystemName);
                    return;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void updateReconnectionInterval(int counter) {
        if (counter > 0) {
            if (this.reconnectionInterval > config.getReconnectionMaxInterval()) {
                this.reconnectionInterval = config.getReconnectionMaxInterval();
            } else {
                this.reconnectionInterval = this.reconnectionInterval * config.getReconnectionIntervalMutiplier();
            }
        } else {
            this.reconnectionInterval = config.getReconnectionInitialInterval();
        }
    }

}
