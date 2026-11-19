/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Objects;

/**
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class FaultResultSerializer extends StdSerializer<FaultResult> {

    public FaultResultSerializer() {
        super(FaultResult.class);
    }

    @Override
    public void serialize(FaultResult faultResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        Objects.requireNonNull(faultResult);
        Objects.requireNonNull(faultResult.getFault());

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("status", String.valueOf(faultResult.getStatus()));
        serializationContext.defaultSerializeProperty("fault", faultResult.getFault(), jsonGenerator);
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "shortCircuitPower", faultResult.getShortCircuitPower());
        if (faultResult.getTimeConstant() != null) {
            jsonGenerator.writeStringProperty("timeConstant", faultResult.getTimeConstant().toString());
        }
        if (faultResult instanceof FortescueFaultResult fortescueFaultResult) {
            fortescueResultSerialization(fortescueFaultResult, jsonGenerator, serializationContext);

        } else if (faultResult instanceof MagnitudeFaultResult) {
            magnitudeResultSerialization(faultResult, jsonGenerator, serializationContext);
        }
        if (!(faultResult.getLimitViolations()).isEmpty()) {
            serializationContext.defaultSerializeProperty("limitViolations", faultResult.getLimitViolations(), jsonGenerator);
        }
        if (!(faultResult.getShortCircuitBusResults()).isEmpty()) {
            serializationContext.defaultSerializeProperty("shortCircuitBusResults", faultResult.getShortCircuitBusResults(), jsonGenerator);
        }

        JsonUtil.writeExtensions(faultResult, jsonGenerator, serializationContext);

        jsonGenerator.writeEndObject();
    }

    private void fortescueResultSerialization(FortescueFaultResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        if (!((result.getFeederResults()).isEmpty())) {
            serializationContext.defaultSerializeProperty("feederResult", result.getFeederResults(), jsonGenerator);
        }
        if (result.getCurrent() != null) {
            serializationContext.defaultSerializeProperty("current", result.getCurrent(), jsonGenerator);
        }
        if (result.getVoltage() != null) {
            serializationContext.defaultSerializeProperty("voltage", result.getVoltage(), jsonGenerator);
        }
    }

    private void magnitudeResultSerialization(FaultResult faultResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        if (!((faultResult.getFeederResults()).isEmpty())) {
            serializationContext.defaultSerializeProperty("feederResult", faultResult.getFeederResults(), jsonGenerator);
        }
        if (!Double.isNaN(((MagnitudeFaultResult) faultResult).getCurrent())) {
            serializationContext.defaultSerializeProperty("currentMagnitude", ((MagnitudeFaultResult) faultResult).getCurrent(), jsonGenerator);
        }
        if (!Double.isNaN(((MagnitudeFaultResult) faultResult).getVoltage())) {
            serializationContext.defaultSerializeProperty("voltageMagnitude", ((MagnitudeFaultResult) faultResult).getVoltage(), jsonGenerator);
        }
    }

}
