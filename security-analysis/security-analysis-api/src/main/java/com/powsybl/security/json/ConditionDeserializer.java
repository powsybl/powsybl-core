package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.condition.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ConditionDeserializer extends StdDeserializer<Condition> {

    private static class ParsingContext {
        String type;
        List<String> violationIds;
    }

    public ConditionDeserializer() {
        super(Condition.class);
    }

    @Override
    public Condition deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "type":
                    context.type = parser.nextTextValue();
                    return true;
                case "violationIds":
                    parser.nextToken();
                    context.violationIds = parser.readValueAs(new TypeReference<List<String>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        switch (context.type) {
            case TrueCondition.NAME:
                return new TrueCondition();
            case AnyViolationCondition.NAME:
                return new AnyViolationCondition();
            case AtLeastOneViolationCondition.NAME:
                return new AtLeastOneViolationCondition(context.violationIds);
            case AllViolationCondition.NAME:
                return new AllViolationCondition(context.violationIds);
            default:
                throw new JsonMappingException(parser, "Unexpected condition type: " + context.type);
        }
    }
}
