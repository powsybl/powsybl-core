/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

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
import java.util.function.Supplier;

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

    // VERSION = 1.0 startTime, stopTime
    // VERSION = 1.1 debugDir
    public static final String VERSION = "1.1";

    public static final double DEFAULT_START_TIME = 0d;
    public static final double DEFAULT_STOP_TIME = 10d;
    public static final String DEFAULT_DEBUG_DIR = null;

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
                parameters.setStartTime(config.getDoubleProperty("startTime", DEFAULT_START_TIME));
                parameters.setStopTime(config.getDoubleProperty("stopTime", DEFAULT_STOP_TIME));
                parameters.setDebugDir(config.getStringProperty("debugDir", DEFAULT_DEBUG_DIR));
            });
    }

    private double startTime;

    private double stopTime;

    private String debugDir;

    /**
     * Constructor with given parameters
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @param stopTime  instant of time at which the dynamic simulation ends, in
     *                  seconds
     * @param debugDir the debug directory where execution files will be dumped
     */
    public DynamicSimulationParameters(double startTime, double stopTime, String debugDir) {
        if (startTime < 0) {
            throw new IllegalStateException("Start time should be zero or positive");
        }
        if (stopTime <= startTime) {
            throw new IllegalStateException("Stop time should be greater than start time");
        }
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.debugDir = debugDir;
    }

    public DynamicSimulationParameters(double startTime, double stopTime) {
        this(startTime, stopTime, DEFAULT_DEBUG_DIR);
    }

    public DynamicSimulationParameters() {
        this(DEFAULT_START_TIME, DEFAULT_STOP_TIME, DEFAULT_DEBUG_DIR);
    }

    protected DynamicSimulationParameters(DynamicSimulationParameters other) {
        Objects.requireNonNull(other);
        startTime = other.startTime;
        stopTime = other.stopTime;
        debugDir = other.debugDir;
    }

    public double getStartTime() {
        return startTime;
    }

    /**
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @return
     */
    public DynamicSimulationParameters setStartTime(double startTime) {
        if (startTime < 0) {
            throw new IllegalStateException("Start time should be zero or positive");
        }
        this.startTime = startTime;
        return this;
    }

    public double getStopTime() {
        return stopTime;
    }

    /**
     *
     * @param stopTime instant of time at which the dynamic simulation ends, in
     *                 seconds
     * @return
     */
    public DynamicSimulationParameters setStopTime(double stopTime) {
        if (stopTime <= startTime) {
            throw new IllegalStateException("Stop time should be greater than start time");
        }
        this.stopTime = stopTime;
        return this;
    }

    public String getDebugDir() {
        return debugDir;
    }

    /**
     *
     * @param debugDir the debug directory where execution files will be dumped
     * @return
     */
    public DynamicSimulationParameters setDebugDir(String debugDir) {
        this.debugDir = debugDir;
        return this;
    }

    protected Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("startTime", startTime);
        map.put("stopTime", stopTime);
        map.put("debugDir", debugDir);
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
