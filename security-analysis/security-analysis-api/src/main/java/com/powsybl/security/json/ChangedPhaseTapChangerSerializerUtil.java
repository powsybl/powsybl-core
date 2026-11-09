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
import com.powsybl.security.results.ChangedPhaseTapChanger;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author Riad BENRADI {@literal <riad.benradi_externe at rte-france.com>}
 */
public final class ChangedPhaseTapChangerSerializerUtil {

    private ChangedPhaseTapChangerSerializerUtil() {
        // utility class
    }

    public static List<ChangedPhaseTapChanger> readChangedPhaseTapChangers(
            JsonParser parser, DeserializationContext deserializationContext) {
        return JsonUtil.readList(deserializationContext, parser, ChangedPhaseTapChanger.class);
    }

    public static void write(Collection<ChangedPhaseTapChanger> changedPhaseTapChangers, JsonGenerator jsonGenerator) throws IOException {
        if (!changedPhaseTapChangers.isEmpty()) {
            jsonGenerator.writeFieldName("changedPhaseTapChangers");
            jsonGenerator.writeStartArray();
            for (var psr : changedPhaseTapChangers.stream()
                    .sorted(Comparator.comparing(ChangedPhaseTapChanger::transformerId)).toList()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("transformerId", psr.transformerId());
                JsonUtil.writeOptionalEnum(jsonGenerator, "side", psr.getSide());
                jsonGenerator.writeNumberField("initialTap", psr.initialTap());
                jsonGenerator.writeNumberField("finalTap", psr.finalTap());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
    }
}
