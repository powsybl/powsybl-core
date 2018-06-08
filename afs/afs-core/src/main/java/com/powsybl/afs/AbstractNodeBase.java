/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for all node objects stored in an AFS tree.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractNodeBase<F> {

    protected final NodeInfo info;

    protected final ListenableAppStorage storage;

    protected int codeVersion;

    public AbstractNodeBase(NodeInfo info, ListenableAppStorage storage, int codeVersion) {
        this.info = Objects.requireNonNull(info);
        this.storage = Objects.requireNonNull(storage);
        this.codeVersion = codeVersion;
    }

    public abstract Optional<F> getParent();

    /**
     * An ID uniquely identifying this node in the file system tree.
     */
    public String getId() {
        return info.getId();
    }

    public String getName() {
        return info.getName();
    }

    public String getDescription() {
        return info.getDescription();
    }

    public void setDescription(String description) {
        storage.setDescription(info.getId(), description);
        info.setDescription(description);
    }

    public ZonedDateTime getCreationDate() {
        return Instant.ofEpochMilli(info.getCreationTime()).atZone(ZoneId.systemDefault());
    }

    public ZonedDateTime getModificationDate() {
        return Instant.ofEpochMilli(info.getModificationTime()).atZone(ZoneId.systemDefault());
    }

    public int getVersion() {
        return info.getVersion();
    }

    protected int getCodeVersion() {
        return codeVersion;
    }

    public boolean isAheadOfVersion() {
        return info.getVersion() > getCodeVersion();
    }

    public abstract NodePath getPath();

    public abstract boolean isFolder();

    private NodeInfo getChildInfo(NodeInfo nodeInfo, String name) {
        Objects.requireNonNull(name);
        NodeInfo childInfo = nodeInfo;
        for (String name2 : name.split(AppFileSystem.PATH_SEPARATOR)) {
            childInfo = storage.getChildNode(childInfo.getId(), name2).orElse(null);
            if (childInfo == null) {
                return null;
            }
        }
        return childInfo;
    }

    protected NodeInfo getChildInfo(String name, String... more) {
        NodeInfo childInfo = getChildInfo(info, name);
        if (childInfo == null) {
            return null;
        }
        for (String name2 : more) {
            childInfo = getChildInfo(childInfo, name2);
            if (childInfo == null) {
                return null;
            }
        }
        return childInfo;
    }

    @Override
    public String toString() {
        return getName();
    }
}
