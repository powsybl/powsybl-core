/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationParameters extends AbstractExtendable<DynamicSimulationParameters> {

    /**
     * A configuration loader interface for the DynamicSimulationParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public static interface ConfigLoader<E extends Extension<DynamicSimulationParameters>>
        extends ExtensionConfigLoader<DynamicSimulationParameters, E> {
    }

    public static final String VERSION = "1.0";

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
        .memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "dynamic-simulation-parameters"));

    public static DynamicSimulationParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static DynamicSimulationParameters load(PlatformConfig platformConfig) {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        parameters.loadExtensions(platformConfig);

        return parameters;
    }

    private void loadExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

}
