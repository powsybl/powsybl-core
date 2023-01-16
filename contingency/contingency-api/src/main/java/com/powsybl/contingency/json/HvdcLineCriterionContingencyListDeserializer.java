/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.DefaultContingencyList;
import com.powsybl.contingency.contingency.list.HvdcLineCriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class HvdcLineCriterionContingencyListDeserializer extends StdDeserializer<HvdcLineCriterionContingencyList> {

    public HvdcLineCriterionContingencyListDeserializer() {
        super(DefaultContingencyList.class);
    }

    @Override
    public HvdcLineCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        TwoCountriesCriterion countryCriterion = null;
        TwoNominalVoltageCriterion nominalVoltageCriterion = null;
        List<PropertyCriterion> propertyCriteria = Collections.emptyList();
        RegexCriterion regexCriterion = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    break;

                case "name":
                    name = parser.nextTextValue();
                    break;

                case "type":
                    parser.nextToken();
                    break;

                case "countryCriterion":
                    parser.nextToken();
                    countryCriterion = (TwoCountriesCriterion) deserializationContext.readValue(parser, Criterion.class);
                    break;
                case "nominalVoltageCriterion":
                    parser.nextToken();
                    nominalVoltageCriterion = (TwoNominalVoltageCriterion) deserializationContext.readValue(parser, Criterion.class);
                    break;
                case "propertyCriteria":
                    parser.nextToken();
                    propertyCriteria = (List<PropertyCriterion>) (List<?>) JsonUtil.readList(deserializationContext, parser, Criterion.class);
                    break;
                case "regexCriterion":
                    parser.nextToken();
                    regexCriterion = (RegexCriterion) deserializationContext.readValue(parser, Criterion.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new HvdcLineCriterionContingencyList(name, countryCriterion,
                nominalVoltageCriterion, propertyCriteria, regexCriterion);
    }
}
