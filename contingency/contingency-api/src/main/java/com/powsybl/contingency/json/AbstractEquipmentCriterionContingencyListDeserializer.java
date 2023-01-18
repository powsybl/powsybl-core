package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.AbstractEquipmentCriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;
import com.powsybl.contingency.contingency.list.criterion.RegexCriterion;

import java.io.IOException;
import java.util.List;

public abstract class AbstractEquipmentCriterionContingencyListDeserializer<T extends AbstractEquipmentCriterionContingencyList> extends StdDeserializer<T> {

    protected AbstractEquipmentCriterionContingencyListDeserializer(Class<T> c) {
        super(c);
    }

    protected static class ParsingContext {
        String name;
        Criterion countryCriterion;
        Criterion nominalVoltageCriterion;
        List<PropertyCriterion> propertyCriteria;
        RegexCriterion regexCriterion;
    }

    protected boolean deserializeCommonAttributes(JsonParser parser, DeserializationContext ctx,
                                                  ParsingContext parsingCtx, String name) throws IOException {
        switch (name) {
            case "name":
                parsingCtx.name = parser.nextTextValue();
                return true;
            case "countryCriterion":
                parser.nextToken();
                parsingCtx.countryCriterion = JsonUtil.readValueWithContext(ctx, parser, Criterion.class);
                return true;
            case "nominalVoltageCriterion":
                parser.nextToken();
                parsingCtx.nominalVoltageCriterion = JsonUtil.readValueWithContext(ctx, parser, Criterion.class);
                return true;
            case "propertyCriteria":
                parser.nextToken();
                parsingCtx.propertyCriteria = JsonUtil.readList(ctx, parser, Criterion.class);
                return true;
            case "regexCriterion":
                parser.nextToken();
                parsingCtx.regexCriterion = JsonUtil.readValueWithContext(ctx, parser, Criterion.class);
                return true;
            case "version":
            case "type":
                parser.nextToken();
                return true;
            default:
                return false;
        }
    }

}
