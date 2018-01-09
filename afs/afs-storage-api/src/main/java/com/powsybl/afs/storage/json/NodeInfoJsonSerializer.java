/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.afs.storage.NodeInfo;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoJsonSerializer extends StdSerializer<NodeInfo> {

    static final String NAME = "name";
    static final String ID = "id";
    static final String DESCRIPTION = "description";
    static final String PSEUDO_CLASS = "pseudoClass";
    static final String CREATION_TIME = "creationTime";
    static final String MODIFICATION_TIME = "modificationTime";
    static final String VERSION = "version";
    static final String METADATA = "metadata";

    public NodeInfoJsonSerializer() {
        super(NodeInfo.class);
    }

    @Override
    public void serialize(NodeInfo nodeInfo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(ID, nodeInfo.getId());
        jsonGenerator.writeStringField(NAME, nodeInfo.getName());
        jsonGenerator.writeStringField(PSEUDO_CLASS, nodeInfo.getPseudoClass());
        jsonGenerator.writeStringField(DESCRIPTION, nodeInfo.getDescription());
        jsonGenerator.writeNumberField(CREATION_TIME, nodeInfo.getCreationTime());
        jsonGenerator.writeNumberField(MODIFICATION_TIME, nodeInfo.getModificationTime());
        jsonGenerator.writeNumberField(VERSION, nodeInfo.getVersion());
        jsonGenerator.writeFieldName(METADATA);
        new NodeGenericMetadataJsonSerializer().serialize(nodeInfo.getGenericMetadata(), jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
