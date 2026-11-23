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
import com.powsybl.iidm.criteria.BoundaryLineCriterion;
import com.powsybl.iidm.criteria.Criterion.CriterionType;
import com.powsybl.iidm.criteria.NetworkElementCriterion.NetworkElementCriterionType;
import com.powsybl.iidm.criteria.SingleCountryCriterion;
import com.powsybl.iidm.criteria.SingleNominalVoltageCriterion;

import java.io.IOException;

/**
 * <p>Deserializer for {@link BoundaryLineCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class BoundaryLineCriterionDeserializer extends AbstractNetworkElementCriterionDeserializer<BoundaryLineCriterion> {
    public BoundaryLineCriterionDeserializer() {
        super(BoundaryLineCriterion.class);
    }

    @Override
    public BoundaryLineCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        AbstractNetworkElementCriterionDeserializer.ParsingContext parsingContext = fillParsingContext(parser, deserializationContext,
                BoundaryLineCriterion.TYPE, CriterionType.SINGLE_COUNTRY, CriterionType.SINGLE_NOMINAL_VOLTAGE);

        return new BoundaryLineCriterion(parsingContext.name,
                (SingleCountryCriterion) parsingContext.countryCriterion,
                (SingleNominalVoltageCriterion) parsingContext.nominalVoltageCriterion);
    }

    @Override
    protected void checkType(ParsingContext parsingCtx, NetworkElementCriterionType expectedCriterionType) {
        String version = parsingCtx.version;
        String type = parsingCtx.type;
        String expectedType;
        if (JsonUtil.compareVersions(version, "1.1") >= 0) {
            expectedType = "boundaryLineCriterion";
        } else {
            expectedType = "danglingLineCriterion";
        }
        if (!expectedType.equals(type)) {
            throw new IllegalStateException(String.format("'type' is expected to be '%s' but encountered value was '%s'",
                    expectedType, type));
        }
    }
}
