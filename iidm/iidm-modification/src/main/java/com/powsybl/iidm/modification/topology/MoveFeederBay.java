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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.cleanNodeBreakerTopology;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createTopology;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.moveFeederBayBusbarSectionReport;
import static com.powsybl.iidm.modification.util.ModificationReports.notFoundConnectableReport;

/**
 * This modification moves a feeder bay from one busbar section to another.
 * It identifies and updates the relevant switches and connections to achieve the move operation.
 *
 * @author  Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public class MoveFeederBay extends AbstractNetworkModification {
    private final String connectableId;
    private final String targetBusOrBusbarSectionId;
    private final String targetVoltageLevelId;
    private final Terminal terminal;

    /**
     * Constructor
     * @param connectableId non-null id of the connectable whose feeder bay will be moved (busbar sections are not accepted)
     * @param terminal non-null id of the terminal
     * @param targetBusbarSectionId non-null id of the target busbar section
     * @param targetVoltageLevelId non-null id of the target voltage Level
     */
    public MoveFeederBay(String connectableId, String targetBusbarSectionId, String targetVoltageLevelId, Terminal terminal) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.targetBusOrBusbarSectionId = Objects.requireNonNull(targetBusbarSectionId);
        this.targetVoltageLevelId = Objects.requireNonNull(targetVoltageLevelId);
        this.terminal = Objects.requireNonNull(terminal);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (!checkConnectable(throwException, reportNode, connectable)) {
            return;
        }
        VoltageLevel voltageLevel = network.getVoltageLevel(targetVoltageLevelId);
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            cleanNodeBreakerTopology(network, connectableId, reportNode);
            createTopology(getSideFromTerminal(terminal, connectable), targetBusOrBusbarSectionId, network, voltageLevel, connectable, namingStrategy, reportNode);
            Map<VoltageLevel, Integer> firstAvailableNodes = new HashMap<>();
            int connectableNode = firstAvailableNodes.compute(voltageLevel, this::getNextAvailableNode);
            terminal.getNodeBreakerView().moveConnectable(connectableNode, voltageLevel.getId());
        } else {
            terminal.getBusBreakerView().moveConnectable(targetBusOrBusbarSectionId, terminal.isConnected());
        }
    }

    private int getSideFromTerminal(Terminal terminal, Connectable<?> connectable) {
        if (connectable instanceof Injection<?>) {
            return 0;
        } else if (connectable instanceof Branch<?>) {
            return terminal.getSide().getNum();
        }
        throw new IllegalStateException("Unexpected connectable: " + connectable);
    }

    private int getNextAvailableNode(VoltageLevel vl, Integer node) {
        return node == null ? vl.getNodeBreakerView().getMaximumNodeIndex() + 1 : node + 1;
    }

    @Override
    public String getName() {
        return "MoveFeederBay";
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        NetworkModificationImpact impact = DEFAULT_IMPACT;

        // Check connectable and target busbar section exists
        Connectable<?> connectable = network.getConnectable(connectableId);
        BusbarSection targetBusbarSection = network.getBusbarSection(targetBusOrBusbarSectionId);
        if (connectable == null || connectable instanceof BusbarSection || targetBusbarSection == null) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            return impact;
        }

        // Check that at least one terminal is in the same voltage level as the target busbar section
        VoltageLevel targetVoltageLevel = targetBusbarSection.getTerminal().getVoltageLevel();
        boolean hasTerminalInTargetVoltageLevel = false;

        for (Terminal terminal : connectable.getTerminals()) {
            if (terminal.getVoltageLevel().getId().equals(targetVoltageLevel.getId())) {
                hasTerminalInTargetVoltageLevel = true;
                break;
            }
        }

        if (!hasTerminalInTargetVoltageLevel) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
            return impact;
        }

        return impact;
    }

    private boolean checkConnectable(boolean throwException, ReportNode reportNode, Connectable<?> connectable) {
        if (connectable instanceof BusbarSection) {
            moveFeederBayBusbarSectionReport(reportNode, connectableId);
            logOrThrow(throwException, "BusbarSection connectables are not allowed as MoveFeederBay input: " + connectableId);
            return false;
        }
        if (connectable == null) {
            notFoundConnectableReport(reportNode, connectableId);
            logOrThrow(throwException, "Connectable not found: " + connectableId);
            return false;
        }
        return true;
    }
}
