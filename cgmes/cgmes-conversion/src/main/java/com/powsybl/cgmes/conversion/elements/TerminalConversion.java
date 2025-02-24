/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.powsybl.cgmes.conversion.elements.AbstractConductingEquipmentConversion.getDefaultIsOpen;
import static com.powsybl.cgmes.conversion.elements.AbstractIdentifiedObjectConversion.defaultValue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class TerminalConversion {

    private TerminalConversion() {
    }

    public static void create(Network network, PropertyBag cgmesTerminal, Context context) {
        String cgmesTerminalId = cgmesTerminal.getId(CgmesNames.TERMINAL);
        boolean connected = cgmesTerminal.asBoolean(CgmesNames.CONNECTED, true);
        if (createFictitiousSwitch(cgmesTerminalId, connected)) {
            create(network, cgmesTerminalId, context);
        }
    }

    private static boolean createFictitiousSwitch(String cgmesTerminalId, boolean connected) {
        return cgmesTerminalId != null && !connected;
    }

    private static void create(Network network, String cgmesTerminalId, Context context) {
        Identifiable<?> identifiable = network.getIdentifiable(cgmesTerminalId);
        if (createFictitiousSwitch(identifiable, context)) {
            if (identifiable instanceof Switch sw) {
                if (createFictitiousSwitch(sw)) {
                    createSwitchForSwitch(sw, getNode(sw, cgmesTerminalId), cgmesTerminalId, context);
                }
            } else if (identifiable instanceof Connectable<?> connectable) {
                Terminal terminal = getTerminal(connectable, cgmesTerminalId);
                if (createFictitiousSwitch(terminal)) {
                    createSwitchForTerminal(terminal, cgmesTerminalId, context);
                }
            }
        }
    }

    private static boolean createFictitiousSwitch(Identifiable<?> identifiable, Context context) {
        return switch (context.config().getCreateFictitiousSwitchesForDisconnectedTerminalsMode()) {
            case NEVER -> false;
            case ALWAYS -> identifiable != null;
            case ALWAYS_EXCEPT_SWITCHES -> identifiable != null && identifiable.getType() != IdentifiableType.SWITCH;
        };
    }

    private static boolean createFictitiousSwitch(Switch sw) {
        return sw.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private static boolean createFictitiousSwitch(Terminal terminal) {
        return terminal != null && terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    private static int getNode(Switch sw, String terminalId) {
        String aliasType = sw.getAliasType(terminalId).orElse("");
        if (aliasType.equals(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1)) {
            return sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
        } else if (aliasType.equals(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2)) {
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
        int newNode = terminal.getVoltageLevel().getNodeBreakerView().getMaximumNodeIndex() + 1;
        createSwitch(terminal.getVoltageLevel(), cgmesTerminalId, node, newNode, context);
        terminal.getNodeBreakerView().moveConnectable(newNode, terminal.getVoltageLevel().getId());
    }

    private static void createSwitch(VoltageLevel voltageLevel, String cgmesTerminalId, int node1, int node2, Context context) {
        String switchId = cgmesTerminalId + "_SW_fict";
        Switch sw = voltageLevel.getNodeBreakerView().newSwitch()
                .setFictitious(true)
                .setId(switchId)
                .setName(cgmesTerminalId)
                .setNode1(node1)
                .setNode2(node2)
                .setOpen(true)
                .setKind(SwitchKind.BREAKER)
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                .add();
        sw.setProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL, "true");
        sw.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL, cgmesTerminalId);
    }

    static Terminal getTerminal(Connectable<?> connectable, String terminalId) {
        String aliasType = connectable.getAliasType(terminalId).orElse("");
        return switch (aliasType) {
            case Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 1 ->
                    connectable.getTerminals().get(0);
            case Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 2 -> connectable.getTerminals().get(1);
            case Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + 3 -> connectable.getTerminals().get(2);
            case Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_BOUNDARY -> null;
            default -> throw new PowsyblException("Unexpected terminal " + terminalId + " in the connectable " + connectable.getId());
        };
    }

    public static void update(Switch sw, Context context) {
        boolean isOpen = getIsTerminalOpen(sw, context).orElse(defaultValue(getDefaultIsOpen(sw), context));
        sw.setOpen(isOpen);
    }

    private static Optional<Boolean> getIsTerminalOpen(Switch sw, Context context) {
        return getIsTerminalConnected(sw, context).map(connected -> !connected);
    }

    private static Optional<Boolean> getIsTerminalConnected(Switch sw, Context context) {
        String cgmesTerminalId = sw.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL);
        return cgmesTerminalId != null
                ? Optional.ofNullable(context.cgmesTerminal(cgmesTerminalId)).flatMap(cgmesTerminal -> cgmesTerminal.asBoolean(CgmesNames.CONNECTED))
                : Optional.empty();
    }
}
