/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.loadflow.json.LoadFlowParametersJsonModule;
import com.powsybl.security.SecurityAnalysisParameters;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class SecurityAnalysisParametersJsonModule extends LoadFlowParametersJsonModule {

    public SecurityAnalysisParametersJsonModule() {
        super();
        addDeserializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersDeserializer());
        addSerializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersSerializer());
    }
}
