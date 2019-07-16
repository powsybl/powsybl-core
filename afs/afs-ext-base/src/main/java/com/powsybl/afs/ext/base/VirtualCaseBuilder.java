/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.*;
import com.powsybl.afs.ext.base.events.VirtualCaseCreated;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCaseBuilder implements ProjectFileBuilder<VirtualCase> {

    private static final String  VIRTUAL_CASE_CREATED = "VIRTUAL_CASE_CREATED";

    private final ProjectFileBuildContext context;

    private String name;

    private ProjectFile aCase;

    private ModificationScript script;

    public VirtualCaseBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public VirtualCaseBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VirtualCaseBuilder withCase(ProjectFile aCase) {
        this.aCase = Objects.requireNonNull(aCase);
        return this;
    }

    public VirtualCaseBuilder withScript(ModificationScript script) {
        this.script = Objects.requireNonNull(script);
        return this;
    }

    @Override
    public VirtualCase build() {
        // check parameters
        if (name == null) {
            throw new AfsException("Name is not set");
        }
        if (aCase == null) {
            throw new AfsException("Case is not set");
        } else {
            if (!(aCase instanceof ProjectCase)) {
                throw new AfsException("Case does not implement " + ProjectCase.class.getName());
            }
        }
        if (script == null) {
            throw new AfsException("Script is not set");
        }

        ProjectFolder folder = new ProjectFolder(new ProjectFileCreationContext(context.getFolderInfo(),
                                                                                context.getStorage(),
                                                                                context.getProject()));

        if (folder.getChild(name).isPresent()) {
            throw new AfsException("Folder '" + folder.getPath() + "' already contains a '" + name + "' node");
        }

        // check links belong to same project
        if (!folder.getProject().getId().equals(aCase.getProject().getId())) {
            throw new AfsException("Case and folder do not belong to the same project");
        }
        if (!folder.getProject().getId().equals(script.getProject().getId())) {
            throw new AfsException("Script and folder do not belong to the same project");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, VirtualCase.PSEUDO_CLASS, "", VirtualCase.VERSION, new NodeGenericMetadata());

        // create case link
        context.getStorage().addDependency(info.getId(), VirtualCase.CASE_DEPENDENCY_NAME, aCase.getId());

        // create script link
        context.getStorage().addDependency(info.getId(), VirtualCase.SCRIPT_DEPENDENCY_NAME, script.getId());

        context.getStorage().setConsistent(info.getId());

        context.getStorage().flush();

        VirtualCase virtualCase = new VirtualCase(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()));

        context.getStorage().getEventsBus().pushEvent(new VirtualCaseCreated(info.getId(),
                context.getFolderInfo().getId(), virtualCase.getPath().toString()), VIRTUAL_CASE_CREATED);

        return virtualCase;
    }
}
