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
                                                                    .setR(1.0)
                                                                    .setX(2.0)
                                                                    .setG(3.0)
                                                                    .setB(4.0)
                                                                    .setRatedU1(5.0)
                                                                    .setRatedU2(6.0)
                                                                    .setVoltageLevel1("vl1")
                                                                    .setVoltageLevel2("vl2")
                                                                    .setConnectableBus1("busA")
                                                                    .setConnectableBus2("busB")
                                                                .add();
        assertEquals("twt", twoWindingsTransformer.getId());
        assertEquals("twt_name", twoWindingsTransformer.getName());
        assertEquals(1.0, twoWindingsTransformer.getR(), 0.0);
        assertEquals(2.0, twoWindingsTransformer.getX(), 0.0);
        assertEquals(3.0, twoWindingsTransformer.getG(), 0.0);
        assertEquals(4.0, twoWindingsTransformer.getB(), 0.0);
        assertEquals(5.0, twoWindingsTransformer.getRatedU1(), 0.0);
        assertEquals(6.0, twoWindingsTransformer.getRatedU2(), 0.0);
        assertEquals(ConnectableType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformer.getType());
        assertSame(substation, twoWindingsTransformer.getSubstation());

        // setter getter
        double r = 0.5;
        twoWindingsTransformer.setR(r);
        assertEquals(r, twoWindingsTransformer.getR(), 0.0);
        double b = 1.0;
        twoWindingsTransformer.setB(b);
        assertEquals(b, twoWindingsTransformer.getB(), 0.0);
        double g = 2.0;
        twoWindingsTransformer.setG(g);
        assertEquals(g, twoWindingsTransformer.getG(), 0.0);
        double x = 4.0;
        twoWindingsTransformer.setX(x);
        assertEquals(x, twoWindingsTransformer.getX(), 0.0);
        double ratedU1 = 8.0;
        twoWindingsTransformer.setRatedU1(ratedU1);
        assertEquals(ratedU1, twoWindingsTransformer.getRatedU1(), 0.0);
        double ratedU2 = 16.0;
        twoWindingsTransformer.setRatedU2(ratedU2);
        assertEquals(ratedU2, twoWindingsTransformer.getRatedU2(), 0.0);
    }

    @Test
    public void testInvalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createTwoWindingTransformer("invalid", "invalid", Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0);
    }

    @Test
    public void testInvalidX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0, Double.NaN, 1.0, 1.0, 1.0, 1.0);
    }

    @Test
    public void testInvalidG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0, 1.0, Double.NaN, 1.0, 1.0, 1.0);
    }

    @Test
    public void testInvalidB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0, 1.0, 1.0, Double.NaN, 1.0, 1.0);
    }

    @Test
    public void testInvalidRatedU1() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated U1 is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0, 1.0, 1.0, 1.0, Double.NaN, 1.0);
    }

    @Test
    public void testInvalidRatedU2() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated U2 is invalid");
        createTwoWindingTransformer("invalid", "invalid", 1.0, 1.0, 1.0, 1.0, 1.0, Double.NaN);
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
                    .setNominalV(200.0)
                    .setHighVoltageLimit(400.0)
                    .setLowVoltageLimit(200.0)
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
                        .setR(1.0)
                        .setX(2.0)
                        .setG(3.0)
                        .setB(4.0)
                        .setRatedU1(5.0)
                        .setRatedU2(6.0)
                        .setVoltageLevel1("vl1")
                        .setVoltageLevel2("vl3")
                        .setConnectableBus1("busA")
                        .setConnectableBus2("busC")
                    .add();
    }

    private void createTwoWindingTransformer(String id, String name, double r, double x, double g, double b,
                                             double ratedU1, double ratedU2) {
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
