/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.powsybl.afs.*;
import com.powsybl.afs.ext.base.ProjectCase;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisRunnerBuilder implements ProjectFileBuilder<SecurityAnalysisRunner> {

    private final ProjectFileBuildContext context;

    private final SecurityAnalysisParameters parameters;

    private String name;

    private ProjectFile aCase;

    private ProjectFile contingencyStore;

    public SecurityAnalysisRunnerBuilder(ProjectFileBuildContext context, SecurityAnalysisParameters parameters) {
        this.context = Objects.requireNonNull(context);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public SecurityAnalysisRunnerBuilder withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public SecurityAnalysisRunnerBuilder withCase(ProjectFile aCase) {
        this.aCase = Objects.requireNonNull(aCase);
        return this;
    }

    public SecurityAnalysisRunnerBuilder withContingencyStore(ProjectFile contingencyStore) {
        this.contingencyStore = Objects.requireNonNull(contingencyStore);
        return this;
    }

    @Override
    public SecurityAnalysisRunner build() {
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

        ProjectFolder folder = new ProjectFolder(new ProjectFileCreationContext(context.getFolderInfo(),
                                                                                context.getStorage(),
                                                                                context.getProject()));

        if (folder.getChild(name).isPresent()) {
            throw new AfsException("Folder '" + folder.getPath() + "' already contains a '" + name + "' node");
        }

        // check links belong to the same project
        if (!folder.getProject().getId().equals(aCase.getProject().getId())) {
            throw new AfsException("Case and folder do not belong to the same project");
        }
        if (contingencyStore != null && !folder.getProject().getId().equals(contingencyStore.getProject().getId())) {
            throw new AfsException("Contingency store and folder do not belong to the same project");
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, SecurityAnalysisRunner.PSEUDO_CLASS,
                                                        "", SecurityAnalysisRunner.VERSION, new NodeGenericMetadata());

        // create case link
        context.getStorage().addDependency(info.getId(), SecurityAnalysisRunner.CASE_DEPENDENCY_NAME, aCase.getId());

        // create contingency store link
        if (contingencyStore != null) {
            context.getStorage().addDependency(info.getId(), SecurityAnalysisRunner.CONTINGENCY_STORE_DEPENDENCY_NAME, contingencyStore.getId());
        }

        // write parameters using default one
        SecurityAnalysisRunner.writeParameters(context.getStorage(), info, parameters);

        context.getStorage().flush();

        return new SecurityAnalysisRunner(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()));
    }
}
