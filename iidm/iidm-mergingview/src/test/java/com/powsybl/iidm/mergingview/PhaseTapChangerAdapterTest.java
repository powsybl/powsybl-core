/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerAdapterTest {
    private Network network;
    private MergingView mergingView;
    private Terminal terminal;

    @Before
    public void setup() {
        mergingView = MergingView.create("PhaseTapChangerAdapterTest", "iidm");
        network = NoEquipmentNetworkFactory.create();
        mergingView.merge(network);

        final Substation substation = mergingView.getSubstation("sub");
        final TwoWindingsTransformer twt = substation.newTwoWindingsTransformer()
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
        terminal = twt.getTerminal(Branch.Side.ONE);
    }

    @Test
    public void testSetterGetter() {
        final String id = "twt";
        final TwoWindingsTransformer twt = mergingView.getTwoWindingsTransformer(id);

        // adder
        final PhaseTapChanger phaseTapChanger = twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setTargetDeadband(1.0)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10.0)
                .setRegulationTerminal(terminal)
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
        assertTrue(twt instanceof TwoWindingsTransformerAdapter);
        assertTrue(phaseTapChanger instanceof PhaseTapChangerAdapter);

        assertEquals(2, phaseTapChanger.getStepCount());
        assertEquals(0, phaseTapChanger.getLowTapPosition());
        assertEquals(1, phaseTapChanger.getHighTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(1.0, phaseTapChanger.getTargetDeadband(), 0.0);
        assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
        assertSame(terminal, phaseTapChanger.getRegulationTerminal());
        assertEquals(10.0, phaseTapChanger.getRegulationValue(), 0.0);

        // setter getter
        phaseTapChanger.setTapPosition(0);
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertSame(phaseTapChanger.getCurrentStep(), phaseTapChanger.getStep(0));
        phaseTapChanger.setRegulationValue(5.0);
        assertEquals(5.0, phaseTapChanger.getRegulationValue(), 0.0);
        phaseTapChanger.setTargetDeadband(0.5);
        assertEquals(0.5, phaseTapChanger.getTargetDeadband(), 0.0);
        phaseTapChanger.setRegulating(false);
        assertFalse(phaseTapChanger.isRegulating());
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        final Terminal terminal2 = twt.getTerminal2();
        phaseTapChanger.setRegulationTerminal(terminal2);
        assertSame(terminal2, phaseTapChanger.getRegulationTerminal());
        final int lowTapPosition = 2;
        phaseTapChanger.setLowTapPosition(lowTapPosition);
        assertEquals(lowTapPosition, phaseTapChanger.getLowTapPosition());

        // Install listener
        final NetworkListener listener = Mockito.mock(MergingNetworkListener.class);
        mergingView.addListener(listener);
        // Remove
        final TwoWindingsTransformer twtToRemove = network.getTwoWindingsTransformer(id);
        final PhaseTapChanger ptcToRemoved = twtToRemove.getPhaseTapChanger();
        phaseTapChanger.remove();
        assertNull(twt.getPhaseTapChanger());
        Mockito.verify(listener, Mockito.times(1)).onUpdate(twtToRemove, "phaseTapChanger", ptcToRemoved, null);
        Mockito.verify(listener, Mockito.never()).onRemoval(Mockito.any(Identifiable.class));
        // Uninstall listener
        mergingView.removeListener(listener);
    }
}
