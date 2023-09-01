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
import com.powsybl.contingency.contingency.list.AbstractEquipmentCriterionContingencyList;
import com.powsybl.contingency.contingency.list.ContingencyList;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public final class CriterionContingencyListSerializer {

    private CriterionContingencyListSerializer() {

    }

    public static void serializeCommonHeadAttributes(ContingencyList contingencyList, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("type", contingencyList.getType());
        jsonGenerator.writeStringField("version", ContingencyList.getVersion());
        jsonGenerator.writeStringField("name", contingencyList.getName());
    }

    public static void serializeCommonCriterionAttributes(AbstractEquipmentCriterionContingencyList criterionContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (criterionContingencyList.getCountryCriterion() != null) {
            serializerProvider.defaultSerializeField("countryCriterion",
                    criterionContingencyList.getCountryCriterion(),
                    jsonGenerator);
        }
        if (criterionContingencyList.getNominalVoltageCriterion() != null) {
            serializerProvider.defaultSerializeField("nominalVoltageCriterion",
                    criterionContingencyList.getNominalVoltageCriterion(),
                    jsonGenerator);
        }
        if (!criterionContingencyList.getPropertyCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("propertyCriteria",
                    criterionContingencyList.getPropertyCriteria(),
                    jsonGenerator);
        }
        if (criterionContingencyList.getRegexCriterion() != null) {
            serializerProvider.defaultSerializeField("regexCriterion",
                    criterionContingencyList.getRegexCriterion(),
                    jsonGenerator);
        }
    }
}
