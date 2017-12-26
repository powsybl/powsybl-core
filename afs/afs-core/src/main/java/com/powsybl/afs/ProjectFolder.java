/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFolder extends ProjectNode implements FolderBase<ProjectNode, ProjectFolder> {

    public static final String PSEUDO_CLASS = "projectFolder";
    public static final int VERSION = 0;

    public ProjectFolder(ProjectFileCreationContext context) {
        super(context, VERSION, true);
    }

    @Override
    public List<ProjectNode> getChildren() {
        return storage.getChildNodes(info.getId())
                .stream()
                .map(fileSystem::findProjectNode)
                .sorted(Comparator.comparing(ProjectNode::getName))
                .collect(Collectors.toList());
    }

    @Override
    public ProjectNode getChild(String name, String... more) {
        NodeInfo childInfo = getChildInfo(name, more);
        return childInfo != null ? fileSystem.findProjectNode(childInfo) : null;
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
        NodeInfo folderInfo = storage.getChildNode(info.getId(), name);
        if (folderInfo == null) {
            folderInfo = storage.createNode(info.getId(), name, PSEUDO_CLASS, "", VERSION, Collections.emptyMap(),
                    Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }
        return new ProjectFolder(new ProjectFileCreationContext(folderInfo, storage, fileSystem));
    }

    public <F extends ProjectFile, B extends ProjectFileBuilder<F>> B fileBuilder(Class<B> clazz) {
        Objects.requireNonNull(clazz);
        ProjectFileExtension extension = fileSystem.getData().getProjectFileExtension(clazz);
        ProjectFileBuilder<F> builder = (ProjectFileBuilder<F>) extension.createProjectFileBuilder(new ProjectFileBuildContext(info, storage, fileSystem));
        return (B) builder;
    }
}
