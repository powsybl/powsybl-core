/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    // VERSION = 1.0 specificCompatibility
    public static final String VERSION = "1.1";

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_NO_GENERATOR_REACTIVE_LIMITS = false;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE = false;

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

        platformConfig.getOptionalModuleConfig("load-flow-default-parameters")
                .ifPresent(config -> {
                    parameters.setVoltageInitMode(config.getEnumProperty("voltageInitMode", VoltageInitMode.class, DEFAULT_VOLTAGE_INIT_MODE));
                    parameters.setTransformerVoltageControlOn(config.getBooleanProperty("transformerVoltageControlOn", DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON));
                    parameters.setNoGeneratorReactiveLimits(config.getBooleanProperty("noGeneratorReactiveLimits", DEFAULT_NO_GENERATOR_REACTIVE_LIMITS));
                    parameters.setPhaseShifterRegulationOn(config.getBooleanProperty("phaseShifterRegulationOn", DEFAULT_PHASE_SHIFTER_REGULATION_ON));
                    // keep old tag name "specificCompatibility" for compatibility
                    parameters.setT2wtSplitShuntAdmittance(config.getBooleanProperty("t2wtSplitShuntAdmittance", config.getBooleanProperty("specificCompatibility", DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE)));
                });
    }

    private VoltageInitMode voltageInitMode;

    private boolean transformerVoltageControlOn;

    private boolean noGeneratorReactiveLimits;

    private boolean phaseShifterRegulationOn;

    private boolean t2wtSplitShuntAdmittance;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn, boolean t2wtSplitShuntAdmittance) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        this.t2wtSplitShuntAdmittance = t2wtSplitShuntAdmittance;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn) {
        this(voltageInitMode, transformerVoltageControlOn, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode) {
        this(voltageInitMode, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    public LoadFlowParameters() {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    protected LoadFlowParameters(LoadFlowParameters other) {
        Objects.requireNonNull(other);
        voltageInitMode = other.voltageInitMode;
        transformerVoltageControlOn = other.transformerVoltageControlOn;
        noGeneratorReactiveLimits = other.noGeneratorReactiveLimits;
        phaseShifterRegulationOn = other.phaseShifterRegulationOn;
        t2wtSplitShuntAdmittance = other.t2wtSplitShuntAdmittance;
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

    /**
     * @deprecated Use {@link #isT2wtSplitShuntAdmittance} instead.
     */
    @Deprecated
    public boolean isSpecificCompatibility() {
        return isT2wtSplitShuntAdmittance();
    }

    public boolean isT2wtSplitShuntAdmittance() {
        return t2wtSplitShuntAdmittance;
    }

    /**
     * @deprecated Use {@link #setT2wtSplitShuntAdmittance} instead.
     */
    @Deprecated
    public LoadFlowParameters setSpecificCompatibility(boolean t2wtSplitShuntAdmittance) {
        return setT2wtSplitShuntAdmittance(t2wtSplitShuntAdmittance);
    }

    public LoadFlowParameters setT2wtSplitShuntAdmittance(boolean t2wtSplitShuntAdmittance) {
        this.t2wtSplitShuntAdmittance = t2wtSplitShuntAdmittance;
        return this;
    }

    protected Map<String, Object> toMap() {
        return ImmutableMap.of("voltageInitMode", voltageInitMode,
                "transformerVoltageControlOn", transformerVoltageControlOn,
                "noGeneratorReactiveLimits", noGeneratorReactiveLimits,
                "phaseShifterRegulationOn", phaseShifterRegulationOn,
                "t2wtSplitShuntAdmittance", t2wtSplitShuntAdmittance);
    }

    /**
     * This copy methods uses json serializer mechanism to rebuild all extensions in the this parameters.
     * If an extension's serializer not found via {@code @AutoService}, the extension would be lost in copied.
     * @return a new copied instance and with original's extensions found based-on json serializer.
     */
    public LoadFlowParameters copy() {
        byte[] bytes = writeInMemory();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return JsonLoadFlowParameters.read(bais);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private byte[] writeInMemory() {
        try (ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder()) {
            JsonLoadFlowParameters.write(this, byteArrayBuilder);
            return byteArrayBuilder.toByteArray();
        }
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
