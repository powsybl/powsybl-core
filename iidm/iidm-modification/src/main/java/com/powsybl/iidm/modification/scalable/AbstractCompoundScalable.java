/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractCompoundScalable extends AbstractScalable {
    protected double minValue = -Double.MAX_VALUE;
    protected double maxValue = Double.MAX_VALUE;

    abstract Collection<Scalable> getScalables();

    @Override
    public double initialValue(Network n) {
        Objects.requireNonNull(n);

        double value = 0;
        for (Scalable scalable : getScalables()) {
            value += scalable.initialValue(n);
        }
        return value;
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        getScalables().forEach(scalable -> scalable.reset(n));
    }

    @Override
    public double maximumValue(Network n) {
        return maximumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double maximumValue(Network n, ScalingConvention powerConvention) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(powerConvention);

        double value = 0;
        for (Scalable scalable : getScalables()) {
            value += scalable.maximumValue(n, powerConvention);
        }
        return (powerConvention == ScalingConvention.GENERATOR) ? Math.min(maxValue, value) : Math.min(-minValue, value);
    }

    @Override
    public double minimumValue(Network n) {
        return minimumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double minimumValue(Network n, ScalingConvention powerConvention) {
        Objects.requireNonNull(n);

        double value = 0;
        for (Scalable scalable : getScalables()) {
            value += scalable.minimumValue(n, powerConvention);
        }
        return (powerConvention == ScalingConvention.GENERATOR) ? Math.max(minValue, value) : Math.max(-maxValue, value);
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        for (Scalable scalable : getScalables()) {
            scalable.filterInjections(n, injections, notFoundInjections);
        }
    }

    /**
     * Returns the value of scaling asked, bounded by the minValue and maxValue.
     * @param variationAsked unbounded value of scaling asked on the scalable
     * @param currentGlobalPower current global power in the network
     * @param scalingConvention This is required because the minValue and maxValue are in GENERATOR convention, so we need to know wwhat convention we use for the scaling.
     * @return the value of scaling asked bounded by the minValue and maxValue, according to the scalingConvention.
     */
    protected double getBoundedVariation(double variationAsked, double currentGlobalPower, ScalingConvention scalingConvention) {
        double minWithConvention = scalingConvention == ScalingConvention.GENERATOR ? minValue : -maxValue;
        double maxWithConvention = scalingConvention == ScalingConvention.GENERATOR ? maxValue : -minValue;
        return Math.min(maxWithConvention - currentGlobalPower, Math.max(minWithConvention - currentGlobalPower, variationAsked));
    }
}
