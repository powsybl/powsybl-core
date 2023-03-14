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

    private final Scalable.ScalingConvention scalingConvention;

    private final boolean reconnect;

    private final boolean constantPowerFactor;

    public ScalingParameters(Scalable.ScalingConvention scalingConvention, boolean reconnect, boolean constantPowerFactor) {
        this.scalingConvention = scalingConvention;
        this.reconnect = reconnect;
        this.constantPowerFactor = constantPowerFactor;
    }

    public ScalingParameters(Scalable.ScalingConvention scalingConvention) {
        this(scalingConvention, false, false);
    }

    public ScalingParameters(boolean reconnect, boolean constantPowerFactor) {
        this(Scalable.ScalingConvention.GENERATOR, reconnect, constantPowerFactor);
    }

    /**
     * @return the scaling convention for the scaling.
     */
    public Scalable.ScalingConvention getScalingConvention() {
        return scalingConvention;
    }

    /**
     * @return a boolean indicating if the terminal of the scalable should be reconnected if it is disconnected.
     */
    public boolean isReconnect() {
        return reconnect;
    }

    /**
     * @return a boolean indicating if the scaling should be done with constant power factor.
     */
    public boolean isConstantPowerFactor() {
        return constantPowerFactor;
    }

    /**
     * @return default scaling parameters: generator convention, not reconnecting terminals and not scaling at constant
     * power factor.
     */
    public static ScalingParameters getDefault() {
        return new ScalingParameters(Scalable.ScalingConvention.GENERATOR, false, false);
    }
}
