/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.config.PlatformConfig;

import java.util.Objects;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.ONESHOT;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.VOLUME;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.DistributionMode.REGULAR_DISTRIBUTION;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.fr>
 */
public class ScalingParameters {

    public static final String VERSION = "1.1";

    public enum DistributionMode {
        PROPORTIONAL_TO_TARGETP,
        PROPORTIONAL_TO_PMAX,
        PROPORTIONAL_TO_DIFF_PMAX_TARGETP,
        PROPORTIONAL_TO_DIFF_TARGETP_PMIN,
        PROPORTIONAL_TO_P0,
        REGULAR_DISTRIBUTION,
        STACKING_UP
    }

    public enum ScalingType {
        DELTA_P,
        TARGET_P
    }

    public enum Priority {
        VOLUME,
        VENTILATION,
        ONESHOT
    }

    public static final Scalable.ScalingConvention DEFAULT_SCALING_CONVENTION = Scalable.ScalingConvention.GENERATOR;
    public static final boolean DEFAULT_CONSTANT_POWER_FACTOR = false;
    public static final boolean DEFAULT_RECONNECT = false;
    public static final boolean DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS = false;
    public static final Priority DEFAULT_PRIORITY = ONESHOT;
    public static final DistributionMode DEFAULT_DISTRIBUTION_MODE = REGULAR_DISTRIBUTION;
    public static final ScalingType DEFAULT_SCALING_TYPE = DELTA_P;

    private Scalable.ScalingConvention scalingConvention = DEFAULT_SCALING_CONVENTION;

    private boolean reconnect = DEFAULT_RECONNECT;

    private boolean constantPowerFactor = DEFAULT_CONSTANT_POWER_FACTOR;

    private boolean allowsGeneratorOutOfActivePowerLimits = DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS;

    private DistributionMode distributionMode = DEFAULT_DISTRIBUTION_MODE;
    private ScalingType scalingType = DEFAULT_SCALING_TYPE;
    private double scalingValue = 0.0;
    private Priority priority = DEFAULT_PRIORITY;

    public ScalingParameters() {
    }

