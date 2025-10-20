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
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.iidm.network.util.TieLineUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
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
        BoundaryLine dl1 = voltageLevelA.newBoundaryLine()
                .setBus("busA")
                .setId("hl1")
                .setEnsureIdUnicity(true)
                .setName(DANGLING1_NAME)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(r)
                .setX(x)
                .setB(hl1b1 + hl1b2)
                .setG(hl1g1 + hl1g2)
                .setPairingKey("ucte")
                .add();
        BoundaryLine dl2 = voltageLevelB.newBoundaryLine()
                .setBus("busB")
                .setId("hl2")
                .setEnsureIdUnicity(true)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(r2)
                .setX(x2)
                .setB(hl2b1 + hl2b2)
                .setG(hl2g1 + hl2g2)
                .add();

        assertEquals(List.of(dl1, dl2), network.getBoundaryLines(BoundaryLineFilter.UNPAIRED));
        assertFalse(network.getBoundaryLines(BoundaryLineFilter.PAIRED).iterator().hasNext());
        assertEquals(List.of(dl1, dl2), network.getBoundaryLines());

        // test paired dangling line retrieval - For unpaired dangling lines
        assertFalse(TieLineUtil.getPairedBoundaryLine(dl1).isPresent());
        assertFalse(TieLineUtil.getPairedBoundaryLine(dl2).isPresent());

        TieLineAdder adder = network.newTieLine().setId("testTie")
                .setName("testNameTie")
                .setBoundaryLine1(dl1.getId())
                .setBoundaryLine2(dl2.getId());
        TieLine tieLine = adder.add();

        assertEquals(List.of(dl1, dl2), network.getBoundaryLines(BoundaryLineFilter.PAIRED));
        assertFalse(network.getBoundaryLines(BoundaryLineFilter.UNPAIRED).iterator().hasNext());

        assertEquals(IdentifiableType.TIE_LINE, tieLine.getType());
        assertEquals("ucte", tieLine.getPairingKey());
        assertEquals("hl1", tieLine.getBoundaryLine1().getId());
        assertEquals(DANGLING1_NAME, tieLine.getBoundaryLine1().getOptionalName().orElse(null));
        assertEquals("hl2", tieLine.getBoundaryLine2().getId());
        assertEquals("hl1", tieLine.getBoundaryLine(voltageLevelA.getId()).getId());
        assertEquals("hl2", tieLine.getBoundaryLine(voltageLevelB.getId()).getId());
        assertNull(tieLine.getBoundaryLine("UnknownVoltageLevelId"));
        assertEquals(11.0, tieLine.getR(), tol);
        assertEquals(22.0, tieLine.getX(), tol);
        assertEquals(0.065, tieLine.getG1(), tol);
        assertEquals(0.02649999999, tieLine.getG2(), tol);
        assertEquals(0.08499999999, tieLine.getB1(), tol);
        assertEquals(0.0285, tieLine.getB2(), tol);

        BoundaryLine boundaryLine1 = tieLine.getBoundaryLine1();
        BoundaryLine boundaryLine2 = tieLine.getBoundaryLine2();

        // Check notification on BoundaryLine changes
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);
        // Apply changes on dangling lines
        boundaryLine1.setR(r + 1);
        boundaryLine1.setX(x + 1);
        boundaryLine1.setG(hl1g1 + hl1g2 + 2);
        boundaryLine1.setB(hl1b1 + hl1b2 + 2);
        boundaryLine2.setR(r + 1);
        boundaryLine2.setX(x + 1);
        boundaryLine2.setG(hl2g1 + hl1g2 + 2);
        boundaryLine2.setB(hl2b1 + hl2b2 + 2);
        verify(mockedListener, times(8)).onUpdate(any(BoundaryLine.class), anyString(), nullable(String.class), any(), any());
        // Remove observer
        network.removeListener(mockedListener);
        // Cancel changes on dangling lines
        boundaryLine1.setR(r);
        boundaryLine1.setX(x);
        boundaryLine1.setG(hl1g1 + hl1g2);
        boundaryLine1.setB(hl1b1 + hl1b2);
        boundaryLine2.setR(r);
        boundaryLine2.setX(x);
        boundaryLine2.setG(hl2g1 + hl2g2);
        boundaryLine2.setB(hl2b1 + hl2b2);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);

        // Reuse adder
        adder.setId("testTie2");
        ValidationException e = assertThrows(ValidationException.class, adder::add);
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
        boundaryLine1.getTerminal().setP(p1).setQ(q1).getBusView().getBus().setV(v1).setAngle(angle1);
        boundaryLine2.getTerminal().setP(p2).setQ(q2).getBusView().getBus().setV(v2).setAngle(angle2);

        // test boundaries values
        SV expectedSV1 = new SV(p1, q1, v1, angle1, TwoSides.ONE);
        SV expectedSV2 = new SV(p2, q2, v2, angle2, TwoSides.ONE);
        assertEquals(expectedSV1.otherSideP(boundaryLine1, false), boundaryLine1.getBoundary().getP(), 0.0d);
        assertEquals(expectedSV1.otherSideQ(boundaryLine1, false), boundaryLine1.getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV2.otherSideP(boundaryLine2, false), boundaryLine2.getBoundary().getP(), 0.0d);
        assertEquals(expectedSV2.otherSideQ(boundaryLine2, false), boundaryLine2.getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV1.otherSideU(boundaryLine1, false), boundaryLine1.getBoundary().getV(), 0.0d);
        assertEquals(expectedSV1.otherSideA(boundaryLine1, false), boundaryLine1.getBoundary().getAngle(), 0.0d);
        assertEquals(expectedSV2.otherSideU(boundaryLine2, false), boundaryLine2.getBoundary().getV(), 0.0d);
        assertEquals(expectedSV2.otherSideA(boundaryLine2, false), boundaryLine2.getBoundary().getAngle(), 0.0d);

        // test paired dangling line retrieval - For paired dangling lines
        Optional<BoundaryLine> otherSide1 = TieLineUtil.getPairedBoundaryLine(boundaryLine1);
        assertTrue(otherSide1.isPresent());
        assertEquals(boundaryLine2, otherSide1.orElseThrow());
        Optional<BoundaryLine> otherSide2 = TieLineUtil.getPairedBoundaryLine(boundaryLine2);
        assertTrue(otherSide2.isPresent());
        assertEquals(boundaryLine1, otherSide2.orElseThrow());

        // try to change pairing key, but not allowed.
        assertThrows(ValidationException.class, () -> boundaryLine1.setPairingKey("new_code"),
                "Dangling line 'hl1': pairing key cannot be set if dangling line is paired.");
    }

    @Test
    public void boundaryLine1NotSet() {
        // adder
        TieLineAdder tieLineAdder = network.newTieLine()
                .setId("testTie")
                .setName("testNameTie");
        ValidationException e = assertThrows(ValidationException.class, tieLineAdder::add);
        assertTrue(e.getMessage().contains("undefined dangling line"));
    }

    @Test
    public void boundaryLine2NotSet() {
        // adder
        BoundaryLine dl1 = voltageLevelA.newBoundaryLine()
                .setBus("busA")
                .setId("hl1")
                .setName(DANGLING1_NAME)
                .setR(10.0)
                .setX(20.0)
                .setB(80.0)
                .setG(65.0)
                .setP0(0.0)
                .setQ0(0.0)
                .setPairingKey("ucte")
                .add();
        // adder
        TieLineAdder tieLineAdder = network.newTieLine()
                .setId("testTie")
                .setName("testNameTie")
                .setBoundaryLine1(dl1.getId());
        ValidationException e = assertThrows(ValidationException.class, tieLineAdder::add);
        assertTrue(e.getMessage().contains("undefined dangling line"));
    }

    @Test
    public void invalidBoundaryLineCharacteristicsR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithBoundaryLine2ByDefault(INVALID, INVALID, INVALID, Double.NaN, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void invalidBoundaryLineCharacteristicsX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithBoundaryLine2ByDefault(INVALID, INVALID, INVALID, 1.0, Double.NaN,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void invalidBoundaryLineCharacteristicsG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithBoundaryLine2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                Double.NaN, 8.5, "code"));
        assertTrue(e.getMessage().contains("g is invalid"));
    }

    @Test
    public void invalidBoundaryLineCharacteristicsB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTieLineWithBoundaryLine2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                6.5, Double.NaN, "code"));
        assertTrue(e.getMessage().contains("b is invalid"));
    }

    @Test
    public void boundaryLineIdNull() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createTieLineWithBoundaryLine2ByDefault(INVALID, INVALID, null, 1.0, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("Dangling line id is not set"));
    }

    @Test
    public void boundaryLineIdEmpty() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> createTieLineWithBoundaryLine2ByDefault(INVALID, INVALID, "", 1.0, 2.0,
                6.5, 8.5, "code"));
        assertTrue(e.getMessage().contains("Invalid id ''"));
    }

    @Test
    public void duplicate() {
        createTieLineWithBoundaryLine2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                6.5, 8.5, DUPLICATE);
        assertThrows(PowsyblException.class, () -> createTieLineWithBoundaryLine2ByDefault(DUPLICATE, DUPLICATE, "id1", 1.0, 2.0,
                6.5, 8.5, DUPLICATE));
    }

    @Test
    public void testRemove() {
        createTieLineWithBoundaryLine2ByDefault(TO_REMOVE, TO_REMOVE, "id1", 1.0, 2.0,
                6.5, 8.5, TO_REMOVE);
        TieLine line = network.getTieLine(TO_REMOVE);
        assertNotNull(line);
        int count = network.getTieLineCount();
        line.remove();
        assertNull(network.getLine(TO_REMOVE));
        assertNotNull(line);
        assertEquals(count - 1L, network.getTieLineCount());
    }

    @Test
    public void testRemoveUpdateBoundaryLines() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        assertNotNull(line1);
        assertNotNull(line2);
        assertEquals(0.0, line1.getBoundaryLine1().getP0());
        assertEquals(0.0, line1.getBoundaryLine1().getQ0());
        assertEquals(0.0, line1.getBoundaryLine2().getP0());
        assertEquals(0.0, line1.getBoundaryLine2().getQ0());
        assertEquals(0.0, line2.getBoundaryLine1().getP0());
        assertEquals(0.0, line2.getBoundaryLine1().getQ0());
        assertEquals(0.0, line2.getBoundaryLine2().getP0());
        assertEquals(0.0, line2.getBoundaryLine2().getQ0());
        line1.remove(true);
        line2.remove(true);
        assertEquals(301.278, line1.getBoundaryLine1().getP0(), 0.001);
        assertEquals(116.563, line1.getBoundaryLine1().getQ0(), 0.001);
        assertEquals(-301.745, line1.getBoundaryLine2().getP0(), 0.001);
        assertEquals(-116.566, line1.getBoundaryLine2().getQ0(), 0.001);
        assertEquals(301.278, line2.getBoundaryLine1().getP0(), 0.001);
        assertEquals(116.563, line2.getBoundaryLine1().getQ0(), 0.001);
        assertEquals(-301.745, line2.getBoundaryLine2().getP0(), 0.001);
        assertEquals(-116.567, line2.getBoundaryLine2().getQ0(), 0.001);
    }

    @Test
    public void testRemoveUpdateBoundaryLinesNotCalculated() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        assertNotNull(line1);
        assertNotNull(line2);
        assertEquals(0.0, line1.getBoundaryLine1().getP0());
        assertEquals(0.0, line1.getBoundaryLine1().getQ0());
        assertEquals(0.0, line1.getBoundaryLine2().getP0());
        assertEquals(0.0, line1.getBoundaryLine2().getQ0());
        assertEquals(0.0, line2.getBoundaryLine1().getP0());
        assertEquals(0.0, line2.getBoundaryLine1().getQ0());
        assertEquals(0.0, line2.getBoundaryLine2().getP0());
        assertEquals(0.0, line2.getBoundaryLine2().getQ0());
        // reset the terminal flows at dangling lines, we simulate we do not have calculated
        line1.getBoundaryLine1().getTerminal().setP(Double.NaN);
        line1.getBoundaryLine1().getTerminal().setQ(Double.NaN);
        line1.getBoundaryLine2().getTerminal().setP(Double.NaN);
        line1.getBoundaryLine2().getTerminal().setQ(Double.NaN);
        line2.getBoundaryLine1().getTerminal().setP(Double.NaN);
        line2.getBoundaryLine1().getTerminal().setQ(Double.NaN);
        line2.getBoundaryLine2().getTerminal().setP(Double.NaN);
        line2.getBoundaryLine2().getTerminal().setQ(Double.NaN);
        // Set some non-zero p0, q0 values to check that:
        // if we remove the tie line without flows calculated
        // p0, q0 of dangling lines are preserved
        line1.getBoundaryLine1().setP0(10);
        line1.getBoundaryLine1().setQ0(20);
        line1.getBoundaryLine2().setP0(-10);
        line1.getBoundaryLine2().setQ0(-20);
        line1.remove(true);
        line2.remove(true);
        assertEquals(10, line1.getBoundaryLine1().getP0(), 0.001);
        assertEquals(20, line1.getBoundaryLine1().getQ0(), 0.001);
        assertEquals(-10, line1.getBoundaryLine2().getP0(), 0.001);
        assertEquals(-20, line1.getBoundaryLine2().getQ0(), 0.001);
        assertEquals(0, line2.getBoundaryLine1().getP0(), 0.001);
        assertEquals(0, line2.getBoundaryLine1().getQ0(), 0.001);
        assertEquals(0, line2.getBoundaryLine2().getP0(), 0.001);
        assertEquals(0, line2.getBoundaryLine2().getQ0(), 0.001);
    }

    @Test
    public void testRemoveUpdateBoundaryLinesDcCalculated() {
        Network eurostagNetwork = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line1 = eurostagNetwork.getTieLine("NHV1_NHV2_1");
        TieLine line2 = eurostagNetwork.getTieLine("NHV1_NHV2_2");
        assertNotNull(line1);
        assertNotNull(line2);
        assertEquals(0.0, line1.getBoundaryLine1().getP0());
        assertEquals(0.0, line1.getBoundaryLine1().getQ0());
        assertEquals(0.0, line1.getBoundaryLine2().getP0());
        assertEquals(0.0, line1.getBoundaryLine2().getQ0());
        assertEquals(0.0, line2.getBoundaryLine1().getP0());
        assertEquals(0.0, line2.getBoundaryLine1().getQ0());
        assertEquals(0.0, line2.getBoundaryLine2().getP0());
        assertEquals(0.0, line2.getBoundaryLine2().getQ0());
        // reset only the terminal q values (simulate only a dc load flow has been calculated)
        line1.getBoundaryLine1().getTerminal().setQ(Double.NaN);
        line1.getBoundaryLine2().getTerminal().setQ(Double.NaN);
        line2.getBoundaryLine1().getTerminal().setQ(Double.NaN);
        line2.getBoundaryLine2().getTerminal().setQ(Double.NaN);
        // Set some non-zero p0, q0 values to check that:
        // if we remove the tie line with only p flows calculated
        // q0 values of dangling lines are preserved
        line1.getBoundaryLine1().setP0(10);
        line1.getBoundaryLine1().setQ0(20);
        line1.getBoundaryLine2().setP0(-10);
        line1.getBoundaryLine2().setQ0(-20);
        line1.remove(true);
        line2.remove(true);
        assertEquals(302.444, line1.getBoundaryLine1().getP0(), 0.001);
        assertEquals(20, line1.getBoundaryLine1().getQ0(), 0.001);
        assertEquals(-300.434, line1.getBoundaryLine2().getP0(), 0.001);
        assertEquals(-20, line1.getBoundaryLine2().getQ0(), 0.001);
        assertEquals(302.444, line2.getBoundaryLine1().getP0(), 0.001);
        assertEquals(0, line2.getBoundaryLine1().getQ0(), 0.001);
        assertEquals(-300.434, line2.getBoundaryLine2().getP0(), 0.001);
        assertEquals(0, line2.getBoundaryLine2().getQ0(), 0.001);
    }

    private void createTieLineWithBoundaryLine2ByDefault(String id, String name, String boundaryLineId, double r, double x,
                                                         double g, double b, String code) {
        BoundaryLine dl1 = voltageLevelA.newBoundaryLine()
                .setBus("busA")
                .setId(boundaryLineId)
                .setName(DANGLING1_NAME)
                .setR(r)
                .setX(x)
                .setB(b)
                .setG(g)
                .setP0(0)
                .setQ0(0)
                .add();
        BoundaryLine dl2 = voltageLevelB.newBoundaryLine()
                .setBus("busB")
                .setId("hl2")
                .setName("half2_name")
                .setR(1.0)
                .setX(2.0)
                .setB(6.5)
                .setG(8.5)
                .setP0(0)
                .setQ0(0)
                .setPairingKey(code)
                .add();
        network.newTieLine()
                .setId(id)
                .setEnsureIdUnicity(true)
                .setName(name)
                .setBoundaryLine1(dl1.getId())
                .setBoundaryLine2(dl2.getId())
                .add();
    }

    @Test
    void testConnectDisconnect() {
        Network networkWithTieLine = createNetworkWithTieLine();
        TieLine tieLine = networkWithTieLine.getTieLine("TL");

        // Check that the tie line is connected
        assertTieLineConnection(tieLine, true, true);

        // Connection fails since it's already connected
        assertFalse(tieLine.connectBoundaryLines());

        // Disconnection fails if switches cannot be opened (here, only fictional switches could be opened)
        assertFalse(tieLine.disconnectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL.negate().and(SwitchPredicates.IS_OPEN.negate())));

        // Disconnection
        assertTrue(tieLine.disconnectBoundaryLines());
        assertTieLineConnection(tieLine, false, false);

        // Disconnection fails since it's already disconnected
        assertFalse(tieLine.disconnectBoundaryLines());

        // Connection fails if switches cannot be opened (here, only fictional switches could be closed)
        assertFalse(tieLine.connectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL.negate()));

        // Connection
        assertTrue(tieLine.connectBoundaryLines());
        assertTieLineConnection(tieLine, true, true);

        // Disconnect one side
        assertTrue(tieLine.disconnectBoundaryLines(SwitchPredicates.IS_CLOSED_BREAKER, TwoSides.ONE));
        assertTieLineConnection(tieLine, false, true);

        // Connection on the other side fails since it's still connected
        assertFalse(tieLine.connectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL_BREAKER, TwoSides.TWO));
    }

    private void assertTieLineConnection(TieLine tieLine, boolean expectedConnectionOnSide1, boolean expectedConnectionOnSide2) {
        assertEquals(expectedConnectionOnSide1, tieLine.getBoundaryLine1().getTerminal().isConnected());
        assertEquals(expectedConnectionOnSide2, tieLine.getBoundaryLine2().getTerminal().isConnected());
    }

    private Network createNetworkWithTieLine() {
        // Initialize the network
        Network networkWithTieLine = FourSubstationsNodeBreakerFactory.create();

        // Existing voltage levels in Node-breaker view
        VoltageLevel s1vl1 = networkWithTieLine.getVoltageLevel("S1VL1");

        // New voltage levels in bus-breaker view
        VoltageLevel s2vl2 = networkWithTieLine.getSubstation("S2").newVoltageLevel()
            .setId("S2VL2")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        // New buses
        s2vl2.getBusBreakerView()
            .newBus()
            .setId("bus22")
            .add();

        /*
         * First Tie line on node-breaker
         */
        // Add a dangling line in the first Voltage level
        createSwitch(s1vl1, "S1VL1_DL_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 20);
        createSwitch(s1vl1, "S1VL1_DL_BREAKER", SwitchKind.BREAKER, false, 20, 21);
        BoundaryLine boundaryLine1 = s1vl1.newBoundaryLine()
            .setId("NHV1_XNODE1")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(20.0)
            .setG(1E-6)
            .setB(386E-6 / 2)
            .setNode(21)
            .setPairingKey("XNODE1")
            .add();

        // Add a dangling line in the second Voltage level
        BoundaryLine boundaryLine2 = s2vl2.newBoundaryLine()
            .setId("S2VL2_DL")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(13.0)
            .setG(2E-6)
            .setB(386E-6 / 2)
            .setBus("bus22")
            .setPairingKey("XNODE1")
            .add();
        networkWithTieLine.newTieLine()
            .setId("TL")
            .setBoundaryLine1(boundaryLine1.getId())
            .setBoundaryLine2(boundaryLine2.getId())
            .add();

        return networkWithTieLine;
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
            .setId(id)
            .setName(id)
            .setKind(kind)
            .setRetained(kind.equals(SwitchKind.BREAKER))
            .setOpen(open)
            .setFictitious(false)
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }
}
