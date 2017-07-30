/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.afs.storage.PseudoClass;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFolder extends ProjectNode implements FolderBase<ProjectNode, ProjectFolder> {

    public static final String PSEUDO_CLASS = PseudoClass.PROJECT_FOLDER_PSEUDO_CLASS;

    public ProjectFolder(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem);
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public List<ProjectNode> getChildren() {
        return storage.getChildNodes(id)
                .stream()
                .map(this::findProjectNode)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectNode getChild(String name, String... more) {
        NodeId childId = getChildId(name, more);
        return childId != null ? findProjectNode(childId) : null;
    }

    @Override
    public ProjectFolder createFolder(String name) {
        NodeId folderId = storage.createNode(id, name, ProjectFolder.PSEUDO_CLASS);
        return new ProjectFolder(folderId, storage, projectId, fileSystem);
    }

    public <F extends ProjectFile, B extends ProjectFileBuilder<F>> B fileBuilder(Class<B> clazz) {
        Objects.requireNonNull(clazz);
        ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtension(clazz);
        ProjectFileBuilder<F> builder = (ProjectFileBuilder<F>) extension.createProjectFileBuilder(id, storage, projectId, fileSystem);
        return (B) builder;
    }
}
