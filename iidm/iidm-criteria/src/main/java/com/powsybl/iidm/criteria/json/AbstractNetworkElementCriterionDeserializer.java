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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.NetworkElementCriterion.NetworkElementCriterionType;
import com.powsybl.iidm.criteria.Criterion;
import com.powsybl.iidm.criteria.Criterion.CriterionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>Abstract class for {@link NetworkElementCriterion} implementations' deserializers.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractNetworkElementCriterionDeserializer<T extends NetworkElementCriterion> extends StdDeserializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkElementCriterionDeserializer.class);

    protected AbstractNetworkElementCriterionDeserializer(Class<T> c) {
        super(c);
    }

    protected static class ParsingContext {
        String name = null;
        Criterion countryCriterion = null;
        Criterion nominalVoltageCriterion = null;
    }

    protected boolean deserializeAttributes(JsonParser parser, DeserializationContext ctx,
                                            ParsingContext parsingCtx, String name,
                                            NetworkElementCriterionType expectedCriterionType,
                                            CriterionType expectedCountriesCriterionType,
                                            CriterionType expectedNominalVoltagesCriterionType) throws IOException {
        String expectedType = expectedCriterionType.getName();
        switch (name) {
            case "countryCriterion" -> {
                parser.nextToken();
                parsingCtx.countryCriterion = JsonUtil.readValue(ctx, parser, Criterion.class);
                if (parsingCtx.countryCriterion != null && parsingCtx.countryCriterion.getType() != expectedCountriesCriterionType) {
                    throw new IllegalStateException(String.format("'%s' equipment criteria only support '%s' type countryCriterion",
                            expectedType, expectedCountriesCriterionType.name()));
                }
                return true;
            }
            case "nominalVoltageCriterion" -> {
                parser.nextToken();
                parsingCtx.nominalVoltageCriterion = JsonUtil.readValue(ctx, parser, Criterion.class);
                if (parsingCtx.nominalVoltageCriterion != null && parsingCtx.nominalVoltageCriterion.getType() != expectedNominalVoltagesCriterionType) {
                    throw new IllegalStateException(String.format("'%s' equipment criteria only support '%s' type nominalVoltageCriterion",
                            expectedType, expectedNominalVoltagesCriterionType.name()));
                }
                return true;
            }
            default -> {
                return deserializeCommonAttributes(parser, parsingCtx, name, expectedCriterionType);
            }
        }
    }

    protected boolean deserializeCommonAttributes(JsonParser parser, ParsingContext parsingCtx, String name,
                                                  NetworkElementCriterionType expectedCriterionType) throws IOException {
        String expectedType = expectedCriterionType.getName();
        switch (name) {
            case "name" -> {
                parsingCtx.name = parser.nextTextValue();
                return true;
            }
            case "version" -> {
                parser.nextToken();
                return true;
            }
            case "type" -> {
                String type = parser.nextTextValue();
                if (!type.equals(expectedType)) {
                    throw new IllegalStateException(String.format("'type' is expected to be '%s' but encountered value was '%s'",
                            expectedType, type));
                }
                return true;
            }
            default -> {
                LOGGER.warn("Ignored element '{}' for criterion of type '{}'", name, expectedType);
                return false;
            }
        }
    }
}
