/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.criteria.Criterion.CriterionType;
import com.powsybl.iidm.criteria.DanglingLineCriterion;
import com.powsybl.iidm.criteria.SingleCountryCriterion;
import com.powsybl.iidm.criteria.SingleNominalVoltageCriterion;

import java.io.IOException;

/**
 * <p>Deserializer for {@link DanglingLineCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DanglingLineCriterionDeserializer extends AbstractNetworkElementCriterionDeserializer<DanglingLineCriterion> {
    public DanglingLineCriterionDeserializer() {
        super(DanglingLineCriterion.class);
    }

    @Override
    public DanglingLineCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        AbstractNetworkElementCriterionDeserializer.ParsingContext parsingContext = new AbstractNetworkElementCriterionDeserializer.ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeAttributes(parser, deserializationContext, parsingContext, name,
                DanglingLineCriterion.TYPE, CriterionType.SINGLE_COUNTRY, CriterionType.SINGLE_NOMINAL_VOLTAGE));

        return new DanglingLineCriterion(parsingContext.name,
                (SingleCountryCriterion) parsingContext.countryCriterion,
                (SingleNominalVoltageCriterion) parsingContext.nominalVoltageCriterion);
    }
}
