/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ExternalSecurityAnalysisConfig {

    private static final boolean DEFAULT_DEBUG = false;
    private static final String DEFAULT_COMMAND = "itools";

    private final boolean debug;
    private final String itoolsCommand;

    public ExternalSecurityAnalysisConfig() {
        this(DEFAULT_DEBUG, DEFAULT_COMMAND);
    }

    public ExternalSecurityAnalysisConfig(boolean debug) {
        this(debug, DEFAULT_COMMAND);
    }

    public ExternalSecurityAnalysisConfig(boolean debug, String itoolsCommand) {
        this.debug = debug;
        Objects.requireNonNull(itoolsCommand);
        if (itoolsCommand.isEmpty()) {
            throw new ConfigurationException("itools command must not be empty.");
        }
        this.itoolsCommand = itoolsCommand;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getItoolsCommand() {
        return itoolsCommand;
    }

    public static ExternalSecurityAnalysisConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ExternalSecurityAnalysisConfig load(PlatformConfig platformConfig) {

        boolean debug = DEFAULT_DEBUG;
        String itoolsCommand = DEFAULT_COMMAND;

        ModuleConfig module = platformConfig.getModuleConfigIfExists("external-security-analysis-config");
        if (module != null) {
            debug = module.getBooleanProperty("debug", DEFAULT_DEBUG);
            itoolsCommand = module.getStringProperty("itools-command", DEFAULT_COMMAND);
        }
        return new ExternalSecurityAnalysisConfig(debug, itoolsCommand);
    }
}
