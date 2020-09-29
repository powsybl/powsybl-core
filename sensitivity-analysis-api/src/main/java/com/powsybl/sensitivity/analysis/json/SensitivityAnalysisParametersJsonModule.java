/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.analysis.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.sensitivity.analysis.SensitivityAnalysisParameters;

/**
 * Json module for sensitivity analysis parameters
 *
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityAnalysisParametersJsonModule extends SimpleModule {

    public SensitivityAnalysisParametersJsonModule() {
        addDeserializer(SensitivityAnalysisParameters.class, new SensitivityAnalysisParametersDeserializer());
        addSerializer(SensitivityAnalysisParameters.class, new SensitivityAnalysisParametersSerializer());
    }
}
