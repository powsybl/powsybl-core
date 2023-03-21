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
import com.powsybl.contingency.contingency.list.InjectionCriterionContingencyList;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class InjectionCriterionContingencyListSerializer extends StdSerializer<InjectionCriterionContingencyList> {

    public InjectionCriterionContingencyListSerializer() {
        super(InjectionCriterionContingencyList.class);
    }

    @Override
    public void serialize(InjectionCriterionContingencyList criterionContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", criterionContingencyList.getType());
        jsonGenerator.writeStringField("version", ContingencyList.getVersion());
        jsonGenerator.writeStringField("name", criterionContingencyList.getName());
        jsonGenerator.writeStringField("identifiableType", criterionContingencyList.getIdentifiableType().toString());
        if (criterionContingencyList.getCountryCriterion() != null && !criterionContingencyList.getCountryCriterion().getCountries().isEmpty()) {
            serializerProvider.defaultSerializeField("countryCriterion",
                    criterionContingencyList.getCountryCriterion(),
                    jsonGenerator);
        }
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
