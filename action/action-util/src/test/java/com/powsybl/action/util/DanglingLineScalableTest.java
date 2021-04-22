/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.action.util.Scalable.ScalingConvention;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.powsybl.action.util.Scalable.ScalingConvention.GENERATOR;
import static com.powsybl.action.util.Scalable.ScalingConvention.LOAD;
import static com.powsybl.action.util.ScalableTestNetwork.createNetworkWithDanglingLine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public class DanglingLineScalableTest {

    private Network network;
    private Scalable dl2;
    private Scalable dl3;
    private Scalable dl4;
    private Scalable dl5;
    private Scalable dl6;
    private ScalingConvention convention;

    @Before
    public void setUp() {
        network = createNetworkWithDanglingLine();
        dl2 = Scalable.onDanglingLine("dl2");

        dl3 = new DanglingLineScalable("dl2", 20, 100);
        dl4 = new DanglingLineScalable("dl2", -10, 100);

        dl5 = new DanglingLineScalable("dl2", ScalingConvention.LOAD);
        dl6 = new DanglingLineScalable("dl2", 20, 100, ScalingConvention.LOAD);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorThrowWhenIdIsNull() {
        new DanglingLineScalable(null);
    }

    @Test(expected = PowsyblException.class)
    public void testConstructorInvalidP() {
        new DanglingLineScalable("dl2", 10, 0);
    }

    @Test
    public void testInitialValue() {
        assertEquals(0, dl2.initialValue(network), 1e-3);
    }

    @Test
    public void testMaximumlValue() {
        assertEquals(Double.MAX_VALUE, dl2.maximumValue(network, LOAD), 0.);
        assertEquals(-20, dl3.maximumValue(network), 0.);
        assertEquals(-20, dl3.maximumValue(network, GENERATOR), 0.);
        assertEquals(100, dl3.maximumValue(network, LOAD), 0.);
        assertEquals(Double.MAX_VALUE, dl5.maximumValue(network), 0.);
        assertEquals(100, dl6.maximumValue(network), 0.);
    }

    @Test
    public void testMinimumlValue() {
        assertEquals(-Double.MAX_VALUE, dl2.minimumValue(network, LOAD), 0.);
        assertEquals(-100, dl3.minimumValue(network), 0.);
        assertEquals(-100, dl3.minimumValue(network, GENERATOR), 0.);
        assertEquals(20, dl3.minimumValue(network, LOAD), 0.);
        assertEquals(-Double.MAX_VALUE, dl5.minimumValue(network), 0.);
        assertEquals(20, dl6.minimumValue(network), 0.);
    }

    @Test
    public void testDanglingLineScaleLoadConvention() {
        //test with ScalingConvention.LOAD
        convention = LOAD;

        //test with default maxValue = Double.MAX_VALUE and minValue = Double.MIN_VALUE
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        assertEquals(50, danglingLine.getP0(), 1e-3);
        assertEquals(20, dl2.scale(network, 20, convention), 1e-3);
        assertEquals(70, danglingLine.getP0(), 1e-3);
        assertEquals(-40, dl2.scale(network, -40, convention), 1e-3);
        assertEquals(30, danglingLine.getP0(), 1e-3);
    }

    @Test
    public void testDanglingLineScaleGeneratorConvention() {
        //test with ScalingConvention.GENERATOR
        convention = GENERATOR;

        //test with default maxValue = Double.MAX_VALUE and minValue = -Double.MAX_VALUE
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        assertEquals(50.0, danglingLine.getP0(), 1e-3);
        assertEquals(20, dl2.scale(network, 20, convention), 1e-3);
        assertEquals(30.0, danglingLine.getP0(), 1e-3);
        assertEquals(-40, dl2.scale(network, -40, convention), 1e-3);
        assertEquals(70.0, danglingLine.getP0(), 1e-3);

        //test with minValue = 20
        assertEquals(100, dl3.maximumValue(network, LOAD), 1e-3);
        assertEquals(20, dl3.minimumValue(network, LOAD), 1e-3);
        assertEquals(70, danglingLine.getP0(), 1e-3);

        assertEquals(50, dl3.scale(network, 70, convention), 1e-3);
        assertEquals(20, danglingLine.getP0(), 1e-3);

        dl3.reset(network);
        //test with p0 outside interval
        assertEquals(0, danglingLine.getP0(), 1e-3);
        assertEquals(0, dl3.scale(network, -40, convention), 1e-3);

        //test DanglingLieScalable with negative minValue
        dl4.reset(network);
        assertEquals(0, danglingLine.getP0(), 1e-3);
        assertEquals(10, dl4.scale(network, 20, convention), 1e-3);
        assertEquals(-10, danglingLine.getP0(), 1e-3);

        //test with a maximum value
        dl4.reset(network);
        assertEquals(0, danglingLine.getP0(), 1e-3);
        assertEquals(-40, dl4.scale(network, -40, convention), 1e-3);
        assertEquals(40, danglingLine.getP0(), 1e-3);
        assertEquals(-60, dl4.scale(network, -80, convention), 1e-3);
        assertEquals(100, danglingLine.getP0(), 1e-3);
    }

    @Test
    public void testFilterInjections() {
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        List<Injection> injections = dl2.filterInjections(network);
        assertEquals(1, injections.size());
        assertSame(danglingLine, injections.get(0));
    }

}
