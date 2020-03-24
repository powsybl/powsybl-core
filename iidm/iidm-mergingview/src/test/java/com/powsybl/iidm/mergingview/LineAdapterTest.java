/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LineAdapterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;

    private Network networkRef;

    @Before
    public void setUp() {
        mergingView = MergingView.create("LineAdapterTest", "iidm");
        networkRef = BatteryNetworkFactory.create();
        mergingView.merge(networkRef);
    }

    @Test
    public void testSetterGetter() {
        final Line lineRef = networkRef.getLine("NHV1_NHV2_1");
        final Line lineAdapted = mergingView.getLine(lineRef.getId());
        // setter / getter
        assertTrue(lineAdapted instanceof LineAdapter);
        assertSame(lineAdapted, mergingView.getBranch(lineRef.getId()));
        assertEquals(lineRef.getId(), lineAdapted.getId());
        assertEquals(lineRef.getName(), lineAdapted.getName());
        assertEquals(lineRef.isTieLine(), lineAdapted.isTieLine());
        assertTrue(lineAdapted.getTerminal1() instanceof TerminalAdapter);
        assertTrue(lineAdapted.getTerminal2() instanceof TerminalAdapter);
        assertSame(lineAdapted.getTerminal1(), lineAdapted.getTerminal(Branch.Side.ONE));
        assertSame(lineAdapted.getTerminal1(), lineAdapted.getTerminal("VLGEN"));
        assertEquals(lineRef.getSide(lineRef.getTerminal1()), lineAdapted.getSide(lineAdapted.getTerminal1()));

        double r = lineAdapted.getR();
        double x = lineAdapted.getX();
        double g1 = lineAdapted.getG1();
        double g2 = lineAdapted.getG2();
        double b1 = lineAdapted.getB1();
        double b2 = lineAdapted.getB2();
        assertEquals(r, lineAdapted.getR(), 0.0);
        lineAdapted.setR(++r);
        assertEquals(r, lineAdapted.getR(), 0.0);
        assertEquals(x, lineAdapted.getX(), 0.0);
        lineAdapted.setX(++x);
        assertEquals(x, lineAdapted.getX(), 0.0);
        assertEquals(g1, lineAdapted.getG1(), 0.0);
        lineAdapted.setG1(++g1);
        assertEquals(g1, lineAdapted.getG1(), 0.0);
        assertEquals(g2, lineAdapted.getG2(), 0.0);
        lineAdapted.setG2(++g2);
        assertEquals(g2, lineAdapted.getG2(), 0.0);
        assertEquals(b1, lineAdapted.getB1(), 0.0);
        lineAdapted.setB1(++b1);
        assertEquals(b1, lineAdapted.getB1(), 0.0);
        assertEquals(b2, lineAdapted.getB2(), 0.0);
        lineAdapted.setB2(++b2);
        assertEquals(b2, lineAdapted.getB2(), 0.0);
        assertSame(lineRef.getCurrentLimits1(), lineAdapted.getCurrentLimits1());
        assertSame(lineRef.getCurrentLimits2(), lineAdapted.getCurrentLimits2());
        assertSame(lineRef.getCurrentLimits(Branch.Side.ONE), lineAdapted.getCurrentLimits(Branch.Side.ONE));
        assertSame(lineRef.getCurrentLimits(Branch.Side.TWO), lineAdapted.getCurrentLimits(Branch.Side.TWO));

        assertEquals(lineRef.isOverloaded(), lineAdapted.isOverloaded());
        assertEquals(lineRef.isOverloaded(0.0f), lineAdapted.isOverloaded(0.0f));
        assertEquals(lineRef.getOverloadDuration(), lineAdapted.getOverloadDuration());
        assertEquals(lineRef.checkPermanentLimit(Branch.Side.ONE, 0.0f), lineAdapted.checkPermanentLimit(Branch.Side.ONE, 0.0f));
        assertEquals(lineRef.checkPermanentLimit(Branch.Side.TWO), lineAdapted.checkPermanentLimit(Branch.Side.TWO));
        assertEquals(lineRef.checkPermanentLimit1(0.0f), lineAdapted.checkPermanentLimit1(0.0f));
        assertEquals(lineRef.checkPermanentLimit1(), lineAdapted.checkPermanentLimit1());
        assertEquals(lineRef.checkPermanentLimit2(0.0f), lineAdapted.checkPermanentLimit2(0.0f));
        assertEquals(lineRef.checkPermanentLimit2(), lineAdapted.checkPermanentLimit2());
        assertEquals(lineRef.checkTemporaryLimits(Branch.Side.ONE, 0.0f), lineAdapted.checkTemporaryLimits(Branch.Side.ONE, 0.0f));
        assertEquals(lineRef.checkTemporaryLimits(Branch.Side.TWO), lineAdapted.checkTemporaryLimits(Branch.Side.TWO));
        assertEquals(lineRef.checkTemporaryLimits1(0.0f), lineAdapted.checkTemporaryLimits1(0.0f));
        assertEquals(lineRef.checkTemporaryLimits1(), lineAdapted.checkTemporaryLimits1());
        assertEquals(lineRef.checkTemporaryLimits2(0.0f), lineAdapted.checkTemporaryLimits2(0.0f));
        assertEquals(lineRef.checkTemporaryLimits2(), lineAdapted.checkTemporaryLimits2());
        assertEquals(lineRef.getType(), lineAdapted.getType());
        assertEquals(lineRef.getTerminals().size(), lineAdapted.getTerminals().size());

        // Topology
        TopologyVisitor visitor = mock(TopologyVisitor.class);
        mergingView.getVoltageLevel("VLGEN").visitEquipments(visitor);
        verify(visitor, times(2)).visitLine(any(Line.class), any(Branch.Side.class));

        // Not implemented yet !
        TestUtil.notImplemented(lineAdapted::remove);
    }

    @Test
    public void adderFromSameNetworkTests() {
        // adder line with both voltage level in same network
        final Line okLine = createLine(mergingView,
                                       "okLine",
                                       "okLineName",
                                       "VLGEN",
                                       "NGEN",
                                       "NGEN");
        assertNotNull(okLine);
        assertEquals(okLine, mergingView.getLine("okLine"));
    }

    @Test
    public void adderFromBothNetworkTests() {
        final Network noEquipNetwork = NoEquipmentNetworkFactory.create();
        mergingView.merge(noEquipNetwork);
        // adder line with both voltage level in different network
        final Line lineOnBothNetwork = createLine(mergingView,
                                                  "lineOnBothNetworkId",
                                                  "lineOnBothNetworkName",
                                                  "vl1",
                                                  "busA",
                                                  "busA");
        assertNotNull(lineOnBothNetwork);
        assertEquals(lineOnBothNetwork, mergingView.getLine("lineOnBothNetworkId"));

        // Exception(s)
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The network already contains an object with the id 'lineOnBothNetworkId'");
        mergingView.newLine()
                       .setId("lineOnBothNetworkId")
                       .setVoltageLevel1("vl1")
                       .setVoltageLevel2("VLBAT")
                   .add();
    }

    @Test
    public void checkP0AndQ0UpdateTests() {
        final Network noEquipNetwork = NoEquipmentNetworkFactory.create();
        mergingView.merge(noEquipNetwork);
        // adder line with both voltage level in different network
        final Line lineOnBothNetwork = createLine(mergingView,
                "lineOnBothNetworkId",
                "lineOnBothNetworkName",
                "vl1",
                "busA",
                "busA");
        assertTrue(lineOnBothNetwork instanceof MergedLine);

        // Get both DanglingLine
        final DanglingLine dl1 = noEquipNetwork.getDanglingLine("lineOnBothNetworkId_1");
        final DanglingLine dl2 = networkRef.getDanglingLine("lineOnBothNetworkId_2");
        assertNotNull(dl1);
        assertNotNull(dl2);
        // Check initial P & Q
        assertEquals(Double.NaN, dl1.getTerminal().getP(), 0.0);
        assertEquals(Double.NaN, dl1.getTerminal().getQ(), 0.0);
        assertEquals(Double.NaN, dl2.getTerminal().getP(), 0.0);
        assertEquals(Double.NaN, dl2.getTerminal().getQ(), 0.0);
        double p1 = -605.0;
        double q1 = -302.5;
        double p2 = 600.0;
        double q2 = 300.0;
        double lossesP = p1 + p2;
        double lossesQ = q1 + q2;
        // Update P & Q
        dl1.getTerminal().setP(p1);
        dl1.getTerminal().setQ(q1);
        dl2.getTerminal().setP(p2);
        dl2.getTerminal().setQ(q2);
        // Check P & Q are updated
        assertEquals(p1 + (lossesP / 2.0), dl1.getP0(), 0.0d);
        assertEquals(q1 + (lossesQ / 2.0), dl1.getQ0(), 0.0d);
        assertEquals((p2 + (lossesP / 2.0)) * -1, dl2.getP0(), 0.0d);
        assertEquals((q2 + (lossesQ / 2.0)) * -1, dl2.getQ0(), 0.0d);
    }

    @Test
    public void addLineWithoutIdTests() {
        mergingView.merge(NoEquipmentNetworkFactory.create());

        // Exception(s)
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Line id is not set");
        mergingView.newLine()
                       .setVoltageLevel1("vl1")
                       .setVoltageLevel2("VLBAT")
                   .add();
    }

    private static Line createLine(Network network, String id, String name,
                                   String voltageLevelId1, String busId1, String connectableBusId1) {
        return network.newLine()
                          .setId(id)
                          .setName(name)
                          .setFictitious(false)
                          .setEnsureIdUnicity(true)
                          .setR(1.0)
                          .setX(2.0)
                          .setG1(3.0)
                          .setG2(3.5)
                          .setB1(4.0)
                          .setB2(4.5)
                          .setVoltageLevel1(voltageLevelId1)
                          .setVoltageLevel2("VLBAT")
                          .setBus1(busId1)
                          .setBus2("NBAT")
                          .setConnectableBus1(connectableBusId1)
                          .setConnectableBus2("NBAT")
                      .add();
    }
}
