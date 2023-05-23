/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.SV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractTieLineTest {

    private static final String DANGLING1_NAME = "dl1_name";

    private static final String INVALID = "invalid";

    private static final String TO_REMOVE = "toRemove";

    private static final String DUPLICATE = "duplicate";

    private Network network;
    private VoltageLevel voltageLevelA;
    private VoltageLevel voltageLevelB;

    @BeforeEach
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        voltageLevelA = network.getVoltageLevel("vl1");
        voltageLevelB = network.getVoltageLevel("vl2");
    }

    @Test
    public void testTieLineAdder() {
        double tol = 0.0000001;

        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 0.03;
        double hl1g2 = 0.035;
        double hl1b1 = 0.04;
        double hl1b2 = 0.045;
        double hl2g1 = 0.013;
        double hl2g2 = 0.0135;
        double hl2b1 = 0.014;
        double hl2b2 = 0.0145;

        // adder
        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus("busA")
                .setId("hl1")
                .setEnsureIdUnicity(true)
                .setName(DANGLING1_NAME)
                .setR(r)
                .setX(x)
                .setB(hl1b1 + hl1b2)
                .setG(hl1g1 + hl1g2)
                .setUcteXnodeCode("ucte")
                .add();
        DanglingLine dl2 = voltageLevelB.newDanglingLine()
                .setBus("busB")
                .setId("hl2")
                .setEnsureIdUnicity(true)
                .setR(r2)
                .setX(x2)
                .setB(hl2b1 + hl2b2)
                .setG(hl2g1 + hl2g2)
                .add();

        assertEquals(List.of(dl1, dl2), network.getDanglingLines(DanglingLineFilter.UNPAIRED));
        assertFalse(network.getDanglingLines(DanglingLineFilter.PAIRED).iterator().hasNext());
        assertEquals(List.of(dl1, dl2), network.getDanglingLines());

        TieLineAdder adder = network.newTieLine().setId("testTie")
                .setName("testNameTie")
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId());
        TieLine tieLine = adder.add();

        assertEquals(List.of(dl1, dl2), network.getDanglingLines(DanglingLineFilter.PAIRED));
        assertFalse(network.getDanglingLines(DanglingLineFilter.UNPAIRED).iterator().hasNext());

        assertEquals(IdentifiableType.TIE_LINE, tieLine.getType());
        assertEquals("ucte", tieLine.getUcteXnodeCode());
        assertEquals("hl1", tieLine.getDanglingLine1().getId());
        assertEquals(DANGLING1_NAME, tieLine.getDanglingLine1().getOptionalName().orElse(null));
        assertEquals("hl2", tieLine.getDanglingLine2().getId());
        assertEquals("hl1", tieLine.getDanglingLine(voltageLevelA.getId()).getId());
        assertEquals("hl2", tieLine.getDanglingLine(voltageLevelB.getId()).getId());
        assertNull(tieLine.getDanglingLine("UnknownVoltageLevelId"));
        assertEquals(11.0, tieLine.getR(), tol);
        assertEquals(22.0, tieLine.getX(), tol);
        assertEquals(0.065, tieLine.getG1(), tol);
        assertEquals(0.02649999999, tieLine.getG2(), tol);
        assertEquals(0.08499999999, tieLine.getB1(), tol);
        assertEquals(0.0285, tieLine.getB2(), tol);

        DanglingLine danglingLine1 = tieLine.getDanglingLine1();

        DanglingLine danglingLine2 = tieLine.getDanglingLine2();

        // Check notification on DanglingLine changes
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);
        // Apply changes on dangling lines
        danglingLine1.setR(r + 1);
        danglingLine1.setX(x + 1);
        danglingLine1.setG(hl1g1 + hl1g2 + 2);
        danglingLine1.setB(hl1b1 + hl1b2 + 2);
        danglingLine2.setR(r + 1);
        danglingLine2.setX(x + 1);
        danglingLine2.setG(hl2g1 + hl1g2 + 2);
        danglingLine2.setB(hl2b1 + hl2b2 + 2);
        verify(mockedListener, times(8)).onUpdate(any(DanglingLine.class), anyString(), any(), any());
        // Remove observer
        network.removeListener(mockedListener);
        // Cancel changes on dangling lines
        danglingLine1.setR(r);
        danglingLine1.setX(x);
        danglingLine1.setG(hl1g1 + hl1g2);
        danglingLine1.setB(hl1b1 + hl1b2);
        danglingLine2.setR(r);
        danglingLine2.setX(x);
        danglingLine2.setG(hl2g1 + hl2g2);
        danglingLine2.setB(hl2b1 + hl2b2);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);

        // Reuse adder
        ValidationException e = assertThrows(ValidationException.class, () -> adder.setId("testTie2").add());
        assertTrue(e.getMessage().contains("already has a tie line"));

        // Update power flows, voltages and angles
        double p1 = -605.0;
        double q1 = -302.5;
        double p2 = 600.0;
        double q2 = 300.0;
        double v1 = 420.0;
        double v2 = 380.0;
        double angle1 = -1e-4;
        double angle2 = -1.7e-3;
        tieLine.getDanglingLine1().getTerminal().setP(p1).setQ(q1).getBusView().getBus().setV(v1).setAngle(angle1);
        tieLine.getDanglingLine2().getTerminal().setP(p2).setQ(q2).getBusView().getBus().setV(v2).setAngle(angle2);

        // test boundaries values
        SV expectedSV1 = new SV(p1, q1, v1, angle1, Branch.Side.ONE);
        SV expectedSV2 = new SV(p2, q2, v2, angle2, Branch.Side.TWO);
        assertEquals(expectedSV1.otherSideP(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV1.otherSideQ(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV2.otherSideP(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV2.otherSideQ(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV1.otherSideU(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getV(), 0.0d);
        assertEquals(expectedSV1.otherSideA(tieLine.getDanglingLine1(), true), tieLine.getDanglingLine1().getBoundary().getAngle(), 0.0d);
        assertEquals(expectedSV2.otherSideU(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getV(), 0.0d);
        assertEquals(expectedSV2.otherSideA(tieLine.getDanglingLine2(), true), tieLine.getDanglingLine2().getBoundary().getAngle(), 0.0d);
    }

    @Test
    public void danglingLine1NotSet() {
        // adder
        ValidationException e = assertThrows(ValidationException.class, () -> network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .add());
        assertTrue(e.getMessage().contains("undefined dangling line"));
    }

    @Test
    public void danglingLine2NotSet() {
        // adder
        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus("busA")
                .setId("hl1")
                .setName(DANGLING1_NAME)
                .setR(10.0)
                .setX(20.0)
                .setB(80.0)
                .setG(65.0)
                .setUcteXnodeCode("ucte")
                .add();
        // adder
        ValidationException e = assertThrows(ValidationException.class, () -> network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .setDanglingLine1(dl1.getId())
                .add());
        assertTrue(e.getMessage().contains("undefined dangling line"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, Double.NaN, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, 1.0, Double.NaN,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                Double.NaN, 8.5, "code"));
        assertTrue(e.getMessage().contains("g is invalid"));
    }

    @Test
    public void invalidDanglingLineCharacteristicsB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                6.5, Double.NaN, "code"));
        assertTrue(e.getMessage().contains("b is invalid"));
    }

    @Test
    public void danglingLineIdNull() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, null, 1.0, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("Dangling line id is not set"));
    }

    @Test
    public void danglingLineIdEmpty() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createTieLineWithDanglingline2ByDefault(INVALID, INVALID, "", 1.0, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("Invalid id ''"));
    }

    @Test
    public void duplicate() {
        createTieLineWithDanglingline2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                6.5, 8.5, DUPLICATE);
        assertThrows(PowsyblException.class, () -> createTieLineWithDanglingline2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                6.5, 8.5, DUPLICATE));
    }

    @Test
    public void testRemove() {
        createTieLineWithDanglingline2ByDefault(TO_REMOVE, TO_REMOVE, "id1", 1.0, 2.0,
                6.5, 8.5, TO_REMOVE);
        TieLine line = network.getTieLine(TO_REMOVE);
        assertNotNull(line);
        int count = network.getTieLineCount();
        line.remove();
        assertNull(network.getLine(TO_REMOVE));
        assertNotNull(line);
        assertEquals(count - 1L, network.getTieLineCount());
    }

    private void createTieLineWithDanglingline2ByDefault(String id, String name, String danglingLineId, double r, double x,
                                                         double g, double b, String code) {
        DanglingLine dl1 = voltageLevelA.newDanglingLine()
                .setBus("busA")
                .setId(danglingLineId)
                .setName(DANGLING1_NAME)
                .setR(r)
                .setX(x)
                .setB(b)
                .setG(g)
                .add();
        DanglingLine dl2 = voltageLevelB.newDanglingLine()
                .setBus("busB")
                .setId("hl2")
                .setName("half2_name")
                .setR(1.0)
                .setX(2.0)
                .setB(6.5)
                .setG(8.5)
                .setUcteXnodeCode(code)
                .add();
        network.newTieLine()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setName(name)
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId())
                .add();
    }
}
