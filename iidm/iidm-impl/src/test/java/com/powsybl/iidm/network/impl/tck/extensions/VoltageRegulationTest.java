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
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
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
}
