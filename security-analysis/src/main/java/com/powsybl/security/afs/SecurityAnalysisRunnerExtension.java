/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ProjectFileExtension;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class SecurityAnalysisRunnerExtension implements ProjectFileExtension<SecurityAnalysisRunner, SecurityAnalysisRunnerBuilder> {

    private final SecurityAnalysisParameters parameters;

    public SecurityAnalysisRunnerExtension() {
        this(SecurityAnalysisParameters.load());
    }

    public SecurityAnalysisRunnerExtension(SecurityAnalysisParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }

    @Override
    public Class<SecurityAnalysisRunner> getProjectFileClass() {
        return SecurityAnalysisRunner.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return SecurityAnalysisRunner.PSEUDO_CLASS;
    }

    @Override
    public Class<SecurityAnalysisRunnerBuilder> getProjectFileBuilderClass() {
        return SecurityAnalysisRunnerBuilder.class;
    }

    @Override
    public SecurityAnalysisRunner createProjectFile(ProjectFileCreationContext context) {
        return new SecurityAnalysisRunner(context);
    }

    @Override
    public SecurityAnalysisRunnerBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new SecurityAnalysisRunnerBuilder(context, parameters);
    }
}
