/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.ShortCircuitParameters;

import java.io.IOException;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParametersSerializer extends StdSerializer<ShortCircuitParameters> {

    public ShortCircuitParametersSerializer() {
        super(ShortCircuitParameters.class);
    }

    @Override
    public void serialize(ShortCircuitParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", ShortCircuitParameters.VERSION);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withLimitViolations", parameters.isWithLimitViolations(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withVoltageMap", parameters.isWithVoltageMap(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withFeederResult", parameters.isWithFeederResult(), false);
        jsonGenerator.writeStringField("studyType", parameters.getStudyType().name());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "subTransStudyReactanceCoefficient", parameters.getSubTransStudyReactanceCoefficient());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "minVoltageDropProportionalThreshold", parameters.getMinVoltageDropProportionalThreshold());
        JsonUtil.writeOptionalStringField(jsonGenerator, "voltageMapType", parameters.getVoltageMapType() != null ? parameters.getVoltageMapType().name() : null);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "useResistances", parameters.isUseResistances(), true);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "useLoads", parameters.isUseLoads(), true);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "useCapacities", parameters.isUseCapacities(), true);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "useShunts", parameters.isUseShunts(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "useTapChangers", parameters.isUseTapChangers(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "useMutuals", parameters.isUseMutuals(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "modelVSC", parameters.isModelVSC(), false);
        JsonUtil.writeOptionalStringField(jsonGenerator, "startedGroupsInsideZone", parameters.getStartedGroupsInsideZone() != null ? parameters.getStartedGroupsInsideZone().name() : null);
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "startedGroupsInsideZoneThreshold", parameters.getStartedGroupsInsideZoneThreshold());
        JsonUtil.writeOptionalStringField(jsonGenerator, "startedGroupsOutOfZone", parameters.getStartedGroupsOutOfZone() != null ? parameters.getStartedGroupsOutOfZone().name() : null);
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "startedGroupsOutOfZoneThreshold", parameters.getStartedGroupsOutOfZoneThreshold());
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonShortCircuitParameters.getExtensionSerializers());
        jsonGenerator.writeEndObject();
    }

}
