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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public abstract class AbstractSubnetworksCreationTest {

    private Network network;
    private Network subnetwork1;
    private Network subnetwork2;

    @BeforeEach
    public void setup() {
        network = Network.create("Root", "format0");
        subnetwork1 = network.createSubnetwork("Sub1", "Sub1", "format1");
        subnetwork2 = network.createSubnetwork("Sub2", "Sub2", "format2");
    }

    @Test
    public void testSubstationCreation() {
        // On root network level
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

        // On a substation: root network level
        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_0");
        assertVoltageLevelCounts(1, 0, 0);
        assertVoltageLevelNetworks(network, "vl0_0");

        // On a substation: subnetwork level
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_0");
        assertVoltageLevelCounts(2, 1, 0);
        assertVoltageLevelNetworks(subnetwork1, "vl1_0");

        // On network: root network level
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
    public void testLineCreation() {
        addSubstation(network, "s0");
        addSubstation(subnetwork1, "s1");
        addSubstation(subnetwork2, "s2");

        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_0");
        addVoltageLevel(network.newVoltageLevel(), "vl0_1");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_0");
        addVoltageLevel(subnetwork1.newVoltageLevel(), "vl1_1");
        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), "vl2_0");
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_1");

        // On root network, voltage levels both in root network
        addLine(network, "l0", "vl0_0", "vl0_1");

        // On root network, voltage levels both in subnetwork1
        addLine(network, "l1", "vl1_0", "vl1_1");

        // On subnetwork2, voltage levels both in subnetwork2
        Line l2 = addLine(subnetwork2, "l2", "vl2_0", "vl2_1");

        // On root network, voltage levels in different subnetworks
        Line l3 = addLine(network, "l3", "vl1_0", "vl2_0");

        // On root network, voltage levels in root network and subnetwork2
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

    @Test
    public void failCreateLineFromSubnetworkBetweenRootAndSubnetwork() {
        addSubstation(network, "s0");
        addSubstation(subnetwork1, "s1");
        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1");

        // On subnetwork1, voltage levels in root network and subnetwork2 => should fail
        Exception e = assertThrows(ValidationException.class, () -> addLine(subnetwork1, "l", "vl0", "vl1"));
        assertTrue(e.getMessage().contains("Create this line from the parent network"));
    }

    @Test
    public void failCreateLineFromASubnetworkInAnother() {
        addSubstation(subnetwork1, "s2");

        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), "vl2_0");
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_1");

        // On subnetwork1, voltage levels both in subnetwork2 => should fail
        PowsyblException e = assertThrows(ValidationException.class, () -> addLine(subnetwork1, "l", "vl2_0", "vl2_1"));
        assertTrue(e.getMessage().contains("Create this line from the parent network"));
    }

    @Test
    public void testTwoWindingsTransformersCreation() {
        addVoltageLevel(network.newVoltageLevel(), "vl0_0");
        addVoltageLevel(network.newVoltageLevel(), 90, "vl0_1");
        addVoltageLevel(subnetwork1.newVoltageLevel(), "vl1_0");
        addVoltageLevel(subnetwork1.newVoltageLevel(), 90, "vl1_1");
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_0");
        addVoltageLevel(subnetwork2.newVoltageLevel(), 90, "vl2_1");

        // On root network, voltage levels both in root network
        addTwoWindingsTransformer(network, "twt0", "vl0_0", 380, "vl0_1", 90);

        // On root network, voltage levels both in subnetwork1
        addTwoWindingsTransformer(network, "twt1", "vl1_0", 380, "vl1_1", 90);

        // On subnetwork2, voltage levels both in subnetwork2
        addTwoWindingsTransformer(subnetwork2, "twt2", "vl2_0", 380, "vl2_1", 90);

        // Detach all
        assertTrue(subnetwork1.isDetachable());
        assertTrue(subnetwork2.isDetachable());
        Network n1 = subnetwork1.detach();
        Network n2 = subnetwork2.detach();
        // - Check Transformers
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, n1.getTwoWindingsTransformerCount());
        assertEquals(1, n2.getTwoWindingsTransformerCount());
        assertNetworks(network, network, network.getTwoWindingsTransformer("twt0"));
        assertNetworks(n1, n1, n1.getTwoWindingsTransformer("twt1"));
        assertNetworks(n2, n2, n2.getTwoWindingsTransformer("twt2"));

        checkIndexNetworks(network);
        checkIndexNetworks(n1);
        checkIndexNetworks(n2);
    }

    @Test
    public void failCreateTwoWindingsTransformerBetweenTwoSubnetworks() {
        addVoltageLevel(subnetwork1.newVoltageLevel(), "vl1_0");
        addSubstation(subnetwork2, "s2");
        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), 90, "vl2_0");

        PowsyblException e = assertThrows(ValidationException.class, () -> addTwoWindingsTransformer(subnetwork1, "twt",
                "vl1_0", 380, "vl2_0", 90));
        assertTrue(e.getMessage().contains("The 2 windings of the transformer shall belong to the same subnetwork"));
    }

    @Test
    public void testThreeWindingsTransformersCreation() {
        addVoltageLevel(network.newVoltageLevel(), "vl0_0");
        addVoltageLevel(network.newVoltageLevel(), 225, "vl0_1");
        addVoltageLevel(network.newVoltageLevel(), 90, "vl0_2");
        addVoltageLevel(subnetwork1.newVoltageLevel(), "vl1_0");
        addVoltageLevel(subnetwork1.newVoltageLevel(), 225, "vl1_1");
        addVoltageLevel(subnetwork1.newVoltageLevel(), 90, "vl1_2");
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_0");
        addVoltageLevel(subnetwork2.newVoltageLevel(), 225, "vl2_1");
        addVoltageLevel(subnetwork2.newVoltageLevel(), 90, "vl2_2");

        // On root network, voltage levels all in root network
        addThreeWindingsTransformer(network, "twt0", "vl0_0", 380, "vl0_1", 225, "vl0_2", 90);

        // On root network, voltage levels all in subnetwork1
        addThreeWindingsTransformer(network, "twt1", "vl1_0", 380, "vl1_1", 225, "vl1_2", 90);

        // On subnetwork2, voltage levels all in subnetwork2
        addThreeWindingsTransformer(subnetwork2, "twt2", "vl2_0", 380, "vl2_1", 225, "vl2_2", 90);

        // Detach all
        assertTrue(subnetwork1.isDetachable());
        assertTrue(subnetwork2.isDetachable());
        Network n1 = subnetwork1.detach();
        Network n2 = subnetwork2.detach();
        // - Check Transformers
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertEquals(1, n1.getThreeWindingsTransformerCount());
        assertEquals(1, n2.getThreeWindingsTransformerCount());
        assertNetworks(network, network, network.getThreeWindingsTransformer("twt0"));
        assertNetworks(n1, n1, n1.getThreeWindingsTransformer("twt1"));
        assertNetworks(n2, n2, n2.getThreeWindingsTransformer("twt2"));

        checkIndexNetworks(network);
        checkIndexNetworks(n1);
        checkIndexNetworks(n2);
    }

    @Test
    public void failCreateThreeWindingsTransformerBetweenDifferentSubnetworks() {
        addVoltageLevel(subnetwork1.newVoltageLevel(), "vl1_0");
        addVoltageLevel(subnetwork1.newVoltageLevel(), 90, "vl1_1");
        addSubstation(subnetwork2, "s2");
        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), 90, "vl2_0");

        PowsyblException e = assertThrows(ValidationException.class, () -> addThreeWindingsTransformer(subnetwork1, "twt",
                "vl1_0", 380, "vl1_1", 90, "vl2_0", 90));
        assertTrue(e.getMessage().contains("The 3 windings of the transformer shall belong to the same subnetwork"));
    }

    @ParameterizedTest()
    @MethodSource("networkParameters")
    public void testValidationWithSubnetworkChanges(String networkId) {
        Network validationOperationsNetwork = network.getId().equals(networkId) ? network : network.getSubnetwork(networkId);
        VoltageLevel voltageLevel1 = addVoltageLevel(subnetwork1.newVoltageLevel().setTopologyKind(TopologyKind.BUS_BREAKER), "vl1");

        assertValidationLevels(ValidationLevel.STEADY_STATE_HYPOTHESIS);
        validationOperationsNetwork.runValidationChecks();
        validationOperationsNetwork.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        assertValidationLevels(ValidationLevel.STEADY_STATE_HYPOTHESIS);
        String bus = getBusId("vl1");
        voltageLevel1.newLoad()
                .setId("unchecked")
                .setP0(1.0).setQ0(1.0)
                .setBus(bus)
                .setConnectableBus(bus)
                .add();
        assertValidationLevels(ValidationLevel.STEADY_STATE_HYPOTHESIS);
        validationOperationsNetwork.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        Load unchecked2 = voltageLevel1.newLoad()
                .setId("unchecked2")
                .setBus(bus)
                .setConnectableBus(bus)
                .add();
        assertValidationLevels(ValidationLevel.EQUIPMENT);
        unchecked2.setP0(0.0).setQ0(0.0);
        assertValidationLevels(ValidationLevel.STEADY_STATE_HYPOTHESIS);
    }

    void assertValidationLevels(ValidationLevel expected) {
        // The validation level must be the same between the root network and its subnetworks
        assertEquals(expected, network.getValidationLevel());
        assertEquals(expected, subnetwork1.getValidationLevel());
        assertEquals(expected, subnetwork2.getValidationLevel());
    }

    static Stream<Arguments> networkParameters() {
        return Stream.of(
                Arguments.of("Root"),
                Arguments.of("Sub1"),
                Arguments.of("Sub2")
        );
    }

    private Substation addSubstation(Network network, String substationId) {
        return network.newSubstation()
                .setId(substationId)
                .setCountry(Country.AQ)
                .add();
    }

    private VoltageLevel addVoltageLevel(VoltageLevelAdder adder, String id) {
        return addVoltageLevel(adder, 380, id);
    }

    private VoltageLevel addVoltageLevel(VoltageLevelAdder adder, double nominalV, String id) {
        VoltageLevel voltageLevel = adder.setId(id)
                .setNominalV(nominalV)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId(getBusId(id))
                .add();
        return voltageLevel;
    }

    private Line addLine(Network network, String id, String vl1, String vl2) {
        return network.newLine()
                .setId(id)
                .setVoltageLevel1(vl1).setBus1(getBusId(vl1))
                .setVoltageLevel2(vl2).setBus2(getBusId(vl2))
                .setR(1.0).setX(1.0).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0)
                .add();
    }

    private TwoWindingsTransformer addTwoWindingsTransformer(Network network, String id, String vlId1, double nominalV1,
                                                             String vlId2, double nominalV2) {
        return network.newTwoWindingsTransformer()
                .setId(id)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .setConnectableBus1(getBusId(vlId1))
                .setBus1(getBusId(vlId1))
                .setConnectableBus2(getBusId(vlId2))
                .setBus2(getBusId(vlId2))
                .setVoltageLevel1(vlId1)
                .setVoltageLevel2(vlId2)
                .setRatedU1(nominalV1)
                .setRatedU2(nominalV2)
                .add();
    }

    private ThreeWindingsTransformer addThreeWindingsTransformer(Network network, String id, String vlId1, double nominalV1,
                                                             String vlId2, double nominalV2, String vlId3, double nominalV3) {
        return network.newThreeWindingsTransformer()
                .setId(id)
                .newLeg1()
                .setRatedU(nominalV1)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .setConnectableBus(getBusId(vlId1))
                .setBus(getBusId(vlId1))
                .setVoltageLevel(vlId1)
                .add()
                .newLeg2()
                .setRatedU(nominalV2)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .setConnectableBus(getBusId(vlId2))
                .setBus(getBusId(vlId2))
                .setVoltageLevel(vlId2)
                .add()
                .newLeg3()
                .setRatedU(nominalV3)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .setConnectableBus(getBusId(vlId3))
                .setBus(getBusId(vlId3))
                .setVoltageLevel(vlId3)
                .add()
                .add();
    }

    private String getBusId(String vlId3) {
        return "bus_" + vlId3;
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
