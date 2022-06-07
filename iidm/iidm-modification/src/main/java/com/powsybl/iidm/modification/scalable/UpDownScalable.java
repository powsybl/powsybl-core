/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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

    public UpDownScalable(Scalable upScalable, Scalable downScalable) {
        this.upScalable = Objects.requireNonNull(upScalable);
        this.downScalable = Objects.requireNonNull(downScalable);
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
            return downScalable.maximumValue(n, scalingConvention) - upScalable.initialValue(n);
        } else {
            return upScalable.maximumValue(n, scalingConvention) + downScalable.initialValue(n);
        }
    }

    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        if (scalingConvention == ScalingConvention.LOAD) {
            return upScalable.minimumValue(n, scalingConvention) - downScalable.initialValue(n);
        } else {
            return downScalable.minimumValue(n, scalingConvention) + upScalable.initialValue(n);
        }
    }

    @Override
    public void filterInjections(Network network, List<Injection> injections, List<String> notFound) {
        upScalable.filterInjections(network, injections, notFound);
        downScalable.filterInjections(network, injections, notFound);
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        return asked > 0 ? upScalable.scale(n, asked, scalingConvention) : downScalable.scale(n, asked, scalingConvention);
    }
}
