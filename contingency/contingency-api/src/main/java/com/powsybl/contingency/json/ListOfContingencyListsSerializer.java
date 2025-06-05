/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.ListOfContingencyLists;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ListOfContingencyListsSerializer extends StdSerializer<ListOfContingencyLists> {

    public ListOfContingencyListsSerializer() {
        super(ListOfContingencyLists.class);
    }

    @Override
    public void serialize(ListOfContingencyLists listOfContingencyLists, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", listOfContingencyLists.getType());
        jsonGenerator.writeStringField("version", ListOfContingencyLists.getVersion());
        jsonGenerator.writeStringField("name", listOfContingencyLists.getName());
        serializerProvider.defaultSerializeField("contingencyLists",
                listOfContingencyLists.getContingencyLists(),
                jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
