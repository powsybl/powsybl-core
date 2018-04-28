/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Project extends File {

    public static final String PSEUDO_CLASS = "project";
    public static final int VERSION = 0;

    public static final String ROOT_FOLDER_NAME = "root";

    public Project(FileCreationContext context) {
        super(context, VERSION);
    }

    public ProjectFolder getRootFolder() {
        NodeInfo rootFolderInfo = storage.getChildNode(info.getId(), ROOT_FOLDER_NAME).orElseThrow(AssertionError::new);
        return new ProjectFolder(new ProjectFileCreationContext(rootFolderInfo, storage, this));
    }

    ProjectNode createProjectNode(NodeInfo nodeInfo) {
        Objects.requireNonNull(nodeInfo);
        if (ProjectFolder.PSEUDO_CLASS.equals(nodeInfo.getPseudoClass())) {
            return new ProjectFolder(new ProjectFileCreationContext(nodeInfo, storage, this));
        } else {
            return createProjectFile(nodeInfo);
        }
    }

    ProjectFile createProjectFile(NodeInfo nodeInfo) {
        Objects.requireNonNull(nodeInfo);
        ProjectFileCreationContext context = new ProjectFileCreationContext(nodeInfo, storage, this);
        ProjectFileExtension extension = fileSystem.getData().getProjectFileExtensionByPseudoClass(nodeInfo.getPseudoClass());
        if (extension != null) {
            return extension.createProjectFile(context);
        } else {
            return new UnknownProjectFile(context);
        }
    }
}
