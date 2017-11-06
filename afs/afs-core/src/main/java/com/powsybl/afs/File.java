/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeInfo;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class File extends Node {

    protected static final String DESCRIPTION = "description";

    protected final FileIcon icon;

    public File(NodeInfo info, AppFileSystemStorage storage, AppFileSystem fileSystem, FileIcon icon) {
        super(info, storage, fileSystem, false);
        this.icon = Objects.requireNonNull(icon);
    }

    public FileIcon getIcon() {
        return icon;
    }

    public String getDescription() {
        return storage.getStringAttribute(info.getId(), DESCRIPTION);
    }
}
