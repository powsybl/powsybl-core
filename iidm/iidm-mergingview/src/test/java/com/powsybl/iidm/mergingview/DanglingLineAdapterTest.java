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
class DanglingLineAdapterTest {

    private MergingView mergingView;

    private Network noEquipNetwork;
    private Network eurostagNetwork;

    @BeforeEach
    void initNetwork() {
        mergingView = MergingView.create("DanglingLineAdapterTest", "iidm");
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
        DanglingLine danglingLine = createDanglingLine(mergingView, voltageLevelId, id, name, r, x, g, b, p0, q0, ucteXnodeCode, busId);
        assertNotNull(mergingView.getDanglingLine(id));
        assertTrue(danglingLine instanceof DanglingLineAdapter);
        assertSame(mergingView, danglingLine.getNetwork());

        assertEquals(IdentifiableType.DANGLING_LINE, danglingLine.getType());
        assertEquals(r, danglingLine.getR(), 0.0);
        assertEquals(x, danglingLine.getX(), 0.0);
        assertEquals(g, danglingLine.getG(), 0.0);
        assertEquals(b, danglingLine.getB(), 0.0);
        assertEquals(p0, danglingLine.getP0(), 0.0);
        assertEquals(q0, danglingLine.getQ0(), 0.0);
        assertEquals(id, danglingLine.getId());
        assertEquals(name, danglingLine.getOptionalName().orElse(null));
        assertEquals(name, danglingLine.getNameOrId());
        assertEquals(ucteXnodeCode, danglingLine.getUcteXnodeCode());

        // setter getter
        double r2 = 11.0;
        double x2 = 21.0;
        double g2 = 31.0;
        double b2 = 41.0;
        double p02 = 51.0;
        double q02 = 61.0;
        danglingLine.setR(r2);
        assertEquals(r2, danglingLine.getR(), 0.0);
        danglingLine.setX(x2);
        assertEquals(x2, danglingLine.getX(), 0.0);
        danglingLine.setG(g2);
        assertEquals(g2, danglingLine.getG(), 0.0);
        danglingLine.setB(b2);
        assertEquals(b2, danglingLine.getB(), 0.0);
        danglingLine.setP0(p02);
        assertEquals(p02, danglingLine.getP0(), 0.0);
        danglingLine.setQ0(q02);
        assertEquals(q02, danglingLine.getQ0(), 0.0);

        danglingLine.newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();
        assertEquals(100.0, danglingLine.getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertTrue(danglingLine.getTerminal() instanceof TerminalAdapter);
        danglingLine.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });
        assertEquals(1, danglingLine.getTerminals().size());

