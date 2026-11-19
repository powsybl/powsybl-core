/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.powsybl.sensitivity.SensitivityAnalysisResult;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityStateStatusJsonSerializer extends StdSerializer<SensitivityAnalysisResult.SensitivityStateStatus> {

    public SensitivityStateStatusJsonSerializer() {
        super(SensitivityAnalysisResult.SensitivityStateStatus.class);
    }

    @Override
    public void serialize(SensitivityAnalysisResult.SensitivityStateStatus value, JsonGenerator jsonGenerator, SerializationContext serializationContext) {
        SensitivityAnalysisResult.SensitivityStateStatus.writeJson(jsonGenerator, value);
    }
}
