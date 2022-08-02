/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.TOP;
import static org.junit.Assert.assertEquals;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateBayTest extends AbstractXmlConverterTest  {

    @Test
    public void attachLoadTestWithBbsId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateBay(loadAdder, "vl1", "bb4", 115);
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
        NetworkModification modification = new CreateBay(loadAdder, "vl1", "bbs1", 115, TOP);
        modification.apply(network);
        assertEquals(TOP, network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getDirection());
    }

    @Test
    public void attachLoadTestWithBbsId3() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        int loadPositionOrder = TopologyModificationUtils.getOrderPosition(TopologyModificationUtils.PositionInsideSection.FIRST, network.getVoltageLevel("vl1"), network.getBusbarSection("bbs2"));
        NetworkModification modification = new CreateBay(loadAdder, "vl1", "bbs2", loadPositionOrder);
        modification.apply(network);
        assertEquals(Optional.of(39), network.getLoad("newLoad").getExtension(ConnectablePosition.class).getFeeder().getOrder());
    }

    @Test
    public void attachLoadTestWithVoltageLevelId() throws IOException {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        LoadAdder loadAdder = network.getVoltageLevel("vl1").newLoad()
                .setId("newLoad")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateBay(loadAdder, "vl1", 71);
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
        CreateBay modification = new CreateBay(loadAdder, "vl1", "bb4", 115);
        assertEquals(loadAdder, modification.getLoadAdder());
        assertEquals("vl1", modification.getVoltageLevelId());
        assertEquals("bb4", modification.getBbsId());
        assertEquals(115, modification.getLoadPositionOrder());
        assertEquals(BOTTOM, modification.getLoadDirection());

        CreateBay modification2 = new CreateBay(loadAdder, "vl1", 115, TOP);
        assertEquals(loadAdder, modification2.getLoadAdder());
        assertEquals("vl1", modification2.getVoltageLevelId());
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
        CreateBay modification = new CreateBayBuilder()
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
    }
}
