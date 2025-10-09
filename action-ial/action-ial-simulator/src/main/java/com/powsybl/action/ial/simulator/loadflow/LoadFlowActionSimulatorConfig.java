/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class LoadFlowActionSimulatorConfig {

    public static LoadFlowActionSimulatorConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowActionSimulatorConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        Optional<ModuleConfig> config = platformConfig.getOptionalModuleConfig("load-flow-action-simulator");
        String loadFlowName = config.flatMap(c -> c.getOptionalStringProperty("load-flow-name")).orElse(null);
        int maxIterations = config.map(c -> c.getOptionalIntProperty("max-iterations").orElse(30)).orElse(30);
        boolean ignorePreContingencyViolations = config.flatMap(c -> c.getOptionalBooleanProperty("ignore-pre-contingency-violations")).orElse(false);
        boolean debug = config.flatMap(c -> c.getOptionalBooleanProperty("debug")).orElse(false);
        CopyStrategy copyStrategy = config.flatMap(c -> c.getOptionalEnumProperty("copy-strategy", CopyStrategy.class)).orElse(CopyStrategy.DEEP);
        return new LoadFlowActionSimulatorConfig(loadFlowName, maxIterations, ignorePreContingencyViolations, debug, copyStrategy);
    }

    private String loadFlowName;

    private int maxIterations;

    private boolean ignorePreContingencyViolations;

    private boolean debug;

    private CopyStrategy copyStrategy;

    public LoadFlowActionSimulatorConfig(String loadFlowName, int maxIterations, boolean ignorePreContingencyViolations,
                                         boolean debug) {
        this(loadFlowName, maxIterations, ignorePreContingencyViolations, debug, CopyStrategy.DEEP);
    }

    public LoadFlowActionSimulatorConfig(String loadFlowName, int maxIterations, boolean ignorePreContingencyViolations,
                                         boolean debug, CopyStrategy copyStrategy) {
        this.loadFlowName = loadFlowName;
        this.maxIterations = maxIterations;
        this.ignorePreContingencyViolations = ignorePreContingencyViolations;
        this.debug = debug;
        this.copyStrategy = Objects.requireNonNull(copyStrategy);
    }

    public Optional<String> getLoadFlowName() {
        return Optional.ofNullable(loadFlowName);
    }

    public void setLoadFlowName(String loadFlowName) {
        this.loadFlowName = loadFlowName;
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

    public CopyStrategy getCopyStrategy() {
        return copyStrategy;
    }

    public void setCopyStrategy(CopyStrategy copyStrategy) {
        this.copyStrategy = Objects.requireNonNull(copyStrategy);
    }
}
