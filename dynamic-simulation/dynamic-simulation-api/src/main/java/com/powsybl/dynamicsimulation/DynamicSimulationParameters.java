/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationParameters extends AbstractExtendable<DynamicSimulationParameters> {

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

    public static final int DEFAULT_START_TIME = 0;
    public static final int DEFAULT_STOP_TIME = 1;

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

    private int startTime;

    private int stopTime;

    /**
     * Constructor with given parameters
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @param stopTime  instant of time at which the dynamic simulation ends, in
     *                  seconds
     */
    public DynamicSimulationParameters(int startTime, int stopTime) {
        if (startTime < 0) {
            throw new IllegalStateException("Start time should be zero or positive");
        }
        if (stopTime <= startTime) {
            throw new IllegalStateException("Stop time should be greater than start time");
        }
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public DynamicSimulationParameters() {
        this(DEFAULT_START_TIME, DEFAULT_STOP_TIME);
    }

    protected DynamicSimulationParameters(DynamicSimulationParameters other) {
        Objects.requireNonNull(other);
        startTime = other.startTime;
        stopTime = other.stopTime;
    }

    public int getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @return
     */
    public DynamicSimulationParameters setStartTime(int startTime) {
        if (startTime < 0) {
            throw new IllegalStateException("Start time should be zero or positive");
        }
        this.startTime = startTime;
        return this;
    }

    public int getStopTime() {
        return stopTime;
    }

    /**
     *
     * @param stopTime instant of time at which the dynamic simulation ends, in
     *                 seconds
     * @return
     */
    public DynamicSimulationParameters setStopTime(int stopTime) {
        if (stopTime <= startTime) {
            throw new IllegalStateException("Stop time should be greater than start time");
        }
        this.stopTime = stopTime;
        return this;
    }

    protected Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("startTime", startTime);
        map.put("stopTime", stopTime);
        return Collections.unmodifiableMap(map);
    }

    public DynamicSimulationParameters copy() {
        return new DynamicSimulationParameters(this);
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

    private void loadExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

}
