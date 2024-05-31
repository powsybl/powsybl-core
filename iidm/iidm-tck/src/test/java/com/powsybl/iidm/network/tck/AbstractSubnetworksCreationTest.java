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
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
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
    public void testAreaCreation() {
        // On root network level
        addArea(network, "a0", "Area0", "AreaType0");
        assertAreaCounts(1, 0, 0);

        // On subnetwork level
        addArea(subnetwork1, "a1", "Area1", "AreaType1");
        assertAreaCounts(2, 1, 0);

        Throwable e = assertThrows(PowsyblException.class, () -> addArea(subnetwork1, "a0", "Area12", "AreaType2"));
        assertTrue(e.getMessage().contains("The network Root already contains an object 'AreaImpl' with the id 'a0'"));
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
        Substation substation0 = addSubstation(network, "s0");
        Substation substation1 = addSubstation(subnetwork1, "s1");
        Substation substation2 = addSubstation(subnetwork2, "s2");
        addVoltageLevel(substation0.newVoltageLevel(), "vl0_0");
        addVoltageLevel(substation0.newVoltageLevel(), 90, "vl0_1");
        addVoltageLevel(substation1.newVoltageLevel(), "vl1_0");
        addVoltageLevel(substation1.newVoltageLevel(), 90, "vl1_1");
        addVoltageLevel(substation2.newVoltageLevel(), "vl2_0");
        addVoltageLevel(substation2.newVoltageLevel(), 90, "vl2_1");

        // On root network
        addTwoWindingsTransformer(substation0, "twt0", "vl0_0", 380, "vl0_1", 90);

        // On subnetwork1
        addTwoWindingsTransformer(substation1, "twt1", "vl1_0", 380, "vl1_1", 90);

        // On subnetwork2
        addTwoWindingsTransformer(substation2, "twt2", "vl2_0", 380, "vl2_1", 90);

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
    public void testThreeWindingsTransformersCreation() {
        Substation substation0 = addSubstation(network, "s0");
        Substation substation1 = addSubstation(subnetwork1, "s1");
        Substation substation2 = addSubstation(subnetwork2, "s2");
        addVoltageLevel(substation0.newVoltageLevel(), "vl0_0");
        addVoltageLevel(substation0.newVoltageLevel(), 225, "vl0_1");
        addVoltageLevel(substation0.newVoltageLevel(), 90, "vl0_2");
        addVoltageLevel(substation1.newVoltageLevel(), "vl1_0");
        addVoltageLevel(substation1.newVoltageLevel(), 225, "vl1_1");
        addVoltageLevel(substation1.newVoltageLevel(), 90, "vl1_2");
        addVoltageLevel(substation2.newVoltageLevel(), "vl2_0");
        addVoltageLevel(substation2.newVoltageLevel(), 225, "vl2_1");
        addVoltageLevel(substation2.newVoltageLevel(), 90, "vl2_2");

        // On root network
        addThreeWindingsTransformer(substation0, "twt0", "vl0_0", 380, "vl0_1", 225, "vl0_2", 90);

        // On subnetwork1
        addThreeWindingsTransformer(substation1, "twt1", "vl1_0", 380, "vl1_1", 225, "vl1_2", 90);

        // On subnetwork2
        addThreeWindingsTransformer(substation2, "twt2", "vl2_0", 380, "vl2_1", 225, "vl2_2", 90);

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

    static Stream<Arguments> networkParameters() {
        return Stream.of(
                Arguments.of("Root"),
                Arguments.of("Sub1"),
                Arguments.of("Sub2")
        );
    }

    @Test
    public void testListeners() {
        MutableBoolean listenerCalled = new MutableBoolean(false);
        NetworkListener listener = new DefaultNetworkListener() {
            @Override
            public void onCreation(Identifiable identifiable) {
                listenerCalled.setTrue();
            }
        };

        // The listener can only be added to the root network.
        assertThrows(PowsyblException.class, () -> subnetwork1.addListener(listener));
        network.addListener(listener);

        // A listener added to the root network is called during subnetworks changes.
        addSubstation(subnetwork1, "s0");
        assertTrue(listenerCalled.booleanValue());

        // The listener can only be removed to the root network.
        assertThrows(PowsyblException.class, () -> subnetwork1.removeListener(listener));
        network.removeListener(listener);

        // After its removal, a listener isn't called anymore during subnetworks changes.
        listenerCalled.setFalse();
        addSubstation(subnetwork1, "s1");
        assertFalse(listenerCalled.booleanValue());
    }

    @Test
    public void testAngleVoltageLimitCreation() {
        addSubstation(network, "s0");
        addSubstation(subnetwork1, "s1");
        addSubstation(subnetwork2, "s2");

        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_0");
        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_1");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_0");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_1");
        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), "vl2_0");
        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), "vl2_1");

        Line l0 = addLine(network, "l0", "vl0_0", "vl0_1");
        Line l1 = addLine(network, "l1", "vl1_0", "vl1_1");
        Line l2 = addLine(network, "l2", "vl2_0", "vl2_1");

        // On root network, terminals both in root network
        VoltageAngleLimit vla0 = addVoltageAngleLimit(network, "vla0", l0.getTerminal1(), l0.getTerminal2());

        // On root network, terminals both in subnetwork1
        VoltageAngleLimit vla1 = addVoltageAngleLimit(network, "vla1", l1.getTerminal1(), l1.getTerminal2());

        // On subnetwork2, terminals both in subnetwork2
        VoltageAngleLimit vla2 = addVoltageAngleLimit(subnetwork2, "vla2", l2.getTerminal1(), l2.getTerminal2());

        // On root network, terminals in different subnetworks
        VoltageAngleLimit vla3 = addVoltageAngleLimit(network, "vla3", l1.getTerminal1(), l2.getTerminal1());

        // On root network, terminals in root network and subnetwork2
        VoltageAngleLimit vla4 = addVoltageAngleLimit(network, "vla4", l0.getTerminal1(), l2.getTerminal1());

        // Try to detach all. Some elements prevent it.
        assertFalse(subnetwork1.isDetachable());
        assertFalse(subnetwork2.isDetachable());
        // Remove problematic elements
        vla3.remove();
        vla4.remove();

        // Test VoltageAngleLimit retrieval
        assertEquals(vla0, network.getVoltageAngleLimit("vla0"));
        assertEquals(vla1, network.getVoltageAngleLimit("vla1"));
        assertEquals(vla2, network.getVoltageAngleLimit("vla2"));
        assertNull(subnetwork1.getVoltageAngleLimit("vla0"));
        assertEquals(vla1, subnetwork1.getVoltageAngleLimit("vla1"));
        assertNull(subnetwork1.getVoltageAngleLimit("vla2"));
        assertNull(subnetwork2.getVoltageAngleLimit("vla0"));
        assertNull(subnetwork2.getVoltageAngleLimit("vla1"));
        assertEquals(vla2, subnetwork2.getVoltageAngleLimit("vla2"));

        // Detach all
        assertTrue(subnetwork1.isDetachable());
        assertTrue(subnetwork2.isDetachable());
        Network n1 = subnetwork1.detach();
        Network n2 = subnetwork2.detach();
        // - Check VoltageAngleLimits
        assertEquals(1, List.of(network.getVoltageAngleLimits()).size());
        assertEquals(1, List.of(n1.getVoltageAngleLimits()).size());
        assertEquals(1, List.of(n2.getVoltageAngleLimits()).size());
        assertEquals(vla0, network.getVoltageAngleLimit("vla0"));
        assertNull(network.getVoltageAngleLimit("vla1"));
        assertNull(network.getVoltageAngleLimit("vla2"));
        assertEquals(vla1, n1.getVoltageAngleLimit("vla1"));
        assertEquals(vla2, n2.getVoltageAngleLimit("vla2"));
    }

    @Test
    public void failCreateVoltageAngleLimitFromSubnetworkBetweenRootAndSubnetwork() {
        addSubstation(network, "s0");
        addSubstation(subnetwork1, "s1");
        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_0");
        addVoltageLevel(network.getSubstation("s0").newVoltageLevel(), "vl0_1");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_0");
        addVoltageLevel(network.getSubstation("s1").newVoltageLevel(), "vl1_1");
        Line l0 = addLine(network, "l0", "vl0_0", "vl0_1");
        Line l1 = addLine(network, "l1", "vl1_0", "vl1_1");

        // On subnetwork1, voltage levels in root network and subnetwork2 => should fail
        Terminal from = l0.getTerminal1();
        Terminal to = l1.getTerminal1();
        Exception e = assertThrows(ValidationException.class, () -> addVoltageAngleLimit(subnetwork1, "vla", from, to));
        assertTrue(e.getMessage().contains("Create this VoltageAngleLimit from the parent network"));
    }

    @Test
    public void failCreateVoltageAngleLimitFromASubnetworkInAnother() {
        addSubstation(subnetwork1, "s2");

        addVoltageLevel(network.getSubstation("s2").newVoltageLevel(), "vl2_0");
        addVoltageLevel(subnetwork2.newVoltageLevel(), "vl2_1");
        Line l2 = addLine(network, "l2", "vl2_0", "vl2_1");

        // On subnetwork1, voltage levels both in subnetwork2 => should fail
        Terminal from = l2.getTerminal1();
        Terminal to = l2.getTerminal2();
        PowsyblException e = assertThrows(ValidationException.class, () -> addVoltageAngleLimit(subnetwork1, "vla", from, to));
        assertTrue(e.getMessage().contains("Create this VoltageAngleLimit from the parent network"));
    }

    @Test
    public void subnetworksWithSubstationFromSameCountry() {
        addSubstation(subnetwork1, "substation1");
        addSubstation(subnetwork1, "substation2");
        assertEquals(1, subnetwork1.getCountryCount());

        subnetwork1.newSubstation()
                .setId("substation3")
                .setCountry(Country.AD)
                .add();
        assertEquals(2, subnetwork1.getCountryCount());
    }

    void assertValidationLevels(ValidationLevel expected) {
        // The validation level must be the same between the root network and its subnetworks
        assertEquals(expected, network.getValidationLevel());
        assertEquals(expected, subnetwork1.getValidationLevel());
        assertEquals(expected, subnetwork2.getValidationLevel());
    }

    private Area addArea(Network network, String id, String name, String areaType) {
        return network.newArea()
                .setId(id)
                .setName(name)
                .setAreaType(areaType)
                .add();
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

    private TwoWindingsTransformer addTwoWindingsTransformer(Substation substation, String id, String vlId1, double nominalV1,
                                                             String vlId2, double nominalV2) {
        return substation.newTwoWindingsTransformer()
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

    private ThreeWindingsTransformer addThreeWindingsTransformer(Substation substation, String id, String vlId1, double nominalV1,
                                                             String vlId2, double nominalV2, String vlId3, double nominalV3) {
        return substation.newThreeWindingsTransformer()
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

    private VoltageAngleLimit addVoltageAngleLimit(Network network, String id, Terminal from, Terminal to) {
        return network.newVoltageAngleLimit()
                .setId(id).from(from).to(to)
                .add();
    }

    private String getBusId(String vlId3) {
        return "bus_" + vlId3;
    }

    void assertAreaCounts(int total, int onSubnetwork1, int onSubnetwork2) {
        assertEquals(total, network.getAreaCount());
        assertEquals(onSubnetwork1, subnetwork1.getAreaCount());
        assertEquals(onSubnetwork2, subnetwork2.getAreaCount());
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
