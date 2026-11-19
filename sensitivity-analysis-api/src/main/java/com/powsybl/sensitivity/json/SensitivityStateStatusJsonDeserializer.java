/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityStateStatusJsonDeserializer extends StdDeserializer<SensitivityAnalysisResult.SensitivityStateStatus> {

    public SensitivityStateStatusJsonDeserializer() {
        super(SensitivityAnalysisResult.SensitivityStateStatus.class);
    }

    @Override
    public SensitivityAnalysisResult.SensitivityStateStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        String version = JsonUtil.getSourceVersion(deserializationContext, SensitivityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE);
        return SensitivityAnalysisResult.SensitivityStateStatus.parseJson(jsonParser, version);
    }
}
