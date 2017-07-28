/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class ProjectFile extends ProjectNode {

    protected ProjectFile(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public abstract FileIcon getIcon();

    public List<ProjectNode> getDependencies() {
        return storage.getDependencies(id)
                .stream()
                .map(this::findProjectNode)
                .collect(Collectors.toList());
    }

    public void onDependencyChanged() {
    }
}
