package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.action.AreaInterchangeTargetUpdateAction;
import com.powsybl.action.AreaInterchangeTargetUpdateActionBuilder;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

public class AreaInterchangeTargetUpdateActionDeserializer extends StdDeserializer<AreaInterchangeTargetUpdateActionBuilder> {

    protected AreaInterchangeTargetUpdateActionDeserializer() {
        super(AreaInterchangeTargetUpdateActionBuilder.class);
    }

    @Override
    public AreaInterchangeTargetUpdateActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        AreaInterchangeTargetUpdateActionBuilder builder = new AreaInterchangeTargetUpdateActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!AreaInterchangeTargetUpdateAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + AreaInterchangeTargetUpdateAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "areaId":
                    builder.withAreaId(jsonParser.nextTextValue());
                    return true;
                case "target":
                    jsonParser.nextToken();
                    builder.withTarget(jsonParser.getValueAsDouble());
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
