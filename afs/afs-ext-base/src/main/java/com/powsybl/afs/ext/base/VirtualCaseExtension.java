/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.auto.service.AutoService;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.ProjectFileExtension;
import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class VirtualCaseExtension implements ProjectFileExtension {
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
    public VirtualCase createProjectFile(NodeInfo info, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        return new VirtualCase(info, storage, projectInfo, fileSystem);
    }

    @Override
    public VirtualCaseBuilder createProjectFileBuilder(NodeInfo folderInfo, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        return new VirtualCaseBuilder(folderInfo, storage, projectInfo, fileSystem);
    }
}
