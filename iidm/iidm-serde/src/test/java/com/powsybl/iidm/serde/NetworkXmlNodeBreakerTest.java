package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NetworkXmlNodeBreakerTest extends AbstractConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        String filename = "/nodeBreaker.xml";

        Network network = Importers.loadNetwork(filename, getClass().getResourceAsStream(filename));
        assertNotNull(network);
        BusbarSection bbs1 = network.getBusbarSection("E1");
        BusbarSection bbs2 = network.getBusbarSection("E2");
        bbs1.getTerminal().getBusBreakerView().getBus().setV(400.0);
        bbs2.getTerminal().getBusBreakerView().getBus().setV(401.0);

        Network network2 = NetworkXml.copy(network);
        bbs1 = network2.getBusbarSection("E1");
        bbs2 = network2.getBusbarSection("E2");
        assertEquals(400.0, bbs1.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
        assertEquals(401.0, bbs2.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
    }
}
