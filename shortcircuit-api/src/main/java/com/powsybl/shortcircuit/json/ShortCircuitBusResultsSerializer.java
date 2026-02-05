/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FortescueShortCircuitBusResults;
import com.powsybl.shortcircuit.ShortCircuitBusResults;
import com.powsybl.shortcircuit.MagnitudeShortCircuitBusResults;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Objects;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class ShortCircuitBusResultsSerializer extends StdSerializer<ShortCircuitBusResults> {

    public ShortCircuitBusResultsSerializer() {
        super(ShortCircuitBusResults.class);
    }

    @Override
    public void serialize(ShortCircuitBusResults busResults, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        Objects.requireNonNull(busResults);

        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "voltageLevelId", busResults.getVoltageLevelId());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "busId", busResults.getBusId());
        if (!Double.isNaN(busResults.getInitialVoltageMagnitude())) {
            serializationContext.defaultSerializeProperty("initialVoltageMagnitude", busResults.getInitialVoltageMagnitude(), jsonGenerator);
        }
        if (busResults instanceof FortescueShortCircuitBusResults fortescueShortCircuitBusResults && fortescueShortCircuitBusResults.getVoltage() != null) {
            serializationContext.defaultSerializeProperty("voltage", fortescueShortCircuitBusResults.getVoltage(), jsonGenerator);
        }
        if (busResults instanceof MagnitudeShortCircuitBusResults magnitudeShortCircuitBusResults && !Double.isNaN(magnitudeShortCircuitBusResults.getVoltage())) {
            serializationContext.defaultSerializeProperty("voltageMagnitude", magnitudeShortCircuitBusResults.getVoltage(), jsonGenerator);
        }
        if (!Double.isNaN(busResults.getVoltageDropProportional())) {
            serializationContext.defaultSerializeProperty("voltageDropProportional", busResults.getVoltageDropProportional(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
