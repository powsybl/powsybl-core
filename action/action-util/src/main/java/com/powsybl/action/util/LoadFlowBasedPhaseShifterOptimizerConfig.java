/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.ConfigVersion;
import com.powsybl.loadflow.LoadFlowFactory;

import java.util.Objects;

import static com.powsybl.commons.config.ConfigVersion.DEFAULT_CONFIG_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowBasedPhaseShifterOptimizerConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "load-flow-based-phase-shifter-optimizer";

    public static LoadFlowBasedPhaseShifterOptimizerConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowBasedPhaseShifterOptimizerConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        ModuleConfig config = platformConfig.getModuleConfig(CONFIG_MODULE_NAME);
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("load-flow-factory", LoadFlowFactory.class);
        String version = config.getOptionalStringProperty("version").orElse(DEFAULT_CONFIG_VERSION);
        return new LoadFlowBasedPhaseShifterOptimizerConfig(new ConfigVersion(version), loadFlowFactoryClass);
    }

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);

    private Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    public LoadFlowBasedPhaseShifterOptimizerConfig(Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
    }

    public LoadFlowBasedPhaseShifterOptimizerConfig(ConfigVersion version, Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this(loadFlowFactoryClass);
        this.version = version;
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    public void setLoadFlowFactoryClass(Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
    }

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }
}
