package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NetworkXmlNodeBreakerTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() {
        String filename = "/nodeBreaker.xml";

        Network network = Network.read("nodeBreaker.xiidm", getClass().getResourceAsStream(filename));
        assertNotNull(network);
        BusbarSection bbs1 = network.getBusbarSection("E1");
        BusbarSection bbs2 = network.getBusbarSection("E2");
        bbs1.getTerminal().getBusBreakerView().getBus().setV(400.0);
        bbs2.getTerminal().getBusBreakerView().getBus().setV(401.0);
        assertEquals(400.0, bbs1.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
        assertEquals(401.0, bbs2.getTerminal().getBusBreakerView().getBus().getV(), 0.0);

        Network network2 = NetworkSerDe.copy(network);
        bbs1 = network2.getBusbarSection("E1");
        bbs2 = network2.getBusbarSection("E2");
        assertEquals(400.0, bbs1.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
        assertEquals(401.0, bbs2.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
    }
}
