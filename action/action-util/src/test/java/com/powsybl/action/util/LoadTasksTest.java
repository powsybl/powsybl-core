/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
public class LoadTasksTest {

    private final Network network = NetworkTest1Factory.create();
    private static final float EPSILON = 1e-6f;

    @Test
    public void test() {
        String loadId = "load1";

        Load load = network.getLoad(loadId);

        assertEquals(10f, load.getP0(), EPSILON);
        assertEquals(3f, load.getQ0(), EPSILON);

        new LoadP0Task(loadId, 20f).modify(network, null);
        assertEquals(20f, load.getP0(), EPSILON);
        new LoadQ0Task(loadId, 1f).modify(network, null);
        assertEquals(1f, load.getQ0(), EPSILON);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidLoadP0() {
        new LoadP0Task("dummy", 1f).modify(network, null);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidLoadQ0() {
        new LoadQ0Task("dummy", 1f).modify(network, null);
    }
}
