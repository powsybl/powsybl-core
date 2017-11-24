/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Folder extends Node implements FolderBase<Node, Folder> {

    public static final String PSEUDO_CLASS = "folder";

    public Folder(FileCreationContext context) {
        super(context, true);
    }

    public boolean isWritable() {
        return storage.isWritable(info.getId());
    }

    @Override
    public List<Node> getChildren() {
        return storage.getChildNodesInfo(info.getId())
                .stream()
                .map(this::findNode)
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Node getChild(String name, String... more) {
        NodeInfo childInfo = getChildInfo(name, more);
        return childInfo != null ? findNode(childInfo) : null;
    }

    @Override
    public <T extends Node> T getChild(Class<T> clazz, String name, String... more) {
        Objects.requireNonNull(clazz);
        Node node = getChild(name, more);
        if (node != null && clazz.isAssignableFrom(node.getClass())) {
            return (T) node;
        }
        return null;
    }

    @Override
    public Folder getFolder(String name, String... more) {
        return getChild(Folder.class, name, more);
    }

    @Override
    public Folder createFolder(String name) {
        NodeId folderId = storage.getChildNode(info.getId(), name);
        if (folderId == null) {
            folderId = storage.createNode(info.getId(), name, PSEUDO_CLASS);
        }
        return new Folder(new FileCreationContext(new NodeInfo(folderId, name, PSEUDO_CLASS), storage, fileSystem));
    }

    public Project createProject(String name) {
        NodeId projectId = storage.getChildNode(info.getId(), name);
        if (projectId == null) {
            projectId = storage.createNode(info.getId(), name, Project.PSEUDO_CLASS);

            // create root project folder
            storage.createNode(projectId, Project.ROOT_FOLDER_NAME, ProjectFolder.PSEUDO_CLASS);
        }
        return new Project(new FileCreationContext(new NodeInfo(projectId, name, Project.PSEUDO_CLASS), storage, fileSystem));
    }
}
