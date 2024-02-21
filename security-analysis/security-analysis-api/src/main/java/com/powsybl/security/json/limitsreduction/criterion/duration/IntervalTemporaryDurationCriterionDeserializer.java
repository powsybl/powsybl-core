/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction.criterion.duration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.limitsreduction.criterion.duration.AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType;
import com.powsybl.security.limitsreduction.criterion.duration.IntervalTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.LimitDurationCriterion.LimitDurationType;

import java.io.IOException;

import static com.powsybl.security.json.limitsreduction.criterion.duration.LimitDurationCriterionSerDeUtil.readAndCheckComparisonType;
import static com.powsybl.security.json.limitsreduction.criterion.duration.LimitDurationCriterionSerDeUtil.readAndCheckType;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IntervalTemporaryDurationCriterionDeserializer extends StdDeserializer<IntervalTemporaryDurationCriterion> {

    public static final String MISSING_BOUND_ATTRIBUTE_MESSAGE = "Missing \"%s\" attribute for \"INTERVAL\" temporary limit duration criterion with non-null \"%s\" attribute.";

    public IntervalTemporaryDurationCriterionDeserializer() {
        super(IntervalTemporaryDurationCriterion.class);
    }

    private static class ParsingContext {
        Integer lowBound;
        Boolean lowClosed;
        Integer highBound;
        Boolean highClosed;
    }

    @Override
    public IntervalTemporaryDurationCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type" -> {
                    readAndCheckType(LimitDurationType.TEMPORARY, parser);
                    return true;
                }
                case "version" -> {
                    parser.nextTextValue();
                    return true;
                }
                case "comparisonType" -> {
                    readAndCheckComparisonType(TemporaryDurationCriterionType.INTERVAL, parser);
                    return true;
                }
                case "lowBound" -> {
                    parser.nextToken();
                    context.lowBound = parser.getValueAsInt();
                    return true;
                }
                case "lowClosed" -> {
                    parser.nextToken();
                    context.lowClosed = parser.getValueAsBoolean();
                    return true;
                }
                case "highBound" -> {
                    parser.nextToken();
                    context.highBound = parser.getValueAsInt();
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
        //TODO Introduce a builder to avoid having IntervalTemporaryDurationCriterion without bounds
        // (for now, this could be done manually)
        IntervalTemporaryDurationCriterion criterion = new IntervalTemporaryDurationCriterion();
        if (checkBoundData(context.lowBound, context.lowClosed, "lowBound", "lowClosed")) {
            criterion.setLowBound(context.lowBound, context.lowClosed);
        }
        if (checkBoundData(context.highBound, context.highClosed, "highBound", "highClosed")) {
            criterion.setHighBound(context.highBound, context.highClosed);
        }
        return criterion;
    }

    private boolean checkBoundData(Integer value, Boolean closed, String valueAttribute, String booleanAttribute) {
        if (value != null && closed == null) {
            throw new IllegalArgumentException(String.format(MISSING_BOUND_ATTRIBUTE_MESSAGE, booleanAttribute, valueAttribute));
        }
        if (value == null && closed != null) {
            throw new IllegalArgumentException(String.format(MISSING_BOUND_ATTRIBUTE_MESSAGE, valueAttribute, booleanAttribute));
        }
        return value != null;
    }
}
