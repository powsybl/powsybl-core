/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.*;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCaseBuilder implements ProjectFileBuilder<VirtualCase> {

    private final NodeInfo folderInfo;

    private final ListenableAppStorage storage;

    private final NodeInfo projectInfo;

    private final AppFileSystem fileSystem;

    private String name;

    private String casePath;

    private String scriptPath;

    public VirtualCaseBuilder(NodeInfo folderInfo, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        this.folderInfo = Objects.requireNonNull(folderInfo);
        this.storage = Objects.requireNonNull(storage);
        this.projectInfo = Objects.requireNonNull(projectInfo);
        this.fileSystem = Objects.requireNonNull(fileSystem);

    }

    public VirtualCaseBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VirtualCaseBuilder withCase(String casePath) {
        this.casePath = casePath;
        return this;
    }

    public VirtualCaseBuilder withScript(String scriptPath) {
        this.scriptPath = scriptPath;
        return this;
    }

    @Override
    public VirtualCase build() {
        // check parameters
        if (name == null) {
            throw new AfsException("Name is not set");
        }
        if (casePath == null) {
            throw new AfsException("Case path is not set");
        }
        if (scriptPath == null) {
            throw new AfsException("Script path is not set");
        }

        if (storage.getChildNode(folderInfo.getId(), name) != null) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // check links
        Project project = new Project(projectInfo, storage, fileSystem);
        ProjectFile aCase = (ProjectFile) project.getRootFolder().getChild(casePath);
        if (!(aCase instanceof ProjectCase)) {
            throw new AfsException("Invalid case path " + casePath);
        }
        ModificationScript script = (ModificationScript) project.getRootFolder().getChild(scriptPath);
        if (script == null) {
            throw new AfsException("Invalid script path " + scriptPath);
        }

        // create project file
        NodeId id = storage.createNode(folderInfo.getId(), name, VirtualCase.PSEUDO_CLASS);

        // create case link
        storage.addDependency(id, VirtualCase.CASE_DEPENDENCY_NAME, aCase.getId());

        // create script link
        storage.addDependency(id, VirtualCase.SCRIPT_DEPENDENCY_NAME, script.getId());

        storage.flush();

        return new VirtualCase(new NodeInfo(id, name, VirtualCase.PSEUDO_CLASS), storage, projectInfo, fileSystem);
    }
}
