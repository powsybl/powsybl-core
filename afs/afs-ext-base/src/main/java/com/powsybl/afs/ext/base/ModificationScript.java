/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.io.CharStreams;
import com.powsybl.afs.FileIcon;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.DefaultAppStorageListener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScript extends ProjectFile implements StorableScript {

    public static final String PSEUDO_CLASS = "modificationScript";
    public static final int VERSION = 0;

    static final String SCRIPT_TYPE = "scriptType";
    static final String SCRIPT_CONTENT = "scriptContent";

    private static final FileIcon SCRIPT_ICON = new FileIcon("script", ModificationScript.class.getResourceAsStream("/icons/script16x16.png"));

    private final List<ScriptListener> listeners = new ArrayList<>();

    public ModificationScript(ProjectFileCreationContext context) {
        super(context, VERSION, SCRIPT_ICON);
        storage.addListener(this, new DefaultAppStorageListener() {
            @Override
            public void nodeDataUpdated(String id, String attributeName) {
                if (id.equals(info.getId()) && SCRIPT_CONTENT.equals(attributeName)) {
                    for (ScriptListener listener : listeners) {
                        listener.scriptUpdated();
                    }
                }
            }
        });
    }

    public ScriptType getScriptType() {
        return ScriptType.valueOf(info.getGenericMetadata().getString(SCRIPT_TYPE));
    }

    @Override
    public String readScript() {
        try {
            return CharStreams.toString(new InputStreamReader(storage.readBinaryData(info.getId(), SCRIPT_CONTENT), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeScript(String content) {
        try (Reader reader = new StringReader(content);
             Writer writer = new OutputStreamWriter(storage.writeBinaryData(info.getId(), SCRIPT_CONTENT), StandardCharsets.UTF_8)) {
            CharStreams.copy(reader, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();
        notifyDependencyListeners();
    }

    @Override
    public void addListener(ScriptListener listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void removeListener(ScriptListener listener) {
        Objects.requireNonNull(listener);
        listeners.remove(listener);
    }
}
