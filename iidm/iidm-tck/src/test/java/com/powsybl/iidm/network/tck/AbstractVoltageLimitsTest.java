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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractVoltageLimitsTest {

    @Test
    public void testBus() {
        Network network = EurostagTutorialExample1Factory.create();
        Bus bus = network.getBusBreakerView().getBus("NLOAD");
        bus.newVoltageLimits()
                .setHighVoltage(220.0)
                .setLowVoltage(140.0)
                .add();

        test(bus);
    }

    @Test
    public void testBusbarSection() {
        Network network = FictitiousSwitchFactory.create();
        BusbarSection busbarSection = network.getBusbarSection("O");
        busbarSection.newVoltageLimits()
                .setHighVoltage(220.0)
                .setLowVoltage(140.0)
                .add();

        test(busbarSection);
    }

    private static void test(VoltageLimitsHolder holder) {
        VoltageLimits voltageLimits = holder.getVoltageLimits();
        assertNotNull(voltageLimits);
        assertEquals(140.0, voltageLimits.getLowVoltage(), 0.0);
        assertEquals(220.0, voltageLimits.getHighVoltage(), 0.0);
        voltageLimits.remove();
        assertNull(holder.getVoltageLimits());
    }
}
