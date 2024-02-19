/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.dynamicsimulation.AbstractDynamicSimulationParameters;

import java.util.Map;
import java.util.Objects;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationContingenciesParameters extends AbstractDynamicSimulationParameters<DynamicSimulationContingenciesParameters> {

    /**
     * A configuration loader interface for the DynamicSimulationParameters
     * extensions loaded from the platform configuration
     *
     * @param <E> The extension class
     */
    public interface ConfigLoader<E extends Extension<DynamicSimulationContingenciesParameters>>
            extends ExtensionConfigLoader<DynamicSimulationContingenciesParameters, E> {
    }

    public static final String VERSION = "1.0";
    static final int DEFAULT_CONTINGENCIES_START_TIME = 5;

    private int contingenciesStartTime;

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
            .memoize(() -> ExtensionProviders.createProvider(DynamicSimulationContingenciesParameters.ConfigLoader.class, "dynamic-simulation-contingencies-parameters"));

    public static DynamicSimulationContingenciesParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static DynamicSimulationContingenciesParameters load(PlatformConfig platformConfig) {
        DynamicSimulationContingenciesParameters parameters = new DynamicSimulationContingenciesParameters();
        load(parameters, platformConfig);
        parameters.loadExtensions(platformConfig);

        return parameters;
    }

    protected static void load(DynamicSimulationContingenciesParameters parameters) {
        load(parameters, PlatformConfig.defaultConfig());
    }

    protected static void load(DynamicSimulationContingenciesParameters parameters, PlatformConfig platformConfig) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(platformConfig);

        platformConfig.getOptionalModuleConfig("dynamic-simulation-contingencies-default-parameters")
                .ifPresent(config -> {
                    parameters.setStartTime(config.getIntProperty("startTime", DEFAULT_START_TIME));
                    parameters.setStopTime(config.getIntProperty("stopTime", DEFAULT_STOP_TIME));
                    parameters.setContingenciesStartTime(config.getIntProperty("contingenciesStartTime", DEFAULT_CONTINGENCIES_START_TIME));
                });
    }

    /**
     * Constructor with given parameters
     *
     * @param startTime instant of time at which the dynamic simulation begins, in
     *                  seconds
     * @param stopTime  instant of time at which the dynamic simulation ends, in
     *                  seconds
     * @param contingenciesStartTime  instant of time at which the contingencies begin, in
     *                  seconds
     */
    public DynamicSimulationContingenciesParameters(int startTime, int stopTime, int contingenciesStartTime) {
        super(startTime, stopTime);
        if (contingenciesStartTime < startTime || contingenciesStartTime > stopTime) {
            throw new IllegalStateException("Contingencies start time should be between simulation start and stop time");
        }
        this.contingenciesStartTime = contingenciesStartTime;
    }

    public DynamicSimulationContingenciesParameters() {
        this(DEFAULT_START_TIME, DEFAULT_STOP_TIME, DEFAULT_CONTINGENCIES_START_TIME);
    }

    protected DynamicSimulationContingenciesParameters(DynamicSimulationContingenciesParameters other) {
        super(other);
        contingenciesStartTime = other.contingenciesStartTime;
    }

    public int getContingenciesStartTime() {
        return contingenciesStartTime;
    }

    public DynamicSimulationContingenciesParameters setContingenciesStartTime(int contingenciesStartTime) {
        if (contingenciesStartTime < getStartTime() || contingenciesStartTime > getStopTime()) {
            throw new IllegalStateException("Contingencies start time should be between simulation start and stop time");
        }
        this.contingenciesStartTime = contingenciesStartTime;
        return self();
    }

    @Override
    public DynamicSimulationContingenciesParameters setStartTime(int startTime) {
        if (contingenciesStartTime < startTime) {
            throw new IllegalStateException("Start time should be lesser than contingencies start time");
        }
        return super.setStartTime(startTime);
    }

    @Override
    public DynamicSimulationContingenciesParameters setStopTime(int stopTime) {
        if (contingenciesStartTime > stopTime) {
            throw new IllegalStateException("Stop time should be greater than contingencies start time");
        }
        return super.setStopTime(stopTime);
    }

    @Override
    protected Map<String, Object> toMap() {
        return ImmutableMap.of("startTime", getStartTime(),
                "stopTime", getStopTime(),
                "contingenciesStartTime", contingenciesStartTime);
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    public DynamicSimulationContingenciesParameters copy() {
        return new DynamicSimulationContingenciesParameters(this);
    }

    private void loadExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    @Override
    protected DynamicSimulationContingenciesParameters self() {
        return this;
    }
}
