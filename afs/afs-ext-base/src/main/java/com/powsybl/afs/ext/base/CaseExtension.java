/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.auto.service.AutoService;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.FileExtension;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;

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
