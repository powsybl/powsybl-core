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
import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.cleanNodeBreakerTopology;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createTopologyAndGetConnectableNode;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.moveFeederBayBusbarSectionReport;
import static com.powsybl.iidm.modification.util.ModificationReports.notFoundConnectableReport;

/**
 * This modification moves a feeder bay from one busBar section to another.
 * It identifies and updates the relevant switches and connections to achieve the move operation.
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public class MoveFeederBay extends AbstractNetworkModification {
    private final String connectableId;
    private final String targetBusOrBusBarSectionId;
    private final String targetVoltageLevelId;
    private final Terminal terminal;

    /**
     * @param connectableId non-null id of the connectable whose feeder bay will be moved (BusOrBusBarSection are not accepted)
     * @param targetBusOrBusBarId non-null id of the target BusOrBusBar section
     * @param targetVoltageLevelId non-null id of the target voltage Level
     * @param terminal non-null terminal
     */
    MoveFeederBay(String connectableId, String targetBusOrBusBarId, String targetVoltageLevelId, Terminal terminal) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.targetBusOrBusBarSectionId = Objects.requireNonNull(targetBusOrBusBarId);
        this.targetVoltageLevelId = Objects.requireNonNull(targetVoltageLevelId);
        this.terminal = Objects.requireNonNull(terminal);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        // Get and validate connectable
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (!validateConnectable(connectable, throwException, reportNode)) {
            return;
        }

        // Get voltage level and perform move operation based on topology kind
        VoltageLevel voltageLevel = network.getVoltageLevel(targetVoltageLevelId);
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            moveInNodeBreakerTopology(network, connectable, voltageLevel, namingStrategy, reportNode);
        } else {
            moveInBusBreakerTopology(terminal);
        }
    }

    /**
     * Move the connectable in node-breaker topology
     */
    private void moveInNodeBreakerTopology(Network network, Connectable<?> connectable,
                                           VoltageLevel voltageLevel, NamingStrategy namingStrategy,
                                           ReportNode reportNode) {
        // Clean existing topology
        cleanNodeBreakerTopology(connectableId, reportNode, terminal);

        // Create new topology and Get node information
        int side = getSideFromTerminal(terminal, connectable);
        int connectableNode = createTopologyAndGetConnectableNode(side, targetBusOrBusBarSectionId, network, voltageLevel, connectable, namingStrategy, reportNode);

        // Move the terminal of the connectable to the new node
        terminal.getNodeBreakerView().moveConnectable(connectableNode, voltageLevel.getId());
    }

    /**
     * Move the connectable in bus-breaker topology
     */
    private void moveInBusBreakerTopology(Terminal terminal) {
        terminal.getBusBreakerView().moveConnectable(targetBusOrBusBarSectionId, terminal.isConnected());
    }

    /**
     * Get the side value
     */
    private int getSideFromTerminal(Terminal terminal, Connectable<?> connectable) {
        if (connectable instanceof Injection<?>) {
            return 0;
        } else if (connectable instanceof Branch<?> || connectable instanceof ThreeWindingsTransformer) {
            return terminal.getSide().getNum();
        }
        throw new IllegalStateException("Unsupported connectable type: " + connectable.getClass().getSimpleName());
    }

    @Override
    public String getName() {
        return "MoveFeederBay";
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Connectable<?> connectable = network.getConnectable(connectableId);
        BusbarSection targetBusbarSection = network.getBusbarSection(targetBusOrBusBarSectionId);

        // Check preconditions: valid connectable and target busbar section
        if (connectable == null || connectable instanceof BusbarSection || targetBusbarSection == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            return impact;
        }

        VoltageLevel targetVoltageLevel = targetBusbarSection.getTerminal().getVoltageLevel();

        // Check if any terminal is in the same voltage level
        boolean hasTerminalInTargetVoltageLevel = connectable.getTerminals().stream()
                .anyMatch(t -> t.getVoltageLevel().getId().equals(targetVoltageLevel.getId()));

        if (!hasTerminalInTargetVoltageLevel) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            return impact;
        }
        return impact;
    }

    /**
     * Check if the connectable is valid for this modification
     */
    private boolean validateConnectable(Connectable<?> connectable, boolean throwException, ReportNode reportNode) {
        if (connectable == null) {
            notFoundConnectableReport(reportNode, connectableId);
            logOrThrow(throwException, "Connectable not found: " + connectableId);
            return false;
        }

        if (connectable instanceof BusbarSection) {
            moveFeederBayBusbarSectionReport(reportNode, connectableId);
            logOrThrow(throwException, "BusbarSection connectables are not allowed as MoveFeederBay input: " + connectableId);
            return false;
        }

        return true;
    }
}
