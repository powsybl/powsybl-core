/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class MergeTest {

    @Test
    public void mergeNodeBreakerTestNPE() throws IOException {
        Network n1 = createNetworkWithDanglingLine("1");
        Network n2 = createNetworkWithDanglingLine("2");

        logVoltageLevel("Network 1 first voltage level", n1.getVoltageLevels().iterator().next());
        n1.merge(n2);
        // If we try to get connected components directly on the merged network,
        // A Null Pointer Exception happens in AbstractConnectable.notifyUpdate:
        // There is a CalculatedBus that has a terminal that refers to the removed DanglingLine
        // DanglingLine object has VoltageLevel == null,
        // NPE comes from trying to getNetwork() using VoltageLevel to notify a change in connected components
        checkConnectedComponents(n1);
    }

    @Test
    public void mergeNodeBreakerTestPass1() {
        Network n1 = createNetworkWithDanglingLine("1");
        Network n2 = createNetworkWithDanglingLine("2");

        // The test passes if we do not log voltage level (exportTopology)
        n1.merge(n2);
        checkConnectedComponents(n1);
    }

    @Test
    public void mergeNodeBreakerTestPass2() throws IOException {
        Network n1 = createNetworkWithDanglingLine("1");
        Network n2 = createNetworkWithDanglingLine("2");

        logVoltageLevel("Network 1 first voltage level", n1.getVoltageLevels().iterator().next());
        // The test also passes if we "force" the connected component calculation before merge
        checkConnectedComponents(n1);
        n1.merge(n2);
        checkConnectedComponents(n1);
    }

    private static void logVoltageLevel(String title, VoltageLevel vl) throws IOException {
        LOG.info(title);
        try (StringWriter w = new StringWriter()) {
            vl.exportTopology(w);
            LOG.info(w.toString());
        }
    }

    private static void checkConnectedComponents(Network n) {
        n.getBusView().getBuses().forEach(b -> assertEquals(0, b.getConnectedComponent().getNum()));
    }

    private static Network createNetworkWithDanglingLine(String nid) {
        Network n = NetworkTest1Factory.create(nid);
        VoltageLevel vl = n.getVoltageLevel(id("voltageLevel1", nid));
        DanglingLine dl = vl.newDanglingLine()
                .setId(id("danglingLineb", nid))
                .setNode(6)
                .setR(1.0)
                .setX(0.1)
                .setG(0.0)
                .setB(0.001)
                .setP0(10)
                .setQ0(1)
                // Same UCTE XnodeCode for dangling lines
                .setUcteXnodeCode("X")
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId(id("voltageLevel1BreakerDLb", nid))
                .setRetained(false)
                .setOpen(false)
                .setNode1(n.getBusbarSection(id("voltageLevel1BusbarSection1", nid)).getTerminal().getNodeBreakerView().getNode())
                .setNode2(dl.getTerminal().getNodeBreakerView().getNode())
                .add();
        return n;
    }

    private static String id(String localId, String networkId) {
        return NetworkTest1Factory.id(localId, networkId);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MergeTest.class);
}
