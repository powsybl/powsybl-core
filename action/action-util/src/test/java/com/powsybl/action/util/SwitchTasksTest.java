/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class SwitchTasksTest {

    private final Network network = NetworkTest1Factory.create();

    @Test
    public void test() {
        String switchId = "generator1Breaker1";

        Switch sw = network.getSwitch(switchId);
        Generator generator = network.getGenerator("generator1");

        Assert.assertFalse(sw.isOpen());
        Assert.assertTrue(generator.getTerminal().isConnected());

        new OpenSwitchTask(switchId).apply(network);
        Assert.assertTrue(sw.isOpen());
        Assert.assertFalse(generator.getTerminal().isConnected());

        new CloseSwitchTask(switchId).apply(network);
        Assert.assertFalse(sw.isOpen());
        Assert.assertTrue(generator.getTerminal().isConnected());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidOpenSwitch() {
        new OpenSwitchTask("dummy").apply(network);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidCloseSwitch() {
        new CloseSwitchTask("dummy").apply(network);
    }
}
