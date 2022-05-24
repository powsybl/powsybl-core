/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.loadflow.LoadFlowParameters;

import java.util.Objects;

/**
 * Parameters for sensitivity analysis.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisParameters extends AbstractExtendable<SensitivityAnalysisParameters> {

    public static final String VERSION = "1.0";

    private LoadFlowParameters loadFlowParameters = new LoadFlowParameters();

    /**
     * Load parameters from platform default config.
     */
    public static SensitivityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Load parameters from a provided platform config.
     */
    public static SensitivityAnalysisParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.readExtensions(platformConfig);

        parameters.setLoadFlowParameters(LoadFlowParameters.load(platformConfig));

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (SensitivityAnalysisProvider provider : new ServiceLoaderCache<>(SensitivityAnalysisProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(sensitivityAnalysisParametersExtension ->
                    addExtension((Class) sensitivityAnalysisParametersExtension.getClass(), sensitivityAnalysisParametersExtension));
        }
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

    public SensitivityAnalysisParameters setLoadFlowParameters(LoadFlowParameters loadFlowParameters) {
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
        return this;
    }
}
