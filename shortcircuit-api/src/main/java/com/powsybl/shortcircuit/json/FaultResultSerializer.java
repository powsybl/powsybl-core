/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.shortcircuit.*;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
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
        if (faultResult instanceof FortescueFaultResult fortescueFaultResult) {
            fortescueResultSerialization(fortescueFaultResult, jsonGenerator, serializerProvider);
        } else if (faultResult instanceof MagnitudeFaultResult) {
            magnitudeResultSerialization(faultResult, jsonGenerator, serializerProvider);
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

    private void fortescueResultSerialization(FortescueFaultResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (!((result.getFeederResults()).isEmpty())) {
            serializerProvider.defaultSerializeField("feederResult", result.getFeederResults(), jsonGenerator);
        }
        if (result.getCurrent() != null) {
            serializerProvider.defaultSerializeField("current", result.getCurrent(), jsonGenerator);
        }
        if (result.getVoltage() != null) {
            serializerProvider.defaultSerializeField("voltage", result.getVoltage(), jsonGenerator);
        }
        if (!Double.isNaN(result.getEquivalentR())) {
            serializerProvider.defaultSerializeField("equivalentR", result.getEquivalentR(), jsonGenerator);
        }
        if (!Double.isNaN(result.getEquivalentRZero())) {
            serializerProvider.defaultSerializeField("equivalentRZero", result.getEquivalentR(), jsonGenerator);
        }
        if (!Double.isNaN(result.getEquivalentX())) {
            serializerProvider.defaultSerializeField("equivalentX", result.getEquivalentX(), jsonGenerator);
        }
        if (!Double.isNaN(result.getEquivalentXZero())) {
            serializerProvider.defaultSerializeField("equivalentXZero", result.getEquivalentX(), jsonGenerator);
        }
    }

    private void magnitudeResultSerialization(FaultResult faultResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        MagnitudeFaultResult magnitudeFaultResult = (MagnitudeFaultResult) faultResult;
        if (!((faultResult.getFeederResults()).isEmpty())) {
            serializerProvider.defaultSerializeField("feederResult", faultResult.getFeederResults(), jsonGenerator);
        }
        if (!Double.isNaN(magnitudeFaultResult.getCurrent())) {
            serializerProvider.defaultSerializeField("currentMagnitude", magnitudeFaultResult.getCurrent(), jsonGenerator);
        }
        if (!Double.isNaN(magnitudeFaultResult.getVoltage())) {
            serializerProvider.defaultSerializeField("voltageMagnitude", magnitudeFaultResult.getVoltage(), jsonGenerator);
        }
        if (!Double.isNaN(magnitudeFaultResult.getEquivalentR())) {
            serializerProvider.defaultSerializeField("equivalentR", magnitudeFaultResult.getEquivalentR(), jsonGenerator);
        }
        if (!Double.isNaN(magnitudeFaultResult.getEquivalentX())) {
            serializerProvider.defaultSerializeField("equivalentX", magnitudeFaultResult.getEquivalentX(), jsonGenerator);
        }
    }

}
