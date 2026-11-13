/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitscaling;

import com.powsybl.security.limitscaling.LimitScaling;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitScalingSerializer extends StdSerializer<LimitScaling> {

    public LimitScalingSerializer() {
        super(LimitScaling.class);
    }

    @Override
    public void serialize(LimitScaling limitScaling, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("value", limitScaling.getValue());
        jsonGenerator.writeStringProperty("limitType", limitScaling.getLimitType().name());
        jsonGenerator.writeBooleanProperty("monitoringOnly", limitScaling.isMonitoringOnly());

        serializationContext.defaultSerializeProperty("contingencyContext",
                limitScaling.getContingencyContext(), jsonGenerator);
        if (!limitScaling.getNetworkElementCriteria().isEmpty()) {
            serializationContext.defaultSerializeProperty("equipmentCriteria",
                    limitScaling.getNetworkElementCriteria(), jsonGenerator);
        }
        if (!limitScaling.getDurationCriteria().isEmpty()) {
            serializationContext.defaultSerializeProperty("durationCriteria",
                    limitScaling.getDurationCriteria(), jsonGenerator);
        }
        if (!limitScaling.getOperationalLimitsGroupIdsSelection().isEmpty()) {
            serializationContext.defaultSerializeProperty("operationalLimitsGroupIdsSelection",
                limitScaling.getOperationalLimitsGroupIdsSelection(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
