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
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

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

    public static final String VERSION = "1.0";

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_NO_GENERATOR_REACTIVE_LIMITS = false;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR2 = false;
    public static final boolean DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR3 = false;

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
                    parameters.setSplitShuntAdmittanceXfmr2(config.getBooleanProperty("splitShuntAdmittanceXfmr2", DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR2));
                    parameters.setSplitShuntAdmittanceXfmr3(config.getBooleanProperty("splitShuntAdmittanceXfmr3", DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR3));
                });
    }

    private VoltageInitMode voltageInitMode;

    private boolean transformerVoltageControlOn;

    private boolean noGeneratorReactiveLimits;

    private boolean phaseShifterRegulationOn;

    private boolean splitShuntAdmittanceXfmr2;

    private boolean splitShuntAdmittanceXfmr3;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
        boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn, boolean splitShuntAdmittanceXfmr2,
        boolean splitShuntAdmittanceXfmr3) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        this.splitShuntAdmittanceXfmr2 = splitShuntAdmittanceXfmr2;
        this.splitShuntAdmittanceXfmr3 = splitShuntAdmittanceXfmr3;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
        boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn, boolean splitShuntAdmittanceXfmr2) {
        this(voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits, phaseShifterRegulationOn,
            splitShuntAdmittanceXfmr2, DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR3);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn) {
        this(voltageInitMode, transformerVoltageControlOn, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR2);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode) {
        this(voltageInitMode, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR2);
    }

    public LoadFlowParameters() {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_SPLIT_SHUNT_ADMITTANCE_XFMR2);
    }

    protected LoadFlowParameters(LoadFlowParameters other) {
        Objects.requireNonNull(other);
        voltageInitMode = other.voltageInitMode;
        transformerVoltageControlOn = other.transformerVoltageControlOn;
        noGeneratorReactiveLimits = other.noGeneratorReactiveLimits;
        phaseShifterRegulationOn = other.phaseShifterRegulationOn;
        splitShuntAdmittanceXfmr2 = other.splitShuntAdmittanceXfmr2;
        splitShuntAdmittanceXfmr3 = other.splitShuntAdmittanceXfmr3;
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

    public boolean isSplitShuntAdmittanceXfmr2() {
        return splitShuntAdmittanceXfmr2;

    }

    public LoadFlowParameters setSplitShuntAdmittanceXfmr2(boolean splitShuntAdmittance) {
        this.splitShuntAdmittanceXfmr2 = splitShuntAdmittance;
        return this;
    }

    public boolean isSplitShuntAdmittanceXfmr3() {
        return splitShuntAdmittanceXfmr3;
    }

    public LoadFlowParameters setSplitShuntAdmittanceXfmr3(boolean splitShuntAdmittanceXfmr3) {
        this.splitShuntAdmittanceXfmr3 = splitShuntAdmittanceXfmr3;
        return this;
    }

    protected Map<String, Object> toMap() {
        return ImmutableMap.<String, Object>builder()
            .put("voltageInitMode", voltageInitMode)
            .put("transformerVoltageControlOn", transformerVoltageControlOn)
            .put("noGeneratorReactiveLimits", noGeneratorReactiveLimits)
            .put("phaseShifterRegulationOn", phaseShifterRegulationOn)
            .put("splitShuntAdmittanceXfmr2", splitShuntAdmittanceXfmr2)
            .put("splitShuntAdmittanceXfmr3", splitShuntAdmittanceXfmr3).build();
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
