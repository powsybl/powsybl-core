/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectNode extends AbstractNodeBase<ProjectFolder> {

    protected final NodeId projectId;

    protected final AppFileSystem fileSystem;

    protected final boolean folder;

    protected ProjectNode(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem, boolean folder) {
        super(id, storage);
        this.projectId = Objects.requireNonNull(projectId);
        this.fileSystem = Objects.requireNonNull(fileSystem);
        this.folder = folder;
    }

    public NodeId getId() {
        return id;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public ProjectFolder getFolder() {
        NodeId parentNode = storage.getParentNode(id);
        return parentNode != null ? new ProjectFolder(parentNode, storage, projectId, fileSystem) : null;
    }

    public NodePath getPath() {
        return NodePath.find(this, path -> path.stream()
                                               .skip(1) // skip project node
                                               .collect(Collectors.joining(AppFileSystem.PATH_SEPARATOR)));
    }

    public Project getProject() {
        return new Project(projectId, storage, fileSystem);
    }

    public void moveTo(ProjectFolder folder) {
        Objects.requireNonNull(folder);
        storage.setParentNode(id, folder.id);
    }

    public void delete() {
        storage.deleteNode(id);
    }

    protected ProjectNode findProjectNode(NodeId nodeId) {
        String projectNodePseudoClass = storage.getNodePseudoClass(nodeId);
        if (ProjectFolder.PSEUDO_CLASS.equals(projectNodePseudoClass)) {
            return new ProjectFolder(nodeId, storage, projectId, fileSystem);
        } else {
            ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtensionByPseudoClass(projectNodePseudoClass);
            if (extension != null) {
                return extension.createProjectFile(nodeId, storage, projectId, fileSystem);
            } else {
                return new UnknownProjectFile(nodeId, storage, projectId, fileSystem);
            }
        }
    }

    protected ProjectFile findProjectFile(NodeId nodeId) {
        String projectNodePseudoClass = storage.getNodePseudoClass(nodeId);
        ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtensionByPseudoClass(projectNodePseudoClass);
        if (extension != null) {
            return extension.createProjectFile(nodeId, storage, projectId, fileSystem);
        } else {
            return new UnknownProjectFile(nodeId, storage, projectId, fileSystem);
        }
    }

    public List<ProjectFile> getBackwardDependencies() {
        return storage.getBackwardDependencies(id)
                .stream()
                .map(this::findProjectFile)
                .collect(Collectors.toList());
    }

    protected void notifyDependencyChanged() {
        getBackwardDependencies().forEach(projectFile -> {
            projectFile.onDependencyChanged();
            // propagate
            projectFile.notifyDependencyChanged();
        });
    }
}
