/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractScalable implements Scalable {

    protected AbstractScalable() {
    }

    @Override
    public List<Injection> filterInjections(Network network, List<String> notFound) {
        List<Injection> injections = new ArrayList<>();
        filterInjections(network, injections, notFound);
        return injections;
    }

    @Override
    public List<Injection> filterInjections(Network network) {
        return filterInjections(network, null);
    }

    @Override
    public double maximumValue(Network n) {
        return maximumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double minimumValue(Network n) {
        return minimumValue(n, ScalingConvention.GENERATOR);
    }

}
