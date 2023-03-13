/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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


    /**
     * @deprecated listGenerators should be replaced by filterInjections
     */
    @Deprecated
    public List<Generator> listGenerators(Network n, List<String> notFoundGenerators) {
        List<Generator> generators = new ArrayList<>();
        listGenerators(n, generators, notFoundGenerators);
        return generators;
    }

    /**
     * @deprecated listGenerators should be replaced by filterInjections
     */
    @Deprecated
    public List<Generator> listGenerators(Network n) {
        return listGenerators(n, null);
    }

    /**
     * @deprecated listGenerators should be replaced by filterInjections
     */
    @Deprecated
    public void listGenerators(Network network, List<Generator> generators, List<String> notFoundGenerators) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(generators);

        List<Injection> injections = filterInjections(network, notFoundGenerators);

        for (Injection injection : injections) {

            if (injection instanceof Generator) {
                generators.add((Generator) injection);
            } else {
                if (notFoundGenerators != null) {
                    notFoundGenerators.add(injection.getId());
                }
            }
        }
    }

    @Override
    public double maximumValue(Network n) {
        return maximumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double minimumValue(Network n) {
        return minimumValue(n, ScalingConvention.GENERATOR);
    }

    @Override
    public double scale(Network n, double asked) {
        return scale(n, asked, ScalingConvention.GENERATOR);
    }

}
