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
public class MergedXnodeXmlSerializerTest extends AbstractConverterTest {

    private static Network createTestNetwork() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .add();
        return network;
    }

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        // extends line
        Line line = network.getLine("L");
        MergedXnode xnode = new MergedXnode(line, 0.5f, 0.5f, 1.0, 2.0, 3.0, 4.0, "XXXXXX11");
        line.addExtension(MergedXnode.class, xnode);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/mergedXnodeRef.xml");

        Line line2 = network2.getLine("L");
        MergedXnode xnode2 = line2.getExtension(MergedXnode.class);
        assertNotNull(xnode2);
        assertEquals(xnode.getRdp(), xnode2.getRdp(), 0f);
        assertEquals(xnode.getXdp(), xnode2.getXdp(), 0f);
        assertEquals(xnode.getXnodeP1(), xnode2.getXnodeP1(), 0.0);
        assertEquals(xnode.getXnodeQ1(), xnode2.getXnodeQ1(), 0.0);
        assertEquals(xnode.getXnodeP2(), xnode2.getXnodeP2(), 0.0);
        assertEquals(xnode.getXnodeQ2(), xnode2.getXnodeQ2(), 0.0);
        assertEquals(xnode.getCode(), xnode2.getCode());
    }
}
