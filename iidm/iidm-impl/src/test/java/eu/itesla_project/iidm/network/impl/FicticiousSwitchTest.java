/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.FicticiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class FicticiousSwitchTest {

    @Test
    public void test() {
        Network network = FicticiousSwitchFactory.create();

        assertTrue(network.getSwitch("F").isFicticious());
        assertFalse(network.getSwitch("R").isFicticious());

        network.getSwitch("F").setFicticious(false);
        network.getSwitch("R").setFicticious(true);

        assertFalse(network.getSwitch("F").isFicticious());
        assertTrue(network.getSwitch("R").isFicticious());
    }
}
