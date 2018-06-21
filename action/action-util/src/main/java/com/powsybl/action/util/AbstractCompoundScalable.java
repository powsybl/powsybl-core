/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractCompoundScalable extends AbstractScalable {

    protected final List<Scalable> scalables;

    protected AbstractCompoundScalable(List<Scalable> scalables) {
        this.scalables = Objects.requireNonNull(scalables);
    }

    @Override
    public double initialValue(Network n) {
        Objects.requireNonNull(n);

        double value = 0;
        for (Scalable scalable : scalables) {
            value += scalable.initialValue(n);
        }
        return value;
    }

    @Override
    public void reset(Network n) {
        Objects.requireNonNull(n);

        scalables.forEach(scalable -> scalable.reset(n));
    }

    @Override
    public double maximumValue(Network n) {
        Objects.requireNonNull(n);

        double value = 0;
        for (Scalable scalable : scalables) {
            value += scalable.maximumValue(n);
        }
        return value;
    }

    @Override
    public void listGenerators(Network n, List<Generator> generators, List<String> notFoundGenerators) {
        Objects.requireNonNull(n);
        Objects.requireNonNull(generators);

        for (Scalable scalable : scalables) {
            scalable.listGenerators(n, generators, notFoundGenerators);
        }
    }
}
