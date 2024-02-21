/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction.criterion.duration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.security.limitsreduction.criterion.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType;
import com.powsybl.security.limitsreduction.criterion.duration.LimitDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.duration.LimitDurationCriterion.LimitDurationType;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LimitDurationCriterionSerDeUtil {

    private LimitDurationCriterionSerDeUtil() {
    }

    public static void serializeCommonHeadAttributes(LimitDurationCriterion criterion, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("type", criterion.getType().name());
        jsonGenerator.writeStringField("version", LimitDurationCriterion.getVersion());
    }

    public static void serializeComparisonType(AbstractTemporaryDurationCriterion criterion, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("comparisonType", criterion.getComparisonType().name());
    }

    private static void readAndCheckTextValue(String expectedValue, JsonParser parser, String errorMessagePrefix) throws IOException {
        if (!expectedValue.equals(parser.nextTextValue())) {
            throw JsonMappingException.from(parser, errorMessagePrefix + expectedValue);
        }
    }

    public static void readAndCheckType(LimitDurationType expectedType, JsonParser parser) throws IOException {
        readAndCheckTextValue(expectedType.name(), parser, "Expected type ");
    }

    public static void readAndCheckComparisonType(TemporaryDurationCriterionType expectedType, JsonParser parser) throws IOException {
        readAndCheckTextValue(expectedType.name(), parser, "Expected comparison type ");
    }
}
