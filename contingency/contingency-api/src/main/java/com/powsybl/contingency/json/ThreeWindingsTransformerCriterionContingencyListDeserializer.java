/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.ThreeWindingsTransformerCriterionContingencyList;
import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.ThreeNominalVoltageCriterion;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ThreeWindingsTransformerCriterionContingencyListDeserializer extends AbstractEquipmentCriterionContingencyListDeserializer<ThreeWindingsTransformerCriterionContingencyList> {

    public ThreeWindingsTransformerCriterionContingencyListDeserializer() {
        super(ThreeWindingsTransformerCriterionContingencyList.class);
    }

    @Override
    public ThreeWindingsTransformerCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        AbstractEquipmentCriterionContingencyListDeserializer.ParsingContext parsingContext = new AbstractEquipmentCriterionContingencyListDeserializer.ParsingContext();
        parser.nextToken();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeCommonAttributes(parser, deserializationContext, parsingContext, name));

        return new ThreeWindingsTransformerCriterionContingencyList(parsingContext.name,
                (SingleCountryCriterion) parsingContext.countryCriterion,
                (ThreeNominalVoltageCriterion) parsingContext.nominalVoltageCriterion,
                parsingContext.propertyCriteria,
                parsingContext.regexCriterion);
    }
}
