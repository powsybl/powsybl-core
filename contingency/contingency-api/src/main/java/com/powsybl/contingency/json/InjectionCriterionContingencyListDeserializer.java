/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.list.InjectionCriterionContingencyList;
import com.powsybl.iidm.criteria.Criterion;
import com.powsybl.iidm.criteria.SingleCountryCriterion;
import com.powsybl.iidm.criteria.SingleNominalVoltageCriterion;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class InjectionCriterionContingencyListDeserializer extends AbstractEquipmentCriterionContingencyListDeserializer<InjectionCriterionContingencyList> {

    public InjectionCriterionContingencyListDeserializer() {
        this(null, null);
    }

    protected InjectionCriterionContingencyListDeserializer(JsonDeserializer<?> criterionDeser, JsonDeserializer<?> propertyDeser) {
        super(InjectionCriterionContingencyList.class, criterionDeser, propertyDeser);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> criterionDeser = ctxt.findContextualValueDeserializer(ctxt.constructType(Criterion.class), property);
        JsonDeserializer<?> propertyDeser = ctxt.findContextualValueDeserializer(
            ctxt.getTypeFactory().constructCollectionType(ArrayList.class, Criterion.class), property);
        return new InjectionCriterionContingencyListDeserializer(criterionDeser, propertyDeser);
    }

    String identifiableType;

    @Override
    public InjectionCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        AbstractEquipmentCriterionContingencyListDeserializer.ParsingContext parsingContext = new AbstractEquipmentCriterionContingencyListDeserializer.ParsingContext();
        parser.nextToken();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            boolean found = deserializeCommonAttributes(parser, deserializationContext, parsingContext,
                    name, InjectionCriterionContingencyList.TYPE);
            if (found) {
                return true;
            }
            if (name.equals("identifiableType")) {
                identifiableType = parser.nextTextValue();
                return true;
            }
            return false;
        });

        return new InjectionCriterionContingencyList(parsingContext.name,
                identifiableType,
                (SingleCountryCriterion) parsingContext.countryCriterion,
                (SingleNominalVoltageCriterion) parsingContext.nominalVoltageCriterion,
                parsingContext.propertyCriteria,
                parsingContext.regexCriterion);
    }
}
