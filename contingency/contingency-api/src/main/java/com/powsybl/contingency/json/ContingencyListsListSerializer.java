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
import com.powsybl.contingency.contingency.list.ContingencyListsList;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ContingencyListsListSerializer extends StdSerializer<ContingencyListsList> {

    public ContingencyListsListSerializer() {
        super(ContingencyListsList.class);
    }

    @Override
    public void serialize(ContingencyListsList contingencyListsList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", contingencyListsList.getType());
        jsonGenerator.writeStringField("version", ContingencyListsList.getVersion());
        jsonGenerator.writeStringField("name", contingencyListsList.getName());
        jsonGenerator.writeObjectField("contingencyLists", contingencyListsList.getContingencyLists());
        jsonGenerator.writeEndObject();
    }
}
