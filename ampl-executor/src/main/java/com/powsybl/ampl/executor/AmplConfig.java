/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Optional;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public class AmplConfig {
    private static final String AMPL_CONFIG_NAME = "ampl";
    private final String amplHome;

    public AmplConfig(String amplHome) {
        this.amplHome = amplHome;
    }

    public static AmplConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static AmplConfig load(PlatformConfig platformConfig) {
        Optional<ModuleConfig> amplModuleConfigOpt = platformConfig.getOptionalModuleConfig(AMPL_CONFIG_NAME);
        if (amplModuleConfigOpt.isEmpty()) {
            throw new ConfigurationException("Module " + AMPL_CONFIG_NAME + " is missing in the configuration file");
        }
        ModuleConfig amplModuleConfig = amplModuleConfigOpt.get();
        String ampl = amplModuleConfig.getStringProperty("homeDir");
        return new AmplConfig(ampl);
    }

    public String getAmplHome() {
        return amplHome;
    }

}
