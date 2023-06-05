/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractCreateConnectableFeederBays extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCreateConnectableFeederBays.class);

    protected final int[] sides;

    protected abstract String getBusOrBusbarSectionId(int side);

    protected abstract void setBus(int side, Bus bus, String voltageLevelId);

    protected abstract void setNode(int side, int node, String voltageLevelId);

    protected abstract Connectable<?> add();

    protected abstract VoltageLevel getVoltageLevel(int side, Connectable<?> connectable);

    protected abstract Integer getPositionOrder(int side);

    protected abstract Optional<String> getFeederName(int side);

    protected abstract ConnectablePosition.Direction getDirection(int side);

    protected abstract int getNode(int side, Connectable<?> connectable);

    protected abstract ConnectablePositionAdder.FeederAdder<?> getFeederAdder(int side, ConnectablePositionAdder<?> connectablePositionAdder);

    protected AbstractCreateConnectableFeederBays(int... sides) {
        this.sides = Arrays.copyOf(sides, sides.length);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        if (!setAdderConnectivity(network, reporter, throwException)) {
            return;
        }
        Connectable<?> connectable = add();
        if (!checkNetworks(connectable, network, reporter, throwException)) {
            return;
        }

        LOGGER.info("New connectable {} of type {} created", connectable.getId(), connectable.getType());
        createdConnectable(reporter, connectable);

        createExtensionAndTopology(connectable, network, reporter);
    }

    private boolean checkOrders(int side, VoltageLevel voltageLevel, Reporter reporter, boolean throwException) {
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        Integer positionOrder = getPositionOrder(side);
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            if (positionOrder == null) {
                unexpectedNullPositionOrder(reporter, voltageLevel.getId());
                LOGGER.error("Position order is null for attachment in node-breaker voltage level {}", voltageLevel.getId());
                if (throwException) {
                    throw new PowsyblException("Position order is null for attachment in node-breaker voltage level " + voltageLevel.getId());
                }
                return false;
            }
            if (positionOrder < 0) {
                unexpectedNegativePositionOrder(reporter, positionOrder, voltageLevel.getId());
                LOGGER.error("Position order is negative ({}) for attachment in node-breaker voltage level {}", positionOrder, voltageLevel.getId());
                if (throwException) {
                    throw new PowsyblException("Position order is negative for attachment in node-breaker voltage level " + voltageLevel.getId() + ": " + positionOrder);
                }
                return false;
            }
        }
        if (positionOrder != null && topologyKind == TopologyKind.BUS_BREAKER) {
            ignoredPositionOrder(reporter, positionOrder, voltageLevel);
            LOGGER.warn("Voltage level {} is BUS_BREAKER. Position order {} is ignored", voltageLevel.getId(), positionOrder);
        }
        return true;
    }

    private boolean checkOrderValue(int side, BusbarSection busbarSection, Set<Integer> takenFeederPositions, Reporter reporter) {
        Integer positionOrder = getPositionOrder(side);

        if (takenFeederPositions.contains(positionOrder)) {
            LOGGER.warn("PositionOrder {} already taken. No position extension created.", positionOrder);
            positionOrderAlreadyTakenReport(reporter, positionOrder);
            return false;
        }

        Optional<Range<Integer>> positionRangeForSection = getPositionRange(busbarSection);
        if (positionRangeForSection.isEmpty()) {
            LOGGER.warn("Positions of adjacent busbar sections do not leave slots for new positions on busbar section '{}'. No position extension created.", busbarSection.getId());
            positionNoSlotLeftByAdjacentBbsReport(reporter, busbarSection.getId());
            return false;
        }

        int minValue = positionRangeForSection.get().getMinimum();
        if (positionOrder < minValue) {
            LOGGER.warn("PositionOrder {} too low (<{}). No position extension created.", positionOrder, minValue);
            positionOrderTooLowReport(reporter, minValue, positionOrder);
            return false;
        }

        int maxValue = positionRangeForSection.get().getMaximum();
        if (positionOrder > maxValue) {
            LOGGER.warn("PositionOrder {} too high (>{}). No position extension created.", positionOrder, maxValue);
            positionOrderTooHighReport(reporter, maxValue, positionOrder);
            return false;
        }

        return true;
    }

    private boolean setAdderConnectivity(Network network, Reporter reporter, boolean throwException) {
        Map<VoltageLevel, Integer> firstAvailableNodes = new HashMap<>();
        for (int side : sides) {
            String busOrBusbarSectionId = getBusOrBusbarSectionId(side);
            Identifiable<?> busOrBusbarSection = network.getIdentifiable(busOrBusbarSectionId);
            if (busOrBusbarSection == null) {
                ModificationLogs.busOrBbsDoesNotExist(busOrBusbarSectionId, reporter, throwException);
                return false;
            }
            if (busOrBusbarSection instanceof Bus) {
                Bus bus = (Bus) busOrBusbarSection; // if bus is an identifiable, the voltage level is BUS_BREAKER
                checkOrders(side, bus.getVoltageLevel(), reporter, throwException); // is always true, can only return a warning
                setBus(side, bus, bus.getVoltageLevel().getId());
            } else if (busOrBusbarSection instanceof BusbarSection) {
                BusbarSection bbs = (BusbarSection) busOrBusbarSection; // if bbs exists, the voltage level is NODE_BREAKER: no necessary topology kind check
                VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
                if (!checkOrders(side, voltageLevel, reporter, throwException)) {
                    return false;
                }
                int connectableNode = firstAvailableNodes.compute(voltageLevel, (vl, node) -> node == null ? vl.getNodeBreakerView().getMaximumNodeIndex() + 1 : node + 1);
                setNode(side, connectableNode, voltageLevel.getId());
            } else {
                LOGGER.error("Unsupported type {} for identifiable {}", busOrBusbarSection.getType(), busOrBusbarSectionId);
                unsupportedIdentifiableType(reporter, busOrBusbarSection.getType(), busOrBusbarSectionId);
                if (throwException) {
                    throw new PowsyblException(String.format("Unsupported type %s for identifiable %s", busOrBusbarSection.getType(), busOrBusbarSectionId));
                }
                return false;
            }
        }
        return true;
    }

    private static boolean checkNetworks(Connectable<?> connectable, Network network, Reporter reporter, boolean throwException) {
        if (connectable.getNetwork() != network) {
            connectable.remove();
            LOGGER.error("Network given in parameters and in connectableAdder are different. Connectable '{}' of type {} was added then removed",
                    connectable.getId(), connectable.getType());
            networkMismatchReport(reporter, connectable.getId(), connectable.getType());
            if (throwException) {
                throw new PowsyblException("Network given in parameters and in connectableAdder are different. Connectable was added then removed");
            }
            return false;
        }
        return true;
    }

    private void createExtensionAndTopology(Connectable<?> connectable, Network network, Reporter reporter) {
        String connectableId = connectable.getId();
        boolean createConnectablePosition = false;
        ConnectablePositionAdder<?> connectablePositionAdder = connectable.newExtension(ConnectablePositionAdder.class);
        for (int side : sides) {
            VoltageLevel voltageLevel = getVoltageLevel(side, connectable);
            String busOrBusbarSectionId = getBusOrBusbarSectionId(side);
            Identifiable<?> busOrBusbarSection = network.getIdentifiable(busOrBusbarSectionId);
            if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
                continue; // no extension nor switches created in bus-breaker topology
            }
            Set<Integer> takenFeederPositions = TopologyModificationUtils.getFeederPositions(voltageLevel);
            int positionOrder = getPositionOrder(side);
            if (!takenFeederPositions.isEmpty() || voltageLevel.getConnectableStream().filter(c -> !(c instanceof BusbarSection)).count() == 1) {
                // check that there is only one connectable (that we added) or there are existing position extensions on other connectables
                if (checkOrderValue(side, (BusbarSection) busOrBusbarSection, takenFeederPositions, reporter)) { // BusbarSection as voltage level is NODE_BREAKER
                    getFeederAdder(side, connectablePositionAdder)
                            .withDirection(getDirection(side))
                            .withOrder(positionOrder)
                            .withName(getFeederName(side).orElse(connectableId))
                            .add();
                    createConnectablePosition = true;
                }
            } else {
                LOGGER.warn("No order positions found on voltageLevel {}. The extension is not created.", voltageLevel.getId());
                noConnectablePositionExtension(reporter, voltageLevel);
            }
            // create switches and a breaker linking the connectable to the busbar sections.
            int node = getNode(side, connectable);
            int forkNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createTopology(side, network, voltageLevel, node, forkNode, connectable, reporter);
        }
        if (createConnectablePosition) {
            connectablePositionAdder.add();
        }
    }

    private void createTopology(int side, Network network, VoltageLevel voltageLevel, int connectableNode, int forkNode, Connectable<?> connectable, Reporter reporter) {
        String baseId = connectable.getId() + (side == 0 ? "" : side);
        String bbsId = getBusOrBusbarSectionId(side);
        BusbarSection bbs = network.getBusbarSection(bbsId);
        int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
        createNodeBreakerSwitches(connectableNode, forkNode, bbsNode, baseId, voltageLevel.getNodeBreakerView());
        BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);
        int parallelBbsNumber = 0;
        if (position == null) {
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs);
        } else {
            List<BusbarSection> bbsList = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
                    .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                    .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex())
                    .filter(b -> !b.getId().equals(bbsId)).collect(Collectors.toList());
            parallelBbsNumber = bbsList.size();
            createTopologyFromBusbarSectionList(voltageLevel, forkNode, baseId, bbsList);
        }
        LOGGER.info("New feeder bay associated to {} of type {} was created and connected to voltage level {} on busbar section {} with a closed disconnector" +
                "and on {} parallel busbar sections with an open disconnector.", connectable.getId(), connectable.getType(), voltageLevel.getId(), bbsId, parallelBbsNumber);
        createdNodeBreakerFeederBay(reporter, voltageLevel.getId(), bbsId, connectable, parallelBbsNumber);
    }
}
