/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.afs.storage.NodeDependency;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeDependencySerializer extends StdSerializer<NodeDependency> {

    public NodeDependencySerializer() {
        super(NodeDependency.class);
    }

    @Override
    public void serialize(NodeDependency dependency, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", dependency.getName());
        jsonGenerator.writeFieldName("nodeInfo");
        new NodeInfoJsonSerializer().serialize(dependency.getNodeInfo(), jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
