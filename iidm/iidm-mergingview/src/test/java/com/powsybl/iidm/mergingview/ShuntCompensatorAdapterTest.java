/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("ShuntCompensatorAdapterTest", "iidm");
    }

    @Test
    public void testSetterGetter() {
        Network networkRef = HvdcTestNetwork.createLcc();
        mergingView.merge(networkRef);

        final ShuntCompensator shuntCExpected = networkRef.getShuntCompensator("C1_Filter1");
        final ShuntCompensator shuntCActual   = mergingView.getShuntCompensator("C1_Filter1");
        assertNotNull(shuntCActual);
        assertTrue(shuntCActual instanceof ShuntCompensatorAdapter);
        assertSame(mergingView, shuntCActual.getNetwork());

        assertEquals(shuntCExpected.getType(), shuntCActual.getType());
        assertTrue(shuntCActual.getTerminal() instanceof TerminalAdapter);
        shuntCActual.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        int maxCount = shuntCExpected.getMaximumSectionCount();
        assertEquals(maxCount, shuntCActual.getMaximumSectionCount());
        assertTrue(shuntCActual.setMaximumSectionCount(++maxCount) instanceof ShuntCompensatorAdapter);
        assertEquals(maxCount, shuntCActual.getMaximumSectionCount());

        int currentCount = shuntCExpected.getCurrentSectionCount();
        assertEquals(currentCount, shuntCActual.getCurrentSectionCount());
        assertTrue(shuntCActual.setCurrentSectionCount(++currentCount) instanceof ShuntCompensatorAdapter);
        assertEquals(currentCount, shuntCActual.getCurrentSectionCount());

        double b = shuntCExpected.getbPerSection();
        assertEquals(b, shuntCActual.getbPerSection(), 0.0d);
        assertTrue(shuntCActual.setbPerSection(++b) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getbPerSection(), shuntCActual.getbPerSection(), 0.0d);

        double currentB = shuntCExpected.getCurrentB();
        assertEquals(currentB, shuntCActual.getCurrentB(), 0.0d);
        assertTrue(shuntCActual.setbPerSection(++currentB) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getCurrentB(), shuntCActual.getCurrentB(), 0.0d);

        Terminal terminal = mergingView.getLccConverterStation("C1").getTerminal();
        assertTrue(shuntCActual.setRegulatingTerminal(terminal) instanceof ShuntCompensatorAdapter);
        assertSame(terminal, shuntCActual.getRegulatingTerminal());

        double targetV = shuntCExpected.getTargetV();
        assertEquals(targetV, shuntCActual.getTargetV(), 0.0d);
        assertTrue(shuntCActual.setTargetV(400) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getTargetV(), shuntCActual.getTargetV(), 0.0d);

        double targetDeadband = shuntCExpected.getTargetDeadband();
        assertEquals(targetDeadband, shuntCActual.getTargetDeadband(), 0.0d);
        assertTrue(shuntCActual.setTargetDeadband(20) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getTargetDeadband(), shuntCActual.getTargetDeadband(), 0.0d);

        boolean voltageRegulatorOn = shuntCExpected.isVoltageRegulatorOn();
        assertEquals(voltageRegulatorOn, shuntCActual.isVoltageRegulatorOn());
        assertTrue(shuntCActual.setVoltageRegulatorOn(!voltageRegulatorOn) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.isVoltageRegulatorOn(), shuntCActual.isVoltageRegulatorOn());

        // Not implemented yet !
        TestUtil.notImplemented(shuntCActual::remove);
    }
}
    //pour les méthodes setComponentNumber de BusExt je peux les remonter dans Bus
