/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class UpDownScalable extends AbstractCompoundScalable {
    private final Scalable upScalable;
    private final Scalable downScalable;

    public UpDownScalable(Scalable upScalable, Scalable downScalable) {
        super(-Double.MAX_VALUE, Double.MAX_VALUE, ScalingConvention.GENERATOR);
        this.upScalable = Objects.requireNonNull(upScalable);
        this.downScalable = Objects.requireNonNull(downScalable);
        scalableActivityMap = new HashMap<>();
        scalableActivityMap.put(upScalable, true);
        scalableActivityMap.put(downScalable, true);
    }

    @Override
    public double getMaximumInjection(Network n, ScalingConvention scalingConvention) {
        double upScalableMax;
        double downScalableMax;

        if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
            upScalableMax = upScalable.getMaximumInjection(n, scalingConvention);
        } else {
            upScalableMax = upScalable.getCurrentInjection(n, scalingConvention);
        }
        if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
            downScalableMax = downScalable.getInitialInjection(scalingConvention);
        } else {
            downScalableMax = downScalable.getMaximumInjection(n, scalingConvention);
        }

        return upScalableMax + downScalableMax;
    }

    @Override
    public double getMinimumInjection(Network n, ScalingConvention scalingConvention) {
        double upScalableMin;
        double downScalableMin;

        if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
            upScalableMin = upScalable.getInitialInjection(scalingConvention);
        } else {
            upScalableMin = upScalable.getCurrentInjection(n, scalingConvention);
        }
        if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
            downScalableMin = downScalable.getMinimumInjection(n, scalingConvention);
        } else {
            downScalableMin = downScalable.getCurrentInjection(n, scalingConvention);
        }

        return downScalableMin + upScalableMin;
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        double done = 0;
        double newAsked = asked;
        if (newAsked > 0) {
            if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
                done += downScalable.scale(n, Math.max(0., Math.min(newAsked, downScalable.getInitialInjection(scalingConvention) - downScalable.getCurrentInjection(n, scalingConvention))), scalingConvention);
                newAsked = newAsked - done;
            }
            if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
                done += upScalable.scale(n, newAsked, scalingConvention);
                return done;
            } else {
                return done;
            }
        } else {
            if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
                done += upScalable.scale(n, Math.min(0., Math.max(newAsked, upScalable.getInitialInjection(scalingConvention) - upScalable.getCurrentInjection(n, scalingConvention))), scalingConvention);
                newAsked = newAsked - done;
            }
            if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
                done += downScalable.scale(n, newAsked, scalingConvention);
                return done;
            } else {
                return done;
            }
        }
    }

    @Override
    public CompoundScalable shallowCopy() {
        Scalable upScalableCopy;
        if (upScalable instanceof CompoundScalable) {
            upScalableCopy = ((CompoundScalable) upScalable).shallowCopy();
        } else {
            upScalableCopy = upScalable;
        }

        Scalable downScalableCopy;
        if (downScalable instanceof CompoundScalable) {
            downScalableCopy = ((CompoundScalable) downScalable).shallowCopy();
        } else {
            downScalableCopy = downScalable;
        }
        UpDownScalable upDownScalableCopy = new UpDownScalable(upScalableCopy, downScalableCopy);
        if (Boolean.FALSE.equals(scalableActivityMap.get(upScalable))) {
            upDownScalableCopy.deactivateScalables(Set.of(upScalableCopy));
        }
        if (Boolean.FALSE.equals(scalableActivityMap.get(downScalable))) {
            upDownScalableCopy.deactivateScalables(Set.of(downScalableCopy));
        }
        return upDownScalableCopy;
    }
}
