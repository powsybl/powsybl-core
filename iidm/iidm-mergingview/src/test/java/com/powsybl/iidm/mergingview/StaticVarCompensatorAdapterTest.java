/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class StaticVarCompensatorAdapterTest {
    private MergingView mergingView;

    @BeforeEach
    void setup() {
        mergingView = MergingView.create("StaticVarCompensatorAdapterTest", "iidm");
    }

    @Test
    void testSetterGetter() {
        Network networkRef = SvcTestCaseFactory.createWithRemoteRegulatingTerminal();
        mergingView.merge(networkRef);

        final StaticVarCompensator svcExpected = networkRef.getStaticVarCompensator("SVC2");
        final StaticVarCompensator svcActual = mergingView.getStaticVarCompensator("SVC2");
        assertNotNull(svcActual);
        assertTrue(svcActual instanceof StaticVarCompensatorAdapter);
        assertSame(mergingView, svcActual.getNetwork());

        assertEquals(svcExpected.getType(), svcActual.getType());
        assertTrue(svcActual.getTerminal() instanceof TerminalAdapter);
        svcActual.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        double bMin = svcExpected.getBmin();
        assertEquals(bMin, svcActual.getBmin(), 0.0d);
        assertTrue(svcActual.setBmin(++bMin) instanceof StaticVarCompensatorAdapter);
        assertEquals(bMin, svcActual.getBmin(), 0.0d);

        double bMax = svcExpected.getBmax();
        assertEquals(bMax, svcActual.getBmax(), 0.0d);
        assertTrue(svcActual.setBmax(++bMax) instanceof StaticVarCompensatorAdapter);
        assertEquals(bMax, svcActual.getBmax(), 0.0d);

        double voltageSetpoint = svcExpected.getVoltageSetpoint();
        assertEquals(voltageSetpoint, svcActual.getVoltageSetpoint(), 0.0d);
        assertTrue(svcActual.setVoltageSetpoint(++voltageSetpoint) instanceof StaticVarCompensatorAdapter);
        assertEquals(voltageSetpoint, svcActual.getVoltageSetpoint(), 0.0d);

        double reactivePowerSetpoint = svcExpected.getReactivePowerSetpoint();
        assertEquals(reactivePowerSetpoint, svcActual.getReactivePowerSetpoint(), 0.0d);
        assertTrue(svcActual.setReactivePowerSetpoint(++reactivePowerSetpoint) instanceof StaticVarCompensatorAdapter);
        assertEquals(reactivePowerSetpoint, svcActual.getReactivePowerSetpoint(), 0.0d);

        StaticVarCompensator.RegulationMode regulationMode = svcExpected.getRegulationMode();
        assertEquals(regulationMode, svcActual.getRegulationMode());
        regulationMode = StaticVarCompensator.RegulationMode.VOLTAGE;
        assertTrue(svcActual.setRegulationMode(regulationMode) instanceof StaticVarCompensatorAdapter);
        assertEquals(regulationMode, svcActual.getRegulationMode());

        assertTrue(svcActual.getRegulatingTerminal() instanceof TerminalAdapter);
        assertEquals(svcActual.getRegulatingTerminal(), mergingView.getLoad("L2").getTerminal());
        assertTrue(svcActual.setRegulatingTerminal(mergingView.getLine("L1").getTerminal1()) instanceof StaticVarCompensatorAdapter);
        assertEquals(svcActual.getRegulatingTerminal(), mergingView.getLine("L1").getTerminal1());

        // Not implemented yet !
        TestUtil.notImplemented(svcActual::remove);
    }
}
