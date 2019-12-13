/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.afs.storage.NodeGenericMetadata;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeMetadataUpdated extends NodeEvent {

    public static final String TYPE = "NODE_METADATA_UPDATED";

    @JsonProperty("metadata")
    private final NodeGenericMetadata metadata;

    @JsonCreator
    public NodeMetadataUpdated(@JsonProperty("id") String id,
                               @JsonProperty("metadata") NodeGenericMetadata metadata) {
        super(id, TYPE);
        this.metadata = Objects.requireNonNull(metadata);
    }

    public NodeGenericMetadata getMetadata() {
        return metadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeMetadataUpdated) {
            NodeMetadataUpdated other = (NodeMetadataUpdated) obj;
            return id.equals(other.id) && Objects.equals(metadata, other.metadata);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeMetadataUpdated(id=" + id + ", metadata=" + metadata + ")";
    }
}
