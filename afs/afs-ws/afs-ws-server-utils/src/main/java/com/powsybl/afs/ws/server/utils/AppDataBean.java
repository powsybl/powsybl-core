/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server.utils;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.AppData;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Named
@Singleton
public class AppDataBean {

    protected AppData appData;

    protected ComputationManager shortTimeExecutionComputationManager;

    protected ComputationManager longTimeExecutionComputationManager;

    public AppData getAppData() {
        return appData;
    }

    public AppStorage getStorage(String fileSystemName) {
        AppStorage storage = appData.getRemotelyAccessibleStorage(fileSystemName);
        if (storage == null) {
            throw new WebApplicationException("App file system '" + fileSystemName + "' not found",
                    Response.Status.NOT_FOUND);
        }
        return storage;
    }

    public AppFileSystem getFileSystem(String name) {
        Objects.requireNonNull(appData);
        Objects.requireNonNull(name);
        AppFileSystem fileSystem = appData.getFileSystem(name);
        if (fileSystem == null) {
            throw new WebApplicationException("App file system '" + name + "' not found", Response.Status.NOT_FOUND);
        }
        return fileSystem;
    }

    public <T extends ProjectFile> T getProjectFile(String fileSystemName, String nodeId, Class<T> clazz) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(clazz);
        AppFileSystem fileSystem = getFileSystem(fileSystemName);
        return fileSystem.findProjectFile(nodeId, clazz);
    }

    public <T extends ProjectFile, U> U getProjectFile(String fileSystemName, String nodeId, Class<T> clazz, Class<U> clazz2) {
        T projectFile = getProjectFile(fileSystemName, nodeId, clazz);
        if (!(clazz2.isAssignableFrom(projectFile.getClass()))) {
            throw new AfsException("Project file '" + nodeId  + "' is not a " + clazz2.getName());
        }
        return (U) projectFile;
    }

    @PostConstruct
    public void init() {
        DefaultComputationManagerConfig config = DefaultComputationManagerConfig.load();
        shortTimeExecutionComputationManager = config.createShortTimeExecutionComputationManager();
        longTimeExecutionComputationManager = config.createLongTimeExecutionComputationManager();
        appData = new AppData(shortTimeExecutionComputationManager, longTimeExecutionComputationManager);
    }

    @PreDestroy
    public void clean() {
        if (appData != null) {
            appData.close();
            shortTimeExecutionComputationManager.close();
            if (longTimeExecutionComputationManager != null) {
                longTimeExecutionComputationManager.close();
            }
        }
    }
}
