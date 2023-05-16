/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.contingency.list.TieLineCriterionContingencyList;

import java.io.IOException;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TieLineCriterionContingencyListSerializer extends StdSerializer<TieLineCriterionContingencyList> {

    public TieLineCriterionContingencyListSerializer() {
        super(TieLineCriterionContingencyList.class);
    }

    @Override
    public void serialize(TieLineCriterionContingencyList criterionContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", criterionContingencyList.getType());
        jsonGenerator.writeStringField("version", ContingencyList.getVersion());
        jsonGenerator.writeStringField("name", criterionContingencyList.getName());
        serializerProvider.defaultSerializeField("countryCriterion",
                criterionContingencyList.getCountryCriterion(),
                jsonGenerator);
        serializerProvider.defaultSerializeField("nominalVoltageCriterion",
                criterionContingencyList.getNominalVoltageCriterion(),
                jsonGenerator);
        serializerProvider.defaultSerializeField("propertyCriteria",
                criterionContingencyList.getPropertyCriteria(),
                jsonGenerator);
        serializerProvider.defaultSerializeField("regexCriterion",
                criterionContingencyList.getRegexCriterion(),
                jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
