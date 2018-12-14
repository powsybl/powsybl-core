/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public abstract class AbstractNetworkReducer implements NetworkReducer {

    private final NetworkFilter filter;

    public AbstractNetworkReducer(NetworkFilter filter) {
        this.filter = Objects.requireNonNull(filter);
    }

    public final void reduce(Network network) {
        // Remove all unwanted lines
        List<Line> lines = network.getLineStream()
                .filter(l -> !filter.accept(l))
                .collect(Collectors.toList());
        lines.forEach(this::reduce);

        // Remove all unwanted two windings transformers
        List<TwoWindingsTransformer> twoWindingsTransformers = network.getTwoWindingsTransformerStream()
                .filter(t -> !filter.accept(t))
                .collect(Collectors.toList());
        twoWindingsTransformers.forEach(this::reduce);

        // Remove all three windings transformers
        List<ThreeWindingsTransformer> threeWindingsTransformers = network.getThreeWindingsTransformerStream()
                .filter(t -> !filter.accept(t))
                .collect(Collectors.toList());
        threeWindingsTransformers.forEach(this::reduce);

        // Remove all unwanted HVDC lines
        List<HvdcLine> hvdcLines = network.getHvdcLineStream()
                .filter(h -> !filter.accept(h))
                .collect(Collectors.toList());
        hvdcLines.forEach(this::reduce);

        // Remove all unwanted voltage levels
        List<VoltageLevel> voltageLevels = network.getVoltageLevelStream()
                .filter(vl -> !filter.accept(vl))
                .collect(Collectors.toList());
        voltageLevels.forEach(this::reduce);

        // Remove all unwanted substations
        List<Substation> substations = network.getSubstationStream()
                .filter(s -> !filter.accept(s))
                .collect(Collectors.toList());
        substations.forEach(this::reduce);
    }

    protected final NetworkFilter getFilter() {
        return filter;
    }

    protected abstract void reduce(Substation substation);

    protected abstract void reduce(VoltageLevel voltageLevel);

    protected abstract void reduce(Line line);

    protected abstract void reduce(TwoWindingsTransformer transformer);

    protected abstract void reduce(ThreeWindingsTransformer transformer);

    protected abstract void reduce(HvdcLine hvdcLine);
}
