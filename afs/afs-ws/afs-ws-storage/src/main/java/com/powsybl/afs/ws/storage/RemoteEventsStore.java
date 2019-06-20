/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.EventsStore;
import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.afs.storage.events.NodeEvent;
import com.powsybl.afs.ws.client.utils.UncheckedDeploymentException;
import com.powsybl.afs.ws.utils.AfsRestApi;
import com.powsybl.commons.util.WeakListenerList;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class RemoteEventsStore implements EventsStore {

    // To trace events per topic locally
    private final Map<String, List<NodeEvent>> topics = new HashMap<>();

    private final WeakListenerList<AppStorageListener> listeners = new WeakListenerList<>();

    public RemoteEventsStore(AppStorage storage, URI restUri) {
        URI wsUri = SocketsUtils.getWebSocketUri(restUri);
        URI endPointUri = URI.create(wsUri + "/messages/" + AfsRestApi.RESOURCE_ROOT + "/" +
                AfsRestApi.VERSION + "/node_events/" + storage.getFileSystemName());

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(new NodeEventClient(storage.getFileSystemName(), listeners), endPointUri);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (DeploymentException e) {
            throw new UncheckedDeploymentException(e);
        }
    }

    @Override
    public void pushEvent(NodeEvent event, String topic) {
        // Nothing to do
        // RemoteAppStorage --> push in server side
        topics.computeIfAbsent(topic, k -> new ArrayList<>());
        topics.get(topic).add(event);
    }

    Map<String, List<NodeEvent>> getTopics() {
        return topics;
    }

    @Override
    public void flush() {
        topics.clear();
    }

    @Override
    public void addListener(AppStorageListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(AppStorageListener l) {
        listeners.remove(l);
    }

    @Override
    public void removeListeners() {
        listeners.removeAll();
    }
}
