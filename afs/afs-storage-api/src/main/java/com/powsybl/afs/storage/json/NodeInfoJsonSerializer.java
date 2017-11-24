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
import java.io.UncheckedIOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoJsonSerializer extends StdSerializer<NodeInfo> {

    public NodeInfoJsonSerializer() {
        super(NodeInfo.class);
    }

    @Override
    public void serialize(NodeInfo nodeInfo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("id", nodeInfo.getId().toString());
            jsonGenerator.writeStringField("name", nodeInfo.getName());
            jsonGenerator.writeStringField("pseudoClass", nodeInfo.getPseudoClass());
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
