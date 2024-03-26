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
import com.powsybl.iidm.criteria.VoltageInterval;

import java.io.IOException;

import static com.powsybl.iidm.criteria.json.util.DeserializerUtils.checkBoundData;

/**
 * <p>Deserializer for {@link VoltageInterval} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class VoltageIntervalDeserializer extends StdDeserializer<VoltageInterval> {

    public static final String MISSING_BOUND_ATTRIBUTE_MESSAGE = "Missing \"%s\" attribute for nominal voltage interval with non-null \"%s\" attribute.";

    public VoltageIntervalDeserializer() {
        super(VoltageInterval.class);
    }

    private static class ParsingContext {
        Double nominalVoltageLowBound;
        Boolean lowClosed;
        Double nominalVoltageHighBound;
        Boolean highClosed;
    }

    @Override
    public VoltageInterval deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
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
        VoltageInterval.Builder builder = VoltageInterval.builder();
        if (checkBoundData(context.nominalVoltageLowBound, context.lowClosed, "nominalVoltageLowBound", "lowClosed", MISSING_BOUND_ATTRIBUTE_MESSAGE)) {
            builder.setLowBound(context.nominalVoltageLowBound, context.lowClosed);
        }
        if (checkBoundData(context.nominalVoltageHighBound, context.highClosed, "nominalVoltageHighBound", "highClosed", MISSING_BOUND_ATTRIBUTE_MESSAGE)) {
            builder.setHighBound(context.nominalVoltageHighBound, context.highClosed);
        }
        return builder.build();
    }
}
