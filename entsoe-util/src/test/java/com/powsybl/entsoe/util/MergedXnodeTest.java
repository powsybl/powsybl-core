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
        Line line = network.newLine()
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

        // extends line
        line.newExtension(MergedXnodeAdder.class).withRdp(0.5f).withXdp(0.75f)
                .withXnodeP1(1.0).withXnodeQ1(2.0)
                .withXnodeP2(1.5).withXnodeQ2(2.5)
                .withLine1Name("")
                .withLine1Fictitious(true)
                .withB1dp(3f / 7)
                .withG1dp(5f / 11)
                .withLine2Name("")
                .withLine2Fictitious(true)
                .withB2dp((float) 3.5 / 8)
                .withG2dp((float) 5.5 / 12)
                .withCode("XXXXXX11")
                .add();

        return network;
    }

    @Test
    public void testCreate() {
        Network network = createTestNetwork();

        // extends line
        Line line = network.getLine("L");
        MergedXnode xnode = line.getExtension(MergedXnode.class);

        assertEquals("mergedXnode", xnode.getName());
        assertSame(line, xnode.getExtendable());

        assertEquals(0.5f, xnode.getRdp(), 0f);
        assertEquals(0.75f, xnode.getXdp(), 0f);
        assertEquals(1.0, xnode.getXnodeP1(), 0.0);
        assertEquals(2.0, xnode.getXnodeQ1(), 0.0);
        assertEquals(1.5, xnode.getXnodeP2(), 0.0);
        assertEquals(2.5, xnode.getXnodeQ2(), 0.0);
        assertTrue(xnode.isLine1Fictitious());
        assertTrue(xnode.isLine1Fictitious());
        assertEquals(3f / 7, xnode.getB1dp(), 0f);
        assertEquals(5f / 11, xnode.getG1dp(), 0f);
        assertEquals(3.5f / 8, xnode.getB2dp(), 0f);
        assertEquals(5.5f / 12, xnode.getG2dp(), 0f);
        assertEquals("XXXXXX11", xnode.getCode());
    }

    @Test
    public void testControls() {
        Network network = createTestNetwork();
        MergedXnode xnode = network.getLine("L").getExtension(MergedXnode.class);

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
        try {
            xnode.setB1dp(2f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setB1dp(-1f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setG1dp(2f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setG1dp(-1f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setB2dp(2f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setB2dp(-1f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setG2dp(2f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            xnode.setG2dp(-1f);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testSetters() {
        Network network = createTestNetwork();
        MergedXnode xnode = network.getLine("L").getExtension(MergedXnode.class);

        xnode.setRdp(0.6f);
        xnode.setXdp(0.6f);
        xnode.setXnodeP1(10.0);
        xnode.setXnodeQ1(11.0);
        xnode.setXnodeP2(20.0);
        xnode.setXnodeQ2(21.0);
        xnode.setCode("XXXXXX21");
        xnode.setLine1Name("Line1");
        xnode.setLine2Name("Line2");
        xnode.setLine1Fictitious(false);
        xnode.setLine2Fictitious(false);
        xnode.setB1dp(12f / 25);
        xnode.setG1dp(14f / 29);
        xnode.setB2dp(22f / 35);
        xnode.setG2dp(24f / 39);
        assertEquals(0.6f, xnode.getRdp(), 0f);
        assertEquals(0.6f, xnode.getXdp(), 0f);
        assertEquals(10.0, xnode.getXnodeP1(), 0.0);
        assertEquals(11.0, xnode.getXnodeQ1(), 0.0);
        assertEquals(20.0, xnode.getXnodeP2(), 0.0);
        assertEquals(21.0, xnode.getXnodeQ2(), 0.0);
        assertEquals("Line1", xnode.getLine1Name());
        assertEquals("Line2", xnode.getLine2Name());
        assertFalse(xnode.isLine1Fictitious());
        assertFalse(xnode.isLine2Fictitious());
        assertEquals(12f / 25, xnode.getB1dp(), 0f);
        assertEquals(14f / 29, xnode.getG1dp(), 0f);
        assertEquals(22f / 35, xnode.getB2dp(), 0f);
        assertEquals(24f / 39, xnode.getG2dp(), 0f);
    }

    @Test
    public void testDefaultValues() {
        Network network = createTestNetwork();
        Line line = network.getLine("L");

        line.newExtension(MergedXnodeAdder.class)
                .withXnodeP1(1.0).withXnodeQ1(2.0)
                .withXnodeP2(1.5).withXnodeQ2(2.5)
                .withLine1Name("")
                .withLine2Name("")
                .withCode("XXXXXX11")
                .add();

        MergedXnode xnode = line.getExtension(MergedXnode.class);
        assertEquals(0.5f, xnode.getRdp(), 0f);
        assertEquals(0.5f, xnode.getXdp(), 0f);
        assertEquals(0.5f, xnode.getB1dp(), 0f);
        assertEquals(0.5f, xnode.getG1dp(), 0f);
        assertEquals(0.5f, xnode.getB2dp(), 0f);
        assertEquals(0.5f, xnode.getB2dp(), 0f);
        assertFalse(xnode.isLine1Fictitious());
        assertFalse(xnode.isLine2Fictitious());

        xnode.setRdp(Float.NaN);
        xnode.setXdp(Float.NaN);
        xnode.setB1dp(Float.NaN);
        xnode.setG1dp(Float.NaN);
        xnode.setB2dp(Float.NaN);
        xnode.setG2dp(Float.NaN);
        assertEquals(0.5f, xnode.getRdp(), 0f);
        assertEquals(0.5f, xnode.getXdp(), 0f);
        assertEquals(0.5f, xnode.getB1dp(), 0f);
        assertEquals(0.5f, xnode.getG1dp(), 0f);
        assertEquals(0.5f, xnode.getB2dp(), 0f);
        assertEquals(0.5f, xnode.getB2dp(), 0f);
    }

    @Test
    public void testDefaultImplAdder() {
        MergedXnodeAdder mergedXnodeAdder = new MergedXnodeAdder() {
            @Override
            public Line add() {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withLine1Name(String line1Name) {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withXnodeP1(double xnodeP1) {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withXnodeQ1(double xnodeQ1) {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withLine2Name(String line2Name) {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withXnodeP2(double xnodeP2) {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withXnodeQ2(double xnodeQ2) {
                return null;
            }

            @Override
            public MergedXnodeAdderImpl withCode(String code) {
                return null;
            }
        };

        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withRdp(Float.NaN));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withXdp(Float.NaN));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withLine1Fictitious(false));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withB1dp(Float.NaN));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withG1dp(Float.NaN));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withLine2Fictitious(false));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withB2dp(Float.NaN));
        assertEquals(mergedXnodeAdder, mergedXnodeAdder.withG2dp(Float.NaN));
    }

    @Test
    public void testDefaultImpl() {
        MergedXnode mergedXnode = new MergedXnode() {
            @Override
            public Line getExtendable() {
                return null;
            }

            @Override
            public void setExtendable(Line extendable) {
            }

            @Override
            public float getRdp() {
                return Float.NaN;
            }

            @Override
            public MergedXnode setRdp(float rdp) {
                return this;
            }

            @Override
            public float getXdp() {
                return Float.NaN;
            }

            @Override
            public MergedXnode setXdp(float xdp) {
                return this;
            }

            @Override
            public String getLine1Name() {
                return "";
            }

            @Override
            public MergedXnode setLine1Name(String line1Name) {
                return this;
            }

            @Override
            public double getXnodeP1() {
                return Double.NaN;
            }

            @Override
            public MergedXnode setXnodeP1(double xnodeP1) {
                return this;
            }

            @Override
            public double getXnodeQ1() {
                return Double.NaN;
            }

            @Override
            public MergedXnode setXnodeQ1(double xnodeQ1) {
                return this;
            }

            @Override
            public String getLine2Name() {
                return "";
            }

            @Override
            public MergedXnode setLine2Name(String line2Name) {
                return this;
            }

            @Override
            public double getXnodeP2() {
                return Double.NaN;
            }

            @Override
            public MergedXnode setXnodeP2(double xnodeP2) {
                return this;
            }

            @Override
            public double getXnodeQ2() {
                return Double.NaN;
            }

            @Override
            public MergedXnode setXnodeQ2(double xnodeQ2) {
                return this;
            }

            @Override
            public String getCode() {
                return "";
            }

            @Override
            public MergedXnode setCode(String code) {
                return this;
            }
        };

        assertEquals(mergedXnode, mergedXnode.setLine1Fictitious(false));
        assertEquals(false, mergedXnode.isLine1Fictitious());
        assertEquals(mergedXnode, mergedXnode.setB1dp(Float.NaN));
        assertEquals(0.5f, mergedXnode.getB1dp(), 0f);
        assertEquals(mergedXnode, mergedXnode.setG1dp(Float.NaN));
        assertEquals(0.5f, mergedXnode.getG1dp(), 0f);

        assertEquals(mergedXnode, mergedXnode.setLine2Fictitious(false));
        assertEquals(false, mergedXnode.isLine2Fictitious());
        assertEquals(mergedXnode, mergedXnode.setB2dp(Float.NaN));
        assertEquals(0.5f, mergedXnode.getB2dp(), 0f);
        assertEquals(mergedXnode, mergedXnode.setG2dp(Float.NaN));
        assertEquals(0.5f, mergedXnode.getG2dp(), 0f);
    }
}
