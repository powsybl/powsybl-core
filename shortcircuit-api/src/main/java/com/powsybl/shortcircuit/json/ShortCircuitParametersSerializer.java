/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.InitialVoltageProfileMode;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import com.powsybl.shortcircuit.StudyType;

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
        jsonGenerator.writeBooleanField("withLimitViolations", parameters.isWithLimitViolations());
        jsonGenerator.writeBooleanField("withVoltageResult", parameters.isWithVoltageResult());
        jsonGenerator.writeBooleanField("withFeederResult", parameters.isWithFeederResult());
        jsonGenerator.writeStringField("studyType", parameters.getStudyType().name());
        if (parameters.getStudyType() == StudyType.SUB_TRANSIENT) {
            JsonUtil.writeOptionalDoubleField(jsonGenerator, "subTransientCoefficient", parameters.getSubTransientCoefficient());
        }
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "minVoltageDropProportionalThreshold", parameters.getMinVoltageDropProportionalThreshold());
        jsonGenerator.writeBooleanField("withFortescueResult", parameters.isWithFortescueResult());
        jsonGenerator.writeBooleanField("withLoads", parameters.isWithLoads());
        jsonGenerator.writeBooleanField("withShuntCompensators", parameters.isWithShuntCompensators());
        jsonGenerator.writeBooleanField("withVSCConverterStations", parameters.isWithVSCConverterStations());
        jsonGenerator.writeBooleanField("withNeutralPosition", parameters.isWithNeutralPosition());
        jsonGenerator.writeStringField("initialVoltageProfileMode", parameters.getInitialVoltageProfileMode().name());
        if (parameters.getInitialVoltageProfileMode() == InitialVoltageProfileMode.CONFIGURED) {
            serializerProvider.defaultSerializeField("voltageRanges", parameters.getVoltageRanges(), jsonGenerator);
        }
        jsonGenerator.writeBooleanField("detailedReport", parameters.isDetailedReport());
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonShortCircuitParameters.getExtensionSerializers()::get);
        jsonGenerator.writeEndObject();
    }

}
