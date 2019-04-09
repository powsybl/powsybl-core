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

    public NodeEventClient(String fileSystemName, WeakListenerList<AppStorageListener> listeners, RemoteServiceConfig config) {
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.listeners = Objects.requireNonNull(listeners);
        this.config = Objects.requireNonNull(config);
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
            LOGGER.warn("Node event websocket session '{}' closed for file system '{}'. Auto-reconnection is enabled.", session.getId(), fileSystemName);
            toReconnectInAfs(session);
        } else {
            LOGGER.error("Node event websocket session '{}' closed for file system '{}'. Auto-reconnection is disabled.", session.getId(), fileSystemName);
        }
    }

    private void toReconnectInAfs(Session session) {
        int currentReconnectionInterval = config.getReconnectionInitialInterval();
        int counter = 0;
        boolean toReconnection = true;
        long closedTime = Instant.now().getEpochSecond();
        while (toReconnection) {

            long currentTime = Instant.now().getEpochSecond();
            if (currentTime - closedTime > config.getReconnectionMax()) {
                LOGGER.error("Node event websocket session '{}' closed for file system '{} with reconnection maximum timeout reached ={}'", session.getId(), fileSystemName, currentTime - closedTime);
                return;
            }
            currentReconnectionInterval = updateReconnectionInterval(counter, currentReconnectionInterval);
            try {
                URI wsUri = RemoteListenableAppStorage.getWebSocketUri(config.getRestUri());
                URI endPointUri = URI.create(wsUri + "/messages/" + AfsRestApi.RESOURCE_ROOT + "/" +
                        AfsRestApi.VERSION + "/node_events/" + fileSystemName);
                LOGGER.info("Trying to reconnect to node event websocket for file system '{}' at {}", fileSystemName, endPointUri);
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, endPointUri);
                toReconnection = false;
                LOGGER.info("Reconnected successfully to node event websocket for file system '{}' at {}", fileSystemName, endPointUri);

            } catch (DeploymentException e) {
                if (e.getCause().getCause() instanceof ConnectException) {
                    try {
                        LOGGER.warn(e.getCause().getCause().getMessage());
                        counter = counter + 1;
                        LOGGER.warn("Failed to reconnect to node event websocket for file system {}. Will retry in {} seconds.", fileSystemName, currentReconnectionInterval);
                        sleep(currentReconnectionInterval * 1000L);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                        throw new UncheckedInterruptedException(e1);
                    }

                } else {
                    LOGGER.error("Node event websocket session '{}' closed for file system '{}'", session.getId(), fileSystemName);
                    return;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private int updateReconnectionInterval(int counter, int currentReconnectionInterval) {
        int currentReconnectionIntervalLocal;
        if (counter > 0) {
            currentReconnectionIntervalLocal = currentReconnectionInterval * config.getReconnectionIntervalMutiplier();
            if (currentReconnectionIntervalLocal > config.getReconnectionTimeout()) {
                currentReconnectionIntervalLocal = config.getReconnectionTimeout();
            }
        } else {
            currentReconnectionIntervalLocal = config.getReconnectionInitialInterval();
        }
        return currentReconnectionIntervalLocal;
    }

}
