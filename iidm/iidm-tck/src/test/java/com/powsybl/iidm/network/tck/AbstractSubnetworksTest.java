/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public abstract class AbstractSubnetworksTest {

    private Network network;
    private Network subnetwork1;
    private Network subnetwork2;

    @BeforeEach
    public void setup() {
        network = Network.create("Main", "format0");
        subnetwork1 = network.createSubnetwork("Sub1", "Sub1", "format1");
        subnetwork2 = network.createSubnetwork("Sub2", "Sub2", "format2");
    }

    @Test
    public void testSubstationCreation() {
        // On main network level
        addSubstation(network, "s0");
        assertSubstationCounts(1, 0, 0);

        // On subnetwork level
        addSubstation(subnetwork1, "s1");
        assertSubstationCounts(2, 1, 0);
    }

    @Test
    public void testVoltageLevelCreation() {
        addSubstation(network, "s0");
        addSubstation(subnetwork1, "s1");
        addSubstation(subnetwork2, "s2");
        assertSubstationCounts(3, 1, 1);

        // On a substation: main network level
        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_0");
        assertVoltageLevelCounts(1, 0, 0);
        assertVoltageLevelNetworks(network, "vl0_0");

        // On a substation: subnetwork level
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_0");
        assertVoltageLevelCounts(2, 1, 0);
        assertVoltageLevelNetworks(subnetwork1, "vl1_0");

        // On network: main network level
        addVoltageLevel(network.newVoltageLevel(), "vl0_1");
        assertVoltageLevelCounts(3, 1, 0);
        assertVoltageLevelNetworks(network, "vl0_1");

        // On network: subnetwork level
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_0");
        assertVoltageLevelCounts(4, 1, 1);
        assertVoltageLevelNetworks(subnetwork2, "vl2_0");

        // Detach all
        assertTrue(subnetwork1.isDetachable());
        assertTrue(subnetwork2.isDetachable());
        Network n1 = subnetwork1.detach();
        Network n2 = subnetwork2.detach();
        // - Check substations
        assertEquals(1, network.getSubstationCount());
        assertEquals(1, n1.getSubstationCount());
        assertEquals(1, n2.getSubstationCount());
        // - check voltage levels
        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(1, n1.getVoltageLevelCount());
        assertEquals(1, n2.getVoltageLevelCount());
        assertNetworks(network, network, network.getVoltageLevel("vl0_0"));
        assertNetworks(n1, n1, n1.getVoltageLevel("vl1_0"));
        assertNetworks(network, network, network.getVoltageLevel("vl0_1"));
        assertNetworks(n2, n2, n2.getVoltageLevel("vl2_0"));
    }

    @Test
    public void testBranchCreation() {
        addSubstation(network, "s0");
        addSubstation(subnetwork1, "s1");
        addSubstation(subnetwork2, "s2");

        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_0");
        addVoltageLevel(network.newVoltageLevel(), "vl0_1");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_0");
        addVoltageLevel(subnetwork1.newVoltageLevel(), "vl1_1");
        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), "vl2_0");
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_1");

        // On main network, voltage levels both in main network
        addLine(network, "l0", "vl0_0", "vl0_1");

        // On main network, voltage levels both in subnetwork1
        addLine(network, "l1", "vl1_0", "vl1_1");

        // On subnetwork1, voltage levels both in subnetwork2 => should fail
        PowsyblException e = assertThrows(ValidationException.class, () -> addLine(subnetwork1, "l2", "vl2_0", "vl2_1"));
        assertTrue(e.getMessage().contains("Create this line from the parent network"));

        // On subnetwork2, voltage levels both in subnetwork2
        Line l2 = addLine(subnetwork2, "l2", "vl2_0", "vl2_1");

        // On main network, voltage levels in different subnetworks
        Line l3 = addLine(network, "l3", "vl1_0", "vl2_0");

        // On subnetwork2, voltage levels in main network and subnetwork2 => should fail
        e = assertThrows(ValidationException.class, () -> addLine(subnetwork2, "l4", "vl0_0", "vl2_0"));
        assertTrue(e.getMessage().contains("Create this line from the parent network"));

        // On main network, voltage levels in main network and subnetwork2
        Line l4 = addLine(network, "l4", "vl0_0", "vl2_0");

        // Try to detach all. Some elements prevent it.
        assertFalse(subnetwork1.isDetachable());
        assertFalse(subnetwork2.isDetachable());
        assertBoundaryElements(subnetwork1, "l3");
        assertBoundaryElements(subnetwork2, "l3", "l4");
        assertTrue(subnetwork1.isBoundaryElement(l3));
        assertFalse(network.isBoundaryElement(l3));
        assertTrue(subnetwork2.isBoundaryElement(l4));
        assertFalse(network.isBoundaryElement(l4));
        assertFalse(subnetwork2.isBoundaryElement(l2));
        assertFalse(network.isBoundaryElement(l2));
        // Remove problematic elements
        l3.remove();
        l4.remove();

        // Detach all
        assertTrue(subnetwork1.isDetachable());
        assertTrue(subnetwork2.isDetachable());
        Network n1 = subnetwork1.detach();
        Network n2 = subnetwork2.detach();
        // - Check Lines
        assertEquals(1, network.getLineCount());
        assertEquals(1, n1.getLineCount());
        assertEquals(1, n2.getLineCount());
        assertNetworks(network, network, network.getLine("l0"));
        assertNetworks(n1, n1, n1.getLine("l1"));
        assertNetworks(n2, n2, n2.getLine("l2"));

        checkIndexNetworks(network);
        checkIndexNetworks(n1);
        checkIndexNetworks(n2);
    }

    private Substation addSubstation(Network network, String substationId) {
        return network.newSubstation()
                .setId(substationId)
                .setCountry(Country.AQ)
                .add();
    }

    private VoltageLevel addVoltageLevel(VoltageLevelAdder adder, String id) {
        VoltageLevel voltageLevel = adder.setId(id)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("bus_" + id)
                .add();
        return voltageLevel;
    }

    private Line addLine(Network network, String id, String vl1, String vl2) {
        return network.newLine()
                .setId(id)
                .setVoltageLevel1(vl1).setBus1("bus_" + vl1)
                .setVoltageLevel2(vl2).setBus2("bus_" + vl2)
                .setR(1.0).setX(1.0).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0)
                .add();
    }

    void assertSubstationCounts(int total, int onSubnetwork1, int onSubnetwork2) {
        assertEquals(total, network.getSubstationCount());
        assertEquals(onSubnetwork1, subnetwork1.getSubstationCount());
        assertEquals(onSubnetwork2, subnetwork2.getSubstationCount());
    }

    void assertVoltageLevelCounts(int total, int onSubnetwork1, int onSubnetwork2) {
        assertEquals(total, network.getVoltageLevelCount());
        assertEquals(onSubnetwork1, subnetwork1.getVoltageLevelCount());
        assertEquals(onSubnetwork2, subnetwork2.getVoltageLevelCount());
    }

    void assertVoltageLevelNetworks(Network expectedParentNetwork, String id) {
        assertNetworks(network, expectedParentNetwork, network.getVoltageLevel(id));
    }

    void checkIndexNetworks(Network independentNetwork) {
        independentNetwork.getIdentifiables().forEach(i -> assertNetworks(independentNetwork, independentNetwork, i));
    }

    void assertNetworks(Network expectedNetwork, Network expectedParentNetwork, Identifiable<?> identifiable) {
        assertEquals(expectedNetwork, identifiable.getNetwork());
        assertEquals(expectedParentNetwork, identifiable.getParentNetwork());
    }

    void assertBoundaryElements(Network subnetwork2, String... expectedBoundaryElementIds) {
        assertArrayEquals(expectedBoundaryElementIds,
                subnetwork2.getBoundaryElements().stream().map(Identifiable::getId).sorted().toArray());
    }

}
