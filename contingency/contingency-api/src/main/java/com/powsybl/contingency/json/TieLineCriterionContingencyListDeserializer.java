/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.TieLineCriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.SingleNominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.TwoCountriesCriterion;

import java.io.IOException;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TieLineCriterionContingencyListDeserializer extends AbstractEquipmentCriterionContingencyListDeserializer<TieLineCriterionContingencyList> {

    public TieLineCriterionContingencyListDeserializer() {
        super(TieLineCriterionContingencyList.class);
    }

    @Override
    public TieLineCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext parsingContext = new ParsingContext();
        parser.nextToken();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeCommonAttributes(parser, deserializationContext, parsingContext, name));

        return new TieLineCriterionContingencyList(parsingContext.name,
                (TwoCountriesCriterion) parsingContext.countryCriterion,
                (SingleNominalVoltageCriterion) parsingContext.nominalVoltageCriterion,
                parsingContext.propertyCriteria,
                parsingContext.regexCriterion);
    }
}
