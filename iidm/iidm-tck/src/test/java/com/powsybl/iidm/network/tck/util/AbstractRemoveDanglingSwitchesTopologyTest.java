/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractRemoveDanglingSwitchesTopologyTest {

    private final List<String> removedObjects = new ArrayList<>();

    @After
    public void tearDown() {
        removedObjects.clear();
    }

    private void addListener(Network network) {
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void afterRemoval(String id) {
                removedObjects.add(id);
            }
        });
    }

    @Test
    public void testRemove() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        addListener(network);
        Load ld1 = network.getLoad("LD1");
        ld1.remove();
        assertEquals(List.of("LD1"), removedObjects);
    }

    @Test
    public void testRemoveAndClean() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        addListener(network);
        Load ld1 = network.getLoad("LD1");
        ld1.remove(true);
        assertEquals(List.of("S1VL1_LD1_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "LD1"), removedObjects);
    }

    /**
     *   L(1)   G(2)
     *   |       |
     *   B1      B2
     *   |       |
     *   ----3----
     *       |
     *       D
     *       |
     *       BBS(0)
     */
    private static Network createNetworkWithForkFeeder() {
        Network network = Network.create("test", "test");
        VoltageLevel vl = network.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        vl.newLoad()
                .setId("LD")
                .setNode(1)
                .setP0(0)
                .setQ0(0)
                .add();
        vl.newGenerator()
                .setId("G")
                .setNode(2)
                .setTargetP(0)
                .setVoltageRegulatorOn(true)
                .setTargetV(400)
                .setMinP(0)
                .setMaxP(10)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D")
                .setNode1(0)
                .setNode2(3)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B1")
                .setNode1(1)
                .setNode2(3)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B2")
                .setNode1(2)
                .setNode2(3)
                .add();
        return network;
    }

    @Test
    public void testRemoveAndCleanWithForkFeeder() {
        Network network = createNetworkWithForkFeeder();
        addListener(network);
        Load ld = network.getLoad("LD");
        ld.remove(true);
        assertEquals(List.of("B1", "LD"), removedObjects);
        assertNull(network.getLoad("LD"));
        assertNull(network.getSwitch("B1"));
        assertNotNull(network.getGenerator("G"));
        assertNotNull(network.getBusbarSection("BBS"));
        assertNotNull(network.getSwitch("B2"));
        assertNotNull(network.getSwitch("D"));
    }
}
