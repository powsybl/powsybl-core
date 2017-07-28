/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import eu.itesla_project.afs.AppFileSystem;
import eu.itesla_project.afs.ProjectFile;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class ProjectCase extends ProjectFile {

    protected ProjectCase(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem);
    }

    public abstract Network loadNetwork();
}
