/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.math.graph.TraverseResult;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public final class TopologyModificationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyModificationUtils.class);

    static final class LoadingLimitsBags {

        private LoadingLimitsBag activePowerLimits;
        private LoadingLimitsBag apparentPowerLimits;
        private LoadingLimitsBag currentLimits;

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

        private double permanentLimit;
        private List<TemporaryLimitsBag> temporaryLimits = new ArrayList<>();

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

    static double checkPercent(double percent) {
        if (Double.isNaN(percent)) {
            throw new PowsyblException("Percent should not be undefined");
        }
        return percent;
    }

    static Identifiable<?> checkIdentifiable(String id, Network network, boolean throwException, Reporter reporter, Logger logger) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            logger.error("Identifiable {} not found", id);
            reporter.report(Report.builder()
                    .withKey("notFoundIdentifiable")
                    .withDefaultMessage("Identifiable ${identifiableId} not found")
                    .withValue("identifiableId", id)
                    .withSeverity(TypedValue.ERROR_SEVERITY)
                    .build());
            if (throwException) {
                throw new PowsyblException("Identifiable " + id + " not found");
            }
        }
        return identifiable;
    }

    static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, boolean throwException, Reporter reporter, Logger logger) {
        if (identifiable instanceof Bus) {
            Bus bus = (Bus) identifiable;
            return bus.getVoltageLevel();
        } else if (identifiable instanceof BusbarSection) {
            BusbarSection bbs = (BusbarSection) identifiable;
            return bbs.getTerminal().getVoltageLevel();
        } else {
            logger.error("Identifiable {} is not a bus or a busbar section", identifiable.getId());
            reporter.report(Report.builder()
                    .withKey("unexpectedIdentifiableType")
                    .withDefaultMessage("Identifiable ${identifiableId} is not a bus or a busbar section")
                    .withValue("identifiableId", identifiable.getId())
                    .withSeverity(TypedValue.ERROR_SEVERITY)
                    .build());
            if (throwException) {
                throw new PowsyblException("Identifiable " + identifiable.getId() + " is not a bus or a busbar section");
            }
            return null;
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

    static void removeVoltageLevelAndSubstation(VoltageLevel voltageLevel, Reporter reporter) {
        Optional<Substation> substation = voltageLevel.getSubstation();
        String vlId = voltageLevel.getId();
        boolean noMoreEquipments = voltageLevel.getConnectableStream().noneMatch(c -> c.getType() != IdentifiableType.BUSBAR_SECTION);
        if (!noMoreEquipments) {
            voltageLevelRemovingEquipmentsLeftReport(reporter, vlId);
        }
        voltageLevel.remove();
        voltageLevelRemovedReport(reporter, vlId);

        substation.ifPresent(s -> {
            if (s.getVoltageLevelStream().count() == 0) {
                String substationId = s.getId();
                s.remove();
                substationRemovedReport(reporter, substationId);
            }
        });
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
                if (connectable instanceof BusbarSection) {
                    BusbarSection otherBbs = (BusbarSection) connectable;
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
        int min = previousSliceMax.map(o -> o + 1).orElse(Integer.MIN_VALUE);
        int max = sliceMin.map(o -> o - 1).orElse(
                getMinOrderUsedAfter(allOrders, sectionIndex).map(o -> o - 1).orElse(Integer.MAX_VALUE));
        return Optional.ofNullable(min <= max ? Range.between(min, max) : null);
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
        int min = sliceMax.map(o -> o + 1).orElse(
                getMaxOrderUsedBefore(allOrders, sectionIndex).map(o -> o + 1).orElse(Integer.MIN_VALUE));
        int max = nextSliceMin.map(o -> o - 1).orElse(Integer.MAX_VALUE);
        return Optional.ofNullable(min <= max ? Range.between(min, max) : null);
    }

    /**
     * Method returning the maximum order in the slice with the highest section index lower to the given section.
     * For two busbar sections with following indexes BBS1 with used orders 1,2,3 and BBS2 with used orders 7,8, this method
     * applied to BBS2 will return 3.
     */
    private static Optional<Integer> getMaxOrderUsedBefore(NavigableMap<Integer, List<Integer>> allOrders, int section) {
        Map.Entry<Integer, List<Integer>> lowerEntry;
        do {
            lowerEntry = allOrders.lowerEntry(section);
        } while (lowerEntry != null && lowerEntry.getValue().isEmpty());

        return Optional.ofNullable(lowerEntry)
                .flatMap(entry -> entry.getValue().stream().max(Comparator.naturalOrder()));
    }

    /**
     * Method returning the minimum order in the slice with the lowest section index higher to the given section.
     * For two busbar sections with following indexes BBS1 with used orders 1,2,3 and BBS2 with used orders 7,8, this method
     * applied to BBS1 will return 7.
     */
    private static Optional<Integer> getMinOrderUsedAfter(NavigableMap<Integer, List<Integer>> allOrders, int section) {
        Map.Entry<Integer, List<Integer>> higherEntry;
        do {
            higherEntry = allOrders.higherEntry(section);
        } while (higherEntry != null && higherEntry.getValue().isEmpty());

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
        voltageLevel.getConnectables().forEach(connectable -> {
            ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
            if (position != null) {
                List<Integer> orders = getOrderPositions(position, voltageLevel, connectable, false, Reporter.NO_OP);
                feederPositionsOrders.put(connectable.getId(), orders);
            }
        });
        return feederPositionsOrders;
    }

    private static void addOrderPositions(Connectable<?> connectable, VoltageLevel voltageLevel, Collection<Integer> feederPositionsOrders) {
        addOrderPositions(connectable, voltageLevel, feederPositionsOrders, false, Reporter.NO_OP);
    }

    /**
     * Method adding order position(s) of a connectable on a given voltage level to the given collection.
     */
    private static void addOrderPositions(Connectable<?> connectable, VoltageLevel voltageLevel, Collection<Integer> feederPositionsOrders, boolean throwException, Reporter reporter) {
        ConnectablePosition<?> position = (ConnectablePosition<?>) connectable.getExtension(ConnectablePosition.class);
        if (position != null) {
            List<Integer> orders = getOrderPositions(position, voltageLevel, connectable, throwException, reporter);
            feederPositionsOrders.addAll(orders);
        }
    }

    private static List<Integer> getOrderPositions(ConnectablePosition<?> position, VoltageLevel voltageLevel, Connectable<?> connectable, boolean throwException, Reporter reporter) {
        if (connectable instanceof Injection) {
            return getInjectionOrder(position, voltageLevel, (Injection<?>) connectable, throwException, reporter);
        } else if (connectable instanceof Branch) {
            return getBranchOrders(position, voltageLevel, (Branch<?>) connectable, throwException, reporter);
        } else if (connectable instanceof ThreeWindingsTransformer) {
            return get3wtOrders(position, voltageLevel, (ThreeWindingsTransformer) connectable, throwException, reporter);
        } else {
            LOGGER.error("Given connectable not supported: {}", connectable.getClass().getName());
            connectableNotSupported(reporter, connectable);
            if (throwException) {
                throw new AssertionError("Given connectable not supported: " + connectable.getClass().getName());
            }
        }
        return Collections.emptyList();
    }

    private static List<Integer> getInjectionOrder(ConnectablePosition<?> position, VoltageLevel voltageLevel, Injection<?> injection, boolean throwException, Reporter reporter) {
        List<Integer> singleOrder = position.getFeeder().getOrder().map(List::of).orElse(Collections.emptyList());
        checkConnectableInVoltageLevel(singleOrder, voltageLevel, injection, throwException, reporter);
        return singleOrder;
    }

    private static List<Integer> getBranchOrders(ConnectablePosition<?> position, VoltageLevel voltageLevel, Branch<?> branch, boolean throwException, Reporter reporter) {
        List<Integer> orders = new ArrayList<>();
        if (branch.getTerminal1().getVoltageLevel() == voltageLevel) {
            position.getFeeder1().getOrder().ifPresent(orders::add);
        }
        if (branch.getTerminal2().getVoltageLevel() == voltageLevel) {
            position.getFeeder2().getOrder().ifPresent(orders::add);
        }
        checkConnectableInVoltageLevel(orders, voltageLevel, branch, throwException, reporter);
        Collections.sort(orders);
        return orders;
    }

    private static List<Integer> get3wtOrders(ConnectablePosition<?> position, VoltageLevel voltageLevel, ThreeWindingsTransformer twt, boolean throwException, Reporter reporter) {
        List<Integer> orders = new ArrayList<>();
        if (twt.getLeg1().getTerminal().getVoltageLevel() == voltageLevel) {
            position.getFeeder1().getOrder().ifPresent(orders::add);
        }
        if (twt.getLeg2().getTerminal().getVoltageLevel() == voltageLevel) {
            position.getFeeder2().getOrder().ifPresent(orders::add);
        }
        if (twt.getLeg3().getTerminal().getVoltageLevel() == voltageLevel) {
            position.getFeeder3().getOrder().ifPresent(orders::add);
        }
        checkConnectableInVoltageLevel(orders, voltageLevel, twt, throwException, reporter);
        Collections.sort(orders);
        return orders;
    }

    private static void checkConnectableInVoltageLevel(List<Integer> orders, VoltageLevel voltageLevel, Connectable<?> connectable, boolean throwException, Reporter reporter) {
        if (orders.isEmpty()) {
            LOGGER.error("Given connectable {} not in voltageLevel {}", connectable.getId(), voltageLevel.getId());
            connectableNotInVoltageLevel(reporter, connectable, voltageLevel);
            if (throwException) {
                throw new AssertionError(String.format("Given connectable %s not in voltageLevel %s ", connectable.getId(), voltageLevel.getId()));
            }
        }
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

    private TopologyModificationUtils() {
    }
}
