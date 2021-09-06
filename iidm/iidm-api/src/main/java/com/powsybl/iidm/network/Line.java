/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;

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
        return move(node, voltageLevel, Side.ONE);
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
        return move(node, voltageLevel, Side.TWO);
    }

    /**
     * Move the line's end on the given side to the given node of the given voltage level.
     * If the given voltage level's topology is not NODE-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not NODE-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move(int node, VoltageLevel voltageLevel, Side side) {
        Objects.requireNonNull(side);
        Objects.requireNonNull(voltageLevel);
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new PowsyblException(String.format("Inconsistent topology for terminals of Line %s. " +
                            "Use move1(Bus, boolean), move2(Bus, boolean) or move(Bus, boolean, Side).",
                    getId()));
        }
        LineAdder adder = initializeAdderToMove();
        if (side == Side.ONE) {
            setIdenticalToSide(Side.TWO, adder)
                    .setNode1(node)
                    .setVoltageLevel1(voltageLevel.getId());
        } else if (side == Side.TWO) {
            setIdenticalToSide(Side.ONE, adder)
                    .setNode2(node)
                    .setVoltageLevel2(voltageLevel.getId());
        }
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
        return move(bus, connected, Side.ONE);
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
        return move(bus, connected, Side.TWO);
    }

    /**
     * Move the line's end on the given side to the given connectable bus with the given connection status.
     * If the given bus' voltage level's topology is not BUS-BREAKER, a runtime exception is thrown.
     * If the topology of the voltage levels previously at the ends of the line is not BUS-BREAKER,
     * a runtime exception is also thrown.
     * Please note that the default implementation returns a new {@link Line} object (copying the line with different end points).
     * The implemented method should return the same {@link Line} object with different terminals.
     */
    default Line move(Bus bus, boolean connected, Side side) {
        Objects.requireNonNull(side);
        Objects.requireNonNull(bus);
        VoltageLevel voltageLevel = bus.getVoltageLevel();
        if (voltageLevel.getTopologyKind() != TopologyKind.BUS_BREAKER) {
            throw new PowsyblException(String.format("Inconsistent topology for terminals of Line %s. Use move1(int, VoltageLevel), " +
                    "move2(int, VoltageLevel) or move(int, VoltageLevel, Side)", getId()));
        }
        LineAdder adder = initializeAdderToMove();
        if (side == Side.ONE) {
            setIdenticalToSide(Side.TWO, adder)
                    .setConnectableBus1(bus.getId())
                    .setBus1(connected ? bus.getId() : null)
                    .setVoltageLevel1(bus.getVoltageLevel().getId());
        } else if (side == Side.TWO) {
            setIdenticalToSide(Side.ONE, adder)
                    .setConnectableBus2(bus.getId())
                    .setBus2(connected ? bus.getId() : null)
                    .setVoltageLevel2(bus.getVoltageLevel().getId());
        }
        remove();
        return adder.add();
    }

    private LineAdder initializeAdderToMove() {
        return getNetwork().newLine()
                .setId(getId())
                .setR(getR())
                .setX(getX())
                .setG1(getG1())
                .setB1(getB1())
                .setG2(getG2())
                .setB2(getB2())
                .setFictitious(isFictitious())
                .setName(getOptionalName().orElse(null));
    }

    private LineAdder setIdenticalToSide(Branch.Side side, LineAdder adder) {
        TopologyKind topologyKind = getTerminal(side).getVoltageLevel().getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            if (side == Side.ONE) {
                return adder.setVoltageLevel1(getTerminal1().getVoltageLevel().getId())
                        .setConnectableBus1(getTerminal1().getBusBreakerView().getConnectableBus().getId())
                        .setBus1(getTerminal1().getBusBreakerView().getBus() != null ? getTerminal1().getBusBreakerView().getBus().getId() : null);
            } else if (side == Side.TWO) {
                return adder.setVoltageLevel2(getTerminal2().getVoltageLevel().getId())
                        .setConnectableBus2(getTerminal2().getBusBreakerView().getConnectableBus().getId())
                        .setBus2(getTerminal2().getBusBreakerView().getBus() != null ? getTerminal2().getBusBreakerView().getBus().getId() : null);
            }
        } else if (topologyKind == TopologyKind.NODE_BREAKER) {
            if (side == Side.ONE) {
                return adder.setVoltageLevel1(getTerminal1().getVoltageLevel().getId())
                        .setNode1(getTerminal1().getNodeBreakerView().getNode());
            } else if (side == Side.TWO) {
                return adder.setVoltageLevel2(getTerminal2().getVoltageLevel().getId())
                        .setNode2(getTerminal2().getNodeBreakerView().getNode());
            }
        }
        throw new AssertionError();
    }
}
