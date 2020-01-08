/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.util.VoltageLevels;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class VoltageLevelsTest {

    @Test
    public void testFindBusById() {
        Network network = NetworkTest1Factory.create();
        VoltageLevel vl = network.getVoltageLevel("voltageLevel1");
        assertEquals(1, vl.getBusView().getBusStream().count());

        network.getSwitch("voltageLevel1Breaker1").setOpen(true);

        assertEquals("voltageLevel1_0", VoltageLevels.findBus(vl, 0).orElse("fail"));
        assertEquals("voltageLevel1_1", VoltageLevels.findBus(vl, 1).orElse("fail"));
        assertFalse(VoltageLevels.findBus(vl, 4).isPresent());
    }

}
