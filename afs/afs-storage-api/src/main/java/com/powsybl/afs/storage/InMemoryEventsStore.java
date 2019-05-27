package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.NodeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryEventsStore implements EventsStore {

    private HashMap<String, List<NodeEvent>> topics;

    public InMemoryEventsStore() {
        topics = new HashMap<>();
    }

    @Override
    public void pushEvent(NodeEvent event, String fileSystem, String topic) {
        if (topics.containsKey(topic)) {
            topics.get(topic).add(event);
        } else {
            ArrayList<NodeEvent> topicEvents = new ArrayList<>();
            topicEvents.add(event);
            topics.put(topic, topicEvents);
        }
    }

    Map<String, List<NodeEvent>> getTopics() {
        return topics;
    }
}
