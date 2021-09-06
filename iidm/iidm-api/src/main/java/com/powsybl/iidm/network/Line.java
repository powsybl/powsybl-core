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

    /**
     * Move the line's end on side ONE to the given node of the given voltage level.
     * If the given voltage level's topology is not NODE-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not NODE-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move1(int node, VoltageLevel voltageLevel) {
        return move(node, voltageLevel, getTerminal2().getNodeBreakerView().getNode(), getTerminal2().getVoltageLevel());
    }

    /**
     * Move the line's end on side TWO to the given node of the given voltage level.
     * If the given voltage level's topology is not NODE-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not NODE-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move2(int node, VoltageLevel voltageLevel) {
        return move(getTerminal1().getNodeBreakerView().getNode(), getTerminal1().getVoltageLevel(), node, voltageLevel);
    }

    /**
     * Move the line's ends to the given nodes of the given voltage levels.
     * If the given voltage levels' topology is not NODE-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not NODE-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move(int node1, VoltageLevel voltageLevel1, int node2, VoltageLevel voltageLevel2) {
        if (voltageLevel1.getTopologyKind() != TopologyKind.NODE_BREAKER
                || voltageLevel2.getTopologyKind() != TopologyKind.NODE_BREAKER
                || getTerminal1().getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new PowsyblException(String.format("Inconsistent topology for terminals of Line %s. " +
                            "Use move1(Bus, boolean), move2(Bus, boolean) or move(Bus, boolean, Bus, boolean).",
                    getId()));
        }
        LineAdder adder = initializeAdderToMove(voltageLevel1, voltageLevel2)
                .setNode1(node1)
                .setNode2(node2);
        remove();
        return adder.add();
    }

    /**
     * Move the line's end on side ONE to the given connectable bus with the given connection status.
     * If the given bus' voltage level's topology is not BUS-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not BUS-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move1(Bus bus, boolean connected) {
        return move(bus, connected, getTerminal2().getBusBreakerView().getConnectableBus(),
                getTerminal2().getBusBreakerView().getBus() != null);
    }

    /**
     * Move the line's end on side TWO to the given connectable bus with the given connection status.
     * If the given bus' voltage level's topology is not BUS-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not BUS-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move2(Bus bus, boolean connected) {
        return move(getTerminal1().getBusBreakerView().getConnectableBus(),
                getTerminal1().getBusBreakerView().getBus() != null,
                bus, connected);
    }

    /**
     * Move the line's ends to the given connectable buses with the given connection status.
     * If the given buses' voltage levels' topology is not BUS-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not BUS-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move(Bus bus1, boolean connected1, Bus bus2, boolean connected2) {
        VoltageLevel voltageLevel1 = bus1.getVoltageLevel();
        VoltageLevel voltageLevel2 = bus2.getVoltageLevel();
        if (voltageLevel2.getTopologyKind() != TopologyKind.BUS_BREAKER
                || voltageLevel1.getTopologyKind() != TopologyKind.BUS_BREAKER
                || getTerminal1().getVoltageLevel().getTopologyKind() != TopologyKind.BUS_BREAKER) {
            throw new PowsyblException(String.format("Inconsistent topology for terminals of Line %s. Use move1(int, VoltageLevel), " +
                            "move2(int, VoltageLevel) or move(int, VoltageLevel, int, VoltageLevel", getId()));
        }
        LineAdder adder = initializeAdderToMove(voltageLevel1, voltageLevel2)
                .setConnectableBus1(bus1.getId())
                .setBus1(connected1 ? bus1.getId() : null)
                .setConnectableBus2(bus2.getId())
                .setBus2(connected2 ? bus2.getId() : null);
        remove();
        return adder.add();
    }

    private LineAdder initializeAdderToMove(VoltageLevel voltageLevel1, VoltageLevel voltageLevel2) {
        return getNetwork().newLine()
                .setId(getId())
                .setR(getR())
                .setX(getX())
                .setG1(getG1())
                .setB1(getB1())
                .setG2(getG2())
                .setB2(getB2())
                .setFictitious(isFictitious())
                .setName(getOptionalName().orElse(null))
                .setVoltageLevel1(voltageLevel1.getId())
                .setVoltageLevel2(voltageLevel2.getId());
    }
}
