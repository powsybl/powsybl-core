/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import org.joda.time.DateTime;
import org.junit.Test;

import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.VoltageLevel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class TerminalRefTest extends AbstractXmlConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripAllVersionnedXmlTest("terminalRef.xiidm");
        roundTripAllVersionnedXmlTest("regulatingTerminal.xml");
    }

    @Test
    public void testTerminalRefBusbarSection() {
        // Check that a regulation that points to a busbar section is properly
        // exported when IIDM XML topology level is configured as bus-breaker or bus-branch

        Network network = createNetworkWithRegulatingTerminalToBusbarSection();

        Network networkBusBreaker = exportImport(network, TopologyLevel.BUS_BREAKER);
        checkGeneratorWasRegulatingBusbarSection(networkBusBreaker);

        Network networkBusBranch = exportImport(network, TopologyLevel.BUS_BRANCH);
        checkGeneratorWasRegulatingBusbarSection(networkBusBranch);
    }

    private Network exportImport(Network network, TopologyLevel topologyLevel) {
        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(topologyLevel);
        Path path = fileSystem.getPath("/terminalRefBusBarSection" + topologyLevel + ".xiidm");
        NetworkXml.write(network, options, path);
        Network network2 = NetworkXml.read(path);
        return network2;
    }

    private static void checkGeneratorWasRegulatingBusbarSection(Network network) {
        // Check the generator is regulating the only bus at high voltage
        Terminal thv = network.getTwoWindingsTransformer("tx1").getTerminal1();
        Terminal treg = network.getGenerator("g1").getRegulatingTerminal();
        assertEquals(thv, treg);
    }

    private static Network createNetworkWithRegulatingTerminalToBusbarSection() {
        Network network = Network.create("terminal-ref-busbarsection", "test")
            .setCaseDate(DateTime.parse("2020-02-03T12:33:26.208+01:00"));

        Substation s1 = network.newSubstation()
            .setId("s1")
            .setCountry(Country.ES)
            .add();
        VoltageLevel vlhv = s1.newVoltageLevel()
            .setId("vlhv")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vlg = s1.newVoltageLevel()
            .setId("vlg")
            .setNominalV(16)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vlhv.getNodeBreakerView().setNodeCount(2);
        BusbarSection busbarSection = vlhv.getNodeBreakerView().newBusbarSection()
            .setId("bbhv")
            .setNode(0)
            .add();
        vlhv.getNodeBreakerView().newSwitch()
            .setId("brhv")
            .setNode1(0)
            .setNode2(1)
            .setKind(SwitchKind.BREAKER)
            .add();
        vlg.getNodeBreakerView().setNodeCount(2);
        vlg.newGenerator()
            .setId("g1")
            .setNode(0)
            .setMinP(0)
            .setMaxP(100)
            .setTargetP(10)
            .setVoltageRegulatorOn(true)
            .setTargetV(400)
            .setRegulatingTerminal(busbarSection.getTerminal())
            .add();
        vlg.getNodeBreakerView().newSwitch()
            .setId("brg")
            .setNode1(0)
            .setNode2(1)
            .setKind(SwitchKind.BREAKER)
            .add();
        s1.newTwoWindingsTransformer()
            .setId("tx1")
            .setVoltageLevel1("vlhv")
            .setVoltageLevel2("vlg")
            .setNode1(1)
            .setNode2(1)
            .setR(10)
            .setX(1)
            .setG(0)
            .setB(0)
            .setRatedU1(400)
            .setRatedU2(16)
            .add();
        return network;
    }
}
