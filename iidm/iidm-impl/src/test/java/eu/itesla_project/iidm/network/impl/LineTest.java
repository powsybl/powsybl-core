/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

public class LineTest {

    @Test
    public void testSetterGetter() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        float r = 10.0f;
        float x = 20.0f;
        float g1 = 30.0f;
        float g2 = 35.0f;
        float b1 = 40.0f;
        float b2 = 45.0f;
        float delta = 0.0f;
        line.setR(r);
        assertEquals(r, line.getR(), delta);
        line.setX(x);
        assertEquals(x, line.getX(), delta);
        line.setG1(g1);
        assertEquals(g1, line.getG1(), delta);
        line.setG2(g2);
        assertEquals(g2, line.getG2(), delta);
        line.setB1(b1);
        assertEquals(b1, line.getB1(), delta);
        line.setB2(b2);
        assertEquals(b2, line.getB2(), delta);
        assertFalse(line.isTieLine());

        CurrentLimits currentLimits1 = line.newCurrentLimits1()
                                            .setPermanentLimit(100)
                                            .beginTemporaryLimit()
                                            .setName("5'")
                                            .setAcceptableDuration(5 * 60)
                                            .setValue(1400)
                                            .setFictitious(true)
                                            .endTemporaryLimit()
                                        .add();
        CurrentLimits currentLimits2 = line.newCurrentLimits2()
                                            .setPermanentLimit(50)
                                            .beginTemporaryLimit()
                                            .setName("20'")
                                            .setAcceptableDuration(20 * 60)
                                            .setValue(1200)
                                            .endTemporaryLimit()
                                        .add();
        assertSame(currentLimits1, line.getCurrentLimits1());
        assertSame(currentLimits2, line.getCurrentLimits2());
    }

    @Test
    public void testTieLineSetterGetter() {
        Network network = EurostagTutorialExample1Factory.create();
        float r = 10.0f;
        float r2 = 1.0f;
        float x = 20.0f;
        float x2 = 2.0f;
        float hl1g1 = 30.0f;
        float hl1g2 = 35.0f;
        float hl1b1 = 40.0f;
        float hl1b2 = 45.0f;
        float hl2g1 = 130.0f;
        float hl2g2 = 135.0f;
        float hl2b1 = 140.0f;
        float hl2b2 = 145.0f;
        float xnodeP = 50.0f;
        float xnodeQ = 60.0f;
        float delta = 0.0f;
        TieLine tieLine = network.newTieLine().setId("testTie")
                            .setName("testNameTie")
                            .setVoltageLevel1("VLHV1")
                            .setBus1("NHV1")
                            .setConnectableBus1("NHV1")
                            .setVoltageLevel2("VLHV2")
                            .setBus2("NHV2")
                            .setConnectableBus2("NHV2")
                            .setUcteXnodeCode("ucte")
                            .line1()
                                .setId("hl1")
                                .setName("half1_name")
                                .setR(r)
                                .setX(x)
                                .setB1(hl1b1)
                                .setB2(hl1b2)
                                .setG1(hl1g1)
                                .setG2(hl1g2)
                                .setXnodeQ(xnodeQ)
                                .setXnodeP(xnodeP)
                            .line2()
                                .setId("hl2")
                                .setR(r2)
                                .setX(x2)
                                .setB1(hl2b1)
                                .setB2(hl2b2)
                                .setG1(hl2g1)
                                .setG2(hl2g2)
                                .setXnodeP(xnodeP)
                                .setXnodeQ(xnodeQ)
                            .add();
        assertTrue(tieLine.isTieLine());
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        assertEquals("half1_name", tieLine.getHalf1().getName());
        assertEquals("hl2", tieLine.getHalf2().getId());
        assertEquals(r + r2, tieLine.getR(), delta);
        assertEquals(x + x2, tieLine.getX(), delta);
        assertEquals(hl1g1 + hl2g1, tieLine.getG1(), delta);
        assertEquals(hl1g2 + hl2g2, tieLine.getG2(), delta);
        assertEquals(hl1b1 + hl2b1, tieLine.getB1(), delta);
        assertEquals(hl1b2 + hl2b2, tieLine.getB2(), delta);

        try {
            tieLine.setR(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setX(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setB1(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setB2(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setG1(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        try {
            tieLine.setG2(1.0f);
            fail();
        } catch (ValidationException ignored) {
        }

        TieLineImpl.HalfLine half1 = tieLine.getHalf1();
        assertEquals(xnodeP, half1.getXnodeP(), delta);
        assertEquals(xnodeQ, half1.getXnodeQ(), delta);
    }

    @Test
    public void testDanglingLine() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        float r = 10.0f;
        float x = 20.0f;
        float g = 30.0f;
        float b = 40.0f;
        float p0 = 50.0f;
        float q0 = 60.0f;
        float delta = 0.0f;
        String id = "danglingId";
        String name = "danlingName";
        String ucteXnodeCode = "code";
        DanglingLine danglingLine = vl.newDanglingLine()
                .setBus("NHV1")
                .setConnectableBus("NHV1")
                .setId(id)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setP0(p0)
                .setQ0(q0)
                .setName(name)
                .setUcteXnodeCode(ucteXnodeCode)
                .add();
        assertEquals(r, danglingLine.getR(), delta);
        assertEquals(x, danglingLine.getX(), delta);
        assertEquals(g, danglingLine.getG(), delta);
        assertEquals(b, danglingLine.getB(), delta);
        assertEquals(p0, danglingLine.getP0(), delta);
        assertEquals(q0, danglingLine.getQ0(), delta);
        assertEquals(id, danglingLine.getId());
        assertEquals(name, danglingLine.getName());
        assertEquals(ucteXnodeCode, danglingLine.getUcteXnodeCode());

        float r2 = 11.0f;
        float x2 = 21.0f;
        float g2 = 31.0f;
        float b2 = 41.0f;
        float p02 = 51.0f;
        float q02 = 61.0f;
        danglingLine.setR(r2);
        assertEquals(r2, danglingLine.getR(), delta);
        danglingLine.setX(x2);
        assertEquals(x2, danglingLine.getX(), delta);
        danglingLine.setG(g2);
        assertEquals(g2, danglingLine.getG(), delta);
        danglingLine.setB(b2);
        assertEquals(b2, danglingLine.getB(), delta);
        danglingLine.setP0(p02);
        assertEquals(p02, danglingLine.getP0(), delta);
        danglingLine.setQ0(q02);
        assertEquals(q02, danglingLine.getQ0(), delta);
    }
}
