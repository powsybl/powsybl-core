/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;

import java.io.IOException;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ContingencySerializer extends StdSerializer<Contingency> {

    public ContingencySerializer() {
        super(Contingency.class);
    }

    @Override
    public void serialize(Contingency contingency, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", contingency.getId());
        jsonGenerator.writeObjectField("elements", contingency.getElements());

        JsonUtil.writeExtensions(contingency, jsonGenerator, serializerProvider);

        jsonGenerator.writeEndObject();
    }
}
