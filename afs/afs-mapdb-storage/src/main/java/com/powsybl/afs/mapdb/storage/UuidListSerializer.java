/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class UuidListSerializer implements Serializer<List<UUID>>, Serializable {

    public static final UuidListSerializer INSTANCE = new UuidListSerializer();

    private UuidListSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, List<UUID> uuids) throws IOException {
        out.writeInt(uuids.size());
        for (int i = 0; i < uuids.size(); i++) {
            UuidSerializer.INSTANCE.serialize(out, uuids.get(i));
        }
    }

    @Override
    public List<UUID> deserialize(DataInput2 input, int available) throws IOException {
        int size = input.readInt();
        List<UUID> uuids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            uuids.add(UuidSerializer.INSTANCE.deserialize(input, available));
        }
        return uuids;
    }
}

