/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkBusBreakerTest1Factory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class RemoveSwitchTest {

    private static boolean checkNodes(Switch sw) {
        try {
            int node1 = sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
            int node2 = sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
            assertTrue(node1 >= 0);
            assertTrue(node2 >= 0);
            return true;
        } catch (PowsyblException x) {
            throw x;
            // return false;
        }
    }

    private static boolean checkBuses(Switch sw) {
        try {
            Bus bus1 = sw.getVoltageLevel().getBusBreakerView().getBus1(sw.getId());
            Bus bus2 = sw.getVoltageLevel().getBusBreakerView().getBus2(sw.getId());
            assertNotNull(bus1);
            assertNotNull(bus2);
            return true;
        } catch (PowsyblException x) {
            throw x;
            // return false;
        }
    }

    @Test
    public void nodeBreakerTest() {
        Network network = NetworkTest1Factory.create();
        Switch sw = network.getSwitch("load1Breaker1");
        checkNodes(sw);

        // The handling of beforeRemoval listener methods catches Throwable exceptions
        // The only way to make the test fail is to save a flag (nodesVerified)
        // and invoke fail() outside the listener

        boolean[] nodesVerified = new boolean[1];
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void beforeRemoval(Identifiable identifiable) {
                nodesVerified[0] = checkNodes((Switch) identifiable);
            }
        });
        sw.getVoltageLevel().getNodeBreakerView().removeSwitch(sw.getId());
        if (!nodesVerified[0]) {
            fail();
        }
    }

    @Test
    public void busBreakerTest() {
        Network network = NetworkBusBreakerTest1Factory.create();
        Switch sw = network.getSwitch("voltageLevel1Breaker1");
        checkBuses(sw);
        boolean[] busesVerified = new boolean[1];
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void beforeRemoval(Identifiable identifiable) {
                busesVerified[0] = checkBuses((Switch) identifiable);
            }
        });
        sw.getVoltageLevel().getBusBreakerView().removeSwitch(sw.getId());
        if (!busesVerified[0]) {
            fail();
        }
    }
}
