/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBBreaker;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBDisconnector;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.busbarSectionNotInTheSameVoltageLevelReport;
import static com.powsybl.iidm.modification.util.ModificationReports.busbarSectionsWithoutPositionReport;
import static com.powsybl.iidm.modification.util.ModificationReports.connectedFeedersReport;
import static com.powsybl.iidm.modification.util.ModificationReports.noBusbarSectionReport;
import static com.powsybl.iidm.modification.util.ModificationReports.wrongNetworkReport;
import static java.util.stream.Collectors.groupingBy;

/**
 * This network modification allows connecting a set of feeders (connectable elements) to specified busbar sections
 * within a network. This operation is applicable only for voltage levels with a NODE_BREAKER topology.
 * The class ensures that the specified busbar sections are all part of the same network, belong to
 * a single voltage level, and possess valid BusbarSectionPosition extensions. It also supports the
 * creation of coupling devices switches.
 * If a switch already exists between the connectable and the busbar section, it is not recreated.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class ConnectFeedersToBusbarSections extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectFeedersToBusbarSections.class);

    private static final String NAME = "ConnectFeedersToBusbarSections";

    private final List<Connectable> connectablesToConnect;
    private final List<BusbarSection> busbarSectionsToConnect;
    private final boolean connectCouplingDevices;
    private final String couplingDeviceSwitchPrefixId;

    ConnectFeedersToBusbarSections(List<Connectable> connectablesToConnect, List<BusbarSection> busbarSectionsToConnect, boolean connectCouplingDevices, String couplingDeviceSwitchPrefixId) {
        this.connectablesToConnect = Objects.requireNonNull(connectablesToConnect);
        this.busbarSectionsToConnect = Objects.requireNonNull(busbarSectionsToConnect);
        this.connectCouplingDevices = connectCouplingDevices;
        this.couplingDeviceSwitchPrefixId = couplingDeviceSwitchPrefixId;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        // checks
        if (busbarSectionsToConnect.isEmpty()) {
            logOrThrow(throwException, "No busbar section provided.");
            noBusbarSectionReport(reportNode);
            return;
        }

        if (busbarSectionsToConnect.stream().anyMatch(b -> b.getNetwork() != network)) {
            logOrThrow(throwException, "All busbar sections must be in the network passed to the method.");
            wrongNetworkReport(reportNode);
            return;
        }

        // Check if all busbar sections belong to exactly one voltage level
        List<VoltageLevel> distinctVoltageLevels = busbarSectionsToConnect.stream().map(bbs -> bbs.getTerminal().getVoltageLevel()).distinct().toList();

        if (distinctVoltageLevels.size() != 1) {
            logOrThrow(throwException, "All busbar sections must all belong to the same voltage level.");
            busbarSectionNotInTheSameVoltageLevelReport(reportNode);
            return;
        }

        VoltageLevel voltageLevel = distinctVoltageLevels.get(0);

        if (busbarSectionsToConnect.stream().map(bbs -> bbs.getExtension(BusbarSectionPosition.class)).anyMatch(Objects::isNull)) {
            logOrThrow(throwException, "All busbar sections must have a BusbarSectionPosition extension.");
            busbarSectionsWithoutPositionReport(reportNode, voltageLevel.getId());
            return;
        }

        List<Connectable<?>> connectedFeeders = connectFeeders(voltageLevel, namingStrategy);

        LOGGER.info("{} are connected to busbar sections {}", connectedFeeders, busbarSectionsToConnect);
        connectedFeedersReport(reportNode, connectedFeeders, busbarSectionsToConnect);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        if (busbarSectionsToConnect.isEmpty() || busbarSectionsToConnect.stream().anyMatch(b -> b.getNetwork() != network) ||
                busbarSectionsToConnect.stream().map(bbs -> bbs.getExtension(BusbarSectionPosition.class)).anyMatch(Objects::isNull) ||
                busbarSectionsToConnect.stream().map(bbs -> bbs.getTerminal().getVoltageLevel()).distinct().count() != 1) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private List<Connectable<?>> connectFeeders(VoltageLevel vl, NamingStrategy namingStrategy) {

        List<Connectable<?>> connectedFeeders = new ArrayList<>();

        // List of busbar sections indexed by section index
        Map<Integer, List<BusbarSection>> busbarSectionsByIndex = vl.getNodeBreakerView()
            .getBusbarSectionStream()
            .collect(groupingBy(this::getSectionIndex));
        Map<Integer, List<BusbarSection>> busbarSectionsToConnectByIndex = busbarSectionsToConnect.stream()
            .collect(groupingBy(this::getSectionIndex));

        busbarSectionsToConnectByIndex.forEach((sectionIndex, busbarSectionList) -> {
            List<BusbarSection> existingBusbarSections = busbarSectionsByIndex.getOrDefault(sectionIndex, Collections.emptyList());
            Map<Integer, SwitchCreationData> switchesToCreate = getSwitchesConnectingConnectablesToExistingBusbarSections(existingBusbarSections);
            busbarSectionList.forEach(bbs -> createSwitchesForBusbarSection(vl, namingStrategy, switchesToCreate, bbs, connectedFeeders));
        });
        return connectedFeeders;
    }

    private void createSwitchesForBusbarSection(VoltageLevel vl, NamingStrategy namingStrategy,
                                                Map<Integer, SwitchCreationData> switchesToCreate, BusbarSection bbs,
                                                List<Connectable<?>> connectedFeeders) {
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        switchesToCreate.forEach((node, switchData) -> {
            if (shouldSkipSwitchCreation(bbs, switchData)) {
                return;
            }
            switchData.connectables.stream().filter(c -> !(c instanceof BusbarSection)).forEach(connectedFeeders::add);
            createSwitch(vl, namingStrategy, bbsNode, node, switchData.connectables.get(0), switchData);
        });
    }

    private Map<Integer, SwitchCreationData> getSwitchesConnectingConnectablesToExistingBusbarSections(List<BusbarSection> existingBusbarSections) {
        Map<Integer, SwitchCreationData> switchesToCreate = new HashMap<>();
        for (BusbarSection busbarSection : existingBusbarSections) {
            switchesToCreate.putAll(getSwitchesConnectingConnectables(busbarSection));
        }
        return switchesToCreate;
    }

    private boolean shouldSkipSwitchCreation(BusbarSection bbs, SwitchCreationData data) {
        boolean isCouplingDevice = isOnlyBusbarSections(data.connectables);

        if (data.startBusbarSection == bbs) {
            return true;
        }

        if (data.connectables.contains(bbs) && !isCouplingDevice) {
            return true;
        }

        if (isCouplingDevice) {
            boolean betweenSections = isCouplingDeviceBetweenSections(data);

            if (betweenSections && data.connectables.contains(bbs)) {
                return true;
            }

            long count = data.connectables.stream().filter(b -> b.equals(bbs)).count();
            return !betweenSections && count > 1;
        }

        return false;
    }

    private void createSwitch(VoltageLevel vl, NamingStrategy namingStrategy,
                              int bbsNode, int node, Connectable<?> connectable, SwitchCreationData switchCreationData) {
        boolean isDisconnector = switchCreationData.sw.getKind() == SwitchKind.DISCONNECTOR;
        String switchId = getSwitchId(namingStrategy, node, connectable, isDisconnector, bbsNode, vl.getId());
        String switchName = getSwitchName(namingStrategy, node, connectable, isDisconnector, bbsNode, vl.getId());
        if (isDisconnector) {
            createNBDisconnector(bbsNode, node, switchId, switchName, vl.getNodeBreakerView(), true, switchCreationData.sw.isFictitious());
        } else {
            createNBBreaker(bbsNode, node, switchId, switchName, vl.getNodeBreakerView(), true, switchCreationData.sw.isFictitious());
        }
    }

    private String getSwitchId(NamingStrategy namingStrategy, int connectableNode, Connectable<?> connectable, boolean isDisconnector, int bbsNode, String voltageLevelId) {
        String baseId = connectable instanceof BusbarSection ?
            couplingDeviceSwitchPrefixId :
            namingStrategy.getSwitchBaseId(connectable, getSide(connectable, voltageLevelId));
        return isDisconnector
            ? namingStrategy.getDisconnectorId(baseId, bbsNode, connectableNode)
            : namingStrategy.getBreakerId(baseId, bbsNode, connectableNode);
    }

    private String getSwitchName(NamingStrategy namingStrategy, int connectableNode, Connectable<?> connectable, boolean isDisconnector, int bbsNode, String voltageLevelId) {
        String baseId = connectable instanceof BusbarSection ?
            couplingDeviceSwitchPrefixId :
            namingStrategy.getSwitchBaseName(connectable, getSide(connectable, voltageLevelId));
        return isDisconnector
            ? namingStrategy.getDisconnectorName(baseId, bbsNode, connectableNode)
            : namingStrategy.getBreakerName(baseId, bbsNode, connectableNode);
    }

    private int getSide(Connectable<?> connectable, String voltageLevelId) {
        if (connectable instanceof Branch<?> branch) {
            return branch.getTerminal(voltageLevelId).getSide().getNum();
        } else if (connectable instanceof ThreeWindingsTransformer threeWindingsTransformer) {
            return threeWindingsTransformer.getTerminal(voltageLevelId).getSide().getNum();
        }
        // Connectable has only one side
        return 0;
    }

    private Map<Integer, SwitchCreationData> getSwitchesConnectingConnectables(BusbarSection busbarSection) {
        Objects.requireNonNull(busbarSection, "Busbar section must not be null");

        Terminal terminal = busbarSection.getTerminal();
        int startNode = terminal.getNodeBreakerView().getNode();
        VoltageLevel.NodeBreakerView nodeBreakerView = terminal.getVoltageLevel().getNodeBreakerView();

        final Map<Integer, SwitchCreationData> result = new HashMap<>();
        final int[] firstNode = new int[1];
        final Map<Integer, List<Connectable<?>>> foundConnectables = new HashMap<>();
        final Map<Integer, Switch> switchesPerNode = new HashMap<>();

        nodeBreakerView.traverse(startNode, (fromNode, sw, toNode) -> {
            if (sw == null) { // Internal connection
                return TraverseResult.CONTINUE;
            }

            // Keep switches that are connected to the busbar section
            if (fromNode == startNode) {
                firstNode[0] = toNode;
                switchesPerNode.put(toNode, sw);
            }

            // Continue if there is no terminal yet
            Optional<Terminal> terminalOpt = nodeBreakerView.getOptionalTerminal(toNode);
            if (terminalOpt.isEmpty()) {
                return TraverseResult.CONTINUE;
            }

            // Add the connectable to the list of connectables at the node
            Connectable<?> connectable = terminalOpt.get().getConnectable();
            List<Connectable<?>> connectablesAtNode = foundConnectables.getOrDefault(firstNode[0], new ArrayList<>());
            connectablesAtNode.add(connectable);
            foundConnectables.put(firstNode[0], connectablesAtNode);

            return TraverseResult.TERMINATE_PATH;
        });

        foundConnectables.forEach((node, connectable) -> {
            if (keepSwitch(connectable, busbarSection)) {
                result.put(node, new SwitchCreationData(switchesPerNode.get(node), connectable, busbarSection));
            }
        });
        return result;
    }

    private boolean keepSwitch(List<Connectable<?>> connectables, BusbarSection startBusbarSection) {
        boolean isOnlyBusbarSections = isOnlyBusbarSections(connectables);
        if (isOnlyBusbarSections) { // Check if we have a coupling device switch
            int startBusbarSectionPosition = startBusbarSection.getExtension(BusbarSectionPosition.class).getBusbarIndex();
            boolean isCouplingDevice = connectables.stream().map(c -> (BusbarSection) c).map(b -> b.getExtension(BusbarSectionPosition.class)).anyMatch(position -> ((BusbarSectionPosition) position).getBusbarIndex() != startBusbarSectionPosition);
            return isCouplingDevice && connectCouplingDevices;
        }
        return connectables.stream().anyMatch(connectablesToConnect::contains);
    }

    private int getSectionIndex(BusbarSection bbs) {
        return bbs.getExtension(BusbarSectionPosition.class).getSectionIndex();
    }

    private boolean isOnlyBusbarSections(List<Connectable<?>> connectables) {
        return connectables.stream().allMatch(BusbarSection.class::isInstance);
    }

    private boolean isCouplingDeviceBetweenSections(SwitchCreationData data) {
        return data.connectables.stream()
            .map(BusbarSection.class::cast)
            .map(this::getSectionIndex)
            .distinct()
            .count() == 2;
    }

    public record SwitchCreationData(Switch sw, List<Connectable<?>> connectables, BusbarSection startBusbarSection) {
    }
}
