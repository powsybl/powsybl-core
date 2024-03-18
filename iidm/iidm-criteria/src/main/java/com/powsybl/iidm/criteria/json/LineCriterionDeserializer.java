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
import com.powsybl.iidm.criteria.LineCriterion;
import com.powsybl.iidm.criteria.TwoCountriesCriterion;
import com.powsybl.iidm.criteria.TwoNominalVoltageCriterion;

import java.io.IOException;

/**
 * <p>Deserializer for {@link LineCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LineCriterionDeserializer extends AbstractNetworkElementCriterionDeserializer<LineCriterion> {
    public LineCriterionDeserializer() {
        super(LineCriterion.class);
    }

    @Override
    public LineCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        AbstractNetworkElementCriterionDeserializer.ParsingContext parsingContext = new AbstractNetworkElementCriterionDeserializer.ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeAttributes(parser, deserializationContext, parsingContext, name,
                LineCriterion.TYPE, CriterionType.TWO_COUNTRY, CriterionType.TWO_NOMINAL_VOLTAGE));

        return new LineCriterion(parsingContext.name,
                (TwoCountriesCriterion) parsingContext.countryCriterion,
                (TwoNominalVoltageCriterion) parsingContext.nominalVoltageCriterion);
    }

}
