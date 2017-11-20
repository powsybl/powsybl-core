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
public class UnknownFile extends File {

    static final FileIcon GHOST_ICON = new FileIcon("ghost", UnknownFile.class.getResourceAsStream("/icons/ghost16x16.png"));

    public UnknownFile(NodeInfo info, ListenableAppStorage storage, AppFileSystem fileSystem) {
        super(info, storage, fileSystem, GHOST_ICON);
    }
}
