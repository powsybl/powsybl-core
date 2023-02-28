/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
class CreateCouplingDeviceTest extends AbstractConverterTest {

    @Test
    void createCouplingDevice2BusbarSectionsSameSectionIndex() throws IOException {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification couplingDeviceModif = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs3")
                .withSwitchPrefixId("sw")
                .build();
        couplingDeviceModif.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
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
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/testNetworkNodeBreakerWithCouplingDeviceDifferentSectionIndex.xml");
    }

    @Test
    void createCouplingDeviceThrowsException() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        NetworkModification couplingDeviceModifWrongBbs = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> couplingDeviceModifWrongBbs.apply(network, true, Reporter.NO_OP));
        assertEquals("Identifiable bbs not found.", e0.getMessage());

        NetworkModification couplingDeviceModifBbsInDifferentVl = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs5")
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> couplingDeviceModifBbsInDifferentVl.apply(network, true, Reporter.NO_OP));
        assertEquals("bbs1 and bbs5 are in two different voltage levels.", e1.getMessage());

        NetworkModification sameBusbarSection = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs1")
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> sameBusbarSection.apply(network, true, Reporter.NO_OP));
        assertEquals("No coupling device can be created on a same bus or busbar section (bbs1)", e2.getMessage());
    }

    @Test
    void createCouplingDeviceWithoutPositionExtensions() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
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
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
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

        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/network_test_bus_breaker_with_coupling_device.xiidm");
    }

    @Test
    void createCouplingDeviceThrowsExceptionIfNotBusbarSectionOrBusId() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("gen1")
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Unexpected type of identifiable gen1: GENERATOR", e2.getMessage());
    }

    @Test
    void createCouplingDeviceThrowsExceptionIfWrongBusbarSectionOrBusId1() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bb1")
                .withBusOrBusbarSectionId2("bbs2")
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Identifiable bb1 not found.", e2.getMessage());
    }

    @Test
    void createCouplingDeviceThrowsExceptionIfWrongBusbarSectionOrBusId2() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusOrBusbarSectionId1("bbs1")
                .withBusOrBusbarSectionId2("bb2")
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Identifiable bb2 not found.", e2.getMessage());
    }

    private Network createSimpleBusBreakerNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
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
