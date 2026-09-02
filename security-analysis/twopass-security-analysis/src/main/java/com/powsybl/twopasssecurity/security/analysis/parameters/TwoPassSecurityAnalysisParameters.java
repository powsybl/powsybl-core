/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.twopasssecurity.security.analysis.parameters;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Map;
import java.util.Optional;

/**
 * This class contains configuration parameters specific to
 * two-pass security analysis.
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}
 */
public class TwoPassSecurityAnalysisParameters extends AbstractExtension<SecurityAnalysisParameters> {

    public static final String NAME = "twopass-security-analysis-parameters";

    /**
     * The name of the security analysis provider to use for the first pass.
     */
    private String firstProviderName;

    /**
     * The name of the security analysis provider to use for the second pass.
     */
    private String secondProviderName;

    public String getFirstProviderName() {
        return firstProviderName;
    }

    public TwoPassSecurityAnalysisParameters setFirstProviderName(String firstProviderName) {
        this.firstProviderName = firstProviderName;
        return this;
    }

    public String getSecondProviderName() {
        return secondProviderName;
    }

    public TwoPassSecurityAnalysisParameters setSecondProviderName(String secondProviderName) {
        this.secondProviderName = secondProviderName;
        return this;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static TwoPassSecurityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static TwoPassSecurityAnalysisParameters load(PlatformConfig platformConfig) {
        TwoPassSecurityAnalysisParameters parameters = new TwoPassSecurityAnalysisParameters();
        platformConfig.getOptionalModuleConfig(NAME).ifPresent(config ->
            parameters.setFirstProviderName(config.getStringProperty("firstProviderName"))
                      .setSecondProviderName(config.getStringProperty("secondProviderName")));

        return parameters;
    }

    public static TwoPassSecurityAnalysisParameters load(Map<String, String> properties) {
        return new TwoPassSecurityAnalysisParameters().update(properties);
    }

    public TwoPassSecurityAnalysisParameters update(Map<String, String> properties) {
        Optional.ofNullable(properties.get("firstProviderName")).ifPresent(this::setFirstProviderName);
        Optional.ofNullable(properties.get("secondProviderName")).ifPresent(this::setSecondProviderName);
        return this;
    }
}
