/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

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

        // Not implemented yet !
        TestUtil.notImplemented(lineAdapted::remove);
    }

    @Test
    public void adderFromSameNetworkTests() {
        // adder line with both voltage level in same network
        final Line okLine = mergingView.newLine()
                                           .setId("okLine")
                                           .setName("okLineName")
                                           .setFictitious(true)
                                           .setEnsureIdUnicity(true)
                                           .setR(1.0)
                                           .setX(2.0)
                                           .setG1(3.0)
                                           .setG2(3.5)
                                           .setB1(4.0)
                                           .setB2(4.5)
                                           .setVoltageLevel1("VLGEN")
                                           .setVoltageLevel2("VLBAT")
                                           .setBus1("NGEN")
                                           .setBus2("NBAT")
                                           .setConnectableBus1("NGEN")
                                           .setConnectableBus2("NBAT")
                                       .add();

        assertEquals(okLine, mergingView.getLine("okLine"));
    }

    @Test
    public void adderFromBothNetworkTests() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Not implemented exception");

        mergingView.merge(NoEquipmentNetworkFactory.create());
        // adder line with both voltage level in different network
        final Line lineOnBothNetwork = mergingView.newLine()
                                                      .setId("lineOnBothNetworkId")
                                                      .setName("lineOnBothNetworkName")
                                                      .setEnsureIdUnicity(true)
                                                      .setR(1.0)
                                                      .setX(2.0)
                                                      .setG1(3.0)
                                                      .setG2(3.5)
                                                      .setB1(4.0)
                                                      .setB2(4.5)
                                                      .setVoltageLevel1("vl1")
                                                      .setVoltageLevel2("VLBAT")
                                                      .setBus1("busA")
                                                      .setBus2("NBAT")
                                                      .setConnectableBus1("busA")
                                                      .setConnectableBus2("NBAT")
                                                  .add();
    }
}
