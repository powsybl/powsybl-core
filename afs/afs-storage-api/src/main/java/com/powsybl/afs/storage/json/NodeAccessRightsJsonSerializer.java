package com.powsybl.afs.storage.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.afs.storage.NodeAccessRights;

import java.io.IOException;
import java.util.Map;

public class NodeAccessRightsJsonSerializer  extends StdSerializer<NodeAccessRights> {

    static final String VALUE = "value";
    static final String NAME = "name";
    static final String TYPE = "type";
    static final String USER = "user";
    static final String GROUP = "group";
    static final String OTHERS = "others";

    public NodeAccessRightsJsonSerializer() {
        super(NodeAccessRights.class);
    }

    @Override
    public void serialize(NodeAccessRights nodeAccessRights, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (Map.Entry<String, Integer> e : nodeAccessRights.getUsersRights().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, USER);
            jsonGenerator.writeStringField(NAME, e.getKey());
            jsonGenerator.writeNumberField(VALUE, e.getValue());
            jsonGenerator.writeEndObject();
        }
        for (Map.Entry<String, Integer> e : nodeAccessRights.getGroupsRights().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, GROUP);
            jsonGenerator.writeStringField(NAME, e.getKey());
            jsonGenerator.writeNumberField(VALUE, e.getValue());
            jsonGenerator.writeEndObject();
        }

        if (nodeAccessRights.getOthersRights() != null) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(TYPE, OTHERS);
            jsonGenerator.writeStringField(NAME, OTHERS);
            jsonGenerator.writeNumberField(VALUE, nodeAccessRights.getOthersRights());
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndArray();
    }
}
