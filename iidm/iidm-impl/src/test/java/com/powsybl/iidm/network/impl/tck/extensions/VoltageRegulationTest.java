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
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.network.impl.TerminalExt;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class VoltageRegulationTest {

    @Test
    void testMultiVariant() {
        String newState1 = "newState1";

        Network network = BatteryNetworkFactory.create();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, newState1);
        network.getVariantManager().setWorkingVariant(newState1);

        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        bat.newExtension(VoltageRegulationAdder.class).withRegulatingTerminal(bat.getTerminal()).withVoltageRegulatorOn(true).withTargetV(50.0).add();
        VoltageRegulation voltageRegulation = bat.getExtension(VoltageRegulation.class);
        assertEquals("voltageRegulation", voltageRegulation.getName());
        assertNotNull(voltageRegulation.getExtendable());
        assertEquals("BAT", voltageRegulation.getExtendable().getId());

        assertEquals(50.0, voltageRegulation.getTargetV(), 0);
        voltageRegulation.setTargetV(51);
        assertEquals(51.0, voltageRegulation.getTargetV(), 0);
        assertTrue(voltageRegulation.isVoltageRegulatorOn());
        voltageRegulation.setVoltageRegulatorOn(false);
        assertFalse(voltageRegulation.isVoltageRegulatorOn());

        String newState2 = "newState2";
        network.getVariantManager().cloneVariant(newState1, newState2);
        network.getVariantManager().setWorkingVariant(newState2);
        assertFalse(voltageRegulation.isVoltageRegulatorOn());
        assertEquals(51.0, voltageRegulation.getTargetV(), 0);

        voltageRegulation.setTargetV(50);
        voltageRegulation.setVoltageRegulatorOn(true);
        assertEquals(50.0, voltageRegulation.getTargetV(), 0);
        assertTrue(voltageRegulation.isVoltageRegulatorOn());

        network.getVariantManager().setWorkingVariant(newState1);
        assertFalse(voltageRegulation.isVoltageRegulatorOn());
        assertEquals(51.0, voltageRegulation.getTargetV(), 0);

        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(50.0, voltageRegulation.getTargetV(), 0);
        assertTrue(voltageRegulation.isVoltageRegulatorOn());
    }

    @Test
    void testVoltageRegulationExtensionCreationException() {

        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        VoltageRegulationAdder adder = bat.newExtension(VoltageRegulationAdder.class)
            .withRegulatingTerminal(bat.getTerminal())
            .withTargetV(50.0);

        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Voltage regulator status is not defined", e.getMessage());

        Network network1 = BatteryNetworkFactory.create();

        VoltageRegulationAdder adder1 = bat.newExtension(VoltageRegulationAdder.class)
            .withRegulatingTerminal(network1.getBattery("BAT").getTerminal())
            .withVoltageRegulatorOn(true)
            .withTargetV(50.0);

        PowsyblException e1 = assertThrows(PowsyblException.class, adder1::add);
        assertEquals("regulating terminal is not part of the same network", e1.getMessage());
    }

    @Test
    void removeTerminalTest() {
        Network network = BatteryNetworkFactory.create();
        var battery = network.getBattery("BAT");
        var battery2 = network.getBattery("BAT2");
        VoltageRegulation voltageRegulation = battery.newExtension(VoltageRegulationAdder.class)
                .withRegulatingTerminal(battery2.getTerminal())
                .withVoltageRegulatorOn(true)
                .withTargetV(50.0)
                .add();
        assertRegulatingTerminal(battery2.getTerminal(), voltageRegulation);
        battery2.remove();
        // Fallback on local terminal
        assertRegulatingTerminal(battery.getTerminal(), voltageRegulation);
    }

    private void assertRegulatingTerminal(Terminal expectedRegulatingTerminal, VoltageRegulation voltageRegulation) {
        assertEquals(expectedRegulatingTerminal, voltageRegulation.getRegulatingTerminal());
        assertEquals(1, ((TerminalExt) voltageRegulation.getRegulatingTerminal()).getReferrerManager().getReferrers().size());
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
        VoltageRegulation voltageRegulation = battery.newExtension(VoltageRegulationAdder.class)
                .withRegulatingTerminal(battery2.getTerminal())
                .withVoltageRegulatorOn(true)
                .withTargetV(50.0)
                .add();
        assertRegulatingTerminal(battery2.getTerminal(), voltageRegulation);
        voltageRegulation.setRegulatingTerminal(battery3.getTerminal());
        assertRegulatingTerminal(battery3.getTerminal(), voltageRegulation);
        // Removing battery 2 should not change the regulating terminal
        battery2.remove();
        assertRegulatingTerminal(battery3.getTerminal(), voltageRegulation);
        // Removing battery 3 should change the regulating terminal to the local one (fallback)
        battery3.remove();
        assertRegulatingTerminal(battery.getTerminal(), voltageRegulation);
    }

    @Test
    void replacementAndCleanUpTest() {
        Network network = BatteryNetworkFactory.create();
        var battery = network.getBattery("BAT");
        var battery2 = network.getBattery("BAT2");
        Terminal battery2Terminal0 = battery2.getTerminal();
        VoltageRegulation voltageRegulation = battery.newExtension(VoltageRegulationAdder.class)
                .withRegulatingTerminal(battery2.getTerminal())
                .withVoltageRegulatorOn(true)
                .withTargetV(50.0)
                .add();
        assertRegulatingTerminal(battery2Terminal0, voltageRegulation);

        // Replacement
        Terminal.BusBreakerView bbView = battery2Terminal0.getBusBreakerView();
        bbView.moveConnectable("NGEN", true);
        assertNotEquals(battery2Terminal0, voltageRegulation.getRegulatingTerminal());
        assertRegulatingTerminal(battery2.getTerminal(), voltageRegulation);

        // Clean up
        TerminalExt regulatingTerminal = (TerminalExt) voltageRegulation.getRegulatingTerminal();
        battery.removeExtension(VoltageRegulation.class);
        assertTrue(regulatingTerminal.getReferrerManager().getReferrers().isEmpty());
    }
}
