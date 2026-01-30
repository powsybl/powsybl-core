/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.list;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierContingencyListSerializer extends StdSerializer<IdentifierContingencyList> {

    public IdentifierContingencyListSerializer() {
        super(IdentifierContingencyList.class);
    }

    @Override
    public void serialize(IdentifierContingencyList identifierContingencyList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", identifierContingencyList.getType());
        jsonGenerator.writeStringProperty("version", IdentifierContingencyList.getVersion());
        jsonGenerator.writeStringProperty("name", identifierContingencyList.getName());
        serializationContext.defaultSerializeProperty("identifiers",
                identifierContingencyList.getIdentifiants(),
                jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
