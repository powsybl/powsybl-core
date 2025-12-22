/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import static com.powsybl.shortcircuit.ShortCircuitAnalysisResult.VERSION;

/**
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ShortCircuitAnalysisResultSerializer extends StdSerializer<ShortCircuitAnalysisResult> {

    ShortCircuitAnalysisResultSerializer() {
        super(ShortCircuitAnalysisResult.class);
    }

    @Override
    public void serialize(ShortCircuitAnalysisResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("version", VERSION);
        serializationContext.defaultSerializeProperty("faultResults", result.getFaultResults(), jsonGenerator);

        JsonUtil.writeExtensions(result, jsonGenerator, serializationContext);

        jsonGenerator.writeEndObject();
    }
}
