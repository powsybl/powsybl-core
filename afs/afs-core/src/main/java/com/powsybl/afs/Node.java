/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import com.powsybl.afs.storage.NodeInfo;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Node extends AbstractNodeBase<Folder> {

    protected final AppFileSystem fileSystem;

    protected final boolean folder;

    protected Node(FileCreationContext context, int codeVersion, boolean folder) {
        super(context.getInfo(), context.getStorage(), codeVersion);
        this.fileSystem = Objects.requireNonNull(context.getFileSystem());
        this.folder = folder;
    }

    public void delete() {
        storage.deleteNode(info.getId());
        storage.flush();
    }

    public void rename(String name) {
        Objects.requireNonNull(name);
        storage.renameNode(info.getId(), name);
        storage.flush();
    }

    public void moveTo(Folder folder) {
        Objects.requireNonNull(folder);
        boolean ancestorDetected = false;
        for (NodeInfo nodeInfo : findNodeAncesters(folder)) {
            if (info.getId().equals(nodeInfo.getId())) {
                ancestorDetected = true;
            }
        }
        if (!ancestorDetected) {
            storage.setParentNode(info.getId(), folder.getId());
            storage.flush();
        }
    }

    private List<NodeInfo> findNodeAncesters(Folder folder) {
        List<NodeInfo> ancesterNodes = new ArrayList<>();
        storage.getParentNode(folder.getId()).ifPresent(nodeParent -> {
            while (nodeParent != null) {
                ancesterNodes.add(nodeParent);
                if (storage.getParentNode(nodeParent.getId()).isPresent()) {
                    nodeParent = storage.getParentNode(nodeParent.getId()).get();
                } else {
                    break;
                }
            }
        });
        return ancesterNodes;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public Optional<Folder> getParent() {
        return storage.getParentNode(info.getId()).map(parentInfo -> new Folder(new FileCreationContext(parentInfo, storage, fileSystem)));
    }

    private static boolean pathStop(Node node) {
        return !node.getParent().isPresent();
    }

    private static String pathToString(List<String> path) {
        StringBuilder builder = new StringBuilder();
        builder.append(path.get(0))
                .append(AppFileSystem.FS_SEPARATOR);
        for (int i = 1; i < path.size(); i++) {
            builder.append(AppFileSystem.PATH_SEPARATOR).append(path.get(i));
        }
        return builder.toString();
    }

    @Override
    public NodePath getPath() {
        return NodePath.find(this, Node::pathStop, Node::pathToString);
    }

    public AppFileSystem getFileSystem() {
        return fileSystem;
    }
}
