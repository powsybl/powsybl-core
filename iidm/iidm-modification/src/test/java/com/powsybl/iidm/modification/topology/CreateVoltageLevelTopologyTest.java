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
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CreateVoltageLevelTopologyTest extends AbstractSerDeTest {

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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/create-vl-topo-test-complete.xiidm");
    }

    @Test
    void testWithNullSwitchKind() {
        Network network = createNbNetwork();
        ReporterModel reporter = new ReporterModel("reportTest", "Testing reporter");
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, null, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals("All switch kinds must be defined", e.getMessage());
        assertEquals("undefinedSwitchKind", reporter.getReports().iterator().next().getReportKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithUnsupportedSwitchKind() {
        Network network = createNbNetwork();
        ReporterModel reporter = new ReporterModel("reportTest", "Testing reporter");
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.LOAD_BREAK_SWITCH, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals("Switch kinds must be DISCONNECTOR or BREAKER", e.getMessage());
        assertEquals("wrongSwitchKind", reporter.getReports().iterator().next().getReportKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNegativeCount() {
        Network network = createNbNetwork();
        ReporterModel reporter = new ReporterModel("reportTest", "Testing reporter");
        NetworkModification modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(-1)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals("busbar count must be >= 1", e.getMessage());
        assertEquals("countLowerThanMin", reporter.getReports().iterator().next().getReportKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
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
        ReporterModel reporter = new ReporterModel("reportTest", "Testing reporter");
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals("Unexpected switch kinds count (1). Should be 3", e.getMessage());
        assertEquals("unexpectedSwitchKindsCount", reporter.getReports().iterator().next().getReportKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNotExistingVoltageLevel() {
        Network network = createNbNetwork();
        ReporterModel reporter = new ReporterModel("reportTest", "Testing reporter");
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("NOT_EXISTING")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals("Voltage level NOT_EXISTING is not found", e.getMessage());
        assertEquals("voltageLevelNotFound", reporter.getReports().iterator().next().getReportKey());
    }

    @Test
    void testWithBusBreakerVoltageLevel() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
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
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-new-voltage-level.xml");
    }

    @Test
    void testErrorIfNotSwitchKindsDefinedAndNodeBreaker() {
        Network network = createNbNetwork();
        ReporterModel reporter = new ReporterModel("reportTest", "Testing reporter");
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reporter));
        assertEquals("Unexpected switch kinds count (0). Should be 3", e.getMessage());
        assertEquals("unexpectedSwitchKindsCount", reporter.getReports().iterator().next().getReportKey());
    }

    @Test
    void testWithReporter() {
        Network network = createNbNetwork();
        ReporterModel reporter = new ReporterModel("reportTestCreateVoltageLevelTopology", "Testing reporter for voltage level topology creation");
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network, reporter);
        testReporter(reporter, "/reporter/create-voltage-level-topology-report.txt");
    }

}
