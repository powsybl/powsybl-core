/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
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
class ConnectVoltageLevelOnLineTest extends AbstractSerDeTest {

    @Test
    void attachVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        ReporterModel reporter = new ReporterModel("reportAttachVoltageLevelOnLineNbTest", "Testing reporter for Attaching voltage level on line - Node breaker");
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), false, reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-line-split-vl.xml");
    }

    @Test
    void connectVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        ReporterModel reporter = new ReporterModel("reportConnectVoltageLevelOnLineNbBbTest", "Testing reporter for connecting voltage level on line - Node breaker");
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-line-split-nb-vl.xml");
    }

    @Test
    void connectVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        ReporterModel reporter = new ReporterModel("reportConnectVoltageLevelOnLineBbTest", "Testing reporter for connecting voltage level on line - Bus breaker");
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("bus")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault(), reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-line-split-bb-vl.xml");
    }

    @Test
    void testConstructor() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        ConnectVoltageLevelOnLine modification = new ConnectVoltageLevelOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).build();
        assertEquals(BBS, modification.getBbsOrBusId());
        assertEquals(50, modification.getPositionPercent(), 0.0);
        assertSame(line, modification.getLine());
        assertEquals(line.getId() + "_1", modification.getLine1Id());
        assertNull(modification.getLine1Name());
        assertEquals(line.getId() + "_2", modification.getLine2Id());
        assertNull(modification.getLine2Name());
    }

    @Test
    void testSetters() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        ConnectVoltageLevelOnLine modification = new ConnectVoltageLevelOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).build();
        modification.setPositionPercent(40.0)
                .setLine1Id(line.getId() + "_A")
                .setLine1Name("A")
                .setLine2Id(line.getId() + "_B")
                .setLine2Name("B");
        assertEquals(40, modification.getPositionPercent(), 0.0);
        assertEquals(line.getId() + "_A", modification.getLine1Id());
        assertEquals("A", modification.getLine1Name());
        assertEquals(line.getId() + "_B", modification.getLine2Id());
        assertEquals("B", modification.getLine2Name());
    }

    @Test
    void testCompleteBuilder() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        BusbarSection bbs = network.getBusbarSection("bbs");
        bbs.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withPositionPercent(40)
                .withBusbarSectionOrBusId(BBS)
                .withLine1Id("FICT1L")
                .withLine1Name("FICT1LName")
                .withLine2Id("FICT2L")
                .withLine2Name("FICT2LName")
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault());
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-line-split-vl-complete.xml");
    }

    @Test
    void testIncompleteBuilder() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network, LocalComputationManager.getDefault());
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-line-split-vl.xml");
    }

    @Test
    void testExceptions() {
        Network network1 = createNbNetworkWithBusbarSection();

        ReporterModel reporter = new ReporterModel("reportTestUndefinedBbs", "Testing reporter with undefined busbar section");
        NetworkModification modification2 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network1.getLine("CJ"))
                .build();
        Reporter subReporterNb = reporter.createSubReporter("nodeBreaker", "Test on node/breaker network");
        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> modification2.apply(network1, true, subReporterNb));
        assertEquals("Bus or busbar section NOT_EXISTING not found", exception2.getMessage());
        ReporterModel firstReport = reporter.getSubReporters().get(0);
        assertEquals("notFoundBusOrBusbarSection", firstReport.getReports().iterator().next().getReportKey());
        assertEquals("nodeBreaker", firstReport.getTaskKey());

        Network network2 = createBbNetwork();
        NetworkModification modification3 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network2.getLine("NHV1_NHV2_1"))
                .build();
        Reporter subReporterBb = reporter.createSubReporter("busBreaker", "Test on bus/breaker network");
        PowsyblException exception3 = assertThrows(PowsyblException.class, () -> modification3.apply(network2, true, subReporterBb));
        assertEquals("Bus or busbar section NOT_EXISTING not found", exception3.getMessage());
        ReporterModel secondReport = reporter.getSubReporters().get(1);
        assertEquals("notFoundBusOrBusbarSection", secondReport.getReports().iterator().next().getReportKey());
        assertEquals("busBreaker", secondReport.getTaskKey());
    }

    @Test
    void testIgnore() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        NetworkModification modification1 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification1.apply(network, new DefaultNamingStrategy());
        NetworkModification modification2 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("LOAD")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification2.apply(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault());
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-tutorial-example1.xml");
    }

    @Test
    void testWithReporter() {
        Network network = createNbNetworkWithBusbarSection();
        ReporterModel reporter = new ReporterModel("reportTestConnectVoltageLevelOnLine", "Testing reporter for connecting voltage level on line");
        new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build().apply(network, new DefaultNamingStrategy(), true, reporter);
        testReporter(reporter, "/reporter/connect-voltage-level-on-line-NB-report.txt");
    }
}
