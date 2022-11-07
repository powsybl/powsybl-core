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
        this.upScalable = Objects.requireNonNull(upScalable);
        this.downScalable = Objects.requireNonNull(downScalable);
        scalableActivityMap = new HashMap<>();
        scalableActivityMap.put(upScalable, true);
        scalableActivityMap.put(downScalable, true);
    }

    @Override
    public double initialValue(Network n) {
        double initialValue = 0;
        if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
            initialValue += upScalable.initialValue(n);
        }
        if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
            initialValue += downScalable.initialValue(n);
        }
        return initialValue;
    }

    @Override
    public void reset(Network n) {
        upScalable.reset(n);
        downScalable.reset(n);
    }

    @Override
    public double maximumValue(Network n, ScalingConvention scalingConvention) {
        double upScalableMax = 0;
        double upScalableInitial = 0;
        double downScalableMax = 0;
        double downScalableInitial = 0;

        if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
            upScalableMax = upScalable.maximumValue(n, scalingConvention);
            upScalableInitial = upScalable.initialValue(n);
        }
        if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
            downScalableMax = downScalable.maximumValue(n, scalingConvention);
            downScalableInitial = downScalable.initialValue(n);
        }

        if (scalingConvention == ScalingConvention.LOAD) {
            return downScalableMax - upScalableInitial;
        } else {
            return upScalableMax + downScalableInitial;
        }
    }

    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        double upScalableMin = 0;
        double upScalableInitial = 0;
        double downScalableMin = 0;
        double downScalableInitial = 0;

        if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
            upScalableMin = upScalable.minimumValue(n, scalingConvention);
            upScalableInitial = upScalable.initialValue(n);
        }
        if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
            downScalableMin = downScalable.minimumValue(n, scalingConvention);
            downScalableInitial = downScalable.initialValue(n);
        }

        if (scalingConvention == ScalingConvention.LOAD) {
            return upScalableMin - downScalableInitial;
        } else {
            return downScalableMin + upScalableInitial;
        }
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        if (asked > 0) {
            if (Boolean.TRUE.equals(scalableActivityMap.get(upScalable))) {
                return upScalable.scale(n, asked, scalingConvention);
            } else {
                return 0;
            }
        } else {
            if (Boolean.TRUE.equals(scalableActivityMap.get(downScalable))) {
                return downScalable.scale(n, asked, scalingConvention);
            } else {
                return 0;
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

        return new UpDownScalable(upScalableCopy, downScalableCopy);
    }
}
