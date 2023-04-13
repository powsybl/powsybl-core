/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class LineAdapterTest {

    private MergingView mergingView;

    private Network networkRef;

    @BeforeEach
    void setUp() {
        mergingView = MergingView.create("LineAdapterTest", "iidm");
        networkRef = BatteryNetworkFactory.create();
        mergingView.merge(networkRef);
    }

    @Test
    void testSetterGetter() {
        final Line lineRef = networkRef.getLine("NHV1_NHV2_1");
        final Line lineAdapted = mergingView.getLine(lineRef.getId());
        // setter / getter
        assertTrue(lineAdapted instanceof LineAdapter);
        assertSame(lineAdapted, mergingView.getBranch(lineRef.getId()));
        assertEquals(lineRef.getId(), lineAdapted.getId());
        assertEquals(lineRef.getOptionalName().orElse(null), lineAdapted.getOptionalName().orElse(null));
        assertEquals(lineRef.getNameOrId(), lineAdapted.getNameOrId());
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

        assertSame(lineRef.getCurrentLimits1().orElse(null), lineAdapted.getCurrentLimits1().orElse(null));
        assertSame(lineRef.getActivePowerLimits1().orElse(null), lineAdapted.getActivePowerLimits1().orElse(null));
        assertSame(lineRef.getApparentPowerLimits1().orElse(null), lineAdapted.getApparentPowerLimits1().orElse(null));
        assertSame(lineRef.getCurrentLimits2().orElse(null), lineAdapted.getCurrentLimits2().orElse(null));
        assertSame(lineRef.getActivePowerLimits2().orElse(null), lineAdapted.getActivePowerLimits2().orElse(null));
        assertSame(lineRef.getApparentPowerLimits2().orElse(null), lineAdapted.getApparentPowerLimits2().orElse(null));
        assertSame(lineRef.getCurrentLimits(Branch.Side.ONE).orElse(null), lineAdapted.getCurrentLimits(Branch.Side.ONE).orElse(null));
        assertSame(lineRef.getActivePowerLimits(Branch.Side.ONE).orElse(null), lineAdapted.getActivePowerLimits(Branch.Side.ONE).orElse(null));
        assertSame(lineRef.getApparentPowerLimits(Branch.Side.ONE).orElse(null), lineAdapted.getApparentPowerLimits(Branch.Side.ONE).orElse(null));
        assertSame(lineRef.getCurrentLimits(Branch.Side.TWO).orElse(null), lineAdapted.getCurrentLimits(Branch.Side.TWO).orElse(null));
        assertSame(lineRef.getActivePowerLimits(Branch.Side.TWO).orElse(null), lineAdapted.getActivePowerLimits(Branch.Side.TWO).orElse(null));
        assertSame(lineRef.getApparentPowerLimits(Branch.Side.TWO).orElse(null), lineAdapted.getApparentPowerLimits(Branch.Side.TWO).orElse(null));
        assertEquals(lineRef.getOperationalLimits1().size(), lineAdapted.getOperationalLimits1().size());
        assertEquals(lineRef.getOperationalLimits2().size(), lineAdapted.getOperationalLimits2().size());

        assertEquals(lineRef.isOverloaded(), lineAdapted.isOverloaded());
        assertEquals(lineRef.isOverloaded(0.0f), lineAdapted.isOverloaded(0.0f));
        assertEquals(lineRef.getOverloadDuration(), lineAdapted.getOverloadDuration());
        assertEquals(lineRef.checkPermanentLimit(Branch.Side.ONE, 0.0f, LimitType.CURRENT), lineAdapted.checkPermanentLimit(Branch.Side.ONE, 0.0f, LimitType.CURRENT));
        assertEquals(lineRef.checkPermanentLimit(Branch.Side.TWO, LimitType.CURRENT), lineAdapted.checkPermanentLimit(Branch.Side.TWO, LimitType.CURRENT));
        assertEquals(lineRef.checkPermanentLimit1(0.0f, LimitType.CURRENT), lineAdapted.checkPermanentLimit1(0.0f, LimitType.CURRENT));
        assertEquals(lineRef.checkPermanentLimit1(LimitType.CURRENT), lineAdapted.checkPermanentLimit1(LimitType.CURRENT));
        assertEquals(lineRef.checkPermanentLimit2(0.0f, LimitType.CURRENT), lineAdapted.checkPermanentLimit2(0.0f, LimitType.CURRENT));
        assertEquals(lineRef.checkPermanentLimit2(LimitType.CURRENT), lineAdapted.checkPermanentLimit2(LimitType.CURRENT));
        assertEquals(lineRef.checkTemporaryLimits(Branch.Side.ONE, 0.0f, LimitType.CURRENT), lineAdapted.checkTemporaryLimits(Branch.Side.ONE, 0.0f, LimitType.CURRENT));
        assertEquals(lineRef.checkTemporaryLimits(Branch.Side.TWO, LimitType.CURRENT), lineAdapted.checkTemporaryLimits(Branch.Side.TWO, LimitType.CURRENT));
        assertEquals(lineRef.checkTemporaryLimits1(0.0f, LimitType.CURRENT), lineAdapted.checkTemporaryLimits1(0.0f, LimitType.CURRENT));
        assertEquals(lineRef.checkTemporaryLimits1(LimitType.CURRENT), lineAdapted.checkTemporaryLimits1(LimitType.CURRENT));
        assertEquals(lineRef.checkTemporaryLimits2(0.0f, LimitType.CURRENT), lineAdapted.checkTemporaryLimits2(0.0f, LimitType.CURRENT));
        assertEquals(lineRef.checkTemporaryLimits2(LimitType.CURRENT), lineAdapted.checkTemporaryLimits2(LimitType.CURRENT));

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

        //Move
        String vlNbId = "VLNB";
        VoltageLevel vlNb = mergingView.getSubstation("P1")
                .newVoltageLevel()
                .setId(vlNbId)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        TestUtil.notImplemented(() -> lineAdapted.getTerminal1().getNodeBreakerView().moveConnectable(0, vlNbId));
        TestUtil.notImplemented(() -> lineAdapted.getTerminal2().getNodeBreakerView().moveConnectable(0, vlNbId));

        TestUtil.notImplemented(() -> lineAdapted.getTerminal1().getBusBreakerView().moveConnectable("busId", false));

        String ngen2Id = "NGEN2";
        Bus ngen2 = mergingView.getVoltageLevel("VLGEN").getBusBreakerView()
                .newBus()
                .setId(ngen2Id)
                .add();
        TestUtil.notImplemented(() -> lineAdapted.getTerminal1().getBusBreakerView().moveConnectable(ngen2Id, true));
        TestUtil.notImplemented(() -> lineAdapted.getTerminal2().getBusBreakerView().moveConnectable(ngen2Id, true));

        String vlGenId = "VLGEN";
        VoltageLevel vlgen = mergingView.getVoltageLevel(vlGenId);
        TestUtil.notImplemented(() -> lineAdapted.getTerminal1().getNodeBreakerView().moveConnectable(0, vlGenId));

        TestUtil.notImplemented(lineAdapted::remove);
    }

    @Test
    void adderFromSameNetworkTests() {
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
    void adderFromBothNetworkTests() {
        // adder line with both voltage level in different network
        final Network noEquipNetwork = NoEquipmentNetworkFactory.create();
        mergingView.merge(noEquipNetwork);

        // Exception(s)
        PowsyblException e = assertThrows(PowsyblException.class, () -> createLine(mergingView,
                "lineOnBothNetworkId",
                "lineOnBothNetworkName",
                "vl1",
                "busA",
                "busA"));
        assertTrue(e.getMessage().contains("Should not create a line between two sub-networks: create a tie line"));
    }

    @Test
    void addLineWithoutIdTests() {
        mergingView.merge(NoEquipmentNetworkFactory.create());

        // Exception(s)
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.newLine()
                       .setVoltageLevel1("vl1")
                       .setVoltageLevel2("vl2")
                   .add());
        assertTrue(e.getMessage().contains("Line id is not set"));
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
                          .setG1(1e-8)
                          .setG2(0.0)
                          .setB1(1.6e-3)
                          .setB2(1.6e-3)
                          .setVoltageLevel1(voltageLevelId1)
                          .setVoltageLevel2("VLBAT")
                          .setBus1(busId1)
                          .setBus2("NBAT")
                          .setConnectableBus1(connectableBusId1)
                          .setConnectableBus2("NBAT")
                      .add();
    }
}
