/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.test.TwoVoltageLevelNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractGroundTest {
    private Network network;

    @BeforeEach
    public void initNetwork() {
        // Network initialisation
        network = TwoVoltageLevelNetworkFactory.create();
    }

    @Test
    public void test() {
        // Get the voltage levels
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        // Create disconnector and ground element in node-breaker view
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_1_6")
            .setKind(SwitchKind.DISCONNECTOR)
            .setOpen(false)
            .setNode1(1)
            .setNode2(6)
            .add();
        Ground groundNB = createGroundNodeBreaker(vl1, "GroundNB", 6, true);

        // Create ground in bus-breaker view
        Ground groundBB = createGroundBusBreaker(vl2, "GroundBB", "BUS2", true);

        // List of grounds
        List<Ground> groundList = List.of(groundNB, groundBB);
        List<Ground> groundListVl1 = Collections.singletonList(groundNB);

        // Test getters
        assertEquals(IdentifiableType.GROUND, groundNB.getType());
        assertEquals(IdentifiableType.GROUND, groundBB.getType());
        assertEquals(vl1, groundNB.getTerminal().getVoltageLevel());
        assertEquals(vl2, groundBB.getTerminal().getVoltageLevel());
        assertEquals("GroundNB", groundNB.getId());
        assertEquals("GroundBB", groundBB.getId());
        assertEquals(network, groundNB.getNetwork());
        assertEquals(network, groundBB.getNetwork());
        assertEquals(network, groundNB.getParentNetwork());
        assertEquals(network, groundBB.getParentNetwork());
        assertEquals(2, network.getIdentifiableStream(IdentifiableType.GROUND).count());
        network.getIdentifiableStream(IdentifiableType.GROUND).forEach(ground -> assertTrue(groundList.contains((Ground) ground)));
        assertEquals(groundNB, network.getGround("GroundNB"));
        assertEquals(2, ((Collection<?>) network.getGrounds()).size());
        network.getGrounds().forEach(ground -> assertTrue(groundList.contains(ground)));
        network.getGroundStream().forEach(ground -> assertTrue(groundList.contains(ground)));
        assertEquals(2, network.getGroundCount());
        assertNull(network.getGround("GroundNB2"));
        assertEquals(1, ((Collection<?>) vl1.getGrounds()).size());
        vl1.getGrounds().forEach(ground -> assertTrue(groundListVl1.contains(ground)));
        vl1.getGroundStream().forEach(ground -> assertTrue(groundListVl1.contains(ground)));
        assertEquals(1, vl1.getGroundCount());
    }

    @Test
    public void testOnSubnetwork() {
        // Subnetwork creation
        Network subnetwork1 = createSubnetwork(network, "Sub1", "Sub1", "format1");
        Network subnetwork2 = createSubnetwork(network, "Sub2", "Sub2", "format2");

        // Get the voltage levels
        VoltageLevel vl1Sub1 = subnetwork1.getVoltageLevel("Sub1_VL1");
        VoltageLevel vl2Sub1 = subnetwork1.getVoltageLevel("Sub1_VL2");

        // Create disconnector and ground element in node-breaker view
        vl1Sub1.getNodeBreakerView().newDisconnector()
            .setId("D_1_6")
            .setKind(SwitchKind.DISCONNECTOR)
            .setOpen(false)
            .setNode1(1)
            .setNode2(6)
            .add();
        vl1Sub1.getNodeBreakerView().newDisconnector()
            .setId("D_2_7")
            .setKind(SwitchKind.DISCONNECTOR)
            .setOpen(false)
            .setNode1(2)
            .setNode2(7)
            .add();
        Ground groundNBSub16 = createGroundNodeBreaker(vl1Sub1, "GroundNBSub1_6", 6, true);
        Ground groundNBSub17 = createGroundNodeBreaker(vl1Sub1, "GroundNBSub1_7", 7, true);

        // Create ground in bus-breaker view
        Ground groundBBSub12 = createGroundBusBreaker(vl2Sub1, "GroundBBSub1_2", "Sub1_BUS2", true);

        // List of grounds
        List<Ground> groundList = List.of(groundNBSub16, groundNBSub17, groundBBSub12);

        // Assertions
        assertEquals(groundNBSub16, subnetwork1.getGround("GroundNBSub1_6"));
        assertEquals(3, ((List<?>) subnetwork1.getGrounds()).size());
        subnetwork1.getGrounds().forEach(ground -> assertTrue(groundList.contains(ground)));
        assertEquals(3, subnetwork1.getGroundStream().count());
        subnetwork1.getGroundStream().forEach(ground -> assertTrue(groundList.contains(ground)));
        assertEquals(3, subnetwork1.getGroundCount());
        assertNull(subnetwork2.getGround("GroundNB"));
    }

    @Test
    public void testCreateSameId() {
        // Get the voltage level
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        // Create first grounds
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_1_6")
            .setKind(SwitchKind.DISCONNECTOR)
            .setOpen(false)
            .setNode1(1)
            .setNode2(6)
            .add();
        createGroundNodeBreaker(vl1, "Ground", 6, false);
        createGroundBusBreaker(vl2, "Ground1", "BUS1", false);

        // Create a second with the same ID
        PowsyblException exception = assertThrows(PowsyblException.class, () -> createGroundNodeBreaker(vl1, "Ground", 7, false));
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'Ground'").matcher(exception.getMessage()).find());
        exception = assertThrows(PowsyblException.class, () -> createGroundBusBreaker(vl2, "Ground1", "BUS1", false));
        assertTrue(Pattern.compile("The network test already contains an object '(\\w+)' with the id 'Ground1'").matcher(exception.getMessage()).find());
        // Nota : the class name is undefined here since it will depend on the implementation of the Ground interface
    }

    @Test
    public void testFictitiousGround() {
        // Get the voltage level
        VoltageLevel vl1 = network.getVoltageLevel("VL1");

        // Create first grounds
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_1_6")
            .setKind(SwitchKind.DISCONNECTOR)
            .setOpen(false)
            .setNode1(1)
            .setNode2(6)
            .add();
        Ground ground = createGroundNodeBreaker(vl1, "Ground", 7, false);

        // Ground cannot be fictitious
        ground.setFictitious(false);
        assertFalse(ground.isFictitious());

        // Create a second with the same ID
        PowsyblException exception = assertThrows(PowsyblException.class, () -> ground.setFictitious(true));
        assertTrue(Pattern.compile("The ground cannot be fictitious.").matcher(exception.getMessage()).find());
    }

    @Test
    public void testCreationError() {
        // Get the voltage level
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        // Create first grounds
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_1_6")
            .setKind(SwitchKind.DISCONNECTOR)
            .setOpen(false)
            .setNode1(1)
            .setNode2(6)
            .add();
        // Create a ground in NB
        GroundAdder groundAdderNB = vl1.newGround()
            .setId("Ground")
            .setEnsureIdUnicity(true);
        ValidationException exception = assertThrows(ValidationException.class, groundAdderNB::add);
        assertEquals("Ground 'Ground': connectable bus is not set", exception.getMessage());
        groundAdderNB = vl1.newGround()
            .setNode(6)
            .setEnsureIdUnicity(true);
        PowsyblException powsyblException = assertThrows(PowsyblException.class, groundAdderNB::add);
        assertEquals("Ground id is not set", powsyblException.getMessage());

        // Create a ground in BB
        GroundAdder groundAdderBB = vl2.newGround()
            .setId("Ground")
            .setEnsureIdUnicity(true);
        exception = assertThrows(ValidationException.class, groundAdderBB::add);
        assertEquals("Ground 'Ground': connectable bus is not set", exception.getMessage());
        groundAdderBB = vl2.newGround()
            .setBus("BUS1")
            .setEnsureIdUnicity(true);
        powsyblException = assertThrows(PowsyblException.class, groundAdderBB::add);
        assertEquals("Ground id is not set", powsyblException.getMessage());
    }

    private Ground createGroundNodeBreaker(VoltageLevel voltageLevel, String id, int node, boolean ensureIdUnicity) {
        return voltageLevel.newGround()
            .setId(id)
            .setNode(node)
            .setEnsureIdUnicity(ensureIdUnicity)
            .add();
    }

    private Ground createGroundBusBreaker(VoltageLevel voltageLevel, String id, String bus, boolean ensureIdUnicity) {
        return voltageLevel.newGround()
            .setId(id)
            .setBus(bus)
            .setEnsureIdUnicity(ensureIdUnicity)
            .add();
    }

    private Network createSubnetwork(Network network, String subnetworkId, String name, String sourceFormat) {
        // Subnetwork creation
        Network subnetwork = network.createSubnetwork(subnetworkId, name, sourceFormat);

        // Substation
        Substation substation = subnetwork.newSubstation()
            .setId(subnetworkId + "_S")
            .setCountry(Country.FR)
            .add();

        // Voltage levels
        VoltageLevel vl1 = substation.newVoltageLevel()
            .setId(subnetworkId + "_VL1")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl2 = substation.newVoltageLevel()
            .setId(subnetworkId + "_VL2")
            .setNominalV(220.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        // Buses and Busbar sections
        BusbarSection bbs1 = vl1.getNodeBreakerView().newBusbarSection()
            .setId(subnetworkId + "_BBS1")
            .setNode(0)
            .add();
        bbs1.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs2 = vl1.getNodeBreakerView().newBusbarSection()
            .setId(subnetworkId + "_BBS2")
            .setNode(1)
            .add();
        bbs2.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(1)
            .add();
        vl2.getBusBreakerView().newBus()
            .setId(subnetworkId + "_BUS1")
            .add();
        vl2.getBusBreakerView().newBus()
            .setId(subnetworkId + "_BUS2")
            .add();

        return subnetwork;
    }
}
