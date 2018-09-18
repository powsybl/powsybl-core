package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class NodeNameUpdated extends NodeEvent {

    @JsonProperty("name")
    private final String name;

    @JsonCreator
    public NodeNameUpdated(@JsonProperty("id") String id,
                           @JsonProperty("name") String name) {
        super(id, NodeEventType.NODE_NAME_UPDATED);
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeNameUpdated) {
            NodeNameUpdated other = (NodeNameUpdated) obj;
            return id.equals(other.id) && Objects.equals(name, other.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeNameUpdated(id=" + id + ", name=" + name + ")";
    }
}
