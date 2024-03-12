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
import com.powsybl.security.limitreduction.LimitReductionDefinition;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitReductionDefinitionSerializer extends StdSerializer<LimitReductionDefinition> {

    public LimitReductionDefinitionSerializer() {
        super(LimitReductionDefinition.class);
    }

    @Override
    public void serialize(LimitReductionDefinition limitReductionDefinition, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("limitReduction", limitReductionDefinition.getLimitReduction());
        jsonGenerator.writeStringField("limitType", limitReductionDefinition.getLimitType().name());
        jsonGenerator.writeBooleanField("monitoringOnly", limitReductionDefinition.isMonitoringOnly());

        if (!limitReductionDefinition.getContingencyContexts().isEmpty()) {
            serializerProvider.defaultSerializeField("contingencyContexts",
                    limitReductionDefinition.getContingencyContexts(), jsonGenerator);
        }
        if (!limitReductionDefinition.getNetworkElementCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("equipmentCriteria",
                    limitReductionDefinition.getNetworkElementCriteria(), jsonGenerator);
        }
        if (!limitReductionDefinition.getDurationCriteria().isEmpty()) {
            serializerProvider.defaultSerializeField("durationCriteria",
                    limitReductionDefinition.getDurationCriteria(), jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
