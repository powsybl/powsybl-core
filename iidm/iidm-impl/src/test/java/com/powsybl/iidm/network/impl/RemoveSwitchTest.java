/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkBusBreakerTest1Factory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class RemoveSwitchTest {

    private static boolean checkNodes(Switch sw) {
        try {
            int node1 = sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
            int node2 = sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId());
            assertTrue(node1 >= 0);
            assertTrue(node2 >= 0);
            return true;
        } catch (PowsyblException x) {
            return false;
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
            return false;
        }
    }

    @Test
    void nodeBreakerRemoveSwitchTest() {
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
    void nodeBreakerRemoveVoltageLevelTest() {
        Network network = NetworkTest1Factory.create();
        Switch sw = network.getSwitch("load1Breaker1");
        checkNodes(sw);

        // We will remove the voltage level completely,
        // we have to check nodes for all switches
        Boolean[] nodesVerified = new Boolean[1];
        // Explicitly consider we have not been able to check any switch
        nodesVerified[0] = null;
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void beforeRemoval(Identifiable identifiable) {
                if (identifiable instanceof Switch) {
                    boolean previous = nodesVerified[0] == null ? true : nodesVerified[0];
                    nodesVerified[0] = previous && checkNodes((Switch) identifiable);
                }
            }
        });
        sw.getVoltageLevel().remove();
        if (nodesVerified[0] == null || !nodesVerified[0]) {
            fail();
        }
    }

    @Test
    void busBreakerRemoveSwitchTest() {
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

    @Test
    void busBreakerRemoveVoltageLevelTest() {
        Network network = NetworkBusBreakerTest1Factory.create();
        Switch sw = network.getSwitch("voltageLevel1Breaker1");
        checkBuses(sw);
        Boolean[] busesVerified = new Boolean[1];
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void beforeRemoval(Identifiable identifiable) {
                if (identifiable instanceof Switch) {
                    boolean previous = busesVerified[0] == null ? true : busesVerified[0];
                    busesVerified[0] = previous && checkBuses((Switch) identifiable);
                }
            }
        });
        sw.getVoltageLevel().remove();
        if (busesVerified[0] == null || !busesVerified[0]) {
            fail();
        }
    }
}
