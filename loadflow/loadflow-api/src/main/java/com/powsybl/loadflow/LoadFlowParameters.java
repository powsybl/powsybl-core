/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Parameters for loadflow computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowParameters extends AbstractExtendable<LoadFlowParameters> {

    public enum VoltageInitMode {
        UNIFORM_VALUES, // v=1pu, theta=0
        PREVIOUS_VALUES,
        DC_VALUES // preprocessing to compute DC angles
    }

    /**
     * BalanceType enum describes the various options for active power slack distribution
     */
    public enum BalanceType {
        /**
         * active power slack distribution on generators, proportional to generator targetP
         */
        PROPORTIONAL_TO_GENERATION_P,
        /**
         * active power slack distribution on generators, proportional to generator maxP
         */
        PROPORTIONAL_TO_GENERATION_P_MAX,
        /**
         * active power slack distribution on generators, proportional to generator maxP - targetP
         */
        PROPORTIONAL_TO_GENERATION_REMAINING_MARGIN,
        /**
         * active power slack distribution on generators, proportional to participationFactor (see ActivePowerControl extension)
         */
        PROPORTIONAL_TO_GENERATION_PARTICIPATION_FACTOR,
        /**
         * active power slack distribution on all loads
         */
        PROPORTIONAL_TO_LOAD,
        /**
         * active power slack distribution on conforming loads (see LoadDetails extension)
         */
        PROPORTIONAL_TO_CONFORM_LOAD,
    }

    public enum ConnectedComponentMode {
        MAIN,
        ALL,
    }

    // VERSION = 1.0 specificCompatibility
    // VERSION = 1.1 t2wtSplitShuntAdmittance
    // VERSION = 1.2 twtSplitShuntAdmittance,
    // VERSION = 1.3 simulShunt, read/write slack bus
    // VERSION = 1.4 dc, distributedSlack, balanceType
    // VERSION = 1.5 dcUseTransformerRatio, countriesToBalance, computedConnectedComponentScope
    // VERSION = 1.6 shuntCompensatorVoltageControlOn instead of simulShunt
    // VERSION = 1.7 hvdcAcEmulation
    // VERSION = 1.8 noGeneratorReactiveLimits -> useReactiveLimits
    public static final String VERSION = "1.8";

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_USE_REACTIVE_LIMITS = true;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE = false;
    public static final boolean DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_READ_SLACK_BUS = true;
    public static final boolean DEFAULT_WRITE_SLACK_BUS = false;
    public static final boolean DEFAULT_DC = false;
    public static final boolean DEFAULT_DISTRIBUTED_SLACK = true;
    public static final BalanceType DEFAULT_BALANCE_TYPE = BalanceType.PROPORTIONAL_TO_GENERATION_P_MAX;
    public static final boolean DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT = true;
    public static final Set<Country> DEFAULT_COUNTRIES_TO_BALANCE = EnumSet.noneOf(Country.class);
    public static final ConnectedComponentMode DEFAULT_CONNECTED_COMPONENT_MODE = ConnectedComponentMode.MAIN;
    public static final boolean DEFAULT_HVDC_AC_EMULATION_ON = true;

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
                    parameters.setUseReactiveLimits(!config.getBooleanProperty("noGeneratorReactiveLimits", !DEFAULT_USE_REACTIVE_LIMITS)); // overwritten by reactiveLimits
                    parameters.setUseReactiveLimits(config.getBooleanProperty("useReactiveLimits", DEFAULT_USE_REACTIVE_LIMITS));
                    parameters.setPhaseShifterRegulationOn(config.getBooleanProperty("phaseShifterRegulationOn", DEFAULT_PHASE_SHIFTER_REGULATION_ON));
                    // keep old tag name "specificCompatibility" for compatibility
                    parameters.setTwtSplitShuntAdmittance(config.getBooleanProperty("twtSplitShuntAdmittance", config.getBooleanProperty("specificCompatibility", DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE)));
                    parameters.setShuntCompensatorVoltageControlOn(config.getBooleanProperty("shuntCompensatorVoltageControlOn",
                            config.getOptionalBooleanProperty("simulShunt").orElse(DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON)));
                    parameters.setReadSlackBus(config.getBooleanProperty("readSlackBus", DEFAULT_READ_SLACK_BUS));
                    parameters.setWriteSlackBus(config.getBooleanProperty("writeSlackBus", DEFAULT_WRITE_SLACK_BUS));
                    parameters.setDc(config.getBooleanProperty("dc", DEFAULT_DC));
                    parameters.setDistributedSlack(config.getBooleanProperty("distributedSlack", DEFAULT_DISTRIBUTED_SLACK));
                    parameters.setBalanceType(config.getEnumProperty("balanceType", BalanceType.class, DEFAULT_BALANCE_TYPE));
                    parameters.setDcUseTransformerRatio(config.getBooleanProperty("dcUseTransformerRatio", DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT));
                    parameters.setCountriesToBalance(config.getEnumSetProperty("countriesToBalance", Country.class, DEFAULT_COUNTRIES_TO_BALANCE));
                    parameters.setConnectedComponentMode(config.getEnumProperty("connectedComponentMode", ConnectedComponentMode.class, DEFAULT_CONNECTED_COMPONENT_MODE));
                });
    }

    private VoltageInitMode voltageInitMode;

    private boolean transformerVoltageControlOn;

    private boolean useReactiveLimits;

    private boolean phaseShifterRegulationOn;

    private boolean twtSplitShuntAdmittance;

    private boolean shuntCompensatorVoltageControlOn;

    private boolean readSlackBus;

    private boolean writeSlackBus;

    private boolean dc;

    private boolean distributedSlack;

    private BalanceType balanceType;

    private boolean dcUseTransformerRatio;

    private Set<Country> countriesToBalance;

    private ConnectedComponentMode connectedComponentMode;

    private boolean hvdcAcEmulation;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean useReactiveLimits, boolean phaseShifterRegulationOn,
                              boolean twtSplitShuntAdmittance, boolean shuntCompensatorVoltageControlOn, boolean readSlackBus, boolean writeSlackBus,
                              boolean dc, boolean distributedSlack, BalanceType balanceType, boolean dcUseTransformerRatio,
                              Set<Country> countriesToBalance, ConnectedComponentMode connectedComponentMode, boolean hvdcAcEmulation) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.useReactiveLimits = useReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        this.shuntCompensatorVoltageControlOn = shuntCompensatorVoltageControlOn;
        this.readSlackBus = readSlackBus;
        this.writeSlackBus = writeSlackBus;
        this.dc = dc;
        this.distributedSlack = distributedSlack;
        this.balanceType = balanceType;
        this.dcUseTransformerRatio = dcUseTransformerRatio;
        this.countriesToBalance = countriesToBalance;
        this.connectedComponentMode = connectedComponentMode;
        this.hvdcAcEmulation = hvdcAcEmulation;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean useReactiveLimits, boolean phaseShifterRegulationOn,
                              boolean twtSplitShuntAdmittance) {
        this(voltageInitMode, transformerVoltageControlOn, useReactiveLimits, phaseShifterRegulationOn, twtSplitShuntAdmittance, DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON, DEFAULT_READ_SLACK_BUS, DEFAULT_WRITE_SLACK_BUS,
                DEFAULT_DC, DEFAULT_DISTRIBUTED_SLACK, DEFAULT_BALANCE_TYPE, DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, DEFAULT_COUNTRIES_TO_BALANCE, DEFAULT_CONNECTED_COMPONENT_MODE, DEFAULT_HVDC_AC_EMULATION_ON);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn) {
        this(voltageInitMode, transformerVoltageControlOn, DEFAULT_USE_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE);
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode) {
        this(voltageInitMode, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_USE_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE);
    }

    public LoadFlowParameters() {
        this(DEFAULT_VOLTAGE_INIT_MODE, DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, DEFAULT_USE_REACTIVE_LIMITS, DEFAULT_PHASE_SHIFTER_REGULATION_ON, DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE);
    }

    protected LoadFlowParameters(LoadFlowParameters other) {
        Objects.requireNonNull(other);
        voltageInitMode = other.voltageInitMode;
        transformerVoltageControlOn = other.transformerVoltageControlOn;
        useReactiveLimits = other.useReactiveLimits;
        phaseShifterRegulationOn = other.phaseShifterRegulationOn;
        twtSplitShuntAdmittance = other.twtSplitShuntAdmittance;
        shuntCompensatorVoltageControlOn = other.shuntCompensatorVoltageControlOn;
        readSlackBus = other.readSlackBus;
        writeSlackBus = other.writeSlackBus;
        dc = other.dc;
        distributedSlack = other.distributedSlack;
        balanceType = other.balanceType;
        dcUseTransformerRatio = other.dcUseTransformerRatio;
        countriesToBalance = other.countriesToBalance;
        connectedComponentMode = other.connectedComponentMode;
        hvdcAcEmulation = other.hvdcAcEmulation;
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

    public boolean isUseReactiveLimits() {
        return useReactiveLimits;
    }

    public LoadFlowParameters setUseReactiveLimits(boolean useReactiveLimits) {
        this.useReactiveLimits = useReactiveLimits;
        return this;
    }

    /**
     * @deprecated Use {@link #isUseReactiveLimits} instead.
     */
    @Deprecated
    public boolean isNoGeneratorReactiveLimits() {
        return !useReactiveLimits;
    }

    /**
     * @deprecated Use {@link #setNoGeneratorReactiveLimits} instead.
     */
    @Deprecated
    public LoadFlowParameters setNoGeneratorReactiveLimits(boolean noGeneratorReactiveLimits) {
        this.useReactiveLimits = !noGeneratorReactiveLimits;
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

    /**
     * @deprecated Use {@link #isShuntCompensatorVoltageControlOn()} instead.
     */
    @Deprecated
    public boolean isSimulShunt() {
        return isShuntCompensatorVoltageControlOn();
    }

    public boolean isShuntCompensatorVoltageControlOn() {
        return shuntCompensatorVoltageControlOn;
    }

    /**
     * @deprecated Use {@link #setShuntCompensatorVoltageControlOn(boolean)} instead.
     */
    @Deprecated
    public LoadFlowParameters setSimulShunt(boolean simulShunt) {
        return setShuntCompensatorVoltageControlOn(simulShunt);
    }

    public LoadFlowParameters setShuntCompensatorVoltageControlOn(boolean shuntCompensatorVoltageControlOn) {
        this.shuntCompensatorVoltageControlOn = shuntCompensatorVoltageControlOn;
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

    public boolean isDc() {
        return dc;
    }

    public LoadFlowParameters setDc(boolean dc) {
        this.dc = dc;
        return this;
    }

    public boolean isDistributedSlack() {
        return distributedSlack;
    }

    public LoadFlowParameters setDistributedSlack(boolean distributedSlack) {
        this.distributedSlack = distributedSlack;
        return this;
    }

    public LoadFlowParameters setBalanceType(BalanceType balanceType) {
        this.balanceType = Objects.requireNonNull(balanceType);
        return this;
    }

    public BalanceType getBalanceType() {
        return balanceType;
    }

    protected Map<String, Object> toMap() {
        ImmutableMap.Builder<String, Object> immutableMapBuilder = ImmutableMap.builder();
        immutableMapBuilder
                .put("voltageInitMode", voltageInitMode)
                .put("transformerVoltageControlOn", transformerVoltageControlOn)
                .put("useReactiveLimits", useReactiveLimits)
                .put("phaseShifterRegulationOn", phaseShifterRegulationOn)
                .put("twtSplitShuntAdmittance", twtSplitShuntAdmittance)
                .put("shuntCompensatorVoltageControlOn", shuntCompensatorVoltageControlOn)
                .put("readSlackBus", readSlackBus)
                .put("writeSlackBus", writeSlackBus)
                .put("dc", dc)
                .put("distributedSlack", distributedSlack)
                .put("balanceType", balanceType)
                .put("dcUseTransformerRatio", dcUseTransformerRatio)
                .put("countriesToBalance", countriesToBalance)
                .put("computedConnectedComponentScope", connectedComponentMode)
                .put("hvdcAcEmulation", hvdcAcEmulation);
        return immutableMapBuilder.build();
    }

    public LoadFlowParameters setDcUseTransformerRatio(boolean dcUseTransformerRatio) {
        this.dcUseTransformerRatio = dcUseTransformerRatio;
        return this;
    }

    public boolean isDcUseTransformerRatio() {
        return dcUseTransformerRatio;
    }

    public LoadFlowParameters setCountriesToBalance(Set<Country> countriesToBalance) {
        this.countriesToBalance = Objects.requireNonNull(countriesToBalance);
        return this;
    }

    public Set<Country> getCountriesToBalance() {
        return Collections.unmodifiableSet(countriesToBalance);
    }

    public ConnectedComponentMode getConnectedComponentMode() {
        return connectedComponentMode;
    }

    public LoadFlowParameters setConnectedComponentMode(ConnectedComponentMode connectedComponentMode) {
        this.connectedComponentMode = connectedComponentMode;
        return this;
    }

    public boolean isHvdcAcEmulation() {
        return hvdcAcEmulation;
    }

    public LoadFlowParameters setHvdcAcEmulation(boolean hvdcAcEmulation) {
        this.hvdcAcEmulation = hvdcAcEmulation;
        return this;
    }

    /**
     * This copy methods uses json serializer mechanism to rebuild all extensions in the this parameters.
     * If an extension's serializer not found via {@code @AutoService}, the extension would be lost in copied.
     *
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
        for (LoadFlowProvider provider : new ServiceLoaderCache<>(LoadFlowProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(loadFlowParametersExtension ->
                    addExtension((Class) loadFlowParametersExtension.getClass(), loadFlowParametersExtension));
        }
    }
}