    /**
     * @deprecated : replace with ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor,
     *                              Priority priority, boolean allowsGeneratorOutOfActivePowerLimits)
     */
    @Deprecated(since = "v6.0.0")
    public ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor,
                             boolean iterative, boolean allowsGeneratorOutOfActivePowerLimits) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
        this.priority = iterative ? VOLUME : ONESHOT;
        this.allowsGeneratorOutOfActivePowerLimits = allowsGeneratorOutOfActivePowerLimits;
    }

    public ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor,
                             Priority priority, boolean allowsGeneratorOutOfActivePowerLimits) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
        this.priority = priority;
        this.allowsGeneratorOutOfActivePowerLimits = allowsGeneratorOutOfActivePowerLimits;
    }

    public ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor,
                             Priority priority, boolean allowsGeneratorOutOfActivePowerLimits,
                             DistributionMode distributionMode, ScalingType scalingType,
                             double scalingValue) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
        this.priority = priority;
        this.allowsGeneratorOutOfActivePowerLimits = allowsGeneratorOutOfActivePowerLimits;
        this.distributionMode = distributionMode;
        this.scalingType = scalingType;
        this.scalingValue = scalingValue;
    }

    /**
     * @return the scaling convention for the scaling, {@link Scalable.ScalingConvention} GENERATOR by default.
     */
    public Scalable.ScalingConvention getScalingConvention() {
        return scalingConvention;
    }

    public ScalingParameters setScalingConvention(Scalable.ScalingConvention scalingConvention) {
        this.scalingConvention = scalingConvention;
        return this;
    }

    /**
     * @return a boolean indicating if the terminal of the scalable should be reconnected if it is disconnected.
     * If the scalable is disconnected, then it will not be scaled.
     */
    public boolean isReconnect() {
        return reconnect;
    }

    public ScalingParameters setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
        return this;
    }

    /**
     * @return a boolean indicating if the scaling should be done with a constant power factor.
     */
    public boolean isConstantPowerFactor() {
        return constantPowerFactor;
    }

    public ScalingParameters setConstantPowerFactor(boolean constantPowerFactor) {
        this.constantPowerFactor = constantPowerFactor;
        return this;
    }

    /**
     * Scale may be iterative or not for {@link ProportionalScalable}. If the iterative mode is activated, the residues
     * due to scalable saturation is divided between the other scalable composing the {@link ProportionalScalable}.
     * @deprecated : replace with method "getPriority"
     * @return the iterative boolean, false by default.
     */
    @Deprecated(since = "v6.0.0")
    public boolean isIterative() {
        return priority == VOLUME;
    }

    /**
     * @deprecated : replace with the method "setPriority"
     */
    @Deprecated(since = "v6.0.0")
    public ScalingParameters setIterative(boolean iterative) {
        return iterative ? setPriority(VOLUME) : setPriority(ONESHOT);
    }

    /**
     * @return a boolean indicating if the scaling allows generators with an initial targetP outside the [Pmin - Pmax] range values
     */
    public boolean isAllowsGeneratorOutOfActivePowerLimits() {
        return allowsGeneratorOutOfActivePowerLimits;
    }

    public ScalingParameters setAllowsGeneratorOutOfActivePowerLimits(boolean allowsGeneratorOutOfActivePowerLimits) {
        this.allowsGeneratorOutOfActivePowerLimits = allowsGeneratorOutOfActivePowerLimits;
        return this;
    }

    /**
     * @return the mode of distribution used to allocate the power to the different elements (loads, generators, etc.)
     */
    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public ScalingParameters setDistributionMode(DistributionMode distributionMode) {
        this.distributionMode = distributionMode;
        return this;
    }

    /**
     * @return the type of scaling asked (DELTA_P or TARGET_P)
     */
    public ScalingType getScalingType() {
        return scalingType;
    }

    public ScalingParameters setScalingType(ScalingType scalingType) {
        this.scalingType = scalingType;
        return this;
    }

    /**
     * @return the power value configured for the scaling.
     */
    public Double getScalingValue() {
        return scalingValue;
    }

    public ScalingParameters setScalingValue(double scalingValue) {
        this.scalingValue = scalingValue;
        return this;
    }

    /**
     * @return an enum representing the priority of the scaling. It can be either VOLUME (the scaling will distribute the
     * power asked as much as possible by iterating if elements get saturated, even if it means not respecting potential
     * percentages), VENTILATION (the scaling will respect the percentages even if it means not scaling all what is
     * asked), or ONESHOT (the scaling will distribute the power asked as is, in one iteration even if elements get
     * saturated and even if it means not respecting potential percentages).
     */
    public Priority getPriority() {
        return priority;
    }

    public ScalingParameters setPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public static ScalingParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ScalingParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        ScalingParameters scalingParameters = new ScalingParameters();
        platformConfig.getOptionalModuleConfig("scaling-default-parameters").ifPresent(config -> {
            scalingParameters.setScalingConvention(config.getEnumProperty("scalingConvention", Scalable.ScalingConvention.class, DEFAULT_SCALING_CONVENTION));
            scalingParameters.setConstantPowerFactor(config.getBooleanProperty("constantPowerFactor", DEFAULT_CONSTANT_POWER_FACTOR));
            scalingParameters.setReconnect(config.getBooleanProperty("reconnect", DEFAULT_RECONNECT));
            scalingParameters.setPriority(config.getEnumProperty("priority", ScalingParameters.Priority.class, DEFAULT_PRIORITY));
            scalingParameters.setAllowsGeneratorOutOfActivePowerLimits(config.getBooleanProperty("allowsGeneratorOutOfActivePowerLimits", DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS));
        });
        return scalingParameters;
    }
}
