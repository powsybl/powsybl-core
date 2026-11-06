/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.LimitViolationsResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LimitViolationsResultSerializer extends StdSerializer<LimitViolationsResult> {

    public LimitViolationsResultSerializer() {
        super(LimitViolationsResult.class);
    }

    @Override
    public void serialize(LimitViolationsResult limitViolationsResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        serializationContext.defaultSerializeProperty("limitViolations", limitViolationsResult.getLimitViolations(), jsonGenerator);
        serializationContext.defaultSerializeProperty("actionsTaken", limitViolationsResult.getActionsTaken(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
