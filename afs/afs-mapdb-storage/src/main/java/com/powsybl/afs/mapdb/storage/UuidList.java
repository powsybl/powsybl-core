/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UuidList {

    private final List<UUID> uuids;

    public UuidList() {
        this(new ArrayList<>());
    }

    public UuidList(List<UUID> uuids) {
        this.uuids = Objects.requireNonNull(uuids);
    }

    public int size() {
        return uuids.size();
    }

    public UUID get(int i) {
        return uuids.get(i);
    }

    public List<UUID> toList() {
        return uuids;
    }

    public UuidList add(UUID uuid) {
        return new UuidList(ImmutableList.<UUID>builder()
                .addAll(uuids)
                .add(uuid)
                .build());
    }

    public UuidList remove(UUID uuid) {
        List<UUID> newUuids = new ArrayList<>(uuids);
        newUuids.remove(uuid);
        return new UuidList(newUuids);
    }

    @Override
    public int hashCode() {
        return uuids.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UuidList) {
            UuidList other = (UuidList) obj;
            return uuids.equals(other.uuids);
        }
        return false;
    }
}
