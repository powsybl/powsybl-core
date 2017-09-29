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
public class ImportedCaseExtension implements ProjectFileExtension {
    @Override
    public Class<ImportedCase> getProjectFileClass() {
        return ImportedCase.class;
    }

    @Override
    public String getProjectFilePseudoClass() {
        return ImportedCase.PSEUDO_CLASS;
    }

    @Override
    public Class<ImportedCaseBuilder> getProjectFileBuilderClass() {
        return ImportedCaseBuilder.class;
    }

    @Override
    public ImportedCase createProjectFile(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        return new ImportedCase(id, storage, projectId, fileSystem);
    }

    @Override
    public ImportedCaseBuilder createProjectFileBuilder(NodeId folderId, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        return new ImportedCaseBuilder(folderId, storage, projectId, fileSystem);
    }
}
