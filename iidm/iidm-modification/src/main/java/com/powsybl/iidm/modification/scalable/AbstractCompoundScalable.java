/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractCompoundScalable extends AbstractScalable {

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
        return value;
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
        return value;
    }

    @Override
    public double scale(Network n, double asked) {
        return scale(n, asked, ScalingConvention.GENERATOR);
    }

    @Override
    public void filterInjections(Network n, List<Injection> injections, List<String> notFoundInjections) {
        for (Scalable scalable : getScalables()) {
            scalable.filterInjections(n, injections, notFoundInjections);
        }
    }

}
