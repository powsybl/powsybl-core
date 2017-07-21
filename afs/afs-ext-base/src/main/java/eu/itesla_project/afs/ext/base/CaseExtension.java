/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import com.google.auto.service.AutoService;
import eu.itesla_project.afs.core.AppFileSystem;
import eu.itesla_project.afs.core.FileExtension;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(FileExtension.class)
public class CaseExtension implements FileExtension {
    @Override
    public Class<Case> getFileClass() {
        return Case.class;
    }

    @Override
    public String getFilePseudoClass() {
        return Case.PSEUDO_CLASS;
    }

    @Override
    public Case createFile(NodeId id, AppFileSystemStorage storage, AppFileSystem fileSystem) {
        return new Case(id, storage, fileSystem);
    }
}
