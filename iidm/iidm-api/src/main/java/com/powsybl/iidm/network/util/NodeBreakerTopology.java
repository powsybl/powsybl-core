/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.math.graph.TraverseResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Provides higher level methods to manipulate node-breaker topology graph.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class NodeBreakerTopology {

    private NodeBreakerTopology() {
    }

    /**
     * Remove switches of components which have no connected equipment,
     * i.e for which all vertices objects are null.
     *
     * @param topo The node-breaker view of a voltage level.
     */
    public static void removeIsolatedSwitches(VoltageLevel.NodeBreakerView topo) {
        Objects.requireNonNull(topo, "Node breaker topology view is null.");

        final Set<Switch> encounteredSwitches = new HashSet<>();
        int[] nodesWithTerminal = Arrays.stream(topo.getNodes()).filter(n -> topo.getTerminal(n) != null).toArray();
        topo.traverse(nodesWithTerminal, (n1, sw, n2) -> {
            encounteredSwitches.add(sw); // the traversing started from a terminal, thus the switch is not isolated
            return TraverseResult.CONTINUE; // if n2 has a terminal, we could also choose to stop as it will be or has already been traversed
        });

        topo.getSwitchStream().filter(sw -> !encounteredSwitches.contains(sw)).forEach(s -> topo.removeSwitch(s.getId()));
    }

    /**
     * Creates a standard connection point from a bus bar section,
     * with a disconnector and a breaker.
     *
     * @param bb Bus bar section to connect to
     * @return   Node index of the connection point
     */
    public static int newStandardConnection(BusbarSection bb) {
        Objects.requireNonNull(bb, "Busbar section is null.");

        int n = bb.getTerminal().getNodeBreakerView().getNode();
        VoltageLevel.NodeBreakerView topo = bb.getTerminal().getVoltageLevel().getNodeBreakerView();

        int oldCount = topo.getMaximumNodeIndex() + 1;
        topo.newDisconnector()
                .setId(String.format("disconnector %s-%d", bb.getId(), oldCount))
                .setNode1(n)
                .setNode2(oldCount)
                .add();
        topo.newBreaker()
                .setId(String.format("breaker %s-%d", bb.getId(), oldCount + 1))
                .setNode1(oldCount)
                .setNode2(oldCount + 1)
                .add();

        return oldCount + 1;
    }
}
