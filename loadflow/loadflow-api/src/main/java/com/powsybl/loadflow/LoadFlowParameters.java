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
import java.util.*;

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

    public enum BalanceType {
        PROPORTIONAL_TO_GENERATION_P,
        PROPORTIONAL_TO_GENERATION_P_MAX,
        PROPORTIONAL_TO_LOAD,
        PROPORTIONAL_TO_CONFORM_LOAD,
    }

    public enum ComputedConnectedComponentType {
        MAIN,
        ALL,
    }

    // VERSION = 1.0 specificCompatibility
    // VERSION = 1.1 t2wtSplitShuntAdmittance
    // VERSION = 1.2 twtSplitShuntAdmittance,
    // VERSION = 1.3 simulShunt, read/write slack bus
    // VERSION = 1.4 dc, distributedSlack, balanceType
    // VERSION = 1.5 dcUseTransformerRatio, countriesToBalance, computedConnectedComponent
    public static final String VERSION = "1.5";

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final boolean DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON = false;
    public static final boolean DEFAULT_NO_GENERATOR_REACTIVE_LIMITS = false;
    public static final boolean DEFAULT_PHASE_SHIFTER_REGULATION_ON = false;
    public static final boolean DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE = false;
    public static final boolean DEFAULT_SIMUL_SHUNT = false;
    public static final boolean DEFAULT_READ_SLACK_BUS = false;
    public static final boolean DEFAULT_WRITE_SLACK_BUS = false;
    public static final boolean DEFAULT_DC = false;
    public static final boolean DEFAULT_DISTRIBUTED_SLACK = true;
    public static final BalanceType DEFAULT_BALANCE_TYPE = BalanceType.PROPORTIONAL_TO_GENERATION_P_MAX;
    public static final boolean DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT = true;
    public static final List<String> DEFAULT_COUNTRIES_TO_BALANCE = new ArrayList<>();
    public static final ComputedConnectedComponentType DEFAULT_COMPUTED_CONNECTED_COMPONENT = ComputedConnectedComponentType.MAIN;

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
                parameters.setDc(config.getBooleanProperty("dc", DEFAULT_DC));
                parameters.setDistributedSlack(config.getBooleanProperty("distributedSlack", DEFAULT_DISTRIBUTED_SLACK));
                parameters.setBalanceType(config.getEnumProperty("balanceType", BalanceType.class, DEFAULT_BALANCE_TYPE));
                parameters.setDcUseTransformerRatio(config.getBooleanProperty("dcUseTransformerRatio", DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT));
                parameters.setCountriesToBalance(config.getStringListProperty("countriesToBalance", DEFAULT_COUNTRIES_TO_BALANCE));
                parameters.setComputedConnectedComponent(config.getEnumProperty("computedConnectedComponent", ComputedConnectedComponentType.class, DEFAULT_COMPUTED_CONNECTED_COMPONENT));
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

    private boolean dc;

    private boolean distributedSlack;

    private BalanceType balanceType;

    private boolean dcUseTransformerRatio;

    private List<String> countriesToBalance;

    private ComputedConnectedComponentType computedConnectedComponent;

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
                              boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn,
                              boolean twtSplitShuntAdmittance, boolean simulShunt, boolean readSlackBus, boolean writeSlackBus,
                              boolean dc, boolean distributedSlack, BalanceType balanceType, boolean dcUseTransformerRatio,
                              List<String> countriesToBalance, ComputedConnectedComponentType computedConnectedComponent) {
        this.voltageInitMode = voltageInitMode;
        this.transformerVoltageControlOn = transformerVoltageControlOn;
        this.noGeneratorReactiveLimits = noGeneratorReactiveLimits;
        this.phaseShifterRegulationOn = phaseShifterRegulationOn;
        this.twtSplitShuntAdmittance = twtSplitShuntAdmittance;
        this.simulShunt = simulShunt;
        this.readSlackBus = readSlackBus;
        this.writeSlackBus = writeSlackBus;
        this.dc = dc;
        this.distributedSlack = distributedSlack;
        this.balanceType = balanceType;
        this.dcUseTransformerRatio = dcUseTransformerRatio;
        this.countriesToBalance = countriesToBalance;
        this.computedConnectedComponent = computedConnectedComponent;
    }

    public LoadFlowParameters(VoltageInitMode voltageInitMode, boolean transformerVoltageControlOn,
        boolean noGeneratorReactiveLimits, boolean phaseShifterRegulationOn,
        boolean twtSplitShuntAdmittance) {
        this(voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits, phaseShifterRegulationOn, twtSplitShuntAdmittance, DEFAULT_SIMUL_SHUNT, DEFAULT_READ_SLACK_BUS, DEFAULT_WRITE_SLACK_BUS,
                DEFAULT_DC, DEFAULT_DISTRIBUTED_SLACK, DEFAULT_BALANCE_TYPE, DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, DEFAULT_COUNTRIES_TO_BALANCE, DEFAULT_COMPUTED_CONNECTED_COMPONENT);
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
        dc = other.dc;
        distributedSlack = other.distributedSlack;
        balanceType = other.balanceType;
        dcUseTransformerRatio = other.dcUseTransformerRatio;
        countriesToBalance = other.countriesToBalance;
        computedConnectedComponent = other.computedConnectedComponent;
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
                .put("noGeneratorReactiveLimits", noGeneratorReactiveLimits)
                .put("phaseShifterRegulationOn", phaseShifterRegulationOn)
                .put("twtSplitShuntAdmittance", twtSplitShuntAdmittance)
                .put("simulShunt", simulShunt)
                .put("readSlackBus", readSlackBus)
                .put("writeSlackBus", writeSlackBus)
                .put("dc", dc)
                .put("distributedSlack", distributedSlack)
                .put("balanceType", balanceType)
                .put("dcUseTransformerRatio", dcUseTransformerRatio)
                .put("countriesToBalance", countriesToBalance)
                .put("computedConnectedComponent", computedConnectedComponent);
        return immutableMapBuilder.build();
    }

    public LoadFlowParameters setDcUseTransformerRatio(boolean dcUseTransformerRatio) {
        this.dcUseTransformerRatio = dcUseTransformerRatio;
        return this;
    }

    public boolean getDcUseTransformerRatio() {
        return dcUseTransformerRatio;
    }

    public LoadFlowParameters setCountriesToBalance(List<String> countriesToBalance) {
        this.countriesToBalance = countriesToBalance;
        return this;
    }

    public List<String> getCountriesToBalance() {
        return countriesToBalance;
    }

    public ComputedConnectedComponentType getComputedConnectedComponent() {
        return computedConnectedComponent;
    }

    public LoadFlowParameters setComputedConnectedComponent(ComputedConnectedComponentType computedConnectedComponent) {
        this.computedConnectedComponent = computedConnectedComponent;
        return this;
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
