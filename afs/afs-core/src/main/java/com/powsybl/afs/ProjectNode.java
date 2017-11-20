/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectNode extends AbstractNodeBase<ProjectFolder> {

    protected final NodeInfo projectInfo;

    protected final AppFileSystem fileSystem;

    protected final boolean folder;

    protected ProjectNode(NodeInfo info, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem, boolean folder) {
        super(info, storage);
        this.projectInfo = Objects.requireNonNull(projectInfo);
        this.fileSystem = Objects.requireNonNull(fileSystem);
        this.folder = folder;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public ProjectFolder getParent() {
        NodeInfo parentInfo = storage.getParentNodeInfo(info.getId());
        return ProjectFolder.PSEUDO_CLASS.equals(parentInfo.getPseudoClass()) ? new ProjectFolder(parentInfo, storage, projectInfo, fileSystem) : null;
    }

    private static boolean pathStop(ProjectNode projectNode) {
        return projectNode.getParent() == null;
    }

    private static String pathToString(List<String> path) {
        return path.stream().skip(1).collect(Collectors.joining(AppFileSystem.PATH_SEPARATOR));
    }

    @Override
    public NodePath getPath() {
        return NodePath.find(this, ProjectNode::pathStop, ProjectNode::pathToString);
    }

    public Project getProject() {
        return new Project(projectInfo, storage, fileSystem);
    }

    public void moveTo(ProjectFolder folder) {
        Objects.requireNonNull(folder);
        storage.setParentNode(info.getId(), folder.getId());
    }

    public void delete() {
        storage.deleteNode(info.getId());
    }

    protected ProjectNode findProjectNode(NodeInfo nodeInfo) {
        if (ProjectFolder.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new ProjectFolder(nodeInfo, storage, projectInfo, fileSystem);
        } else {
            ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
            if (extension != null) {
                return extension.createProjectFile(nodeInfo, storage, projectInfo, fileSystem);
            } else {
                return new UnknownProjectFile(nodeInfo, storage, projectInfo, fileSystem);
            }
        }
    }

    protected ProjectFile findProjectFile(NodeInfo nodeInfo) {
        ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
        if (extension != null) {
            return extension.createProjectFile(nodeInfo, storage, projectInfo, fileSystem);
        } else {
            return new UnknownProjectFile(nodeInfo, storage, projectInfo, fileSystem);
        }
    }

    public List<ProjectFile> getBackwardDependencies() {
        return storage.getBackwardDependenciesInfo(info.getId())
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
