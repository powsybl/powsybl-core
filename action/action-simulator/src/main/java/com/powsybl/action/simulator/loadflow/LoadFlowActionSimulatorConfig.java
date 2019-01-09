/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.ConfigVersion;
import com.powsybl.loadflow.LoadFlowFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class LoadFlowActionSimulatorConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "load-flow-action-simulator";

    public static LoadFlowActionSimulatorConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowActionSimulatorConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        ModuleConfig config = platformConfig.getModuleConfig(CONFIG_MODULE_NAME);
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("load-flow-factory", LoadFlowFactory.class);
        int maxIterations = config.getIntProperty("max-iterations");
        boolean ignorePreContingencyViolations = config.getBooleanProperty("ignore-pre-contingency-violations", false);
        boolean debug = config.getBooleanProperty("debug", false);
        CopyStrategy copyStrategy = config.getEnumProperty("copy-strategy", CopyStrategy.class, CopyStrategy.DEEP);
        return config.getOptionalStringProperty("version")
                .map(v -> new LoadFlowActionSimulatorConfig(new ConfigVersion(v), loadFlowFactoryClass, maxIterations, ignorePreContingencyViolations, debug, copyStrategy))
                .orElseGet(() -> new LoadFlowActionSimulatorConfig(loadFlowFactoryClass, maxIterations, ignorePreContingencyViolations, debug, copyStrategy));
    }

    static final String DEFAULT_CONFIG_VERSION = "1.1";

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);

    private Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    private int maxIterations;

    private boolean ignorePreContingencyViolations;

    private boolean debug;

    private CopyStrategy copyStrategy;

    public LoadFlowActionSimulatorConfig(Class<? extends LoadFlowFactory> loadFlowFactoryClass, int maxIterations, boolean ignorePreContingencyViolations,
                                         boolean debug) {
        this(loadFlowFactoryClass, maxIterations, ignorePreContingencyViolations, debug, CopyStrategy.DEEP);
    }

    public LoadFlowActionSimulatorConfig(Class<? extends LoadFlowFactory> loadFlowFactoryClass, int maxIterations, boolean ignorePreContingencyViolations,
                                         boolean debug, CopyStrategy copyStrategy) {
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
        this.maxIterations = maxIterations;
        this.ignorePreContingencyViolations = ignorePreContingencyViolations;
        this.debug = debug;
        this.copyStrategy = Objects.requireNonNull(copyStrategy);
    }

    public LoadFlowActionSimulatorConfig(ConfigVersion version, Class<? extends LoadFlowFactory> loadFlowFactoryClass, int maxIterations,
                                         boolean ignorePreContingencyViolations, boolean debug, CopyStrategy copyStrategy) {
        this(loadFlowFactoryClass, maxIterations, ignorePreContingencyViolations, debug, copyStrategy);
        this.version = version;
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

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }

    public CopyStrategy getCopyStrategy() {
        return copyStrategy;
    }

    public void setCopyStrategy(CopyStrategy copyStrategy) {
        this.copyStrategy = Objects.requireNonNull(copyStrategy);
    }
}
