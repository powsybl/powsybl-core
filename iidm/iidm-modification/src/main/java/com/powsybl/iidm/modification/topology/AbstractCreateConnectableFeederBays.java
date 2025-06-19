/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.modification.util.ModificationLogs;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
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

    protected abstract boolean getForceExtensionCreation(int side);

    protected AbstractCreateConnectableFeederBays(int... sides) {
        this.sides = Arrays.copyOf(sides, sides.length);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        // Set the connectable bus or node
        if (!setAdderConnectivity(network, reportNode, throwException)) {
            return;
        }

        // Check if ConnectablePosition extension can and should be created
        Map<Integer, Boolean> createExtension = new HashMap<>();
        for (int side : sides) {
            String busOrBusbarSectionId = getBusOrBusbarSectionId(side);
            Identifiable<?> busOrBusbarSection = network.getIdentifiable(busOrBusbarSectionId);
            if (busOrBusbarSection instanceof BusbarSection bbs) {
                VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
                Set<Integer> takenFeederPositions = TopologyModificationUtils.getFeederPositions(voltageLevel);
                boolean checkOrderValue = checkOrderValue(side, bbs, takenFeederPositions, reportNode, throwException);
                if (!checkOrderValue) {
                    if (getForceExtensionCreation(side)) {
                        return;
                    } else {
                        createExtension.put(side, false);
                    }
                } else {
                    createExtension.put(side, true);
                }
            }
        }

        // Add the element on the network
        Connectable<?> connectable = add();
        if (!checkNetworks(connectable, network, reportNode, throwException)) {
            return;
        }

        LOGGER.info("New connectable {} of type {} created", connectable.getId(), connectable.getType());
        createdConnectable(reportNode, connectable);

        createExtensionAndTopology(createExtension, connectable, network, namingStrategy, reportNode);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        for (int side : sides) {
            // Get the busOrBusbarSection corresponding to the side parameter
            String busOrBusbarSectionId = getBusOrBusbarSectionId(side);
            Identifiable<?> busOrBusbarSection = network.getIdentifiable(busOrBusbarSectionId);

            if (busOrBusbarSection instanceof BusbarSection) {
                if (getPositionOrder(side) == null || getPositionOrder(side) < 0) {
                    impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
                    return impact;
                }
            } else if (!(busOrBusbarSection instanceof Bus)) {
                impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
                return impact;
            }
        }
        return impact;
    }

    private boolean checkOrders(int side, Identifiable<?> busOrBusbarSection, ReportNode reportNode, boolean throwException) {
        Integer positionOrder = getPositionOrder(side);
        if (busOrBusbarSection instanceof BusbarSection bbs) {
            VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
            if (positionOrder == null) {
                unexpectedNullPositionOrder(reportNode, voltageLevel.getId());
                logOrThrow(throwException, "Position order is null for attachment in node-breaker voltage level " + voltageLevel.getId());
                return false;
            }
            if (positionOrder < 0) {
                unexpectedNegativePositionOrder(reportNode, positionOrder, voltageLevel.getId());
                logOrThrow(throwException, "Position order is negative for attachment in node-breaker voltage level " + voltageLevel.getId() + ": " + positionOrder);
                return false;
            }
        }
        if (positionOrder != null && busOrBusbarSection instanceof Bus bus) {
            VoltageLevel voltageLevel = bus.getVoltageLevel();
            ignoredPositionOrder(reportNode, positionOrder, voltageLevel);
            LOGGER.warn("Voltage level {} is BUS_BREAKER. Position order {} is ignored", voltageLevel.getId(), positionOrder);
        }
        return true;
    }

    private boolean checkOrderValue(int side, BusbarSection busbarSection, Set<Integer> takenFeederPositions, ReportNode reportNode, boolean throwException) {
        Integer positionOrder = getPositionOrder(side);

        if (takenFeederPositions.contains(positionOrder)) {
            if (getForceExtensionCreation(side)) {
                LOGGER.error("PositionOrder {} already taken.", positionOrder);
                positionOrderAlreadyTakenReport(reportNode, positionOrder, TypedValue.ERROR_SEVERITY);
                if (throwException) {
                    throw new PowsyblException("PositionOrder " + positionOrder + " already taken.");
                }
            } else {
                LOGGER.warn("PositionOrder {} already taken.", positionOrder);
                positionOrderAlreadyTakenReport(reportNode, positionOrder, TypedValue.WARN_SEVERITY);
            }
            return false;
        }

        Optional<Range<Integer>> positionRangeForSection = getPositionRange(busbarSection);
        if (positionRangeForSection.isEmpty()) {
            if (getForceExtensionCreation(side)) {
                LOGGER.error("Positions of adjacent busbar sections do not leave slots for new positions on busbar section '{}'.", busbarSection.getId());
                positionNoSlotLeftByAdjacentBbsReport(reportNode, busbarSection.getId(), TypedValue.ERROR_SEVERITY);
                if (throwException) {
                    throw new PowsyblException("Positions of adjacent busbar sections do not leave slots for new positions on busbar section '" + busbarSection.getId() + "'.");
                }
            } else {
                LOGGER.warn("Positions of adjacent busbar sections do not leave slots for new positions on busbar section '{}'.", busbarSection.getId());
                positionNoSlotLeftByAdjacentBbsReport(reportNode, busbarSection.getId(), TypedValue.WARN_SEVERITY);
            }
            return false;
        }

        int minValue = positionRangeForSection.get().getMinimum();
        if (positionOrder < minValue) {
            if (getForceExtensionCreation(side)) {
                LOGGER.error("PositionOrder {} too low (<{}).", positionOrder, minValue);
                positionOrderTooLowReport(reportNode, minValue, positionOrder, TypedValue.ERROR_SEVERITY);
                if (throwException) {
                    throw new PowsyblException("PositionOrder " + positionOrder + " too low (<" + minValue + ").");
                }
            } else {
                LOGGER.warn("PositionOrder {} too low (<{}).", positionOrder, minValue);
                positionOrderTooLowReport(reportNode, minValue, positionOrder, TypedValue.WARN_SEVERITY);
            }
            return false;
        }

        int maxValue = positionRangeForSection.get().getMaximum();
        if (positionOrder > maxValue) {
            if (getForceExtensionCreation(side)) {
                LOGGER.error("PositionOrder {} too high (>{}).", positionOrder, maxValue);
                positionOrderTooHighReport(reportNode, maxValue, positionOrder, TypedValue.ERROR_SEVERITY);
                if (throwException) {
                    throw new PowsyblException("PositionOrder " + positionOrder + " too high (>" + maxValue + ").");
                }
            } else {
                LOGGER.warn("PositionOrder {} too high (>{}).", positionOrder, maxValue);
                positionOrderTooHighReport(reportNode, maxValue, positionOrder, TypedValue.WARN_SEVERITY);
            }
            return false;
        }

        return true;
    }

    /**
     * Set the connectable bus/node on the injection or branch adder for each side of the Feeder Bay(s)
     * @return true if the connectable bus(es) or node(s) has(have) been set, else return false
     */
    private boolean setAdderConnectivity(Network network, ReportNode reportNode, boolean throwException) {
        Map<VoltageLevel, Integer> firstAvailableNodes = new HashMap<>();
        for (int side : sides) {
            // Get the busOrBusbarSection corresponding to the side parameter
            String busOrBusbarSectionId = getBusOrBusbarSectionId(side);
            Identifiable<?> busOrBusbarSection = network.getIdentifiable(busOrBusbarSectionId);
            if (busOrBusbarSection == null) {
                ModificationLogs.busOrBbsDoesNotExist(busOrBusbarSectionId, reportNode, throwException);
                return false;
            }

            // Set the connectable bus/node on the injection or branch adder
            if (busOrBusbarSection instanceof Bus bus) {
                // if bus is an identifiable, the voltage level is BUS_BREAKER
                checkOrders(side, bus, reportNode, throwException); // is always true, can only return a warning
                setBus(side, bus, bus.getVoltageLevel().getId());
            } else if (busOrBusbarSection instanceof BusbarSection bbs) {
                // if bbs exists, the voltage level is NODE_BREAKER: no necessary topology kind check
                VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
                if (!checkOrders(side, bbs, reportNode, throwException)) {
                    return false;
                }
                // Store or update in the hashmap the next available node for the current voltage level
                int connectableNode = firstAvailableNodes.compute(voltageLevel, this::getNextAvailableNode);
                setNode(side, connectableNode, voltageLevel.getId());
            } else {
                unsupportedIdentifiableType(reportNode, busOrBusbarSection.getType(), busOrBusbarSectionId);
                logOrThrow(throwException, String.format("Unsupported type %s for identifiable %s", busOrBusbarSection.getType(), busOrBusbarSectionId));
                return false;
            }
        }
        return true;
    }

    private int getNextAvailableNode(VoltageLevel vl, Integer node) {
        return node == null ? vl.getNodeBreakerView().getMaximumNodeIndex() + 1 : node + 1;
    }

    private static boolean checkNetworks(Connectable<?> connectable, Network network, ReportNode reportNode, boolean throwException) {
        if (connectable.getNetwork() != network) {
            connectable.remove();
            networkMismatchReport(reportNode, connectable.getId(), connectable.getType());
            logOrThrow(throwException, String.format("Network given in parameters and in connectableAdder are different. Connectable %s of type %s was added then removed",
                    connectable.getId(), connectable.getType()));
            return false;
        }
        return true;
    }

    private void createExtensionAndTopology(Map<Integer, Boolean> createExtension, Connectable<?> connectable, Network network, NamingStrategy namingStrategy, ReportNode reportNode) {
        String connectableId = connectable.getId();
        boolean createConnectablePosition = false;
        ConnectablePositionAdder<?> connectablePositionAdder = connectable.newExtension(ConnectablePositionAdder.class);
        for (int side : sides) {
            VoltageLevel voltageLevel = getVoltageLevel(side, connectable);
            String busOrBusbarSectionId = getBusOrBusbarSectionId(side);
            if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
                continue; // no extension nor switches created in bus-breaker topology
            }

            // Get the set of existing position extensions on other connectables
            Set<Integer> takenFeederPositions = TopologyModificationUtils.getFeederPositions(voltageLevel);

            // Get the wanted position for the new connectable
            int positionOrder = getPositionOrder(side);
            if (!takenFeederPositions.isEmpty() || voltageLevel.getConnectableStream().filter(c -> !(c instanceof BusbarSection)).count() == 1) {
                // check that there are existing position extensions on other connectables or there is only one connectable (that we added)
                if (createExtension.get(side)) { // BusbarSection as voltage level is NODE_BREAKER
                    getFeederAdder(side, connectablePositionAdder)
                            .withDirection(getDirection(side))
                            .withOrder(positionOrder)
                            .withName(getFeederName(side).orElse(connectableId))
                            .add();
                    createConnectablePosition = true;
                }
            } else {
                LOGGER.warn("No ConnectablePosition extension found on voltageLevel {}. The ConnectablePosition extension is not created for new feeder {}.", voltageLevel.getId(), connectableId);
                noConnectablePositionExtension(reportNode, voltageLevel, connectableId);
            }
            int connectableNode = getNode(side, connectable);
            // create switches and a breaker linking the connectable to the busbar sections.
            createTopologyWithConnectableNode(side, busOrBusbarSectionId, network, voltageLevel, connectableNode, connectable, namingStrategy, reportNode);
        }
        if (createConnectablePosition) {
            connectablePositionAdder.add();
        }
    }
}
