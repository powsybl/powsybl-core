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
import com.powsybl.afs.storage.NodeGenericMetadata;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeGenericMetadataJsonDeserializer extends StdDeserializer<NodeGenericMetadata> {

    private static class JsonParsingContext {
        final NodeGenericMetadata metadata = new NodeGenericMetadata();
        String type = null;
        String name = null;
    }

    public NodeGenericMetadataJsonDeserializer() {
        super(NodeGenericMetadata.class);
    }

    private static void parseFieldName(JsonParser jsonParser, JsonParsingContext parsingContext) throws IOException {
        switch (jsonParser.getCurrentName()) {
            case NodeGenericMetadataJsonSerializer.TYPE:
                jsonParser.nextToken();
                parsingContext.type = jsonParser.getValueAsString();
                break;

            case NodeGenericMetadataJsonSerializer.NAME:
                jsonParser.nextToken();
                parsingContext.name = jsonParser.getValueAsString();
                break;

            case NodeGenericMetadataJsonSerializer.VALUE:
                Objects.requireNonNull(parsingContext.name);
                Objects.requireNonNull(parsingContext.type);
                jsonParser.nextToken();
                switch (parsingContext.type) {
                    case NodeGenericMetadataJsonSerializer.STRING:
                        parsingContext.metadata.setString(parsingContext.name, jsonParser.getValueAsString());
                        break;
                    case NodeGenericMetadataJsonSerializer.DOUBLE:
                        parsingContext.metadata.setDouble(parsingContext.name, jsonParser.getValueAsDouble());
                        break;
                    case NodeGenericMetadataJsonSerializer.INT:
                        parsingContext.metadata.setInt(parsingContext.name, jsonParser.getValueAsInt());
                        break;
                    case NodeGenericMetadataJsonSerializer.BOOLEAN:
                        parsingContext.metadata.setBoolean(parsingContext.name, jsonParser.getValueAsBoolean());
                        break;
                    default:
                        throw new AssertionError("Unexpected metadata type " + parsingContext.type);
                }
                break;

            default:
                throw new AssertionError("Unexpected field: " + jsonParser.getCurrentName());

        }
    }

    @Override
    public NodeGenericMetadata deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonParsingContext parsingContext = new JsonParsingContext();
        JsonToken token;
        while ((token = jsonParser.nextToken()) != null) {
            if (token == JsonToken.END_ARRAY) {
                break;
            } else if (token == JsonToken.FIELD_NAME) {
                parseFieldName(jsonParser, parsingContext);
            }
        }
        return parsingContext.metadata;
    }
}
