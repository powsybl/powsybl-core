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

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_CGMES_ORIGINAL_CLASS;
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
        return (connectable != null) ? Optional.ofNullable(getTerminal(connectable, terminalId)) : Optional.empty();
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
        // P0 and Q0 are set to zero for all variants except the current one
        // Two calls to setP0 and setQ0 are required to achieve this
        Load load = terminal.getVoltageLevel().newLoad()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setName(id)
                .setName(id)
                // These calls set P0 and Q0 to zero for all variants
                .setP0(0.0)
                .setQ0(0.0)
                .setFictitious(true)
                .setLoadType(LoadType.FICTITIOUS)
                .setNode(newNode)
                .add();
        // These call set P0 and Q0 for the current variant
        load.setP0(p0).setQ0(q0);
        load.getTerminal().setP(p0).setQ(q0);
        load.setProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SV_INJECTION);
    }

    private static void createBusBreakerLoad(Bus bus, PropertyBag svInjection) {
        String id = svInjection.getId(CgmesNames.SV_INJECTION);
        double p0 = svInjection.asDouble("pInjection");
        double q0 = svInjection.asDouble("qInjection", 0.0);
        // P0 and Q0 are set to zero for all variants except the current one
        // Two calls to setP0 and setQ0 are required to achieve this
        Load load = bus.getVoltageLevel().newLoad()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setName(id)
                // These calls set P0 and Q0 to zero for all variants
                .setP0(0.0)
                .setQ0(0.0)
                .setFictitious(true)
                .setLoadType(LoadType.FICTITIOUS)
                .setConnectableBus(bus.getId())
                .setBus(bus.getId())
                .add();
        // These call set P0 and Q0 for the current variant
        load.setP0(p0).setQ0(q0);
        load.getTerminal().setP(p0).setQ(q0);
        load.setProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SV_INJECTION);
    }

    public static void resetSvInjectionLoadsForCurrentVariant(Network network) {
        network.getLoadStream()
                .filter(load -> CgmesNames.SV_INJECTION.equals(load.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS)))
                .forEach(load -> load.setP0(0.0).setQ0(0.0));
    }
}
