/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.InitialVoltageProfileMode;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import com.powsybl.shortcircuit.StudyType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParametersSerializer extends StdSerializer<ShortCircuitParameters> {

    public ShortCircuitParametersSerializer() {
        super(ShortCircuitParameters.class);
    }

    @Override
    public void serialize(ShortCircuitParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", ShortCircuitParameters.VERSION);
        jsonGenerator.writeBooleanProperty("withLimitViolations", parameters.isWithLimitViolations());
        jsonGenerator.writeBooleanProperty("withVoltageResult", parameters.isWithVoltageResult());
        jsonGenerator.writeBooleanProperty("withFeederResult", parameters.isWithFeederResult());
        jsonGenerator.writeStringProperty("studyType", parameters.getStudyType().name());
        if (parameters.getStudyType() == StudyType.SUB_TRANSIENT) {
            JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "subTransientCoefficient", parameters.getSubTransientCoefficient());
        }
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "minVoltageDropProportionalThreshold", parameters.getMinVoltageDropProportionalThreshold());
        jsonGenerator.writeBooleanProperty("withFortescueResult", parameters.isWithFortescueResult());
        jsonGenerator.writeBooleanProperty("withLoads", parameters.isWithLoads());
        jsonGenerator.writeBooleanProperty("withShuntCompensators", parameters.isWithShuntCompensators());
        jsonGenerator.writeBooleanProperty("withVSCConverterStations", parameters.isWithVSCConverterStations());
        jsonGenerator.writeBooleanProperty("withNeutralPosition", parameters.isWithNeutralPosition());
        jsonGenerator.writeStringProperty("initialVoltageProfileMode", parameters.getInitialVoltageProfileMode().name());
        if (parameters.getInitialVoltageProfileMode() == InitialVoltageProfileMode.CONFIGURED) {
            serializationContext.defaultSerializeProperty("voltageRanges", parameters.getVoltageRanges(), jsonGenerator);
        }
        jsonGenerator.writeBooleanProperty("detailedReport", parameters.isDetailedReport());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "debugDir", parameters.getDebugDir());
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonShortCircuitParameters.getExtensionSerializers()::get);
        jsonGenerator.writeEndObject();
    }

}
