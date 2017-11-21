/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFileBuildContext {

    private final NodeInfo folderInfo;

    private final ListenableAppStorage storage;

    private final NodeInfo projectInfo;

    private final AppFileSystem fileSystem;

    public ProjectFileBuildContext(NodeInfo folderInfo, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        this.folderInfo = Objects.requireNonNull(folderInfo);
        this.storage = Objects.requireNonNull(storage);
        this.projectInfo = Objects.requireNonNull(projectInfo);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public NodeInfo getFolderInfo() {
        return folderInfo;
    }

    public ListenableAppStorage getStorage() {
        return storage;
    }

    public NodeInfo getProjectInfo() {
        return projectInfo;
    }

    public AppFileSystem getFileSystem() {
        return fileSystem;
    }
}
