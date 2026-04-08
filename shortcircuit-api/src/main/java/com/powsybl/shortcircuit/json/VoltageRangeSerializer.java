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
import com.powsybl.shortcircuit.VoltageRange;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class VoltageRangeSerializer extends StdSerializer<VoltageRange> {

    VoltageRangeSerializer() {
        super(VoltageRange.class);
    }

    @Override
    public void serialize(VoltageRange voltageRange, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        Objects.requireNonNull(voltageRange);

        jsonGenerator.writeStartObject();
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "minimumNominalVoltage", voltageRange.getMinimumNominalVoltage());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "maximumNominalVoltage", voltageRange.getMaximumNominalVoltage());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "voltageRangeCoefficient", voltageRange.getRangeCoefficient());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "voltage", voltageRange.getVoltage());
        jsonGenerator.writeEndObject();

    }
}
