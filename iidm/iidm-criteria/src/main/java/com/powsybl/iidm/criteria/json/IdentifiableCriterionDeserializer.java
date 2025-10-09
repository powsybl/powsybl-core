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
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.criteria.AtLeastOneCountryCriterion;
import com.powsybl.iidm.criteria.AtLeastOneNominalVoltageCriterion;
import com.powsybl.iidm.criteria.Criterion.CriterionType;
import com.powsybl.iidm.criteria.IdentifiableCriterion;
import com.powsybl.iidm.criteria.NetworkElementCriterion;

import java.io.IOException;

/**
 * <p>Deserializer for {@link IdentifiableCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IdentifiableCriterionDeserializer extends AbstractNetworkElementCriterionDeserializer<IdentifiableCriterion> {
    public IdentifiableCriterionDeserializer() {
        super(IdentifiableCriterion.class);
    }

    @Override
    public IdentifiableCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext parsingContext = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> deserializeAttributes(parser, deserializationContext, parsingContext, name,
                IdentifiableCriterion.TYPE, CriterionType.AT_LEAST_ONE_COUNTRY, CriterionType.AT_LEAST_ONE_NOMINAL_VOLTAGE));

        if (parsingContext.countryCriterion != null) {
            if (parsingContext.nominalVoltageCriterion != null) {
                return new IdentifiableCriterion(parsingContext.name,
                        (AtLeastOneCountryCriterion) parsingContext.countryCriterion,
                        (AtLeastOneNominalVoltageCriterion) parsingContext.nominalVoltageCriterion);
            } else {
                return new IdentifiableCriterion(parsingContext.name,
                        (AtLeastOneCountryCriterion) parsingContext.countryCriterion);
            }
        } else if (parsingContext.nominalVoltageCriterion != null) {
            return new IdentifiableCriterion(parsingContext.name,
                    (AtLeastOneNominalVoltageCriterion) parsingContext.nominalVoltageCriterion);
        } else {
            throw new PowsyblException("Criterion of type '" + NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIABLE.getName() + "'" +
                    " should have at least one sub-criterion");
        }
    }
}
