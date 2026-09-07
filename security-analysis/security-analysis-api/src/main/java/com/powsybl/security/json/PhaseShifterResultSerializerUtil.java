/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.MovedPhaseShifterResult;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Riad BENRADI {@literal <riad.benradi_externe at rte-france.com>}
 */
public final class PhaseShifterResultSerializerUtil {

    private PhaseShifterResultSerializerUtil() {
        // utility class
    }

    public static List<MovedPhaseShifterResult> readPhaseShifterResults(
            JsonParser parser, DeserializationContext deserializationContext) {
        return JsonUtil.readList(deserializationContext, parser, MovedPhaseShifterResult.class);
    }

    public static void write(Map<String, MovedPhaseShifterResult> phaseShifterResults, JsonGenerator jsonGenerator) throws IOException {
        if (!phaseShifterResults.isEmpty()) {
            jsonGenerator.writeFieldName("phaseShifterResults");
            jsonGenerator.writeStartArray();
            for (var psr : phaseShifterResults.values().stream()
                    .sorted(Comparator.comparing(MovedPhaseShifterResult::transformerId)).toList()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("transformerId", psr.transformerId());
                jsonGenerator.writeNumberField("initialTap", psr.initialTap());
                jsonGenerator.writeNumberField("newTap", psr.newTap());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
    }
}
