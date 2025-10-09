/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.strategy.OperatorStrategyList;

import java.io.IOException;

import static com.powsybl.security.strategy.OperatorStrategyList.VERSION;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyListSerializer extends StdSerializer<OperatorStrategyList> {

    public OperatorStrategyListSerializer() {
        super(OperatorStrategyList.class);
    }

    @Override
    public void serialize(OperatorStrategyList operatorStrategyList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        serializerProvider.defaultSerializeField("operatorStrategies", operatorStrategyList.getOperatorStrategies(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
