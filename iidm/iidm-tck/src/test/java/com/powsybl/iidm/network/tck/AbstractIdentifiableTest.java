/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractIdentifiableTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        // Test updating id
        Load load = network.getLoad("LOAD");
        assertEquals("LOAD", load.getId());
        load.setId("NEW_LOAD_ID");
        assertEquals(load.getId(), network.getIdentifiable("NEW_LOAD_ID").getId());
        assertEquals("NEW_LOAD_ID", load.getId());

        // Test setting an already used id
        Generator gen = network.getGenerator("GEN");
        assertThrows(PowsyblException.class, () -> gen.setId("NEW_LOAD_ID"), "Object with id (NEW_LOAD_ID) already exists");

        // Test setting same ID
        load.setId("NEW_LOAD_ID");
        assertEquals("NEW_LOAD_ID", load.getId());

        // Test nothing is returned when using old id
        assertNull(network.getIdentifiable("LOAD"));
    }

    @Test
    public void testSetIdBusesBusBreaker() {
        Network network = EurostagTutorialExample1Factory.create();
        int count = 0;
        for (Bus bus : network.getBusBreakerView().getBusStream().toList()) {
            String newId = String.valueOf(count++);
            bus.setId(newId);
            assertEquals(newId, bus.getId());
        }
    }

    @Test
    public void testSetIdBusesNodeBreaker() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        int count = 0;
        for (Bus bus : network.getBusBreakerView().getBusStream().toList()) {
            bus.setId(String.valueOf(count++));
        }
    }

    @Test
    public void testSetIdBusUpdatesConnectableBusOfTerminals() {
        Network network = EurostagTutorialExample1Factory.create();
        Bus nhv1 = network.getBusBreakerView().getBus("NHV1");
        List<String> connectedEquipmentIds = nhv1.getConnectedTerminalStream()
                .map(t -> t.getConnectable().getId())
                .sorted()
                .toList();
        assertTrue(connectedEquipmentIds.size() > 1, "NHV1 is expected to have several terminals connected");

        nhv1.setId("NEW_NHV1");

        assertNull(network.getBusBreakerView().getBus("NHV1"));
        Bus renamed = network.getBusBreakerView().getBus("NEW_NHV1");
        assertNotNull(renamed);
        assertEquals(connectedEquipmentIds, renamed.getConnectedTerminalStream()
                .map(t -> t.getConnectable().getId())
                .sorted()
                .toList());
        renamed.getConnectedTerminalStream().forEach(t ->
                assertEquals("NEW_NHV1", t.getBusBreakerView().getConnectableBus().getId()));
    }

    @Test
    public void testSetIdBusAlreadyExists() {
        Network network = EurostagTutorialExample1Factory.create();
        Bus nhv1 = network.getBusBreakerView().getBus("NHV1");
        Exception e = assertThrows(PowsyblException.class, () -> nhv1.setId("NHV2"));
        assertEquals("Object with id (NHV2) already exists", e.getMessage());
    }

    @Test
    public void testSetIdSwitch() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VoltageLevel vl = network.getVoltageLevel("S1VL1");
        Switch sw = vl.getNodeBreakerView().getSwitchStream().findFirst().orElseThrow();
        String oldId = sw.getId();
        int node1 = vl.getNodeBreakerView().getNode1(oldId);
        int node2 = vl.getNodeBreakerView().getNode2(oldId);
        boolean open = sw.isOpen();

        sw.setId("NEW_SWITCH_ID");

        assertNull(vl.getNodeBreakerView().getSwitch(oldId));
        assertSame(sw, vl.getNodeBreakerView().getSwitch("NEW_SWITCH_ID"));
        assertEquals(node1, vl.getNodeBreakerView().getNode1("NEW_SWITCH_ID"));
        assertEquals(node2, vl.getNodeBreakerView().getNode2("NEW_SWITCH_ID"));
        sw.setOpen(!open);
        assertEquals(!open, vl.getNodeBreakerView().getSwitch("NEW_SWITCH_ID").isOpen());
    }

    @Test
    public void testSetIdSwitchAlreadyExists() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        VoltageLevel vl = network.getVoltageLevel("S1VL1");
        List<Switch> switches = vl.getNodeBreakerView().getSwitchStream().toList();
        assertTrue(switches.size() > 1, "test network must have at least two switches in S1VL1");
        Exception e = assertThrows(PowsyblException.class, () -> switches.get(0).setId(switches.get(1).getId()));
        assertEquals("Object with id (S1VL1_LD1_BREAKER) already exists", e.getMessage());
    }
}
