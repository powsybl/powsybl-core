/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.FileIcon;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ModificationScript extends ProjectFile {

    public static final String PSEUDO_CLASS = "modificationScript";

    static final String SCRIPT_TYPE = "scriptType";
    static final String SCRIPT_CONTENT = "scriptContent";

    public enum ScriptType {
        GROOVY
    }

    private static final FileIcon SCRIPT_ICON = new FileIcon("script", ModificationScript.class.getResourceAsStream("/icons/script16x16.png"));

    public ModificationScript(NodeInfo info, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        super(info, storage, projectInfo, fileSystem, SCRIPT_ICON);
    }

    public ScriptType getScriptType() {
        return ScriptType.valueOf(storage.getStringAttribute(info.getId(), SCRIPT_TYPE));
    }

    public String read() {
        return storage.getStringAttribute(info.getId(), SCRIPT_CONTENT);
    }

    public void write(String content) {
        storage.setStringAttribute(info.getId(), SCRIPT_CONTENT, content);
        storage.flush();
        notifyDependencyChanged();
    }
}
