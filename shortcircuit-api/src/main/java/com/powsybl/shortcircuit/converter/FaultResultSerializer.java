/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

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
        jsonGenerator.writeObjectField("fault", faultResult.getFault());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "threePhaseFaultActivePower", faultResult.getThreePhaseFaultActivePower());
        if (faultResult.getTimeConstant() != null) {
            jsonGenerator.writeStringField("timeConstant", faultResult.getTimeConstant().toString());
        }
        if (!(faultResult.getFeederResults()).isEmpty()) {
            jsonGenerator.writeObjectField("feederResult", faultResult.getFeederResults());
        }
        if (!(faultResult.getLimitViolations()).isEmpty()) {
            jsonGenerator.writeObjectField("limitViolations", faultResult.getLimitViolations());
        }
        if (faultResult.getCurrent() != null) {
            jsonGenerator.writeObjectField("current", faultResult.getCurrent());
        }
        if (faultResult.getVoltage() != null) {
            jsonGenerator.writeObjectField("voltage", faultResult.getVoltage());
        }
        JsonUtil.writeExtensions(faultResult, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
