/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.afs.storage.NodeId;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UuidNodeId implements NodeId {

    private final UUID uuid;

    public UuidNodeId(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UuidNodeId) {
            return ((UuidNodeId) obj).uuid.equals(uuid);
        }
        return false;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    public static UuidNodeId generate() {
        return new UuidNodeId(UUID.randomUUID());
    }
}
