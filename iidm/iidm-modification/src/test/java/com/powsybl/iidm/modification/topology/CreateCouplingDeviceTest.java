/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.testReporter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class CreateCouplingDeviceTest extends AbstractSerDeTest {

    @Test
    void createCouplingDevice2BusbarSectionsSameSectionIndex() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification couplingDeviceModif = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs3")
                .withSwitchPrefixId("sw")
                .build();
        couplingDeviceModif.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/testNetworkNodeBreakerWithCouplingDeviceSameSectionIndex.xml");
    }

    @Test
    void createCouplingDevice2BusbarSectionsDifferentSectionIndex() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification couplingDeviceModif = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        couplingDeviceModif.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/testNetworkNodeBreakerWithCouplingDeviceDifferentSectionIndex.xml");
    }

    @Test
    void createCouplingDeviceThrowsException() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        ReporterModel reporter1 = new ReporterModel("testReporterWrongBbs", "Testing reporter with wrong busbar section ID");
        NetworkModification couplingDeviceModifWrongBbs = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> couplingDeviceModifWrongBbs.apply(network, true, reporter1));
        assertEquals("Bus or busbar section bbs not found", e0.getMessage());
        assertEquals("notFoundBusOrBusbarSection", reporter1.getReports().iterator().next().getReportKey());

        ReporterModel reporter2 = new ReporterModel("testReporterBbsInDifferentVl", "Testing reporter with busbar sections in different voltage levels");
        NetworkModification couplingDeviceModifBbsInDifferentVl = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs5")
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> couplingDeviceModifBbsInDifferentVl.apply(network, true, reporter2));
        assertEquals("bbs1 and bbs5 are in two different voltage levels.", e1.getMessage());
        assertEquals("unexpectedDifferentVoltageLevels", reporter2.getReports().iterator().next().getReportKey());

        ReporterModel reporter3 = new ReporterModel("testReporterSameBbs", "Testing reporter with same busbar section");
        NetworkModification sameBusbarSection = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs1")
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> sameBusbarSection.apply(network, true, reporter3));
        assertEquals("No coupling device can be created on a same bus or busbar section (bbs1)", e2.getMessage());
        assertEquals("noCouplingDeviceOnSameBusOrBusbarSection", reporter3.getReports().iterator().next().getReportKey());
    }

    @Test
    void createCouplingDeviceWithoutPositionExtensions() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/testNetworkNodeBreakerWithCouplingDeviceWithoutExtensions.xml");
    }

    @Test
    void createCouplingDevice3BusbarSections() throws IOException {
        Network network = Network.read("testNetwork3BusbarSections.xiidm", getClass().getResourceAsStream("/testNetwork3BusbarSections.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("VLTEST13")
                .withBusOrBusbarSectionId2("VLTEST23")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/testNetwork3BusbarSectionsWithCouplingDevice.xiidm");
    }

    @Test
    void createCouplingDeviceBusBreaker() throws IOException {
        Network network = createSimpleBusBreakerNetwork();
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bus_1_1")
                .withBusOrBusbarSectionId2("bus_2_2")
                .build();
        modification.apply(network);

        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/network_test_bus_breaker_with_coupling_device.xiidm");
    }

    @Test
    void testWithReporter() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReporterModel reporter = new ReporterModel("reportTestCreateCouplingDevice", "Testing reporter for coupling device creation");
        new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs3")
                .withSwitchPrefixId("sw")
                .build().apply(network, true, reporter);
        testReporter(reporter, "/reporter/create-coupling-device-report.txt");
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void createCouplingDeviceThrowsException(String bbs1, String bbs2, String message, String reporterKey) {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReporterModel reporter = new ReporterModel("ReporterTest", "Testing reporter");
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1(bbs1)
                .withBusOrBusbarSectionId2(bbs2)
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals(message, e2.getMessage());
        assertEquals(reporterKey, reporter.getReports().iterator().next().getReportKey());
    }

    private static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("bbs1", "gen1", "Unexpected type of identifiable gen1: GENERATOR", "unexpectedIdentifiableType"),
                Arguments.of("bb1", "bbs2", "Bus or busbar section bb1 not found", "notFoundBusOrBusbarSection"),
                Arguments.of("bbs1", "bb2", "Bus or busbar section bb2 not found", "notFoundBusOrBusbarSection")
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
}
