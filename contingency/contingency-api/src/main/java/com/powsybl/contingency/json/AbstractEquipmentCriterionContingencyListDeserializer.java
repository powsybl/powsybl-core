/**
 Copyright (c) 2023, RTE (http://www.rte-france.com)
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this
 file, You can obtain one at http://mozilla.org/MPL/2.0/.
 SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.list.AbstractEquipmentCriterionContingencyList;
import com.powsybl.iidm.criteria.Criterion;
import com.powsybl.iidm.criteria.PropertyCriterion;
import com.powsybl.iidm.criteria.RegexCriterion;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;

/**
 @author Hugo Kulesza hugo.kulesza@rte-france.com
 */
public abstract class AbstractEquipmentCriterionContingencyListDeserializer<T extends AbstractEquipmentCriterionContingencyList>
    extends StdDeserializer<T> {

    protected final ValueDeserializer<Object> criterionDeserializer;
    protected final ValueDeserializer<Object> propertyCriteriaDeserializer;

    protected AbstractEquipmentCriterionContingencyListDeserializer(Class<T> c) {
        this(c, null, null);
    }

    protected AbstractEquipmentCriterionContingencyListDeserializer(Class<T> c,
                                                                    ValueDeserializer<Object> criterionDeserializer,
                                                                    ValueDeserializer<Object> propertyCriteriaDeserializer) {
        super(c);
        this.criterionDeserializer = criterionDeserializer;
        this.propertyCriteriaDeserializer = propertyCriteriaDeserializer;
    }

    protected abstract ValueDeserializer<T> create(ValueDeserializer<Object> criterionDeserializer,
                                                ValueDeserializer<Object> propertyCriteriaDeserializer);

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws DatabindException {
        return create(JsonUtil.buildValueDeserializer(ctxt, property, Criterion.class),
            JsonUtil.buildListDeserializer(ctxt, property, Criterion.class));
    }

    protected static class ParsingContext {
        String name;
        Criterion countryCriterion = null;
        Criterion nominalVoltageCriterion = null;
        List<PropertyCriterion> propertyCriteria = Collections.emptyList();
        RegexCriterion regexCriterion = null;
    }

    protected boolean deserializeCommonAttributes(JsonParser parser, DeserializationContext ctx,
                                                  ParsingContext parsingCtx, String name, String expectedType) throws JacksonException {
        switch (name) {
            case "name" -> {
                parsingCtx.name = parser.nextStringValue();
                return true;
            }
            case "countryCriterion" -> {
                parser.nextToken();
                parsingCtx.countryCriterion = readCriterion(parser, ctx);
                return true;
            }
            case "nominalVoltageCriterion" -> {
                parser.nextToken();
                parsingCtx.nominalVoltageCriterion = readCriterion(parser, ctx);
                return true;
            }
            case "propertyCriteria" -> {
                parser.nextToken();
                parsingCtx.propertyCriteria = JsonUtil.readList(propertyCriteriaDeserializer, ctx, parser, PropertyCriterion.class);
                return true;
            }
            case "regexCriterion" -> {
                parser.nextToken();
                parsingCtx.regexCriterion = (RegexCriterion) readCriterion(parser, ctx);
                return true;
            }
            case "version" -> {
                parser.nextToken();
                return true;
            }
            case "type" -> {
                if (expectedType == null || !expectedType.equals(parser.nextStringValue())) {
                    throw new IllegalStateException("type should be: " + expectedType);
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private Criterion readCriterion(JsonParser parser, DeserializationContext ctx) {
        return JsonUtil.readValue(criterionDeserializer, ctx, parser, Criterion.class);
    }
}
