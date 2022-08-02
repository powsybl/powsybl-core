/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.math.graph.TraverseResult;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
final class TopologyModificationUtils {

    static final class LoadingLimitsBags {

        private final LoadingLimitsBag activePowerLimits;
        private final LoadingLimitsBag apparentPowerLimits;
        private final LoadingLimitsBag currentLimits;

        LoadingLimitsBags(Supplier<Optional<ActivePowerLimits>> activePowerLimitsGetter, Supplier<Optional<ApparentPowerLimits>> apparentPowerLimitsGetter,
                          Supplier<Optional<CurrentLimits>> currentLimitsGetter) {
            activePowerLimits = activePowerLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
            apparentPowerLimits = apparentPowerLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
            currentLimits = currentLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
        }

        Optional<LoadingLimitsBag> getActivePowerLimits() {
            return Optional.ofNullable(activePowerLimits);
        }

        Optional<LoadingLimitsBag> getApparentPowerLimits() {
            return Optional.ofNullable(apparentPowerLimits);
        }

        Optional<LoadingLimitsBag> getCurrentLimits() {
            return Optional.ofNullable(currentLimits);
        }
    }

    private static final class LoadingLimitsBag {

        private final double permanentLimit;
        private final List<TemporaryLimitsBag> temporaryLimits = new ArrayList<>();

        private LoadingLimitsBag(LoadingLimits limits) {
            this.permanentLimit = limits.getPermanentLimit();
            for (LoadingLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                temporaryLimits.add(new TemporaryLimitsBag(tl));
            }
        }

        private double getPermanentLimit() {
            return permanentLimit;
        }

        private List<TemporaryLimitsBag> getTemporaryLimits() {
            return ImmutableList.copyOf(temporaryLimits);
        }
    }

    private static final class TemporaryLimitsBag {

        private final String name;
        private final int acceptableDuration;
        private final boolean fictitious;
        private final double value;

        private TemporaryLimitsBag(LoadingLimits.TemporaryLimit temporaryLimit) {
            this.name = temporaryLimit.getName();
            this.acceptableDuration = temporaryLimit.getAcceptableDuration();
            this.fictitious = temporaryLimit.isFictitious();
            this.value = temporaryLimit.getValue();
        }

        private String getName() {
            return name;
        }

        private int getAcceptableDuration() {
            return acceptableDuration;
        }

        private boolean isFictitious() {
            return fictitious;
        }

        private double getValue() {
            return value;
        }
    }

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

    static void addLoadingLimits(Line created, LoadingLimitsBags limits, Branch.Side side) {
        if (side == Branch.Side.ONE) {
            limits.getActivePowerLimits().ifPresent(lim -> addLoadingLimits(created.newActivePowerLimits1(), lim));
            limits.getApparentPowerLimits().ifPresent(lim -> addLoadingLimits(created.newApparentPowerLimits1(), lim));
            limits.getCurrentLimits().ifPresent(lim -> addLoadingLimits(created.newCurrentLimits1(), lim));
        } else {
            limits.getActivePowerLimits().ifPresent(lim -> addLoadingLimits(created.newActivePowerLimits2(), lim));
            limits.getApparentPowerLimits().ifPresent(lim -> addLoadingLimits(created.newApparentPowerLimits2(), lim));
            limits.getCurrentLimits().ifPresent(lim -> addLoadingLimits(created.newCurrentLimits2(), lim));
        }
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void addLoadingLimits(A adder, LoadingLimitsBag limits) {
        adder.setPermanentLimit(limits.getPermanentLimit());
        for (TemporaryLimitsBag tl : limits.getTemporaryLimits()) {
            adder.beginTemporaryLimit()
                    .setName(tl.getName())
                    .setAcceptableDuration(tl.getAcceptableDuration())
                    .setFictitious(tl.isFictitious())
                    .setValue(tl.getValue())
                    .endTemporaryLimit();
        }
        adder.add();
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String prefix, VoltageLevel.NodeBreakerView view) {
        createNodeBreakerSwitches(node1, middleNode, node2, "", prefix, view);
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String suffix, String prefix, VoltageLevel.NodeBreakerView view) {
        createNodeBreakerSwitches(node1, middleNode, node2, suffix, prefix, view, false);
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String suffix, String prefix, VoltageLevel.NodeBreakerView view, boolean open) {
        createNBBreaker(node1, middleNode, suffix, prefix, view, open);
        createNBDisconnector(middleNode, node2, suffix, prefix, view, open);
    }

    static void createNBBreaker(int node1, int node2, String suffix, String prefix, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(prefix + "_BREAKER" + suffix)
                .setKind(SwitchKind.BREAKER)
                .setOpen(open)
                .setRetained(true)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    static void createNBDisconnector(int node1, int node2, String suffix, String prefix, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(prefix + "_DISCONNECTOR" + suffix)
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

    /**
     * Utility method that associates a bus bar section position index to the orders taken by all the connectables
     * of the bus bar sections of this index.
     **/
    static Map<Integer, List<Integer>> getSliceOrdersMap(VoltageLevel voltageLevel) {
        Map<Integer, List<Integer>> sliceIndexOrdersMap = new TreeMap<>();
        Map<BusbarSection, List<Integer>> busbarSectionsOrdersMap = new LinkedHashMap<>();
        voltageLevel.getConnectableStream(BusbarSection.class)
                .forEach(bbs -> fillConnectableOrders(bbs, busbarSectionsOrdersMap));
        busbarSectionsOrdersMap.forEach((bbs, orders) -> {
            BusbarSectionPosition bbPosition = bbs.getExtension(BusbarSectionPosition.class);
            sliceIndexOrdersMap.putIfAbsent(bbPosition.getSectionIndex(), orders);
        });
        return sliceIndexOrdersMap;
    }

    static void fillConnectableOrders(BusbarSection bbs, Map<BusbarSection, List<Integer>> busbarSectionsOrdersMap) {
        BusbarSectionPosition bbPosition = bbs.getExtension(BusbarSectionPosition.class);
        int bbSection = bbPosition.getSectionIndex();

        if (busbarSectionsOrdersMap.containsKey(bbs)) {
            return;
        }
        List<Integer> orders = busbarSectionsOrdersMap.compute(bbs, (k, v) -> new ArrayList<>());

        bbs.getTerminal().traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel() != bbs.getTerminal().getVoltageLevel()) {
                    return TraverseResult.TERMINATE_PATH;
                }
                Connectable<?> connectable = terminal.getConnectable();
                if (connectable instanceof BusbarSection) {
                    BusbarSection otherBbs = (BusbarSection) connectable;
                    BusbarSectionPosition otherBbPosition = otherBbs.getExtension(BusbarSectionPosition.class);
                    if (otherBbPosition.getSectionIndex() == bbSection) {
                        busbarSectionsOrdersMap.put(otherBbs, orders);
                    } else {
                        return TraverseResult.TERMINATE_PATH;
                    }
                }
                ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
                if (position != null) {
                    addOrders(position, orders);
                }
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return TraverseResult.CONTINUE;
            }
        });
    }

    static void addOrders(ConnectablePosition<?> position, List<Integer> orders) {
        if (position.getFeeder() != null) {
            position.getFeeder().getOrder().ifPresent(orders::add);
        } else if (position.getFeeder1() != null) {
            position.getFeeder1().getOrder().ifPresent(orders::add);
            if (position.getFeeder2() != null) {
                position.getFeeder2().getOrder().ifPresent(orders::add);
                if (position.getFeeder3() != null) {
                    position.getFeeder3().getOrder().ifPresent(orders::add);
                }
            }
        }
    }

    private TopologyModificationUtils() {
    }
}
