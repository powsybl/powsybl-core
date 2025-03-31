/**
 Copyright (c) 2023, RTE (http://www.rte-france.com)
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this
 file, You can obtain one at http://mozilla.org/MPL/2.0/.
 SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.AbstractEquipmentCriterionContingencyList;
import com.powsybl.iidm.criteria.Criterion;
import com.powsybl.iidm.criteria.PropertyCriterion;
import com.powsybl.iidm.criteria.RegexCriterion;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 @author Hugo Kulesza hugo.kulesza@rte-france.com
 */
public abstract class AbstractEquipmentCriterionContingencyListDeserializer<T extends AbstractEquipmentCriterionContingencyList> extends StdDeserializer<T> {

    protected AbstractEquipmentCriterionContingencyListDeserializer(Class<T> c) {
        super(c);
    }

    protected static class ParsingContext {
        String name;
        Criterion countryCriterion = null;
        Criterion nominalVoltageCriterion = null;
        List<PropertyCriterion> propertyCriteria = Collections.emptyList();
        RegexCriterion regexCriterion = null;
    }

    protected boolean deserializeCommonAttributes(JsonParser parser, DeserializationContext ctx,
                                                  ParsingContext parsingCtx, String name, String expectedType) throws IOException {
        switch (name) {
            case "name" -> {
                parsingCtx.name = parser.nextTextValue();
                return true;
            }
            case "countryCriterion" -> {
                parser.nextToken();
                parsingCtx.countryCriterion = JsonUtil.readValue(ctx, parser, Criterion.class);
                return true;
            }
            case "nominalVoltageCriterion" -> {
                parser.nextToken();
                parsingCtx.nominalVoltageCriterion = JsonUtil.readValue(ctx, parser, Criterion.class);
                return true;
            }
            case "propertyCriteria" -> {
                parser.nextToken();
                parsingCtx.propertyCriteria = JsonUtil.readList(ctx, parser, Criterion.class);
                return true;
            }
            case "regexCriterion" -> {
                parser.nextToken();
                parsingCtx.regexCriterion = JsonUtil.readValue(ctx, parser, Criterion.class);
                return true;
            }
            case "version" -> {
                parser.nextToken();
                return true;
            }
            case "type" -> {
                // parser.nextTextValue() could returns null
                String typeStr = Objects.requireNonNull(parser.nextTextValue());
             
                if (!typeStr.equals(expectedType)) {
                    throw new IllegalStateException("type should be: " + expectedType);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

}
