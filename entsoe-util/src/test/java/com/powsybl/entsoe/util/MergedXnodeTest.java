/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MergedXnodeTest {
    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
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
    public void test() {
        Network network = createTestNetwork();

        // extends line
        Line line = network.getLine("L");
        line.newExtension(MergedXnodeAdder.class).withRdp(0.5f).withXdp(0.5f).withXnodeP1(1.0).withXnodeQ1(2.0)
                .withXnodeP2(3.0).withXnodeQ2(4.0).withLine1Name("").withLine2Name("").withCode("XXXXXX11").add();
        MergedXnode xnode = line.getExtension(MergedXnode.class);

        assertEquals("mergedXnode", xnode.getName());
        assertSame(line, xnode.getExtendable());

        assertEquals(0.5f, xnode.getRdp(), 0f);
        assertEquals(0.5f, xnode.getXdp(), 0f);
        assertEquals(1.0, xnode.getXnodeP1(), 0.0);
        assertEquals(2.0, xnode.getXnodeQ1(), 0.0);
        assertEquals(3.0, xnode.getXnodeP2(), 0.0);
        assertEquals(4.0, xnode.getXnodeQ2(), 0.0);
        assertEquals("XXXXXX11", xnode.getCode());

        try {
            xnode.setCode(null);
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            xnode.setRdp(2f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setXdp(-3f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setLine1Name(null);
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            xnode.setLine2Name(null);
            fail();
        } catch (NullPointerException ignored) {
        }

        xnode.setRdp(0.6f);
        xnode.setXdp(0.6f);
        xnode.setXnodeP1(10.0);
        xnode.setXnodeQ1(11.0);
        xnode.setXnodeP2(12.0);
        xnode.setXnodeQ2(13.0);
        xnode.setCode("XXXXXX21");
        xnode.setLine1Name("Line1");
        xnode.setLine2Name("Line2");

        assertEquals(0.6f, xnode.getRdp(), 0f);
        assertEquals(0.6f, xnode.getXdp(), 0f);
        assertEquals(10.0, xnode.getXnodeP1(), 0.0);
        assertEquals(11.0, xnode.getXnodeQ1(), 0.0);
        assertEquals(12.0, xnode.getXnodeP2(), 0.0);
        assertEquals(13.0, xnode.getXnodeQ2(), 0.0);
        assertEquals("Line1", xnode.getLine1Name());
        assertEquals("Line2", xnode.getLine2Name());

    }

}
