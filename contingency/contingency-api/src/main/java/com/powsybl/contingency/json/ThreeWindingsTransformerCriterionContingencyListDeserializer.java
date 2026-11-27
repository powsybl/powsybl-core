/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.list.ThreeWindingsTransformerCriterionContingencyList;
import com.powsybl.iidm.criteria.SingleCountryCriterion;
import com.powsybl.iidm.criteria.ThreeNominalVoltageCriterion;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ThreeWindingsTransformerCriterionContingencyListDeserializer extends AbstractEquipmentCriterionContingencyListDeserializer<ThreeWindingsTransformerCriterionContingencyList> {

    public ThreeWindingsTransformerCriterionContingencyListDeserializer() {
        this(null, null);
    }

    public ThreeWindingsTransformerCriterionContingencyListDeserializer(ValueDeserializer<Object> criterionDeser, ValueDeserializer<Object> propertyDeser) {
        super(ThreeWindingsTransformerCriterionContingencyList.class, criterionDeser, propertyDeser);
    }

    @Override
    protected ThreeWindingsTransformerCriterionContingencyListDeserializer create(ValueDeserializer<Object> criterionDeser, ValueDeserializer<Object> propertyDeser) {
        return new ThreeWindingsTransformerCriterionContingencyListDeserializer(criterionDeser, propertyDeser);
    }

    @Override
    public ThreeWindingsTransformerCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        AbstractEquipmentCriterionContingencyListDeserializer.ParsingContext parsingContext = new AbstractEquipmentCriterionContingencyListDeserializer.ParsingContext();
        parser.nextToken();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeCommonAttributes(parser, deserializationContext,
                parsingContext, name, ThreeWindingsTransformerCriterionContingencyList.TYPE));

        return new ThreeWindingsTransformerCriterionContingencyList(parsingContext.name,
                (SingleCountryCriterion) parsingContext.countryCriterion,
                (ThreeNominalVoltageCriterion) parsingContext.nominalVoltageCriterion,
                parsingContext.propertyCriteria,
                parsingContext.regexCriterion);
    }
}
