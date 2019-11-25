/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppStorageArchive;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A folder in an {@link AppFileSystem} tree.
 *
 * <p>
 * Folders may have children folders or files, and provides methods to create new children.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Folder extends Node implements FolderBase<Node, Folder> {

    public static final String PSEUDO_CLASS = "folder";
    public static final int VERSION = 0;

    public Folder(FileCreationContext context) {
        super(context, VERSION, true);
    }

    public boolean isWritable() {
        return storage.isWritable(info.getId());
    }

    /**
     * Get the children nodes of this folder.
     */
    @Override
    public List<Node> getChildren() {
        return storage.getChildNodes(info.getId())
                .stream()
                .map(fileSystem::createNode)
                .sorted(Comparator.comparing(Node::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Gets the child node at path "name/name2/..." relative to this folder, or empty if it does not exist.
     */
    @Override
    public Optional<Node> getChild(String name, String... more) {
        NodeInfo childInfo = getChildInfo(name, more);
        return Optional.ofNullable(childInfo).map(fileSystem::createNode);
    }

    /**
     * Gets the child node of class T at path "name/name2/..." relative to this folder, in a typesafe way, or empty if it does not exist.
     */
    @Override
    public <T extends Node> Optional<T> getChild(Class<T> clazz, String name, String... more) {
        Objects.requireNonNull(clazz);
        return getChild(name, more)
                .filter(node -> clazz.isAssignableFrom(node.getClass()))
                .map(clazz::cast);
    }

    /**
     * Gets the folder at path "name/name2/..." relative to this folder, or empty if it does not exist.
     */
    @Override
    public Optional<Folder> getFolder(String name, String... more) {
        return getChild(Folder.class, name, more);
    }

    /**
     * Creates a subfolder of this folder. If a folder with same name already exists, returns the existing folder.
     */
    @Override
    public Folder createFolder(String name) {
        NodeInfo folderInfo = storage.getChildNode(info.getId(), name)
                .orElseGet(() -> {
                    NodeInfo newFolderInfo = storage.createNode(info.getId(), name, PSEUDO_CLASS, "", VERSION, new NodeGenericMetadata());
                    storage.setConsistent(newFolderInfo.getId());
                    storage.flush();
                    return newFolderInfo;
                });
        return new Folder(new FileCreationContext(folderInfo, storage, fileSystem));

    }

    /**
     * Creates a new {@link Project} in this folder. If a project with same name already exists, returns the existing project.
     */
    public Project createProject(String name) {
        NodeInfo projectInfo = storage.getChildNode(info.getId(), name)
                .orElseGet(() -> {
                    NodeInfo newProjectInfo = storage.createNode(info.getId(), name, Project.PSEUDO_CLASS, "", Project.VERSION, new NodeGenericMetadata());
                    storage.setConsistent(newProjectInfo.getId());
                    // create root project folder
                    NodeInfo newProjectInfoRootFolder = storage.createNode(newProjectInfo.getId(), Project.ROOT_FOLDER_NAME, ProjectFolder.PSEUDO_CLASS, "", ProjectFolder.VERSION, new NodeGenericMetadata());
                    storage.setConsistent(newProjectInfoRootFolder.getId());
                    storage.flush();
                    return newProjectInfo;
                });
        return new Project(new FileCreationContext(projectInfo, storage, fileSystem));
    }

    public void archiveChildren(Path dir) {
        Objects.requireNonNull(dir);
        try {
            new AppStorageArchive(storage).archiveChildren(info, dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete() {
        if (getChildren().isEmpty()) {
            super.delete();
        } else {
            throw new AfsException("non-empty folders can not be deleted");
        }
    }
}
