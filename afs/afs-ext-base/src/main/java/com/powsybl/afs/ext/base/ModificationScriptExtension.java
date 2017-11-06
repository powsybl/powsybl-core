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
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeInfo;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ProjectFileExtension.class)
public class ModificationScriptExtension implements ProjectFileExtension {
    @Override
    public Class<ModificationScript> getProjectFileClass() {
        return ModificationScript.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return ModificationScript.PSEUDO_CLASS;
    }

    @Override
    public Class<ModificationScriptBuilder> getProjectFileBuilderClass() {
        return ModificationScriptBuilder.class;
    }

    @Override
    public ModificationScript createProjectFile(NodeInfo info, AppFileSystemStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        return new ModificationScript(info, storage, projectInfo, fileSystem);
    }

    @Override
    public ModificationScriptBuilder createProjectFileBuilder(NodeInfo folderInfo, AppFileSystemStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem) {
        return new ModificationScriptBuilder(folderInfo, storage, projectInfo, fileSystem);
    }
}
