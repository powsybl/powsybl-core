package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.action.AreaInterchangeTargetUpdateAction;

import java.io.IOException;

public class AreaInterchangeTargetUpdateActionSerializer extends StdSerializer<AreaInterchangeTargetUpdateAction> {

    AreaInterchangeTargetUpdateActionSerializer() {
        super(AreaInterchangeTargetUpdateAction.class);
    }

    @Override
    public void serialize(AreaInterchangeTargetUpdateAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("areaId", action.getAreaId());
        jsonGenerator.writeNumberField("target", action.getTarget());
        jsonGenerator.writeEndObject();
    }
}
