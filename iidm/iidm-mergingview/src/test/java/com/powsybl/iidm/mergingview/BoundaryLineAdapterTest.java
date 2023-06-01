/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.SV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class BoundaryLineAdapterTest {

    private MergingView mergingView;

    private Network noEquipNetwork;
    private Network eurostagNetwork;

    @BeforeEach
    void initNetwork() {
        mergingView = MergingView.create("BoundaryLineAdapterTest", "iidm");
        noEquipNetwork = NoEquipmentNetworkFactory.create();
        eurostagNetwork = EurostagTutorialExample1Factory.create();
    }

    @Test
    void baseTests() {
        mergingView.merge(noEquipNetwork);

        double r = 10.0;
        double x = 20.0;
        double g = 30.0;
        double b = 40.0;
        double p0 = 50.0;
        double q0 = 60.0;
        String id = "danglingId";
        String name = "danglingName";
        String ucteXnodeCode = "standalone";
        String busId = "busA";
        String voltageLevelId = "vl1";

        // adder
        BoundaryLine boundaryLine = createBoundaryLine(mergingView, voltageLevelId, id, name, r, x, g, b, p0, q0, ucteXnodeCode, busId);
        assertNotNull(mergingView.getBoundaryLine(id));
        assertTrue(boundaryLine instanceof BoundaryLineAdapter);
        assertSame(mergingView, boundaryLine.getNetwork());

        assertEquals(IdentifiableType.DANGLING_LINE, boundaryLine.getType());
        assertEquals(r, boundaryLine.getR(), 0.0);
        assertEquals(x, boundaryLine.getX(), 0.0);
        assertEquals(g, boundaryLine.getG(), 0.0);
        assertEquals(b, boundaryLine.getB(), 0.0);
        assertEquals(p0, boundaryLine.getP0(), 0.0);
        assertEquals(q0, boundaryLine.getQ0(), 0.0);
        assertEquals(id, boundaryLine.getId());
        assertEquals(name, boundaryLine.getOptionalName().orElse(null));
        assertEquals(name, boundaryLine.getNameOrId());
        assertEquals(ucteXnodeCode, boundaryLine.getUcteXnodeCode());

        // setter getter
        double r2 = 11.0;
        double x2 = 21.0;
        double g2 = 31.0;
        double b2 = 41.0;
        double p02 = 51.0;
        double q02 = 61.0;
        boundaryLine.setR(r2);
        assertEquals(r2, boundaryLine.getR(), 0.0);
        boundaryLine.setX(x2);
        assertEquals(x2, boundaryLine.getX(), 0.0);
        boundaryLine.setG(g2);
        assertEquals(g2, boundaryLine.getG(), 0.0);
        boundaryLine.setB(b2);
        assertEquals(b2, boundaryLine.getB(), 0.0);
        boundaryLine.setP0(p02);
        assertEquals(p02, boundaryLine.getP0(), 0.0);
        boundaryLine.setQ0(q02);
        assertEquals(q02, boundaryLine.getQ0(), 0.0);

        boundaryLine.newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();
        assertEquals(100.0, boundaryLine.getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertTrue(boundaryLine.getTerminal() instanceof TerminalAdapter);
        boundaryLine.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });
        assertEquals(1, boundaryLine.getTerminals().size());

        try {
            createBoundaryLine(mergingView, voltageLevelId, id, name, r, x, g, b, p0, q0, ucteXnodeCode, busId);
            fail();
        } catch (PowsyblException e) {
            assertEquals("The network already contains an object 'BoundaryLineAdapter' with the id 'danglingId'", e.getMessage());
        }
    }

    @Test
    void pairedBoundaryLine() {
        mergingView.merge(noEquipNetwork);
        double p10 = 0.11710908004064359;
        double q10 = -0.012883304869602126;
        final BoundaryLine bl1 = createBoundaryLine(mergingView, "vl1", "dl1", "dl1", 0.01138, 0.05017, 0.0, 0.06280, p10, q10, "code", "busA");
        bl1.setProperty("keyTest", "test");
        assertNotNull(mergingView.getBoundaryLine("dl1"));
        assertEquals(1, mergingView.getBoundaryLineCount());
        assertEquals(0, mergingView.getLineCount());
        double p20 = -0.11713527;
        double q20 = 0.01301712;
        final BoundaryLine bl2 = createBoundaryLine(eurostagNetwork, "VLHV1", "dl2", "dl2", 0.01038, 0.04917, 0.0, 0.07280, p20, q20, "code", "NHV1");
        mergingView.merge(eurostagNetwork);
        // Check no access to Dl1 & Dl2
        assertEquals(2, mergingView.getBoundaryLineCount());
        assertNotNull(mergingView.getBoundaryLine("dl1"));
        assertNotNull(mergingView.getBoundaryLine("dl2"));
        // Check access to MergedLine
        assertEquals(2, mergingView.getLineCount());
        assertEquals(4, mergingView.getBranchCount());
        final TieLine line = mergingView.getTieLine("dl1 + dl2");
        assertEquals(1, mergingView.getTieLineCount());
        assertSame(line, mergingView.getIdentifiable("dl1 + dl2"));
        assertSame(line.getBoundaryLine1(), mergingView.getIdentifiable("dl1"));
        assertSame(line.getBoundaryLine2(), mergingView.getIdentifiable("dl2"));
        assertTrue(line instanceof MergedLine);
        assertTrue(mergingView.getIdentifiables().contains(line));

        // Properties
        assertFalse(line.removeProperty("noFound"));
        assertTrue(line.hasProperty("keyTest"));
        assertTrue(line.removeProperty("keyTest"));
        assertFalse(line.hasProperty("keyTest"));
        line.setProperty("keyTest", "test");
        assertTrue(line.hasProperty("keyTest"));
        assertTrue(line.removeProperty("keyTest"));
        assertFalse(line.hasProperty("keyTest"));

        // MergedLine
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals(IdentifiableType.TIE_LINE, mergedLine.getType());
        assertSame(mergingView, mergedLine.getNetwork());
        assertSame(bl1.getTerminal(), mergedLine.getBoundaryLine(Branch.Side.ONE).getTerminal());
        assertSame(bl1.getTerminal(), mergedLine.getBoundaryLine1().getTerminal());
        final CurrentLimits currentLimits1 = mergedLine.getBoundaryLine1().newCurrentLimits()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400)
                .endTemporaryLimit()
                .add();
        final ActivePowerLimits activePowerLimits1 = mergedLine.getBoundaryLine1().newActivePowerLimits()
                .setPermanentLimit(600)
                .add();
        final ApparentPowerLimits apparentPowerLimits1 = mergedLine.getBoundaryLine1().newApparentPowerLimits()
                .setPermanentLimit(110.0)
                .add();
        final CurrentLimits currentLimits2 = mergedLine.getBoundaryLine2().newCurrentLimits()
                .setPermanentLimit(50)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        final ActivePowerLimits activePowerLimits2 = mergedLine.getBoundaryLine2().newActivePowerLimits()
                .setPermanentLimit(800)
                .add();
        final ApparentPowerLimits apparentPowerLimits2 = mergedLine.getBoundaryLine2().newApparentPowerLimits()
                .setPermanentLimit(132.4)
                .add();

        assertSame(currentLimits1, mergedLine.getBoundaryLine1().getCurrentLimits().orElse(null));
        assertSame(activePowerLimits1, mergedLine.getBoundaryLine1().getActivePowerLimits().orElse(null));
        assertSame(apparentPowerLimits1, mergedLine.getBoundaryLine1().getApparentPowerLimits().orElse(null));
        assertEquals(3, mergedLine.getBoundaryLine1().getOperationalLimits().size());
        assertTrue(mergedLine.getBoundaryLine1().getOperationalLimits().contains(currentLimits1));
        assertTrue(mergedLine.getBoundaryLine1().getOperationalLimits().contains(activePowerLimits1));
        assertTrue(mergedLine.getBoundaryLine1().getOperationalLimits().contains(apparentPowerLimits1));
        assertSame(currentLimits2, mergedLine.getBoundaryLine2().getCurrentLimits().orElse(null));
        assertSame(activePowerLimits2, mergedLine.getBoundaryLine2().getActivePowerLimits().orElse(null));
        assertSame(apparentPowerLimits2, mergedLine.getBoundaryLine2().getApparentPowerLimits().orElse(null));
        assertEquals(3, mergedLine.getBoundaryLine2().getOperationalLimits().size());
        assertTrue(mergedLine.getBoundaryLine2().getOperationalLimits().contains(currentLimits2));
        assertTrue(mergedLine.getBoundaryLine2().getOperationalLimits().contains(activePowerLimits2));
        assertTrue(mergedLine.getBoundaryLine2().getOperationalLimits().contains(apparentPowerLimits2));
        assertSame(currentLimits1, mergedLine.getBoundaryLine(Branch.Side.ONE).getCurrentLimits().orElse(null));
        assertSame(activePowerLimits1, mergedLine.getBoundaryLine(Branch.Side.ONE).getActivePowerLimits().orElse(null));
        assertSame(apparentPowerLimits2, mergedLine.getBoundaryLine(Branch.Side.TWO).getApparentPowerLimits().orElse(null));

        assertEquals("dl1 + dl2", mergedLine.getId());
        assertEquals("dl1 + dl2", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl1 + dl2", mergedLine.getNameOrId());
        assertEquals(0.02176, mergedLine.getR(), 1.0e-10);
        assertEquals(0.09934, mergedLine.getX(), 1.0e-10);
        assertEquals(0.0, mergedLine.getG1(), 1.0e-10);
        assertEquals(0.0, mergedLine.getG2(), 1.0e-10);
        assertEquals(0.06280, mergedLine.getB1(), 1.0e-10);
        assertEquals(0.07280, mergedLine.getB2(), 1.0e-10);
        assertEquals(p10, bl1.getP0(), 0.0d);
        assertEquals(q10, bl1.getQ0(), 0.0d);
        assertEquals(p20, bl2.getP0(), 0.0d);
        assertEquals(q20, bl2.getQ0(), 0.0d);

        /*assertFalse(mergedLine.isOverloaded());
        assertEquals(Integer.MAX_VALUE, mergedLine.getOverloadDuration());
        assertFalse(mergedLine.checkPermanentLimit(Branch.Side.ONE, LimitType.CURRENT));
        assertFalse(mergedLine.checkPermanentLimit(Branch.Side.TWO, LimitType.CURRENT));
        assertFalse(mergedLine.checkPermanentLimit(Branch.Side.ONE));
        assertFalse(mergedLine.checkPermanentLimit(Branch.Side.TWO));
        assertFalse(mergedLine.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(mergedLine.checkPermanentLimit2(LimitType.CURRENT));
        assertFalse(mergedLine.checkPermanentLimit1());
        assertFalse(mergedLine.checkPermanentLimit2());
        assertNull(mergedLine.checkTemporaryLimits(Branch.Side.ONE, LimitType.CURRENT));
        assertNull(mergedLine.checkTemporaryLimits(Branch.Side.TWO, LimitType.CURRENT));
        assertNull(mergedLine.checkTemporaryLimits(Branch.Side.ONE));
        assertNull(mergedLine.checkTemporaryLimits(Branch.Side.TWO));
        assertNull(mergedLine.checkTemporaryLimits1(1.0f, LimitType.CURRENT));
        assertNull(mergedLine.checkTemporaryLimits1(LimitType.CURRENT));
        assertNull(mergedLine.checkTemporaryLimits1(1.0f));
        assertNull(mergedLine.checkTemporaryLimits1());
        assertNull(mergedLine.checkTemporaryLimits2(1.0f, LimitType.CURRENT));
        assertNull(mergedLine.checkTemporaryLimits2(LimitType.CURRENT));
        assertNull(mergedLine.checkTemporaryLimits2(1.0f));
        assertNull(mergedLine.checkTemporaryLimits2());*/
        mergedLine.getBoundaryLine1().getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });
        mergedLine.getBoundaryLine2().getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        double p1 = 0.117273045626974;
        double q1 = -0.081804351176928;
        double p2 = -0.11700000;
        double q2 = -0.06700000;
        final BoundaryLine bl1Bis = mergedLine.getBoundaryLine("vl1");
        assertNotNull(bl1Bis);
        final Terminal t1 = bl1Bis.getTerminal();
        assertNotNull(t1);
        assertSame(mergedLine.getBoundaryLine(Branch.Side.ONE), bl1Bis);
        final BoundaryLine bl2Bis = mergedLine.getBoundaryLine("VLHV1");
        assertNotNull(bl2Bis);
        final Terminal t2 = bl2Bis.getTerminal();
        assertNotNull(t2);
        assertSame(mergedLine.getBoundaryLine(Branch.Side.TWO), bl2Bis);

        // Boundary
        assertSame(bl1, bl1.getBoundary().getBoundaryLine());
        assertSame(mergedLine.getBoundaryLine1().getTerminal().getVoltageLevel(), bl1.getBoundary().getNetworkSideVoltageLevel());
        assertSame(mergedLine.getBoundaryLine2().getTerminal().getVoltageLevel(), mergingView.getVoltageLevel(bl2.getBoundary().getNetworkSideVoltageLevel().getId()));

        // Update P & Q
        t1.setP(p1);
        t1.setQ(q1);
        t2.setP(p2);
        t2.setQ(q2);
        // Update V & Angle
        double v1 = 1.052585464510671;
        double angle1 = Math.toDegrees(-0.017414259263883);
        double v2 = 1.05137589;
        double angle2 = Math.toDegrees(-0.02818192);
        t1.getBusView().getBus().setV(v1).setAngle(angle1);
        t2.getBusView().getBus().setV(v2).setAngle(angle2);

        // Check P & Q are computed by Listener

        SV expectedSVbl1 = new SV(p1, q1, v1, angle1, Branch.Side.ONE).otherSide(bl1, true);
        SV expectedSVbl2 = new SV(p2, q2, v2, angle2, Branch.Side.ONE).otherSide(bl2, true);

        assertEquals(expectedSVbl1.getP(), bl1.getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVbl1.getP(), mergedLine.getBoundaryLine1().getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVbl1.getQ(), bl1.getBoundary().getQ(), 1.0e-8);
        assertEquals(expectedSVbl1.getQ(), mergedLine.getBoundaryLine1().getBoundary().getQ(), 1.0e-8);
        assertEquals(expectedSVbl2.getP(), bl2.getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVbl2.getP(), mergedLine.getBoundaryLine2().getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVbl2.getQ(), bl2.getBoundary().getQ(), 1.0e-8);
        assertEquals(expectedSVbl2.getQ(), mergedLine.getBoundaryLine2().getBoundary().getQ(), 1.0e-8);
        // Check V & Angle are computed by Listener
        assertEquals(expectedSVbl1.getU(), mergedLine.getBoundaryLine1().getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVbl1.getA(), bl1.getBoundary().getAngle(), 1.0e-7);
        assertEquals(expectedSVbl1.getA(), mergedLine.getBoundaryLine1().getBoundary().getAngle(), 1.0e-7);
        assertEquals(expectedSVbl1.getU(), bl1.getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVbl2.getU(), bl2.getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVbl2.getU(), mergedLine.getBoundaryLine2().getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVbl2.getA(), bl2.getBoundary().getAngle(), 1.0e-7);
        assertEquals(expectedSVbl2.getA(), mergedLine.getBoundaryLine2().getBoundary().getAngle(), 1.0e-7);

        SV expectedSVmlEnd2 = new SV(p1, q1, v1, angle1, Branch.Side.ONE).otherSide(mergedLine);
        assertEquals(p2, expectedSVmlEnd2.getP(), 1.0e-6);
        assertEquals(q2, expectedSVmlEnd2.getQ(), 1.0e-6);
        assertEquals(v2, expectedSVmlEnd2.getU(), 1.0e-6);
        assertEquals(angle2, expectedSVmlEnd2.getA(), 1.0e-6);
        SV expectedSVmlEnd1 = new SV(p2, q2, v2, angle2, Branch.Side.TWO).otherSide(mergedLine);
        assertEquals(p1, expectedSVmlEnd1.getP(), 1.0e-6);
        assertEquals(q1, expectedSVmlEnd1.getQ(), 1.0e-6);
        assertEquals(v1, expectedSVmlEnd1.getU(), 1.0e-6);
        assertEquals(angle1, expectedSVmlEnd1.getA(), 1.0e-6);

        mergedLine.setFictitious(true);
        assertTrue(mergedLine.isFictitious());

        // Exceptions when creating boundary lines with mergedline ids

        try {
            createBoundaryLine(mergingView, "vl1", "dl1", "dl1", 1.0, 1.0, 1.0, 1.0, p10, q10, "code", "busA");
            fail();
        } catch (PowsyblException e) {
            assertEquals("The network already contains an object 'BoundaryLineAdapter' with the id 'dl1'", e.getMessage());
        }

        try {
            createBoundaryLine(mergingView, "vl1", "dl1 + dl2", "dl1 + dl2", 1.0, 1.0, 1.0, 1.0, p10, q10, "code", "busA");
            fail();
        } catch (PowsyblException e) {
            assertEquals("The network already contains an object 'MergedLine' with the id 'dl1 + dl2'", e.getMessage());
        }

        // Not implemented yet !
        TestUtil.notImplemented(mergedLine::remove);
        TestUtil.notImplemented(() -> mergedLine.addExtension(null, null));
        assertNull(mergedLine.getExtension(null));
        assertNull(mergedLine.getExtensionByName(""));
        TestUtil.notImplemented(() -> mergedLine.removeExtension(null));
        assertNotNull(mergedLine.getExtensions());
        assertEquals(0, mergedLine.getExtensions().size());
    }

    @Test
    void testProperties() {
        final BoundaryLine bl1 = createBoundaryLine(noEquipNetwork, "vl1", "dl1", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busA");
        bl1.setProperty("ucteCode", bl1.getUcteXnodeCode()); // test equals property
        bl1.setProperty("id", bl1.getId()); // test not equals property
        bl1.setProperty("network", "noEquipNetwork"); // test empty property
        bl1.setProperty("vl", ""); // test empty property

        final BoundaryLine bl2 = createBoundaryLine(eurostagNetwork, "VLHV1", "dl2", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "NHV1");
        bl2.setProperty("ucteCode", bl2.getUcteXnodeCode()); // test equals property
        bl2.setProperty("id", bl2.getId()); // test not equals property
        bl2.setProperty("network", ""); // test empty property
        bl2.setProperty("vl", "vl2"); // test empty property

        mergingView.merge(noEquipNetwork, eurostagNetwork);
        final TieLine line = mergingView.getTieLine("dl1 + dl2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("dl", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl", mergedLine.getNameOrId());

        assertTrue(mergedLine.hasProperty());
        assertTrue(mergedLine.hasProperty("ucteCode"));
        assertEquals(11, mergedLine.getPropertyNames().size());
        mergedLine.setProperty("key", "value");
        assertEquals("value", mergedLine.getProperty("key"));
        assertEquals("defaultValue", mergedLine.getProperty("noKey", "defaultValue"));
    }

    @Test
    void testListener() {
        mergingView.merge(noEquipNetwork);
        mergingView.newSubstation().setId("S").add().newVoltageLevel().setId("VL").setNominalV(220).setTopologyKind(TopologyKind.BUS_BREAKER).add().getBusBreakerView().newBus().setId("B").add();
        createBoundaryLine(mergingView, "VL", "testListener1", "testListener2", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "B");
        assertNotNull(mergingView.getBoundaryLine("testListener1"));
        assertEquals(1, mergingView.getBoundaryLineCount());
        assertEquals(0, mergingView.getLineCount());
        createBoundaryLine(noEquipNetwork, "vl2", "testListener2", "testListener1", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "busB");
        assertNotNull(mergingView.getBoundaryLine("testListener1"));
        assertNotNull(mergingView.getBoundaryLine("testListener2"));
        assertEquals(2, mergingView.getBoundaryLineCount());
        assertEquals(0, mergingView.getLineCount());
        assertEquals(1, mergingView.getTieLineCount());
        final TieLine line = mergingView.getTieLine("testListener1 + testListener2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("testListener2 + testListener1", mergedLine.getOptionalName().orElse(null));
        assertEquals("testListener2 + testListener1", mergedLine.getNameOrId());
    }

    @Test
    void pairedBoundaryLineWithSameId() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createBoundaryLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "NGEN");
        createBoundaryLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "busA");
        mergingView.merge(network, noEquipNetwork);
        assertNotNull(mergingView.getBoundaryLine("dl"));
        TieLine merged = mergingView.getTieLine("dl"); // FIXME two identifiables with same ID in merging view (fringe case)
        assertNotNull(merged);
    }

    @Test
    void failBoundaryLinesWithSameIdAndNullXnodeCode() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createBoundaryLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "NGEN");
        createBoundaryLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "busA");
        try {
            mergingView.merge(network, noEquipNetwork);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Boundary line couple dl have inconsistent Xnodes (null,null)", e.getMessage());
        }
    }

    @Test
    void failBoundaryLinesWithSameIdAndDifferentXnodeCode() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createBoundaryLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "NGEN");
        createBoundaryLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code2", "busA");
        try {
            mergingView.merge(network, noEquipNetwork);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Boundary line couple dl have inconsistent Xnodes (code,code2)", e.getMessage());
        }
    }

    @Test
    void multipleBoundaryLines() {
        createBoundaryLine(noEquipNetwork, "vl1", "dl1", "dl1_name", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busA");
        createBoundaryLine(eurostagNetwork, "VLHV1", "dl2", "dl2_name", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "NHV1");
        createBoundaryLine(eurostagNetwork, "VLHV1", "dl3", "dl3_name", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", null, "NHV1");
        mergingView.merge(noEquipNetwork, eurostagNetwork);
        assertNotNull(mergingView.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", mergingView.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", mergingView.getTieLine("dl1 + dl2").getNameOrId());
    }

    private static BoundaryLine createBoundaryLine(Network n, String vlId, String id, String name, double r, double x, double g, double b,
                                                   double p0, double q0, String ucteCode, String busId) {

        return createBoundaryLine(n, vlId, id, name, r, x, g, b, p0, q0, ucteCode, busId, busId);
    }

    private static BoundaryLine createBoundaryLine(Network n, String vlId, String id, String name, double r, double x, double g, double b,
                                                   double p0, double q0, String ucteCode, String busId, String connectableBusId) {

        return n.getVoltageLevel(vlId).newBoundaryLine()
                .setId(id)
                .setName(name)
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setP0(p0)
                .setQ0(q0)
                .setUcteXnodeCode(ucteCode)
                .setBus(busId)
                .setConnectableBus(connectableBusId)
                .setEnsureIdUnicity(false)
                .add();
    }
}
