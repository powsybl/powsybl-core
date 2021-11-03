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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdapterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;

    private Network noEquipNetwork;

    @Before
    public void initNetwork() {
        mergingView = MergingView.create("DanglingLineAdapterTest", "iidm");
        noEquipNetwork = NoEquipmentNetworkFactory.create();
    }

    @Test
    public void baseTests() {
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
        assertEquals(100.0, danglingLine.getCurrentLimits().getPermanentLimit(), 0.0);

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
    public void mergedDanglingLine() {
        mergingView.merge(noEquipNetwork);
        double p10 = -606.2968572845882;
        double q10 = -305.09371456918353;
        final DanglingLine dl1 = createDanglingLine(mergingView, "vl1", "dl1", "dl1", 1.0, 1.0, 1.0, 1.0, p10, q10, "code", "busA");
        assertNotNull(mergingView.getDanglingLine("dl1"));
        assertEquals(1, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        double p20 = 598.4418282548444;
        double q20 = 296.88365650968643;
        final DanglingLine dl2 = createDanglingLine(mergingView, "vl2", "dl2", "dl2", 1.0, 1.0, 1.0, 1.0, p20, q20, "code", "busB");
        // Check no access to Dl1 & Dl2
        assertEquals(0, mergingView.getDanglingLineCount());
        assertNull(mergingView.getDanglingLine("dl1"));
        assertNull(mergingView.getDanglingLine("dl2"));
        // Check access to MergedLine
        assertEquals(1, mergingView.getLineCount());
        assertEquals(1, mergingView.getBranchCount());
        final Line line = mergingView.getLine("dl1 + dl2");
        assertSame(line, mergingView.getIdentifiable("dl1 + dl2"));
        assertSame(line, mergingView.getBranch("dl1 + dl2"));
        assertSame(line, mergingView.getIdentifiable("dl1"));
        assertSame(line, mergingView.getIdentifiable("dl2"));
        assertTrue(line instanceof MergedLine);
        assertTrue(mergingView.getIdentifiables().contains(line));

        // MergedLine
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals(IdentifiableType.LINE, mergedLine.getType());
        assertTrue(mergedLine.isTieLine());
        assertSame(mergingView, mergedLine.getNetwork());
        assertSame(dl1.getTerminal(), mergedLine.getTerminal(Branch.Side.ONE));
        assertSame(dl1.getTerminal(), mergedLine.getTerminal1());
        assertSame(dl2.getTerminal(), mergedLine.getTerminal(Branch.Side.TWO));
        assertSame(dl2.getTerminal(), mergedLine.getTerminal2());
        assertSame(dl1.getCurrentLimits(), mergedLine.getCurrentLimits(Branch.Side.ONE));
        assertSame(dl1.getCurrentLimits(), mergedLine.getCurrentLimits1());
        assertSame(dl2.getCurrentLimits(), mergedLine.getCurrentLimits(Branch.Side.TWO));
        assertSame(dl2.getCurrentLimits(), mergedLine.getCurrentLimits2());
        final CurrentLimits currentLimits1 = mergedLine.newCurrentLimits1()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400)
                .endTemporaryLimit()
                .add();
        final ActivePowerLimits activePowerLimits1 = mergedLine.newActivePowerLimits1()
                .setPermanentLimit(600)
                .add();
        final ApparentPowerLimits apparentPowerLimits1 = mergedLine.newApparentPowerLimits1()
                .setPermanentLimit(110.0)
                .add();
        final CurrentLimits currentLimits2 = mergedLine.newCurrentLimits2()
                .setPermanentLimit(50)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        final ActivePowerLimits activePowerLimits2 = mergedLine.newActivePowerLimits2()
                .setPermanentLimit(800)
                .add();
        final ApparentPowerLimits apparentPowerLimits2 = mergedLine.newApparentPowerLimits2()
                .setPermanentLimit(132.4)
                .add();
        assertSame(currentLimits1, mergedLine.getCurrentLimits1());
        assertSame(activePowerLimits1, mergedLine.getActivePowerLimits1());
        assertSame(apparentPowerLimits1, mergedLine.getApparentPowerLimits1());
        assertEquals(3, mergedLine.getOperationalLimits1().size());
        assertTrue(mergedLine.getOperationalLimits1().contains(currentLimits1));
        assertTrue(mergedLine.getOperationalLimits1().contains(activePowerLimits1));
        assertTrue(mergedLine.getOperationalLimits1().contains(apparentPowerLimits1));
        assertSame(currentLimits2, mergedLine.getCurrentLimits2());
        assertSame(activePowerLimits2, mergedLine.getActivePowerLimits2());
        assertSame(apparentPowerLimits2, mergedLine.getApparentPowerLimits2());
        assertEquals(3, mergedLine.getOperationalLimits2().size());
        assertTrue(mergedLine.getOperationalLimits2().contains(currentLimits2));
        assertTrue(mergedLine.getOperationalLimits2().contains(activePowerLimits2));
        assertTrue(mergedLine.getOperationalLimits2().contains(apparentPowerLimits2));
        assertSame(currentLimits1, mergedLine.getCurrentLimits(Branch.Side.ONE));
        assertSame(activePowerLimits1, mergedLine.getActivePowerLimits(Branch.Side.ONE));
        assertSame(apparentPowerLimits1, mergedLine.getApparentPowerLimits(Branch.Side.ONE));
        assertSame(currentLimits2, mergedLine.getCurrentLimits(Branch.Side.TWO));
        assertSame(activePowerLimits2, mergedLine.getActivePowerLimits(Branch.Side.TWO));
        assertSame(apparentPowerLimits2, mergedLine.getApparentPowerLimits(Branch.Side.TWO));
        assertEquals("dl1 + dl2", mergedLine.getId());
        assertEquals("dl1 + dl2", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl1 + dl2", mergedLine.getNameOrId());
        assertSame(mergedLine, mergedLine.setR(1.0d));
        assertEquals(dl1.getR() + dl2.getR(), mergedLine.getR(), 0.0d);
        assertSame(mergedLine, mergedLine.setX(2.0d));
        assertEquals(dl1.getX() + dl2.getX(), mergedLine.getX(), 0.0d);
        assertSame(mergedLine, mergedLine.setG1(0.0d));
        assertEquals(dl1.getG(), mergedLine.getG1(), 0.0d);
        assertSame(mergedLine, mergedLine.setG2(0.0d));
        assertEquals(dl2.getG(), mergedLine.getG2(), 0.0d);
        assertSame(mergedLine, mergedLine.setB1(0.0d));
        assertEquals(dl1.getB(), mergedLine.getB1(), 0.0d);
        assertSame(mergedLine, mergedLine.setB2(0.0d));
        assertEquals(dl2.getB(), mergedLine.getB2(), 0.0d);
        assertEquals(p10, dl1.getP0(), 0.0d);
        assertEquals(q10, dl1.getQ0(), 0.0d);
        assertEquals(p20, dl2.getP0(), 0.0d);
        assertEquals(q20, dl2.getQ0(), 0.0d);

        assertFalse(mergedLine.isOverloaded());
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
        assertNull(mergedLine.checkTemporaryLimits2());
        mergedLine.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        double p1 = -605.0;
        double q1 = -302.5;
        double p2 = 600.0;
        double q2 = 300.0;
        final Terminal t1 = mergedLine.getTerminal("vl1");
        assertNotNull(t1);
        assertEquals(Branch.Side.ONE, mergedLine.getSide(t1));
        final Terminal t2 = mergedLine.getTerminal("vl2");
        assertNotNull(t2);
        assertEquals(Branch.Side.TWO, mergedLine.getSide(t2));

        // Boundary
        assertEquals(Branch.Side.ONE, dl1.getBoundary().getSide());
        assertSame(mergedLine, dl1.getBoundary().getConnectable());
        assertSame(mergedLine.getTerminal1().getVoltageLevel(), dl1.getBoundary().getVoltageLevel());
        assertEquals(Branch.Side.TWO, dl2.getBoundary().getSide());
        assertSame(mergedLine, dl2.getBoundary().getConnectable());
        assertSame(mergedLine.getTerminal2().getVoltageLevel(), dl2.getBoundary().getVoltageLevel());

        // Update P & Q
        t1.setP(p1);
        t1.setQ(q1);
        t2.setP(p2);
        t2.setQ(q2);
        // Update V & Angle
        double v1 = 420.0;
        double v2 = 380.0;
        double angle1 = -1e-4;
        double angle2 = -1.7e-3;
        t1.getBusView().getBus().setV(v1).setAngle(angle1);
        t2.getBusView().getBus().setV(v2).setAngle(angle2);

        // Check P & Q are computed by Listener
        SV expectedSV1 = new SV(p1, q1, v1, angle1, Branch.Side.ONE).otherSide(dl1, true);
        SV expectedSV2 = new SV(p2, q2, v2, angle2, Branch.Side.ONE).otherSide(dl2, true);
        assertEquals(expectedSV1.getP(), dl1.getBoundary().getP(), 0.0d);
        assertEquals(expectedSV1.getP(), mergedLine.getHalf1().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV1.getQ(), dl1.getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV1.getQ(), mergedLine.getHalf1().getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV2.getP(), dl2.getBoundary().getP(), 0.0d);
        assertEquals(expectedSV2.getP(), mergedLine.getHalf2().getBoundary().getP(), 0.0d);
        assertEquals(expectedSV2.getQ(), dl2.getBoundary().getQ(), 0.0d);
        assertEquals(expectedSV2.getQ(), mergedLine.getHalf2().getBoundary().getQ(), 0.0d);
        // Check V & Angle are computed by Listener
        assertEquals(expectedSV1.getU(), mergedLine.getHalf1().getBoundary().getV(), 1.0e-8);
        assertEquals(expectedSV1.getA(), dl1.getBoundary().getAngle(), 1.0e-8);
        assertEquals(expectedSV1.getA(), mergedLine.getHalf1().getBoundary().getAngle(), 1.0e-8);
        assertEquals(expectedSV1.getU(), dl1.getBoundary().getV(), 1.0e-8);
        assertEquals(expectedSV2.getU(), dl2.getBoundary().getV(), 1.0e-8);
        assertEquals(expectedSV2.getU(), mergedLine.getHalf2().getBoundary().getV(), 1.0e-8);
        assertEquals(expectedSV2.getA(), dl2.getBoundary().getAngle(), 1.0e-8);
        assertEquals(expectedSV2.getA(), mergedLine.getHalf2().getBoundary().getAngle(), 1.0e-8);

        mergedLine.setFictitious(true);
        assertTrue(mergedLine.isFictitious());

        // Exceptions when creating dangling lines with mergedline ids

        try {
            createDanglingLine(mergingView, "vl1", "dl1", "dl1", 1.0, 1.0, 1.0, 1.0, p10, q10, "code", "busA");
            fail();
        } catch (PowsyblException e) {
            assertEquals("The network already contains an object 'MergedLine' with the id 'dl1'", e.getMessage());
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

        // Exception(s)
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("No terminal connected to voltage level invalid");
        mergedLine.getTerminal("invalid");
    }

    @Test
    public void testProperties() {
        final DanglingLine dl1 = createDanglingLine(noEquipNetwork, "vl1", "dl1", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busA");
        dl1.setProperty("ucteCode", dl1.getUcteXnodeCode()); // test equals property
        dl1.setProperty("id", dl1.getId()); // test not equals property
        dl1.setProperty("network", "noEquipNetwork"); // test empty property
        dl1.setProperty("vl", ""); // test empty property

        final DanglingLine dl2 = createDanglingLine(noEquipNetwork, "vl2", "dl2", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busB");
        dl2.setProperty("ucteCode", dl2.getUcteXnodeCode()); // test equals property
        dl2.setProperty("id", dl2.getId()); // test not equals property
        dl2.setProperty("network", ""); // test empty property
        dl2.setProperty("vl", "vl2"); // test empty property

        mergingView.merge(noEquipNetwork);
        final Line line = mergingView.getLine("dl1 + dl2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("dl", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl", mergedLine.getNameOrId());

        assertTrue(mergedLine.hasProperty());
        assertTrue(mergedLine.hasProperty("ucteCode"));
        assertEquals(3, mergedLine.getPropertyNames().size());
        mergedLine.setProperty("key", "value");
        assertEquals("value", mergedLine.getProperty("key"));
        assertEquals("defaultValue", mergedLine.getProperty("noKey", "defaultValue"));
    }

    @Test
    public void testListener() {
        mergingView.merge(noEquipNetwork);
        createDanglingLine(mergingView, "vl1", "testListener1", "testListener2", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "busA");
        assertNotNull(mergingView.getDanglingLine("testListener1"));
        assertEquals(1, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        createDanglingLine(noEquipNetwork, "vl2", "testListener2", "testListener1", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "busB");
        assertNull(mergingView.getDanglingLine("testListener1"));
        assertNull(mergingView.getDanglingLine("testListener2"));
        assertEquals(0, mergingView.getDanglingLineCount());
        assertEquals(1, mergingView.getLineCount());
        final Line line = mergingView.getLine("testListener1 + testListener2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("testListener1 + testListener2", mergedLine.getOptionalName().orElse(null));
        assertEquals("testListener1 + testListener2", mergedLine.getNameOrId());

    }

    @Test
    public void mergedDanglingLineWithSameId() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createDanglingLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "NGEN");
        createDanglingLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "busA");
        mergingView.merge(network, noEquipNetwork);
        assertNull(mergingView.getDanglingLine("dl"));
        Line merged = mergingView.getLine("dl");
        assertNotNull(merged);
    }

    @Test
    public void failDanglingLinesWithSameIdAndNullXnodeCode() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createDanglingLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "NGEN");
        createDanglingLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, null, "busA");
        try {
            mergingView.merge(network, noEquipNetwork);
            fail();
        } catch (PowsyblException e) {
            assertEquals("The object 'dl' already exists into merging view", e.getMessage());
        }
    }

    @Test
    public void failDanglingLinesWithSameIdAndDifferentXnodeCode() {
        double p0 = 1.0;
        double q0 = 1.0;
        Network network = EurostagTutorialExample1Factory.create();
        createDanglingLine(network, "VLGEN", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "NGEN");
        createDanglingLine(noEquipNetwork, "vl1", "dl", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code2", "busA");
        try {
            mergingView.merge(network, noEquipNetwork);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Dangling line couple dl have inconsistent Xnodes (code!=code2)", e.getMessage());
        }
    }

    private static DanglingLine createDanglingLine(Network n, String vlId, String id, String name, double r, double x, double g, double b,
                                                   double p0, double q0, String ucteCode, String busId) {

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
                                                     .setConnectableBus(busId)
                                                     .setEnsureIdUnicity(false)
                                                 .add();
    }
}
