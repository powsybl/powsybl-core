/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitreduction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.limitreduction.LimitReduction;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitReductionSerializer extends StdSerializer<LimitReduction> {

    public LimitReductionSerializer() {
        super(LimitReduction.class);
    }

    @Override
    public void serialize(LimitReduction limitReduction, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("value", limitReduction.getValue());
        jsonGenerator.writeStringField("limitType", limitReduction.getLimitType().name());
        jsonGenerator.writeBooleanField("monitoringOnly", limitReduction.isMonitoringOnly());

        serializerProvider.defaultSerializeField("contingencyContext",
                limitReduction.getContingencyContext(), jsonGenerator);
        if (!limitReduction.getNetworkElementCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("equipmentCriteria",
                    limitReduction.getNetworkElementCriteria(), jsonGenerator);
        }
        if (!limitReduction.getDurationCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("durationCriteria",
                    limitReduction.getDurationCriteria(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
