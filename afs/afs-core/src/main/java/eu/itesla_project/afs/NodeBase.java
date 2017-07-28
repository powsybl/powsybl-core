/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class NodeBase<FOLDER> {

    protected final NodeId id;

    protected final AppFileSystemStorage storage;

    public NodeBase(NodeId id, AppFileSystemStorage storage) {
        this.id = Objects.requireNonNull(id);
        this.storage = Objects.requireNonNull(storage);
    }

    public abstract FOLDER getFolder();

    public String getName() {
        return storage.getNodeName(id);
    }

    public abstract NodePath getPath();

    public abstract boolean isFolder();

    private NodeId getChildId(NodeId nodeId, String name) {
        Objects.requireNonNull(name);
        NodeId childId = nodeId;
        for (String name2 : name.split(AppFileSystem.PATH_SEPARATOR)) {
            childId = storage.getChildNode(childId, name2);
            if (childId == null) {
                return null;
            }
        }
        return childId;
    }

    protected NodeId getChildId(String name, String... more) {
        NodeId childId = getChildId(id, name);
        for (String name2 : more) {
            childId = getChildId(childId, name2);
            if (childId == null) {
                return null;
            }
        }
        return childId;
    }

    @Override
    public String toString() {
        return getName();
    }
}
