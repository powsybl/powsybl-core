/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.afs.storage.events.NodeCreated;
import com.powsybl.afs.storage.events.NodeEvent;
import com.powsybl.afs.storage.events.NodeRemoved;
import com.powsybl.commons.util.WeakListenerList;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A folder in a {@link Project} tree.
 *
 * <p>
 * Project folders may have children project folders or files, and provides methods to create new children.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFolder extends ProjectNode implements FolderBase<ProjectNode, ProjectFolder> {

    public static final String PSEUDO_CLASS = "projectFolder";
    public static final int VERSION = 0;

    private final WeakListenerList<ProjectFolderListener> listeners = new WeakListenerList<>();

    private final AppStorageListener l = eventList -> {
        for (NodeEvent event : eventList.getEvents()) {
            switch (event.getType()) {
                case NodeCreated.TYPE:
                    if (getId().equals(((NodeCreated) event).getParentId())) {
                        listeners.notify(listener -> listener.childAdded(event.getId()));
                    }
                    break;

                case NodeRemoved.TYPE:
                    if (getId().equals(((NodeRemoved) event).getParentId())) {
                        listeners.notify(listener -> listener.childRemoved(event.getId()));
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public ProjectFolder(ProjectFileCreationContext context) {
        super(context, VERSION, true);
        storage.getEventsBus().addListener(l);
    }

    /**
     * Gets the list of children nodes of this project folder.
     */
    @Override
    public List<ProjectNode> getChildren() {
        return storage.getChildNodes(info.getId())
                .stream()
                .map(project::createProjectNode)
                .sorted(Comparator.comparing(ProjectNode::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Gets the child node at path "name/name2/..." relative to this folder, or empty if it does not exist.
     */
    @Override
    public Optional<ProjectNode> getChild(String name, String... more) {
        NodeInfo childInfo = getChildInfo(name, more);
        return Optional.ofNullable(childInfo).map(project::createProjectNode);
    }

    /**
     * Gets the child node of class T at path "name/name2/..." relative to this folder, in a typesafe way, or empty if it does not exist.
     */
    @Override
    public <T extends ProjectNode> Optional<T> getChild(Class<T> clazz, String name, String... more) {
        Objects.requireNonNull(clazz);
        return getChild(name, more)
                .filter(projectNode -> clazz.isAssignableFrom(projectNode.getClass()))
                .map(clazz::cast);
    }

    /**
     * Gets the folder at path "name/name2/..." relative to this folder, or empty if it does not exist.
     */
    @Override
    public Optional<ProjectFolder> getFolder(String name, String... more) {
        return getChild(ProjectFolder.class, name, more);
    }

    /**
     * Creates a subfolder of this folder. If a folder with same name already exists, returns the existing folder.
     */
    @Override
    public ProjectFolder createFolder(String name) {
        NodeInfo folderInfo = storage.getChildNode(info.getId(), name)
                .orElseGet(() -> {
                    NodeInfo newFolderInfo = storage.createNode(ProjectFolder.this.info.getId(), name, PSEUDO_CLASS, "", VERSION, new NodeGenericMetadata());
                    storage.setConsistent(newFolderInfo.getId());
                    storage.flush();
                    return newFolderInfo;
                });
        return new ProjectFolder(new ProjectFileCreationContext(folderInfo, storage, project));
    }

    /**
     * Gets a project file builder for type B, to build a new project file in this folder.
     */
    public <F extends ProjectFile, B extends ProjectFileBuilder<F>> B fileBuilder(Class<B> clazz) {
        Objects.requireNonNull(clazz);
        ProjectFileExtension extension = project.getFileSystem().getData().getProjectFileExtension(clazz);
        return (B) extension.createProjectFileBuilder(new ProjectFileBuildContext(info, storage, project));
    }

    public void addListener(ProjectFolderListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProjectFolderListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListeners() {
        listeners.removeAll();
    }
}
