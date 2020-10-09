/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.loadflow.LoadFlowParameters;

import java.util.Objects;

/**
 * Parameters for security analysis computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain LECLERC <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisParameters extends AbstractExtendable<SecurityAnalysisParameters> {

    /**
     * A configuration loader interface for the SecurityAnalysisParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public static interface ConfigLoader<E extends Extension<SecurityAnalysisParameters>> extends ExtensionConfigLoader<SecurityAnalysisParameters, E> {
    }

    public static final String VERSION = "1.0";

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER =
        Suppliers.memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "security-analysis-parameters"));

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
        parameters.readExtensions(platformConfig);

        parameters.setLoadFlowParameters(LoadFlowParameters.load(platformConfig));

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

    public SecurityAnalysisParameters setLoadFlowParameters(LoadFlowParameters loadFlowParameters) {
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
        return this;
    }
}
