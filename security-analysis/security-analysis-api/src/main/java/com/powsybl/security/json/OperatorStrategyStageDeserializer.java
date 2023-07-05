package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.strategy.OperatorStrategyStage;

import java.io.IOException;
import java.util.List;

public class OperatorStrategyStageDeserializer extends StdDeserializer<OperatorStrategyStage> {

    public OperatorStrategyStageDeserializer() {
        super(OperatorStrategyStage.class);
    }

    private static class ParsingContext {
        String id;
        Condition condition;
        List<String> actionIds;
    }

    @Override
    public OperatorStrategyStage deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        OperatorStrategyStageDeserializer.ParsingContext context = new OperatorStrategyStageDeserializer.ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "id":
                    parser.nextToken();
                    context.id = parser.getValueAsString();
                    return true;
                case "condition":
                    parser.nextToken();
                    context.condition = JsonUtil.readValue(deserializationContext, parser, Condition.class);
                    return true;
                case "actionIds":
                    parser.nextToken();
                    context.actionIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    return true;
                default:
                    return false;
            }
        });
        return new OperatorStrategyStage(context.id, context.condition, context.actionIds);
    }
}
