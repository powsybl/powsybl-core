/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.GENERATOR;
import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.LOAD;
import static com.powsybl.iidm.modification.scalable.ScalableTestNetwork.createNetworkWithDanglingLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class DanglingLineScalableTest {

    private Network network;
    private Scalable dl2;
    private Scalable dl3;
    private Scalable dl4;
    private Scalable dl5;
    private Scalable dl6;
    private ScalingConvention convention;

    @BeforeEach
    void setUp() {
        network = createNetworkWithDanglingLine();
        dl2 = Scalable.onDanglingLine("dl2");

        dl3 = new DanglingLineScalable("dl2", 20, 100);
        dl4 = new DanglingLineScalable("dl2", -10, 100);

        dl5 = Scalable.onDanglingLine("dl2", ScalingConvention.LOAD);
        dl6 = Scalable.onDanglingLine("dl2", 20, 100, ScalingConvention.LOAD);
    }

    @Test
    void testConstructorThrowWhenIdIsNull() {
        assertThrows(NullPointerException.class, () -> new DanglingLineScalable(null));
    }

    @Test
    void testConstructorInvalidP() {
        assertThrows(PowsyblException.class, () -> new DanglingLineScalable("dl2", 10, 0));
    }

    @Test
    void testInitialValue() {
        assertEquals(0, dl2.initialValue(network), 1e-3);
    }

    @Test
    void testMaximumlValue() {
        assertEquals(Double.MAX_VALUE, dl2.maximumValue(network, LOAD), 0.);
        assertEquals(-20, dl3.maximumValue(network), 0.);
        assertEquals(-20, dl3.maximumValue(network, GENERATOR), 0.);
        assertEquals(100, dl3.maximumValue(network, LOAD), 0.);
        assertEquals(Double.MAX_VALUE, dl5.maximumValue(network), 0.);
        assertEquals(100, dl6.maximumValue(network), 0.);
    }

    @Test
    void testMinimumValue() {
        assertEquals(-Double.MAX_VALUE, dl2.minimumValue(network, LOAD), 0.);
        assertEquals(-100, dl3.minimumValue(network), 0.);
        assertEquals(-100, dl3.minimumValue(network, GENERATOR), 0.);
        assertEquals(20, dl3.minimumValue(network, LOAD), 0.);
        assertEquals(-Double.MAX_VALUE, dl5.minimumValue(network), 0.);
        assertEquals(20, dl6.minimumValue(network), 0.);
    }

    @Test
    void testDanglingLineScaleLoadConvention() {
        //test with ScalingConvention.LOAD
        convention = LOAD;
        ScalingParameters parameters = new ScalingParameters().setScalingConvention(convention);

        //test with default maxValue = Double.MAX_VALUE and minValue = Double.MIN_VALUE
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        assertEquals(50, danglingLine.getP0(), 1e-3);
        assertEquals(20, dl2.scale(network, 20, parameters), 1e-3);
        assertEquals(70, danglingLine.getP0(), 1e-3);
        assertEquals(-40, dl2.scale(network, -40, parameters), 1e-3);
        assertEquals(30, danglingLine.getP0(), 1e-3);
    }

    @Test
    void testDanglingLineScaleGeneratorConvention() {
        //test with ScalingConvention.GENERATOR
        //test with default maxValue = Double.MAX_VALUE and minValue = -Double.MAX_VALUE
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        assertEquals(50.0, danglingLine.getP0(), 1e-3);
        assertEquals(20, dl2.scale(network, 20), 1e-3);
        assertEquals(30.0, danglingLine.getP0(), 1e-3);
        assertEquals(-40, dl2.scale(network, -40), 1e-3);
        assertEquals(70.0, danglingLine.getP0(), 1e-3);

        //test with minValue = 20
        assertEquals(100, dl3.maximumValue(network, LOAD), 1e-3);
        assertEquals(20, dl3.minimumValue(network, LOAD), 1e-3);
        assertEquals(70, danglingLine.getP0(), 1e-3);

        assertEquals(50, dl3.scale(network, 70), 1e-3);
        assertEquals(20, danglingLine.getP0(), 1e-3);

        dl3.reset(network);
        //test with p0 outside interval
        assertEquals(0, danglingLine.getP0(), 1e-3);
        assertEquals(0, dl3.scale(network, -40), 1e-3);

        //test DanglingLieScalable with negative minValue
        dl4.reset(network);
        assertEquals(0, danglingLine.getP0(), 1e-3);
        assertEquals(10, dl4.scale(network, 20), 1e-3);
        assertEquals(-10, danglingLine.getP0(), 1e-3);

        //test with a maximum value
        dl4.reset(network);
        assertEquals(0, danglingLine.getP0(), 1e-3);
        assertEquals(-40, dl4.scale(network, -40), 1e-3);
        assertEquals(40, danglingLine.getP0(), 1e-3);
        assertEquals(-60, dl4.scale(network, -80), 1e-3);
        assertEquals(100, danglingLine.getP0(), 1e-3);
    }

    @Test
    void testFilterInjections() {
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        List<Injection> injections = dl2.filterInjections(network);
        assertEquals(1, injections.size());
        assertSame(danglingLine, injections.get(0));
    }

    @Test
    void testReconnectDanglingLine() {
        DanglingLine danglingLine = network.getDanglingLine("dl2");
        danglingLine.getTerminal().disconnect();
        assertFalse(danglingLine.getTerminal().isConnected());

        // Load convention
        convention = LOAD;
        assertEquals(50, danglingLine.getP0(), 1e-3);
        ScalingParameters parameters = new ScalingParameters().setScalingConvention(convention).setReconnect(true);
        assertEquals(20, dl2.scale(network, 20, parameters), 1e-3);
        assertEquals(70, danglingLine.getP0(), 1e-3);
        assertTrue(danglingLine.getTerminal().isConnected());

        // Generator convention
        danglingLine.getTerminal().disconnect();
        assertFalse(danglingLine.getTerminal().isConnected());
        convention = GENERATOR;
        ScalingParameters parameters2 = new ScalingParameters().setScalingConvention(convention).setReconnect(true);
        assertEquals(20, dl2.scale(network, 20, parameters2), 1e-3);
        assertEquals(50.0, danglingLine.getP0(), 1e-3);
        assertTrue(danglingLine.getTerminal().isConnected());

        //reconnect to false
        danglingLine.getTerminal().disconnect();
        assertFalse(danglingLine.getTerminal().isConnected());
        assertEquals(0.0, dl2.scale(network, 10));
        assertEquals(50.0, danglingLine.getP0(), 1e-3);
    }

}
