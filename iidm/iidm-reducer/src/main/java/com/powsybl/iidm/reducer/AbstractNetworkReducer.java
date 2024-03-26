/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public abstract class AbstractNetworkReducer implements NetworkReducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkReducer.class);

    private final NetworkPredicate predicate;

    private Set<String> vlIds = new HashSet<>();

    public AbstractNetworkReducer(NetworkPredicate predicate) {
        this.predicate = Objects.requireNonNull(predicate);
    }

    public final void reduce(Network network) {
        buildVoltageLevelIdSet(network);

        // Remove all unwanted lines
        List<Line> lines = network.getLineStream()
                .filter(l -> !test(l))
                .toList();
        lines.forEach(this::reduce);

        // Remove all unwanted two windings transformers
        List<TwoWindingsTransformer> twoWindingsTransformers = network.getTwoWindingsTransformerStream()
                .filter(t -> !test(t))
                .toList();
        twoWindingsTransformers.forEach(this::reduce);

        // Remove all three windings transformers
        List<ThreeWindingsTransformer> threeWindingsTransformers = network.getThreeWindingsTransformerStream()
                .filter(t -> !test(t))
                .toList();
        threeWindingsTransformers.forEach(this::reduce);

        // Remove all unwanted HVDC lines
        List<HvdcLine> hvdcLines = network.getHvdcLineStream()
                .filter(h -> !test(h))
                .toList();
        hvdcLines.forEach(this::reduce);

        // Remove all unwanted voltage levels
        List<VoltageLevel> voltageLevels = network.getVoltageLevelStream()
                .filter(vl -> !test(vl))
                .toList();
        voltageLevels.forEach(this::reduce);

        // Remove all unwanted substations
        List<Substation> substations = network.getSubstationStream()
                .filter(s -> !test(s))
                .toList();
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
        Objects.requireNonNull(voltageLevel);
        return vlIds.contains(voltageLevel.getId());
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

    protected void buildVoltageLevelIdSet(Network network) {
        List<String> voltageLevels = network.getVoltageLevelStream()
                .filter(predicate::test)
                .map(VoltageLevel::getId)
                .toList();
        vlIds.addAll(voltageLevels);

        //Adding necessary vl for three winding transformers
        List<ThreeWindingsTransformer> threeWindingsTransformers = network.getThreeWindingsTransformerStream()
                .filter(t -> !test(t))
                .collect(Collectors.toList());
        checkThreeWindingsTransformersToKeep(threeWindingsTransformers);
    }

    private void checkThreeWindingsTransformersToKeep(List<ThreeWindingsTransformer> threeWindingsTransformers) {
        List<ThreeWindingsTransformer> modifiedTransformers = new ArrayList<>();
        threeWindingsTransformers.stream()
                .filter(this::mustBeKept)
                .forEach(transformer -> {
                    VoltageLevel vlToAdd = findVoltageLevelToAdd(transformer);
                    if (vlToAdd != null) {
                        vlIds.add(vlToAdd.getId());
                        modifiedTransformers.add(transformer);
                        LOGGER.info("It is not possible to keep exactly 2 out of 3 voltage levels connected to a three winding transformer (here {}).\n" +
                                " Adding voltage level {} to the voltage levels kept after the reduction.", transformer.getId(), vlToAdd.getId());
                    }
                });

        if (!modifiedTransformers.isEmpty()) {
            threeWindingsTransformers.removeAll(modifiedTransformers);
            checkThreeWindingsTransformersToKeep(threeWindingsTransformers);
        }
    }

    private boolean mustBeKept(ThreeWindingsTransformer transformer) {
        long count = transformer.getLegStream()
                .filter(leg -> test(leg.getTerminal().getVoltageLevel()))
                .count();
        return count == 2;
    }

    private VoltageLevel findVoltageLevelToAdd(ThreeWindingsTransformer transformer) {
        VoltageLevel vl1 = transformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = transformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel vl3 = transformer.getLeg3().getTerminal().getVoltageLevel();
        if (!test(vl1)) {
            return vl1;
        } else if (!test(vl2)) {
            return vl2;
        } else if (!test(vl3)) {
            return vl3;
        } else {
            return null;
        }
    }
}
