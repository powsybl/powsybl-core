/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.contingency.list.IdentifierContingencyList;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierContingencyListSerializer extends StdSerializer<IdentifierContingencyList> {

    public IdentifierContingencyListSerializer() {
        super(IdentifierContingencyList.class);
    }

    @Override
    public void serialize(IdentifierContingencyList identifierContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", identifierContingencyList.getType());
        jsonGenerator.writeStringField("version", ContingencyList.getVersion());
        jsonGenerator.writeStringField("name", identifierContingencyList.getName());
        jsonGenerator.writeStringField("identifiableType", identifierContingencyList.getIdentifiableType().toString());
        jsonGenerator.writeObjectField("identifiers", identifierContingencyList.getIdentifiants());
        jsonGenerator.writeEndObject();
    }
}
