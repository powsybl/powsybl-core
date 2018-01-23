/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NamedLink {

    private final UUID nodeUuid;

    private final String name;

    public NamedLink(UUID nodeUuid, String name) {
        this.nodeUuid = Objects.requireNonNull(nodeUuid);
        this.name = Objects.requireNonNull(name);
    }

    public UUID getNodeUuid() {
        return nodeUuid;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return nodeUuid.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NamedLink) {
            NamedLink other = (NamedLink) obj;
            return nodeUuid.equals(other.nodeUuid) && name.equals(other.name);
        }
        return false;
    }
}
