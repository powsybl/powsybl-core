/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitreduction;

import com.powsybl.security.limitreduction.LimitReduction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitReductionSerializer extends StdSerializer<LimitReduction> {

    public LimitReductionSerializer() {
        super(LimitReduction.class);
    }

    @Override
    public void serialize(LimitReduction limitReduction, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("value", limitReduction.getValue());
        jsonGenerator.writeStringProperty("limitType", limitReduction.getLimitType().name());
        jsonGenerator.writeBooleanProperty("monitoringOnly", limitReduction.isMonitoringOnly());

        serializationContext.defaultSerializeProperty("contingencyContext",
                limitReduction.getContingencyContext(), jsonGenerator);
        if (!limitReduction.getNetworkElementCriteria().isEmpty()) {
            serializationContext.defaultSerializeProperty("equipmentCriteria",
                    limitReduction.getNetworkElementCriteria(), jsonGenerator);
        }
        if (!limitReduction.getDurationCriteria().isEmpty()) {
            serializationContext.defaultSerializeProperty("durationCriteria",
                    limitReduction.getDurationCriteria(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
