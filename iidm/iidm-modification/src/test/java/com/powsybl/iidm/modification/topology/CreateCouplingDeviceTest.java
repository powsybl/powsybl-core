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
import com.powsybl.iidm.xml.NetworkXml;
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
                .withBusbarSectionId1("bbs1")
                .withBusbarSectionId2("bbs3")
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
                .withBusbarSectionId1("bbs1")
                .withBusbarSectionId2("bbs2")
                .build();
        couplingDeviceModif.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/testNetworkNodeBreakerWithCouplingDeviceDifferentSectionIndex.xml");
    }

    @Test
    void createCouplingDeviceThrowsException() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        NetworkModification couplingDeviceModifWrongBbs = new CreateCouplingDeviceBuilder()
                .withBusbarSectionId1("bbs")
                .withBusbarSectionId2("bbs2")
                .build();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> couplingDeviceModifWrongBbs.apply(network, true, Reporter.NO_OP));
        assertEquals("Busbar section bbs not found.", e0.getMessage());

        NetworkModification couplingDeviceModifBbsInDifferentVl = new CreateCouplingDeviceBuilder()
                .withBusbarSectionId1("bbs1")
                .withBusbarSectionId2("bbs5")
                .build();
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> couplingDeviceModifBbsInDifferentVl.apply(network, true, Reporter.NO_OP));
        assertEquals("Busbar sections bbs1 and bbs5 are in two different voltage levels.", e1.getMessage());

        NetworkModification sameBusbarSection = new CreateCouplingDeviceBuilder()
                .withBusbarSectionId1("bbs1")
                .withBusbarSectionId2("bbs1")
                .build();
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> sameBusbarSection.apply(network, true, Reporter.NO_OP));
        assertEquals("No coupling device can be created on a same busbar section (bbs1)", e2.getMessage());
    }

    @Test
    void createCouplingDeviceWithoutPositionExtensions() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithoutExtensions.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithoutExtensions.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusbarSectionId1("bbs1")
                .withBusbarSectionId2("bbs2")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/testNetworkNodeBreakerWithCouplingDeviceWithoutExtensions.xml");
    }

    @Test
    void createCouplingDevice3BusbarSections() throws IOException {
        Network network = Network.read("testNetwork3BusbarSections.xiidm", getClass().getResourceAsStream("/testNetwork3BusbarSections.xiidm"));
        NetworkModification modification = new CreateCouplingDeviceBuilder()
                .withBusbarSectionId1("VLTEST13")
                .withBusbarSectionId2("VLTEST23")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/testNetwork3BusbarSectionsWithCouplingDevice.xiidm");
    }
}
