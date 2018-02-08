/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisParameters extends AbstractExtendable<SecurityAnalysisParameters> {

    public static SecurityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static SecurityAnalysisParameters load(PlatformConfig platformConfig) {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        return parameters;
    }
}
