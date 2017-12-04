/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.FileIcon;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCase extends ProjectFile implements ProjectCase, RunnableScript {

    public static final String PSEUDO_CLASS = "virtualCase";
    public static final int VERSION = 0;

    private static final FileIcon VIRTUAL_CASE_ICON = new FileIcon("virtualCase", VirtualCase.class.getResourceAsStream("/icons/virtualCase16x16.png"));

    static final String CASE_DEPENDENCY_NAME = "case";
    static final String SCRIPT_DEPENDENCY_NAME = "script";

    public VirtualCase(ProjectFileCreationContext context) {
        super(context, VERSION, VIRTUAL_CASE_ICON);
    }

    public ProjectCase getCase() {
        return (ProjectCase) fileSystem.findProjectFile(storage.getDependencyInfo(info.getId(), CASE_DEPENDENCY_NAME));
    }

    public ModificationScript getScript() {
        return (ModificationScript) fileSystem.findProjectFile(storage.getDependencyInfo(info.getId(), SCRIPT_DEPENDENCY_NAME));
    }

    @Override
    public Network getNetwork() {
        return fileSystem.findService(NetworkService.class).getNetwork(this);
    }

    @Override
    public ScriptError getScriptError() {
        return fileSystem.findService(NetworkService.class).getScriptError(this);
    }

    @Override
    public String getScriptOutput() {
        return fileSystem.findService(NetworkService.class).getScriptOutput(this);
    }

    @Override
    public ScriptType getScriptType() {
        return getScript().getScriptType();
    }

    @Override
    public String readScript() {
        return getScript().readScript();
    }

    @Override
    public void writeScript(String content) {
        getScript().writeScript(content);
    }

    @Override
    public void onDependencyChanged() {
        fileSystem.findService(NetworkService.class).invalidateCache(this);
    }
}
