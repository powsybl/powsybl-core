/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.Assert.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBayTest extends AbstractXmlConverterTest  {

    @Test
    public void createLoadTestWithBbsId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", "bb4", 115);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-load-bbs1.xml");
    }

    @Test
    public void createLoadTestWithBbsId2() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        int loadPositionOrder = TopologyModificationUtils.getLastUnusedOrderPosition(network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", "bbs1", loadPositionOrder, TOP);
        modification.apply(network);
        assertEquals(TOP, network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getDirection());
        assertEquals(111, loadPositionOrder);
    }

    @Test
    public void createLoadTestWithBbsId3() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        int loadPositionOrder = TopologyModificationUtils.getFirstUnusedOrderPosition(network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", "bbs2", loadPositionOrder);
        modification.apply(network);
        assertEquals(Optional.of(39), network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getOrder());
        assertEquals(39, loadPositionOrder);
    }

    @Test
    public void createLoadTestWithVoltageLevelId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        BusbarSection bbs = TopologyModificationUtils.getFirstBusbarSection(network.getVoltageLevel("vl1"));
        assertEquals("bbs1", bbs.getId());
        NetworkModification modification = new CreateFeederBay(loadAdder, "vl1", bbs.getId(), 71);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-load-vl1.xml");
    }

    @Test
    public void testConstructor() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        CreateFeederBay modification = new CreateFeederBay(loadAdder, "vl1", "bb4", 115);
        assertEquals(loadAdder, modification.getInjectionAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getInjectionPositionOrder());
        assertEquals(BOTTOM, modification.getInjectionDirection());
    }

    @Test
    public void testBuilder() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        CreateFeederBay modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withBbsId("bb4")
                .withInjectionPositionOrder(115)
                .build();
        assertEquals(loadAdder, modification.getInjectionAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getInjectionPositionOrder());
        assertEquals(BOTTOM, modification.getInjectionDirection());

        CreateFeederBay modification1 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withInjectionPositionOrder(115)
                .withInjectionDirection(TOP)
                .build();
        assertEquals(loadAdder, modification1.getInjectionAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertNull(modification1.getBbsId());
        assertEquals(115, modification1.getInjectionPositionOrder());
        assertEquals(TOP, modification1.getInjectionDirection());
    }

    @Test
    public void testException() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        CreateFeederBay modification = new CreateFeederBay(loadAdder, "vl", "bbs4", 115);
        assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        CreateFeederBay modification1 = new CreateFeederBay(loadAdder, "vl1", "bbs5", 115);
        assertThrows(PowsyblException.class, () -> modification1.apply(network, true, Reporter.NO_OP));
        Network network1 = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        modification1.setBbsId("bbs1");
        assertThrows(PowsyblException.class, () -> modification1.apply(network1, true, Reporter.NO_OP));
        modification1.setBbsId("bbs");
        assertThrows(PowsyblException.class, () -> modification1.apply(network, true, Reporter.NO_OP));
        CreateFeederBay modification2 = new CreateFeederBay(loadAdder, "vl", "bbs4", 0);
        assertThrows(PowsyblException.class, () -> modification2.apply(network, true, Reporter.NO_OP));
    }

    @Test
    public void createGeneratorTestWithBbsId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        GeneratorAdder generatorAdder = network.getVoltageLevel("vl1").newGenerator()
                .setId("newGenerator")
                .setVoltageRegulatorOn(true)
                .setMaxP(9999)
                .setMinP(-9999)
                .setTargetV(25.5)
                .setTargetP(600)
                .setTargetQ(300)
                .setRatedS(10)
                .setEnergySource(EnergySource.NUCLEAR)
                .setEnsureIdUnicity(true);
        NetworkModification modification = new CreateFeederBay(generatorAdder, "vl1", "bbs1", 115);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-generator-bbs1.xml");
    }

    @Test
    public void testFeederOrders() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Set<Integer> feederOrders = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl1"));
        assertEquals(13, feederOrders.size());
        assertTrue(feederOrders.contains(100));
        Set<Integer> feederOrders2 = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl2"));
        assertEquals(9, feederOrders2.size());
        Set<Integer> feederOrders3 = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vlSubst2"));
        assertEquals(1, feederOrders3.size());
    }
}
