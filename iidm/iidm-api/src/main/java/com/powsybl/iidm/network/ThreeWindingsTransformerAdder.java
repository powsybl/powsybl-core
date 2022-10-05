/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ThreeWindingsTransformerAdder extends IdentifiableAdder<ThreeWindingsTransformerAdder> {

    interface LegAdder {

        LegAdder setVoltageLevel(String voltageLevelId);

        LegAdder setNode(int node);

        LegAdder setBus(String bus);

        LegAdder setConnectableBus(String connectableBus);

        LegAdder setR(double r);

        LegAdder setX(double x);

        LegAdder setG(double g);

        LegAdder setB(double b);

        LegAdder setRatedU(double ratedU);

        default LegAdder setRatedS(double ratedS) {
            throw new UnsupportedOperationException();
        }

        ThreeWindingsTransformerAdder add();
    }

    LegAdder newLeg1();

    default LegAdder newLeg1(ThreeWindingsTransformer.Leg leg) {
        Objects.requireNonNull(leg);
        LegAdder adder = newLeg1()
                .setR(leg.getR())
                .setX(leg.getX())
                .setG(leg.getG())
                .setB(leg.getB())
                .setRatedS(leg.getRatedS())
                .setRatedU(leg.getRatedU());
        VoltageLevel vl = leg.getTerminal().getVoltageLevel();
        adder.setVoltageLevel(vl.getId());
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            adder.setNode(leg.getTerminal().getNodeBreakerView().getNode());
        } else {
            adder.setConnectableBus(leg.getTerminal().getBusBreakerView().getConnectableBus().getId());
            Optional.ofNullable(leg.getTerminal().getBusBreakerView().getBus())
                    .ifPresent(bus -> adder.setBus(bus.getId()));
        }
        return adder;
    }

    LegAdder newLeg2();

    default LegAdder newLeg2(ThreeWindingsTransformer.Leg leg) {
        Objects.requireNonNull(leg);
        LegAdder adder = newLeg2()
                .setR(leg.getR())
                .setX(leg.getX())
                .setG(leg.getG())
                .setB(leg.getB())
                .setRatedS(leg.getRatedS())
                .setRatedU(leg.getRatedU());
        VoltageLevel vl = leg.getTerminal().getVoltageLevel();
        adder.setVoltageLevel(vl.getId());
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            adder.setNode(leg.getTerminal().getNodeBreakerView().getNode());
        } else {
            adder.setConnectableBus(leg.getTerminal().getBusBreakerView().getConnectableBus().getId());
            Optional.ofNullable(leg.getTerminal().getBusBreakerView().getBus())
                    .ifPresent(bus -> adder.setBus(bus.getId()));
        }
        return adder;
    }

    LegAdder newLeg3();

    default LegAdder newLeg3(ThreeWindingsTransformer.Leg leg) {
        Objects.requireNonNull(leg);
        LegAdder adder = newLeg3()
                .setR(leg.getR())
                .setX(leg.getX())
                .setG(leg.getG())
                .setB(leg.getB())
                .setRatedS(leg.getRatedS())
                .setRatedU(leg.getRatedU());
        VoltageLevel vl = leg.getTerminal().getVoltageLevel();
        adder.setVoltageLevel(vl.getId());
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            adder.setNode(leg.getTerminal().getNodeBreakerView().getNode());
        } else {
            adder.setConnectableBus(leg.getTerminal().getBusBreakerView().getConnectableBus().getId());
            Optional.ofNullable(leg.getTerminal().getBusBreakerView().getBus())
                    .ifPresent(bus -> adder.setBus(bus.getId()));
        }
        return adder;
    }

    ThreeWindingsTransformerAdder setRatedU0(double ratedU0);

    ThreeWindingsTransformer add();
}
