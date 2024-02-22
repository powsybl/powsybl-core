/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.util.criterion.Criterion.CriterionType;
import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.ThreeNominalVoltageCriterion;
import com.powsybl.iidm.network.util.criterion.ThreeWindingsTransformerCriterion;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ThreeWindingsTransformerCriterionDeserializer extends AbstractNetworkElementCriterionDeserializer<ThreeWindingsTransformerCriterion> {
    public ThreeWindingsTransformerCriterionDeserializer() {
        super(ThreeWindingsTransformerCriterion.class);
    }

    @Override
    public ThreeWindingsTransformerCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext parsingContext = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeAttributes(parser, deserializationContext, parsingContext, name,
                ThreeWindingsTransformerCriterion.TYPE, CriterionType.SINGLE_COUNTRY, CriterionType.THREE_NOMINAL_VOLTAGE));

        return new ThreeWindingsTransformerCriterion(parsingContext.name)
                .setSingleCountryCriterion((SingleCountryCriterion) parsingContext.countryCriterion)
                .setThreeNominalVoltageCriterion((ThreeNominalVoltageCriterion) parsingContext.nominalVoltageCriterion);
    }
}
