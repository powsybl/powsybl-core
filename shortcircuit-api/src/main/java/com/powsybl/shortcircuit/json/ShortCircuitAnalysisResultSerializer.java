/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import java.io.IOException;

import static com.powsybl.shortcircuit.ShortCircuitAnalysisResult.VERSION;

/**
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ShortCircuitAnalysisResultSerializer extends StdSerializer<ShortCircuitAnalysisResult> {

    ShortCircuitAnalysisResultSerializer() {
        super(ShortCircuitAnalysisResult.class);
    }

    @Override
    public void serialize(ShortCircuitAnalysisResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", VERSION);
        serializerProvider.defaultSerializeField("faultResults", result.getFaultResults(), jsonGenerator);

        JsonUtil.writeExtensions(result, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
