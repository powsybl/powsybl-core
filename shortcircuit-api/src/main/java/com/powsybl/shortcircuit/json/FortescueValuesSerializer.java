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
import com.powsybl.shortcircuit.FortescueValue;

import java.io.IOException;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class FortescueValuesSerializer extends StdSerializer<FortescueValue> {
    public FortescueValuesSerializer() {
        super(FortescueValue.class);
    }

    @Override
    public void serialize(FortescueValue value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        // Fortescue components.
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "directMagnitude", value.getPositiveMagnitude());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "zeroMagnitude", value.getZeroMagnitude());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "inverseMagnitude", value.getNegativeMagnitude());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "directAngle", value.getPositiveAngle());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "zeroAngle", value.getZeroAngle());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "inverseAngle", value.getNegativeAngle());

        jsonGenerator.writeEndObject();
    }
}
