/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyScreening.security.analysis.parameters;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Map;
import java.util.Optional;

/**
 * This class contains configuration parameters specific to
 * contingency screening security analysis.
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>} */

public class ContingencyScreeningSecurityAnalysisParameters extends AbstractExtension<SecurityAnalysisParameters> {

    public static final String NAME = "contingency-screening-security-analysis-parameters";

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

    public void setFirstProviderName(String firstProviderName) {
        this.firstProviderName = firstProviderName;
    }

    public String getSecondProviderName() {
        return secondProviderName;
    }

    public void setSecondProviderName(String secondProviderName) {
        this.secondProviderName = secondProviderName;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static ContingencyScreeningSecurityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ContingencyScreeningSecurityAnalysisParameters load(PlatformConfig platformConfig) {
        ContingencyScreeningSecurityAnalysisParameters parameters = new ContingencyScreeningSecurityAnalysisParameters();
        platformConfig.getOptionalModuleConfig(NAME).ifPresent(config -> {
            parameters.setFirstProviderName(config.getStringProperty("firstProviderName"));
            parameters.setSecondProviderName(config.getStringProperty("secondProviderName"));
        });

        return parameters;
    }

    public static ContingencyScreeningSecurityAnalysisParameters load(Map<String, String> properties) {
        return new ContingencyScreeningSecurityAnalysisParameters().update(properties);
    }

    public ContingencyScreeningSecurityAnalysisParameters update(Map<String, String> properties) {
        Optional.ofNullable(properties.get("firstProviderName")).ifPresent(this::setFirstProviderName);
        Optional.ofNullable(properties.get("secondProviderName")).ifPresent(this::setSecondProviderName);
        return this;
    }
}
