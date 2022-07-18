/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.function.BiConsumer;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
final class TopologyModificationUtils {

    static double checkPercent(double percent) {
        if (Double.isNaN(percent)) {
            throw new PowsyblException("Percent should not be undefined");
        }
        return percent;
    }

    static LineAdder createLineAdder(double percent, String id, String name, String voltageLevelId1, String voltageLevelId2, Network network, Line line) {
        return network.newLine()
                .setId(id)
                .setName(name)
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setR(line.getR() * percent / 100)
                .setX(line.getX() * percent / 100)
                .setG1(line.getG1() * percent / 100)
                .setB1(line.getB1() * percent / 100)
                .setG2(line.getG2() * percent / 100)
                .setB2(line.getB2() * percent / 100);
    }

    static void attachLine(Terminal terminal, LineAdder adder, BiConsumer<Bus, LineAdder> connectableBusSetter,
                                   BiConsumer<Bus, LineAdder> busSetter, BiConsumer<Integer, LineAdder> nodeSetter) {
        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            connectableBusSetter.accept(terminal.getBusBreakerView().getConnectableBus(), adder);
            Bus bus = terminal.getBusBreakerView().getBus();
            if (bus != null) {
                busSetter.accept(bus, adder);
            }
        } else if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            int node = terminal.getNodeBreakerView().getNode();
            nodeSetter.accept(node, adder);
        } else {
            throw new AssertionError();
        }
    }

    static void addLoadingLimits(Line created, Line original, Branch.Side side) {
        if (side == Branch.Side.ONE) {
            original.getActivePowerLimits1().ifPresent(lim -> addLoadingLimits(created.newActivePowerLimits1(), lim));
            original.getApparentPowerLimits1().ifPresent(lim -> addLoadingLimits(created.newApparentPowerLimits1(), lim));
            original.getCurrentLimits1().ifPresent(lim -> addLoadingLimits(created.newCurrentLimits1(), lim));
        } else {
            original.getActivePowerLimits2().ifPresent(lim -> addLoadingLimits(created.newActivePowerLimits2(), lim));
            original.getApparentPowerLimits2().ifPresent(lim -> addLoadingLimits(created.newApparentPowerLimits2(), lim));
            original.getCurrentLimits2().ifPresent(lim -> addLoadingLimits(created.newCurrentLimits2(), lim));
        }
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void addLoadingLimits(A adder, L limits) {
        if (limits != null) {
            adder.setPermanentLimit(limits.getPermanentLimit());
            for (LoadingLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                adder.beginTemporaryLimit()
                        .setName(tl.getName())
                        .setAcceptableDuration(tl.getAcceptableDuration())
                        .setFictitious(tl.isFictitious())
                        .setValue(tl.getValue())
                        .endTemporaryLimit();
            }
            adder.add();
        }
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String lineId, VoltageLevel.NodeBreakerView view) {
        createNodeBreakerSwitches(node1, middleNode, node2, "", lineId, view);
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String suffix, String lineId, VoltageLevel.NodeBreakerView view) {
        createNodeBreakerSwitches(node1, middleNode, node2, suffix, lineId, view, false);
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String suffix, String lineId, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(lineId + "_BREAKER" + suffix)
                .setKind(SwitchKind.BREAKER)
                .setOpen(open)
                .setRetained(true)
                .setNode1(node1)
                .setNode2(middleNode)
                .add();
        view.newSwitch()
                .setId(lineId + "_DISCONNECTOR" + suffix)
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(open)
                .setNode1(middleNode)
                .setNode2(node2)
                .add();
    }

    static void createNodeBreakerBreaker(int node1, int node2, String suffix, String loadId, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(loadId + "BREAKER_" + suffix)
                .setKind(SwitchKind.BREAKER)
                .setOpen(open)
                .setRetained(true)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    static void createNodeBreakerDisconnector(int node1, int node2, String suffix, String loadId, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(loadId + "DISCONNECTOR_" + suffix)
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(open)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    static void createBusBreakerSwitches(String busId1, String middleBusId, String busId2, String lineId, VoltageLevel.BusBreakerView view) {
        view.newSwitch()
                .setId(lineId + "_SW_1")
                .setOpen(false)
                .setBus1(busId1)
                .setBus2(middleBusId)
                .add();
        view.newSwitch()
                .setId(lineId + "_SW_2")
                .setOpen(false)
                .setBus1(middleBusId)
                .setBus2(busId2)
                .add();
    }

    static LoadAdder createLoadAdder(String loadId, LoadType loadType, double p0, double q0, VoltageLevel voltageLevel) {
        return voltageLevel.newLoad()
                .setLoadType(loadType)
                .setP0(p0)
                .setQ0(q0)
                .setId(loadId);
    }

    private TopologyModificationUtils() {
    }
}
