/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.PseudoClass;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Folder extends Node implements FolderBase<Node, Folder> {

    public static final String PSEUDO_CLASS = PseudoClass.FOLDER_PSEUDO_CLASS;

    public Folder(NodeId id, AppFileSystemStorage storage, AppFileSystem fileSystem) {
        super(id, storage, fileSystem, true);
    }

    public boolean isWritable() {
        return storage.isWritable(id);
    }

    @Override
    public List<Node> getChildren() {
        return storage.getChildNodes(id)
                .stream()
                .map(this::findNode)
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Node getChild(String name, String... more) {
        NodeId childId = getChildId(name, more);
        return childId != null ? findNode(childId) : null;
    }

    @Override
    public Folder createFolder(String name) {
        NodeId folderId = storage.getChildNode(id, name);
        if (folderId == null) {
            folderId = storage.createNode(id, name, Folder.PSEUDO_CLASS);
        }
        return new Folder(folderId, storage, fileSystem);
    }

    public Project createProject(String name) {
        NodeId projectNodeId = storage.getChildNode(id, name);
        if (projectNodeId == null) {
            projectNodeId = storage.createNode(id, name, Project.PSEUDO_CLASS);
        }
        return new Project(projectNodeId, storage, fileSystem);
    }
}
