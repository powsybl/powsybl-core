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

    protected double initialInjection;

    protected final double minInjection;

    protected final double maxInjection;

    protected AbstractScalable(double minInjection, double maxInjection, ScalingConvention scalingConvention) {
        if (scalingConvention == ScalingConvention.LOAD) {
            this.minInjection = -maxInjection;
            this.maxInjection = -minInjection;
        } else {
            this.minInjection = minInjection;
            this.maxInjection = maxInjection;
        }
    }

    @Override
    public double getInitialInjection(ScalingConvention scalingConvention) {
        return scalingConvention.equals(ScalingConvention.GENERATOR) ? initialInjection : -initialInjection;
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
    public double getMaximumExtraScaling(Network n, ScalingConvention scalingConvention) {
        return getMaximumInjection(n, scalingConvention) - getCurrentInjection(n, scalingConvention);
    }

    @Override
    public double getMinimumExtraScaling(Network n, ScalingConvention scalingConvention) {
        return getMinimumInjection(n, scalingConvention) - getCurrentInjection(n, scalingConvention);
    }

    @Override
    public void reset(Network n) {
        scale(n, initialInjection - getCurrentInjection(n, ScalingConvention.GENERATOR), ScalingConvention.GENERATOR);
    }
}
