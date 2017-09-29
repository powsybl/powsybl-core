/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class TwoWindingsTransformerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private Substation substation;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        substation = network.getSubstation("sub");
    }

    @Test
    public void baseTests() {
        // adder
        TwoWindingsTransformer twoWindingsTransformer = substation.newTwoWindingsTransformer()
                                                                    .setId("twt")
                                                                    .setName("twt_name")
                                                                    .setR(1.0f)
                                                                    .setX(2.0f)
                                                                    .setG(3.0f)
                                                                    .setB(4.0f)
                                                                    .setRatedU1(5.0f)
                                                                    .setRatedU2(6.0f)
                                                                    .setVoltageLevel1("vl1")
                                                                    .setVoltageLevel2("vl2")
                                                                    .setConnectableBus1("busA")
                                                                    .setConnectableBus2("busB")
                                                                .add();
        assertEquals("twt", twoWindingsTransformer.getId());
        assertEquals("twt_name", twoWindingsTransformer.getName());
        assertEquals(1.0f, twoWindingsTransformer.getR(), 0.0f);
        assertEquals(2.0f, twoWindingsTransformer.getX(), 0.0f);
        assertEquals(3.0f, twoWindingsTransformer.getG(), 0.0f);
        assertEquals(4.0f, twoWindingsTransformer.getB(), 0.0f);
        assertEquals(5.0f, twoWindingsTransformer.getRatedU1(), 0.0f);
        assertEquals(6.0f, twoWindingsTransformer.getRatedU2(), 0.0f);
        assertEquals(ConnectableType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformer.getType());
        assertSame(substation, twoWindingsTransformer.getSubstation());

        // setter getter
        float r = 0.5f;
        twoWindingsTransformer.setR(r);
        assertEquals(r, twoWindingsTransformer.getR(), 0.0f);
        float b = 1.0f;
        twoWindingsTransformer.setB(b);
        assertEquals(b, twoWindingsTransformer.getB(), 0.0f);
        float g = 2.0f;
        twoWindingsTransformer.setG(g);
        assertEquals(g, twoWindingsTransformer.getG(), 0.0f);
        float x = 4.0f;
        twoWindingsTransformer.setX(x);
        assertEquals(x, twoWindingsTransformer.getX(), 0.0f);
        float ratedU1 = 8.0f;
        twoWindingsTransformer.setRatedU1(ratedU1);
        assertEquals(ratedU1, twoWindingsTransformer.getRatedU1(), 0.0f);
        float ratedU2 = 16.0f;
        twoWindingsTransformer.setRatedU2(ratedU2);
        assertEquals(ratedU2, twoWindingsTransformer.getRatedU2(), 0.0f);
    }

    @Test
    public void testInvalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createTwoWindingTransformer("invalid", "invalid", Float.NaN, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Test
    public void testInvalidX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0f, Float.NaN, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Test
    public void testInvalidG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0f, 1.0f, Float.NaN, 1.0f, 1.0f, 1.0f);
    }

    @Test
    public void testInvalidB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0f, 1.0f, 1.0f, Float.NaN, 1.0f, 1.0f);
    }

    @Test
    public void testInvalidRatedU1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated U1 is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0f, 1.0f, 1.0f, 1.0f, Float.NaN, 1.0f);
    }

    @Test
    public void testInvalidRatedU2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated U2 is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Float.NaN);
    }

    @Test
    public void transformerNotInSameSubstation() {
        Substation anotherSubstation = network.newSubstation()
                    .setId("subB")
                    .setName("n")
                    .setCountry(Country.FR)
                    .setTso("RTE")
                .add();
        VoltageLevel voltageLevelC = anotherSubstation.newVoltageLevel()
                    .setId("vl3")
                    .setName("vl3")
                    .setNominalV(200.0f)
                    .setHighVoltageLimit(400.0f)
                    .setLowVoltageLimit(200.0f)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevelC.getBusBreakerView().newBus()
                    .setId("busC")
                    .setName("busC")
                .add();
        thrown.expect(ValidationException.class);
        thrown.expectMessage("the 2 windings of the transformer shall belong to the substation");
        substation.newTwoWindingsTransformer().setId("invalidTwt")
                        .setName("twt_name")
                        .setR(1.0f)
                        .setX(2.0f)
                        .setG(3.0f)
                        .setB(4.0f)
                        .setRatedU1(5.0f)
                        .setRatedU2(6.0f)
                        .setVoltageLevel1("vl1")
                        .setVoltageLevel2("vl3")
                        .setConnectableBus1("busA")
                        .setConnectableBus2("busC")
                    .add();
    }

    private void createTwoWindingTransformer(String id, String name, float r, float x, float g, float b,
                                             float ratedU1, float ratedU2) {
        substation.newTwoWindingsTransformer()
                    .setId(id)
                    .setName(name)
                    .setR(r)
                    .setX(x)
                    .setG(g)
                    .setB(b)
                    .setRatedU1(ratedU1)
                    .setRatedU2(ratedU2)
                    .setVoltageLevel1("vl1")
                    .setVoltageLevel2("vl2")
                    .setConnectableBus1("busA")
                    .setConnectableBus2("busB")
                .add();
    }

}
