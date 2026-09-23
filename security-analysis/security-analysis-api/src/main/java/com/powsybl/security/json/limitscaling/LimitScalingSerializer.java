/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitscaling;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.limitscaling.LimitScaling;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitScalingSerializer extends StdSerializer<LimitScaling> {

    public LimitScalingSerializer() {
        super(LimitScaling.class);
    }

    @Override
    public void serialize(LimitScaling limitScaling, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("value", limitScaling.getValue());
        jsonGenerator.writeStringField("limitType", limitScaling.getLimitType().name());
        jsonGenerator.writeBooleanField("monitoringOnly", limitScaling.isMonitoringOnly());

        serializerProvider.defaultSerializeField("contingencyContext",
                limitScaling.getContingencyContext(), jsonGenerator);
        if (!limitScaling.getNetworkElementCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("equipmentCriteria",
                    limitScaling.getNetworkElementCriteria(), jsonGenerator);
        }
        if (!limitScaling.getDurationCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("durationCriteria",
                    limitScaling.getDurationCriteria(), jsonGenerator);
        }
        if (!limitScaling.getOperationalLimitsGroupIdsSelection().isEmpty()) {
            serializerProvider.defaultSerializeField("operationalLimitsGroupIdsSelection",
                limitScaling.getOperationalLimitsGroupIdsSelection(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
