/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.ChangedPhaseTapChanger;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

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

    public static void write(Collection<ChangedPhaseTapChanger> changedPhaseTapChangers, JsonGenerator jsonGenerator) throws JacksonException {
        if (!changedPhaseTapChangers.isEmpty()) {
            jsonGenerator.writeName("changedPhaseTapChangers");
            jsonGenerator.writeStartArray();
            for (var psr : changedPhaseTapChangers.stream()
                    .sorted(Comparator.comparing(ChangedPhaseTapChanger::transformerId)).toList()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringProperty("transformerId", psr.transformerId());
                JsonUtil.writeOptionalEnum(jsonGenerator, "side", psr.getSide());
                jsonGenerator.writeNumberProperty("initialTap", psr.initialTap());
                jsonGenerator.writeNumberProperty("finalTap", psr.finalTap());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
    }
}
