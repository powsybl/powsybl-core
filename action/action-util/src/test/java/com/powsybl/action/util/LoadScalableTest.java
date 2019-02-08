/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.action.util.ScalableTestNetwork.createNetwork;
import static org.junit.Assert.*;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
public class LoadScalableTest {

    private Network network;
    private Scalable l1;
    private Scalable l2;

    @Before
    public void setUp() {

        network = createNetwork();
        l1 = Scalable.load("l1");

        l2 = new LoadScalable("l1", 110);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorThrowWhenIdIsNull() {
        new LoadScalable(null);
    }

    @Test
    public void testInitialValue() {
        assertEquals(0, l1.initialValue(network), 1e-3);
    }


    @Test
    public void testMaximumlValue() {
        assertEquals(Double.POSITIVE_INFINITY, l1.maximumValue(network), 1e-3);
    }

    @Test
    public void testLoadScale() {
        Load load = network.getLoad("l1");
        assertEquals(100, load.getP0(), 1e-3);
        assertEquals(20, l1.scale(network, 20), 1e-3);
        assertEquals(-40, l1.scale(network, -40), 1e-3);

        //test with a maximum value
        assertEquals(120, load.getP0(), 1e-3);
        assertEquals(10, l2.scale(network, -40), 1e-3);
        assertEquals(40, l2.scale(network, 40), 1e-3);
        assertEquals(-40, l2.scale(network, -50), 1e-3);

    }
}
