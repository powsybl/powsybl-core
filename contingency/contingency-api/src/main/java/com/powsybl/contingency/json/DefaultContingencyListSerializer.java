/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency.json;

import com.powsybl.contingency.list.DefaultContingencyList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class DefaultContingencyListSerializer extends StdSerializer<DefaultContingencyList> {

    public DefaultContingencyListSerializer() {
        super(DefaultContingencyList.class);
    }

    @Override
    public void serialize(DefaultContingencyList contingencyList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        CriterionContingencyListSerializer.serializeCommonHeadAttributes(contingencyList, jsonGenerator);
        serializationContext.defaultSerializeProperty("contingencies", contingencyList.getContingencies(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
