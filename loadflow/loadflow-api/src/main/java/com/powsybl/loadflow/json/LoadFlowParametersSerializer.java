/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.LoadFlowParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class LoadFlowParametersSerializer extends StdSerializer<LoadFlowParameters> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowParametersSerializer.class);

    LoadFlowParametersSerializer() {
        super(LoadFlowParameters.class);
    }

    @Override
    public void serialize(LoadFlowParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", LoadFlowParameters.VERSION);
        jsonGenerator.writeStringField("voltageInitMode", parameters.getVoltageInitMode().name());
        jsonGenerator.writeBooleanField("transformerVoltageControlOn", parameters.isTransformerVoltageControlOn());
        jsonGenerator.writeBooleanField("phaseShifterRegulationOn", parameters.isPhaseShifterRegulationOn());
        jsonGenerator.writeBooleanField("useReactiveLimits", parameters.isUseReactiveLimits());
        jsonGenerator.writeBooleanField("twtSplitShuntAdmittance", parameters.isTwtSplitShuntAdmittance());
        jsonGenerator.writeBooleanField("shuntCompensatorVoltageControlOn", parameters.isShuntCompensatorVoltageControlOn());
        jsonGenerator.writeBooleanField("readSlackBus", parameters.isReadSlackBus());
        jsonGenerator.writeBooleanField("writeSlackBus", parameters.isWriteSlackBus());
        jsonGenerator.writeBooleanField("dc", parameters.isDc());
        jsonGenerator.writeBooleanField("distributedSlack", parameters.isDistributedSlack());
        jsonGenerator.writeStringField("balanceType", parameters.getBalanceType().name());
        jsonGenerator.writeBooleanField("dcUseTransformerRatio", parameters.isDcUseTransformerRatio());
        jsonGenerator.writeArrayFieldStart("countriesToBalance");
        for (Country arg : parameters.getCountriesToBalance()) {
            jsonGenerator.writeString(arg.name());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeStringField("componentMode", parameters.getComponentMode().name());
        jsonGenerator.writeBooleanField("hvdcAcEmulation", parameters.isHvdcAcEmulation());
        jsonGenerator.writeNumberField("dcPowerFactor", parameters.getDcPowerFactor());
        JsonUtil.writeOptionalStringField(jsonGenerator, "debugDir", parameters.getDebugDir());

        Set<String> notSerialized = JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonLoadFlowParameters.getExtensionSerializers()::get);
        if (!notSerialized.isEmpty()) {
            LOGGER.warn("No JSON serializer found for load flow parameters extensions {}, they are not written", notSerialized);
        }

        jsonGenerator.writeEndObject();
    }
}
