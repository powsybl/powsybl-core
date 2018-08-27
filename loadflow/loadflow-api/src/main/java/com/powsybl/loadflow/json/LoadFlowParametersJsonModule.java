/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.loadflow.LoadFlowParameters;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class LoadFlowParametersJsonModule extends SimpleModule {

    public LoadFlowParametersJsonModule() {
        addDeserializer(LoadFlowParameters.class, new LoadFlowParametersDeserializer());
        addSerializer(LoadFlowParameters.class, new LoadFlowParametersSerializer());
    }
}
