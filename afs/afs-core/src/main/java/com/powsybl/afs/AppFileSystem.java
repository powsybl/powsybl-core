/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.*;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppFileSystem implements AutoCloseable {

    public static final String FS_SEPARATOR = ":";
    public static final String PATH_SEPARATOR = "/";

    private final String name;

    private final boolean remotelyAccessible;

    private final ListenableAppStorage storage;

    private final NodeInfo rootNodeInfo;

    private final TaskMonitor taskMonitor;

    private AppData data;

    public AppFileSystem(String name, boolean remotelyAccessible, AppStorage storage) {
        this(name, remotelyAccessible, new DefaultListenableAppStorage(storage), new LocalTaskMonitor());
    }

    public AppFileSystem(String name, boolean remotelyAccessible, ListenableAppStorage storage, TaskMonitor taskMonitor) {
        this.name = Objects.requireNonNull(name);
        this.remotelyAccessible = remotelyAccessible;
        this.storage = Objects.requireNonNull(storage);
        this.taskMonitor = Objects.requireNonNull(taskMonitor);
        rootNodeInfo = storage.createRootNodeIfNotExists(name, Folder.PSEUDO_CLASS);
    }

    public String getName() {
        return name;
    }

    public boolean isRemotelyAccessible() {
        return remotelyAccessible;
    }

    ListenableAppStorage getStorage() {
        return storage;
    }

    public Folder getRootFolder() {
        return new Folder(new FileCreationContext(rootNodeInfo, storage, this));
    }

    public Node findNode(NodeInfo nodeInfo) {
        Objects.requireNonNull(nodeInfo);
        Objects.requireNonNull(data);
        FileCreationContext context = new FileCreationContext(nodeInfo, storage, this);
        if (Folder.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new Folder(context);
        } else if (Project.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new Project(context);
        } else {
            FileExtension extension = data.getFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
            if (extension != null) {
                return extension.createFile(context);
            } else {
                return new UnknownFile(context);
            }
        }
    }

    public <T extends ProjectFile> T findProjectFile(String nodeId, Class<T> clazz) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(clazz);
        NodeInfo nodeInfo = storage.getNodeInfo(nodeId);
        ProjectFile projectFile = createProjectFile(nodeInfo);
        return clazz.isAssignableFrom(projectFile.getClass()) ? (T) projectFile : null;
    }

    ProjectNode createProjectNode(NodeInfo nodeInfo) {
        if (ProjectFolder.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new ProjectFolder(new ProjectFileCreationContext(nodeInfo, storage, this));
        } else {
            return createProjectFile(nodeInfo);
        }
    }

    ProjectFile createProjectFile(NodeInfo nodeInfo) {
        Objects.requireNonNull(data);
        ProjectFileCreationContext context = new ProjectFileCreationContext(nodeInfo, storage, this);
        ProjectFileExtension extension = data.getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
        if (extension != null) {
            return extension.createProjectFile(context);
        } else {
            return new UnknownProjectFile(context);
        }
    }

    public TaskMonitor getTaskMonitor() {
        return taskMonitor;
    }

    public AppData getData() {
        return data;
    }

    void setData(AppData data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public void close() {
        storage.close();
        taskMonitor.close();
    }
}
