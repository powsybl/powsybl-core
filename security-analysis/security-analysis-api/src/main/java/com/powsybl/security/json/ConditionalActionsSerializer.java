package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.strategy.ConditionalActions;

import java.io.IOException;

public class ConditionalActionsSerializer extends StdSerializer<ConditionalActions> {

    public ConditionalActionsSerializer() {
        super(ConditionalActions.class);
    }

    @Override
    public void serialize(ConditionalActions operatorStrategyStage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", operatorStrategyStage.getId());
        serializerProvider.defaultSerializeField("condition", operatorStrategyStage.getCondition(), jsonGenerator);
        serializerProvider.defaultSerializeField("actionIds", operatorStrategyStage.getActionIds(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
