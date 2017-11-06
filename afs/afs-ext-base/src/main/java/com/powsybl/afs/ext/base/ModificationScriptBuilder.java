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
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScriptBuilder implements ProjectFileBuilder<ModificationScript> {

    private final NodeInfo folderInfo;

    private final AppFileSystemStorage storage;

    private final NodeInfo projectInfo;

    private final AppFileSystem fileSystem;

    private String name;

    private ModificationScript.ScriptType type;

    private String content;

    public ModificationScriptBuilder(NodeInfo folderInfo, AppFileSystemStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        this.folderInfo = Objects.requireNonNull(folderInfo);
        this.storage = Objects.requireNonNull(storage);
        this.projectInfo = Objects.requireNonNull(projectInfo);
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

        if (storage.getChildNode(folderInfo.getId(), name) != null) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeId id = storage.createNode(folderInfo.getId(), name, ModificationScript.PSEUDO_CLASS);

        // set type
        storage.setStringAttribute(id, ModificationScript.SCRIPT_TYPE, type.name());

        // store script
        storage.setStringAttribute(id, ModificationScript.SCRIPT_CONTENT, content);

        storage.flush();

        return new ModificationScript(new NodeInfo(id, name, ModificationScript.PSEUDO_CLASS), storage, projectInfo, fileSystem);
    }
}
