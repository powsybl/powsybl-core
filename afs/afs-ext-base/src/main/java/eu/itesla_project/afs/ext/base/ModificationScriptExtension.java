/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import com.google.auto.service.AutoService;
import eu.itesla_project.afs.AppFileSystem;
import eu.itesla_project.afs.ProjectFileExtension;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;

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
    public ModificationScript createProjectFile(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        return new ModificationScript(id, storage, projectId, fileSystem);
    }

    @Override
    public ModificationScriptBuilder createProjectFileBuilder(NodeId folderId, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        return new ModificationScriptBuilder(folderId, storage, projectId, fileSystem);
    }
}
