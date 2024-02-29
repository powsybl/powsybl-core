/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.duration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion.LimitDurationType;

import java.io.IOException;
import java.util.Arrays;

/**
 * <p>Utility class for serialization/deserialization of {@link LimitDurationCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LimitDurationCriterionSerDeUtil {

    private LimitDurationCriterionSerDeUtil() {
    }

    public enum SerializationType {
        PERMANENT(LimitDurationType.PERMANENT, null),
        TEMPORARY_ALL(LimitDurationType.TEMPORARY, TemporaryDurationCriterionType.ALL),
        TEMPORARY_EQUALITY(LimitDurationType.TEMPORARY, TemporaryDurationCriterionType.EQUALITY),
        TEMPORARY_INTERVAL(LimitDurationType.TEMPORARY, TemporaryDurationCriterionType.INTERVAL);

        private final LimitDurationType type;

        private final TemporaryDurationCriterionType temporaryComparisonType;

        SerializationType(LimitDurationType type, TemporaryDurationCriterionType temporaryComparisonType) {
            this.type = type;
            this.temporaryComparisonType = temporaryComparisonType;
        }

        public static SerializationType getFor(LimitDurationType type, TemporaryDurationCriterionType temporaryComparisonType) {
            return Arrays.stream(values())
                    .filter(v -> v.type == type && v.temporaryComparisonType == temporaryComparisonType)
                    .findFirst()
                    .orElseThrow();
        }

        public static SerializationType getFor(LimitDurationCriterion criterion) {
            if (criterion.getType() == LimitDurationType.PERMANENT) {
                return PERMANENT;
            }
            return getFor(LimitDurationType.TEMPORARY, ((AbstractTemporaryDurationCriterion) criterion).getComparisonType());
        }
    }

    public static void serializeCommonHeadAttributes(LimitDurationCriterion criterion, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("type", SerializationType.getFor(criterion).name());
        jsonGenerator.writeStringField("version", LimitDurationCriterion.getVersion());
    }

    public static void readAndCheckType(LimitDurationType expectedType,
                                        TemporaryDurationCriterionType expectedComparisonType,
                                        JsonParser parser) throws IOException {
        String expectedValue = SerializationType.getFor(expectedType, expectedComparisonType).name();
        if (!expectedValue.equals(parser.nextTextValue())) {
            throw JsonMappingException.from(parser, "Expected type " + expectedValue);
        }
    }
}
