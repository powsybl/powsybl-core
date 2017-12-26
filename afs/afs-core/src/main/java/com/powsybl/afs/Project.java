/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeInfo;

import java.util.ResourceBundle;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Project extends File {

    public static final String PSEUDO_CLASS = "project";
    public static final int VERSION = 0;

    public static final String ROOT_FOLDER_NAME = "root";

    private static final String PROJECT_LABEL = ResourceBundle.getBundle("lang.Project").getString("Project");

    private static final FileIcon PROJECT_ICON = new FileIcon(PROJECT_LABEL, Project.class.getResourceAsStream("/icons/project16x16.png"));

    public Project(FileCreationContext context) {
        super(context, VERSION, PROJECT_ICON);
    }

    public ProjectFolder getRootFolder() {
        NodeInfo rootFolderInfo = storage.getChildNode(info.getId(), ROOT_FOLDER_NAME);
        return new ProjectFolder(new ProjectFileCreationContext(rootFolderInfo, storage, fileSystem));
    }
}
