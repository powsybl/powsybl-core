/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.loadflow.LoadFlowFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowActionSimulatorConfig {

    public static LoadFlowActionSimulatorConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowActionSimulatorConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        ModuleConfig config = platformConfig.getModuleConfig("load-flow-action-simulator");
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("load-flow-factory", LoadFlowFactory.class);
        int maxIterations = config.getIntProperty("max-iterations");
        boolean ignorePreContingencyViolations = config.getBooleanProperty("ignore-pre-contingency-violations", false);
        boolean debug = config.getBooleanProperty("debug", false);
        return new LoadFlowActionSimulatorConfig(loadFlowFactoryClass, maxIterations, ignorePreContingencyViolations, debug);
    }

    private Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    private int maxIterations;

    private boolean ignorePreContingencyViolations;

    private boolean debug;

    public LoadFlowActionSimulatorConfig(Class<? extends LoadFlowFactory> loadFlowFactoryClass, int maxIterations, boolean ignorePreContingencyViolations,
                                         boolean debug) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
        this.maxIterations = maxIterations;
        this.ignorePreContingencyViolations = ignorePreContingencyViolations;
        this.debug = debug;
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    public void setLoadFlowFactoryClass(Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public boolean isIgnorePreContingencyViolations() {
        return ignorePreContingencyViolations;
    }

    public void setIgnorePreContingencyViolations(boolean ignorePreContingencyViolations) {
        this.ignorePreContingencyViolations = ignorePreContingencyViolations;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
