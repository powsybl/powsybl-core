/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * Configuration for a {@link SecurityAnalysis}. It might be loaded from a {@link PlatformConfig}.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisConfig {

    private static final String DEFAULT_PREPROCESSOR_NAME = null;

    private final String preprocessorName;

    public SecurityAnalysisConfig() {
        this(DEFAULT_PREPROCESSOR_NAME);
    }

    public SecurityAnalysisConfig(String preprocessorName) {
        this.preprocessorName = preprocessorName;
    }

    /**
     * The name of the security analysis input preprocessor to be run before the actual computation.
     * If {@literal empty}, default behaviour will be used.
     *
     * @return
     */
    public Optional<String> getPreprocessorName() {
        return Optional.ofNullable(preprocessorName);
    }

    /**
     * Loads the security analysis configuration defined in the default {@link PlatformConfig}.
     *
     * @return the security analysis config loaded from the default platform config.
     */
    public static SecurityAnalysisConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Loads the security analysis configuration defined in the specified {@link PlatformConfig}.
     *
     * @param platformConfig the platform configuration from which the security analysis config should be loaded.
     * @return the security analysis config loaded from the specified platform config.
     */
    public static SecurityAnalysisConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("security-analysis")
                .flatMap(module -> module.getOptionalStringProperty("preprocessor"))
                .map(SecurityAnalysisConfig::new)
                .orElseGet(SecurityAnalysisConfig::new);
    }
}
