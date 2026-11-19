/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitscaling;

import com.powsybl.security.limitscaling.LimitScalingList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import static com.powsybl.security.limitscaling.LimitScalingList.VERSION;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitScalingListSerializer extends StdSerializer<LimitScalingList> {

    public LimitScalingListSerializer() {
        super(LimitScalingList.class);
    }

    @Override
    public void serialize(LimitScalingList limitScalingList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", VERSION);
        serializationContext.defaultSerializeProperty("limitScalings", limitScalingList.getLimitScalings(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
