/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}0
 */
public class SecurityAnalysisJsonModule extends AbstractSecurityAnalysisJsonModule {

    public SecurityAnalysisJsonModule() {
        this(getServices());
    }

    public SecurityAnalysisJsonModule(Collection<SecurityAnalysisJsonPlugin> plugins) {
        super(plugins);
        addSerializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersSerializer());
        addDeserializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersDeserializer());
    }
}
