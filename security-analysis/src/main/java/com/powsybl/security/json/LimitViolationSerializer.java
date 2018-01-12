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
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolation;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LimitViolationSerializer extends StdSerializer<LimitViolation> {

    public LimitViolationSerializer() {
        super(LimitViolation.class);
    }

    @Override
    public void serialize(LimitViolation limitViolation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("subjectId", limitViolation.getSubjectId());
        jsonGenerator.writeStringField("limitType", limitViolation.getLimitType().name());
        JsonUtil.writeOptionalStringField(jsonGenerator, "limitName", limitViolation.getLimitName());
        JsonUtil.writeOptionalIntegerField(jsonGenerator, "acceptableDuration", limitViolation.getAcceptableDuration());
        jsonGenerator.writeNumberField("limit", limitViolation.getLimit());
        jsonGenerator.writeNumberField("limitReduction", limitViolation.getLimitReduction());
        jsonGenerator.writeNumberField("value", limitViolation.getValue());
        JsonUtil.writeOptionalEnumField(jsonGenerator, "side", limitViolation.getSide());

        JsonUtil.writeExtensions(limitViolation, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
