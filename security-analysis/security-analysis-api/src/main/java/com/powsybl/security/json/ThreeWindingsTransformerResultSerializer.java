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
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.ThreeWindingsTransformerResult;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ThreeWindingsTransformerResultSerializer extends StdSerializer<ThreeWindingsTransformerResult> {

    public ThreeWindingsTransformerResultSerializer() {
        super(ThreeWindingsTransformerResult.class);
    }

    @Override
    public void serialize(ThreeWindingsTransformerResult transfoResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("threeWindingsTransformerId", transfoResult.getThreeWindingsTransformerId());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "p1", transfoResult.getP1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "q1", transfoResult.getQ1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "i1", transfoResult.getI1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "p2", transfoResult.getP2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "q2", transfoResult.getQ2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "i2", transfoResult.getI2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "p3", transfoResult.getP3());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "q3", transfoResult.getQ3());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "i3", transfoResult.getI3());
        JsonUtil.writeExtensions(transfoResult, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
