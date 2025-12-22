/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.contingency.list.InjectionCriterionContingencyList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class InjectionCriterionContingencyListSerializer extends StdSerializer<InjectionCriterionContingencyList> {

    public InjectionCriterionContingencyListSerializer() {
        super(InjectionCriterionContingencyList.class);
    }

    @Override
    public void serialize(InjectionCriterionContingencyList criterionContingencyList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        CriterionContingencyListSerializer.serializeCommonHeadAttributes(criterionContingencyList, jsonGenerator);
        jsonGenerator.writeStringProperty("identifiableType", criterionContingencyList.getIdentifiableType().toString());
        CriterionContingencyListSerializer.serializeCommonCriterionAttributes(criterionContingencyList, jsonGenerator, serializationContext);
        jsonGenerator.writeEndObject();
    }
}
