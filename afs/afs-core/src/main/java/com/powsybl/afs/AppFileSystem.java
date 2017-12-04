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

    private AppData data;

    public AppFileSystem(String name, boolean remotelyAccessible, AppStorage storage) {
        this(name, remotelyAccessible, new DefaultListenableAppStorage(storage));
    }

    public AppFileSystem(String name, boolean remotelyAccessible, ListenableAppStorage storage) {
        this.name = Objects.requireNonNull(name);
        this.remotelyAccessible = remotelyAccessible;
        this.storage = Objects.requireNonNull(storage);
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

    public ProjectNode findProjectNode(NodeInfo nodeInfo) {
        if (ProjectFolder.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new ProjectFolder(new ProjectFileCreationContext(nodeInfo, storage, this));
        } else {
            return findProjectFile(nodeInfo);
        }
    }

    public ProjectFile findProjectFile(NodeInfo nodeInfo) {
        Objects.requireNonNull(data);
        ProjectFileCreationContext context = new ProjectFileCreationContext(nodeInfo, storage, this);
        ProjectFileExtension extension = data.getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
        if (extension != null) {
            return extension.createProjectFile(context);
        } else {
            return new UnknownProjectFile(context);
        }
    }

    public <U> U findService(Class<U> serviceClass) {
        Objects.requireNonNull(data);
        return data.findService(serviceClass, storage.isRemote());
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
    }
}
