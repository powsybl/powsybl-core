/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConditionalActionsResultSerializer extends StdSerializer<OperatorStrategyResult.ConditionalActionsResult> {

    public ConditionalActionsResultSerializer() {
        super(OperatorStrategyResult.ConditionalActionsResult.class);
    }

    @Override
    public void serialize(OperatorStrategyResult.ConditionalActionsResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        serializationContext.defaultSerializeProperty("conditionalActionsId", result.getConditionalActionsId(), jsonGenerator);
        serializationContext.defaultSerializeProperty("status", result.getStatus(), jsonGenerator);
        serializationContext.defaultSerializeProperty("limitViolationsResult", result.getLimitViolationsResult(), jsonGenerator);
        serializationContext.defaultSerializeProperty("networkResult", result.getNetworkResult(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
