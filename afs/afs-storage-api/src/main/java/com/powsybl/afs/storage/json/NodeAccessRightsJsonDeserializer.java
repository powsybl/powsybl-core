package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.afs.storage.NodeAccessRights;

import java.io.IOException;
import java.util.Objects;

public class NodeAccessRightsJsonDeserializer  extends StdDeserializer<NodeAccessRights> {

    private static class JsonParsingContext {
        final NodeAccessRights accessRights = new NodeAccessRights();
        String type = null;
        String name = null;
    }

    public NodeAccessRightsJsonDeserializer() {
        super(NodeAccessRights.class);
    }

    private static void parseFieldName(JsonParser jsonParser, NodeAccessRightsJsonDeserializer.JsonParsingContext parsingContext) throws IOException {
        switch (jsonParser.getCurrentName()) {
            case NodeAccessRightsJsonSerializer.TYPE:
                jsonParser.nextToken();
                parsingContext.type = jsonParser.getValueAsString();
                break;

            case NodeAccessRightsJsonSerializer.NAME:
                jsonParser.nextToken();
                parsingContext.name = jsonParser.getValueAsString();
                break;

            case NodeAccessRightsJsonSerializer.VALUE:
                Objects.requireNonNull(parsingContext.type);
                Objects.requireNonNull(parsingContext.name);
                jsonParser.nextToken();
                switch (parsingContext.type) {
                    case NodeAccessRightsJsonSerializer.USER:
                        parsingContext.accessRights.setUserRights(parsingContext.name, (short) jsonParser.getValueAsInt());
                        break;
                    case NodeAccessRightsJsonSerializer.GROUP:
                        parsingContext.accessRights.setGroupRights(parsingContext.name, (short) jsonParser.getValueAsInt());
                        break;
                    case NodeAccessRightsJsonSerializer.OTHERS:
                        parsingContext.accessRights.setOthersRights((short) jsonParser.getValueAsInt());
                        break;
                    default:
                        throw new AssertionError("Unexpected access right type " + parsingContext.type);
                }
                break;

            default:
                throw new AssertionError("Unexpected field: " + jsonParser.getCurrentName());

        }
    }

    @Override
    public NodeAccessRights deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        NodeAccessRightsJsonDeserializer.JsonParsingContext parsingContext = new NodeAccessRightsJsonDeserializer.JsonParsingContext();
        JsonToken token;
        while ((token = jsonParser.nextToken()) != null) {
            if (token == JsonToken.END_ARRAY) {
                break;
            } else if (token == JsonToken.FIELD_NAME) {
                parseFieldName(jsonParser, parsingContext);
            }
        }
        return parsingContext.accessRights;
    }
}
