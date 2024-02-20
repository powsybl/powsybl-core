/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationParameters extends AbstractDynamicSimulationParameters<DynamicSimulationParameters> {

    /**
     * A configuration loader interface for the DynamicSimulationParameters
     * extensions loaded from the platform configuration
     *
     * @param <E> The extension class
     */
    public interface ConfigLoader<E extends Extension<DynamicSimulationParameters>>
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
        load(parameters, platformConfig);
        parameters.loadExtensions(platformConfig);

        return parameters;
    }

    protected static void load(DynamicSimulationParameters parameters) {
        load(parameters, PlatformConfig.defaultConfig());
    }

    protected static void load(DynamicSimulationParameters parameters, PlatformConfig platformConfig) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(platformConfig);

        platformConfig.getOptionalModuleConfig("dynamic-simulation-default-parameters")
            .ifPresent(config -> {
                parameters.setStartTime(config.getIntProperty("startTime", DEFAULT_START_TIME));
                parameters.setStopTime(config.getIntProperty("stopTime", DEFAULT_STOP_TIME));
            });
    }

    /**
     * Constructor with given parameters
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @param stopTime  instant of time at which the dynamic simulation ends, in
     *                  seconds
     */
    public DynamicSimulationParameters(int startTime, int stopTime) {
        super(startTime, stopTime);
    }

    public DynamicSimulationParameters() {
        super(DEFAULT_START_TIME, DEFAULT_STOP_TIME);
    }

    protected DynamicSimulationParameters(DynamicSimulationParameters other) {
        super(other);
    }

    @Override
    protected Map<String, Object> toMap() {
        return ImmutableMap.of("startTime", getStartTime(),
            "stopTime", getStopTime());
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    public DynamicSimulationParameters copy() {
        return new DynamicSimulationParameters(this);
    }

    private void loadExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    @Override
    protected DynamicSimulationParameters self() {
        return this;
    }
}
