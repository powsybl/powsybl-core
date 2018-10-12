/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.Objects;

/**
 * Low level information about a node in an AFS tree. Should seldom be used by AFS API users.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfo {

    private final String id;

    private String name;

    private final String pseudoClass;

    private String description = "";

    private final long creationTime;

    private long modificationTime;

    private int version;

    private final NodeGenericMetadata genericMetadata;

    public NodeInfo(String id, String name, String pseudoClass, String description, long creationTime, long modificationTime,
                    int version, NodeGenericMetadata genericMetadata) {
        this.id = Objects.requireNonNull(id);
        this.name = checkName(name);
        this.pseudoClass = Objects.requireNonNull(pseudoClass);
        this.description = Objects.requireNonNull(description);
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.version = version;
        this.genericMetadata = Objects.requireNonNull(genericMetadata);
    }

    /**
     * Returns the ID of this node, uniquely identifying it in the file system.
     */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = checkName(name);
    }

    private static String checkName(String name) {
        Objects.requireNonNull(name);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name should not be empty");
        }
        return name;
    }

    public String getPseudoClass() {
        return pseudoClass;
    }

    public String getDescription() {
        return description;
    }

    public NodeInfo setDescription(String description) {
        this.description = Objects.requireNonNull(description);
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public NodeInfo setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public NodeInfo setVersion(int version) {
        this.version = version;
        return this;
    }

    public NodeGenericMetadata getGenericMetadata() {
        return genericMetadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, pseudoClass, description, creationTime, modificationTime, version, genericMetadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeInfo) {
            NodeInfo other = (NodeInfo) obj;
            return id.equals(other.id) && name.equals(other.name) && pseudoClass.equals(other.pseudoClass) &&
                    description.equals(other.description) && creationTime == other.creationTime &&
                    modificationTime == other.modificationTime && version == other.version &&
                    genericMetadata.equals(other.genericMetadata);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeInfo(id=" + id + ", name=" + name + ", pseudoClass=" + pseudoClass +
                ", description=" + description + ", creationTime=" + creationTime +
                ", modificationTime=" + modificationTime + ", version=" + version +
                ", genericMetadata=" + genericMetadata + ")";
    }
}
