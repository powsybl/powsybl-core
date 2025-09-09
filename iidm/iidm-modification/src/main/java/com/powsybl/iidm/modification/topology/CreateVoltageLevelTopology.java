/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static java.util.stream.Collectors.groupingBy;

/**
 * Creates symmetrical matrix topology in a given voltage level,
 * containing a given number of busbar with a given number of sections each.<br/>
 * See {@link CreateVoltageLevelTopologyBuilder}.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CreateVoltageLevelTopology extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(CreateVoltageLevelTopology.class);

    private final String voltageLevelId;

    private final int lowBusOrBusbarIndex;
    private final int alignedBusesOrBusbarCount;
    private final int lowSectionIndex;
    private final int sectionCount;

    private final String busOrBusbarSectionPrefixId;
    private final String switchPrefixId;

    private boolean connectExistingConnectables;

    private final List<SwitchKind> switchKinds;

    CreateVoltageLevelTopology(String voltageLevelId, int lowBusOrBusbarIndex, Integer alignedBusesOrBusbarCount,
                               int lowSectionIndex, Integer sectionCount,
                               String busOrBusbarSectionPrefixId, String switchPrefixId, List<SwitchKind> switchKinds,
                               boolean connectExistingConnectables) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId, "Undefined voltage level ID");
        this.lowBusOrBusbarIndex = lowBusOrBusbarIndex;
        this.alignedBusesOrBusbarCount = Objects.requireNonNull(alignedBusesOrBusbarCount, "Undefined aligned buses or busbars count");
        this.lowSectionIndex = lowSectionIndex;
        this.sectionCount = Objects.requireNonNull(sectionCount, "Undefined section count");
        this.busOrBusbarSectionPrefixId = Objects.requireNonNull(busOrBusbarSectionPrefixId, "Undefined busbar section prefix ID");
        this.switchPrefixId = Objects.requireNonNull(switchPrefixId, "Undefined switch prefix ID");
        this.switchKinds = switchKinds;
        this.connectExistingConnectables = connectExistingConnectables;
    }

    @Override
    public String getName() {
        return "CreateVoltageLevelTopology";
    }

    private static boolean checkCountAttributes(Integer count, String type, int min, ReportNode reportNode, boolean throwException) {
        if (count < min) {
            LOG.error("{} must be >= {}", type, min);
            countLowerThanMin(reportNode, type, min);
            logOrThrow(throwException, type + " must be >= " + min);
            return false;
        }
        return true;
    }

    private boolean checkCountAttributes(int lowBusOrBusbarIndex, int alignedBusesOrBusbarCount, int lowSectionIndex,
                                         int sectionCount, boolean throwException, ReportNode reportNode) {
        return checkCountAttributes(lowBusOrBusbarIndex, "low busbar index", 0, reportNode, throwException) &&
        checkCountAttributes(alignedBusesOrBusbarCount, "busbar count", 1, reportNode, throwException) &&
        checkCountAttributes(lowSectionIndex, "low section index", 0, reportNode, throwException) &&
        checkCountAttributes(sectionCount, "section count", 1, reportNode, throwException);
    }

    private static boolean checkSwitchKinds(List<SwitchKind> switchKinds, int sectionCount, ReportNode reportNode, boolean throwException) {
        Objects.requireNonNull(switchKinds, "Undefined switch kinds");
        if (switchKinds.size() != sectionCount - 1) {
            unexpectedSwitchKindsCount(reportNode, switchKinds.size(), sectionCount - 1);
            logOrThrow(throwException, "Unexpected switch kinds count (" + switchKinds.size() + "). Should be " + (sectionCount - 1));
            return false;
        }
        if (switchKinds.contains(null)) {
            undefinedSwitchKind(reportNode);
            logOrThrow(throwException, "All switch kinds must be defined");
            return false;
        }
        if (switchKinds.stream().anyMatch(kind -> kind != SwitchKind.DISCONNECTOR && kind != SwitchKind.BREAKER)) {
            wrongSwitchKind(reportNode);
            logOrThrow(throwException, "Switch kinds must be DISCONNECTOR or BREAKER");
            return false;
        }
        return true;
    }

    private boolean checkBusbarSectionPosition(VoltageLevel voltageLevel, boolean throwException, ReportNode reportNode) {
        List<BusbarSectionPosition> bbsPosition = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                .map(bbs -> bbs.getExtension(BusbarSectionPosition.class))
                .map(BusbarSectionPosition.class::cast)
                .filter(Objects::nonNull)
                .toList();

        boolean positionTaken = bbsPosition.stream().anyMatch(position -> position.getBusbarIndex() >= lowBusOrBusbarIndex
                && position.getBusbarIndex() < lowBusOrBusbarIndex + alignedBusesOrBusbarCount
                && position.getSectionIndex() >= lowSectionIndex
                && position.getSectionIndex() < lowSectionIndex + sectionCount);

        if (bbsPosition.isEmpty()) {
            LOG.info("No busbar section position found but some busbar sections already exist. Their connectables will not be connected.");
            connectExistingConnectables = false;
            return true;
        }
        if (positionTaken) {
            wrongBusbarPosition(reportNode);
            logOrThrow(throwException, "A busbar section already has the same position.");
            return false;
        }
        return true;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public int getLowBusOrBusbarIndex() {
        return lowBusOrBusbarIndex;
    }

    public int getAlignedBusesOrBusbarCount() {
        return alignedBusesOrBusbarCount;
    }

    public int getLowSectionIndex() {
        return lowSectionIndex;
    }

    public int getSectionCount() {
        return sectionCount;
    }

    public List<SwitchKind> getSwitchKinds() {
        return Collections.unmodifiableList(switchKinds);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        //checks
        if (!checkCountAttributes(lowBusOrBusbarIndex, alignedBusesOrBusbarCount, lowSectionIndex, sectionCount, throwException, reportNode)) {
            return;
        }

        // Get the voltage level
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            LOG.error("Voltage level {} is not found", voltageLevelId);
            notFoundVoltageLevelReport(reportNode, voltageLevelId);
            logOrThrow(throwException, String.format("Voltage level %s is not found", voltageLevelId));
            return;
        }
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            if (!switchKinds.isEmpty()) {
                LOG.warn("Voltage level {} is BUS_BREAKER. Switchkinds is ignored.", voltageLevelId);
            }
            // Create buses
            createBuses(voltageLevel, namingStrategy);
            // Create switches between buses
            createBusBreakerSwitches(voltageLevel, namingStrategy);
        } else {
            if (!checkBusbarSectionPosition(voltageLevel, throwException, reportNode)) {
                return;
            }
            // Check switch kinds
            if (!checkSwitchKinds(switchKinds, sectionCount, reportNode, throwException)) {
                return;
            }
            // Create busbar sections
            createBusbarSections(voltageLevel, namingStrategy);
            // Create switches
            createSwitches(voltageLevel, namingStrategy);
            // Connect connectables that are on parallel busbar sections
            if (connectExistingConnectables) {
                connectConnectables(voltageLevel, namingStrategy);
            }
        }
        LOG.info("New symmetrical topology in voltage level {}: creation of {} bus(es) or busbar(s) with {} section(s) each.", voltageLevelId, alignedBusesOrBusbarCount, sectionCount);
        createdNewSymmetricalTopology(reportNode, voltageLevelId, alignedBusesOrBusbarCount, sectionCount);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        if (!checkCountAttributes(lowBusOrBusbarIndex, alignedBusesOrBusbarCount, lowSectionIndex, sectionCount, false, ReportNode.NO_OP)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else {
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null ||
                voltageLevel.getTopologyKind() != TopologyKind.BUS_BREAKER &&
                    (switchKinds.size() != sectionCount - 1 || switchKinds.contains(null)
                        || switchKinds.stream().anyMatch(kind -> kind != SwitchKind.DISCONNECTOR && kind != SwitchKind.BREAKER))) {
                impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            }
        }
        return impact;
    }

    private void createBusbarSections(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        int node = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busbarNum = lowBusOrBusbarIndex; busbarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busbarNum++) {
                BusbarSection bbs = voltageLevel.getNodeBreakerView().newBusbarSection()
                        .setId(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busbarNum, sectionNum))
                        .setNode(node)
                        .add();
                bbs.newExtension(BusbarSectionPositionAdder.class)
                        .withBusbarIndex(busbarNum)
                        .withSectionIndex(sectionNum)
                        .add();
                node++;
            }
        }
    }

    private void createBuses(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busNum = lowBusOrBusbarIndex; busNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busNum++) {
                voltageLevel.getBusBreakerView().newBus()
                        .setId(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, busNum, sectionNum))
                        .add();
            }
        }
    }

    private void createBusBreakerSwitches(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            for (int busNum = lowBusOrBusbarIndex; busNum < lowSectionIndex + alignedBusesOrBusbarCount; busNum++) {
                String bus1Id = namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, busNum, sectionNum);
                String bus2Id = namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, busNum, sectionNum + 1);
                createBusBreakerSwitch(bus1Id, bus2Id, namingStrategy.getSwitchId(switchPrefixId, busNum, sectionNum), voltageLevel.getBusBreakerView());
            }
        }
    }

    private void createSwitches(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            SwitchKind switchKind = switchKinds.get(sectionNum - 1);
            for (int busBarNum = lowBusOrBusbarIndex; busBarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busBarNum++) {
                // Busbarsections on which to connect the disconnectors
                BusbarSection bbs1 = voltageLevel.getNodeBreakerView().getBusbarSection(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busBarNum, sectionNum));
                BusbarSection bbs2 = voltageLevel.getNodeBreakerView().getBusbarSection(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busBarNum, sectionNum + 1));

                // Nodes
                int node1 = getNode(bbs1.getId(), voltageLevel);
                int node2 = getNode(bbs2.getId(), voltageLevel);

                if (switchKind == SwitchKind.BREAKER) {
                    // New nodes
                    int newNode1 = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
                    int newNode2 = newNode1 + 1;

                    // Prefix
                    String chunkingPrefixId = namingStrategy.getChunkPrefix(switchPrefixId, switchKinds, busBarNum, sectionNum, sectionNum + 1);

                    // Breaker and disconnectors creation
                    createNBDisconnector(node1, newNode1, namingStrategy.getDisconnectorBetweenChunksId(bbs1, chunkingPrefixId, node1, newNode1), voltageLevel.getNodeBreakerView(), false);
                    createNBBreaker(newNode1, newNode2, namingStrategy.getBreakerId(chunkingPrefixId, busBarNum, sectionNum), voltageLevel.getNodeBreakerView(), false);
                    createNBDisconnector(newNode2, node2, namingStrategy.getDisconnectorBetweenChunksId(bbs2, chunkingPrefixId, newNode2, node2), voltageLevel.getNodeBreakerView(), false);
                } else if (switchKind == SwitchKind.DISCONNECTOR) {
                    // Prefix
                    String sectioningPrefix = namingStrategy.getSectioningPrefix(switchPrefixId, bbs1, busBarNum, sectionNum, sectionNum + 1);

                    // Disconnector creation
                    createNBDisconnector(node1, node2, namingStrategy.getDisconnectorId(sectioningPrefix, node1, node2), voltageLevel.getNodeBreakerView(), false);
                } // other cases cannot happen (has been checked in the constructor)
            }
        }
    }

    private void connectConnectables(VoltageLevel vl, NamingStrategy namingStrategy) {
        Map<Integer, List<BusbarSection>> busbarSectionsByIndex = vl.getConnectableStream()
                .filter(BusbarSection.class::isInstance)
                .map(c -> (BusbarSection) c)
                .collect(groupingBy(bbs -> bbs.getExtension(BusbarSectionPosition.class).getSectionIndex()));

        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            List<BusbarSection> bbsList = busbarSectionsByIndex.getOrDefault(sectionNum, Collections.emptyList());
            Map<Integer, SwitchCreationData> switchesToCreate = new HashMap<>();
            bbsList.forEach(bbs -> {
                Map<Integer, SwitchCreationData> switchList = getSwitchesConnectingToNonBusbarSectionConnectables(bbs);
                switchesToCreate.putAll(switchList);
            });
            for (int busBarNum = lowBusOrBusbarIndex; busBarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busBarNum++) {
                BusbarSection bbs = vl.getNodeBreakerView().getBusbarSection(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busBarNum, sectionNum));
                VoltageLevel.NodeBreakerView nodeBreakerView = vl.getNodeBreakerView();
                int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
                switchesToCreate.forEach((node, switchCreationData) -> {
                    Connectable<?> connectable = switchCreationData.connectable;

                    boolean isDisconnector = switchCreationData.sw.getKind() == SwitchKind.DISCONNECTOR;

                    String switchId = getSwitchId(namingStrategy, node, connectable, isDisconnector, bbsNode);

                    if (isDisconnector) {
                        createNBDisconnector(bbsNode, node, switchId, nodeBreakerView, true, switchCreationData.sw.isFictitious());
                    } else {
                        createNBBreaker(bbsNode, node, switchId, nodeBreakerView, true, switchCreationData.sw.isFictitious());
                    }

                });
            }
        }
    }

    private String getSwitchId(NamingStrategy namingStrategy, Integer node, Connectable<?> connectable, boolean isDisconnector, int bbsNode) {
        String switchId;
        if (connectable instanceof BusbarSection) {
            switchId = isDisconnector
                    ? namingStrategy.getDisconnectorId(switchPrefixId, bbsNode, node)
                    : namingStrategy.getBreakerId(switchPrefixId, bbsNode, node);
        } else {
            int side = getSide(connectable);
            String baseId = namingStrategy.getSwitchBaseId(connectable, side);
            switchId = isDisconnector
                    ? namingStrategy.getDisconnectorId(baseId, bbsNode, node)
                    : namingStrategy.getBreakerId(baseId, bbsNode, node);
        }
        return switchId;
    }

    private int getSide(Connectable<?> connectable) {
        if (connectable instanceof Branch<?> b) {
            return b.getTerminal(voltageLevelId).getSide().getNum();
        } else if (connectable instanceof ThreeWindingsTransformer threeWindingsTransformer) {
            return threeWindingsTransformer.getTerminal(voltageLevelId).getSide().getNum();
        }
        // Connectable has only one side
        return 0;
    }

    public record SwitchCreationData(Switch sw, Connectable<?> connectable) {
    }

    private static Map<Integer, SwitchCreationData> getSwitchesConnectingToNonBusbarSectionConnectables(BusbarSection busbarSection) {
        Objects.requireNonNull(busbarSection, "Busbar section must not be null");

        Terminal terminal = busbarSection.getTerminal();
        int startNode = terminal.getNodeBreakerView().getNode();
        VoltageLevel.NodeBreakerView nodeBreakerView = terminal.getVoltageLevel().getNodeBreakerView();

        int inputSectionIndex = busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex();

        final Map<Integer, SwitchCreationData> result = new HashMap<>();
        final int[] firstNode = new int[1];
        final Switch[] firstSwitch = new Switch[1];

        nodeBreakerView.traverse(startNode, (fromNode, sw, toNode) -> {
            if (fromNode == startNode) { // We keep switches that are connected to the busbar section
                firstNode[0] = toNode;
                firstSwitch[0] = sw;
            }

            Optional<Terminal> terminalOpt = nodeBreakerView.getOptionalTerminal(toNode);
            if (terminalOpt.isEmpty()) {
                return TraverseResult.CONTINUE;
            }

            Connectable<?> connectable = terminalOpt.get().getConnectable();

            if (!(connectable instanceof BusbarSection otherBusbarSection)) {
                // We found a connectable that is not a busbarSection, we keep the switch
                result.put(firstNode[0], new SwitchCreationData(firstSwitch[0], connectable));
                return TraverseResult.TERMINATE_PATH;
            }

            int otherSectionIndex = otherBusbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex();

            if (inputSectionIndex == otherSectionIndex) {
                // We also keep coupling devices
                result.put(firstNode[0], new SwitchCreationData(firstSwitch[0], connectable));
            }

            return TraverseResult.TERMINATE_PATH;
        });

        return result;
    }

    private static int getNode(String busBarSectionId, VoltageLevel voltageLevel) {
        return voltageLevel.getNodeBreakerView().getBusbarSection(busBarSectionId).getTerminal().getNodeBreakerView().getNode();
    }
}
