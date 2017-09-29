/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScriptBuilder implements ProjectFileBuilder<ModificationScript> {

    private final NodeId folderId;

    private final AppFileSystemStorage storage;

    private final NodeId projectId;

    private final AppFileSystem fileSystem;

    private String name;

    private ModificationScript.ScriptType type;

    private String content;

    public ModificationScriptBuilder(NodeId folderId, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        this.folderId = Objects.requireNonNull(folderId);
        this.storage = Objects.requireNonNull(storage);
        this.projectId = Objects.requireNonNull(projectId);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public ModificationScriptBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ModificationScriptBuilder withType(ModificationScript.ScriptType type) {
        this.type = type;
        return this;
    }

    public ModificationScriptBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public ModificationScript build() {
        // check parameters
        if (name == null) {
            throw new AfsException("Name is not set");
        }
        if (type == null) {
            throw new AfsException("Script type is not set");
        }
        if (content == null) {
            throw new AfsException("Content is not set");
        }

        // create project file
        NodeId id = storage.createNode(folderId, name, ModificationScript.PSEUDO_CLASS);

        // set type
        storage.setStringAttribute(id, ModificationScript.SCRIPT_TYPE, type.name());

        // store script
        storage.setStringAttribute(id, ModificationScript.SCRIPT_CONTENT, content);

        storage.flush();

        return new ModificationScript(id, storage, projectId, fileSystem);
    }
}
