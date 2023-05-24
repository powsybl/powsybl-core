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

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.fr>
 */
public class ScalingParameters {

    public static final String VERSION = "1.0";

    public static final Scalable.ScalingConvention DEFAULT_SCALING_CONVENTION = Scalable.ScalingConvention.GENERATOR;
    public static final boolean DEFAULT_CONSTANT_POWER_FACTOR = false;
    public static final boolean DEFAULT_RECONNECT = false;
    public static final boolean DEFAULT_ITERATIVE = false;
    public static final boolean DEFAULT_ALLOW_OUT_OF_BOUNDS_GENERATOR_TARGETP = false;

    private Scalable.ScalingConvention scalingConvention = DEFAULT_SCALING_CONVENTION;

    private boolean reconnect = DEFAULT_RECONNECT;

    private boolean constantPowerFactor = DEFAULT_CONSTANT_POWER_FACTOR;

    private boolean iterative = DEFAULT_ITERATIVE;

    private boolean allowOutOfBoundsGeneratorTargetP = DEFAULT_ALLOW_OUT_OF_BOUNDS_GENERATOR_TARGETP;

    public ScalingParameters() {
    }

    public ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor,
                             boolean iterative, boolean allowOutOfBoundsGeneratorTargetP) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
        this.iterative = iterative;
        this.allowOutOfBoundsGeneratorTargetP = allowOutOfBoundsGeneratorTargetP;
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
     * @return the iterative boolean, false by default.
     */
    public boolean isIterative() {
        return iterative;
    }

    public ScalingParameters setIterative(boolean iterative) {
        this.iterative = iterative;
        return this;
    }

    /**
     * @return a boolean indicating if the scaling allow a generator with an initial targetP outside the [min - max] range values
     */
    public boolean isAllowOutOfBoundsGeneratorTargetP() {
        return allowOutOfBoundsGeneratorTargetP;
    }

    public ScalingParameters setAllowOutOfBoundsGeneratorTargetP(boolean allowOutOfBoundsGeneratorTargetP) {
        this.allowOutOfBoundsGeneratorTargetP = allowOutOfBoundsGeneratorTargetP;
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
            scalingParameters.setIterative(config.getBooleanProperty("iterative", DEFAULT_ITERATIVE));
            scalingParameters.setAllowOutOfBoundsGeneratorTargetP(config.getBooleanProperty("allowOutOfBoundsGeneratorTargetP", DEFAULT_ALLOW_OUT_OF_BOUNDS_GENERATOR_TARGETP));

        });
        return scalingParameters;
    }
}
