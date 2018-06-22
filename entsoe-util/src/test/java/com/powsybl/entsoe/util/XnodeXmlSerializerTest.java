package com.powsybl.entsoe.util;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public class XnodeXmlSerializerTest extends AbstractConverterTest {

    private static Network createTestNetwork() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B")
                .add();
        vl.newDanglingLine()
                .setId("DL")
                .setBus("B")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setP0(0.0)
                .setQ0(0.0)
                .add();
        return network;
    }

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        // extends dangling line
        DanglingLine dl = network.getDanglingLine("DL");
        Xnode xnode = new Xnode(dl, "XXXXXX11");
        dl.addExtension(Xnode.class, xnode);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/xnodeRef.xml");

        DanglingLine dl2 = network2.getDanglingLine("DL");
        Xnode xnode2 = dl2.getExtension(Xnode.class);
        assertNotNull(xnode2);
        assertEquals(xnode.getCode(), xnode2.getCode());
    }
}
