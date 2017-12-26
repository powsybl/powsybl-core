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
import com.powsybl.afs.storage.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoJsonDeserializer extends StdDeserializer<NodeInfo> {

    private enum MetadataType {
        STRING_METADATA,
        DOUBLE_METADATA,
        INT_METADATA,
        BOOLEAN_METADATA
    }

    private static class JsonParsingContext {
        NodeId id = null;
        String name = null;
        String pseudoClass = null;
        String description = null;
        long creationTime = -1;
        long modificationTime = -1;
        int version = -1;
        final NodeMetadata metadata = new NodeMetadata();
        MetadataType metadataType = null;
        String metadataName = null;
    }

    public NodeInfoJsonDeserializer() {
        super(NodeInfo.class);
    }

    private static void parseFieldName(JsonParser jsonParser, AppStorage storage, JsonParsingContext parsingContext) throws IOException {
        switch (jsonParser.getCurrentName()) {
            case NodeInfoJsonSerializer.ID:
                jsonParser.nextToken();
                parsingContext.id = storage.fromString(jsonParser.getValueAsString());
                break;

            case NodeInfoJsonSerializer.NAME:
                jsonParser.nextToken();
                if (parsingContext.metadataType == null) {
                    parsingContext.name = jsonParser.getValueAsString();
                } else {
                    parsingContext.metadataName = jsonParser.getValueAsString();
                }
                break;

            case NodeInfoJsonSerializer.PSEUDO_CLASS:
                jsonParser.nextToken();
                parsingContext.pseudoClass = jsonParser.getValueAsString();
                break;

            case NodeInfoJsonSerializer.DESCRIPTION:
                jsonParser.nextToken();
                parsingContext.description = jsonParser.getValueAsString();
                break;

            case NodeInfoJsonSerializer.CREATION_TIME:
                jsonParser.nextToken();
                parsingContext.creationTime = jsonParser.getValueAsLong();
                break;

            case NodeInfoJsonSerializer.MODIFICATION_TIME:
                jsonParser.nextToken();
                parsingContext.modificationTime = jsonParser.getValueAsLong();
                break;

            case NodeInfoJsonSerializer.VERSION:
                jsonParser.nextToken();
                parsingContext.version = jsonParser.getValueAsInt();
                break;

            case NodeInfoJsonSerializer.VALUE:
                Objects.requireNonNull(parsingContext.metadataName);
                Objects.requireNonNull(parsingContext.metadataType);
                jsonParser.nextToken();
                switch (parsingContext.metadataType) {
                    case STRING_METADATA:
                        parsingContext.metadata.setStringMetadata(parsingContext.metadataName, jsonParser.getValueAsString());
                        break;
                    case DOUBLE_METADATA:
                        parsingContext.metadata.setDoubleMetadata(parsingContext.metadataName, jsonParser.getValueAsDouble());
                        break;
                    case INT_METADATA:
                        parsingContext.metadata.setIntMetadata(parsingContext.metadataName, jsonParser.getValueAsInt());
                        break;
                    case BOOLEAN_METADATA:
                        parsingContext.metadata.setBooleanMetadata(parsingContext.metadataName, jsonParser.getValueAsBoolean());
                        break;
                    default:
                        throw new AssertionError("Unexpected metadata type " + parsingContext.metadataType);
                }
                break;

            case NodeInfoJsonSerializer.STRING_METADATA:
                parsingContext.metadataType = MetadataType.STRING_METADATA;
                break;

            case NodeInfoJsonSerializer.DOUBLE_METADATA:
                parsingContext.metadataType = MetadataType.DOUBLE_METADATA;
                break;

            case NodeInfoJsonSerializer.INT_METADATA:
                parsingContext.metadataType = MetadataType.INT_METADATA;
                break;

            case NodeInfoJsonSerializer.BOOLEAN_METADATA:
                parsingContext.metadataType = MetadataType.BOOLEAN_METADATA;
                break;

            default:
                throw new AssertionError("Unexpected field: " + jsonParser.getCurrentName());

        }
    }

    @Override
    public NodeInfo deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        AppStorage storage = (AppStorage) deserializationContext.getAttribute("storage");
        if (storage == null) {
            throw new AfsStorageException("Storage not found in deserialization context");
        }
        try {
            JsonParsingContext parsingContext = new JsonParsingContext();
            JsonToken token;
            while ((token = jsonParser.nextToken()) != null) {
                if (token == JsonToken.END_ARRAY) {
                    parsingContext.metadataType = null;
                    parsingContext.metadataName = null;
                } else if (token == JsonToken.FIELD_NAME) {
                    parseFieldName(jsonParser, storage, parsingContext);
                }
            }
            return new NodeInfo(parsingContext.id, parsingContext.name, parsingContext.pseudoClass, parsingContext.description,
                                parsingContext.creationTime, parsingContext.modificationTime, parsingContext.version, parsingContext.metadata);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
