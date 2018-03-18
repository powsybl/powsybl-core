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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectNode extends AbstractNodeBase<ProjectFolder> {

    protected final AppFileSystem fileSystem;

    protected final boolean folder;

    protected ProjectNode(ProjectFileCreationContext context, int codeVersion, boolean folder) {
        super(context.getInfo(), context.getStorage(), codeVersion);
        this.fileSystem = Objects.requireNonNull(context.getFileSystem());
        this.folder = folder;
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public Optional<ProjectFolder> getParent() {
        return storage.getParentNode(info.getId())
                .filter(parentInfo -> ProjectFolder.PSEUDO_CLASS.equals(parentInfo.getPseudoClass()))
                .map(parentInfo -> new ProjectFolder(new ProjectFileCreationContext(parentInfo, storage, fileSystem)));
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
        // walk the node hierarchy until finding a project node
        NodeInfo parentInfo = storage.getParentNode(info.getId()).orElseThrow(AssertionError::new);
        while (!Project.PSEUDO_CLASS.equals(parentInfo.getPseudoClass())) {
            parentInfo = storage.getParentNode(parentInfo.getId()).orElseThrow(AssertionError::new);
        }
        return new Project(new FileCreationContext(parentInfo, storage, fileSystem));
    }

    public void moveTo(ProjectFolder folder) {
        Objects.requireNonNull(folder);
        storage.setParentNode(info.getId(), folder.getId());
        storage.flush();
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
                .map(fileSystem::createProjectFile)
                .collect(Collectors.toList());
    }

    protected void invalidate() {
        // propagate
        getBackwardDependencies().forEach(ProjectNode::invalidate);
    }

    public AppFileSystem getFileSystem() {
        return fileSystem;
    }
}
