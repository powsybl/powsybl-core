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
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisRunnerBuilder implements ProjectFileBuilder<SecurityAnalysisRunner> {

    private final ProjectFileBuildContext context;

    private final SecurityAnalysisParameters parameters;

    private String name;

    private String casePath;

    private String contingencyStorePath;

    public SecurityAnalysisRunnerBuilder(ProjectFileBuildContext context, SecurityAnalysisParameters parameters) {
        this.context = Objects.requireNonNull(context);
        this.parameters = Objects.requireNonNull(parameters);
    }

    public SecurityAnalysisRunnerBuilder withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public SecurityAnalysisRunnerBuilder withCase(String casePath) {
        this.casePath = Objects.requireNonNull(casePath);
        return this;
    }

    public SecurityAnalysisRunnerBuilder withContingencyStore(String contingencyStorePath) {
        this.contingencyStorePath = Objects.requireNonNull(contingencyStorePath);
        return this;
    }

    @Override
    public SecurityAnalysisRunner build() {
        if (name == null) {
            throw new AfsException("Name is not set");
        }
        if (casePath == null) {
            throw new AfsException("Case path is not set");
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
        Optional<ProjectFile> contingencyStore;
        if (contingencyStorePath != null) {
            contingencyStore = project.getRootFolder().getChild(ProjectFile.class, contingencyStorePath);
            if (!contingencyStore.isPresent() || !(contingencyStore.get() instanceof ContingenciesProvider)) {
                throw new AfsException("Invalid contingency store path " + contingencyStorePath);
            }
        } else {
            contingencyStore = Optional.empty();
        }

        // create project file
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, SecurityAnalysisRunner.PSEUDO_CLASS,
                                                        "", SecurityAnalysisRunner.VERSION, new NodeGenericMetadata());

        // create case link
        context.getStorage().addDependency(info.getId(), SecurityAnalysisRunner.CASE_DEPENDENCY_NAME, aCase.get().getId());

        // create contingency store link
        contingencyStore.ifPresent(projectFile -> context.getStorage().addDependency(info.getId(), SecurityAnalysisRunner.CONTINGENCY_PROVIDER_DEPENDENCY_NAME, projectFile.getId()));

        // write parameters using default one
        SecurityAnalysisRunner.writeParameters(context.getStorage(), info, parameters);

        context.getStorage().flush();

        return new SecurityAnalysisRunner(new ProjectFileCreationContext(info, context.getStorage(), context.getFileSystem()));
    }
}
