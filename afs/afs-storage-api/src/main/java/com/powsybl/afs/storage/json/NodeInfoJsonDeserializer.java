/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoJsonDeserializer extends StdDeserializer<NodeInfo> {

    private final transient AppFileSystemStorage storage;

    public NodeInfoJsonDeserializer(AppFileSystemStorage storage) {
        super(NodeInfo.class);
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public NodeInfo deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        Objects.requireNonNull(storage, "NodeInfoJsonDeserializer should not be serialized/deserialized");
        try {
            NodeId id = null;
            String name = null;
            String pseudoClass = null;
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                switch (jsonParser.getCurrentName()) {
                    case "id":
                        jsonParser.nextToken();
                        id = storage.fromString(jsonParser.getValueAsString());
                        break;

                    case "name":
                        jsonParser.nextToken();
                        name = jsonParser.getValueAsString();
                        break;

                    case "pseudoClass":
                        jsonParser.nextToken();
                        pseudoClass = jsonParser.getValueAsString();
                        break;

                    default:
                        throw new AssertionError("Unexpected field: " + jsonParser.getCurrentName());
                }
            }
            return new NodeInfo(id, name, pseudoClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
