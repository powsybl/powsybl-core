/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.common.base.Suppliers;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic parameters for short circuit-computations.
 * May contain extensions for implementation-specific parameters.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParameters extends AbstractExtendable<ShortCircuitParameters> {

    private boolean subTransStudy = ShortCircuitConstants.SUBTRANS_STUDY;

    private boolean withFeederResult = ShortCircuitConstants.WITH_FEEDER_RESULT; //In case of systematic study only

    public interface ConfigLoader<E extends Extension<ShortCircuitParameters>>
            extends ExtensionConfigLoader<ShortCircuitParameters, E> {
    }

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
            .memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "short-circuit-parameters"));

    public static ShortCircuitParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ShortCircuitParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        ShortCircuitParameters parameters = new ShortCircuitParameters();
        parameters.readExtensions(platformConfig);

        ModuleConfig config = platformConfig.getOptionalModuleConfig("short-circuit-parameters").orElse(null);
        if (config != null) {
            parameters.setSubTransStudy(config.getBooleanProperty("subTransStudy", ShortCircuitConstants.SUBTRANS_STUDY));
            // TODO: add a condition on systematic study
            parameters.setWithFeederResult(config.getBooleanProperty("withFeederResult", ShortCircuitConstants.WITH_FEEDER_RESULT));

        }
        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    public boolean isSubTransStudy() {
        return subTransStudy;
    }

    public ShortCircuitParameters setSubTransStudy(boolean subTransStudy) {
        this.subTransStudy = subTransStudy;
        return this;
    }

    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    public ShortCircuitParameters setWithFeederResult(boolean withFeederResult) {
        this.withFeederResult = withFeederResult;
        return this;
    }

}
