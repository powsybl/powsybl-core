/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.api;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;

import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowParameters implements Cloneable {

    private static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    private static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    private static final boolean DEFAULT_NO_GENERATOR_REACTIVE_LIMITS = false;
    private static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;

    public enum VoltageInitMode {
        UNIFORM_VALUES, // v=1pu, theta=0
        PREVIOUS_VALUES,
        DC_VALUES // preprocessing to compute DC angles
    }

    public static LoadFlowParameters load() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        load(parameters);
        return parameters;
    }

    protected static void load(LoadFlowParameters parameters) {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfigIfExists("load-flow-default-parameters");
        if (config != null) {
            parameters.setVoltageInitMode(config.getEnumProperty("voltageInitMode", VoltageInitMode.class, DEFAULT_VOLTAGE_INIT_MODE));
            parameters.setTransformerVoltageControlOn(config.getBooleanProperty("transformerVoltageControlOn", DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON));
            parameters.setNoGeneratorReactiveLimits(config.getBooleanProperty("noGeneratorReactiveLimits", DEFAULT_NO_GENERATOR_REACTIVE_LIMITS));
            parameters.setPhaseShifterRegulationOn(config.getBooleanProperty("phaseShifterRegulationOn", DEFAULT_PHASE_SHIFTER_REGULATION_ON));
        }
    }

    private VoltageInitMode voltageInitMode;

    private boolean transformerVoltageControlOn;

    private boolean noGeneratorReactiveLimits;

    private boolean phaseShifterRegulationOn;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn) {
        this(voltageInitMode, transformerVoltageControlOn, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode) {
        this(voltageInitMode, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON);
    }

    public LoadFlowParameters() {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON);
    }

    protected LoadFlowParameters(LoadFlowParameters other) {
        Objects.requireNonNull(other);
        voltageInitMode = other.voltageInitMode;
        transformerVoltageControlOn = other.transformerVoltageControlOn;
        noGeneratorReactiveLimits = other.noGeneratorReactiveLimits;
        phaseShifterRegulationOn = other.phaseShifterRegulationOn;
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

    protected Map<String, Object> toMap() {
        return ImmutableMap.of("voltageInitMode", voltageInitMode,
                               "transformerVoltageControlOn", transformerVoltageControlOn,
                               "noGeneratorReactiveLimits", noGeneratorReactiveLimits,
                               "phaseShifterRegulationOn", phaseShifterRegulationOn);
    }

    @Override
    public LoadFlowParameters clone() {
        return new LoadFlowParameters(this);
    }

    @Override
    public String toString() {
        return toMap().toString();
    }
}
