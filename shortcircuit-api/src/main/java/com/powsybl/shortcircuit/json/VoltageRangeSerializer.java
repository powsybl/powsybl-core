/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.VoltageRange;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class VoltageRangeSerializer extends StdSerializer<VoltageRange> {

    VoltageRangeSerializer() {
        super(VoltageRange.class);
    }

    @Override
    public void serialize(VoltageRange voltageRange, JsonGenerator jsonGenerator, SerializationContext provider) throws JacksonException {
        Objects.requireNonNull(voltageRange);

        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "minimumNominalVoltage", voltageRange.getMinimumNominalVoltage());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "maximumNominalVoltage", voltageRange.getMaximumNominalVoltage());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "voltageRangeCoefficient", voltageRange.getRangeCoefficient());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "voltage", voltageRange.getVoltage());
        jsonGenerator.writeEndObject();

    }
}
