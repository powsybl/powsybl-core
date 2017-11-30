/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScriptBuilder implements ProjectFileBuilder<ModificationScript> {

    private final ProjectFileBuildContext context;

    private String name;

    private ModificationScript.ScriptType type;

    private String content;

    public ModificationScriptBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
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

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), name) != null) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeId id = context.getStorage().createNode(context.getFolderInfo().getId(), name, ModificationScript.PSEUDO_CLASS);

        // set type
        context.getStorage().setStringAttribute(id, ModificationScript.SCRIPT_TYPE, type.name());

        // store script
        context.getStorage().setStringAttribute(id, ModificationScript.SCRIPT_CONTENT, content);

        context.getStorage().flush();

        return new ModificationScript(new ProjectFileCreationContext(new NodeInfo(id, name, ModificationScript.PSEUDO_CLASS),
                                                                     context.getStorage(),
                                                                     context.getFileSystem()));
    }
}
