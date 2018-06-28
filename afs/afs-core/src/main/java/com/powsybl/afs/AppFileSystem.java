/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.DefaultListenableAppStorage;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 *
 * An AppFileSystem instance is a tree of {@link Node} objects, starting with its root folder.
 * <p>
 * {@link Node} objects may be {@link Folder}s or {@link File}, or any new file type added
 * by the user through the extension mechanism (see {@link FileExtension}).
 * An application may have several instances of {@link AppFileSystem}, each one with a unique name.
 * They are accessed through the parent {@link AppData} instance.
 *
 * <p>
 * The AppFileSystem is backed by an {@link AppStorage} implementation, which is in charge of maintaining
 * the state of data of the AppFileSystem. The implementation may bring additional functionalities:
 * in-memory storage, database storage, remote storage, ...
 *
 * <p>
 * Users of an AppFileSystem should not need to interact directly with the underlying {@link AppStorage}.
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

    /**
     * Creates a new Node in this file system. This is a low level method, and should seldom be used by the AFS API users.
     */
    public Node createNode(NodeInfo nodeInfo) {
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

    /**
     * Get a project file by its ID.
     */
    public <T extends ProjectFile> T findProjectFile(String projectFileId, Class<T> clazz) {
        Objects.requireNonNull(projectFileId);
        Objects.requireNonNull(clazz);

        // get file info
        NodeInfo projectFileInfo = storage.getNodeInfo(projectFileId);

        // walk the node hierarchy until finding a project
        NodeInfo parentInfo = storage.getParentNode(projectFileInfo.getId()).orElse(null);
        while (parentInfo != null && !Project.PSEUDO_CLASS.equals(parentInfo.getPseudoClass())) {
            parentInfo = storage.getParentNode(parentInfo.getId()).orElse(null);
        }
        if (parentInfo == null) {
            throw new AfsException("Node '" + projectFileId + "' is probably not a project file, parent project has not been found");
        }

        // create the project
        Project project = new Project(new FileCreationContext(parentInfo, storage, this));

        // then create the file
        ProjectFile projectFile = project.createProjectFile(projectFileInfo);

        // check file is the right type
        if (!clazz.isAssignableFrom(projectFile.getClass())) {
            throw new AfsException("Project file '" + projectFileId + "' is not a " + clazz.getName()
                    + " instance (" + projectFile.getClass() + ")");
        }

        return (T) projectFile;
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

    public boolean isClosed() {
        return storage.isClosed();
    }

    @Override
    public void close() {
        storage.close();
        taskMonitor.close();
    }
}
