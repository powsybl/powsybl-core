/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.sensitivity.SensitivityComputationParameters;

/**
 * Json module for sensitivity computation parameters
 *
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityComputationParametersJsonModule extends SimpleModule {

    public SensitivityComputationParametersJsonModule() {
        addDeserializer(SensitivityComputationParameters.class, new SensitivityComputationParametersDeserializer());
        addSerializer(SensitivityComputationParameters.class, new SensitivityComputationParametersSerializer());
    }
}
