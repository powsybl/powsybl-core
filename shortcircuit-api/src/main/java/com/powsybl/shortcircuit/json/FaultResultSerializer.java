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
        if (faultResult instanceof FortescueFaultResult) {
            fortescueResultSerialization((FortescueFaultResult) faultResult, jsonGenerator);

        } else if (faultResult instanceof MagnitudeFaultResult) {
            magnitudeResultSerialization(faultResult, jsonGenerator);
        }
        if (!(faultResult.getLimitViolations()).isEmpty()) {
            serializerProvider.defaultSerializeField("limitViolations", faultResult.getLimitViolations(), jsonGenerator);
        }
        if (!(faultResult.getShortCircuitBusResults()).isEmpty()) {
            serializerProvider.defaultSerializeField("shortCircuitBusResults", faultResult.getShortCircuitBusResults(), jsonGenerator);
        }

        JsonUtil.writeExtensions(faultResult, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }

    private void fortescueResultSerialization(FortescueFaultResult result, JsonGenerator jsonGenerator) throws IOException {
        if (!((result.getFeederResults()).isEmpty())) {
            serializeProvider.defaultSerializeField("feederResult", result.getFeederResults(), jsonGenerator);
        }
        if (result.getCurrent() != null) {
            serializeProvider.defaultSerializeField("current", result.getCurrent(), jsonGenerator);
        }
        if (result.getVoltage() != null) {
            serializeProvider.defaultSerializeField("voltage", result.getVoltage(),jsonGenerator);
        }
    }

    private void magnitudeResultSerialization(FaultResult faultResult, JsonGenerator jsonGenerator) throws IOException {
        if (!((faultResult.getFeederResults()).isEmpty())) {
            serializeProvider.defaultSerializeField("feederResult", faultResult.getFeederResults(), jsonGenerator);
        }
        if (!Double.isNaN(((MagnitudeFaultResult) faultResult).getCurrent())) {
            serializeProvider.defaultSerializeField("currentMagnitude", ((MagnitudeFaultResult) faultResult).getCurrent(), jsonGenerator);
        }
        if (!Double.isNaN(((MagnitudeFaultResult) faultResult).getVoltage())) {
            serializeProvider.defaultSerializeField("voltageMagnitude", ((MagnitudeFaultResult) faultResult).getVoltage(), jsonGenerator);
        }
    }

}
