/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FeederResult;
import com.powsybl.shortcircuit.FortescueFeederResult;
import com.powsybl.shortcircuit.MagnitudeFeederResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class FeederResultSerializer extends StdSerializer<FeederResult> {

    public FeederResultSerializer() {
        super(FeederResult.class);
    }

    @Override
    public void serialize(FeederResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("connectableId", result.getConnectableId());
        if (result instanceof FortescueFeederResult fortescueFeederResult) {
            if (fortescueFeederResult.getCurrent() != null) {
                serializationContext.defaultSerializeProperty("current", fortescueFeederResult.getCurrent(), jsonGenerator);
            }
        } else {
            if (!Double.isNaN(((MagnitudeFeederResult) result).getCurrent())) {
                serializationContext.defaultSerializeProperty("currentMagnitude", ((MagnitudeFeederResult) result).getCurrent(), jsonGenerator);
            }
        }
        JsonUtil.writeOptionalEnumProperty(jsonGenerator, "side", result.getSide());
        jsonGenerator.writeEndObject();
    }
}
