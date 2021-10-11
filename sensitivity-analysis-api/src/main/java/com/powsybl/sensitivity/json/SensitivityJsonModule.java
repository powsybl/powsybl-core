/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityValue;
import com.powsybl.sensitivity.SensitivityVariableSet;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityJsonModule extends SimpleModule {

    public SensitivityJsonModule() {
        addDeserializer(SensitivityAnalysisParameters.class, new SensitivityAnalysisParametersJsonDeserializer());
        addSerializer(SensitivityAnalysisParameters.class, new SensitivityAnalysisParametersJsonSerializer());
        addSerializer(SensitivityFactor.class, new SensitivityFactorJsonSerializer());
        addDeserializer(SensitivityFactor.class, new SensitivityFactorJsonDeserializer());
        addSerializer(SensitivityValue.class, new SensitivityValueJsonSerializer());
        addDeserializer(SensitivityValue.class, new SensitivityValueJsonDeserializer());
        addSerializer(SensitivityVariableSet.class, new SensitivityVariableSetJsonSerializer());
        addDeserializer(SensitivityVariableSet.class, new SensitivityVariableSetJsonDeserializer());
    }
}
