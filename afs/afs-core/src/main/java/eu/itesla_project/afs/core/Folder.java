/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.core;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.afs.storage.PseudoClass;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Folder extends Node implements FolderBase<Node> {

    public static final String PSEUDO_CLASS = PseudoClass.FOLDER_PSEUDO_CLASS;

    public Folder(NodeId id, AppFileSystemStorage storage, AppFileSystem fileSystem) {
        super(id, storage, fileSystem);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    public boolean isWritable() {
        return storage.isWritable(id);
    }

    @Override
    public List<Node> getChildren() {
        return storage.getChildNodes(id)
                .stream()
                .map(this::findNode)
                .collect(Collectors.toList());
    }

    @Override
    public Node getChild(String name, String... more) {
        NodeId childId = getChildId(name, more);
        return childId != null ? findNode(childId) : null;
    }

    public Project createProject(String name, String description) {
        NodeId projectId = storage.createNode(id, name, Project.PSEUDO_CLASS);
        storage.setStringAttribute(projectId, "description", description);
        return new Project(projectId, storage, fileSystem);
    }
}