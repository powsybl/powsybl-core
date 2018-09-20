/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.VersionConfig;
import com.powsybl.commons.extensions.*;

import java.util.Map;
import java.util.Objects;

/**
 * Parameters for loadflow computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowParameters extends AbstractExtendable<LoadFlowParameters> {

    /**
     * A configuration loader interface for the LoadFlowParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ConfigLoader<E extends Extension<LoadFlowParameters>> extends ExtensionConfigLoader<LoadFlowParameters, E> {
    }

    public enum VoltageInitMode {
        UNIFORM_VALUES, // v=1pu, theta=0
        PREVIOUS_VALUES,
        DC_VALUES // preprocessing to compute DC angles
    }

    public static final VersionConfig DEFAULT_VERSION = VersionConfig.LATEST_VERSION;
    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_NO_GENERATOR_REACTIVE_LIMITS = false;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_SPECIFIC_COMPATIBILITY = false;

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "loadflow-parameters"));

    /**
     * Loads parameters from the default platform configuration.
     */
    public static LoadFlowParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Load parameters from a provided platform configuration.
     */
    public static LoadFlowParameters load(PlatformConfig platformConfig) {
        LoadFlowParameters parameters = new LoadFlowParameters();
        load(parameters, platformConfig);

        parameters.loadExtensions(platformConfig);

        return parameters;
    }

    protected static void load(LoadFlowParameters parameters) {
        load(parameters, PlatformConfig.defaultConfig());
    }

    protected static void load(LoadFlowParameters parameters, PlatformConfig platformConfig) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(platformConfig);

        ModuleConfig config = platformConfig.getModuleConfigIfExists("load-flow-default-parameters");
        if (config != null) {
            parameters.setVersion(config.hasProperty("version") ? VersionConfig.valueOfByString(config.getStringProperty("version")) : platformConfig.getVersion());
            switch (parameters.getVersion()) {
                case VERSION_1_0:
                    parameters.setVoltageInitMode(config.getEnumProperty("voltageInitMode", VoltageInitMode.class, DEFAULT_VOLTAGE_INIT_MODE));
                    parameters.setTransformerVoltageControlOn(config.getBooleanProperty("transformerVoltageControlOn", DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON));
                    parameters.setNoGeneratorReactiveLimits(config.getBooleanProperty("noGeneratorReactiveLimits", DEFAULT_NO_GENERATOR_REACTIVE_LIMITS));
                    parameters.setPhaseShifterRegulationOn(config.getBooleanProperty("phaseShifterRegulationOn", DEFAULT_PHASE_SHIFTER_REGULATION_ON));
                    parameters.setSpecificCompatibility(config.getBooleanProperty("specificCompatibility", DEFAULT_SPECIFIC_COMPATIBILITY));
                    break;
                case LATEST_VERSION:
                    parameters.setVoltageInitMode(config.getEnumProperty("voltage-init-mode", VoltageInitMode.class, DEFAULT_VOLTAGE_INIT_MODE));
                    parameters.setTransformerVoltageControlOn(config.getBooleanProperty("transformer-voltage-control-on", DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON));
                    parameters.setNoGeneratorReactiveLimits(config.getBooleanProperty("no-generator-reactive-limits", DEFAULT_NO_GENERATOR_REACTIVE_LIMITS));
                    parameters.setPhaseShifterRegulationOn(config.getBooleanProperty("phase-shifter-regulation-on", DEFAULT_PHASE_SHIFTER_REGULATION_ON));
                    parameters.setSpecificCompatibility(config.getBooleanProperty("specific-compatibility", DEFAULT_SPECIFIC_COMPATIBILITY));
                    break;
                default:
                    throw new PowsyblException("Unexpected module version : this version is not supported");
            }
        }
    }

    private VersionConfig version;

    private VoltageInitMode voltageInitMode;

    private boolean transformerVoltageControlOn;

    private boolean noGeneratorReactiveLimits;

    private boolean phaseShifterRegulationOn;

    private boolean specificCompatibility;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn, boolean specificCompatibility, VersionConfig version) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        this.specificCompatibility = specificCompatibility;
        this.version = version;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn, boolean specificCompatibility) {
        this(voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits, phaseShifterRegulationOn, specificCompatibility, DEFAULT_VERSION);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn) {
        this(voltageInitMode, transformerVoltageControlOn, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPECIFIC_COMPATIBILITY, DEFAULT_VERSION);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode) {
        this(voltageInitMode, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPECIFIC_COMPATIBILITY, DEFAULT_VERSION);
    }

    public LoadFlowParameters(VersionConfig version) {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPECIFIC_COMPATIBILITY, version);
    }

    public LoadFlowParameters() {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPECIFIC_COMPATIBILITY, DEFAULT_VERSION);
    }

    protected LoadFlowParameters(LoadFlowParameters other) {
        Objects.requireNonNull(other);
        voltageInitMode = other.voltageInitMode;
        transformerVoltageControlOn = other.transformerVoltageControlOn;
        noGeneratorReactiveLimits = other.noGeneratorReactiveLimits;
        phaseShifterRegulationOn = other.phaseShifterRegulationOn;
        specificCompatibility = other.specificCompatibility;
        version = other.version;
    }

    public VersionConfig getVersion() {
        return version;
    }

    public LoadFlowParameters setVersion(VersionConfig version) {
        this.version = version;
        return this;
    }

    public VoltageInitMode getVoltageInitMode() {
        return voltageInitMode;
    }

    public LoadFlowParameters setVoltageInitMode(VoltageInitMode voltageInitMode) {
        this.voltageInitMode = Objects.requireNonNull(voltageInitMode);
        return this;
    }

    public boolean isTransformerVoltageControlOn() {
        return transformerVoltageControlOn;
    }

    public LoadFlowParameters setTransformerVoltageControlOn(boolean transformerVoltageControlOn) {
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        return this;
    }

    public boolean isNoGeneratorReactiveLimits() {
        return noGeneratorReactiveLimits;
    }

    public LoadFlowParameters setNoGeneratorReactiveLimits(boolean noGeneratorReactiveLimits) {
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        return this;
    }

    public boolean isPhaseShifterRegulationOn() {
        return phaseShifterRegulationOn;
    }

    public LoadFlowParameters setPhaseShifterRegulationOn(boolean phaseShifterRegulationOn) {
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        return this;
    }

    public boolean isSpecificCompatibility() {
        return specificCompatibility;
    }

    public LoadFlowParameters setSpecificCompatibility(boolean specificCompatibility) {
        this.specificCompatibility = specificCompatibility;
        return this;
    }

    protected Map<String, Object> toMap() {
        return ImmutableMap.of("voltageInitMode", voltageInitMode,
                "transformerVoltageControlOn", transformerVoltageControlOn,
                "noGeneratorReactiveLimits", noGeneratorReactiveLimits,
                "phaseShifterRegulationOn", phaseShifterRegulationOn,
                "specificCompatibility", specificCompatibility);
    }

    public LoadFlowParameters copy() {
        return new LoadFlowParameters(this);
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
