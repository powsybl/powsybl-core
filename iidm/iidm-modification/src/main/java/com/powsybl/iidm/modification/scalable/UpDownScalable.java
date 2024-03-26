/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class UpDownScalable extends AbstractScalable {
    private final Scalable upScalable;
    private final Scalable downScalable;
    private final double minValue;
    private final double maxValue;

    public UpDownScalable(Scalable upScalable, Scalable downScalable) {
        this(upScalable, downScalable, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public UpDownScalable(Scalable upScalable, Scalable downScalable, double minValue, double maxValue) {
        this.upScalable = Objects.requireNonNull(upScalable);
        this.downScalable = Objects.requireNonNull(downScalable);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public double initialValue(Network n) {
        return upScalable.initialValue(n) + downScalable.initialValue(n);
    }

    @Override
    public void reset(Network n) {
        upScalable.reset(n);
        downScalable.reset(n);
    }

    @Override
    public double maximumValue(Network n, ScalingConvention scalingConvention) {
        if (scalingConvention == ScalingConvention.LOAD) {
            return Math.min(downScalable.maximumValue(n, scalingConvention) - upScalable.initialValue(n), maxValue);
        } else {
            return Math.min(upScalable.maximumValue(n, scalingConvention) + downScalable.initialValue(n), maxValue);
        }
    }

    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        if (scalingConvention == ScalingConvention.LOAD) {
            return Math.max(upScalable.minimumValue(n, scalingConvention) - downScalable.initialValue(n), minValue);
        } else {
            return Math.max(downScalable.minimumValue(n, scalingConvention) + upScalable.initialValue(n), minValue);
        }
    }

    @Override
    public void filterInjections(Network network, List<Injection> injections, List<String> notFound) {
        upScalable.filterInjections(network, injections, notFound);
        downScalable.filterInjections(network, injections, notFound);
    }

    @Override
    public double scale(Network n, double asked, ScalingParameters parameters) {
        double minWithConvention = parameters.getScalingConvention() == ScalingConvention.GENERATOR ? minValue : -maxValue;
        double maxWithConvention = parameters.getScalingConvention() == ScalingConvention.GENERATOR ? maxValue : -minValue;
        double boundedAsked = asked > 0 ?
            Math.min(asked, maxWithConvention - getSteadyStatePower(n, asked, parameters.getScalingConvention())) :
            Math.max(asked, minWithConvention - getSteadyStatePower(n, asked, parameters.getScalingConvention()));
        return asked > 0 ? upScalable.scale(n, boundedAsked, parameters) : downScalable.scale(n, boundedAsked, parameters);
    }

    @Override
    public double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention) {
        return asked > 0 ? upScalable.getSteadyStatePower(network, asked, scalingConvention) : downScalable.getSteadyStatePower(network, asked, scalingConvention);
    }
}
