/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.*;
import com.powsybl.commons.util.WeakListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public abstract class AbstractAppStorage implements AppStorage, ListenableAppStorage {

    protected EventsStore eventsStore;

    private  final Logger logger = LoggerFactory.getLogger(AbstractAppStorage.class);

    private final WeakListenerList<AppStorageListener> listeners = new WeakListenerList<>();

    private NodeEventList eventList = new NodeEventList();

    private final Lock lock = new ReentrantLock();

    @Override
    public void setEventStore(EventsStore eventStore) {
        this.eventsStore = eventStore;
    }

    @Override
    public EventsStore getEventStore() {
        return this.eventsStore;
    }

    @Override
    public void flush() {
        lock.lock();
        try {
            listeners.log();
            listeners.notify(l -> l.onEvents(eventList));
            eventList = new NodeEventList();
        } finally {
            lock.unlock();
        }
    }

    protected void pushEvent(NodeEvent event, String topic) {
        eventList.addEvent(event);
        if (eventsStore == null) {
            logger.warn("Event can't be pushed : No EventStore instance is available.");
            return;
        }
        eventsStore.pushEvent(event, topic);
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
