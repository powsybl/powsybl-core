/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.afs;

import com.google.common.io.CharStreams;
import com.powsybl.afs.AfsException;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileBuilder;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActionScriptBuilder implements ProjectFileBuilder<ActionScript> {

    private final ProjectFileBuildContext context;

    private String name;

    private String content;

    public ActionScriptBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public ActionScriptBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ActionScriptBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public ActionScript build() {
        // check parameters
        if (name == null) {
            throw new AfsException("Name is not set");
        }
        if (content == null) {
            throw new AfsException("Content is not set");
        }

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), name).isPresent()) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, ActionScript.PSEUDO_CLASS, "", ActionScript.VERSION,
                new NodeGenericMetadata());

        // store script
        try (Reader reader = new StringReader(content);
             Writer writer = new OutputStreamWriter(context.getStorage().writeBinaryData(info.getId(), ActionScript.SCRIPT_CONTENT), StandardCharsets.UTF_8)) {
            CharStreams.copy(reader, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        context.getStorage().flush();

        return new ActionScript(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()));
    }
}
