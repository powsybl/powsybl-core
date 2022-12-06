/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.shortcircuit.SimpleShortCircuitBusResults;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.shortcircuit.SimpleShortCircuitBusResults.VERSION;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class SimpleShortCircuitBusResultsSerializer extends StdSerializer<SimpleShortCircuitBusResults> {

    public SimpleShortCircuitBusResultsSerializer() {
        super(SimpleShortCircuitBusResults.class);
    }

    @Override
    public void serialize(SimpleShortCircuitBusResults busResults, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Objects.requireNonNull(busResults);

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        JsonUtil.writeOptionalStringField(jsonGenerator, "voltageLevelId", busResults.getVoltageLevelId());
        JsonUtil.writeOptionalStringField(jsonGenerator, "busId", busResults.getBusId());
        if (!Double.isNaN(busResults.getVoltage())) {
            jsonGenerator.writeObjectField("voltage", busResults.getVoltage());
        }
        if (!Double.isNaN(busResults.getVoltageDropProportional())) {
            jsonGenerator.writeObjectField("voltageDropProportional", busResults.getVoltageDropProportional());
        }

        jsonGenerator.writeEndObject();
    }
}
