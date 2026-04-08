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
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "directMagnitude", value.getPositiveMagnitude());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "zeroMagnitude", value.getZeroMagnitude());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "inverseMagnitude", value.getNegativeMagnitude());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "directAngle", value.getPositiveAngle());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "zeroAngle", value.getZeroAngle());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "inverseAngle", value.getNegativeAngle());

        jsonGenerator.writeEndObject();
    }
}
