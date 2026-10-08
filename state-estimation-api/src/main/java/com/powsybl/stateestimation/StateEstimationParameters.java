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
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Objects;
import java.util.Optional;

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

    public static final String VERSION = "1.0";

    private static final String MODULE_CONFIG_NAME = "state-estimation-default-parameters";

    public enum VoltageInitMode {
        UNIFORM_VALUES, // 1 pu, 0 radian
        PREVIOUS_VALUES, // the voltages already held by the network
        DC_VALUES // angles from a DC load flow, magnitudes at 1 pu
    }

    public enum ZeroInjectionMode {
        IGNORED, // a bus with no device is treated like any other unmeasured bus
        HIGH_CONFIDENCE_MEASUREMENT, // a zero-valued measurement with a very small standard deviation
        EQUALITY_CONSTRAINT // enforced exactly, so no weight is involved
    }

    public enum BusInjectionPolicy {
        REQUIRE_ALL_METERED, // form a bus injection only where every connected device has a meter
        SUM_METERED // sum the metered devices, treating the others as zero
    }

    public enum DuplicateMeasurementPolicy {
        MERGE_INVERSE_VARIANCE, // combine into one reading weighted by the inverse of each variance
        KEEP_ALL, // every reading contributes its own row
        THROW
    }

    public enum UnobservablePolicy {
        THROW,
        SKIP_COMPONENT, // report the component as unobservable and estimate the others
        ESTIMATE_OBSERVABLE_ISLANDS // estimate each observable island of the component separately
    }

    public enum ComponentMode {
        MAIN, // the main connected component only
        ALL
    }

    public static final VoltageInitMode DEFAULT_VOLTAGE_INIT_MODE = VoltageInitMode.UNIFORM_VALUES;
    public static final ZeroInjectionMode DEFAULT_ZERO_INJECTION_MODE = ZeroInjectionMode.HIGH_CONFIDENCE_MEASUREMENT;
    public static final BusInjectionPolicy DEFAULT_BUS_INJECTION_POLICY = BusInjectionPolicy.REQUIRE_ALL_METERED;
    public static final DuplicateMeasurementPolicy DEFAULT_DUPLICATE_MEASUREMENT_POLICY = DuplicateMeasurementPolicy.MERGE_INVERSE_VARIANCE;
    public static final UnobservablePolicy DEFAULT_UNOBSERVABLE_POLICY = UnobservablePolicy.ESTIMATE_OBSERVABLE_ISLANDS;
    public static final ComponentMode DEFAULT_COMPONENT_MODE = ComponentMode.MAIN;
    public static final double DEFAULT_RELATIVE_STANDARD_DEVIATION_FLOOR = 0.0; // no floor
    public static final double DEFAULT_RESIDUAL_FLAGGING_THRESHOLD = 3.0;
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

    public static StateEstimationParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

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
            provider.getSpecificParametersClass().ifPresent(clazz -> {
                Optional<Extension<StateEstimationParameters>> extension = Optional.ofNullable(getExtension(clazz));
                extension.ifPresentOrElse(ext -> provider.updateSpecificParameters(ext, platformConfig),
                        () -> provider.loadSpecificParameters(platformConfig)
                                .ifPresent(ext -> addExtension((Class) ext.getClass(), ext)));
            });
        }
    }

    public VoltageInitMode getVoltageInitMode() {
        return voltageInitMode;
    }

    public StateEstimationParameters setVoltageInitMode(VoltageInitMode voltageInitMode) {
        this.voltageInitMode = Objects.requireNonNull(voltageInitMode);
        return this;
    }

    public ZeroInjectionMode getZeroInjectionMode() {
        return zeroInjectionMode;
    }

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

    public StateEstimationParameters setBusInjectionPolicy(BusInjectionPolicy busInjectionPolicy) {
        this.busInjectionPolicy = Objects.requireNonNull(busInjectionPolicy);
        return this;
    }

    public DuplicateMeasurementPolicy getDuplicateMeasurementPolicy() {
        return duplicateMeasurementPolicy;
    }

    public StateEstimationParameters setDuplicateMeasurementPolicy(DuplicateMeasurementPolicy duplicateMeasurementPolicy) {
        this.duplicateMeasurementPolicy = Objects.requireNonNull(duplicateMeasurementPolicy);
        return this;
    }

    public UnobservablePolicy getUnobservablePolicy() {
        return unobservablePolicy;
    }

    public StateEstimationParameters setUnobservablePolicy(UnobservablePolicy unobservablePolicy) {
        this.unobservablePolicy = Objects.requireNonNull(unobservablePolicy);
        return this;
    }

    public ComponentMode getComponentMode() {
        return componentMode;
    }

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

    public StateEstimationParameters setWriteResultsToNetwork(boolean writeResultsToNetwork) {
        this.writeResultsToNetwork = writeResultsToNetwork;
        return this;
    }
}
