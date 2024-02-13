/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import com.powsybl.security.AbstractSecurityAnalysisParameters;
import com.powsybl.security.dynamic.json.JsonDynamicSecurityAnalysisParameters;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisParameters extends AbstractSecurityAnalysisParameters<DynamicSecurityAnalysisParameters> {

    public static final String VERSION = "1.0";

    private DynamicSimulationParameters dynamicSimulationParameters = new DynamicSimulationParameters();
    private DynamicContingenciesParameters dynamicContingenciesParameters = new DynamicContingenciesParameters();

    public static class DynamicContingenciesParameters {

        //TODO should be between start and stop time
        static final int DEFAULT_CONTINGENCIES_START_TIME = 0;

        @JsonProperty("contingencies-start-time")
        private int contingenciesStartTime = DEFAULT_CONTINGENCIES_START_TIME;

        public int getContingenciesStartTime() {
            return contingenciesStartTime;
        }

        public DynamicContingenciesParameters setContingenciesStartTime(int contingenciesStartTime) {
            this.contingenciesStartTime = contingenciesStartTime;
            return this;
        }

        public void load(ModuleConfig config) {
            setContingenciesStartTime(config.getIntProperty("contingencies-start-time", DEFAULT_CONTINGENCIES_START_TIME));
        }
    }

    /**
     * Load parameters from platform default config.
     */
    public static DynamicSecurityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Load parameters from a provided platform config.
     */
    public static DynamicSecurityAnalysisParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();

        parameters.setDynamicSimulationParameters(DynamicSimulationParameters.load(platformConfig));
        platformConfig.getOptionalModuleConfig("dynamic-security-analysis-default-parameters")
                .ifPresent(config -> {
                    parameters.getIncreasedViolationsParameters().load(config);
                    parameters.getDynamicContingenciesParameters().load(config);
                });
        parameters.readExtensions(platformConfig);
        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (DynamicSecurityAnalysisProvider provider : new ServiceLoaderCache<>(DynamicSecurityAnalysisProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(securityAnalysisParametersExtension ->
                    addExtension((Class) securityAnalysisParametersExtension.getClass(), securityAnalysisParametersExtension));
        }
    }

    public DynamicSimulationParameters getDynamicSimulationParameters() {
        return dynamicSimulationParameters;
    }

    public DynamicSecurityAnalysisParameters setDynamicSimulationParameters(DynamicSimulationParameters dynamicSimulationParameters) {
        this.dynamicSimulationParameters = dynamicSimulationParameters;
        return self();
    }

    public DynamicContingenciesParameters getDynamicContingenciesParameters() {
        return dynamicContingenciesParameters;
    }

    public DynamicSecurityAnalysisParameters setDynamicContingenciesParameters(DynamicContingenciesParameters dynamicContingenciesParameters) {
        this.dynamicContingenciesParameters = dynamicContingenciesParameters;
        return self();
    }

    protected DynamicSecurityAnalysisParameters self() {
        return this;
    }

    @Override
    public void write(Path parametersPath) {
        JsonDynamicSecurityAnalysisParameters.write(this, parametersPath);
    }

    @Override
    public void update(Path parametersPath) {
        JsonDynamicSecurityAnalysisParameters.update(this, parametersPath);
    }
}
