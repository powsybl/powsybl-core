/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 *
 */
package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.TerminalExt;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class VoltageRegulationExtensionTest {

    private void assertRegulatingTerminal(Terminal expectedRegulatingTerminal, VoltageRegulation voltageRegulation) {
        assertRegulatingTerminal(expectedRegulatingTerminal, voltageRegulation, null);
    }

    private void assertRegulatingTerminal(Terminal expectedRegulatingTerminal, VoltageRegulation voltageRegulation, Terminal oldTerminal) {
        assertEquals(expectedRegulatingTerminal, voltageRegulation.getTerminal());
        if (expectedRegulatingTerminal != null) {
            assertEquals(1, ((TerminalExt) voltageRegulation.getTerminal()).getReferrerManager().getReferrers().size());
        }
        if (oldTerminal != null) {
            assertTrue(((TerminalExt) oldTerminal).getReferrerManager().getReferrers().isEmpty());
        }
    }

    @Test
    void replacementAndCleanUpTest() {
        Network network = BatteryNetworkFactory.create();
        var battery = network.getBattery("BAT");
        var battery2 = network.getBattery("BAT2");
        Terminal battery2Terminal0 = battery2.getTerminal();
        VoltageRegulation voltageRegulation = battery.newVoltageRegulation()
            .withTerminal(battery2.getTerminal())
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(50.0)
            .build();
        assertRegulatingTerminal(battery2Terminal0, voltageRegulation);

        // Replacement
        Terminal.BusBreakerView bbView = battery2Terminal0.getBusBreakerView();
        bbView.moveConnectable("NGEN", true);
        assertNotEquals(battery2Terminal0, voltageRegulation.getTerminal());
        assertRegulatingTerminal(battery2.getTerminal(), voltageRegulation, battery2Terminal0);

        // Clean up
        TerminalExt regulatingTerminal = (TerminalExt) voltageRegulation.getTerminal();
        battery.removeVoltageRegulation();
        assertTrue(regulatingTerminal.getReferrerManager().getReferrers().isEmpty());
    }
}
