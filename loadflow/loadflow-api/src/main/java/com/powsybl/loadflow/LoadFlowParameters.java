/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Parameters for loadflow computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LoadFlowParameters extends AbstractExtendable<LoadFlowParameters> {

    /**
     * @deprecated Downstream code should declare its own logger.
     */
    @Deprecated(forRemoval = true)
    public static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowParameters.class);

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
         * active power slack distribution on generators, proportional to generator maxP - targetP if active production is increased, and proportional to targetP - minP if decreased.
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

    public enum ComponentMode {
        MAIN_CONNECTED,
        ALL_CONNECTED,
        MAIN_SYNCHRONOUS;

        @JsonCreator
        public static ComponentMode fromString(String value) {
            return switch (value) {
                case "MAIN" -> MAIN_CONNECTED;
                case "ALL" -> ALL_CONNECTED;
                case "MAIN_CONNECTED" -> MAIN_CONNECTED;
                case "ALL_CONNECTED" -> ALL_CONNECTED;
                case "MAIN_SYNCHRONOUS" -> MAIN_SYNCHRONOUS;
                default -> throw new IllegalArgumentException("ComponentMode unknown value: " + value);
            };
        }
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
    // VERSION = 1.9 dcPowerFactor
    // VERSION = 1.10 componentMode instead of connectedComponentMode
    // VERSION = 1.11 debugDir
    public static final String VERSION = "1.11";

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_USE_REACTIVE_LIMITS = true;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE = false;
    public static final boolean DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_READ_SLACK_BUS = true;
    public static final boolean DEFAULT_WRITE_SLACK_BUS = true;
    public static final boolean DEFAULT_DC = false;
    public static final boolean DEFAULT_DISTRIBUTED_SLACK = true;
    public static final BalanceType DEFAULT_BALANCE_TYPE = BalanceType.PROPORTIONAL_TO_GENERATION_P_MAX;
    public static final boolean DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT = true;
    public static final Set<Country> DEFAULT_COUNTRIES_TO_BALANCE = Collections.unmodifiableSet(EnumSet.noneOf(Country.class));
    public static final ComponentMode DEFAULT_COMPONENT_MODE = ComponentMode.MAIN_CONNECTED;
    public static final boolean DEFAULT_HVDC_AC_EMULATION_ON = true;
    public static final double DEFAULT_DC_POWER_FACTOR = 1d;
    public static final String DEFAULT_DEBUG_DIR = null;

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
        LoadFlowParameters parameters = new LoadFlowParameters(platformConfig);
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

        platformConfig.getOptionalModuleConfig("load-flow")
                .flatMap(c -> c.getOptionalStringProperty("default-parameters-loader"))
                .ifPresent(name -> LOGGER.warn("Property 'default-parameters-loader' of module 'load-flow' ('{}') is ignored: "
                        + "LoadFlowDefaultParametersLoader has been removed. Ship the values as a runtime configuration "
                        + "instead, see the runtime configuration documentation.", name));

        platformConfig.getOptionalModuleConfig("load-flow-default-parameters")
                .ifPresent(config -> loadDefaultParametersConfig(parameters, config));
    }

    private static void loadDefaultParametersConfig(LoadFlowParameters parameters, ModuleConfig config) {
        parameters.setVoltageInitMode(config.getEnumProperty("voltageInitMode", VoltageInitMode.class, DEFAULT_VOLTAGE_INIT_MODE));
        parameters.setTransformerVoltageControlOn(config.getBooleanProperty("transformerVoltageControlOn", DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON));
        // deprecated alias "noGeneratorReactiveLimits" is the negation of "useReactiveLimits"
        // the fallback is lazy: a stale legacy key must not be read (nor fail) when the new name is present
        parameters.setUseReactiveLimits(config.getOptionalBooleanProperty("useReactiveLimits")
            .orElseGet(() -> !config.getBooleanProperty("noGeneratorReactiveLimits", !DEFAULT_USE_REACTIVE_LIMITS)));
        parameters.setPhaseShifterRegulationOn(config.getBooleanProperty("phaseShifterRegulationOn", DEFAULT_PHASE_SHIFTER_REGULATION_ON));
        parameters.setTwtSplitShuntAdmittance(config.getOptionalBooleanProperty("twtSplitShuntAdmittance")
            .orElseGet(() -> config.getBooleanProperty("specificCompatibility", DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE)));
        parameters.setShuntCompensatorVoltageControlOn(config.getOptionalBooleanProperty("shuntCompensatorVoltageControlOn")
            .orElseGet(() -> config.getBooleanProperty("simulShunt", DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON)));
        parameters.setReadSlackBus(config.getBooleanProperty("readSlackBus", DEFAULT_READ_SLACK_BUS));
        parameters.setWriteSlackBus(config.getBooleanProperty("writeSlackBus", DEFAULT_WRITE_SLACK_BUS));
        parameters.setDc(config.getBooleanProperty("dc", DEFAULT_DC));
        parameters.setDistributedSlack(config.getBooleanProperty("distributedSlack", DEFAULT_DISTRIBUTED_SLACK));
        parameters.setBalanceType(config.getEnumProperty("balanceType", BalanceType.class, DEFAULT_BALANCE_TYPE));
        parameters.setDcUseTransformerRatio(config.getBooleanProperty("dcUseTransformerRatio", DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT));
        parameters.setCountriesToBalance(config.getEnumSetProperty("countriesToBalance", Country.class, DEFAULT_COUNTRIES_TO_BALANCE));
        parameters.setComponentMode(config.getOptionalEnumProperty("componentMode", ComponentMode.class)
            .orElseGet(() -> config.getEnumProperty("connectedComponentMode", ComponentMode.class, DEFAULT_COMPONENT_MODE)));
        parameters.setHvdcAcEmulation(config.getBooleanProperty("hvdcAcEmulation", DEFAULT_HVDC_AC_EMULATION_ON));
        parameters.setDcPowerFactor(config.getDoubleProperty("dcPowerFactor", DEFAULT_DC_POWER_FACTOR));
        parameters.setDebugDir(config.getStringProperty("debugDir", DEFAULT_DEBUG_DIR));
    }

    private VoltageInitMode voltageInitMode = DEFAULT_VOLTAGE_INIT_MODE;

    private boolean transformerVoltageControlOn = DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON;

    private boolean useReactiveLimits = DEFAULT_USE_REACTIVE_LIMITS;

    private boolean phaseShifterRegulationOn = DEFAULT_PHASE_SHIFTER_REGULATION_ON;

    private boolean twtSplitShuntAdmittance = DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE;

    private boolean shuntCompensatorVoltageControlOn = DEFAULT_SHUNT_COMPENSATOR_VOLTAGE_CONTROL_ON;

    private boolean readSlackBus = DEFAULT_READ_SLACK_BUS;

    private boolean writeSlackBus = DEFAULT_WRITE_SLACK_BUS;

    private boolean dc = DEFAULT_DC;

    private boolean distributedSlack = DEFAULT_DISTRIBUTED_SLACK;

    private BalanceType balanceType = DEFAULT_BALANCE_TYPE;

    private boolean dcUseTransformerRatio = DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT;

    private Set<Country> countriesToBalance = DEFAULT_COUNTRIES_TO_BALANCE;

    private ComponentMode componentMode = DEFAULT_COMPONENT_MODE;

    private boolean hvdcAcEmulation = DEFAULT_HVDC_AC_EMULATION_ON;

    private double dcPowerFactor = DEFAULT_DC_POWER_FACTOR;

    private String debugDir = DEFAULT_DEBUG_DIR;

    public LoadFlowParameters() {
        this(PlatformConfig.defaultConfig());
    }

    protected LoadFlowParameters(PlatformConfig config) {
        // nothing to do: default values are set by the field initializers
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
        componentMode = other.componentMode;
        hvdcAcEmulation = other.hvdcAcEmulation;
        dcPowerFactor = other.dcPowerFactor;
        debugDir = other.debugDir;
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

    public boolean isPhaseShifterRegulationOn() {
        return phaseShifterRegulationOn;
    }

    public LoadFlowParameters setPhaseShifterRegulationOn(boolean phaseShifterRegulationOn) {
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        return this;
    }

    public boolean isTwtSplitShuntAdmittance() {
        return twtSplitShuntAdmittance;
    }

    public LoadFlowParameters setTwtSplitShuntAdmittance(boolean twtSplitShuntAdmittance) {
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        return this;
    }

    public boolean isShuntCompensatorVoltageControlOn() {
        return shuntCompensatorVoltageControlOn;
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

    public LoadFlowParameters setDcUseTransformerRatio(boolean dcUseTransformerRatio) {
        this.dcUseTransformerRatio = dcUseTransformerRatio;
        return this;
    }

    public boolean isDcUseTransformerRatio() {
        return dcUseTransformerRatio;
    }

    public LoadFlowParameters setCountriesToBalance(Set<Country> countriesToBalance) {
        this.countriesToBalance = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(countriesToBalance)));
        return this;
    }

    public Set<Country> getCountriesToBalance() {
        return Collections.unmodifiableSet(countriesToBalance);
    }

    public ComponentMode getComponentMode() {
        return componentMode;
    }

    public LoadFlowParameters setComponentMode(ComponentMode componentMode) {
        this.componentMode = componentMode;
        return this;
    }

    public boolean isHvdcAcEmulation() {
        return hvdcAcEmulation;
    }

    public LoadFlowParameters setHvdcAcEmulation(boolean hvdcAcEmulation) {
        this.hvdcAcEmulation = hvdcAcEmulation;
        return this;
    }

    public double getDcPowerFactor() {
        return dcPowerFactor;
    }

    public LoadFlowParameters setDcPowerFactor(double dcPowerFactor) {
        if (dcPowerFactor <= 0 || dcPowerFactor > 1) {
            throw new IllegalArgumentException("Invalid DC power factor: " + dcPowerFactor);
        }
        this.dcPowerFactor = dcPowerFactor;
        return this;
    }

    public String getDebugDir() {
        return debugDir;
    }

    /**
     * @param debugDir the debug directory where execution files will be dumped
     */
    public LoadFlowParameters setDebugDir(String debugDir) {
        this.debugDir = debugDir;
        return this;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("voltageInitMode", voltageInitMode);
        map.put("transformerVoltageControlOn", transformerVoltageControlOn);
        map.put("useReactiveLimits", useReactiveLimits);
        map.put("phaseShifterRegulationOn", phaseShifterRegulationOn);
        map.put("twtSplitShuntAdmittance", twtSplitShuntAdmittance);
        map.put("shuntCompensatorVoltageControlOn", shuntCompensatorVoltageControlOn);
        map.put("readSlackBus", readSlackBus);
        map.put("writeSlackBus", writeSlackBus);
        map.put("dc", dc);
        map.put("distributedSlack", distributedSlack);
        map.put("balanceType", balanceType);
        map.put("dcUseTransformerRatio", dcUseTransformerRatio);
        map.put("countriesToBalance", countriesToBalance);
        map.put("componentMode", componentMode);
        map.put("hvdcAcEmulation", hvdcAcEmulation);
        map.put("dcPowerFactor", dcPowerFactor);
        map.put("debugDir", debugDir);
        return Collections.unmodifiableMap(map);
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

    protected void loadExtensions(PlatformConfig platformConfig) {
        for (LoadFlowProvider provider : new ServiceLoaderCache<>(LoadFlowProvider.class).getServices()) {
            provider.getSpecificParametersClass().ifPresent(clazz -> {
                Optional<Extension<LoadFlowParameters>> extension = Optional.ofNullable(getExtension(clazz));
                extension.ifPresentOrElse(ext -> provider.updateSpecificParameters(ext, platformConfig),
                        () -> provider.loadSpecificParameters(platformConfig)
                                .ifPresent(ext -> addExtension((Class) ext.getClass(), ext)));
            });
        }
    }
}
