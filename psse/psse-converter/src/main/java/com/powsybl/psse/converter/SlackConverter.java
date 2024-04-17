/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.network.util.TerminalFinder;
import com.powsybl.psse.model.pf.PsseBus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SlackConverter extends AbstractConverter {

    SlackConverter(List<PsseBus> psseBusList, ContainersMapping containerMapping, Network network, NodeBreakerImport nodeBreakerImport) {
        super(containerMapping, network);
        this.psseBusList = Objects.requireNonNull(psseBusList);
        this.nodeBreakerImport = Objects.requireNonNull(nodeBreakerImport);
    }

    void create() {
        for (PsseBus psseBus : psseBusList) {
            if (psseBus.getIde() == 3) {
                Bus bus;
                Optional<NodeBreakerImport.NodeBreakerControlNode> slackControlNode = nodeBreakerImport.getSlackControlNode(psseBus.getI());
                if (slackControlNode.isPresent()) {
                    Terminal terminal = obtainTerminalNode(getNetwork(), slackControlNode.get().getVoltageLevelId(), slackControlNode.get().getNode());
                    bus = terminal != null ? terminal.getBusBreakerView().getBus() : null;
                } else {
                    String busId = AbstractConverter.getBusId(psseBus.getI());
                    bus = getNetwork().getBusBreakerView().getBus(busId);
                }
                if (slackBusIsValidForIidm(bus)) {
                    SlackTerminal.attach(bus);
                }
            }
        }
    }

    private static boolean slackBusIsValidForIidm(Bus bus) {
        return bus != null && TerminalFinder.getDefault().find(bus.getConnectedTerminals()).isPresent();
    }

    private final List<PsseBus> psseBusList;
    private final NodeBreakerImport nodeBreakerImport;
}
