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
import com.powsybl.shortcircuit.FortescueShortCircuitBusResults;
import com.powsybl.shortcircuit.ShortCircuitBusResults;
import com.powsybl.shortcircuit.MagnitudeShortCircuitBusResults;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class ShortCircuitBusResultsSerializer extends StdSerializer<ShortCircuitBusResults> {

    public ShortCircuitBusResultsSerializer() {
        super(ShortCircuitBusResults.class);
    }

    @Override
    public void serialize(ShortCircuitBusResults busResults, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Objects.requireNonNull(busResults);

        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalStringField(jsonGenerator, "voltageLevelId", busResults.getVoltageLevelId());
        JsonUtil.writeOptionalStringField(jsonGenerator, "busId", busResults.getBusId());
        if (!Double.isNaN(busResults.getInitialVoltageMagnitude())) {
            serializerProvider.defaultSerializeField("initialVoltageMagnitude", busResults.getInitialVoltageMagnitude(), jsonGenerator);
        }
        if (busResults instanceof FortescueShortCircuitBusResults fortescueShortCircuitBusResults && fortescueShortCircuitBusResults.getVoltage() != null) {
            serializerProvider.defaultSerializeField("voltage", fortescueShortCircuitBusResults.getVoltage(), jsonGenerator);
        }
        if (busResults instanceof MagnitudeShortCircuitBusResults magnitudeShortCircuitBusResults && !Double.isNaN(magnitudeShortCircuitBusResults.getVoltage())) {
            serializerProvider.defaultSerializeField("voltageMagnitude", magnitudeShortCircuitBusResults.getVoltage(), jsonGenerator);
        }
        if (!Double.isNaN(busResults.getVoltageDropProportional())) {
            serializerProvider.defaultSerializeField("voltageDropProportional", busResults.getVoltageDropProportional(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
