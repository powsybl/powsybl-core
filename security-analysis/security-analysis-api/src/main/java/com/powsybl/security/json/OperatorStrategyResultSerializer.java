/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.results.OperatorStrategyResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyResultSerializer extends StdSerializer<OperatorStrategyResult> {

    public OperatorStrategyResultSerializer() {
        super(OperatorStrategyResult.class);
    }

    @Override
    public void serialize(OperatorStrategyResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        serializationContext.defaultSerializeProperty("operatorStrategy", result.getOperatorStrategy(), jsonGenerator);
        serializationContext.defaultSerializeProperty("conditionalActionsResults", result.getConditionalActionsResults(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
