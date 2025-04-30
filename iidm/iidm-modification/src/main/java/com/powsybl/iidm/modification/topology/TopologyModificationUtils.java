/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.util.ModificationReports;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.math.graph.TraverseResult;
import org.apache.commons.lang3.Range;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class TopologyModificationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyModificationUtils.class);

    private TopologyModificationUtils() {
    }

    public static final class LoadingLimitsBags {

        private final LoadingLimitsBag activePowerLimits;
        private final LoadingLimitsBag apparentPowerLimits;
        private final LoadingLimitsBag currentLimits;

        public LoadingLimitsBags(Supplier<Optional<ActivePowerLimits>> activePowerLimitsGetter, Supplier<Optional<ApparentPowerLimits>> apparentPowerLimitsGetter,
                          Supplier<Optional<CurrentLimits>> currentLimitsGetter) {
            activePowerLimits = activePowerLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
            apparentPowerLimits = apparentPowerLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
            currentLimits = currentLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
        }

        LoadingLimitsBags(LoadingLimitsBag activePowerLimits, LoadingLimitsBag apparentPowerLimits, LoadingLimitsBag currentLimits) {
            this.activePowerLimits = activePowerLimits;
            this.apparentPowerLimits = apparentPowerLimits;
            this.currentLimits = currentLimits;
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
        private List<TemporaryLimitsBag> temporaryLimits = new ArrayList<>();

        private LoadingLimitsBag(LoadingLimits limits) {
            this.permanentLimit = limits.getPermanentLimit();
            for (LoadingLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                temporaryLimits.add(new TemporaryLimitsBag(tl));
            }
        }

        private LoadingLimitsBag(double permanentLimit, List<TemporaryLimitsBag> temporaryLimitsBags) {
            this.permanentLimit = permanentLimit;
            this.temporaryLimits = temporaryLimitsBags;
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

        TemporaryLimitsBag(LoadingLimits.TemporaryLimit temporaryLimit) {
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

    static LineAdder createLineAdder(String id, String name, String voltageLevelId1, String voltageLevelId2, Network network, Line line1, Line line2) {
        return network.newLine()
                .setId(id)
                .setName(name)
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setR(line1.getR() + line2.getR())
                .setX(line1.getX() + line2.getX())
                .setG1(line1.getG1() + line2.getG1())
                .setB1(line1.getB1() + line2.getB1())
                .setG2(line1.getG2() + line2.getG2())
                .setB2(line1.getB2() + line2.getB2());
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
            throw new IllegalStateException();
        }
    }

    public static void addLoadingLimits(Line created, LoadingLimitsBags limits, TwoSides side) {
        if (side == TwoSides.ONE) {
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

    static void removeVoltageLevelAndSubstation(VoltageLevel voltageLevel, ReportNode reportNode) {
        Optional<Substation> substation = voltageLevel.getSubstation();
        String vlId = voltageLevel.getId();
        boolean noMoreEquipments = voltageLevel.getConnectableStream().noneMatch(c -> c.getType() != IdentifiableType.BUSBAR_SECTION);
        if (!noMoreEquipments) {
            voltageLevelRemovingEquipmentsLeftReport(reportNode, vlId);
            LOGGER.warn("Voltage level {} still contains equipments", vlId);
        }
        voltageLevel.remove();
        voltageLevelRemovedReport(reportNode, vlId);
        LOGGER.info("Voltage level {} removed", vlId);

        substation.ifPresent(s -> {
            if (s.getVoltageLevelStream().count() == 0) {
                String substationId = s.getId();
                s.remove();
                substationRemovedReport(reportNode, substationId);
                LOGGER.info("Substation {} removed", substationId);
            }
        });
    }

    static void createNBBreaker(int node1, int node2, String id, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setKind(SwitchKind.BREAKER)
                .setOpen(open)
                .setRetained(true)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    static void createNBDisconnector(int node1, int node2, String id, VoltageLevel.NodeBreakerView view, boolean open) {
        view.newSwitch()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(open)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    static void createBusBreakerSwitch(String busId1, String busId2, String id, VoltageLevel.BusBreakerView view) {
        view.newSwitch()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setOpen(false)
                .setBus1(busId1)
                .setBus2(busId2)
                .add();
    }

    /**
     * Utility method that associates a busbar section position index to the orders taken by all the connectables
     * of the busbar sections of this index.
     **/
    static NavigableMap<Integer, List<Integer>> getSliceOrdersMap(VoltageLevel voltageLevel) {
        // Compute the map of connectables by busbar sections
        Map<BusbarSection, Set<Connectable<?>>> connectablesByBbs = new LinkedHashMap<>();
        voltageLevel.getConnectableStream(BusbarSection.class)
                .forEach(bbs -> fillConnectablesMap(bbs, connectablesByBbs));

        // Merging the map by section index
        Map<Integer, Set<Connectable<?>>> connectablesBySectionIndex = new LinkedHashMap<>();
        connectablesByBbs.forEach((bbs, connectables) -> {
            BusbarSectionPosition bbPosition = bbs.getExtension(BusbarSectionPosition.class);
            if (bbPosition != null) {
                connectablesBySectionIndex.merge(bbPosition.getSectionIndex(), connectables, (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                });
            }
        });

        // Get the orders corresponding map
        TreeMap<Integer, List<Integer>> ordersBySectionIndex = new TreeMap<>();
        connectablesBySectionIndex.forEach((sectionIndex, connectables) -> {
            List<Integer> orders = new ArrayList<>();
            connectables.forEach(connectable -> addOrderPositions(connectable, voltageLevel, orders));
            ordersBySectionIndex.put(sectionIndex, orders);
        });

        return ordersBySectionIndex;
    }

    /**
     * Method that fills the map connectablesByBbs with all the connectables of a busbar section.
     */
    static void fillConnectablesMap(BusbarSection bbs, Map<BusbarSection, Set<Connectable<?>>> connectablesByBbs) {
        BusbarSectionPosition bbPosition = bbs.getExtension(BusbarSectionPosition.class);
        int bbSection = bbPosition.getSectionIndex();

        if (connectablesByBbs.containsKey(bbs)) {
            return;
        }
        Set<Connectable<?>> connectables = connectablesByBbs.compute(bbs, (k, v) -> new LinkedHashSet<>());

        bbs.getTerminal().traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel() != bbs.getTerminal().getVoltageLevel()) {
                    return TraverseResult.TERMINATE_PATH;
                }
                Connectable<?> connectable = terminal.getConnectable();
                if (connectable instanceof BusbarSection otherBbs) {
                    BusbarSectionPosition otherBbPosition = otherBbs.getExtension(BusbarSectionPosition.class);
                    if (otherBbPosition.getSectionIndex() == bbSection) {
                        connectablesByBbs.put(otherBbs, connectables);
                    } else {
                        return TraverseResult.TERMINATE_PATH;
                    }
                }
                connectables.add(connectable);
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return TraverseResult.CONTINUE;
            }
        });
    }

    /**
     * Get the list of parallel busbar sections on a given busbar section position
     *
     * @param voltageLevel Voltage level in which to find the busbar sections
     * @param position busbar section position according to which busbar sections are found
     * @return the list of busbar sections in the voltage level that have the same section position as the given position
     */
    static List<BusbarSection> getParallelBusbarSections(VoltageLevel voltageLevel, BusbarSectionPosition position) {
        // List of the bars for the second section
        return voltageLevel.getNodeBreakerView().getBusbarSectionStream()
            .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
            .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex()).toList();
    }

    /**
     * Creates a breaker and a disconnector between the connectable and the specified busbar
     */
    static void createNodeBreakerSwitchesTopology(VoltageLevel voltageLevel, int connectableNode, int forkNode, NamingStrategy namingStrategy, String baseId, BusbarSection bbs) {
        createNodeBreakerSwitchesTopology(voltageLevel, connectableNode, forkNode, namingStrategy, baseId, List.of(bbs), bbs);
    }

    /**
     * Creates open disconnectors between the fork node and every busbar section of the list in a voltage level
     */
    static void createNodeBreakerSwitchesTopology(VoltageLevel voltageLevel, int connectableNode, int forkNode, NamingStrategy namingStrategy, String baseId, List<BusbarSection> bbsList, BusbarSection bbs) {
        // Closed breaker
        createNBBreaker(connectableNode, forkNode, namingStrategy.getBreakerId(baseId), voltageLevel.getNodeBreakerView(), false);

        // Disconnectors - only the one on the chosen busbarsection is closed
        createDisconnectorTopology(voltageLevel, forkNode, namingStrategy, baseId, bbsList, bbs);
    }

    /**
     * Creates disconnectors between the fork node and every busbar section of the list in a voltage level. Each disconnector will be closed if it is connected to the given bar, else opened
     */
    static void createDisconnectorTopology(VoltageLevel voltageLevel, int forkNode, NamingStrategy namingStrategy, String baseId, List<BusbarSection> bbsList, BusbarSection bbs) {
        createDisconnectorTopology(voltageLevel, forkNode, namingStrategy, baseId, bbsList, bbs, 0);
    }

    /**
     * Creates disconnectors between the fork node and every busbar section of the list in a voltage level. Each disconnector will be closed if it is connected to the given bar, else opened
     */
    static void createDisconnectorTopology(VoltageLevel voltageLevel, int forkNode, NamingStrategy namingStrategy, String baseId, List<BusbarSection> bbsList, BusbarSection bbs, int side) {
        // Disconnectors - only the one on the chosen busbarsection is closed
        bbsList.forEach(b -> {
            int bbsNode = b.getTerminal().getNodeBreakerView().getNode();
            createNBDisconnector(forkNode, bbsNode, namingStrategy.getDisconnectorId(b, baseId, forkNode, bbsNode, side), voltageLevel.getNodeBreakerView(), b != bbs);
        });
    }

    /**
     * Get all the unused positions before the lowest used position on the busbar section bbs.
     * It is a range between the maximum used position on the busbar section with the highest section index lower than the section
     * index of the given busbar section and the minimum position on the given busbar section.
     * For two busbar sections with following indexes BBS1 with used orders 1,2,3 and BBS2 with used orders 7,8, this method
     * applied to BBS2 will return a range from 4 to 6.
     */
    public static Optional<Range<Integer>> getUnusedOrderPositionsBefore(BusbarSection bbs) {
        BusbarSectionPosition busbarSectionPosition = bbs.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition == null) {
            throw new PowsyblException("busbarSection has no BusbarSectionPosition extension");
        }
        VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
        NavigableMap<Integer, List<Integer>> allOrders = getSliceOrdersMap(voltageLevel);

        int sectionIndex = busbarSectionPosition.getSectionIndex();
        Optional<Integer> previousSliceMax = getMaxOrderUsedBefore(allOrders, sectionIndex);
        Optional<Integer> sliceMin = allOrders.get(sectionIndex).stream().min(Comparator.naturalOrder());
        int min = previousSliceMax.map(o -> o + 1).orElse(0);
        int max = sliceMin.or(() -> getMinOrderUsedAfter(allOrders, sectionIndex)).map(o -> o - 1).orElse(Integer.MAX_VALUE);
        return Optional.ofNullable(min <= max ? Range.of(min, max) : null);
    }

    /**
     * Get all the unused positions after the highest used position on the busbar section bbs.
     * It is a range between the minimum used position on the busbar section with the lowest section index higher than the section
     * index of the given busbar section and the maximum position on the given busbar section.
     * For two busbar sections with following indexes BBS1 with used orders 1,2,3 and BBS2 with used orders 7,8, this method
     * applied to BBS1 will return a range from 4 to 6.
     */
    public static Optional<Range<Integer>> getUnusedOrderPositionsAfter(BusbarSection bbs) {
        BusbarSectionPosition busbarSectionPosition = bbs.getExtension(BusbarSectionPosition.class);
        if (busbarSectionPosition == null) {
            throw new PowsyblException("busbarSection has no BusbarSectionPosition extension");
        }
        VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
        NavigableMap<Integer, List<Integer>> allOrders = getSliceOrdersMap(voltageLevel);

        int sectionIndex = busbarSectionPosition.getSectionIndex();
        Optional<Integer> nextSliceMin = getMinOrderUsedAfter(allOrders, sectionIndex);
        Optional<Integer> sliceMax = allOrders.get(sectionIndex).stream().max(Comparator.naturalOrder());
        int min = sliceMax.or(() -> getMaxOrderUsedBefore(allOrders, sectionIndex)).map(o -> o + 1).orElse(0);
        int max = nextSliceMin.map(o -> o - 1).orElse(Integer.MAX_VALUE);
        return Optional.ofNullable(min <= max ? Range.of(min, max) : null);
    }

    /**
     * Get the range of connectable positions delimited by neighbouring busbar sections, for a given busbar section.
     * If the range is empty (for instance if positions max on left side is above position min on right side), the range returned is empty.
     * Note that the connectable positions needs to be in ascending order in the voltage level for ascending busbar section index positions.
     */
    public static Optional<Range<Integer>> getPositionRange(BusbarSection bbs) {
        BusbarSectionPosition positionExtension = bbs.getExtension(BusbarSectionPosition.class);
        if (positionExtension != null) {
            VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
            NavigableMap<Integer, List<Integer>> allOrders = getSliceOrdersMap(voltageLevel);

            int sectionIndex = positionExtension.getSectionIndex();
            int max = getMinOrderUsedAfter(allOrders, sectionIndex).map(o -> o - 1).orElse(Integer.MAX_VALUE);
            int min = getMaxOrderUsedBefore(allOrders, sectionIndex).map(o -> o + 1).orElse(0);

            return Optional.ofNullable(min <= max ? Range.of(min, max) : null);
        }
        return Optional.of(Range.of(0, Integer.MAX_VALUE));
    }

    /**
     * Method returning the maximum order in the slice with the highest section index lower to the given section.
     * For two busbar sections with following indexes BBS1 with used orders 1,2,3 and BBS2 with used orders 7,8, this method
     * applied to BBS2 will return 3.
     */
    public static Optional<Integer> getMaxOrderUsedBefore(NavigableMap<Integer, List<Integer>> allOrders, int section) {
        int s = section;
        Map.Entry<Integer, List<Integer>> lowerEntry;
        do {
            lowerEntry = allOrders.lowerEntry(s);
            if (lowerEntry == null) {
                break;
            }
            s = lowerEntry.getKey();
        } while (lowerEntry.getValue().isEmpty());

        return Optional.ofNullable(lowerEntry)
                .flatMap(entry -> entry.getValue().stream().max(Comparator.naturalOrder()));
    }

    /**
     * Method returning the minimum order in the slice with the lowest section index higher to the given section.
     * For two busbar sections with following indexes BBS1 with used orders 1,2,3 and BBS2 with used orders 7,8, this method
     * applied to BBS1 will return 7.
     */
    public static Optional<Integer> getMinOrderUsedAfter(NavigableMap<Integer, List<Integer>> allOrders, int section) {
        int s = section;
        Map.Entry<Integer, List<Integer>> higherEntry;
        do {
            higherEntry = allOrders.higherEntry(s);
            if (higherEntry == null) {
                break;
            }
            s = higherEntry.getKey();
        } while (higherEntry.getValue().isEmpty());

        return Optional.ofNullable(higherEntry)
                .flatMap(entry -> entry.getValue().stream().min(Comparator.naturalOrder()));
    }

    /**
     * Utility method to get all the taken feeder positions on a voltage level.
     */
    public static Set<Integer> getFeederPositions(VoltageLevel voltageLevel) {
        Set<Integer> feederPositionsOrders = new HashSet<>();
        voltageLevel.getConnectables().forEach(connectable -> addOrderPositions(connectable, voltageLevel, feederPositionsOrders));
        return feederPositionsOrders;
    }

    /**
     * Utility method to get all the taken feeder positions on a voltage level by connectable.
     */
    public static Map<String, List<Integer>> getFeederPositionsByConnectable(VoltageLevel voltageLevel) {
        Map<String, List<Integer>> feederPositionsOrders = new HashMap<>();
        getFeedersByConnectable(voltageLevel).forEach((k, v) -> {
            List<Integer> orders = new ArrayList<>();
            v.forEach(feeder -> feeder.getOrder().ifPresent(orders::add));
            if (orders.size() > 1) {
                Collections.sort(orders);
            }
            feederPositionsOrders.put(k, orders);
        });
        return feederPositionsOrders;
    }

    private static void addOrderPositions(Connectable<?> connectable, VoltageLevel voltageLevel, Collection<Integer> feederPositionsOrders) {
        addOrderPositions(connectable, voltageLevel, feederPositionsOrders, false, ReportNode.NO_OP);
    }

    /**
     * Method adding order position(s) of a connectable on a given voltage level to the given collection.
     */
    private static void addOrderPositions(Connectable<?> connectable, VoltageLevel voltageLevel, Collection<Integer> feederPositionsOrders, boolean throwException, ReportNode reportNode) {
        ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
        if (position != null) {
            List<Integer> orders = getOrderPositions(position, voltageLevel, connectable, throwException, reportNode);
            feederPositionsOrders.addAll(orders);
        }
    }

    /**
     * Utility method to get all the feeders on a voltage level by connectable.
     */
    public static Map<String, List<ConnectablePosition.Feeder>> getFeedersByConnectable(VoltageLevel voltageLevel) {
        Map<String, List<ConnectablePosition.Feeder>> feedersByConnectable = new HashMap<>();
        voltageLevel.getConnectables().forEach(connectable -> {
            ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
            if (position != null) {
                List<ConnectablePosition.Feeder> feeder = getFeeders(position, voltageLevel, connectable, false, ReportNode.NO_OP);
                feedersByConnectable.put(connectable.getId(), feeder);
            }
        });
        return feedersByConnectable;
    }

    private static List<Integer> getOrderPositions(ConnectablePosition<?> position, VoltageLevel voltageLevel, Connectable<?> connectable, boolean throwException, ReportNode reportNode) {
        List<ConnectablePosition.Feeder> feeders;
        if (connectable instanceof Injection) {
            feeders = getInjectionFeeder(position);
        } else if (connectable instanceof Branch) {
            feeders = getBranchFeeders(position, voltageLevel, (Branch<?>) connectable);
        } else if (connectable instanceof ThreeWindingsTransformer twt) {
            feeders = get3wtFeeders(position, voltageLevel, twt);
        } else {
            LOGGER.error("Given connectable not supported: {}", connectable.getClass().getName());
            connectableNotSupported(reportNode, connectable);
            if (throwException) {
                throw new IllegalStateException("Given connectable not supported: " + connectable.getClass().getName());
            }
            return Collections.emptyList();
        }
        List<Integer> orders = new ArrayList<>();
        feeders.forEach(feeder -> feeder.getOrder().ifPresent(orders::add));
        if (orders.size() > 1) {
            Collections.sort(orders);
        }
        return orders;
    }

    private static List<ConnectablePosition.Feeder> getFeeders(ConnectablePosition<?> position, VoltageLevel voltageLevel, Connectable<?> connectable, boolean throwException, ReportNode reportNode) {
        if (connectable instanceof Injection) {
            return getInjectionFeeder(position);
        } else if (connectable instanceof Branch) {
            return getBranchFeeders(position, voltageLevel, (Branch<?>) connectable);
        } else if (connectable instanceof ThreeWindingsTransformer twt) {
            return get3wtFeeders(position, voltageLevel, twt);
        } else {
            LOGGER.error("Given connectable not supported: {}", connectable.getClass().getName());
            connectableNotSupported(reportNode, connectable);
            if (throwException) {
                throw new IllegalStateException("Given connectable not supported: " + connectable.getClass().getName());
            }
        }
        return Collections.emptyList();
    }

    private static List<ConnectablePosition.Feeder> getInjectionFeeder(ConnectablePosition<?> position) {
        return Optional.ofNullable(position.getFeeder()).map(List::of).orElse(Collections.emptyList());
    }

    private static List<ConnectablePosition.Feeder> getBranchFeeders(ConnectablePosition<?> position, VoltageLevel voltageLevel, Branch<?> branch) {
        List<ConnectablePosition.Feeder> feeders = new ArrayList<>();
        if (branch.getTerminal1().getVoltageLevel() == voltageLevel) {
            Optional.ofNullable(position.getFeeder1()).ifPresent(feeders::add);
        }
        if (branch.getTerminal2().getVoltageLevel() == voltageLevel) {
            Optional.ofNullable(position.getFeeder2()).ifPresent(feeders::add);
        }
        return feeders;
    }

    private static List<ConnectablePosition.Feeder> get3wtFeeders(ConnectablePosition<?> position, VoltageLevel voltageLevel, ThreeWindingsTransformer twt) {
        List<ConnectablePosition.Feeder> feeders = new ArrayList<>();
        if (twt.getLeg1().getTerminal().getVoltageLevel() == voltageLevel) {
            Optional.ofNullable(position.getFeeder1()).ifPresent(feeders::add);
        }
        if (twt.getLeg2().getTerminal().getVoltageLevel() == voltageLevel) {
            Optional.ofNullable(position.getFeeder2()).ifPresent(feeders::add);
        }
        if (twt.getLeg3().getTerminal().getVoltageLevel() == voltageLevel) {
            Optional.ofNullable(position.getFeeder3()).ifPresent(feeders::add);
        }
        return feeders;
    }

    /**
     * Method returning the first busbar section with the lowest BusbarSectionIndex if there are the BusbarSectionPosition extensions and the first busbar section found otherwise.
     */
    public static BusbarSection getFirstBusbarSection(VoltageLevel voltageLevel) {
        BusbarSection bbs;
        if (voltageLevel.getNodeBreakerView().getBusbarSectionStream().anyMatch(b -> b.getExtension(BusbarSectionPosition.class) != null)) {
            bbs = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                    .min(Comparator.comparingInt((BusbarSection b) -> {
                        BusbarSectionPosition position = b.getExtension(BusbarSectionPosition.class);
                        return position == null ? Integer.MAX_VALUE : position.getSectionIndex();
                    }).thenComparingInt((BusbarSection b) -> {
                        BusbarSectionPosition position = b.getExtension(BusbarSectionPosition.class);
                        return position == null ? Integer.MAX_VALUE : position.getBusbarIndex();
                    })).orElse(null);
        } else {
            bbs = voltageLevel.getNodeBreakerView().getBusbarSectionStream().findFirst().orElse(null);
        }
        if (bbs == null) {
            throw new PowsyblException(String.format("Voltage level %s has no busbar section.", voltageLevel.getId()));
        }
        return bbs;
    }

    private static Optional<LoadingLimitsBag> mergeLimits(String lineId,
                                                          Optional<LoadingLimitsBag> limits1,
                                                          Optional<LoadingLimitsBag> limitsTeePointSide,
                                                          ReportNode reportNode) {
        Optional<LoadingLimitsBag> limits;

        double permanentLimit = limits1.map(LoadingLimitsBag::getPermanentLimit).orElse(Double.NaN);
        List<TemporaryLimitsBag> temporaryLimits1 = limits1.map(LoadingLimitsBag::getTemporaryLimits).orElse(new ArrayList<>());
        List<TemporaryLimitsBag> temporaryLimitsTeePointSide = limitsTeePointSide.map(LoadingLimitsBag::getTemporaryLimits).orElse(new ArrayList<>());
        List<TemporaryLimitsBag> temporaryLimits = new ArrayList<>();

        if (!limitsTeePointSide.isPresent()) {  // no limits on tee point side : we keep limits on other side
            limits = limits1;
        } else {
            // permanent limit : we keep the minimum permanent limit from both sides
            if (Double.isNaN(permanentLimit)) {
                permanentLimit = limitsTeePointSide.get().getPermanentLimit();
            } else if (!Double.isNaN(limitsTeePointSide.get().getPermanentLimit())) {
                permanentLimit = Math.min(permanentLimit, limitsTeePointSide.get().getPermanentLimit());
            }

            // temporary limits on both sides : they are ignored, otherwise, we keep temporary limits on side where they are defined
            if (!temporaryLimits1.isEmpty() && !temporaryLimitsTeePointSide.isEmpty()) {
                LOGGER.warn("Temporary limits on both sides for line {} : They are ignored", lineId);
                ModificationReports.ignoreTemporaryLimitsOnBothLineSides(reportNode, lineId);
            } else {
                temporaryLimits = !temporaryLimits1.isEmpty() ? temporaryLimits1 : temporaryLimitsTeePointSide;
            }

            limits = Optional.of(new LoadingLimitsBag(permanentLimit, temporaryLimits));
        }

        return limits;
    }

    public static LoadingLimitsBags mergeLimits(String lineId, LoadingLimitsBags limits, LoadingLimitsBags limitsTeePointSide, ReportNode reportNode) {
        Optional<LoadingLimitsBag> activePowerLimits = mergeLimits(lineId, limits.getActivePowerLimits(), limitsTeePointSide.getActivePowerLimits(), reportNode);
        Optional<LoadingLimitsBag> apparentPowerLimits = mergeLimits(lineId, limits.getApparentPowerLimits(), limitsTeePointSide.getApparentPowerLimits(), reportNode);
        Optional<LoadingLimitsBag> currentLimits = mergeLimits(lineId, limits.getCurrentLimits(), limitsTeePointSide.getCurrentLimits(), reportNode);

        return new LoadingLimitsBags(activePowerLimits.orElse(null), apparentPowerLimits.orElse(null), currentLimits.orElse(null));
    }

    /**
     * Find tee point connecting the 3 given lines, if any
     * @return the tee point connecting the 3 given lines or null if none
     */
    public static VoltageLevel findTeePoint(Line line1, Line line2, Line line3) {
        Map<VoltageLevel, Long> countVoltageLevels = Stream.of(line1, line2, line3)
                .map(Line::getTerminals)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Terminal::getVoltageLevel, Collectors.counting()));
        var commonVlMapEntry = Collections.max(countVoltageLevels.entrySet(), Map.Entry.comparingByValue());
        // If the lines are connected by a tee point, there should be 4 distinct voltage levels and one of them should be found 3 times
        if (countVoltageLevels.size() == 4 && commonVlMapEntry.getValue() == 3) {
            return commonVlMapEntry.getKey();
        } else {
            return null;
        }
    }

    /**
     * Create topology and generate new connectable node and return it.
     */
    public static int createTopologyAndGetConnectableNode(int side, String busOrBusbarSectionId, Network network, VoltageLevel voltageLevel, Connectable<?> connectable, NamingStrategy namingStrategy, ReportNode reportNode) {
        int forkNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int connectableNode = forkNode + 1;
        buildTopology(side, busOrBusbarSectionId, network, voltageLevel, forkNode, connectableNode, connectable, namingStrategy, reportNode);
        return connectableNode;
    }

    /**
     * Create topology by using the provided connectable node (pre-determined connectable node)
     */
    public static void createTopologyWithConnectableNode(int side, String busOrBusbarSectionId, Network network, VoltageLevel voltageLevel, int connectableNode, Connectable<?> connectable, NamingStrategy namingStrategy, ReportNode reportNode) {
        int forkNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        buildTopology(side, busOrBusbarSectionId, network, voltageLevel, forkNode, connectableNode, connectable, namingStrategy, reportNode);
    }

    private static void buildTopology(int side, String busOrBusbarSectionId, Network network, VoltageLevel voltageLevel, int forkNode, int connectableNode, Connectable<?> connectable, NamingStrategy namingStrategy, ReportNode reportNode) {
        // Information gathering
        String baseId = namingStrategy.getSwitchBaseId(connectable, side);
        BusbarSection bbs = network.getBusbarSection(busOrBusbarSectionId);
        BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);

        // Topology creation
        int parallelBbsNumber = 0;
        if (position == null) {
            // No position extension is present so only one disconnector is needed
            createNodeBreakerSwitchesTopology(voltageLevel, connectableNode, forkNode, namingStrategy, baseId, bbs);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs.getId());
            noBusbarSectionPositionExtensionReport(reportNode, bbs);
        } else {
            List<BusbarSection> bbsList = getParallelBusbarSections(voltageLevel, position);
            parallelBbsNumber = bbsList.size() - 1;
            createNodeBreakerSwitchesTopology(voltageLevel, connectableNode, forkNode, namingStrategy, baseId, bbsList, bbs);
        }
        LOGGER.info("New feeder bay associated to {} of type {} was created and connected to voltage level {} on busbar section {} with a closed disconnector " +
                "and on {} parallel busbar sections with an open disconnector.", connectable.getId(), connectable.getType(), voltageLevel.getId(), busOrBusbarSectionId, parallelBbsNumber);
        createdNodeBreakerFeederBay(reportNode, voltageLevel.getId(), busOrBusbarSectionId, connectable, parallelBbsNumber);
    }

    public static Integer getOppositeNode(Graph<Integer, Object> graph, int node, Object e) {
        Integer edgeSource = graph.getEdgeSource(e);
        return edgeSource == node ? graph.getEdgeTarget(e) : edgeSource;
    }

    /**
     * Starting from the given node, traverse the graph and remove all the switches and/or internal connections until a
     * fork node is encountered, for which special care is needed to clean the topology.
     */
    public static void cleanTopology(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, String connectableId, ReportNode reportNode) {
        Set<Object> edges = graph.edgesOf(node);
        if (edges.size() == 1) {
            Object edge = edges.iterator().next();
            Integer oppositeNode = getOppositeNode(graph, node, edge);
            removeSwitchOrInternalConnection(nbv, graph, edge, reportNode);
            cleanTopology(nbv, graph, oppositeNode, connectableId, reportNode);
        } else if (edges.size() > 1) {
            cleanFork(nbv, graph, node, edges, connectableId, reportNode);
        }
    }

    /**
     * Cleans up the topology for a node-breaker topology
     */
    public static void cleanNodeBreakerTopology(Network network, String connectableId, ReportNode reportNode) {
        Connectable<?> connectable = network.getConnectable(connectableId);
        for (Terminal t : connectable.getTerminals()) {
            if (t.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                Graph<Integer, Object> graph = createGraphFromTerminal(t);
                int node = t.getNodeBreakerView().getNode();
                cleanTopology(t.getVoltageLevel().getNodeBreakerView(), graph, node, connectableId, reportNode);
            }
        }
    }

    public static void removeSwitchOrInternalConnection(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph,
                                                         Object edge, ReportNode reportNode) {
        if (edge instanceof Switch sw) {
            String switchId = sw.getId();
            nbv.removeSwitch(switchId);
            removedSwitchReport(reportNode, switchId);
            LOGGER.info("Switch {} removed", switchId);
        } else {
            Pair<Integer, Integer> ic = (Pair<Integer, Integer>) edge;
            nbv.removeInternalConnections(ic.getFirst(), ic.getSecond());
            removedInternalConnectionReport(reportNode, ic.getFirst(), ic.getSecond());
            LOGGER.info("Internal connection between {} and {} removed", ic.getFirst(), ic.getSecond());
        }
        graph.removeEdge(edge);
    }

    /**
     * Try to remove all edges of the given fork node
     */
    public static void cleanFork(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, Set<Object> edges, String connectableId, ReportNode reportNode) {
        List<Object> toBusesOnly = new ArrayList<>();
        List<Object> mixed = new ArrayList<>();
        for (Object edge : edges) {
            List<Connectable<?>> connectables = getLinkedConnectables(nbv, graph, node, edge);
            if (connectables.stream().allMatch(BusbarSection.class::isInstance)) {
                // the edge is only linked to busbarSections, or to no connectables, hence it's a good candidate for removal
                toBusesOnly.add(edge);
            } else if (connectables.stream().noneMatch(BusbarSection.class::isInstance)) {
                // the edge is only linked to other non-busbarSection connectables, no further cleaning can be done
                // Note that connectables cannot be empty because of previous if
                String otherConnectableId = connectables.stream().map(Connectable::getId).findFirst().orElse("none");
                removeFeederBayAborted(reportNode, connectableId, node, otherConnectableId);
                LOGGER.info("Remove feeder bay of {} cannot go further node {}, as it is connected to {}", connectableId, node, otherConnectableId);
                return;
            } else {
                // the edge is linked to busbarSections and non-busbarSection connectables, some further cleaning can be done if there's only one edge of that type
                mixed.add(edge);
            }
        }

        // We now know there are only edges which are
        // - either only linked to busbarSections and no other connectables
        // - or linked to busbarSections and connectables
        // The former ones can be removed:
        for (Object edge : toBusesOnly) {
            removeAllSwitchesAndInternalConnections(nbv, graph, node, edge, reportNode);
        }
        // We don't remove the latter ones if more than one, as this would break the connection between them
        if (mixed.size() == 1) {
            // If only one, we're cleaning the dangling switches and/or internal connections
            cleanMixedTopology(nbv, graph, node, reportNode);
        }
    }

    private static List<Connectable<?>> getLinkedConnectables(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, Integer node, Object edge) {
        Set<Integer> visitedNodes = new HashSet<>();
        visitedNodes.add(node);
        List<Connectable<?>> connectables = new ArrayList<>();
        searchConnectables(nbv, graph, getOppositeNode(graph, node, edge), visitedNodes, connectables);
        return connectables;
    }

    public static Graph<Integer, Object> createGraphFromTerminal(Terminal terminal) {
        Graph<Integer, Object> graph = new Pseudograph<>(Object.class);
        int node = terminal.getNodeBreakerView().getNode();
        VoltageLevel.NodeBreakerView vlNbv = terminal.getVoltageLevel().getNodeBreakerView();
        graph.addVertex(node);
        vlNbv.traverse(node, (node1, sw, node2) -> {
            TraverseResult result = vlNbv.getOptionalTerminal(node2)
                    .map(Terminal::getConnectable)
                    .filter(BusbarSection.class::isInstance)
                    .map(c -> TraverseResult.TERMINATE_PATH)
                    .orElse(TraverseResult.CONTINUE);
            graph.addVertex(node2);
            graph.addEdge(node1, node2, sw != null ? sw : Pair.of(node1, node2));
            return result;
        });
        return graph;
    }

    /**
     * Starting from the given node, traverse the graph and remove all the switches and/or internal connections until a
     * fork node is encountered or a node on which a connectable is connected
     */
    private static void cleanMixedTopology(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, ReportNode reportNode) {
        // Get the next edge and the opposite node
        Set<Object> edges = graph.edgesOf(node);
        Object edge = edges.iterator().next();
        Integer oppositeNode = getOppositeNode(graph, node, edge);

        // Remove the switch or internal connection on the current edge
        removeSwitchOrInternalConnection(nbv, graph, edge, reportNode);

        // List the connectables connected to the opposite node
        List<Connectable<?>> connectables = new ArrayList<>();
        nbv.getOptionalTerminal(oppositeNode).map(Terminal::getConnectable).ifPresent(connectables::add);

        // If there is only one edge on the opposite node and no connectable, continue to remove the elements
        if (graph.edgesOf(oppositeNode).size() == 1 && connectables.isEmpty()) {
            cleanMixedTopology(nbv, graph, oppositeNode, reportNode);
        }
    }

    private static void searchConnectables(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, Integer node,
                                           Set<Integer> visitedNodes, List<Connectable<?>> connectables) {
        if (visitedNodes.contains(node)) {
            return;
        }
        nbv.getOptionalTerminal(node).map(Terminal::getConnectable).ifPresent(connectables::add);
        if (!isBusbarSection(nbv, node)) {
            visitedNodes.add(node);
            for (Object e : graph.edgesOf(node)) {
                searchConnectables(nbv, graph, getOppositeNode(graph, node, e), visitedNodes, connectables);
            }
        }
    }

    /**
     * Traverse the graph and remove all switches and internal connections until encountering a {@link BusbarSection}.
     */
    private static void removeAllSwitchesAndInternalConnections(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph,
                                                                int originNode, Object edge, ReportNode reportNode) {
        // in case of loops inside the traversed bay, the edge might have been already removed
        if (!graph.containsEdge(edge)) {
            return;
        }

        Integer oppositeNode = getOppositeNode(graph, originNode, edge);
        removeSwitchOrInternalConnection(nbv, graph, edge, reportNode);
        if (!isBusbarSection(nbv, oppositeNode)) {
            for (Object otherEdge : new ArrayList<>(graph.edgesOf(oppositeNode))) {
                removeAllSwitchesAndInternalConnections(nbv, graph, oppositeNode, otherEdge, reportNode);
            }
        }
    }

    private static boolean isBusbarSection(VoltageLevel.NodeBreakerView nbv, Integer node) {
        Optional<Connectable<?>> c = nbv.getOptionalTerminal(node).map(Terminal::getConnectable);
        return c.isPresent() && c.get() instanceof BusbarSection;
    }
}
