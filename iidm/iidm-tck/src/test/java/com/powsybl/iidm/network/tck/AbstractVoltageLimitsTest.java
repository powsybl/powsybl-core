/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractVoltageLimitsTest {

    @Test
    public void testBus() {
        Network network = EurostagTutorialExample1Factory.create();
        Bus bus = network.getBusBreakerView().getBus("NLOAD");
        Bus bbus = bus.getConnectedTerminalStream().findFirst().map(t -> t.getBusView().getBus()).orElseThrow(() -> new AssertionError("Should not happen"));
        try {
            bbus.newVoltageLimits();
            fail();
        } catch (ValidationException e) {
            assertEquals("Bus 'VLLOAD_0': no voltage limit can be created on a calculated object: directly set on the configured object", e.getMessage());
        }
        bus.newVoltageLimits()
                .setHighVoltage(220.0)
                .setLowVoltage(140.0)
                .add();

        testCalculated("Bus 'VLLOAD_0'", bbus);
        testConfigured("Bus 'NLOAD'", bus);
    }

    @Test
    public void testBusbarSection() {
        Network network = FictitiousSwitchFactory.create();
        BusbarSection busbarSection = network.getBusbarSection("O");
        Bus bus = busbarSection.getTerminal().getBusBreakerView().getBus();
        try {
            bus.newVoltageLimits();
            fail();
        } catch (ValidationException e) {
            assertEquals("Bus 'N_0': no voltage limit can be created on a calculated object: directly set on the configured object", e.getMessage());
        }
        busbarSection.newVoltageLimits()
                .setHighVoltage(220.0)
                .setLowVoltage(140.0)
                .add();

        testCalculated("Bus 'N_0'", bus);
        testConfigured("Busbar section 'O'", busbarSection);

        busbarSection.newVoltageLimits().setHighVoltage(220.0).add();
        assertNotNull(bus.getVoltageLimits());
        assertTrue(Double.isNaN(bus.getVoltageLimits().getLowVoltage()));

        busbarSection.newVoltageLimits().setLowVoltage(140.0).add();
        assertNotNull(bus.getVoltageLimits());
        assertTrue(Double.isNaN(bus.getVoltageLimits().getHighVoltage()));
    }

    @Test
    public void testFail() {
        Network network = EurostagTutorialExample1Factory.create();
        Bus bus = network.getBusBreakerView().getBus("NLOAD");
        try {
            bus.newVoltageLimits().add();
            fail();
        } catch (ValidationException e) {
            assertEquals("Bus 'NLOAD': At least the low or the high voltage limit must be defined.", e.getMessage());
        }
    }

    private static void testConfigured(String message, VoltageLimitsHolder holder) {
        VoltageLimits voltageLimits = holder.getVoltageLimits();
        assertNotNull(voltageLimits);
        assertEquals(140.0, voltageLimits.getLowVoltage(), 0.0);
        assertEquals(220.0, voltageLimits.getHighVoltage(), 0.0);
        voltageLimits.setHighVoltage(Double.NaN);
        assertTrue(Double.isNaN(voltageLimits.getHighVoltage()));
        try {
            voltageLimits.setLowVoltage(Double.NaN);
            fail();
        } catch (ValidationException e) {
            assertEquals(message + ": At least the low or the high voltage limit must be defined.", e.getMessage());
        }
        voltageLimits.setLowVoltage(120.0);
        assertEquals(120.0, voltageLimits.getLowVoltage(), 0.0);
        voltageLimits.remove();
        assertNull(holder.getVoltageLimits());
    }

    private static void testCalculated(String message, VoltageLimitsHolder holder) {
        VoltageLimits voltageLimits = holder.getVoltageLimits();
        assertNotNull(voltageLimits);
        assertEquals(140.0, voltageLimits.getLowVoltage(), 0.0);
        assertEquals(220.0, voltageLimits.getHighVoltage(), 0.0);
        try {
            voltageLimits.setHighVoltage(100);
            fail();
        } catch (ValidationException e) {
            assertEquals(message + ": Voltage limits cannot be set on a calculated object: directly set on the configured object.", e.getMessage());
        }
        try {
            voltageLimits.setLowVoltage(100);
            fail();
        } catch (ValidationException e) {
            assertEquals(message + ": Voltage limits cannot be set on a calculated object: directly set on the configured object.", e.getMessage());
        }
        try {
            voltageLimits.remove();
            fail();
        } catch (ValidationException e) {
            assertEquals(message + ": Voltage limits cannot be removed from a calculated object: directly remove from the configured object.", e.getMessage());
        }
    }
}
