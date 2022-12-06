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
import com.powsybl.shortcircuit.*;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Teofil-Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class FaultResultSerializer extends StdSerializer<FaultResult> {

    public FaultResultSerializer() {
        super(FaultResult.class);
    }

    @Override
    public void serialize(FaultResult faultResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Objects.requireNonNull(faultResult);
        Objects.requireNonNull(faultResult.getFault());

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("status", String.valueOf(faultResult.getStatus()));
        serializerProvider.defaultSerializeField("fault", faultResult.getFault(), jsonGenerator);
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "shortCircuitPower", faultResult.getShortCircuitPower());
        if (faultResult.getTimeConstant() != null) {
            jsonGenerator.writeStringField("timeConstant", faultResult.getTimeConstant().toString());
        }
        if (!(faultResult.getFeederResults()).isEmpty()) {
            serializerProvider.defaultSerializeField("feederResult", faultResult.getFeederResults(), jsonGenerator);
        }
        if (!(faultResult.getLimitViolations()).isEmpty()) {
            serializerProvider.defaultSerializeField("limitViolations", faultResult.getLimitViolations(), jsonGenerator);
        }
        if (faultResult.getCurrent() != null) {
            serializerProvider.defaultSerializeField("current", faultResult.getCurrent(), jsonGenerator);
        }
        if (faultResult.getVoltage() != null) {
            serializerProvider.defaultSerializeField("voltage", faultResult.getVoltage(), jsonGenerator);
        }
        if (!(faultResult.getShortCircuitBusResults()).isEmpty()) {
            serializerProvider.defaultSerializeField("shortCircuitBusResults", faultResult.getShortCircuitBusResults(), jsonGenerator);
        }
        if (!(faultResult.getSimpleShortCircuitBusResults()).isEmpty()) {
            jsonGenerator.writeObjectField("simpleShortCircuitBusResults", faultResult.getSimpleShortCircuitBusResults());
        }
        JsonUtil.writeExtensions(faultResult, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
