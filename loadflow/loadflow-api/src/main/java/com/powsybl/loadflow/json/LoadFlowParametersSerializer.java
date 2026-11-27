/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.LoadFlowParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

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
    public void serialize(LoadFlowParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("version", LoadFlowParameters.VERSION);
        jsonGenerator.writeStringProperty("voltageInitMode", parameters.getVoltageInitMode().name());
        jsonGenerator.writeBooleanProperty("transformerVoltageControlOn", parameters.isTransformerVoltageControlOn());
        jsonGenerator.writeBooleanProperty("phaseShifterRegulationOn", parameters.isPhaseShifterRegulationOn());
        jsonGenerator.writeBooleanProperty("useReactiveLimits", parameters.isUseReactiveLimits());
        jsonGenerator.writeBooleanProperty("twtSplitShuntAdmittance", parameters.isTwtSplitShuntAdmittance());
        jsonGenerator.writeBooleanProperty("shuntCompensatorVoltageControlOn", parameters.isShuntCompensatorVoltageControlOn());
        jsonGenerator.writeBooleanProperty("readSlackBus", parameters.isReadSlackBus());
        jsonGenerator.writeBooleanProperty("writeSlackBus", parameters.isWriteSlackBus());
        jsonGenerator.writeBooleanProperty("dc", parameters.isDc());
        jsonGenerator.writeBooleanProperty("distributedSlack", parameters.isDistributedSlack());
        jsonGenerator.writeStringProperty("balanceType", parameters.getBalanceType().name());
        jsonGenerator.writeBooleanProperty("dcUseTransformerRatio", parameters.isDcUseTransformerRatio());
        jsonGenerator.writeArrayPropertyStart("countriesToBalance");
        for (Country arg : parameters.getCountriesToBalance()) {
            jsonGenerator.writeString(arg.name());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeStringProperty("componentMode", parameters.getComponentMode().name());
        jsonGenerator.writeBooleanProperty("hvdcAcEmulation", parameters.isHvdcAcEmulation());
        jsonGenerator.writeNumberProperty("dcPowerFactor", parameters.getDcPowerFactor());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "debugDir", parameters.getDebugDir());

        Set<String> notSerialized = JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonLoadFlowParameters.getExtensionSerializers()::get);
        if (!notSerialized.isEmpty()) {
            LOGGER.warn("No JSON serializer found for load flow parameters extensions {}, they are not written", notSerialized);
        }

        jsonGenerator.writeEndObject();
    }
}
