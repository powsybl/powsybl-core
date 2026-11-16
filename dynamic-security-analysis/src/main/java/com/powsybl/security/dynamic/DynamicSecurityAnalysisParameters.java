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
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import com.powsybl.security.dynamic.json.JsonDynamicSecurityAnalysisParameters;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Parameters for dynamic security analysis computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisParameters extends AbstractExtendable<DynamicSecurityAnalysisParameters> {

    // VERSION = 1.0 dynamicSimulationParameters, contingenciesParameters
    // VERSION = 1.1 debugDir
    public static final String VERSION = "1.1";

    private DynamicSimulationParameters dynamicSimulationParameters = new DynamicSimulationParameters();
    private ContingenciesParameters contingenciesParameters = new ContingenciesParameters();
    private String debugDir;

    public static class ContingenciesParameters {

        static final double DEFAULT_CONTINGENCIES_START_TIME = 5d;

        @JsonProperty("contingencies-start-time")
        private double contingenciesStartTime;

        public ContingenciesParameters() {
            this(DEFAULT_CONTINGENCIES_START_TIME);
        }

        public ContingenciesParameters(double contingenciesStartTime) {
            this.contingenciesStartTime = contingenciesStartTime;
        }

        /**
         * Defines when the contingencies start during the dynamic simulation.
         * Must be between {@link DynamicSimulationParameters#getStartTime()} and {@link DynamicSimulationParameters#getStopTime()} ()}
         * @return
         */
        public double getContingenciesStartTime() {
            return contingenciesStartTime;
        }

        public ContingenciesParameters setContingenciesStartTime(double contingenciesStartTime) {
            this.contingenciesStartTime = contingenciesStartTime;
            return this;
        }

        public void load(ModuleConfig config) {
            setContingenciesStartTime(config.getDoubleProperty("contingencies-start-time", DEFAULT_CONTINGENCIES_START_TIME));
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
                .ifPresent(config -> parameters.getDynamicContingenciesParameters().load(config));
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
        return this;
    }

    public ContingenciesParameters getDynamicContingenciesParameters() {
        return contingenciesParameters;
    }

    public DynamicSecurityAnalysisParameters setDynamicContingenciesParameters(ContingenciesParameters contingenciesParameters) {
        this.contingenciesParameters = contingenciesParameters;
        return this;
    }

    public String getDebugDir() {
        return debugDir;
    }

    public DynamicSecurityAnalysisParameters setDebugDir(String debugDir) {
        this.debugDir = debugDir;
        return this;
    }

    public void write(Path parametersPath) {
        JsonDynamicSecurityAnalysisParameters.write(this, parametersPath);
    }

    public void update(Path parametersPath) {
        JsonDynamicSecurityAnalysisParameters.update(this, parametersPath);
    }
}
