/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Objects;

/**
 * Settings that change what an estimate means, independently of how it is computed.
 *
 * <p>Anything that only affects how a particular implementation reaches the answer, such as a
 * convergence tolerance or an iteration cap, belongs in an
 * {@link com.powsybl.commons.extensions.Extension} contributed by that implementation through
 * {@link StateEstimationProvider#getSpecificParametersClass()}.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public class StateEstimationParameters extends AbstractExtendable<StateEstimationParameters> {

    /** Version of this parameter set. It changes when a setting is added, removed or renamed. */
    public static final String VERSION = "1.0";

    private static final String MODULE_CONFIG_NAME = "state-estimation-default-parameters";

    /** Where the starting point of the iteration comes from. */
    public enum VoltageInitMode {
        /** 1 pu and 0 radian at every bus. */
        UNIFORM_VALUES,
        /** The voltages already held by the network. */
        PREVIOUS_VALUES,
        /** Angles from a DC load flow, magnitudes at 1 pu. */
        DC_VALUES
    }

    /** How a bus known to inject nothing is handled. */
    public enum ZeroInjectionMode {
        /** A bus with no device is treated like any other unmeasured bus. */
        IGNORED,
        /** A zero-valued measurement with a very small standard deviation. */
        HIGH_CONFIDENCE_MEASUREMENT,
        /** Enforced exactly, so no weight is involved. */
        EQUALITY_CONSTRAINT
    }

    /** How a bus injection is formed from the measurements of the devices connected to the bus. */
    public enum BusInjectionPolicy {
        /** Form a bus injection only where every connected device has a meter. */
        REQUIRE_ALL_METERED,
        /** Sum the metered devices, treating the others as zero. */
        SUM_METERED
    }

    /** What to do with two measurements of the same quantity at the same place. */
    public enum DuplicateMeasurementPolicy {
        /** Combine into one reading weighted by the inverse of each variance. */
        MERGE_INVERSE_VARIANCE,
        /** Every reading contributes its own row. */
        KEEP_ALL,
        /** Refuse to estimate. */
        THROW
    }

    /** What to do with a component the measurements do not determine. */
    public enum UnobservablePolicy {
        /** Refuse to estimate. */
        THROW,
        /** Report the component as unobservable and estimate the others. */
        SKIP_COMPONENT,
        /** Estimate each observable island of the component separately. */
        ESTIMATE_OBSERVABLE_ISLANDS
    }

    /** Which connected components are estimated. */
    public enum ComponentMode {
        /** The main connected component only. */
        MAIN,
        /** Every connected component. */
        ALL
    }

    /** Default for {@link #getVoltageInitMode()}. */
    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    /** Default for {@link #getZeroInjectionMode()}. */
    public static final ZeroInjectionMode DEFAULT_ZERO_INJECTION_MODE = ZeroInjectionMode.HIGH_CONFIDENCE_MEASUREMENT;
    /** Default for {@link #getBusInjectionPolicy()}. */
    public static final BusInjectionPolicy DEFAULT_BUS_INJECTION_POLICY = BusInjectionPolicy.REQUIRE_ALL_METERED;
    /** Default for {@link #getDuplicateMeasurementPolicy()}. */
    public static final DuplicateMeasurementPolicy DEFAULT_DUPLICATE_MEASUREMENT_POLICY = DuplicateMeasurementPolicy.MERGE_INVERSE_VARIANCE;
    /** Default for {@link #getUnobservablePolicy()}. */
    public static final UnobservablePolicy DEFAULT_UNOBSERVABLE_POLICY = UnobservablePolicy.ESTIMATE_OBSERVABLE_ISLANDS;
    /** Default for {@link #getComponentMode()}. */
    public static final ComponentMode DEFAULT_COMPONENT_MODE = ComponentMode.MAIN;
    /** Default for {@link #getRelativeStandardDeviationFloor()}. Zero applies no floor. */
    public static final double DEFAULT_RELATIVE_STANDARD_DEVIATION_FLOOR = 0.0;
    /** Default for {@link #getResidualFlaggingThreshold()}. */
    public static final double DEFAULT_RESIDUAL_FLAGGING_THRESHOLD = 3.0;
    /** Default for {@link #isWriteResultsToNetwork()}. */
    public static final boolean DEFAULT_WRITE_RESULTS_TO_NETWORK = false;

    private VoltageInitMode voltageInitMode = DEFAULT_VOLTAGE_INIT_MODE;
    private ZeroInjectionMode zeroInjectionMode = DEFAULT_ZERO_INJECTION_MODE;
    private BusInjectionPolicy busInjectionPolicy = DEFAULT_BUS_INJECTION_POLICY;
    private DuplicateMeasurementPolicy duplicateMeasurementPolicy = DEFAULT_DUPLICATE_MEASUREMENT_POLICY;
    private UnobservablePolicy unobservablePolicy = DEFAULT_UNOBSERVABLE_POLICY;
    private ComponentMode componentMode = DEFAULT_COMPONENT_MODE;
    private double relativeStandardDeviationFloor = DEFAULT_RELATIVE_STANDARD_DEVIATION_FLOOR;
    private double residualFlaggingThreshold = DEFAULT_RESIDUAL_FLAGGING_THRESHOLD;
    private boolean writeResultsToNetwork = DEFAULT_WRITE_RESULTS_TO_NETWORK;

    /**
     * Parameters read from the default platform configuration, with the implementations' specific
     * parameters attached as extensions.
     */
    public static StateEstimationParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Parameters read from the given platform configuration, with the implementations' specific
     * parameters attached as extensions.
     *
     * @param platformConfig the platform configuration to read from
     * @return the parameters read from that platform configuration
     */
    public static StateEstimationParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        StateEstimationParameters parameters = new StateEstimationParameters();
        platformConfig.getOptionalModuleConfig(MODULE_CONFIG_NAME).ifPresent(parameters::readConfig);
        parameters.loadExtensions(platformConfig);
        return parameters;
    }

    private void readConfig(ModuleConfig config) {
        config.getOptionalEnumProperty("voltageInitMode", VoltageInitMode.class).ifPresent(this::setVoltageInitMode);
        config.getOptionalEnumProperty("zeroInjectionMode", ZeroInjectionMode.class).ifPresent(this::setZeroInjectionMode);
        config.getOptionalEnumProperty("busInjectionPolicy", BusInjectionPolicy.class).ifPresent(this::setBusInjectionPolicy);
        config.getOptionalEnumProperty("duplicateMeasurementPolicy", DuplicateMeasurementPolicy.class).ifPresent(this::setDuplicateMeasurementPolicy);
        config.getOptionalEnumProperty("unobservablePolicy", UnobservablePolicy.class).ifPresent(this::setUnobservablePolicy);
        config.getOptionalEnumProperty("componentMode", ComponentMode.class).ifPresent(this::setComponentMode);
        config.getOptionalDoubleProperty("relativeStandardDeviationFloor").ifPresent(this::setRelativeStandardDeviationFloor);
        config.getOptionalDoubleProperty("residualFlaggingThreshold").ifPresent(this::setResidualFlaggingThreshold);
        config.getOptionalBooleanProperty("writeResultsToNetwork").ifPresent(this::setWriteResultsToNetwork);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadExtensions(PlatformConfig platformConfig) {
        for (StateEstimationProvider provider : new ServiceLoaderCache<>(StateEstimationProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig)
                    .ifPresent(extension -> addExtension((Class) extension.getClass(), extension));
        }
    }

    /** Where the starting point of the iteration comes from. */
    public VoltageInitMode getVoltageInitMode() {
        return voltageInitMode;
    }

    /**
     * Sets where the starting point of the iteration comes from.
     *
     * @param voltageInitMode where the starting point of the iteration comes from
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setVoltageInitMode(VoltageInitMode voltageInitMode) {
        this.voltageInitMode = Objects.requireNonNull(voltageInitMode);
        return this;
    }

    /** How a bus known to inject nothing is handled. */
    public ZeroInjectionMode getZeroInjectionMode() {
        return zeroInjectionMode;
    }

    /**
     * Sets how a bus known to inject nothing is handled.
     *
     * @param zeroInjectionMode how a bus known to inject nothing is handled
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setZeroInjectionMode(ZeroInjectionMode zeroInjectionMode) {
        this.zeroInjectionMode = Objects.requireNonNull(zeroInjectionMode);
        return this;
    }

    /**
     * How a bus injection is formed from the measurements of the devices connected to the bus.
     * Summing only the metered devices gives the wrong injection whenever an unmetered device
     * carries power, so the default forms an injection only where every device has a meter.
     */
    public BusInjectionPolicy getBusInjectionPolicy() {
        return busInjectionPolicy;
    }

    /**
     * Sets how a bus injection is formed from the measurements of the devices connected to the bus.
     *
     * @param busInjectionPolicy how a bus injection is formed from the measurements of the connected devices
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setBusInjectionPolicy(BusInjectionPolicy busInjectionPolicy) {
        this.busInjectionPolicy = Objects.requireNonNull(busInjectionPolicy);
        return this;
    }

    /** What to do with two measurements of the same quantity at the same place. */
    public DuplicateMeasurementPolicy getDuplicateMeasurementPolicy() {
        return duplicateMeasurementPolicy;
    }

    /**
     * Sets what to do with two measurements of the same quantity at the same place.
     *
     * @param duplicateMeasurementPolicy what to do with two measurements of the same quantity at the same place
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setDuplicateMeasurementPolicy(DuplicateMeasurementPolicy duplicateMeasurementPolicy) {
        this.duplicateMeasurementPolicy = Objects.requireNonNull(duplicateMeasurementPolicy);
        return this;
    }

    /** What to do with a component the measurements do not determine. */
    public UnobservablePolicy getUnobservablePolicy() {
        return unobservablePolicy;
    }

    /**
     * Sets what to do with a component the measurements do not determine.
     *
     * @param unobservablePolicy what to do with a component the measurements do not determine
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setUnobservablePolicy(UnobservablePolicy unobservablePolicy) {
        this.unobservablePolicy = Objects.requireNonNull(unobservablePolicy);
        return this;
    }

    /** Which connected components are estimated. */
    public ComponentMode getComponentMode() {
        return componentMode;
    }

    /**
     * Sets which connected components are estimated.
     *
     * @param componentMode which connected components are estimated
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setComponentMode(ComponentMode componentMode) {
        this.componentMode = Objects.requireNonNull(componentMode);
        return this;
    }

    /**
     * Lower bound on a measurement's standard deviation, expressed as a fraction of the measured
     * value so that one setting covers megawatts, kilovolts and amperes alike. It stops a reading
     * that claims near-perfect accuracy from dominating the estimate. Zero, the default, applies
     * no floor.
     */
    public double getRelativeStandardDeviationFloor() {
        return relativeStandardDeviationFloor;
    }

    /**
     * Sets the lower bound on a measurement's standard deviation, as a fraction of the measured
     * value. Zero applies no floor.
     *
     * <p>Throws {@link IllegalArgumentException} if the value is negative.</p>
     *
     * @param relativeStandardDeviationFloor the lower bound, as a fraction of the measured value
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setRelativeStandardDeviationFloor(double relativeStandardDeviationFloor) {
        if (relativeStandardDeviationFloor < 0) {
            throw new IllegalArgumentException("Relative standard deviation floor must not be negative: " + relativeStandardDeviationFloor);
        }
        this.relativeStandardDeviationFloor = relativeStandardDeviationFloor;
        return this;
    }

    /**
     * A measurement is flagged once its normalized residual exceeds this multiple of its own
     * standard deviation.
     */
    public double getResidualFlaggingThreshold() {
        return residualFlaggingThreshold;
    }

    /**
     * Sets the multiple of its own standard deviation beyond which a measurement is flagged.
     *
     * <p>Throws {@link IllegalArgumentException} if the value is not positive.</p>
     *
     * @param residualFlaggingThreshold the multiple of the standard deviation beyond which a measurement is flagged
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setResidualFlaggingThreshold(double residualFlaggingThreshold) {
        if (residualFlaggingThreshold <= 0) {
            throw new IllegalArgumentException("Residual flagging threshold must be positive: " + residualFlaggingThreshold);
        }
        this.residualFlaggingThreshold = residualFlaggingThreshold;
        return this;
    }

    /**
     * Whether the estimated state is written back into the network. The result carries it either
     * way.
     */
    public boolean isWriteResultsToNetwork() {
        return writeResultsToNetwork;
    }

    /**
     * Sets whether the estimated state is written back into the network.
     *
     * @param writeResultsToNetwork whether the estimated state is written back into the network
     * @return this, so that calls can be chained
     */
    public StateEstimationParameters setWriteResultsToNetwork(boolean writeResultsToNetwork) {
        this.writeResultsToNetwork = writeResultsToNetwork;
        return this;
    }
}
