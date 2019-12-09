/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFileBuildContext extends ProjectFileContext {

    private final NodeInfo folderInfo;

    public ProjectFileBuildContext(NodeInfo folderInfo, AppStorage storage, Project project) {
        super(storage, project);
        this.folderInfo = Objects.requireNonNull(folderInfo);
    }

    public NodeInfo getFolderInfo() {
        return folderInfo;
    }
}
