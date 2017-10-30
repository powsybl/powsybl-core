/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Node extends AbstractNodeBase<Folder> {

    protected final AppFileSystem fileSystem;

    protected final boolean folder;

    protected Node(NodeId id, AppFileSystemStorage storage, AppFileSystem fileSystem, boolean folder) {
        super(id, storage);
        this.fileSystem = Objects.requireNonNull(fileSystem);
        this.folder = folder;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public Folder getParent() {
        NodeId parentNodeId = storage.getParentNode(id);
        return parentNodeId != null ? new Folder(parentNodeId, storage, fileSystem) : null;
    }

    @Override
    public NodePath getPath() {
        return NodePath.find(this, path -> {
            StringBuilder builder = new StringBuilder();
            builder.append(path.get(0))
                    .append(AppFileSystem.FS_SEPARATOR);
            for (int i = 1; i < path.size(); i++) {
                builder.append(AppFileSystem.PATH_SEPARATOR).append(path.get(i));
            }
            return builder.toString();
        });
    }

    public AppFileSystem getFileSystem() {
        return fileSystem;
    }

    protected Node findNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        String nodePseudoClass = storage.getNodePseudoClass(nodeId);
        if (Folder.PSEUDO_CLASS.equals(nodePseudoClass)) {
            return new Folder(nodeId, storage, fileSystem);
        } else if (Project.PSEUDO_CLASS.equals(nodePseudoClass)) {
            return new Project(nodeId, storage, fileSystem);
        } else {
            FileExtension extension = fileSystem.getData().getFileExtensionByPseudoClass(nodePseudoClass);
            if (extension != null) {
                return extension.createFile(nodeId, storage, fileSystem);
            } else {
                return new UnknownFile(nodeId, storage, fileSystem);
            }
        }
    }
}
