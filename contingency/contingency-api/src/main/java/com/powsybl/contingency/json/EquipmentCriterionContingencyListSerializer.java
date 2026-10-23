/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.contingency.list.AbstractEquipmentCriterionContingencyList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class EquipmentCriterionContingencyListSerializer<T extends AbstractEquipmentCriterionContingencyList> extends StdSerializer<T> {

    public EquipmentCriterionContingencyListSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(T criterionContingencyList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        CriterionContingencyListSerializer.serializeCommonHeadAttributes(criterionContingencyList, jsonGenerator);
        CriterionContingencyListSerializer.serializeCommonCriterionAttributes(criterionContingencyList, jsonGenerator, serializationContext);
        jsonGenerator.writeEndObject();
    }
}
