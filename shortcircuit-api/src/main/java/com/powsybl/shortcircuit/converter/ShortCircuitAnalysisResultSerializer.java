/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import java.io.IOException;

/**
 * @author Teofil-Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ShortCircuitAnalysisResultSerializer extends StdSerializer<ShortCircuitAnalysisResult> {

    private static final String VERSION = "1.0";

    ShortCircuitAnalysisResultSerializer() {
        super(ShortCircuitAnalysisResult.class);
    }

    @Override
    public void serialize(ShortCircuitAnalysisResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", VERSION);
        if (result.getNetworkMetadata() != null) {
            jsonGenerator.writeObjectField("network", result.getNetworkMetadata());
        }
        jsonGenerator.writeObjectField("faultResults", result.getFaultResults());

        JsonUtil.writeExtensions(result, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
