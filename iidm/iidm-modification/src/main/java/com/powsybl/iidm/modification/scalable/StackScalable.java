/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.stream.Collectors;

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
        scalableActivityMap = scalables.stream().collect(Collectors.toMap(scalable -> scalable, scalable -> true, (first, second) -> first));
    }

    @Override
    Collection<Scalable> getScalables() {
        return scalables;
    }

    @Override
    public AbstractCompoundScalable shallowCopy() {
        StackScalable stackScalable = new StackScalable(new ArrayList<>(scalables));
        stackScalable.deactivateScalables(scalableActivityMap.keySet().stream().filter(scalable -> !scalableActivityMap.get(scalable)).collect(Collectors.toSet()));
        return stackScalable;
    }

    @Override
    public double scale(Network n, double asked, ScalingConvention scalingConvention) {
        Objects.requireNonNull(n);

        double done = 0;
        double remaining = asked;
        for (Scalable scalable : scalables) {
            if (Math.abs(remaining) > EPSILON && Boolean.TRUE.equals(scalableActivityMap.get(scalable))) {
                double v = scalable.scale(n, remaining, scalingConvention);
                done += v;
                remaining -= v;
            }
        }
        return done;
    }
}
