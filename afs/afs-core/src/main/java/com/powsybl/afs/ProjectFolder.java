/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFolder extends ProjectNode implements FolderBase<ProjectNode, ProjectFolder> {

    public static final String PSEUDO_CLASS = "projectFolder";

    public ProjectFolder(NodeInfo info, AppFileSystemStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        super(info, storage, projectInfo, fileSystem, true);
    }

    @Override
    public List<ProjectNode> getChildren() {
        return storage.getChildNodesInfo(info.getId())
                .stream()
                .map(this::findProjectNode)
                .sorted(Comparator.comparing(ProjectNode::getName))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectNode getChild(String name, String... more) {
        NodeInfo childInfo = getChildInfo(name, more);
        return childInfo != null ? findProjectNode(childInfo) : null;
    }

    @Override
    public <T extends ProjectNode> T getChild(Class<T> clazz, String name, String... more) {
        Objects.requireNonNull(clazz);
        ProjectNode projectNode = getChild(name, more);
        if (projectNode != null && clazz.isAssignableFrom(projectNode.getClass())) {
            return (T) projectNode;
        }
        return null;
    }

    @Override
    public ProjectFolder getFolder(String name, String... more) {
        return getChild(ProjectFolder.class, name, more);
    }

    @Override
    public ProjectFolder createFolder(String name) {
        NodeId folderId = storage.getChildNode(info.getId(), name);
        if (folderId == null) {
            folderId = storage.createNode(info.getId(), name, ProjectFolder.PSEUDO_CLASS);
        }
        return new ProjectFolder(new NodeInfo(folderId, name, ProjectFolder.PSEUDO_CLASS), storage, projectInfo, fileSystem);
    }

    public <F extends ProjectFile, B extends ProjectFileBuilder<F>> B fileBuilder(Class<B> clazz) {
        Objects.requireNonNull(clazz);
        ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtension(clazz);
        ProjectFileBuilder<F> builder = (ProjectFileBuilder<F>) extension.createProjectFileBuilder(info, storage, projectInfo, fileSystem);
        return (B) builder;
    }
}
