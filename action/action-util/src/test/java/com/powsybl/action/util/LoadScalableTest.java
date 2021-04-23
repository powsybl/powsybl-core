/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.action.util.Scalable.ScalingConvention;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.action.util.Scalable.ScalingConvention.*;
import static com.powsybl.action.util.ScalableTestNetwork.createNetwork;
import static org.junit.Assert.*;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
public class LoadScalableTest {

    private Network network;
    private Scalable l1;
    private Scalable l2;
    private Scalable l3;
    private Scalable l4;
    private ScalingConvention convention;

    @Before
    public void setUp() {

        network = createNetwork();
        l1 = Scalable.onLoad("l1");

        l2 = new LoadScalable("l1", 110);
        l3 = new LoadScalable("l1", 20, 100);
        l4 = new LoadScalable("l1", -10, 100);

    }

    @Test(expected = NullPointerException.class)
    public void testConstructorThrowWhenIdIsNull() {
        new LoadScalable(null);
    }

    @Test(expected = PowsyblException.class)
    public void testConstructorInvalidP() {
        new LoadScalable("l1", 10, 0);
    }

    @Test
    public void testInitialValue() {
        assertEquals(0, l1.initialValue(network), 1e-3);
    }

    @Test
    public void testMaximumlValue() {
        assertEquals(Double.MAX_VALUE, l1.maximumValue(network, LOAD), 0.);
        assertEquals(-20, l3.maximumValue(network), 0.);
        assertEquals(-20, l3.maximumValue(network, GENERATOR), 0.);
        assertEquals(100, l3.maximumValue(network, LOAD), 0.);
    }

    @Test
    public void testMinimumlValue() {
        assertEquals(0, l1.minimumValue(network, LOAD), 0.);
        assertEquals(-100, l3.minimumValue(network), 0.);
        assertEquals(-100, l3.minimumValue(network, GENERATOR), 0.);
        assertEquals(20, l3.minimumValue(network, LOAD), 0.);
    }

    @Test
    public void testLoadScaleGeneratorConvention() {
        //test with ScalingConvention.GENERATOR
        convention = GENERATOR;

        //test with default maxValue = Double.MAX_VALUE and minValue = 0
        Load load = network.getLoad("l1");
        assertEquals(100, load.getP0(), 1e-3);
        assertEquals(20, l1.scale(network, 20, convention), 1e-3);
        assertEquals(80, load.getP0(), 1e-3);
        assertEquals(-40, l1.scale(network, -40, convention), 1e-3);
        assertEquals(120, load.getP0(), 1e-3);

        //test minValue = 0
        assertEquals(120, l1.scale(network, 140, convention), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);

        //test with a maximum value
        l2.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(-40, l2.scale(network, -40, convention), 1e-3);
        assertEquals(40, load.getP0(), 1e-3);
        assertEquals(-70, l2.scale(network, -80, convention), 1e-3);
        assertEquals(110, load.getP0(), 1e-3);
        assertEquals(110, l2.scale(network, 120, convention), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(-50, l2.scale(network, -50, convention), 1e-3);
        assertEquals(50, load.getP0(), 1e-3);

        //test with minValue = 20
        assertEquals(100, l3.maximumValue(network, LOAD), 1e-3);
        assertEquals(20, l3.minimumValue(network, LOAD), 1e-3);
        assertEquals(50, load.getP0(), 1e-3);

        assertEquals(30, l3.scale(network, 50, convention), 1e-3);
        assertEquals(20, load.getP0(), 1e-3);

        l3.reset(network);
        //test with p0 outside interval
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(0, l3.scale(network, -40, convention), 1e-3);

        //test LoadScalable with negative minValue
        l4.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(10, l4.scale(network, 20, convention), 1e-3);
        assertEquals(-10, load.getP0(), 1e-3);

    }

    @Test
    public void testLoadScaleLoadConvention() {

        //test with ScalingConvention.LOAD
        convention = LOAD;

        //test with default maxValue = Double.MAX_VALUE and minValue = 0
        Load load = network.getLoad("l1");
        assertEquals(100, load.getP0(), 1e-3);
        assertEquals(20, l1.scale(network, 20, convention), 1e-3);
        assertEquals(120, load.getP0(), 1e-3);
        assertEquals(-40, l1.scale(network, -40, convention), 1e-3);
        assertEquals(80, load.getP0(), 1e-3);

        //test minValue = 0
        assertEquals(-80, l1.scale(network, -140, convention), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);

        //test with a maximum value
        l2.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(0, l2.scale(network, -40, convention), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(110, l2.scale(network, 120, convention), 1e-3);
        assertEquals(110, load.getP0(), 1e-3);
        assertEquals(-80, l2.scale(network, -80, convention), 1e-3);
        assertEquals(30, load.getP0(), 1e-3);

        //test with minValue = 20
        assertEquals(-10, l3.scale(network, -30, convention), 1e-3);
        assertEquals(20, load.getP0(), 1e-3);

        l3.reset(network);
        //test with p0 outside interval
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(0, l3.scale(network, -40, convention), 1e-3);

        //test LoadScalable with negative minValue
        l4.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(-10, l4.scale(network, -20, convention), 1e-3);
        assertEquals(-10, load.getP0(), 1e-3);
    }
}
