/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LoadFlowBasedPhaseShifterOptimizerConfig {

    public static LoadFlowBasedPhaseShifterOptimizerConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LoadFlowBasedPhaseShifterOptimizerConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        String loadFlowName = platformConfig.getOptionalModuleConfig("load-flow-based-phase-shifter-optimizer")
                .flatMap(moduleConfig -> moduleConfig.getOptionalStringProperty("load-flow-name"))
                .orElse(null);
        return new LoadFlowBasedPhaseShifterOptimizerConfig(loadFlowName);

    }

    private String loadFlowName;

    public LoadFlowBasedPhaseShifterOptimizerConfig(String loadFlowName) {
        this.loadFlowName = loadFlowName;
    }

    public Optional<String> getLoadFlowName() {
        return Optional.ofNullable(loadFlowName);
    }

    public void setLoadFlowName(String loadFlowName) {
        this.loadFlowName = loadFlowName;
    }
}
