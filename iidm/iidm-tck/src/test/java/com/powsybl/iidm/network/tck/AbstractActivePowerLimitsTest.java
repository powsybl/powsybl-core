/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.ActivePowerLimits;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractActivePowerLimitsTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        line.newActivePowerLimits1()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                    .setValue(370)
                    .setAcceptableDuration(20 * 60)
                    .setName("20'")
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setValue(380)
                    .setAcceptableDuration(10 * 60)
                    .setName("10'")
                .endTemporaryLimit()
                .add();
        line.newActivePowerLimits2()
                .setPermanentLimit(400)
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("NHV1_NHV2_1");

        // limits1
        assertFalse(l.getOperationalLimits1().isEmpty());
        ActivePowerLimits limits1 = l.getActivePowerLimits1();
        assertNotNull(limits1);
        assertEquals(350, limits1.getPermanentLimit(), 0.0);
        assertEquals(2, limits1.getTemporaryLimits().size());
        assertEquals("20'", limits1.getTemporaryLimit(20 * 60).getName());
        assertEquals(370, limits1.getTemporaryLimit(20 * 60).getValue(), 0.0);
        assertEquals("10'", limits1.getTemporaryLimit(10 * 60).getName());
        assertEquals(380, limits1.getTemporaryLimit(10 * 60).getValue(), 0.0);

        // limits2
        assertFalse(l.getOperationalLimits2().isEmpty());
        ActivePowerLimits limits2 = l.getActivePowerLimits2();
        assertNotNull(limits2);
        assertEquals(400, limits2.getPermanentLimit(), 0.0);
        assertTrue(limits2.getTemporaryLimits().isEmpty());
    }
}
