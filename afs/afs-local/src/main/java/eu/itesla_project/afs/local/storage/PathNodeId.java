/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import eu.itesla_project.afs.storage.NodeId;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PathNodeId implements NodeId {

    private final Path path;

    public PathNodeId(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    public Path getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathNodeId) {
            return ((PathNodeId) obj).getPath().equals(path);
        }
        return false;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
