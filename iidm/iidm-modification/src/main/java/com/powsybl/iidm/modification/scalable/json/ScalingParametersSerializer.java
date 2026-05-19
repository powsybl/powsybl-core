/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.powsybl.iidm.modification.scalable.ScalingParameters;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ScalingParametersSerializer extends StdSerializer<ScalingParameters> {

    ScalingParametersSerializer() {
        super(ScalingParameters.class);
    }

    @Override
    public void serialize(ScalingParameters scalingParameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("version", ScalingParameters.VERSION);
        jsonGenerator.writeStringProperty("scalingConvention", scalingParameters.getScalingConvention().name());
        jsonGenerator.writeBooleanProperty("constantPowerFactor", scalingParameters.isConstantPowerFactor());
        jsonGenerator.writeBooleanProperty("reconnect", scalingParameters.isReconnect());
        jsonGenerator.writeStringProperty("priority", scalingParameters.getPriority().name());

        jsonGenerator.writeArrayPropertyStart("ignoredInjectionIds");
        for (String id : scalingParameters.getIgnoredInjectionIds().stream().sorted().toList()) { //sorted alphabetically
            jsonGenerator.writeString(id);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
