/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;

class ScalableAdapter extends AbstractScalable {

    private final String id;

    public ScalableAdapter(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public ScalableAdapter(Injection<?> injection) {
        Objects.requireNonNull(injection);
        this.id = injection.getId();
    }

    private Scalable getScalable(Network n) {
        Objects.requireNonNull(n);
        Identifiable<?> identifiable = n.getIdentifiable(id);
        if (identifiable instanceof Generator) {
            return new GeneratorScalable(id);
        } else if (identifiable instanceof Load) {
            return new LoadScalable(id);
        } else if (identifiable instanceof DanglingLine) {
            return new DanglingLineScalable(id);
        } else {
            throw new PowsyblException("Unable to create a scalable from " + identifiable.getClass());
        }
    }

    @Override
    public double initialValue(Network n) {
        return getScalable(n).initialValue(n);
    }

    @Override
    public void reset(Network n) {
        getScalable(n).reset(n);
    }

    @Override
    public double maximumValue(Network n, ScalingConvention scalingConvention) {
        return getScalable(n).maximumValue(n, scalingConvention);
    }

    @Override
    public double minimumValue(Network n, ScalingConvention scalingConvention) {
        return getScalable(n).minimumValue(n, scalingConvention);
    }

    @Override
    public void filterInjections(Network network, List<Injection> injections, List<String> notFound) {
        getScalable(network).filterInjections(network, injections, notFound);
    }

    @Override
    public double scale(Network n, double asked, ScalingParameters parameters) {
        return getScalable(n).scale(n, asked, parameters);
    }

    /**
     * Compute the percentage of asked power available for the scale. It takes into account the scaling convention
     * specified by the user and the sign of the asked power.
     *
     * @param network Network on which the scaling is done
     * @param asked Asked power (can be positive or negative)
     * @param scalingPercentage Percentage of the asked power that shall be distributed to the current injection
     * @param scalingConvention Scaling convention (GENERATOR or LOAD)
     * @return the percentage of asked power available for the scale on the current injection
     */
    double availablePowerInPercentageOfAsked(Network network, double asked, double scalingPercentage, ScalingConvention scalingConvention) {
        Objects.requireNonNull(network);
        if (getScalable(network) instanceof GeneratorScalable generatorScalable) {
            return generatorScalable.availablePowerInPercentageOfAsked(network, asked, scalingPercentage, scalingConvention);
        } else {
            Identifiable<?> identifiable = network.getIdentifiable(id);
            throw new PowsyblException(String.format("RESPECT_OF_DISTRIBUTION mode can only be used with a Generator, not %s",
                identifiable.getClass()));
        }
    }

    @Override
    public double getSteadyStatePower(Network network, double asked, ScalingConvention scalingConvention) {
        return getScalable(network).getSteadyStatePower(network, asked, scalingConvention);
    }
}
