/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.powsybl.iidm.network.TopologyKind.BUS_BREAKER;

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
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been connected, else {@code false}.
     */
    static boolean connectAllTerminals(Identifiable<?> identifiable, List<? extends Terminal> terminals,
                                       Predicate<Switch> isTypeSwitchToOperate, ReportNode reportNode) {
        return connectAllTerminals(identifiable, terminals, isTypeSwitchToOperate, false, reportNode);
    }

    /**
     * Connect the specified terminals. It will connect all the specified terminals or none if at least one cannot be
     * connected.
     * @param identifiable network element to connect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be connected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isTypeSwitchToOperate type of switches that can be operated
     * @param dryRun is it a dry run? If {@code true}, no terminal will be effectively connected.
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been connected (or can be connected, when
     * {@code dryRun} is {@code true}), else {@code false}.
     */
    static boolean connectAllTerminals(Identifiable<?> identifiable, List<? extends Terminal> terminals,
                                       Predicate<Switch> isTypeSwitchToOperate, boolean dryRun, ReportNode reportNode) {
        // Booleans
        boolean isAlreadyConnected = true;
        boolean isNowConnected = true;

        // Initialisation of a list to open in case some terminals are in node-breaker view
        Set<SwitchImpl> switchForDisconnection = new HashSet<>();

        // We try to connect each terminal
        for (Terminal terminal : terminals) {
            // Check if the terminal is already connected
            if (terminal.isConnected()) {
                reportNode.newReportNode()
                    .withMessageTemplate("alreadyConnectedTerminal", "A terminal of identifiable ${identifiable} is already connected.")
                    .withUntypedValue("identifiable", identifiable.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
                continue;
            } else {
                isAlreadyConnected = false;
            }

            // If it's a node-breaker terminal, the switches to connect are added to a set
            if (terminal.getVoltageLevel() instanceof NodeBreakerVoltageLevel nodeBreakerVoltageLevel) {
                isNowConnected = nodeBreakerVoltageLevel.getConnectingSwitches(terminal, isTypeSwitchToOperate, switchForDisconnection);
            }
            // If it's a bus-breaker terminal, there is nothing to do

            // Exit if the terminal cannot be connected
            if (!isNowConnected) {
                return false;
            }
        }

        // Exit if the connectable is already fully connected
        if (isAlreadyConnected) {
            return false;
        }

        // Exit if it's a dry run
        if (dryRun) {
            return true;
        }

        // Connect all bus-breaker terminals
        for (Terminal terminal : terminals) {
            if (!terminal.isConnected()
                && terminal.getVoltageLevel().getTopologyKind() == BUS_BREAKER) {
                // At this point, isNowConnected should always stay true but let's be careful
                isNowConnected = isNowConnected && terminal.connect(isTypeSwitchToOperate);
            }
        }

        // Disconnect all switches on node-breaker terminals
        switchForDisconnection.forEach(sw -> sw.setOpen(false));
        return isNowConnected;
    }

    /**
     * Disconnect the specified terminals. It will disconnect all the specified terminals or none if at least one cannot
     * be disconnected.
     * @param identifiable network element to disconnect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be connected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isSwitchOpenable type of switches that can be operated
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been disconnected, else {@code false}.
     */
    static boolean disconnectAllTerminals(Identifiable<?> identifiable, List<? extends Terminal> terminals,
                                          Predicate<Switch> isSwitchOpenable, ReportNode reportNode) {
        return disconnectAllTerminals(identifiable, terminals, isSwitchOpenable, false, reportNode);
    }

    /**
     * Disconnect the specified terminals. It will disconnect all the specified terminals or none if at least one cannot
     * be disconnected.
     * @param identifiable network element to disconnect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be connected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isSwitchOpenable type of switches that can be operated
     * @param dryRun is it a dry run? If {@code true}, no terminal will be effectively disconnected.
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been disconnected (or can be disconnected, when
     * {@code dryRun} is {@code true}), else {@code false}.
     */
    static boolean disconnectAllTerminals(Identifiable<?> identifiable, List<? extends Terminal> terminals,
                                          Predicate<Switch> isSwitchOpenable, boolean dryRun, ReportNode reportNode) {
        // Booleans
        boolean isAlreadyDisconnected = true;
        boolean isNowDisconnected = true;

        // Initialisation of a list to open in case some terminals are in node-breaker view
        Set<SwitchImpl> switchForDisconnection = new HashSet<>();

        // We try to disconnect each terminal
        for (Terminal terminal : terminals) {
            // Check if the terminal is already disconnected
            if (!terminal.isConnected()) {
                reportNode.newReportNode()
                    .withMessageTemplate("alreadyDisconnectedTerminal", "A terminal of identifiable ${identifiable} is already disconnected.")
                    .withUntypedValue("identifiable", identifiable.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
                continue;
            }
            // The terminal is connected
            isAlreadyDisconnected = false;

            // If it's a node-breaker terminal, the switches to disconnect are added to a set
            if (terminal.getVoltageLevel() instanceof NodeBreakerVoltageLevel nodeBreakerVoltageLevel
                && !nodeBreakerVoltageLevel.getDisconnectingSwitches(terminal, isSwitchOpenable, switchForDisconnection)) {
                // Exit if the terminal cannot be disconnected
                return false;
            }
            // If it's a bus-breaker terminal, there is nothing to do
        }

        // Exit if the connectable is already fully disconnected
        if (isAlreadyDisconnected) {
            return false;
        }

        // Exit if it's a dry run
        if (dryRun) {
            return true;
        }

        // Disconnect all bus-breaker terminals
        for (Terminal terminal : terminals) {
            if (terminal.isConnected()
                && terminal.getVoltageLevel().getTopologyKind() == BUS_BREAKER) {
                // At this point, isNowDisconnected should always stay true but let's be careful
                isNowDisconnected = isNowDisconnected && terminal.disconnect(isSwitchOpenable);
            }
        }
        // Disconnect all switches on node-breaker terminals
        switchForDisconnection.forEach(sw -> sw.setOpen(true));
        return isNowDisconnected;
    }
}
