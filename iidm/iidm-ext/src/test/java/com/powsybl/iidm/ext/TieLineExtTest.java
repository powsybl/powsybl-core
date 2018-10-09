/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.ext;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class TieLineExtTest extends AbstractConverterTest {

    @Test
    public void test() {
        Line line = Mockito.mock(Line.class);
        TieLineExt.HalfLineImpl hl1 = new TieLineExt.HalfLineImpl();
        hl1.setId("hl1")
                .setB1(1.0)
                .setB2(2.0)
                .setG1(3.0)
                .setG2(4.0)
                .setR(5.0)
                .setX(6.0)
                .setXnodeP(7.0)
                .setXnodeQ(8.0);
        TieLineExt.HalfLineImpl hl2 = new TieLineExt.HalfLineImpl();
        hl2.setId("hl2")
                .setB1(11.0)
                .setB2(12.0)
                .setG1(13.0)
                .setG2(14.0)
                .setR(15.0)
                .setX(16.0)
                .setXnodeP(17.0)
                .setXnodeQ(18.0);

        TieLineExt tieLine = new TieLineExt(line, "ucte", hl1, hl2);
        assertEquals(20.0, tieLine.getR(), 0.0);
        assertEquals(22.0, tieLine.getX(), 0.0);
        assertEquals(16.0, tieLine.getG1(), 0.0);
        assertEquals(12.0, tieLine.getB1(), 0.0);
        assertEquals(18.0, tieLine.getG2(), 0.0);
        assertEquals(14.0, tieLine.getB2(), 0.0);
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        Assert.assertSame(line, tieLine.getExtendable());
    }

    @Test
    public void testRoundTripV11() throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        addTieLineExt(network);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/tieLineRef.xml");
    }

    private static void addTieLineExt(Network network) {
        double r1 = 10.0;
        double r2 = 1.0;
        double x1 = 20.0;
        double x2 = 2.0;
        double hl1g1 = 30.0;
        double hl1g2 = 35.0;
        double hl1b1 = 40.0;
        double hl1b2 = 45.0;
        double hl2g1 = 130.0;
        double hl2g2 = 135.0;
        double hl2b1 = 140.0;
        double hl2b2 = 145.0;
        double xnodeP = 50.0;
        double xnodeQ = 60.0;
        Line line = network.newLine().setId("tieline")
                .setR(r1 + r2)
                .setX(x1 + x2)
                .setG1(hl1g1 + hl2g2)
                .setG2(hl1g2 + hl2g2)
                .setB1(hl1b1 + hl2b1)
                .setB2(hl1b2 + hl2b2)
                .setConnectableBus1("busA")
                .setConnectableBus2("busB")
                .setVoltageLevel1("vl1")
                .setVoltageLevel2("vl2")
                .add();
        TieLineExt.HalfLineImpl hl1 = new TieLineExt.HalfLineImpl();
        TieLineExt.HalfLineImpl hl2 = new TieLineExt.HalfLineImpl();
        hl1.setId("hl1");
        hl1.setR(r1);
        hl1.setX(x1);
        hl1.setG1(hl1g1);
        hl1.setB1(hl1b1);
        hl1.setG2(hl1g2);
        hl1.setB2(hl1b2);
        hl1.setXnodeP(xnodeP);
        hl1.setXnodeQ(xnodeQ);
        hl2.setId("hl2");
        hl2.setR(r2);
        hl2.setX(x2);
        hl2.setG1(hl2g1);
        hl2.setB1(hl2b1);
        hl2.setG2(hl2g2);
        hl2.setB2(hl2b2);
        hl2.setXnodeP(xnodeP);
        hl2.setXnodeQ(xnodeQ);
        TieLineExt tieLineExt = new TieLineExt(line, "ucte", hl1, hl2);
        line.addExtension(TieLineExt.class, tieLineExt);
    }

}
