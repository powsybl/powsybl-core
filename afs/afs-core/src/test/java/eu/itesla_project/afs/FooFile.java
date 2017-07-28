/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class FooFile extends ProjectFile {

    FooFile(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem);
    }

    @Override
    public FileIcon getIcon() {
        return new FileIcon("?", new byte[]{});
    }
}
