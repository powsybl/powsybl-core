package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBBreaker;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBDisconnector;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static java.util.stream.Collectors.groupingBy;

/**
 * This network modification allows connecting a set of feeders (connectable elements) to specified busbar sections
 * within a network. This operation is applicable only for voltage levels with a NODE_BREAKER topology.
 * The class ensures that the specified busbar sections are all part of the same network, belong to
 * a single voltage level, and possess valid BusbarSectionPosition extensions. It also supports the
 * creation of coupling devices switches.
 * If a switch already exists between the connectable and the busbar section, it is not recreated.
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
                busbarSectionsToConnect.stream().map(bbs -> bbs.getTerminal().getVoltageLevel()).distinct().count() != 1 ||
                busbarSectionsToConnect.stream().anyMatch(b -> b.getTerminal().getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private List<Connectable<?>> connectFeeders(VoltageLevel vl, NamingStrategy namingStrategy) {

        List<Connectable<?>> connectedFeeders = new ArrayList<>();

        Map<Integer, List<BusbarSection>> busbarSectionsByIndex = vl.getConnectableStream()
                .filter(BusbarSection.class::isInstance)
                .map(BusbarSection.class::cast)
                .collect(groupingBy(this::getSectionIndex));

        Map<Integer, List<BusbarSection>> busbarSectionsToConnectByIndex = busbarSectionsToConnect.stream()
                .collect(groupingBy(this::getSectionIndex));

        VoltageLevel.NodeBreakerView nodeBreakerView = vl.getNodeBreakerView();

        for (Map.Entry<Integer, List<BusbarSection>> entry : busbarSectionsToConnectByIndex.entrySet()) {
            Integer sectionIndex = entry.getKey();
            List<BusbarSection> busbarSections = entry.getValue();
            List<BusbarSection> existingBusbarSections = busbarSectionsByIndex.getOrDefault(sectionIndex, Collections.emptyList());

            Map<Integer, SwitchCreationData> switchesToCreate = new HashMap<>();
            for (BusbarSection busbarSection : existingBusbarSections) {
                switchesToCreate.putAll(getSwitchesConnectingConnectables(busbarSection));
            }

            for (BusbarSection bbs : busbarSections) {
                int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();

                for (Map.Entry<Integer, SwitchCreationData> switchEntry : switchesToCreate.entrySet()) {
                    int node = switchEntry.getKey();
                    SwitchCreationData switchData = switchEntry.getValue();

                    if (shouldSkipSwitchCreation(bbs, switchData)) {
                        continue;
                    }
                    switchData.connectables.stream().filter(c -> !(c instanceof BusbarSection)).forEach(connectedFeeders::add);
                    createSwitch(vl, namingStrategy, nodeBreakerView, bbsNode, node, switchData.connectables.get(0), switchData);
                }
            }
        }
        return connectedFeeders;
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

    private void createSwitch(VoltageLevel vl, NamingStrategy namingStrategy, VoltageLevel.NodeBreakerView nodeBreakerView,
                              int bbsNode, int node, Connectable<?> connectable, SwitchCreationData switchCreationData) {
        boolean isDisconnector = switchCreationData.sw.getKind() == SwitchKind.DISCONNECTOR;
        String switchId = getSwitchId(namingStrategy, node, connectable, isDisconnector, bbsNode, vl.getId());

        if (isDisconnector) {
            createNBDisconnector(bbsNode, node, switchId, nodeBreakerView, true, switchCreationData.sw.isFictitious());
        } else {
            createNBBreaker(bbsNode, node, switchId, nodeBreakerView, true, switchCreationData.sw.isFictitious());
        }
    }

    private String getSwitchId(NamingStrategy namingStrategy, int connectableNode, Connectable<?> connectable, boolean isDisconnector, int bbsNode, String voltageLevelId) {
        String switchId;
        if (connectable instanceof BusbarSection) {
            switchId = isDisconnector
                    ? namingStrategy.getDisconnectorId(couplingDeviceSwitchPrefixId, bbsNode, connectableNode)
                    : namingStrategy.getBreakerId(couplingDeviceSwitchPrefixId, bbsNode, connectableNode);
        } else {
            int side = getSide(connectable, voltageLevelId);
            String baseId = namingStrategy.getSwitchBaseId(connectable, side);
            switchId = isDisconnector
                    ? namingStrategy.getDisconnectorId(baseId, bbsNode, connectableNode)
                    : namingStrategy.getBreakerId(baseId, bbsNode, connectableNode);
        }
        return switchId;
    }

    private int getSide(Connectable<?> connectable, String voltageLevelId) {
        if (connectable instanceof Branch<?> b) {
            return b.getTerminal(voltageLevelId).getSide().getNum();
        } else if (connectable instanceof ThreeWindingsTransformer threeWindingsTransformer) {
            return threeWindingsTransformer.getTerminal(voltageLevelId).getSide().getNum();
        }
        // Connectable has only one side
        return 0;
    }

    public record SwitchCreationData(Switch sw, List<Connectable<?>> connectables, BusbarSection startBusbarSection) {
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
            if (sw == null) {
                return TraverseResult.CONTINUE;
            }

            if (fromNode == startNode) { // We keep switches that are connected to the busbar section
                firstNode[0] = toNode;
                switchesPerNode.put(toNode, sw);
            }

            Optional<Terminal> terminalOpt = nodeBreakerView.getOptionalTerminal(toNode);
            if (terminalOpt.isEmpty()) {
                return TraverseResult.CONTINUE;
            }

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
}
