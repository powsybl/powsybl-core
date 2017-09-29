/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.util;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.loadflow.api.LoadFlowFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowBasedPhaseShifterOptimizerConfig {

    public static LoadFlowBasedPhaseShifterOptimizerConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowBasedPhaseShifterOptimizerConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        ModuleConfig config = platformConfig.getModuleConfig("load-flow-based-phase-shifter-optimizer");
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("load-flow-factory", LoadFlowFactory.class);
        return new LoadFlowBasedPhaseShifterOptimizerConfig(loadFlowFactoryClass);

    }

    private Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    public LoadFlowBasedPhaseShifterOptimizerConfig(Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    public void setLoadFlowFactoryClass(Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
    }
}
