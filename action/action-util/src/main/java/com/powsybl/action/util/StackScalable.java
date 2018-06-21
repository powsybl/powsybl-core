/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StackScalable extends AbstractCompoundScalable {

    StackScalable(Scalable... scalables) {
        this(Arrays.asList(scalables));
    }

    StackScalable(List<Scalable> scalables) {
        super(scalables);
    }

    @Override
    public double scale(Network n, double asked) {
        Objects.requireNonNull(n);

        double done = 0;
        double remaining = asked;
        for (Scalable scalable : scalables) {
            if (remaining > 0) {
                double v = scalable.scale(n, remaining);
                done += v;
                remaining -= v;
            }
        }
        return done;
    }

}
