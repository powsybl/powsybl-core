package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.results.OperatorStrategyResult;

import java.io.IOException;

public class ConditionalActionsResultSerializer extends StdSerializer<OperatorStrategyResult.ConditionalActionsResult> {

    public ConditionalActionsResultSerializer() {
        super(OperatorStrategyResult.ConditionalActionsResult.class);
    }

    @Override
    public void serialize(OperatorStrategyResult.ConditionalActionsResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializerProvider.defaultSerializeField("status", result.getStatus(), jsonGenerator);
        serializerProvider.defaultSerializeField("limitViolationsResult", result.getLimitViolationsResult(), jsonGenerator);
        serializerProvider.defaultSerializeField("networkResult", result.getNetworkResult(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
