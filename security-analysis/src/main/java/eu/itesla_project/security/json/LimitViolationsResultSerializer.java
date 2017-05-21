/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.itesla_project.security.LimitViolationFilter;
import eu.itesla_project.security.LimitViolationsResult;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LimitViolationsResultSerializer extends StdSerializer<LimitViolationsResult> {

    private final LimitViolationFilter filter;

    public LimitViolationsResultSerializer(LimitViolationFilter filter) {
        super(LimitViolationsResult.class);

        this.filter = filter;
    }

    @Override
    public void serialize(LimitViolationsResult limitViolationsResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanField("computationOk", limitViolationsResult.isComputationOk());
        jsonGenerator.writeObjectField("limitViolations", filter.apply(limitViolationsResult.getLimitViolations()));
        jsonGenerator.writeObjectField("actionsTaken", limitViolationsResult.getActionsTaken());
        jsonGenerator.writeEndObject();
    }
}
