/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.NodeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
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
