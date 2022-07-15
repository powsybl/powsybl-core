/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.condition.*;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ConditionSerializer extends StdSerializer<Condition> {

    public ConditionSerializer() {
        super(Condition.class);
    }

    @Override
    public void serialize(Condition condition, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", condition.getType());
        switch (condition.getType()) {
            case AllViolationCondition.NAME:
                jsonGenerator.writeObjectField("violationIds", ((AllViolationCondition) condition).getViolationIds());
                break;
            case AtLeastOneViolationCondition.NAME:
                jsonGenerator.writeObjectField("violationIds", ((AtLeastOneViolationCondition) condition).getViolationIds());
                break;
            case TrueCondition.NAME:
            case AnyViolationCondition.NAME:
                break;
            default:
                throw new IllegalArgumentException("condition type \'" + condition.getType() + "\' does not exist");
        }
        jsonGenerator.writeEndObject();
    }
}
