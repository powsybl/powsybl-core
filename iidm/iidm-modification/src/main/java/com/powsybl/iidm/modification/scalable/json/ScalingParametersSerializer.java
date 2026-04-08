/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.iidm.modification.scalable.ScalingParameters;

import java.io.IOException;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ScalingParametersSerializer extends StdSerializer<ScalingParameters> {

    ScalingParametersSerializer() {
        super(ScalingParameters.class);
    }

    @Override
    public void serialize(ScalingParameters scalingParameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", ScalingParameters.VERSION);
        jsonGenerator.writeStringField("scalingConvention", scalingParameters.getScalingConvention().name());
        jsonGenerator.writeBooleanField("constantPowerFactor", scalingParameters.isConstantPowerFactor());
        jsonGenerator.writeBooleanField("reconnect", scalingParameters.isReconnect());
        jsonGenerator.writeStringField("priority", scalingParameters.getPriority().name());

        jsonGenerator.writeArrayFieldStart("ignoredInjectionIds");
        for (String id : scalingParameters.getIgnoredInjectionIds().stream().sorted().toList()) { //sorted alphabetically
            jsonGenerator.writeString(id);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
