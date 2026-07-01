/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Optional;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ContingencySerializer extends StdSerializer<Contingency> {

    public ContingencySerializer() {
        super(Contingency.class);
    }

    @Override
    public void serialize(Contingency contingency, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("id", contingency.getId());
        Optional<String> contingencyName = contingency.getName();
        if (contingencyName.isPresent()) {
            jsonGenerator.writeStringProperty("name", contingencyName.get());
        }
        serializationContext.defaultSerializeProperty("elements", contingency.getElements(), jsonGenerator);

        JsonUtil.writeExtensions(contingency, jsonGenerator, serializationContext);

        jsonGenerator.writeEndObject();
    }
}
