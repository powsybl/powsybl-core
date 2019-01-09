/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.afs;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ProjectFileBuildContext;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ProjectFileExtension;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class ContingencyStoreExtension implements ProjectFileExtension<ContingencyStore, ContingencyStoreBuilder> {

    @Override
    public Class<ContingencyStore> getProjectFileClass() {
        return ContingencyStore.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return ContingencyStore.PSEUDO_CLASS;
    }

    @Override
    public Class<ContingencyStoreBuilder> getProjectFileBuilderClass() {
        return ContingencyStoreBuilder.class;
    }

    @Override
    public ContingencyStore createProjectFile(ProjectFileCreationContext context) {
        return new ContingencyStore(context);
    }

    @Override
    public ContingencyStoreBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new ContingencyStoreBuilder(context);
    }
}
