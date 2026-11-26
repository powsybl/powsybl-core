/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion.getDefaultIsOpen;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class TerminalConversion {

    private TerminalConversion() {
    }

    /**
     * Create a fictitious open switch at a CGMES terminal if it is disconnected, in node-breaker voltage levels only.
     *
     * @param network the network to update
     * @param cgmesTerminalId the CGMES id of the terminal
     * @param context the update context
     */
    public static void create(Network network, String cgmesTerminalId, Context context) {
        boolean connected = context.isCgmesTerminalConnected(cgmesTerminalId).orElse(true);
        if (createFictitiousSwitch(network, cgmesTerminalId, connected, context)) {
            addFictitiousSwitch(network, cgmesTerminalId, context);
        }
    }

    /**
     * Tell whether a fictitious switch must be created at a CGMES terminal.
     *
     * @param network the network to update
     * @param cgmesTerminalId the CGMES id of the terminal
     * @param connected the connected status of the terminal
     * @param context the update context
     * @return {@code true} if the terminal is disconnected, has no fictitious switch yet, and the configuration allows
     * the creation
     */
    private static boolean createFictitiousSwitch(Network network, String cgmesTerminalId, boolean connected, Context context) {
        // Terminal id shouldn't be null
        Objects.requireNonNull(cgmesTerminalId);

        // Only create if it's a disconnected terminal of a connectable that's not an in-service busbar section.
        Identifiable<?> identifiable = network.getIdentifiable(cgmesTerminalId);
        if (connected || identifiable == null || isInServiceBusbarSection(identifiable, cgmesTerminalId, context)) {
            return false;
        }

        // Check if a fictitious switch has already been created (from a previous update).
        if (network.getSwitchStream()
            .anyMatch(s -> "true".equals(s.getProperty(PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL))
                && cgmesTerminalId.equals(s.getProperty(PROPERTY_TERMINAL)))) {
            return false;
        }

        // Check if configuration allows fictitious switch creation.
        return switch (context.config().getCreateFictitiousSwitchesForDisconnectedTerminalsMode()) {
            case NEVER -> false;
            case ALWAYS -> true;
            case ALWAYS_EXCEPT_SWITCHES -> identifiable.getType() != IdentifiableType.SWITCH;
        };
    }

    /**
     * Tell whether an IIDM object is a busbar section that is not out of service. The disconnected terminal of such a
     * busbar section is ignored. The terminal of an out-of-service busbar section gets a fictitious switch, which detaches
     * the busbar section from its node.
     *
     * @param identifiable the IIDM object holding the terminal
     * @param cgmesTerminalId the CGMES id of the terminal
     * @param context the update context
     * @return {@code true} if the object is a busbar section that is not out of service
     */
    private static boolean isInServiceBusbarSection(Identifiable<?> identifiable, String cgmesTerminalId, Context context) {
        return identifiable.getType() == IdentifiableType.BUSBAR_SECTION && !context.isTerminalOfOutOfServiceEquipment(cgmesTerminalId);
    }

    /**
     * Add an open fictitious switch at a CGMES terminal of a node-breaker voltage level, which disconnects it.
     *
     * @param network the network to update
     * @param cgmesTerminalId the CGMES id of the terminal
     * @param context the update context
     */
    private static void addFictitiousSwitch(Network network, String cgmesTerminalId, Context context) {
        Identifiable<?> identifiable = network.getIdentifiable(cgmesTerminalId);
        if (identifiable instanceof Switch sw) {
            if (sw.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                createSwitchForSwitch(sw, getNode(sw, cgmesTerminalId), cgmesTerminalId, context);
            }
        } else if (identifiable instanceof Connectable<?> connectable) {
            Terminal terminal = getTerminal(connectable, cgmesTerminalId);
            if (terminal != null && terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                createSwitchForTerminal(terminal, cgmesTerminalId, context);
            }
        }
    }

    private static int getNode(Switch sw, String terminalId) {
        String aliasType = sw.getAliasType(terminalId).orElse("");
        if (ALIAS_TERMINAL1.equals(aliasType)) {
            return sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
        } else if (ALIAS_TERMINAL2.equals(aliasType)) {
            return sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId());
        } else {
            throw new PowsyblException("Unexpected terminal " + terminalId + " in the switch " + sw.getId());
        }
    }

    private static void createSwitchForSwitch(Switch sw, int node, String cgmesTerminalId, Context context) {
        int newNode = sw.getVoltageLevel().getNodeBreakerView().getMaximumNodeIndex() + 1;
        // better to create the new switch in the correct direction
        if (node == sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId())) {
            createSwitch(sw.getVoltageLevel(), cgmesTerminalId, node, newNode, context);
        } else {
            createSwitch(sw.getVoltageLevel(), cgmesTerminalId, newNode, node, context);
        }
        moveSwitchNode(sw, node, newNode);
    }

    private static void moveSwitchNode(Switch sw, int node, int newNode) {
        int node1 = sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
        int node2 = sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId());
        VoltageLevel.NodeBreakerView.SwitchAdder adder = sw.getVoltageLevel().getNodeBreakerView().newSwitch()
                .setId(sw.getId())
                .setName(sw.getNameOrId())
                .setNode1(node1 == node ? newNode : node1)
                .setNode2(node2 == node ? newNode : node2)
                .setKind(sw.getKind())
                .setRetained(sw.isRetained())
                .setOpen(sw.isOpen())
                .setFictitious(sw.isFictitious());

        // Copy properties and aliases
        Map<String, String> properties = new HashMap<>();
        sw.getPropertyNames().forEach(propertyName -> properties.put(propertyName, sw.getProperty(propertyName)));
        Map<String, String> aliases = new HashMap<>();
        sw.getAliases().forEach(alias -> aliases.put(alias, sw.getAliasType(alias).orElse("")));

        // remove the current switch
        sw.getVoltageLevel().getNodeBreakerView().removeSwitch(sw.getId());

        // create the new switch and set properties and aliases
        Switch newSw = adder.add();
        properties.forEach(newSw::setProperty);
        aliases.forEach(newSw::addAlias);
    }

    private static void createSwitchForTerminal(Terminal terminal, String cgmesTerminalId, Context context) {
        int node = terminal.getNodeBreakerView().getNode();
        List<Integer> nodesInternalConnectedTo = terminal.getVoltageLevel().getNodeBreakerView().getNodesInternalConnectedTo(node);
        int numberOfSwitchesConnectedTo = terminal.getVoltageLevel().getNodeBreakerView().getSwitches(node).size();
        if (nodesInternalConnectedTo.size() == 1 && numberOfSwitchesConnectedTo == 0) {
            // There is no need anymore of the internal connection with the addition of the fictitious switch.
            int otherNode = nodesInternalConnectedTo.getFirst();
            createSwitch(terminal.getVoltageLevel(), cgmesTerminalId, node, otherNode, context);
            terminal.getVoltageLevel().getNodeBreakerView().removeInternalConnections(node, otherNode);
        } else {
            int newNode = terminal.getVoltageLevel().getNodeBreakerView().getMaximumNodeIndex() + 1;
            createSwitch(terminal.getVoltageLevel(), cgmesTerminalId, node, newNode, context);
            terminal.getNodeBreakerView().moveConnectable(newNode, terminal.getVoltageLevel().getId());
        }
    }

    // The terminal associated with the fictitious switch is recorded as a property because the alias must be unique.
    // In some cases, it may have already been recorded as an alias associated with other equipment
    private static void createSwitch(VoltageLevel voltageLevel, String cgmesTerminalId, int node1, int node2, Context context) {
        String switchId = cgmesTerminalId + "_SW_fict";
        // The switch is closed for all variants but the current one.
        // 2 calls to setOpen() are needed.
        Switch sw = voltageLevel.getNodeBreakerView().newSwitch()
                .setFictitious(true)
                .setId(switchId)
                .setName(cgmesTerminalId)
                .setNode1(node1)
                .setNode2(node2)
                // This makes it closed for all variants.
                .setOpen(false)
                .setKind(SwitchKind.BREAKER)
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                .add();
        // This makes it open for the current variant.
        sw.setOpen(true);
        sw.setProperty(PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL, "true");
        sw.setProperty(PROPERTY_TERMINAL, cgmesTerminalId);
    }

    static Terminal getTerminal(Connectable<?> connectable, String terminalId) {
        String aliasType = connectable.getAliasType(terminalId).orElse("");
        return switch (aliasType) {
            case ALIAS_TERMINAL, ALIAS_TERMINAL1 -> connectable.getTerminals().get(0);
            case ALIAS_TERMINAL2 -> connectable.getTerminals().get(1);
            case ALIAS_TERMINAL3 -> connectable.getTerminals().get(2);
            case ALIAS_TERMINAL_BOUNDARY -> null;
            default -> throw new PowsyblException("Unexpected terminal " + terminalId + " in the connectable " + connectable.getId());
        };
    }

    public static void update(Switch sw, Context context) {
        boolean isOpen = getIsTerminalOpen(sw, context).orElse(getDefaultIsOpen(sw, context));
        sw.setOpen(isOpen);
    }

    private static Optional<Boolean> getIsTerminalOpen(Switch sw, Context context) {
        return getIsTerminalConnected(sw, context).map(connected -> !connected);
    }

    /**
     * Get the connected status of the CGMES terminal of a fictitious switch created for a disconnected terminal.
     *
     * @param sw the fictitious switch
     * @param context the update context
     * @return the connected status of the terminal (see {@link Context#isCgmesTerminalConnected(String)}), or an empty
     * optional if it is unknown
     */
    private static Optional<Boolean> getIsTerminalConnected(Switch sw, Context context) {
        String cgmesTerminalId = sw.getProperty(PROPERTY_TERMINAL);
        return cgmesTerminalId != null
                ? context.isCgmesTerminalConnected(cgmesTerminalId)
                : Optional.empty();
    }
}
