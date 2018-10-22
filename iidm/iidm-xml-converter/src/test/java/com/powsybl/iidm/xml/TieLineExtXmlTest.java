/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ext.TieLineExt;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class TieLineExtXmlTest extends AbstractConverterTest {

    private static final String TIELINE_REF = "/tieLineRef.xml";

    private final String id = "tieline";
    private final double r1 = 10.0;
    private final double r2 = 1.0;
    private final double x1 = 20.0;
    private final double x2 = 2.0;
    private final double hl1g1 = 30.0;
    private final double hl1g2 = 35.0;
    private final double hl1b1 = 40.0;
    private final double hl1b2 = 45.0;
    private final double hl2g1 = 130.0;
    private final double hl2g2 = 135.0;
    private final double hl2b1 = 140.0;
    private final double hl2b2 = 145.0;
    private final double xnodeP = 50.0;
    private final double xnodeQ = 60.0;

    @Test
    public void testRoundTripV11() throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        addTieLineExt(network);
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read, TIELINE_REF);
    }

    @Test
    public void testReadV10() throws IOException {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/refs_V1_0" + TIELINE_REF));
        Line line = network.getLine(id);
        assertTrue(line.isTieLine());
        TieLineExt extension = line.getExtension(TieLineExt.class);
        assertNotNull(extension);
        TieLineExt.HalfLine hl1 = extension.getHalf1();
        assertEquals("hl1", hl1.getId());
        assertEquals(r1, hl1.getR(), 0.0);
        assertEquals(x1, hl1.getX(), 0.0);
        assertEquals(hl1g1, hl1.getG1(), 0.0);
        assertEquals(hl1b1, hl1.getB1(), 0.0);
        assertEquals(hl1g2, hl1.getG2(), 0.0);
        assertEquals(hl1b2, hl1.getB2(), 0.0);
        assertEquals(xnodeP, hl1.getXnodeP(), 0.0);
        assertEquals(xnodeQ, hl1.getXnodeQ(), 0.0);
        TieLineExt.HalfLine hl2 = extension.getHalf2();
        assertEquals("hl2", hl2.getId());
        assertEquals(r2, hl2.getR(), 0.0);
        assertEquals(x2, hl2.getX(), 0.0);
        assertEquals(hl2g1, hl2.getG1(), 0.0);
        assertEquals(hl2b1, hl2.getB1(), 0.0);
        assertEquals(hl2g2, hl2.getG2(), 0.0);
        assertEquals(hl2b2, hl2.getB2(), 0.0);
        assertEquals(xnodeP, hl2.getXnodeP(), 0.0);
        assertEquals(xnodeQ, hl2.getXnodeQ(), 0.0);
    }

    private void addTieLineExt(Network network) {
        Line line = network.newLine().setId(id)
                .setR(r1 + r2)
                .setX(x1 + x2)
                .setG1(hl1g1 + hl2g1)
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
