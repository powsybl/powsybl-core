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
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UuidNodeIdSerializer implements Serializer<UuidNodeId>, Serializable {

    public static final UuidNodeIdSerializer INSTANCE = new UuidNodeIdSerializer();

    @Override
    public void serialize(DataOutput2 out, UuidNodeId nodeId) throws IOException {
        out.writeLong(nodeId.getUuid().getLeastSignificantBits());
        out.writeLong(nodeId.getUuid().getMostSignificantBits());
    }

    @Override
    public UuidNodeId deserialize(DataInput2 input, int available) throws IOException {
        long leastSignificantBits = input.readLong();
        long mostSignificantBits = input.readLong();
        return new UuidNodeId(new UUID(mostSignificantBits, leastSignificantBits));
    }
}
