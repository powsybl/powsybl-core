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
import com.powsybl.contingency.contingency.list.AbstractEquipmentCriterionContingencyList;

import java.io.IOException;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class EquipmentCriterionContingencyListSerializer<T extends AbstractEquipmentCriterionContingencyList> extends StdSerializer<T> {

    public EquipmentCriterionContingencyListSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(T criterionContingencyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        CriterionContingencyListSerializer.serializeCommonHeadAttributes(criterionContingencyList, jsonGenerator);
        CriterionContingencyListSerializer.serializeCommonCriterionAttributes(criterionContingencyList, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
