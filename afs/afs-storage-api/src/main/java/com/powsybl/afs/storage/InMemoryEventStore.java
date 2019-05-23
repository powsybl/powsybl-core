package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.NodeEvent;

public class InMemoryEventStore implements EventStore {

    @Override
    public void pushEvent(NodeEvent event, String fileSystem) {
       // TO DO
    }

    @Override
    public void addTopic() {
        // TO DO
    }
}
