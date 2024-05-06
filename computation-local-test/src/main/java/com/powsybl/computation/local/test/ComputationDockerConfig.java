/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation.local.test;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ComputationDockerConfig {

    private String dockerImageId;
    public static final String IMAGE = "image";
    public static final String MODULE = "computation-docker";

    public static ComputationDockerConfig load(PlatformConfig config) {
        Map<String, String> properties = new HashMap<>();
        ModuleConfig module = config.getOptionalModuleConfig(MODULE).orElseThrow(() ->
                new ConfigurationException("Module " + MODULE + " is missing in your configuration file"));
        module.getOptionalStringProperty(IMAGE).ifPresent(value -> properties.put(IMAGE, value));
        return load(properties);
    }

    public static ComputationDockerConfig load(Map<String, String> properties) {
        return new ComputationDockerConfig().update(properties);
    }

    public ComputationDockerConfig setDockerImageId(String dockerImageId) {
        this.dockerImageId = Objects.requireNonNull(dockerImageId);
        return this;
    }

    public ComputationDockerConfig update(Map<String, String> properties) {
        Objects.requireNonNull(properties);
        Optional.ofNullable(properties.get(IMAGE)).ifPresent(this::setDockerImageId);
        return this;
    }

    public String getDockerImageId() {
        return dockerImageId;
    }
}
