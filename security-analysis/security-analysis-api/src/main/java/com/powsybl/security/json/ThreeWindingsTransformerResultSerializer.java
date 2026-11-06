/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ThreeWindingsTransformerResultSerializer extends StdSerializer<ThreeWindingsTransformerResult> {

    public ThreeWindingsTransformerResultSerializer() {
        super(ThreeWindingsTransformerResult.class);
    }

    @Override
    public void serialize(ThreeWindingsTransformerResult transfoResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("threeWindingsTransformerId", transfoResult.getThreeWindingsTransformerId());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "p1", transfoResult.getP1());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "q1", transfoResult.getQ1());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "i1", transfoResult.getI1());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "p2", transfoResult.getP2());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "q2", transfoResult.getQ2());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "i2", transfoResult.getI2());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "p3", transfoResult.getP3());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "q3", transfoResult.getQ3());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "i3", transfoResult.getI3());
        JsonUtil.writeExtensions(transfoResult, jsonGenerator, serializationContext);
        jsonGenerator.writeEndObject();
    }
}
