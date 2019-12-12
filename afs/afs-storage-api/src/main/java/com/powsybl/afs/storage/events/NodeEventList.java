/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeEventList {

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("events")
    private List<NodeEvent> events;

    public NodeEventList() {
        this(new ArrayList<>(), null);
    }

    public NodeEventList(String topic) {
        this(new ArrayList<>(), topic);
    }

    public NodeEventList(String topic, NodeEvent... events) {
        this(new ArrayList<>(Arrays.asList(events)), topic);
    }

    public NodeEventList(NodeEvent... events) {
        this(new ArrayList<>(Arrays.asList(events)), null);
    }

    @JsonCreator
    public NodeEventList(@JsonProperty("events") List<NodeEvent> events, @JsonProperty("topic") String topic) {
        this.events = Objects.requireNonNull(events);
        this.topic = topic;
    }

    public void addEvent(NodeEvent event) {
        events.add(event);
    }

    public List<NodeEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public int hashCode() {
        return events.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeEventList) {
            NodeEventList other = (NodeEventList) obj;
            return events.equals(other.events);
        }
        return false;
    }

    @Override
    public String toString() {
        return events.toString();
    }
}
