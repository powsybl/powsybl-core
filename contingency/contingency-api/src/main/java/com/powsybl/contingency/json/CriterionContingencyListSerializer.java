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
import com.powsybl.contingency.CriterionContingencyList;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionContingencyListSerializer extends StdSerializer<CriterionContingencyList> {

    public CriterionContingencyListSerializer() {
        super(CriterionContingencyList.class);
    }

    @Override
    public void serialize(CriterionContingencyList criterionContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", criterionContingencyList.getType());
        jsonGenerator.writeStringField("version", CriterionContingencyList.getVersion());
        jsonGenerator.writeStringField("name", criterionContingencyList.getName());
        jsonGenerator.writeStringField("identifiableType", criterionContingencyList.getIdentifiableType().toString());
        jsonGenerator.writeObjectField("criteria", criterionContingencyList.getCriteria());
        jsonGenerator.writeEndObject();
    }
}
