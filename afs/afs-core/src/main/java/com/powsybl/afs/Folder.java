/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
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

    @Override
    public List<Node> getChildren() {
        return storage.getChildNodes(info.getId())
                .stream()
                .map(fileSystem::createNode)
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Node> getChild(String name, String... more) {
        NodeInfo childInfo = getChildInfo(name, more);
        return Optional.ofNullable(childInfo).map(fileSystem::createNode);
    }

    @Override
    public <T extends Node> Optional<T> getChild(Class<T> clazz, String name, String... more) {
        Objects.requireNonNull(clazz);
        return getChild(name, more)
                .filter(node -> clazz.isAssignableFrom(node.getClass()))
                .map(clazz::cast);
    }

    @Override
    public Optional<Folder> getFolder(String name, String... more) {
        return getChild(Folder.class, name, more);
    }

    @Override
    public Folder createFolder(String name) {
        NodeInfo folderInfo = storage.getChildNode(info.getId(), name)
                .orElseGet(() -> {
                    NodeInfo newFolderInfo = storage.createNode(info.getId(), name, PSEUDO_CLASS, "", VERSION, new NodeGenericMetadata());
                    storage.flush();
                    return newFolderInfo;
                });
        return new Folder(new FileCreationContext(folderInfo, storage, fileSystem));
    }

    public Project createProject(String name) {
        NodeInfo projectInfo = storage.getChildNode(info.getId(), name)
                .orElseGet(() -> {
                    NodeInfo newProjectInfo = storage.createNode(info.getId(), name, Project.PSEUDO_CLASS, "", Project.VERSION, new NodeGenericMetadata());
                    // create root project folder
                    storage.createNode(newProjectInfo.getId(), Project.ROOT_FOLDER_NAME, ProjectFolder.PSEUDO_CLASS, "", ProjectFolder.VERSION, new NodeGenericMetadata());
                    storage.flush();
                    return newProjectInfo;
                });
        return new Project(new FileCreationContext(projectInfo, storage, fileSystem));
    }
}
