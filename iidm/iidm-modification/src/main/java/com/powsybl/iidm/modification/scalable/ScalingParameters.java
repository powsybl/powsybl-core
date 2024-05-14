/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.config.PlatformConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.ONESHOT;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.ScalingType.DELTA_P;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class ScalingParameters {

    public static final String VERSION = "1.1";

    public enum ScalingType {
        DELTA_P,
        TARGET_P
    }

    public enum Priority {
        RESPECT_OF_VOLUME_ASKED,
        RESPECT_OF_DISTRIBUTION,
        ONESHOT
    }

    public static final Scalable.ScalingConvention DEFAULT_SCALING_CONVENTION = Scalable.ScalingConvention.GENERATOR;
    public static final boolean DEFAULT_CONSTANT_POWER_FACTOR = false;
    public static final boolean DEFAULT_RECONNECT = false;
    public static final boolean DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS = false;
    public static final Priority DEFAULT_PRIORITY = ONESHOT;
    public static final ScalingType DEFAULT_SCALING_TYPE = DELTA_P;
    public static final Set<String> DEFAULT_IGNORED_INJECTION_IDS = Collections.emptySet();

    private Scalable.ScalingConvention scalingConvention = DEFAULT_SCALING_CONVENTION;
    private boolean reconnect = DEFAULT_RECONNECT;
    private boolean constantPowerFactor = DEFAULT_CONSTANT_POWER_FACTOR;
    private boolean allowsGeneratorOutOfActivePowerLimits = DEFAULT_ALLOWS_GENERATOR_OUT_OF_ACTIVE_POWER_LIMITS;
    private ScalingType scalingType = DEFAULT_SCALING_TYPE;
    private Priority priority = DEFAULT_PRIORITY;
    private Set<String> ignoredInjectionIds = DEFAULT_IGNORED_INJECTION_IDS;

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
        this.priority = iterative ? RESPECT_OF_VOLUME_ASKED : ONESHOT;
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
                             Priority priority, boolean allowsGeneratorOutOfActivePowerLimits, ScalingType scalingType) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
        this.priority = priority;
        this.allowsGeneratorOutOfActivePowerLimits = allowsGeneratorOutOfActivePowerLimits;
        this.scalingType = scalingType;
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
        return priority == RESPECT_OF_VOLUME_ASKED;
    }

    /**
     * @deprecated : replace with the method "setPriority"
     */
    @Deprecated(since = "v6.0.0")
    public ScalingParameters setIterative(boolean iterative) {
        return iterative ? setPriority(RESPECT_OF_VOLUME_ASKED) : setPriority(ONESHOT);
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
     * @return an enum representing the priority of the scaling. It can be either RESPECT_OF_VOLUME_ASKED (the scaling
     * will distribute the power asked as much as possible by iterating if elements get saturated, even if it means not
     * respecting potential percentages), RESPECT_OF_DISTRIBUTION (the scaling will respect the percentages even if it
     * means not scaling all what is asked), or ONESHOT (the scaling will distribute the power asked as is, in one
     * iteration even if elements get saturated and even if it means not respecting potential percentages).
     */
    public Priority getPriority() {
        return priority;
    }

    public ScalingParameters setPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public Set<String> getIgnoredInjectionIds() {
        return ignoredInjectionIds;
    }

    public ScalingParameters setIgnoredInjectionIds(Set<String> ignoredInjectionIds) {
        this.ignoredInjectionIds = new HashSet<>(ignoredInjectionIds);
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
