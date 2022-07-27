/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.results.OperatorStrategyResult;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategyResultSerializer extends StdSerializer<OperatorStrategyResult> {

    public OperatorStrategyResultSerializer() {
        super(OperatorStrategyResult.class);
    }

    @Override
    public void serialize(OperatorStrategyResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("operatorStrategy", result.getOperatorStrategy());
        jsonGenerator.writeObjectField("limitViolationsResult", result.getLimitViolationsResult());
        jsonGenerator.writeObjectField("networkResult", result.getNetworkResult());
        jsonGenerator.writeEndObject();
    }
}
