package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.strategy.OperatorStrategyStage;

import java.io.IOException;

public class OperatorStrategyStageSerializer extends StdSerializer<OperatorStrategyStage> {

    public OperatorStrategyStageSerializer() {
        super(OperatorStrategyStage.class);
    }

    @Override
    public void serialize(OperatorStrategyStage operatorStrategyStage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", operatorStrategyStage.getId());
        serializerProvider.defaultSerializeField("condition", operatorStrategyStage.getCondition(), jsonGenerator);
        serializerProvider.defaultSerializeField("actionIds", operatorStrategyStage.getActionIds(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
