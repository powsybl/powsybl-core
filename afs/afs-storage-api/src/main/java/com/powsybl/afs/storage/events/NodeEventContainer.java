/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * @author Chamseddine Benhamed <Chamseddine.Benhamed at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
public class NodeEventContainer {

    @JsonProperty("filesystemName")
    private final String fileSystemName;

    @JsonProperty("nodeEvent")
    private final NodeEvent nodeEvent;

    @JsonProperty("topic")
    private final String topic;

    public  NodeEventContainer() {
        this.topic = null;
        this.nodeEvent = null;
        this.fileSystemName = null;
    }

    public NodeEventContainer(NodeEvent nodeEvent, String fileSystemName, String topic) {
        this.nodeEvent = Objects.requireNonNull(nodeEvent);
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.topic = Objects.requireNonNull(topic);
    }

    public NodeEvent getNodeEvent() {
        return nodeEvent;
    }

    public String getFileSystemName() {
        return fileSystemName;
    }

    public String getTopic() {
        return topic;
    }
}
