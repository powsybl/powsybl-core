/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

import static com.powsybl.cgmes.conversion.elements.TerminalConversion.getTerminal;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class SvInjectionConversion {

    private SvInjectionConversion() {
    }

    public static void create(Network network, PropertyBag svInjection) {
        findTerminal(network, svInjection).ifPresentOrElse(
                terminal -> {
                    if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                        createNodeBreakerLoad(terminal, svInjection);
                    } else {
                        createBusBreakerLoad(terminal.getBusBreakerView().getConnectableBus(), svInjection);
                    }
                },
                () -> findBus(network, svInjection).ifPresent(bus -> createBusBreakerLoad(bus, svInjection))
        );
    }

    private static Optional<Terminal> findTerminal(Network network, PropertyBag svInjection) {
        String terminalId = svInjection.getId(CgmesNames.TERMINAL);
        Connectable<?> connectable = (terminalId != null) ? network.getConnectable(terminalId) : null;
        return (connectable != null) ? Optional.of(getTerminal(connectable, terminalId)) : Optional.empty();
    }

    private static Optional<Bus> findBus(Network network, PropertyBag svInjection) {
        String topologicalNodeId = svInjection.getId(CgmesNames.TOPOLOGICAL_NODE);
        return topologicalNodeId != null ? Optional.ofNullable(network.getBusBreakerView().getBus(topologicalNodeId)) : Optional.empty();
    }

    private static void createNodeBreakerLoad(Terminal terminal, PropertyBag svInjection) {
        String id = svInjection.getId(CgmesNames.SV_INJECTION);
        double p0 = svInjection.asDouble("pInjection");
        double q0 = svInjection.asDouble("qInjection", 0.0);
        int node = terminal.getNodeBreakerView().getNode();
        int newNode = terminal.getVoltageLevel().getNodeBreakerView().getMaximumNodeIndex() + 1;

        terminal.getVoltageLevel().getNodeBreakerView().newInternalConnection().setNode1(node).setNode2(newNode).add();
        Load load = terminal.getVoltageLevel().newLoad()
                .setId(id)
                .setName(id)
                .setP0(p0)
                .setQ0(q0)
                .setFictitious(true)
                .setLoadType(LoadType.FICTITIOUS)
                .setNode(newNode)
                .add();
        load.getTerminal().setP(p0).setQ(q0);
    }

    private static void createBusBreakerLoad(Bus bus, PropertyBag svInjection) {
        String id = svInjection.getId(CgmesNames.SV_INJECTION);
        double p0 = svInjection.asDouble("pInjection");
        double q0 = svInjection.asDouble("qInjection", 0.0);
        Load load = bus.getVoltageLevel().newLoad()
                .setId(id)
                .setName(id)
                .setP0(p0)
                .setQ0(q0)
                .setFictitious(true)
                .setLoadType(LoadType.FICTITIOUS)
                .setConnectableBus(bus.getId())
                .setBus(bus.getId())
                .add();
        load.getTerminal().setP(p0).setQ(q0);
    }
}
