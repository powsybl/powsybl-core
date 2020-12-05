/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.DefaultContingencyList;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DefaultContingencyListSerializer extends StdSerializer<DefaultContingencyList> {

    public DefaultContingencyListSerializer() {
        super(DefaultContingencyList.class);
    }

    @Override
    public void serialize(DefaultContingencyList contingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", DefaultContingencyList.VERSION);
        jsonGenerator.writeStringField("name", contingencyList.getName());
        jsonGenerator.writeObjectField("contingencies", contingencyList.getContingencies());
        jsonGenerator.writeEndObject();
    }
}
