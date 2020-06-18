/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLimits;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractVoltageLimitsTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        load.newVoltageLimits()
                .setHighVoltage(220.0)
                .setLowVoltage(140.0)
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Load load = network.getLoad("LOAD");

        assertFalse(load.getOperationalLimits().isEmpty());
        assertEquals(1, load.getOperationalLimits().size());
        VoltageLimits voltageLimits = load.getVoltageLimits();
        assertNotNull(voltageLimits);
        assertEquals(140.0, voltageLimits.getLowVoltage(), 0.0);
        assertEquals(220.0, voltageLimits.getHighVoltage(), 0.0);
    }
}
