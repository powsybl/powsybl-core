/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusBreakerView;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;
    private Substation substation;
    private TwoWindingsTransformer twt;

    @Before
    public void setup() {
        mergingView = MergingView.create("TwoWindingsTransformerAdapterTest", "iidm");
        mergingView.merge(NoEquipmentNetworkFactory.create());
        substation = mergingView.getSubstation("sub");
        twt = createTwoWindingsTransformer();
    }

    @Test
    public void testSetterGetter() {
        assertNotNull(twt);
        assertSame(mergingView.getTwoWindingsTransformer("twt"), mergingView.getBranch("twt"));
        assertTrue(twt instanceof TwoWindingsTransformerAdapter);
        assertSame(mergingView, twt.getNetwork());

        assertEquals(ConnectableType.TWO_WINDINGS_TRANSFORMER, twt.getType());
        assertSame(substation, twt.getSubstation());

        assertNotNull(twt.getRatioTapChanger());
        assertTrue(twt.getRatioTapChanger() instanceof RatioTapChangerAdapter);

        assertNotNull(twt.getPhaseTapChanger());
        assertTrue(twt.getPhaseTapChanger() instanceof PhaseTapChangerAdapter);

        // setter getter
        final double r = 0.5;
        assertTrue(twt.setR(r) instanceof AbstractAdapter<?>);
        assertEquals(r, twt.getR(), 0.0);
        final double b = 1.0;
        assertTrue(twt.setB(b) instanceof AbstractAdapter<?>);
        assertEquals(b, twt.getB(), 0.0);
        final double g = 2.0;
        assertTrue(twt.setG(g) instanceof AbstractAdapter<?>);
        assertEquals(g, twt.getG(), 0.0);
        final double x = 4.0;
        assertTrue(twt.setX(x) instanceof AbstractAdapter<?>);
        assertEquals(x, twt.getX(), 0.0);
        final double ratedU1 = 8.0;
        assertTrue(twt.setRatedU1(ratedU1) instanceof AbstractAdapter<?>);
        assertEquals(ratedU1, twt.getRatedU1(), 0.0);
        final double ratedU2 = 16.0;
        assertTrue(twt.setRatedU2(ratedU2) instanceof AbstractAdapter<?>);
        assertEquals(ratedU2, twt.getRatedU2(), 0.0);

        assertEquals(substation.getTwoWindingsTransformerStream().count(), substation.getTwoWindingsTransformerCount());

        assertEquals(2, twt.getTerminals().size());
        assertEquals(twt.getTerminal1(), twt.getTerminal(Side.ONE));
        assertEquals(twt.getTerminal2(), twt.getTerminal(Side.TWO));
        assertEquals(Side.ONE, twt.getSide(twt.getTerminal1()));
        assertEquals(Side.TWO, twt.getSide(twt.getTerminal2()));
        assertEquals(twt.getTerminal1(), twt.getTerminal("vl1"));
        assertEquals(twt.getTerminal2(), twt.getTerminal("vl2"));
        final CurrentLimits currentLimits1 = twt.newCurrentLimits1()
                                                    .setPermanentLimit(100)
                                                    .beginTemporaryLimit()
                                                        .setName("5'")
                                                        .setAcceptableDuration(5 * 60)
                                                        .setValue(1400)
                                                    .endTemporaryLimit()
                                                .add();
        final CurrentLimits currentLimits2 = twt.newCurrentLimits2()
                                                .setPermanentLimit(50)
                                                    .beginTemporaryLimit()
                                                        .setName("20'")
                                                        .setAcceptableDuration(20 * 60)
                                                        .setValue(1200)
                                                    .endTemporaryLimit()
                                                .add();
        assertFalse(currentLimits1 instanceof AbstractAdapter<?>);
        assertFalse(currentLimits2 instanceof AbstractAdapter<?>);
        assertEquals(twt.getCurrentLimits(Side.ONE), twt.getCurrentLimits1());
        assertEquals(twt.getCurrentLimits(Side.TWO), twt.getCurrentLimits2());
        assertFalse(twt.isOverloaded());
        assertFalse(twt.isOverloaded(0.0f));
        assertEquals(Integer.MAX_VALUE, twt.getOverloadDuration());

        assertFalse(twt.checkPermanentLimit(Branch.Side.ONE));
        assertFalse(twt.checkPermanentLimit(Branch.Side.ONE, 0.9f));
        assertFalse(twt.checkPermanentLimit1());
        assertFalse(twt.checkPermanentLimit1(0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.ONE, 0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.ONE));
        assertNull(twt.checkTemporaryLimits1());
        assertNull(twt.checkTemporaryLimits1(0.9f));

        assertFalse(twt.checkPermanentLimit(Branch.Side.TWO, 0.9f));
        assertFalse(twt.checkPermanentLimit(Branch.Side.TWO));
        assertFalse(twt.checkPermanentLimit2());
        assertFalse(twt.checkPermanentLimit2(0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.TWO, 0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.TWO));
        assertNull(twt.checkTemporaryLimits2());
        assertNull(twt.checkTemporaryLimits2(0.9f));

        // Not implemented yet !
        TestUtil.notImplemented(twt::remove);
    }

    @Test
    public void testTerminalSetterGetter() {
        final Terminal t1 = twt.getTerminal1();
        assertTrue(t1 instanceof AbstractAdapter<?>);
        assertNotNull(t1.getVoltageLevel());

        final BusBreakerView busBreakerView = t1.getBusBreakerView();
        assertTrue(busBreakerView instanceof AbstractAdapter<?>);
        busBreakerView.setConnectableBus("busA");
        assertNull(busBreakerView.getBus());
        assertNotNull(busBreakerView.getConnectableBus());

        final BusView busView = t1.getBusView();
        assertTrue(busView instanceof AbstractAdapter<?>);
        assertNull(busView.getBus());
        assertNull(busView.getConnectableBus());

        assertNotNull(t1.getConnectable());
        final double p = 4.0;
        assertTrue(t1.setP(p) instanceof AbstractAdapter<?>);
        assertEquals(p, t1.getP(), 0.0);
        final double q = 5.0;
        assertTrue(t1.setQ(q) instanceof AbstractAdapter<?>);
        assertEquals(q, t1.getQ(), 0.0);
        assertEquals(Double.NaN, t1.getI(), 0.0);
        assertTrue(t1.connect());
        assertTrue(t1.disconnect());
        assertFalse(t1.isConnected());

        final List<String> traversed = new ArrayList<>();
        t1.traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(final Terminal terminal, final boolean connected) {
                traversed.add(terminal.getConnectable()
                        .getId());
                return true;
            }

            @Override
            public boolean traverse(final Switch aSwitch) {
                return true;
            }
        });
        assertEquals(Arrays.asList("twt", "twt"), traversed);

        Terminal.NodeBreakerView nodeBreakerView = t1.getNodeBreakerView();
        assertNotNull(nodeBreakerView);
        assertTrue(nodeBreakerView instanceof AbstractAdapter<?>);

        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Not supported in a bus breaker topology");
        nodeBreakerView.getNode();
    }

    private TwoWindingsTransformer createTwoWindingsTransformer() {
        final TwoWindingsTransformer twoWt = substation.newTwoWindingsTransformer()
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

        twoWt.newRatioTapChanger()
                 .setLowTapPosition(0)
                 .setTapPosition(1)
                 .setLoadTapChangingCapabilities(false)
                 .setRegulating(true)
                 .setTargetDeadband(1.0)
                 .setTargetV(220.0)
                 .setRegulationTerminal(twoWt.getTerminal1())
                     .beginStep()
                         .setR(39.78473)
                         .setX(39.784725)
                         .setG(0.0)
                         .setB(0.0)
                         .setRho(1.0)
                     .endStep()
                     .beginStep()
                         .setR(39.78474)
                         .setX(39.784726)
                         .setG(0.0)
                         .setB(0.0)
                         .setRho(1.0)
                     .endStep()
                     .beginStep()
                         .setR(39.78475)
                         .setX(39.784727)
                         .setG(0.0)
                         .setB(0.0)
                         .setRho(1.0)
                     .endStep()
             .add();

        twoWt.newPhaseTapChanger()
                 .setTapPosition(1)
                 .setLowTapPosition(0)
                 .setRegulating(true)
                 .setTargetDeadband(1.0)
                 .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                 .setRegulationValue(10.0)
                 .setRegulationTerminal(twoWt.getTerminal1())
                     .beginStep()
                         .setR(1.0)
                         .setX(2.0)
                         .setG(3.0)
                         .setB(4.0)
                         .setAlpha(5.0)
                         .setRho(6.0)
                     .endStep()
                     .beginStep()
                         .setR(1.0)
                         .setX(2.0)
                         .setG(3.0)
                         .setB(4.0)
                         .setAlpha(5.0)
                         .setRho(6.0)
                     .endStep()
             .add();
        return twoWt;
    }
}
