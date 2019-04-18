/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.io.CharStreams;
import com.powsybl.afs.ProjectFileCreationContext;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScript extends AbstractModificationScript implements StorableScript {

    public static final String PSEUDO_CLASS = "modificationScript";
    public static final int VERSION = 0;

    static final String SCRIPT_TYPE = "scriptType";
    static final String SCRIPT_CONTENT = "scriptContent";

    public ModificationScript(ProjectFileCreationContext context) {
        super(context, VERSION, SCRIPT_CONTENT);
    }

    @Override
    public String getScriptLabel() {
        return null;
    }

    public ScriptType getScriptType() {
        return ScriptType.valueOf(info.getGenericMetadata().getString(SCRIPT_TYPE));
    }

    @Override
    public void writeScript(String content) {
        try (Reader reader = new StringReader(content);
             Writer writer = new OutputStreamWriter(storage.writeBinaryData(info.getId(), scriptContentName), StandardCharsets.UTF_8)) {
            CharStreams.copy(reader, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.updateModificationTime(info.getId());
        storage.flush();

        // invalidate backward dependencies
        invalidate();
    }
}
