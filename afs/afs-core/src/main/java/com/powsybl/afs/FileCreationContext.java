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
public class FileCreationContext {

    private final NodeInfo info;

    private final ListenableAppStorage storage;

    private final AppFileSystem fileSystem;

    public FileCreationContext(NodeInfo info, ListenableAppStorage storage, AppFileSystem fileSystem) {
        this.info = Objects.requireNonNull(info);
        this.storage = Objects.requireNonNull(storage);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public NodeInfo getInfo() {
        return info;
    }

    public ListenableAppStorage getStorage() {
        return storage;
    }

    public AppFileSystem getFileSystem() {
        return fileSystem;
    }
}
