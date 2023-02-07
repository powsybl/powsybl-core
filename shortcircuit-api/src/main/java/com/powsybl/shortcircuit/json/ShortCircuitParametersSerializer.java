/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.ShortCircuitParameters;

import java.io.IOException;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParametersSerializer extends StdSerializer<ShortCircuitParameters> {

    public ShortCircuitParametersSerializer() {
        super(ShortCircuitParameters.class);
    }

    @Override
    public void serialize(ShortCircuitParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", ShortCircuitParameters.VERSION);
        jsonGenerator.writeBooleanField("withLimitViolations", parameters.isWithLimitViolations());
        jsonGenerator.writeBooleanField("withVoltageResult", parameters.isWithVoltageResult());
        jsonGenerator.writeBooleanField("withFeederResult", parameters.isWithFeederResult());
        jsonGenerator.writeStringField("studyType", parameters.getStudyType().name());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "minVoltageDropProportionalThreshold", parameters.getMinVoltageDropProportionalThreshold());
        jsonGenerator.writeBooleanField("withFortescueResult", parameters.isWithFortescueResult());
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonShortCircuitParameters.getExtensionSerializers());
        jsonGenerator.writeEndObject();
    }

}
