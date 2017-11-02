/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class FooFileBuilder implements ProjectFileBuilder<FooFile> {

    private final NodeId folderId;

    private final AppFileSystemStorage storage;

    private final NodeInfo projectInfo;

    private final AppFileSystem fileSystem;

    private String name;

    FooFileBuilder(NodeId folderId, AppFileSystemStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        this.folderId = Objects.requireNonNull(folderId);
        this.storage = Objects.requireNonNull(storage);
        this.projectInfo = Objects.requireNonNull(projectInfo);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public FooFileBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public FooFile build() {
        if (name == null) {
            throw new IllegalStateException("name is not set");
        }
        String pseudoClass = "foo";
        NodeId id = storage.createNode(folderId, name, pseudoClass);
        return new FooFile(new NodeInfo(id, name, pseudoClass), storage, projectInfo, fileSystem);
    }
}
