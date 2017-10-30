/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.LimitViolationsResult;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LimitViolationsResultSerializer extends StdSerializer<LimitViolationsResult> {

    public LimitViolationsResultSerializer() {
        super(LimitViolationsResult.class);
    }

    @Override
    public void serialize(LimitViolationsResult limitViolationsResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanField("computationOk", limitViolationsResult.isComputationOk());
        jsonGenerator.writeObjectField("limitViolations", limitViolationsResult.getLimitViolations());
        jsonGenerator.writeObjectField("actionsTaken", limitViolationsResult.getActionsTaken());
        jsonGenerator.writeEndObject();
    }
}
