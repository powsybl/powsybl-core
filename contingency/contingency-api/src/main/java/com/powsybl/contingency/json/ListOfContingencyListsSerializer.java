/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.contingency.list.ListOfContingencyLists;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ListOfContingencyListsSerializer extends StdSerializer<ListOfContingencyLists> {

    public ListOfContingencyListsSerializer() {
        super(ListOfContingencyLists.class);
    }

    @Override
    public void serialize(ListOfContingencyLists listOfContingencyLists, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", listOfContingencyLists.getType());
        jsonGenerator.writeStringProperty("version", ListOfContingencyLists.getVersion());
        jsonGenerator.writeStringProperty("name", listOfContingencyLists.getName());
        serializationContext.defaultSerializeProperty("contingencyLists",
                listOfContingencyLists.getContingencyLists(),
                jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
