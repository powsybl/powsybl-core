/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
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

    public enum ConnectedComponentMode {
        MAIN,
        ALL,
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowParameters.class);

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
    public static final String VERSION = "1.9";

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
    public static final ConnectedComponentMode DEFAULT_CONNECTED_COMPONENT_MODE = ConnectedComponentMode.MAIN;
    public static final boolean DEFAULT_HVDC_AC_EMULATION_ON = true;
    public static final double DEFAULT_DC_POWER_FACTOR = 1d;

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

        // Only the parameters present in platformConfig will be updated and no default value will be set for the absent parameters
        // (unlike what is done for the other parameters classes).
        // This is needed for the LoadFlowDefaultParametersLoader mechanism to work (else the default values defined
        // by the loader will be overwritten by the hardcoded ones).
        platformConfig.getOptionalModuleConfig("load-flow-default-parameters")
                .ifPresent(config -> {
                    config.getOptionalEnumProperty("voltageInitMode", VoltageInitMode.class).ifPresent(parameters::setVoltageInitMode);
                    config.getOptionalBooleanProperty("transformerVoltageControlOn").ifPresent(parameters::setTransformerVoltageControlOn);
                    config.getOptionalBooleanProperty("useReactiveLimits").ifPresentOrElse(parameters::setUseReactiveLimits,
                            () -> config.getOptionalBooleanProperty("noGeneratorReactiveLimits").ifPresent(value -> parameters.setUseReactiveLimits(!value)));
                    config.getOptionalBooleanProperty("phaseShifterRegulationOn").ifPresent(parameters::setPhaseShifterRegulationOn);
                    config.getOptionalBooleanProperty("twtSplitShuntAdmittance").ifPresentOrElse(parameters::setTwtSplitShuntAdmittance,
                            () -> config.getOptionalBooleanProperty("specificCompatibility").ifPresent(parameters::setTwtSplitShuntAdmittance));
                    config.getOptionalBooleanProperty("shuntCompensatorVoltageControlOn").ifPresentOrElse(parameters::setShuntCompensatorVoltageControlOn,
                            () -> config.getOptionalBooleanProperty("simulShunt").ifPresent(parameters::setShuntCompensatorVoltageControlOn));
                    config.getOptionalBooleanProperty("readSlackBus").ifPresent(parameters::setReadSlackBus);
                    config.getOptionalBooleanProperty("writeSlackBus").ifPresent(parameters::setWriteSlackBus);
                    config.getOptionalBooleanProperty("dc").ifPresent(parameters::setDc);
                    config.getOptionalBooleanProperty("distributedSlack").ifPresent(parameters::setDistributedSlack);
                    config.getOptionalEnumProperty("balanceType", BalanceType.class).ifPresent(parameters::setBalanceType);
                    config.getOptionalBooleanProperty("dcUseTransformerRatio").ifPresent(parameters::setDcUseTransformerRatio);
                    config.getOptionalEnumSetProperty("countriesToBalance", Country.class).ifPresent(parameters::setCountriesToBalance);
                    config.getOptionalEnumProperty("connectedComponentMode", ConnectedComponentMode.class).ifPresent(parameters::setConnectedComponentMode);
                    config.getOptionalBooleanProperty("hvdcAcEmulation").ifPresent(parameters::setHvdcAcEmulation);
                    config.getOptionalDoubleProperty("dcPowerFactor").ifPresent(parameters::setDcPowerFactor);
                });
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

    private ConnectedComponentMode connectedComponentMode = DEFAULT_CONNECTED_COMPONENT_MODE;

    private boolean hvdcAcEmulation = DEFAULT_HVDC_AC_EMULATION_ON;

    private double dcPowerFactor = DEFAULT_DC_POWER_FACTOR;

    public LoadFlowParameters() {
        this(ServiceLoader.load(LoadFlowDefaultParametersLoader.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList());
    }

    public LoadFlowParameters(List<LoadFlowDefaultParametersLoader> defaultParametersLoaders) {
        int numberOfLoadersFound = Objects.requireNonNull(defaultParametersLoaders).size();
        if (numberOfLoadersFound > 1) {
            List<String> names = defaultParametersLoaders.stream()
                    .map(LoadFlowDefaultParametersLoader::getSourceName)
                    .toList();
            LOGGER.warn("Multiple default loadflow parameters classes have been found in the class path : {}. No default parameters file loaded",
                    names);
        } else if (numberOfLoadersFound == 1) {
            LoadFlowDefaultParametersLoader loader = defaultParametersLoaders.get(0);
            JsonLoadFlowParameters.update(this, loader.loadDefaultParametersFromFile());
            LOGGER.debug("Default loadflow configuration has been updated using the reference file from parameters loader '{}'", loader.getSourceName());
        }
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
        dcPowerFactor = other.dcPowerFactor;
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
    @Deprecated(since = "5.1.0")
    public boolean isNoGeneratorReactiveLimits() {
        return !useReactiveLimits;
    }

    /**
     * @deprecated Use {@link #setNoGeneratorReactiveLimits} instead.
     */
    @Deprecated(since = "5.1.0")
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

    public boolean isTwtSplitShuntAdmittance() {
        return twtSplitShuntAdmittance;
    }

    public LoadFlowParameters setTwtSplitShuntAdmittance(boolean twtSplitShuntAdmittance) {
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        return this;
    }

    /**
     * @deprecated Use {@link #isShuntCompensatorVoltageControlOn()} instead.
     */
    @Deprecated(since = "4.7.0")
    public boolean isSimulShunt() {
        return isShuntCompensatorVoltageControlOn();
    }

    public boolean isShuntCompensatorVoltageControlOn() {
        return shuntCompensatorVoltageControlOn;
    }

    /**
     * @deprecated Use {@link #setShuntCompensatorVoltageControlOn(boolean)} instead.
     */
    @Deprecated(since = "4.7.0")
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

    public Map<String, Object> toMap() {
        return ImmutableMap.<String, Object>builder()
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
                .put("hvdcAcEmulation", hvdcAcEmulation)
                .put("dcPowerFactor", dcPowerFactor)
                .build();
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
            provider.loadSpecificParameters(platformConfig).ifPresent(extension ->
                    addExtension((Class) extension.getClass(), extension));
        }
    }
}
