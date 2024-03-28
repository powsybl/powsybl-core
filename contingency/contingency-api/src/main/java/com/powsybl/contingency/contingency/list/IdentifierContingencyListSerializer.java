/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.contingency.list;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierContingencyListSerializer extends StdSerializer<IdentifierContingencyList> {

    public IdentifierContingencyListSerializer() {
        super(IdentifierContingencyList.class);
    }

    @Override
    public void serialize(IdentifierContingencyList identifierContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", identifierContingencyList.getType());
        jsonGenerator.writeStringField("version", IdentifierContingencyList.getVersion());
        jsonGenerator.writeStringField("name", identifierContingencyList.getName());
        serializerProvider.defaultSerializeField("identifiers",
                identifierContingencyList.getIdentifiants(),
                jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
