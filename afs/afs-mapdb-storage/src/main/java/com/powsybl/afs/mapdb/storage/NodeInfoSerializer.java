/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoSerializer implements Serializer<NodeInfo>, Serializable {

    public static final NodeInfoSerializer INSTANCE = new NodeInfoSerializer();

    @Override
    public void serialize(DataOutput2 out, NodeInfo nodeInfo) throws IOException {
        out.writeInt(MapDbStorageConstants.STORAGE_VERSION);
        UuidSerializer.INSTANCE.serialize(out, MapDbAppStorage.checkNodeId(nodeInfo.getId()));
        out.writeUTF(nodeInfo.getName());
        out.writeUTF(nodeInfo.getPseudoClass());
        out.writeUTF(nodeInfo.getDescription());
        out.writeLong(nodeInfo.getCreationTime());
        out.writeLong(nodeInfo.getModificationTime());
        out.writeInt(nodeInfo.getVersion());
        out.writeInt(nodeInfo.getGenericMetadata().getStrings().size());
        for (Map.Entry<String, String> e : nodeInfo.getGenericMetadata().getStrings().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }
        out.writeInt(nodeInfo.getGenericMetadata().getDoubles().size());
        for (Map.Entry<String, Double> e : nodeInfo.getGenericMetadata().getDoubles().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeDouble(e.getValue());
        }
        out.writeInt(nodeInfo.getGenericMetadata().getInts().size());
        for (Map.Entry<String, Integer> e : nodeInfo.getGenericMetadata().getInts().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
        }
        out.writeInt(nodeInfo.getGenericMetadata().getBooleans().size());
        for (Map.Entry<String, Boolean> e : nodeInfo.getGenericMetadata().getBooleans().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeBoolean(e.getValue());
        }
    }

    @Override
    public NodeInfo deserialize(DataInput2 input, int available) throws IOException {
        input.readInt(); // Storage version is retrieved here
        String nodeId = UuidSerializer.INSTANCE.deserialize(input, available).toString();
        String name = input.readUTF();
        String pseudoClass = input.readUTF();
        String description = input.readUTF();
        long creationTime = input.readLong();
        long modificationTime = input.readLong();
        int version = input.readInt();
        NodeGenericMetadata metadata = new NodeGenericMetadata();
        int stringMetadataSize = input.readInt();
        for (int i = 0; i < stringMetadataSize; i++) {
            metadata.setString(input.readUTF(), input.readUTF());
        }
        int doubleMetadataSize = input.readInt();
        for (int i = 0; i < doubleMetadataSize; i++) {
            metadata.setDouble(input.readUTF(), input.readDouble());
        }
        int intMetadataSize = input.readInt();
        for (int i = 0; i < intMetadataSize; i++) {
            metadata.setInt(input.readUTF(), input.readInt());
        }
        int booleanMetadataSize = input.readInt();
        for (int i = 0; i < booleanMetadataSize; i++) {
            metadata.setBoolean(input.readUTF(), input.readBoolean());
        }
        // Check storage version here to deserialize further version specific data
        return new NodeInfo(nodeId, name, pseudoClass, description, creationTime, modificationTime, version, metadata);
    }
}
