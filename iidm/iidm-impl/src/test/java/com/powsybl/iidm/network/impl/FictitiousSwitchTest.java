/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class FictitiousSwitchTest {

    @Test
    public void test() {
        Network network = FictitiousSwitchFactory.create();

        assertTrue(network.getSwitch("F").isFictitious());
        assertFalse(network.getSwitch("R").isFictitious());

        network.getSwitch("F").setFictitious(false);
        network.getSwitch("R").setFictitious(true);

        assertFalse(network.getSwitch("F").isFictitious());
        assertTrue(network.getSwitch("R").isFictitious());
    }
}
