/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.fr>
 */
public class ScalingParameters {

    private Scalable.ScalingConvention scalingConvention = Scalable.ScalingConvention.GENERATOR;

    private boolean reconnect;

    private boolean constantPowerFactor;

    private boolean iterative;

    public ScalingParameters() {
    }

    public ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor,
                             boolean iterative) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
        this.iterative = iterative;
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
}
