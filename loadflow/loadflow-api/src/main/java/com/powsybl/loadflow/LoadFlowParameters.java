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
    // VERSION = 1.1 t2wtSplitShuntAdmittance
    // VERSION = 1.2 twtSplitShuntAdmittance,
    // VERSION = 1.3 simulShunt, read/write slack bus
    public static final String VERSION = "1.3";

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_NO_GENERATOR_REACTIVE_LIMITS = false;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE = false;
    public static final boolean DEFAULT_SIMUL_SHUNT = false;
    public static final boolean DEFAULT_READ_SLACK_BUS = false;
    public static final boolean DEFAULT_WRITE_SLACK_BUS = false;

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
                parameters.setTwtSplitShuntAdmittance(config.getBooleanProperty("twtSplitShuntAdmittance", config.getBooleanProperty("specificCompatibility", DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE)));
                parameters.setSimulShunt(config.getBooleanProperty("simulShunt", DEFAULT_SIMUL_SHUNT));
                parameters.setReadSlackBus(config.getBooleanProperty("readSlackBus", DEFAULT_READ_SLACK_BUS));
                parameters.setWriteSlackBus(config.getBooleanProperty("writeSlackBus", DEFAULT_WRITE_SLACK_BUS));
            });
    }

    private VoltageInitMode voltageInitMode;

    private boolean transformerVoltageControlOn;

    private boolean noGeneratorReactiveLimits;

    private boolean phaseShifterRegulationOn;

    private boolean twtSplitShuntAdmittance;

    private boolean simulShunt;

    private boolean readSlackBus;

    private boolean writeSlackBus;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn,
                              boolean twtSplitShuntAdmittance, boolean simulShunt, boolean readSlackBus, boolean writeSlackBus) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        this.simulShunt = simulShunt;
        this.readSlackBus = readSlackBus;
        this.writeSlackBus = writeSlackBus;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
        boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn,
        boolean twtSplitShuntAdmittance) {
        this(voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits, phaseShifterRegulationOn, twtSplitShuntAdmittance, DEFAULT_SIMUL_SHUNT, DEFAULT_READ_SLACK_BUS, DEFAULT_WRITE_SLACK_BUS);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn) {
        this(voltageInitMode, transformerVoltageControlOn, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode) {
        this(voltageInitMode, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE);
    }

    public LoadFlowParameters() {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_NO_GENERATOR_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE);
    }

    protected LoadFlowParameters(LoadFlowParameters other) {
        Objects.requireNonNull(other);
        voltageInitMode = other.voltageInitMode;
        transformerVoltageControlOn = other.transformerVoltageControlOn;
        noGeneratorReactiveLimits = other.noGeneratorReactiveLimits;
        phaseShifterRegulationOn = other.phaseShifterRegulationOn;
        twtSplitShuntAdmittance = other.twtSplitShuntAdmittance;
        simulShunt = other.simulShunt;
        readSlackBus = other.readSlackBus;
        writeSlackBus = other.writeSlackBus;
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
     * @deprecated Use {@link #isTwtSplitShuntAdmittance} instead.
     */
    @Deprecated
    public boolean isSpecificCompatibility() {
        return isTwtSplitShuntAdmittance();
    }

    /**
     * @deprecated Use {@link #isTwtSplitShuntAdmittance} instead.
     */
    @Deprecated
    public boolean isT2wtSplitShuntAdmittance() {
        return isTwtSplitShuntAdmittance();
    }

    public boolean isTwtSplitShuntAdmittance() {
        return twtSplitShuntAdmittance;
    }

    /**
     * @deprecated Use {@link #setTwtSplitShuntAdmittance} instead.
     */
    @Deprecated
    public LoadFlowParameters setSpecificCompatibility(boolean twtSplitShuntAdmittance) {
        return setTwtSplitShuntAdmittance(twtSplitShuntAdmittance);
    }

    /**
     * @deprecated Use {@link #setTwtSplitShuntAdmittance} instead.
     */
    @Deprecated
    public LoadFlowParameters setT2wtSplitShuntAdmittance(boolean twtSplitShuntAdmittance) {
        return setTwtSplitShuntAdmittance(twtSplitShuntAdmittance);
    }

    public LoadFlowParameters setTwtSplitShuntAdmittance(boolean twtSplitShuntAdmittance) {
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        return this;
    }

    public boolean isSimulShunt() {
        return simulShunt;
    }

    public LoadFlowParameters setSimulShunt(boolean simulShunt) {
        this.simulShunt = simulShunt;
        return this;
    }

    public boolean isReadSlackBus() {
        return readSlackBus;
    }

    public LoadFlowParameters setReadSlackBus(boolean readSlackBus) {
        this.readSlackBus = readSlackBus;
        return this;
    }

    public boolean isWriteSlackBus() {
        return writeSlackBus;
    }

    public LoadFlowParameters setWriteSlackBus(boolean writeSlackBus) {
        this.writeSlackBus = writeSlackBus;
        return this;
    }

    protected Map<String, Object> toMap() {
        ImmutableMap.Builder<String, Object> immutableMapBuilder = ImmutableMap.builder();
        immutableMapBuilder
                .put("voltageInitMode", voltageInitMode)
                .put("transformerVoltageControlOn", transformerVoltageControlOn)
                .put("noGeneratorReactiveLimits", noGeneratorReactiveLimits)
                .put("phaseShifterRegulationOn", phaseShifterRegulationOn)
                .put("twtSplitShuntAdmittance", twtSplitShuntAdmittance)
                .put("simulShunt", simulShunt)
                .put("readSlackBus", readSlackBus)
                .put("writeSlackBus", writeSlackBus);
        return immutableMapBuilder.build();
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
