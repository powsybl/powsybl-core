/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class AttachLoadTest extends AbstractXmlConverterTest  {

    @Test
    public void attachLoadTestWithBbsId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new AttachLoad(loadAdder, "vl1", "bb4", 115);
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network-node-breaker-with-new-load-bbs1.xml");
    }

    @Test
    public void attachLoadTestWithBbsId2() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new AttachLoad(loadAdder, "vl1", "bbs1", 115, TOP);
        modification.apply(network);
        assertEquals(TOP, network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getDirection());
    }

    @Test
    public void attachLoadTestWithVoltageLevelId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new AttachLoad(loadAdder, "vl1");
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
        AttachLoad modification = new AttachLoad(loadAdder, "vl1", "bb4", 115);
        assertEquals(loadAdder, modification.getLoadAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getLoadPositionOrder());
        assertEquals(BOTTOM, modification.getLoadDirection());

        AttachLoad modification2 = new AttachLoad(loadAdder, "vl1", AttachLoad.PositionInsideSection.LAST, TOP);
        assertEquals(loadAdder, modification2.getLoadAdder());
        assertEquals("vl1", modification2.getVoltageLevelId());
        assertEquals(AttachLoad.PositionInsideSection.LAST, modification2.getLoadPositionInsideSection());
        assertEquals(TOP, modification2.getLoadDirection());
    }

    @Test
    public void testBuilder() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        AttachLoad modification = new AttachLoadBuilder()
                .withLoadAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withBbsId("bb4")
                .withLoadPositionOrder(115)
                .build();
        assertEquals(loadAdder, modification.getLoadAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getLoadPositionOrder());
        assertEquals(BOTTOM, modification.getLoadDirection());

        AttachLoadBuilder builder = new AttachLoadBuilder()
                .withLoadAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withLoadPositionOrder(115);

        assertThrows(PowsyblException.class, builder::build);

        AttachLoad modification1 = new AttachLoadBuilder()
                .withLoadAdder(loadAdder)
                .withVoltageLevelId("vl1")
                .withLoadPositionInsideSection(AttachLoad.PositionInsideSection.LAST)
                .build();
        assertEquals(AttachLoad.PositionInsideSection.LAST, modification1.getLoadPositionInsideSection());
    }

    @Test
    public void testFeederOrders() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Map<String, Integer> feederOrders = TopologyModificationUtils.getFeederOrders(network.getVoltageLevel("vl1"));
        assertEquals(90, feederOrders.get("trf8_terminal1"), 0);
        assertEquals(80, feederOrders.get("load2"), 0);
        assertEquals(0, feederOrders.get("load1"), 0);
        assertEquals(20, feederOrders.get("gen1"), 0);
        assertEquals(70, feederOrders.get("line1_terminal1"), 0);
        Map<String, Integer> feederOrders2 = TopologyModificationUtils.getFeederOrders(network.getVoltageLevel("vl2"));
        assertEquals(60, feederOrders2.get("trf8_terminal2"), 0);
        Map<String, Integer> feederOrders3 = TopologyModificationUtils.getFeederOrders(network.getVoltageLevel("vlSubst2"));
        assertEquals(10, feederOrders3.get("line1_terminal2"), 0);
        Map<String, Integer> feederOrders4 = TopologyModificationUtils.getFeederOrders(network.getVoltageLevel("vl3"));
        assertEquals(40, feederOrders4.get("trf8_terminal3"), 0);
    }
}
