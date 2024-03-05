package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.action.AbstractLoadAction;
import com.powsybl.security.action.LoadAction;

import java.io.IOException;

public abstract class AbstractLoadActionDeserializer <T extends AbstractLoadAction> extends StdDeserializer<T> {

    protected AbstractLoadActionDeserializer(Class<T> vc) {
        super(vc);
    }

    protected static class ParsingContext {
        String id;
        String loadId;
        Boolean relativeValue;
        Double activePowerValue;
        Double reactivePowerValue;
    }

    protected abstract T createAction(ParsingContext context);

    @Override
    public T deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!LoadAction.NAME.equals(parser.nextTextValue())) {
                        throw JsonMappingException.from(parser, "Expected type " + LoadAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = parser.nextTextValue();
                    return true;
                case "loadId":
                    context.loadId = parser.nextTextValue();
                    return true;
                case "relativeValue":
                    parser.nextToken();
                    context.relativeValue = parser.getValueAsBoolean();
                    return true;
                case "activePowerValue":
                    parser.nextToken();
                    context.activePowerValue = parser.getValueAsDouble();
                    return true;
                case "reactivePowerValue":
                    parser.nextToken();
                    context.reactivePowerValue = parser.getValueAsDouble();
                    return true;
                default:
                    return false;
            }
        });
        return createAction(context);
    }
}
