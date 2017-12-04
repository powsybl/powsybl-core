/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ProjectFileExtension;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class VirtualCaseExtension implements ProjectFileExtension<VirtualCase, VirtualCaseBuilder> {
    @Override
    public Class<VirtualCase> getProjectFileClass() {
        return VirtualCase.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return VirtualCase.PSEUDO_CLASS;
    }

    @Override
    public Class<VirtualCaseBuilder> getProjectFileBuilderClass() {
        return VirtualCaseBuilder.class;
    }

    @Override
    public VirtualCase createProjectFile(ProjectFileCreationContext context) {
        return new VirtualCase(context);
    }

    @Override
    public VirtualCaseBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new VirtualCaseBuilder(context);
    }
}
