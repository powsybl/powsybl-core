/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.contingency.list.AbstractEquipmentCriterionContingencyList;
import com.powsybl.contingency.list.ContingencyList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public final class CriterionContingencyListSerializer {

    private CriterionContingencyListSerializer() {

    }

    public static void serializeCommonHeadAttributes(ContingencyList contingencyList, JsonGenerator jsonGenerator) throws JacksonException {
        jsonGenerator.writeStringProperty("type", contingencyList.getType());
        jsonGenerator.writeStringProperty("version", ContingencyList.getVersion());
        jsonGenerator.writeStringProperty("name", contingencyList.getName());
    }

    public static void serializeCommonCriterionAttributes(AbstractEquipmentCriterionContingencyList criterionContingencyList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        if (criterionContingencyList.getCountryCriterion() != null) {
            serializationContext.defaultSerializeProperty("countryCriterion",
                    criterionContingencyList.getCountryCriterion(),
                    jsonGenerator);
        }
        if (criterionContingencyList.getNominalVoltageCriterion() != null) {
            serializationContext.defaultSerializeProperty("nominalVoltageCriterion",
                    criterionContingencyList.getNominalVoltageCriterion(),
                    jsonGenerator);
        }
        if (!criterionContingencyList.getPropertyCriteria().isEmpty()) {
            serializationContext.defaultSerializeProperty("propertyCriteria",
                    criterionContingencyList.getPropertyCriteria(),
                    jsonGenerator);
        }
        if (criterionContingencyList.getRegexCriterion() != null) {
            serializationContext.defaultSerializeProperty("regexCriterion",
                    criterionContingencyList.getRegexCriterion(),
                    jsonGenerator);
        }
    }
}
