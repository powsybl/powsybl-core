/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class CreateCouplingDeviceTest extends AbstractModificationTest {

    @Test
    void createCouplingDevice2BusbarSectionsSameSectionIndex() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification couplingDeviceModif = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs3")
                .withSwitchPrefixId("sw")
                .build();
        couplingDeviceModif.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithCouplingDeviceSameSectionIndex.xml");
    }

    @Test
    void createCouplingDevice2BusbarSectionsDifferentSectionIndex() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification couplingDeviceModif = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        couplingDeviceModif.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithCouplingDeviceDifferentSectionIndex.xml");
    }

    @Test
    void createCouplingDeviceThrowsException() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testReportNodeWrongBbs")
                .build();
        NetworkModification couplingDeviceModifWrongBbs = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        assertDoesNotThrow(() -> couplingDeviceModifWrongBbs.apply(network, false, ReportNode.NO_OP));
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> couplingDeviceModifWrongBbs.apply(network, true, reportNode1));
        assertEquals("Bus or busbar section bbs not found", e0.getMessage());
        assertEquals("core.iidm.modification.notFoundBusOrBusbarSection", reportNode1.getChildren().get(0).getMessageKey());

        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testReportNodeBbsInDifferentVl")
                .build();
        NetworkModification couplingDeviceModifBbsInDifferentVl = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs5")
                .build();
        assertDoesNotThrow(() -> couplingDeviceModifBbsInDifferentVl.apply(network, false, ReportNode.NO_OP));
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> couplingDeviceModifBbsInDifferentVl.apply(network, true, reportNode2));
        assertEquals("bbs1 and bbs5 are in two different voltage levels.", e1.getMessage());
        assertEquals("core.iidm.modification.unexpectedDifferentVoltageLevels", reportNode2.getChildren().get(0).getMessageKey());

        ReportNode reportNode3 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testReportNodeSameBbs")
                .build();
        NetworkModification sameBusbarSection = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs1")
                .build();
        assertDoesNotThrow(() -> sameBusbarSection.apply(network, false, ReportNode.NO_OP));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> sameBusbarSection.apply(network, true, reportNode3));
        assertEquals("No coupling device can be created on a same bus or busbar section (bbs1)", e2.getMessage());
        assertEquals("core.iidm.modification.noCouplingDeviceOnSameBusOrBusbarSection", reportNode3.getChildren().get(0).getMessageKey());
    }

    @Test
    void createCouplingDeviceWithoutPositionExtensions() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        modification.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithCouplingDeviceWithoutExtensions.xml");
    }

    @Test
    void createCouplingDevice3BusbarSections() throws IOException {
        Network network = Network.read("testNetwork3BusbarSections.xiidm", getClass().getResourceAsStream("/testNetwork3BusbarSections.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("VLTEST13")
                .withBusOrBusbarSectionId2("VLTEST23")
                .build();
        modification.apply(network);
        writeXmlTest(network, "/testNetwork3BusbarSectionsWithCouplingDevice.xiidm");
    }

    @Test
    void createCouplingDeviceBusBreaker() throws IOException {
        Network network = createSimpleBusBreakerNetwork();
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bus_1_1")
                .withBusOrBusbarSectionId2("bus_2_2")
                .build();
        modification.apply(network);

        writeXmlTest(network, "/network_test_bus_breaker_with_coupling_device.xiidm");
    }

    @Test
    void testWithReportNode() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateCouplingDevice")
                .build();
        new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs3")
                .withSwitchPrefixId("sw")
                .build().apply(network, true, reportNode);
        testReportNode(reportNode, "/reportNode/create-coupling-device-report.txt");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void createCouplingDeviceThrowsException(String bbs1, String bbs2, String message, String messageKey) {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("ReportNodeTest")
                .build();
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1(bbs1)
                .withBusOrBusbarSectionId2(bbs2)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals(message, e2.getMessage());
        assertEquals(messageKey, reportNode.getChildren().get(0).getMessageKey());
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("bbs1", "gen1", "Unexpected type of identifiable gen1: GENERATOR", "core.iidm.modification.unexpectedIdentifiableType"),
                Arguments.of("bb1", "bbs2", "Bus or busbar section bb1 not found", "core.iidm.modification.notFoundBusOrBusbarSection"),
                Arguments.of("bbs1", "bb2", "Bus or busbar section bb2 not found", "core.iidm.modification.notFoundBusOrBusbarSection")
        );
    }

    private Network createSimpleBusBreakerNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        VoltageLevel vltest = network.newVoltageLevel()
                .setNominalV(400)
                .setId("VLTEST")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vltest.getBusBreakerView().newBus()
                .setId("bus_1_1")
                .add();
        vltest.getBusBreakerView().newBus()
                .setId("bus_1_2")
                .add();
        vltest.getBusBreakerView().newBus()
                .setId("bus_2_1")
                .add();
        vltest.getBusBreakerView().newBus()
                .setId("bus_2_2")
                .add();
        vltest.getBusBreakerView().newSwitch()
                .setId("switch_1")
                .setBus1("bus_1_1")
                .setBus2("bus_1_2")
                .setOpen(false)
                .add();
        vltest.getBusBreakerView().newSwitch()
                .setId("switch_2")
                .setBus1("bus_2_1")
                .setBus2("bus_2_2")
                .setOpen(false)
                .add();
        return network;
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new CreateCouplingDeviceBuilder()
            .withBusOrBusbarSectionId1("bbs1")
            .withBusOrBusbarSectionId2("bbs2")
            .build();
        assertEquals("CreateCouplingDevice", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification couplingDeviceModif = new CreateCouplingDeviceBuilder()
            .withBusOrBusbarSectionId1("bbs1")
            .withBusOrBusbarSectionId2("bbs3")
            .withSwitchPrefixId("sw")
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, couplingDeviceModif.hasImpactOnNetwork(network));

        couplingDeviceModif = new CreateCouplingDeviceBuilder()
            .withBusOrBusbarSectionId1("WRONG_BBS")
            .withBusOrBusbarSectionId2("bbs3")
            .withSwitchPrefixId("sw")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, couplingDeviceModif.hasImpactOnNetwork(network));

        couplingDeviceModif = new CreateCouplingDeviceBuilder()
            .withBusOrBusbarSectionId1("bbs1")
            .withBusOrBusbarSectionId2("WRONG_BBS")
            .withSwitchPrefixId("sw")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, couplingDeviceModif.hasImpactOnNetwork(network));

        couplingDeviceModif = new CreateCouplingDeviceBuilder()
            .withBusOrBusbarSectionId1("bbs1")
            .withBusOrBusbarSectionId2("bbs1")
            .withSwitchPrefixId("sw")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, couplingDeviceModif.hasImpactOnNetwork(network));
    }
}
