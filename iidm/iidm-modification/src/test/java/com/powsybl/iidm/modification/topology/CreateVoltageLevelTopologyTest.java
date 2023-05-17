/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VLTEST;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class CreateVoltageLevelTopologyTest extends AbstractConverterTest {

    @Test
    void test() throws IOException {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/create-vl-topo-test.xiidm");
    }

    @Test
    void testComplete() throws IOException {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withBusbarSectionPrefixId("BBS_TEST")
                .withSwitchPrefixId("SW_TEST")
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/create-vl-topo-test-complete.xiidm");
    }

    @Test
    void testWithNullSwitchKind() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, null, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("All switch kinds must be defined", e.getMessage());
    }

    @Test
    void testWithUnsupportedSwitchKind() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.LOAD_BREAK_SWITCH, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Switch kinds must be DISCONNECTOR or BREAKER", e.getMessage());
    }

    @Test
    void testWithNegativeCount() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopologyBuilder modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(-1)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR);
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.build().apply(network, true, Reporter.NO_OP));
        assertEquals("busBar count must be >= 1", e.getMessage());
    }

    @Test
    void testWithOneSection() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(2)
                .withSectionCount(1)
                .build();
        modification.apply(network);

        BusbarSection bbs1 = network.getBusbarSection("VLTEST_1_1");
        BusbarSection bbs2 = network.getBusbarSection("VLTEST_2_1");
        assertNotNull(bbs1);
        assertNotNull(bbs2);

        BusbarSectionPosition bbsp1 = bbs1.getExtension(BusbarSectionPosition.class);
        BusbarSectionPosition bbsp2 = bbs2.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsp1);
        assertNotNull(bbsp2);

        assertEquals(1, bbsp1.getBusbarIndex());
        assertEquals(1, bbsp1.getSectionIndex());

        assertEquals(2, bbsp2.getBusbarIndex());
        assertEquals(1, bbsp2.getSectionIndex());
    }

    @Test
    void testWithUnexpectedSwitchKindsSize() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Unexpected switch kinds count (1). Should be 3", e.getMessage());
    }

    @Test
    void testWithNotExistingVoltageLevel() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("NOT_EXISTING")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Voltage level NOT_EXISTING is not found", e.getMessage());
    }

    @Test
    void testWithBusBreakerVoltageLevel() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.newVoltageLevel()
                .setId("VLTEST")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("VLTEST")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        modification.apply(network);
        roundTripTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-new-voltage-level.xml");
    }

    @Test
    void testErrorIfNotSwitchKindsDefinedAndNodeBreaker() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Unexpected switch kinds count (0). Should be 3", e.getMessage());
    }
}
