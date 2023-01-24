/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import java.util.NoSuchElementException;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

public class AmplConfig {
    private final static String AMPL_CONFIG_NAME = "ampl";
    private final String amplHome;

    public AmplConfig(String amplHome) {
        this.amplHome = amplHome;
    }

    public static AmplConfig getConfig() {
        try {
            ModuleConfig amplModuleConfig = PlatformConfig.defaultConfig().getOptionalModuleConfig(AMPL_CONFIG_NAME)
                    .get();
            String ampl = amplModuleConfig.getStringProperty("homeDir");
            return new AmplConfig(ampl);
        } catch (NoSuchElementException rethrow) {
            throw new ConfigurationException("Module " + AMPL_CONFIG_NAME + " is missing in configuration");
        }
    }

    public String getAmplHome() {
        return amplHome;
    }

}
