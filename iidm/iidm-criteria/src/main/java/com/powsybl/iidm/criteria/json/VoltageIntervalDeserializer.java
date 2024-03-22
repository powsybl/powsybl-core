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
import com.powsybl.iidm.criteria.SingleNominalVoltageCriterion;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>Deserializer for {@link com.powsybl.iidm.criteria.SingleNominalVoltageCriterion.VoltageInterval} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class VoltageIntervalDeserializer extends StdDeserializer<SingleNominalVoltageCriterion.VoltageInterval> {

    public VoltageIntervalDeserializer() {
        super(SingleNominalVoltageCriterion.VoltageInterval.class);
    }

    private static class ParsingContext {
        Double nominalVoltageLowBound;
        Boolean lowClosed;
        Double nominalVoltageHighBound;
        Boolean highClosed;
    }

    @Override
    public SingleNominalVoltageCriterion.VoltageInterval deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "nominalVoltageLowBound" -> {
                    parser.nextToken();
                    context.nominalVoltageLowBound = parser.getValueAsDouble();
                    return true;
                }
                case "lowClosed" -> {
                    parser.nextToken();
                    context.lowClosed = parser.getValueAsBoolean();
                    return true;
                }
                case "nominalVoltageHighBound" -> {
                    parser.nextToken();
                    context.nominalVoltageHighBound = parser.getValueAsDouble();
                    return true;
                }
                case "highClosed" -> {
                    parser.nextToken();
                    context.highClosed = parser.getValueAsBoolean();
                    return true;
                }
                default -> {
                    return false;
                }
            }
        });
        String errorMessageTemplate = "Attribute '%s' should be defined";
        Objects.requireNonNull(context.nominalVoltageLowBound, String.format(errorMessageTemplate, "nominalVoltageLowBound"));
        Objects.requireNonNull(context.nominalVoltageHighBound, String.format(errorMessageTemplate, "nominalVoltageHighBound"));
        Objects.requireNonNull(context.lowClosed, String.format(errorMessageTemplate, "lowClosed"));
        Objects.requireNonNull(context.highClosed, String.format(errorMessageTemplate, "highClosed"));
        return new SingleNominalVoltageCriterion.VoltageInterval(context.nominalVoltageLowBound, context.nominalVoltageHighBound,
                context.lowClosed, context.highClosed);
    }
}
