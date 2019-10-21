/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectNode extends AbstractNodeBase<ProjectFolder> {

    protected final Project project;

    protected final boolean folder;

    protected ProjectNode(ProjectFileCreationContext context, int codeVersion, boolean folder) {
        super(context.getInfo(), context.getStorage(), codeVersion);
        this.project = Objects.requireNonNull(context.getProject());
        this.folder = folder;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public Optional<ProjectFolder> getParent() {
        return getParentInfo()
                .filter(parentInfo -> ProjectFolder.PSEUDO_CLASS.equals(parentInfo.getPseudoClass()))
                .map(parentInfo -> new ProjectFolder(new ProjectFileCreationContext(parentInfo, storage, project)));
    }

    private static boolean pathStop(ProjectNode projectNode) {
        return !projectNode.getParent().isPresent();
    }

    private static String pathToString(List<String> path) {
        return path.stream().skip(1).collect(Collectors.joining(AppFileSystem.PATH_SEPARATOR));
    }

    @Override
    public NodePath getPath() {
        return NodePath.find(this, ProjectNode::pathStop, ProjectNode::pathToString);
    }

    public Project getProject() {
        return project;
    }

    public void delete() {
        // has to be done before delete!!!
        invalidate();

        storage.deleteNode(info.getId());
        storage.flush();
    }

    public List<ProjectFile> getBackwardDependencies() {
        return storage.getBackwardDependencies(info.getId())
                .stream()
                .map(project::createProjectFile)
                .collect(Collectors.toList());
    }

    protected void invalidate() {
        // propagate
        getBackwardDependencies().forEach(ProjectNode::invalidate);
    }

    public AppFileSystem getFileSystem() {
        return project.getFileSystem();
    }

    public <U> U findService(Class<U> serviceClass) {
        return project.getFileSystem().getData().findService(serviceClass, storage.isRemote());
    }
}
