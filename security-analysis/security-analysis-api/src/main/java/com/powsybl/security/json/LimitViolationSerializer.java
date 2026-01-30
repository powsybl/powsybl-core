/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.ViolationLocation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Optional;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LimitViolationSerializer extends StdSerializer<LimitViolation> {

    public LimitViolationSerializer() {
        super(LimitViolation.class);
    }

    @Override
    public void serialize(LimitViolation limitViolation, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("subjectId", limitViolation.getSubjectId());
        Optional<ViolationLocation> violationLocation = limitViolation.getViolationLocation();
        if (violationLocation.isPresent()) {
            serializationContext.defaultSerializeProperty("violationLocation", violationLocation.get(), jsonGenerator);
        }
        if (limitViolation.getSubjectName() != null) {
            jsonGenerator.writeStringProperty("subjectName", limitViolation.getSubjectName());
        }
        jsonGenerator.writeStringProperty("limitType", limitViolation.getLimitType().name());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "limitName", limitViolation.getLimitName());
        JsonUtil.writeOptionalIntegerProperty(jsonGenerator, "acceptableDuration", limitViolation.getAcceptableDuration());
        jsonGenerator.writeNumberProperty("limit", limitViolation.getLimit());
        jsonGenerator.writeNumberProperty("limitReduction", limitViolation.getLimitReduction());
        jsonGenerator.writeNumberProperty("value", limitViolation.getValue());
        JsonUtil.writeOptionalEnumProperty(jsonGenerator, "side", limitViolation.getSide());

        JsonUtil.writeExtensions(limitViolation, jsonGenerator, serializationContext);

        jsonGenerator.writeEndObject();
    }
}
