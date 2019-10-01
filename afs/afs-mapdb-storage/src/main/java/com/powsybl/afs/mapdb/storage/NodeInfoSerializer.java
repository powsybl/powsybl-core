/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.afs.storage.NodeAccessRights;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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
        out.writeInt(nodeInfo.getAccessRights().getUsersRights().size());
        for (Map.Entry<String, Integer> e : nodeInfo.getAccessRights().getUsersRights().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
        }
        out.writeInt(nodeInfo.getAccessRights().getGroupsRights().size());
        for (Map.Entry<String, Integer> e : nodeInfo.getAccessRights().getGroupsRights().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
        }
        out.writeInt(Objects.isNull(nodeInfo.getAccessRights().getOthersRights()) ? 0 : 1);
        if (!Objects.isNull(nodeInfo.getAccessRights().getOthersRights())) {
            out.writeInt(nodeInfo.getAccessRights().getOthersRights());
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
        NodeAccessRights accessRights = new NodeAccessRights();
        int usersRightsSize = input.readInt();
        for (int i = 0; i < usersRightsSize; i++) {
            accessRights.setUserRights(input.readUTF(), input.readInt());
        }
        int groupsRightsSize = input.readInt();
        for (int i = 0; i < groupsRightsSize; i++) {
            accessRights.setGroupRights(input.readUTF(), input.readInt());
        }
        int othersRightsSize = input.readInt();
        for (int i = 0; i < othersRightsSize; i++) {
            accessRights.setOthersRights(input.readInt());
        }

        return new NodeInfo(nodeId, name, pseudoClass, description, creationTime, modificationTime, version, metadata, accessRights);
    }
}
