/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.security.strategy.OperatorStrategy;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategySerializer extends StdSerializer<OperatorStrategy> {

    public OperatorStrategySerializer() {
        super(OperatorStrategy.class);
    }

    @Override
    public void serialize(OperatorStrategy operatorStrategy, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("id", operatorStrategy.getId());
        ContingencyContext contingencyContext = operatorStrategy.getContingencyContext();
        jsonGenerator.writeStringProperty("contingencyContextType", contingencyContext.getContextType().name());
        if (contingencyContext.getContingencyId() != null) {
            jsonGenerator.writeStringProperty("contingencyId", contingencyContext.getContingencyId());
        }
        serializationContext.defaultSerializeProperty("conditionalActions", operatorStrategy.getConditionalActions(), jsonGenerator);
        JsonUtil.writeExtensions(operatorStrategy, jsonGenerator, serializationContext);
        jsonGenerator.writeEndObject();
    }
}
