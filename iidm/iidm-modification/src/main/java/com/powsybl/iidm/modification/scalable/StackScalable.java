/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StackScalable extends AbstractCompoundScalable {
    private static final double EPSILON = 1e-5;

    private final List<Scalable> scalables;

    StackScalable(Scalable... scalables) {
        this(Arrays.asList(scalables));
    }

    StackScalable(List<Scalable> scalables) {
        this.scalables = Objects.requireNonNull(scalables);
    }

    @Override
    Collection<Scalable> getScalables() {
        return scalables;
    }

    @Override
    public double scale(Network n, double asked, ScalingParameters parameters) {
        Objects.requireNonNull(n);

        // Compute the current power value
        double currentGlobalPower = getCurrentPower(n, parameters.getScalingConvention());

        // Variation asked
        double variationAsked = Scalable.getVariationAsked(parameters, asked, currentGlobalPower);

        double done = 0;
        double remaining = variationAsked;
        for (Scalable scalable : scalables) {
            if (Math.abs(remaining) > EPSILON) {
                double v = scalable.scale(n, remaining, parameters);
                done += v;
                remaining -= v;
            }
        }
        return done;
    }

    @Override
    public double getCurrentPower(Network network, ScalingConvention scalingConvention) {
        return scalables.stream().mapToDouble(scalable -> scalable.getCurrentPower(network, scalingConvention)).sum();
    }
}
