/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

/**
 * An AC line.
 * <p>
 * The equivalent &#960; model used is:
 * <div>
 *    <object data="doc-files/line.svg" type="image/svg+xml"></object>
 * </div>
 * To create a line, see {@link LineAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see LineAdder
 */
public interface Line extends Branch<Line>, LineCharacteristics<Line> {

    boolean isTieLine();

    default Line move1(int node, VoltageLevel voltageLevel) {
        return move(node, voltageLevel, getTerminal2().getNodeBreakerView().getNode(), getTerminal2().getVoltageLevel());
    }

    default Line move2(int node, VoltageLevel voltageLevel) {
        return move(getTerminal1().getNodeBreakerView().getNode(), getTerminal1().getVoltageLevel(), node, voltageLevel);
    }

    default Line move(int node1, VoltageLevel voltageLevel1, int node2, VoltageLevel voltageLevel2) {
        if (voltageLevel1.getTopologyKind() != TopologyKind.NODE_BREAKER
                || voltageLevel2.getTopologyKind() != TopologyKind.NODE_BREAKER
                || getTerminal1().getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER
                || getTerminal2().getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new PowsyblException(String.format("Inconsistent topology for terminals of Line %s. . " +
                            "Use move1(Bus, boolean), move2(Bus, boolean) or move(Bus, boolean, Bus, boolean).",
                    getId()));
        }
        Network network = getNetwork();
        LineAdder adder = network.newLine()
                .setId(getId())
                .setR(getR())
                .setX(getX())
                .setG1(getG1())
                .setB1(getB1())
                .setG2(getG2())
                .setB2(getB2())
                .setFictitious(isFictitious())
                .setName(getOptionalName().orElse(null))
                .setNode1(node1)
                .setVoltageLevel1(voltageLevel1.getId())
                .setNode2(node2)
                .setVoltageLevel2(voltageLevel2.getId());
        remove();
        return adder.add();
    }

    default Line move1(Bus bus, boolean connected) {
        return move(bus, connected, getTerminal2().getBusBreakerView().getConnectableBus(),
                getTerminal2().getBusBreakerView().getBus() != null);
    }

    default Line move2(Bus bus, boolean connected) {
        return move(getTerminal1().getBusBreakerView().getConnectableBus(),
                getTerminal1().getBusBreakerView().getBus() != null,
                bus, connected);
    }

    default Line move(Bus bus1, boolean connected1, Bus bus2, boolean connected2) {
        VoltageLevel voltageLevel1 = bus1.getVoltageLevel();
        VoltageLevel voltageLevel2 = bus2.getVoltageLevel();
        if (voltageLevel2.getTopologyKind() != TopologyKind.BUS_BREAKER
                || voltageLevel1.getTopologyKind() != TopologyKind.BUS_BREAKER
                || getTerminal1().getVoltageLevel().getTopologyKind() != TopologyKind.BUS_BREAKER
                || getTerminal2().getVoltageLevel().getTopologyKind() != TopologyKind.BUS_BREAKER) {
            throw new PowsyblException(String.format("Inconsistent topology for terminals of Line %s. Use move1(int, VoltageLevel), " +
                            "move2(int, VoltageLevel) or move(int, VoltageLevel, int, VoltageLevel", getId()));
        }
        Network network = getNetwork();
        LineAdder adder = network.newLine()
                .setId(getId())
                .setR(getR())
                .setX(getX())
                .setG1(getG1())
                .setB1(getB1())
                .setG2(getG2())
                .setB2(getB2())
                .setFictitious(isFictitious())
                .setName(getOptionalName().orElse(null))
                .setConnectableBus1(bus1.getId())
                .setBus1(connected1 ? bus1.getId() : null)
                .setVoltageLevel1(voltageLevel1.getId())
                .setConnectableBus2(bus2.getId())
                .setBus2(connected2 ? bus2.getId() : null)
                .setVoltageLevel2(voltageLevel2.getId());
        remove();
        return adder.add();
    }
}
