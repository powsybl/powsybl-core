/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.strategy.ConditionalActions;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConditionalActionsSerializer extends StdSerializer<ConditionalActions> {

    public ConditionalActionsSerializer() {
        super(ConditionalActions.class);
    }

    @Override
    public void serialize(ConditionalActions operatorStrategyStage, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("id", operatorStrategyStage.getId());
        serializationContext.defaultSerializeProperty("condition", operatorStrategyStage.getCondition(), jsonGenerator);
        serializationContext.defaultSerializeProperty("actionIds", operatorStrategyStage.getActionIds(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
