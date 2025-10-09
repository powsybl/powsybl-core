/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.sensitivity.SensitivityAnalysisResult;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityContingencyStatusJsonDeserializer extends StdDeserializer<SensitivityAnalysisResult.SensitivityContingencyStatus> {

    public SensitivityContingencyStatusJsonDeserializer() {
        super(SensitivityAnalysisResult.SensitivityContingencyStatus.class);
    }

    @Override
    public SensitivityAnalysisResult.SensitivityContingencyStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        return SensitivityAnalysisResult.SensitivityContingencyStatus.parseJson(jsonParser);
    }
}
