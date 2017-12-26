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
import java.util.HashMap;
import java.util.Map;

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
        out.writeInt(nodeInfo.getStringMetadata().size());
        for (Map.Entry<String, String> e : nodeInfo.getStringMetadata().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }
        out.writeInt(nodeInfo.getDoubleMetadata().size());
        for (Map.Entry<String, Double> e : nodeInfo.getDoubleMetadata().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeDouble(e.getValue());
        }
        out.writeInt(nodeInfo.getIntMetadata().size());
        for (Map.Entry<String, Integer> e : nodeInfo.getIntMetadata().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
        }
        out.writeInt(nodeInfo.getBooleanMetadata().size());
        for (Map.Entry<String, Boolean> e : nodeInfo.getBooleanMetadata().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeBoolean(e.getValue());
        }
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
        int stringMetadataSize = input.readInt();
        Map<String, String> stringMetadata = new HashMap<>(stringMetadataSize);
        for (int i = 0; i < stringMetadataSize; i++) {
            stringMetadata.put(input.readUTF(), input.readUTF());
        }
        int doubleMetadataSize = input.readInt();
        Map<String, Double> doubleMetadata = new HashMap<>(doubleMetadataSize);
        for (int i = 0; i < doubleMetadataSize; i++) {
            doubleMetadata.put(input.readUTF(), input.readDouble());
        }
        int intMetadataSize = input.readInt();
        Map<String, Integer> intMetadata = new HashMap<>(intMetadataSize);
        for (int i = 0; i < intMetadataSize; i++) {
            intMetadata.put(input.readUTF(), input.readInt());
        }
        int booleanMetadataSize = input.readInt();
        Map<String, Boolean> booleanMetadata = new HashMap<>(booleanMetadataSize);
        for (int i = 0; i < booleanMetadataSize; i++) {
            booleanMetadata.put(input.readUTF(), input.readBoolean());
        }
        return new NodeInfo(nodeId, name, pseudoClass, description, creationTime, modificationTime, version,
                stringMetadata, doubleMetadata, intMetadata, booleanMetadata);
    }
}
