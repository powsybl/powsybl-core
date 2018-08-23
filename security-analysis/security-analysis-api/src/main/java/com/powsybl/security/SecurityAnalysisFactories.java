/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.ComponentDefaultConfig;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public final class SecurityAnalysisFactories {

    private SecurityAnalysisFactories() {
    }

    /**
     * Returns a factory as defined in the {@link ComponentDefaultConfig}.
     */
    public static SecurityAnalysisFactory newDefaultFactory() {
        return ComponentDefaultConfig.load().newFactoryImpl(SecurityAnalysisFactory.class);
    }

}