        try {
            createDanglingLine(mergingView, voltageLevelId, id, name, r, x, g, b, p0, q0, ucteXnodeCode, busId);
            fail();
        } catch (PowsyblException e) {
            assertEquals("The network already contains an object 'DanglingLineAdapter' with the id 'danglingId'", e.getMessage());
        }
    }

    @Test
    void mergedDanglingLine() {
        mergingView.merge(noEquipNetwork);
        double p10 = 0.11710908004064359;
        double q10 = -0.012883304869602126;
        final DanglingLine dl1 = createDanglingLine(mergingView, "vl1", "dl1", "dl1", 0.01138, 0.05017, 0.0, 0.06280, p10, q10, "code", "busA");
        dl1.setProperty("keyTest", "test");
        assertNotNull(mergingView.getDanglingLine("dl1"));
        assertEquals(1, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        double p20 = -0.11713527;
        double q20 = 0.01301712;
        final DanglingLine dl2 = createDanglingLine(eurostagNetwork, "VLHV1", "dl2", "dl2", 0.01038, 0.04917, 0.0, 0.07280, p20, q20, "code", "NHV1");
        mergingView.merge(eurostagNetwork);
        // Check no access to Dl1 & Dl2
        assertEquals(2, mergingView.getDanglingLineCount());
        assertNotNull(mergingView.getDanglingLine("dl1"));
        assertNotNull(mergingView.getDanglingLine("dl2"));
        // Check access to MergedLine
        assertEquals(2, mergingView.getLineCount());
        assertEquals(4, mergingView.getBranchCount());
        final TieLine line = mergingView.getTieLine("dl1 + dl2");
        assertEquals(1, mergingView.getTieLineCount());
        assertSame(line, mergingView.getIdentifiable("dl1 + dl2"));
        assertSame(line.getHalf1(), mergingView.getIdentifiable("dl1"));
        assertSame(line.getHalf2(), mergingView.getIdentifiable("dl2"));
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
        assertSame(dl1.getTerminal(), mergedLine.getHalf(Branch.Side.ONE).getTerminal());
        assertSame(dl1.getTerminal(), mergedLine.getHalf1().getTerminal());
        final CurrentLimits currentLimits1 = mergedLine.getHalf1().newCurrentLimits()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400)
                .endTemporaryLimit()
                .add();
        final ActivePowerLimits activePowerLimits1 = mergedLine.getHalf1().newActivePowerLimits()
                .setPermanentLimit(600)
                .add();
        final ApparentPowerLimits apparentPowerLimits1 = mergedLine.getHalf1().newApparentPowerLimits()
                .setPermanentLimit(110.0)
                .add();
        final CurrentLimits currentLimits2 = mergedLine.getHalf2().newCurrentLimits()
                .setPermanentLimit(50)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        final ActivePowerLimits activePowerLimits2 = mergedLine.getHalf2().newActivePowerLimits()
                .setPermanentLimit(800)
                .add();
        final ApparentPowerLimits apparentPowerLimits2 = mergedLine.getHalf2().newApparentPowerLimits()
                .setPermanentLimit(132.4)
                .add();

        assertSame(currentLimits1, mergedLine.getHalf1().getCurrentLimits().orElse(null));
        assertSame(activePowerLimits1, mergedLine.getHalf1().getActivePowerLimits().orElse(null));
        assertSame(apparentPowerLimits1, mergedLine.getHalf1().getApparentPowerLimits().orElse(null));
        assertEquals(3, mergedLine.getHalf1().getOperationalLimits().size());
        assertTrue(mergedLine.getHalf1().getOperationalLimits().contains(currentLimits1));
        assertTrue(mergedLine.getHalf1().getOperationalLimits().contains(activePowerLimits1));
        assertTrue(mergedLine.getHalf1().getOperationalLimits().contains(apparentPowerLimits1));
        assertSame(currentLimits2, mergedLine.getHalf2().getCurrentLimits().orElse(null));
        assertSame(activePowerLimits2, mergedLine.getHalf2().getActivePowerLimits().orElse(null));
        assertSame(apparentPowerLimits2, mergedLine.getHalf2().getApparentPowerLimits().orElse(null));
        assertEquals(3, mergedLine.getHalf2().getOperationalLimits().size());
        assertTrue(mergedLine.getHalf2().getOperationalLimits().contains(currentLimits2));
        assertTrue(mergedLine.getHalf2().getOperationalLimits().contains(activePowerLimits2));
        assertTrue(mergedLine.getHalf2().getOperationalLimits().contains(apparentPowerLimits2));
        assertSame(currentLimits1, mergedLine.getHalf(Branch.Side.ONE).getCurrentLimits().orElse(null));
        assertSame(activePowerLimits1, mergedLine.getHalf(Branch.Side.ONE).getActivePowerLimits().orElse(null));
        assertSame(apparentPowerLimits2, mergedLine.getHalf(Branch.Side.TWO).getApparentPowerLimits().orElse(null));

        assertEquals("dl1 + dl2", mergedLine.getId());
        assertEquals("dl1 + dl2", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl1 + dl2", mergedLine.getNameOrId());
        assertEquals(0.02176, mergedLine.getR(), 1.0e-10);
        assertEquals(0.09934, mergedLine.getX(), 1.0e-10);
        assertEquals(0.0, mergedLine.getG1(), 1.0e-10);
        assertEquals(0.0, mergedLine.getG2(), 1.0e-10);
        assertEquals(0.06280, mergedLine.getB1(), 1.0e-10);
        assertEquals(0.07280, mergedLine.getB2(), 1.0e-10);
        assertEquals(p10, dl1.getP0(), 0.0d);
        assertEquals(q10, dl1.getQ0(), 0.0d);
        assertEquals(p20, dl2.getP0(), 0.0d);
        assertEquals(q20, dl2.getQ0(), 0.0d);

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
        mergedLine.getHalf1().getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });
        mergedLine.getHalf2().getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        double p1 = 0.117273045626974;
        double q1 = -0.081804351176928;
        double p2 = -0.11700000;
        double q2 = -0.06700000;
        final DanglingLine dl1Bis = mergedLine.getHalf("vl1");
        assertNotNull(dl1Bis);
        final Terminal t1 = dl1Bis.getTerminal();
        assertNotNull(t1);
        assertSame(mergedLine.getHalf(Branch.Side.ONE), dl1Bis);
        final DanglingLine dl2Bis = mergedLine.getHalf("VLHV1");
        assertNotNull(dl2Bis);
        final Terminal t2 = dl2Bis.getTerminal();
        assertNotNull(t2);
        assertSame(mergedLine.getHalf(Branch.Side.TWO), dl2Bis);

        // Boundary
        assertSame(dl1, dl1.getBoundary().getConnectable());
        assertSame(mergedLine.getHalf1().getTerminal().getVoltageLevel(), dl1.getBoundary().getNetworkSideVoltageLevel());
        assertSame(mergedLine.getHalf2().getTerminal().getVoltageLevel(), mergingView.getVoltageLevel(dl2.getBoundary().getNetworkSideVoltageLevel().getId()));

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

        SV expectedSVdl1 = new SV(p1, q1, v1, angle1, Branch.Side.ONE).otherSide(dl1, true);
        SV expectedSVdl2 = new SV(p2, q2, v2, angle2, Branch.Side.ONE).otherSide(dl2, true);

        assertEquals(expectedSVdl1.getP(), dl1.getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVdl1.getP(), mergedLine.getHalf1().getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVdl1.getQ(), dl1.getBoundary().getQ(), 1.0e-8);
        assertEquals(expectedSVdl1.getQ(), mergedLine.getHalf1().getBoundary().getQ(), 1.0e-8);
        assertEquals(expectedSVdl2.getP(), dl2.getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVdl2.getP(), mergedLine.getHalf2().getBoundary().getP(), 1.0e-8);
        assertEquals(expectedSVdl2.getQ(), dl2.getBoundary().getQ(), 1.0e-8);
        assertEquals(expectedSVdl2.getQ(), mergedLine.getHalf2().getBoundary().getQ(), 1.0e-8);
        // Check V & Angle are computed by Listener
        assertEquals(expectedSVdl1.getU(), mergedLine.getHalf1().getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVdl1.getA(), dl1.getBoundary().getAngle(), 1.0e-7);
        assertEquals(expectedSVdl1.getA(), mergedLine.getHalf1().getBoundary().getAngle(), 1.0e-7);
        assertEquals(expectedSVdl1.getU(), dl1.getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVdl2.getU(), dl2.getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVdl2.getU(), mergedLine.getHalf2().getBoundary().getV(), 1.0e-7);
        assertEquals(expectedSVdl2.getA(), dl2.getBoundary().getAngle(), 1.0e-7);
        assertEquals(expectedSVdl2.getA(), mergedLine.getHalf2().getBoundary().getAngle(), 1.0e-7);

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

        // Exceptions when creating dangling lines with mergedline ids

        try {
            createDanglingLine(mergingView, "vl1", "dl1", "dl1", 1.0, 1.0, 1.0, 1.0, p10, q10, "code", "busA");
            fail();
        } catch (PowsyblException e) {
            assertEquals("The network already contains an object 'DanglingLineAdapter' with the id 'dl1'", e.getMessage());
        }

        try {
            createDanglingLine(mergingView, "vl1", "dl1 + dl2", "dl1 + dl2", 1.0, 1.0, 1.0, 1.0, p10, q10, "code", "busA");
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
        final DanglingLine dl1 = createDanglingLine(noEquipNetwork, "vl1", "dl1", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busA");
        dl1.setProperty("ucteCode", dl1.getUcteXnodeCode()); // test equals property
        dl1.setProperty("id", dl1.getId()); // test not equals property
        dl1.setProperty("network", "noEquipNetwork"); // test empty property
        dl1.setProperty("vl", ""); // test empty property

        final DanglingLine dl2 = createDanglingLine(eurostagNetwork, "VLHV1", "dl2", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "NHV1");
        dl2.setProperty("ucteCode", dl2.getUcteXnodeCode()); // test equals property
        dl2.setProperty("id", dl2.getId()); // test not equals property
        dl2.setProperty("network", ""); // test empty property
        dl2.setProperty("vl", "vl2"); // test empty property

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
        createDanglingLine(mergingView, "VL", "testListener1", "testListener2", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "B");
        assertNotNull(mergingView.getDanglingLine("testListener1"));
        assertEquals(1, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        createDanglingLine(noEquipNetwork, "vl2", "testListener2", "testListener1", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "busB");
        assertNotNull(mergingView.getDanglingLine("testListener1"));
        assertNotNull(mergingView.getDanglingLine("testListener2"));
        assertEquals(2, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        assertEquals(1, mergingView.getTieLineCount());
        final TieLine line = mergingView.getTieLine("testListener1 + testListener2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("testListener2 + testListener1", mergedLine.getOptionalName().orElse(null));
        assertEquals("testListener2 + testListener1", mergedLine.getNameOrId());
    }

    @Test
    void mergedDanglingLineWithSameId() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createDanglingLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "NGEN");
        createDanglingLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "busA");
        mergingView.merge(network, noEquipNetwork);
        assertNotNull(mergingView.getDanglingLine("dl"));
        TieLine merged = mergingView.getTieLine("dl"); // FIXME two identifiables with same ID in merging view (fringe case)
        assertNotNull(merged);
    }

    @Test
    void failDanglingLinesWithSameIdAndNullXnodeCode() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createDanglingLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "NGEN");
        createDanglingLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "busA");
        try {
            mergingView.merge(network, noEquipNetwork);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Dangling line couple dl have inconsistent Xnodes (null,null)", e.getMessage());
        }
    }

    @Test
    void failDanglingLinesWithSameIdAndDifferentXnodeCode() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createDanglingLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "NGEN");
        createDanglingLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code2", "busA");
        try {
            mergingView.merge(network, noEquipNetwork);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Dangling line couple dl have inconsistent Xnodes (code,code2)", e.getMessage());
        }
    }

    @Test
    void multipleDanglingLines() {
        createDanglingLine(noEquipNetwork, "vl1", "dl1", "dl1_name", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busA");
        createDanglingLine(eurostagNetwork, "VLHV1", "dl2", "dl2_name", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "NHV1");
        createDanglingLine(eurostagNetwork, "VLHV1", "dl3", "dl3_name", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", null, "NHV1");
        mergingView.merge(noEquipNetwork, eurostagNetwork);
        assertNotNull(mergingView.getTieLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", mergingView.getTieLine("dl1 + dl2").getOptionalName().orElse(null));
        assertEquals("dl1_name + dl2_name", mergingView.getTieLine("dl1 + dl2").getNameOrId());
    }

    private static DanglingLine createDanglingLine(Network n, String vlId, String id, String name, double r, double x, double g, double b,
                                                   double p0, double q0, String ucteCode, String busId) {

        return createDanglingLine(n, vlId, id, name, r, x, g, b, p0, q0, ucteCode, busId, busId);
    }

    private static DanglingLine createDanglingLine(Network n, String vlId, String id, String name, double r, double x, double g, double b,
                                                   double p0, double q0, String ucteCode, String busId, String connectableBusId) {

        return n.getVoltageLevel(vlId).newDanglingLine()
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
