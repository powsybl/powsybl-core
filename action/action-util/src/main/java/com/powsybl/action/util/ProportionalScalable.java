/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ProportionalScalable extends AbstractCompoundScalable {

    private static void checkPercentages(List<Float> percentages, List<Scalable> scalables) {
        Objects.requireNonNull(percentages);
        Objects.requireNonNull(scalables);

        if (scalables.size() != percentages.size()) {
            throw new IllegalArgumentException("percentage and scalable list must have the same size");
        }
        double sum = percentages.stream().mapToDouble(Double::valueOf).sum();
        if (Math.abs(100 - sum) > 0.01) {
            throw new IllegalArgumentException("Sum of percentages must be equals to 100 (" + sum + ")");
        }
    }

    private final List<Float> percentages;

    ProportionalScalable(List<Float> percentages, List<Scalable> scalables) {
        super(scalables);
        checkPercentages(percentages, scalables);
        this.percentages = Objects.requireNonNull(percentages);
    }

    @Override
    public double scale(Network n, double asked) {
        Objects.requireNonNull(n);

        double done = 0;
        for (int i = 0; i < scalables.size(); i++) {
            Scalable s = scalables.get(i);
            double p = percentages.get(i);
            done += s.scale(n, p / 100 * asked);
        }
        return done;
    }

}

