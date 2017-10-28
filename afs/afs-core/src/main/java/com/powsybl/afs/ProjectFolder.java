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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFolder extends ProjectNode implements FolderBase<ProjectNode, ProjectFolder> {

    public static final String PSEUDO_CLASS = PseudoClass.PROJECT_FOLDER_PSEUDO_CLASS;

    public ProjectFolder(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem, true);
    }

    @Override
    public List<ProjectNode> getChildren() {
        return storage.getChildNodes(id)
                .stream()
                .map(this::findProjectNode)
                .sorted(Comparator.comparing(ProjectNode::getName))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectNode getChild(String name, String... more) {
        NodeId childId = getChildId(name, more);
        return childId != null ? findProjectNode(childId) : null;
    }

    @Override
    public ProjectFolder createFolder(String name) {
        NodeId folderNodeId = storage.getChildNode(id, name);
        if (folderNodeId == null) {
            folderNodeId = storage.createNode(id, name, ProjectFolder.PSEUDO_CLASS);
        }
        return new ProjectFolder(folderNodeId, storage, projectId, fileSystem);
    }

    public <F extends ProjectFile, B extends ProjectFileBuilder<F>> B fileBuilder(Class<B> clazz) {
        Objects.requireNonNull(clazz);
        ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtension(clazz);
        ProjectFileBuilder<F> builder = (ProjectFileBuilder<F>) extension.createProjectFileBuilder(id, storage, projectId, fileSystem);
        return (B) builder;
    }
}
