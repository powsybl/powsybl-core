/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.storage;

import com.powsybl.afs.storage.EventsStore;
import com.powsybl.afs.storage.ForwardingAppStorage;
import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;
import com.powsybl.commons.util.WeakListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteListenableAppStorage extends ForwardingAppStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteListenableAppStorage.class);

    private final WeakListenerList<AppStorageListener> listeners = new WeakListenerList<>();

    private final RemoteEventsStore eventsStore;

    public RemoteListenableAppStorage(RemoteAppStorage storage, URI restUri) {
        super(storage);
        this.eventsStore = new RemoteEventsStore(storage, restUri);
    }

    @Override
    public EventsStore getEventsStore() {
        return eventsStore;
    }

    static URI getWebSocketUri(URI restUri) {
        try {
            String wsScheme;
            switch (restUri.getScheme()) {
                case "http":
                    wsScheme = "ws";
                    break;
                case "https":
                    wsScheme = "wss";
                    break;
                default:
                    throw new AssertionError("Unexpected scheme " + restUri.getScheme());
            }
            return new URI(wsScheme, restUri.getUserInfo(), restUri.getHost(), restUri.getPort(), restUri.getPath(), restUri.getQuery(), null);
        } catch (URISyntaxException e) {
            throw new UncheckedUriSyntaxException(e);
        }
    }
}
