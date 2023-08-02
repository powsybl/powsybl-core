/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.shortcircuit.ConfiguredInitialVoltageProfileCoefficient;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ConfiguredInitialVoltageProfileCoefficientSerializer extends StdSerializer<ConfiguredInitialVoltageProfileCoefficient> {

    ConfiguredInitialVoltageProfileCoefficientSerializer() {
        super(ConfiguredInitialVoltageProfileCoefficient.class);
    }

    @Override
    public void serialize(ConfiguredInitialVoltageProfileCoefficient coefficient, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        Objects.requireNonNull(coefficient);

        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "minimumVoltage", coefficient.getMinimumVoltage());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "maximumVoltage", coefficient.getMaximumVoltage());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "voltageRangeCoefficient", coefficient.getRangeCoefficient());
        jsonGenerator.writeEndObject();

    }
}
