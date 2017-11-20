/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.afs.storage.NodeInfo;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ProjectFileExtension {

    Class<? extends ProjectFile> getProjectFileClass();

    String getProjectFilePseudoClass();

    Class<? extends ProjectFileBuilder<? extends ProjectFile>> getProjectFileBuilderClass();

    <T extends ProjectFile> T createProjectFile(NodeInfo info, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem);

    ProjectFileBuilder<? extends ProjectFile> createProjectFileBuilder(NodeInfo folderInfo, ListenableAppStorage storage, NodeInfo projectInfo, AppFileSystem fileSystem);
}
