/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.util.NetworkReports;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import static com.powsybl.iidm.network.TopologyKind.BUS_BREAKER;
import static com.powsybl.iidm.network.TopologyKind.NODE_BREAKER;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class ConnectDisconnectUtil {

    private ConnectDisconnectUtil() {
    }

    /**
     * Connect the specified terminals. It will connect all the specified terminals or none if at least one cannot be
     * connected.
     * @param identifiable network element to connect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be connected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isTypeSwitchToOperate type of switches that can be operated
     * @param connectionElementsContainer container for elements that will be operated
     * @param operateSwitchesFromHere if {@code true}, the switches will be operated. Else, the elements will be added to the connectionElementsContainer
     * @param propagateConnectionIfNeeded if {@code true}, the connection can be propagated trough lines and voltage levels until it is either done or not possible
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been connected, else {@code false}.
     */
    static ConnectDisconnectStatus connectAllTerminals(Identifiable<?> identifiable, List<? extends Terminal> terminals,
                                       Predicate<Switch> isTypeSwitchToOperate,
                                       ConnectionElementsContainer connectionElementsContainer,
                                       boolean operateSwitchesFromHere, boolean propagateConnectionIfNeeded,
                                       ReportNode reportNode) {

        // Booleans
        boolean isAlreadyConnected = true;
        boolean isNowConnected = true;

        // Initialize a list of switches to close and of bus-breaker terminals to connect
        ConnectionElementsContainer localConnectionElementsContainer = new ConnectionElementsContainer(new HashSet<>(), new HashSet<>(), new HashSet<>(connectionElementsContainer.connectables()));

        // We try to connect each terminal
        for (Terminal terminal : terminals) {
            // Check if the terminal is already connected
            if (terminal.isConnected() && !propagateConnectionIfNeeded) {
                NetworkReports.alreadyConnectedIdentifiableTerminal(reportNode, identifiable.getId());
                continue;
            } else {
                isAlreadyConnected = false;
            }

            // If it's a node-breaker terminal, the switches to connect are added to a set
            if (terminal.getVoltageLevel().getTopologyKind() == NODE_BREAKER) {
                NodeBreakerTopologyModel topologyModel = (NodeBreakerTopologyModel) ((VoltageLevelImpl) terminal.getVoltageLevel()).getTopologyModel();
                isNowConnected = topologyModel.canTheTerminalBeConnected(terminal, isTypeSwitchToOperate, propagateConnectionIfNeeded,
                    localConnectionElementsContainer);
            } else if (terminal.getVoltageLevel().getTopologyKind() == BUS_BREAKER) {
                // If it's a bus-breaker terminal, the terminal is just added to the set
                localConnectionElementsContainer.busBreakerTerminalsToOperate().add(terminal);
            }

            // Exit if the terminal cannot be connected
            if (!isNowConnected) {
                return ConnectDisconnectStatus.FAILURE;
            }
        }

        // Exit if the connectable is already fully connected
        if (isAlreadyConnected) {
            return ConnectDisconnectStatus.NO_CHANGE_NEEDED;
        }

        if (operateSwitchesFromHere) {
            // Close all switches on node-breaker terminals to connect the network element
            localConnectionElementsContainer.switchesToOperate().forEach(sw -> sw.setOpen(false));

            // Connect all bus-breaker terminals
            localConnectionElementsContainer.busBreakerTerminalsToOperate().forEach(t -> t.connect(isTypeSwitchToOperate));
        } else {
            // Add the switches and the bus-breaker terminals to the sets
            connectionElementsContainer.addAll(localConnectionElementsContainer);
        }

        return ConnectDisconnectStatus.SUCCESS;
    }

    /**
     * Disconnect the specified terminals. It will disconnect all the specified terminals or none if at least one cannot
     * be disconnected.
     * @param identifiable network element to disconnect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be disconnected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isSwitchOpenable type of switches that can be operated
     * @param connectionElementsContainer container for elements that will be operated
     * @param operateSwitchesFromHere if {@code true}, the switches will be operated. Else, the elements will be added to the connectionElementsContainer
     * @param propagateDisconnectionIfNeeded if {@code true}, the disconnection can be propagated trough lines and voltage levels until it is either done or not possible
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been disconnected, else {@code false}.
     */
    static ConnectDisconnectStatus disconnectAllTerminals(Identifiable<?> identifiable, List<? extends Terminal> terminals,
                                          Predicate<Switch> isSwitchOpenable,
                                          ConnectionElementsContainer connectionElementsContainer,
                                          boolean operateSwitchesFromHere, boolean propagateDisconnectionIfNeeded,
                                          ReportNode reportNode) {
        // Booleans
        boolean isAlreadyDisconnected = true;

        // Initialize a list of switches to open and of bus-breaker terminals to disconnect
        ConnectionElementsContainer localConnectionElementsContainer = new ConnectionElementsContainer(new HashSet<>(), new HashSet<>(), new HashSet<>(connectionElementsContainer.connectables()));

        // We try to disconnect each terminal
        for (Terminal terminal : terminals) {
            // Check if the terminal is already disconnected
            if (!terminal.isConnected()) {
                NetworkReports.alreadyDisconnectedIdentifiableTerminal(reportNode, identifiable.getId());
                continue;
            }
            // The terminal is connected
            isAlreadyDisconnected = false;

            // If it's a node-breaker terminal, the switches to disconnect are added to a set
            if (terminal.getVoltageLevel().getTopologyKind() == NODE_BREAKER) {
                NodeBreakerTopologyModel topologyModel = (NodeBreakerTopologyModel) ((VoltageLevelImpl) terminal.getVoltageLevel()).getTopologyModel();
                if (!topologyModel.canTheTerminalBeDisconnected(terminal, isSwitchOpenable, propagateDisconnectionIfNeeded, localConnectionElementsContainer)) {
                    // Exit if the terminal cannot be disconnected
                    return ConnectDisconnectStatus.FAILURE;
                }
            } else if (terminal.getVoltageLevel().getTopologyKind() == BUS_BREAKER) {
                // If it's a bus-breaker terminal, the terminal is just added to the set
                localConnectionElementsContainer.busBreakerTerminalsToOperate().add(terminal);
            }
        }

        // Exit if the connectable is already fully disconnected
        if (isAlreadyDisconnected) {
            return ConnectDisconnectStatus.NO_CHANGE_NEEDED;
        }

        if (operateSwitchesFromHere) {
            // Open all switches on node-breaker terminals to disconnect the network element
            localConnectionElementsContainer.switchesToOperate().forEach(sw -> sw.setOpen(true));

            // Disconnect all bus-breaker terminals
            localConnectionElementsContainer.busBreakerTerminalsToOperate().forEach(t -> t.disconnect(isSwitchOpenable));
        } else {
            // Add the switches and the bus-breaker terminals to the sets
            connectionElementsContainer.addAll(localConnectionElementsContainer);
        }

        return ConnectDisconnectStatus.SUCCESS;
    }

    enum ConnectDisconnectStatus {
        SUCCESS,
        FAILURE,
        NO_CHANGE_NEEDED
    }
}
