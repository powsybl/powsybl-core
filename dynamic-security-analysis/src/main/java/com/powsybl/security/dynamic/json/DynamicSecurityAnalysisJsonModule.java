/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.json.AbstractSecurityAnalysisJsonModule;
import com.powsybl.security.json.SecurityAnalysisJsonPlugin;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisJsonModule extends AbstractSecurityAnalysisJsonModule {

    public DynamicSecurityAnalysisJsonModule() {
        this(getServices());
    }

    public DynamicSecurityAnalysisJsonModule(Collection<SecurityAnalysisJsonPlugin> plugins) {
        super(plugins);
        addSerializer(DynamicSecurityAnalysisParameters.class, new DynamicSecurityAnalysisParametersSerializer());
        addDeserializer(DynamicSecurityAnalysisParameters.class, new DynamicSecurityAnalysisParametersDeserializer());
    }
}
