/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.*;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCaseBuilder implements ProjectFileBuilder<VirtualCase> {

    private final ProjectFileBuildContext context;

    private String name;

    private String casePath;

    private String scriptPath;

    public VirtualCaseBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
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

        if (context.getStorage().getChildNode(context.getFolderInfo().getId(), name).isPresent()) {
            throw new AfsException("Parent folder already contains a '" + name + "' node");
        }

        // check links
        Project project = new ProjectFolder(new ProjectFileCreationContext(context.getFolderInfo(),
                                                                           context.getStorage(),
                                                                           context.getFileSystem())).getProject();
        Optional<ProjectFile> aCase = project.getRootFolder().getChild(ProjectFile.class, casePath);
        if (!aCase.isPresent() || !(aCase.get() instanceof ProjectCase)) {
            throw new AfsException("Invalid case path " + casePath);
        }
        Optional<ModificationScript> script = project.getRootFolder().getChild(ModificationScript.class, scriptPath);
        if (!script.isPresent()) {
            throw new AfsException("Invalid script path " + scriptPath);
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, VirtualCase.PSEUDO_CLASS, "", VirtualCase.VERSION, new NodeGenericMetadata());

        // create case link
        context.getStorage().addDependency(info.getId(), VirtualCase.CASE_DEPENDENCY_NAME, aCase.get().getId());

        // create script link
        context.getStorage().addDependency(info.getId(), VirtualCase.SCRIPT_DEPENDENCY_NAME, script.get().getId());

        context.getStorage().flush();

        return new VirtualCase(new ProjectFileCreationContext(info, context.getStorage(), context.getFileSystem()));
    }
}
