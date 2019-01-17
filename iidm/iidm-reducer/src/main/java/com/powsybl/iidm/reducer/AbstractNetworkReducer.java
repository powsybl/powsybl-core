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

    private final NetworkPredicate predicate;

    public AbstractNetworkReducer(NetworkPredicate predicate) {
        this.predicate = Objects.requireNonNull(predicate);
    }

    public final void reduce(Network network) {
        // Remove all unwanted lines
        List<Line> lines = network.getLineStream()
                .filter(l -> !test(l))
                .collect(Collectors.toList());
        lines.forEach(this::reduce);

        // Remove all unwanted two windings transformers
        List<TwoWindingsTransformer> twoWindingsTransformers = network.getTwoWindingsTransformerStream()
                .filter(t -> !test(t))
                .collect(Collectors.toList());
        twoWindingsTransformers.forEach(this::reduce);

        // Remove all three windings transformers
        List<ThreeWindingsTransformer> threeWindingsTransformers = network.getThreeWindingsTransformerStream()
                .filter(t -> !test(t))
                .collect(Collectors.toList());
        threeWindingsTransformers.forEach(this::reduce);

        // Remove all unwanted HVDC lines
        List<HvdcLine> hvdcLines = network.getHvdcLineStream()
                .filter(h -> !test(h))
                .collect(Collectors.toList());
        hvdcLines.forEach(this::reduce);

        // Remove all unwanted voltage levels
        List<VoltageLevel> voltageLevels = network.getVoltageLevelStream()
                .filter(vl -> !test(vl))
                .collect(Collectors.toList());
        voltageLevels.forEach(this::reduce);

        // Remove all unwanted substations
        List<Substation> substations = network.getSubstationStream()
                .filter(s -> !test(s))
                .collect(Collectors.toList());
        substations.forEach(this::reduce);
    }

    protected final NetworkPredicate getPredicate() {
        return predicate;
    }

    protected abstract void reduce(Substation substation);

    protected abstract void reduce(VoltageLevel voltageLevel);

    protected abstract void reduce(Line line);

    protected abstract void reduce(TwoWindingsTransformer transformer);

    protected abstract void reduce(ThreeWindingsTransformer transformer);

    protected abstract void reduce(HvdcLine hvdcLine);

    protected boolean test(Substation substation) {
        return predicate.test(substation);
    }

    protected boolean test(VoltageLevel voltageLevel) {
        return predicate.test(voltageLevel);
    }

    /**
     * Return true if the given {@link Line} should be kept in the network, false otherwise
     */
    protected boolean test(Line line) {
        return test((Branch) line);
    }

    /**
     * Return true if the given {@link TwoWindingsTransformer} should be kept in the network, false otherwise
     */
    protected boolean test(TwoWindingsTransformer transformer) {
        return test((Branch) transformer);
    }

    /**
     * Return true if the given {@link Branch} should be kept in the network, false otherwise
     */
    private boolean test(Branch branch) {
        Objects.requireNonNull(branch);
        VoltageLevel vl1 = branch.getTerminal1().getVoltageLevel();
        VoltageLevel vl2 = branch.getTerminal2().getVoltageLevel();

        return test(vl1) && test(vl2);
    }

    /**
     * Return true if the given {@link ThreeWindingsTransformer} should be kept in the network, false otherwise
     */
    protected boolean test(ThreeWindingsTransformer transformer) {
        Objects.requireNonNull(transformer);
        VoltageLevel vl1 = transformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = transformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel vl3 = transformer.getLeg3().getTerminal().getVoltageLevel();

        return test(vl1) && test(vl2) && test(vl3);
    }

    /**
     * Return true if the given {@link HvdcLine} should be kept in the network, false otherwise
     */
    protected boolean test(HvdcLine hvdcLine) {
        Objects.requireNonNull(hvdcLine);
        VoltageLevel vl1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel();

        return test(vl1) && test(vl2);
    }
}
