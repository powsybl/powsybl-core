/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.StudyType;

import java.io.IOException;

import static com.powsybl.shortcircuit.FaultParameters.VERSION;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultParametersSerializer extends StdSerializer<FaultParameters> {

    public FaultParametersSerializer() {
        super(FaultParameters.class);
    }

    @Override
    public void serialize(FaultParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", VERSION);
        jsonGenerator.writeStringField("id", parameters.getId());
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withLimitViolations", parameters.isWithLimitViolations(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withVoltageResult", parameters.isWithVoltageResult(), false);
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withFeederResult", parameters.isWithFeederResult(), false);
        JsonUtil.writeOptionalStringField(jsonGenerator, "studyType", parameters.getStudyType() != null ? parameters.getStudyType().name() : null);
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "minVoltageDropProportionalThreshold", parameters.getMinVoltageDropProportionalThreshold());
        JsonUtil.writeOptionalBooleanField(jsonGenerator, "withFortescueResult", parameters.isWithFortescueResult(), false);
        if (parameters.getStudyType() == StudyType.SUB_TRANSIENT) {
            JsonUtil.writeOptionalDoubleField(jsonGenerator, "subTransientCoefficient", parameters.getSubTransientCoefficient());
        }
        jsonGenerator.writeEndObject();
    }
}
