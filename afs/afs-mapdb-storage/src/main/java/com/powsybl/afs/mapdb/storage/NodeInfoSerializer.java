/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoSerializer implements Serializer<NodeInfo>, Serializable {

    public static final NodeInfoSerializer INSTANCE = new NodeInfoSerializer();

    @Override
    public void serialize(DataOutput2 out, NodeInfo nodeInfo) throws IOException {
        UuidNodeIdSerializer.INSTANCE.serialize(out, (UuidNodeId) nodeInfo.getId());
        out.writeUTF(nodeInfo.getName());
        out.writeUTF(nodeInfo.getPseudoClass());
        out.writeUTF(nodeInfo.getDescription());
        out.writeLong(nodeInfo.getCreationTime());
        out.writeLong(nodeInfo.getModificationTime());
        out.writeInt(nodeInfo.getVersion());
    }

    @Override
    public NodeInfo deserialize(DataInput2 input, int available) throws IOException {
        NodeId nodeId = UuidNodeIdSerializer.INSTANCE.deserialize(input, available);
        String name = input.readUTF();
        String pseudoClass = input.readUTF();
        String description = input.readUTF();
        long creationTime = input.readLong();
        long modificationTime = input.readLong();
        int version = input.readInt();
        return new NodeInfo(nodeId, name, pseudoClass, description, creationTime, modificationTime, version);
    }
}
