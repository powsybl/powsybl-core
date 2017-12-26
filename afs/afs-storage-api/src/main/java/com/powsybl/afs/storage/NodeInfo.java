/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfo {

    private final NodeId id;

    private final String name;

    private final String pseudoClass;

    private String description;

    private long creationTime;

    private long modificationTime;

    private int version;

    private final Map<String, String> stringMetadata;

    private final Map<String, Double> doubleMetadata;

    private final Map<String, Integer> intMetadata;

    private final Map<String, Boolean> booleanMetadata;

    public NodeInfo(NodeId id, String name, String pseudoClass, String description, long creationTime, long modificationTime,
                    int version, Map<String, String> stringMetadata, Map<String, Double> doubleMetadata, Map<String, Integer> intMetadata,
                    Map<String, Boolean> booleanMetadata) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.pseudoClass = Objects.requireNonNull(pseudoClass);
        this.description = Objects.requireNonNull(description);
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.version = version;
        this.stringMetadata = Objects.requireNonNull(stringMetadata);
        this.doubleMetadata = Objects.requireNonNull(doubleMetadata);
        this.intMetadata = Objects.requireNonNull(intMetadata);
        this.booleanMetadata = Objects.requireNonNull(booleanMetadata);
    }

    public NodeId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPseudoClass() {
        return pseudoClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description);
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, String> getStringMetadata() {
        return stringMetadata;
    }

    public Map<String, Double> getDoubleMetadata() {
        return doubleMetadata;
    }

    public Map<String, Integer> getIntMetadata() {
        return intMetadata;
    }

    public Map<String, Boolean> getBooleanMetadata() {
        return booleanMetadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, pseudoClass, description, creationTime, modificationTime, version,
                stringMetadata, doubleMetadata, intMetadata, booleanMetadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeInfo) {
            NodeInfo other = (NodeInfo) obj;
            return id.equals(other.id) && name.equals(other.name) && pseudoClass.equals(other.pseudoClass) &&
                    description.equals(other.description) && creationTime == other.creationTime &&
                    modificationTime == other.modificationTime && version == other.version &&
                    stringMetadata.equals(other.stringMetadata) && doubleMetadata.equals(other.doubleMetadata) &&
                    intMetadata.equals(other.intMetadata) && booleanMetadata.equals(other.booleanMetadata);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeInfo(id=" + id + ", name=" + name + ", pseudoClass=" + pseudoClass +
                ", description=" + description + ", creationTime=" + creationTime +
                ", modificationTime=" + modificationTime + ", version=" + version +
                ", stringMetadata=" + stringMetadata + ", doubleMetadata=" + doubleMetadata +
                ", intMetadata=" + intMetadata + ", booleanMetadata=" + booleanMetadata + ")";
    }
}
