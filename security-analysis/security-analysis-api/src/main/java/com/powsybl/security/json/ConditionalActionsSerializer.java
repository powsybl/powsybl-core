/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.strategy.ConditionalActions;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConditionalActionsSerializer extends StdSerializer<ConditionalActions> {

    public ConditionalActionsSerializer() {
        super(ConditionalActions.class);
    }

    @Override
    public void serialize(ConditionalActions operatorStrategyStage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", operatorStrategyStage.getId());
        serializerProvider.defaultSerializeField("condition", operatorStrategyStage.getCondition(), jsonGenerator);
        serializerProvider.defaultSerializeField("actionIds", operatorStrategyStage.getActionIds(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
