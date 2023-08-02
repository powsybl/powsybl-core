/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Parameters for security analysis computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain LECLERC <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisParameters extends AbstractSecurityAnalysisParameters<SecurityAnalysisParameters> {

    // VERSION = 1.0
    // VERSION = 1.1 IncreasedViolationsParameters adding.
    public static final String VERSION = "1.1";

    private LoadFlowParameters loadFlowParameters = new LoadFlowParameters();

    /**
     * Load parameters from platform default config.
     */
    public static SecurityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Load parameters from a provided platform config.
     */
    public static SecurityAnalysisParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();

        parameters.setLoadFlowParameters(LoadFlowParameters.load(platformConfig));
        platformConfig.getOptionalModuleConfig("security-analysis-default-parameters")
                .ifPresent(config -> parameters.getIncreasedViolationsParameters().load(config));
        parameters.readExtensions(platformConfig);
        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (SecurityAnalysisProvider provider : new ServiceLoaderCache<>(SecurityAnalysisProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(securityAnalysisParametersExtension ->
                    addExtension((Class) securityAnalysisParametersExtension.getClass(), securityAnalysisParametersExtension));
        }
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

    public SecurityAnalysisParameters setLoadFlowParameters(LoadFlowParameters loadFlowParameters) {
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
        return self();
    }

    protected SecurityAnalysisParameters self() {
        return this;
    }

    public void write(Path parametersPath) {
        JsonSecurityAnalysisParameters.write(this, parametersPath);
    }

    @Override
    public void update(Path parametersPath) {
        JsonSecurityAnalysisParameters.update(this, parametersPath);
    }
}
