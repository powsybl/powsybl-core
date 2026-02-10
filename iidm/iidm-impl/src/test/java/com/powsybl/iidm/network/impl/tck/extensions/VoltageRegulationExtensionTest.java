/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 *
 */
package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.TerminalExt;
import com.powsybl.iidm.network.regulation.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class VoltageRegulationExtensionTest {

    @Test
    void testMultiVariant() {
        String newState1 = "newState1";

        Network network = BatteryNetworkFactory.create();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, newState1);
        network.getVariantManager().setWorkingVariant(newState1);

        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        VoltageRegulation voltageRegulation = bat.newVoltageRegulation()
//            .withTerminal(bat.getTerminal())
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTargetValue(50.0)
            .build();
        assertNotNull(voltageRegulation);

        assertEquals(50.0, voltageRegulation.getTargetValue(), 0);
        voltageRegulation.setTargetValue(51.0);
        assertEquals(51.0, voltageRegulation.getTargetValue(), 0);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        voltageRegulation.setMode(RegulationMode.REACTIVE_POWER);
        assertEquals(RegulationMode.REACTIVE_POWER, voltageRegulation.getMode());

        String newState2 = "newState2";
        network.getVariantManager().cloneVariant(newState1, newState2);
        network.getVariantManager().setWorkingVariant(newState2);
        assertEquals(RegulationMode.REACTIVE_POWER, voltageRegulation.getMode());
        assertEquals(51.0, voltageRegulation.getTargetValue(), 0);

        voltageRegulation.setTargetValue(50.0);
        voltageRegulation.setMode(RegulationMode.VOLTAGE);
        assertEquals(50.0, voltageRegulation.getTargetValue(), 0);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());

        network.getVariantManager().setWorkingVariant(newState1);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertEquals(51.0, voltageRegulation.getTargetValue(), 0);

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(50.0, voltageRegulation.getTargetValue(), 0);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
    }

    @Test
    @Disabled("TODO MSA modify this test to be compliant with VoltageRegulation validation")
    void testVoltageRegulationExtensionCreationException() {

        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        VoltageRegulationBuilder builder = bat.newVoltageRegulation()
            .withTerminal(bat.getTerminal())
            .withTargetValue(50.0);

        PowsyblException e = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Voltage regulator status is not defined", e.getMessage());

        Network network1 = BatteryNetworkFactory.create();

        VoltageRegulationBuilder builder1 = bat.newVoltageRegulation()
            .withTerminal(network1.getBattery("BAT").getTerminal())
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(50.0);

        PowsyblException e1 = assertThrows(PowsyblException.class, builder1::build);
        assertEquals("regulating terminal is not part of the same network", e1.getMessage());
    }

    @Test
    void removeTerminalTest() {
        Network network = BatteryNetworkFactory.create();
        var battery = network.getBattery("BAT");
        var battery2 = network.getBattery("BAT2");
        VoltageRegulation voltageRegulation = battery.newVoltageRegulation()
            .withTerminal(battery2.getTerminal())
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTargetValue(50.0)
            .build();
        assertRegulatingTerminal(battery2.getTerminal(), voltageRegulation);
        battery2.remove();
        // Fallback on local terminal
        assertRegulatingTerminal(null, voltageRegulation, battery2.getTerminal());
    }

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
    void changeTerminalTest() {
        Network network = BatteryNetworkFactory.create();
        var vlbat = network.getVoltageLevel("VLBAT");
        Bus nbat = vlbat.getBusBreakerView().getBus("NBAT");

        Battery battery3 = vlbat.newBattery()
                .setId("BAT3")
                .setBus(nbat.getId())
                .setConnectableBus(nbat.getId())
                .setTargetP(9999.99)
                .setTargetQ(9999.99)
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .add();
        battery3.newMinMaxReactiveLimits()
                .setMinQ(-9999.99)
                .setMaxQ(9999.99)
                .add();
        battery3.getTerminal().setP(-605);
        battery3.getTerminal().setQ(-225);

        var battery = network.getBattery("BAT");
        var battery2 = network.getBattery("BAT2");
        VoltageRegulation voltageRegulation = battery.newVoltageRegulation()
            .withTerminal(battery2.getTerminal())
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTargetValue(50.0)
            .build();
        assertRegulatingTerminal(battery2.getTerminal(), voltageRegulation);
        voltageRegulation.setTerminal(battery3.getTerminal());
        assertRegulatingTerminal(battery3.getTerminal(), voltageRegulation, battery2.getTerminal());
        // Removing battery 2 should not change the regulating terminal
        battery2.remove();
        assertRegulatingTerminal(battery3.getTerminal(), voltageRegulation);
        // Removing battery 3 should change the regulating terminal to the local one (fallback)
        battery3.remove();
        assertRegulatingTerminal(null, voltageRegulation, battery3.getTerminal());
        // Switch to local regulation (this was already the case)
        voltageRegulation.setTerminal(null);
        assertRegulatingTerminal(null, voltageRegulation);
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
            .withRegulating(true)
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
