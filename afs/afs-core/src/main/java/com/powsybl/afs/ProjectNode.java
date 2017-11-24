/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

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

    protected ProjectNode(ProjectFileCreationContext context, boolean folder) {
        super(context.getInfo(), context.getStorage());
        this.projectInfo = Objects.requireNonNull(context.getProjectInfo());
        this.fileSystem = Objects.requireNonNull(context.getFileSystem());
        this.folder = folder;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public ProjectFolder getParent() {
        NodeInfo parentInfo = storage.getParentNodeInfo(info.getId());
        return ProjectFolder.PSEUDO_CLASS.equals(parentInfo.getPseudoClass()) ? new ProjectFolder(new ProjectFileCreationContext(parentInfo, storage, projectInfo, fileSystem))
                                                                              : null;
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
        return new Project(new FileCreationContext(projectInfo, storage, fileSystem));
    }

    public void moveTo(ProjectFolder folder) {
        Objects.requireNonNull(folder);
        storage.setParentNode(info.getId(), folder.getId());
    }

    public void delete() {
        storage.deleteNode(info.getId());
    }

    protected ProjectNode findProjectNode(NodeInfo nodeInfo) {
        ProjectFileCreationContext context = new ProjectFileCreationContext(nodeInfo, storage, projectInfo, fileSystem);
        if (ProjectFolder.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new ProjectFolder(context);
        } else {
            ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
            if (extension != null) {
                return extension.createProjectFile(context);
            } else {
                return new UnknownProjectFile(context);
            }
        }
    }

    protected ProjectFile findProjectFile(NodeInfo nodeInfo) {
        ProjectFileCreationContext context = new ProjectFileCreationContext(nodeInfo, storage, projectInfo, fileSystem);
        ProjectFileExtension extension = getProject().getFileSystem().getData().getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
        if (extension != null) {
            return extension.createProjectFile(context);
        } else {
            return new UnknownProjectFile(context);
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
