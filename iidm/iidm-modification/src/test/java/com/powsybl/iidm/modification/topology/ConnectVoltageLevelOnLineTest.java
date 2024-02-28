/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.ReportNode;
import com.powsybl.commons.reporter.ReportNodeImpl;
import com.powsybl.commons.reporter.ReportRootImpl;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Iterator;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class ConnectVoltageLevelOnLineTest extends AbstractModificationTest {

    @Test
    void attachVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        ReportNode reporter = new ReportRootImpl().newReportNode().withKey("reportAttachVoltageLevelOnLineNbTest").withMessageTemplate("Testing reporter for Attaching voltage level on line - Node breaker").add();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), false, reporter);
        writeXmlTest(network, "/fictitious-line-split-vl.xml");
    }

    @Test
    void connectVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        ReportNode reporter = new ReportRootImpl().newReportNode().withKey("reportConnectVoltageLevelOnLineNbBbTest").withMessageTemplate("Testing reporter for connecting voltage level on line - Node breaker").add();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), reporter);
        writeXmlTest(network, "/eurostag-line-split-nb-vl.xml");
    }

    @Test
    void connectVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        ReportNode reporter = new ReportRootImpl().newReportNode().withKey("reportConnectVoltageLevelOnLineBbTest").withMessageTemplate("Testing reporter for connecting voltage level on line - Bus breaker").add();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("bus")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();
        modification.apply(network, new DefaultNamingStrategy(), LocalComputationManager.getDefault(), reporter);
        writeXmlTest(network, "/eurostag-line-split-bb-vl.xml");
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
        writeXmlTest(network, "/fictitious-line-split-vl-complete.xml");
    }

    @Test
    void testIncompleteBuilder() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();
        modification.apply(network, LocalComputationManager.getDefault());
        writeXmlTest(network, "/fictitious-line-split-vl.xml");
    }

    @Test
    void testExceptions() {
        Network network1 = createNbNetworkWithBusbarSection();

        ReportNode reporter = new ReportRootImpl().newReportNode().withKey("reportTestUndefinedBbs").withMessageTemplate("Testing reporter with undefined busbar section").add();
        NetworkModification modification2 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network1.getLine("CJ"))
                .build();
        ReportNode subReportNodeNb = reporter.newReportNode().withKey("nodeBreaker").withMessageTemplate("Test on node/breaker network").add();
        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> modification2.apply(network1, true, subReportNodeNb));
        assertEquals("Bus or busbar section NOT_EXISTING not found", exception2.getMessage());
        ReportNodeImpl firstReport = (ReportNodeImpl) reporter.getChildren().iterator().next();
        assertEquals("notFoundBusOrBusbarSection", firstReport.getChildren().iterator().next().getKey());
        assertEquals("nodeBreaker", firstReport.getKey());

        Network network2 = createBbNetwork();
        NetworkModification modification3 = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(network2.getLine("NHV1_NHV2_1"))
                .build();
        ReportNode subReportNodeBb = reporter.newReportNode().withKey("busBreaker").withMessageTemplate("Test on bus/breaker network").add();
        PowsyblException exception3 = assertThrows(PowsyblException.class, () -> modification3.apply(network2, true, subReportNodeBb));
        assertEquals("Bus or busbar section NOT_EXISTING not found", exception3.getMessage());
        Iterator<ReportNode> iterator = reporter.getChildren().iterator();
        iterator.next();
        ReportNodeImpl secondReport = (ReportNodeImpl) iterator.next();
        assertEquals("notFoundBusOrBusbarSection", secondReport.getChildren().iterator().next().getKey());
        assertEquals("busBreaker", secondReport.getKey());
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
        writeXmlTest(network, "/eurostag-tutorial-example1.xml");
    }

    @Test
    void testWithReporter() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        ReportNode reporter = new ReportRootImpl().newReportNode().withKey("reportTestConnectVoltageLevelOnLine").withMessageTemplate("Testing reporter for connecting voltage level on line").add();
        new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build().apply(network, new DefaultNamingStrategy(), true, reporter);
        testReporter(reporter, "/reporter/connect-voltage-level-on-line-NB-report.txt");
    }
}
