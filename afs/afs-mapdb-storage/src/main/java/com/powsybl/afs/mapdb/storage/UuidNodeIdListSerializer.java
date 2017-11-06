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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class UuidNodeIdListSerializer implements Serializer<UuidNodeIdList>, Serializable {

    public static final UuidNodeIdListSerializer INSTANCE = new UuidNodeIdListSerializer();

    private UuidNodeIdListSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, UuidNodeIdList nodeIdList) throws IOException {
        out.writeInt(nodeIdList.size());
        for (int i = 0; i < nodeIdList.size(); i++) {
            UuidNodeIdSerializer.INSTANCE.serialize(out, nodeIdList.get(i));
        }
    }

    @Override
    public UuidNodeIdList deserialize(DataInput2 input, int available) throws IOException {
        int size = input.readInt();
        List<UuidNodeId> nodeIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            nodeIds.add(UuidNodeIdSerializer.INSTANCE.deserialize(input, available));
        }
        return new UuidNodeIdList(nodeIds);
    }
}

