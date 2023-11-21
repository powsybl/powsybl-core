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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Create a new Line from a given Line Adder and attach it on an existing Line by cutting the latter.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CreateLineOnLineTest extends AbstractSerDeTest {

    @Test
    void createLineOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-line-split-l.xml");
    }

    @Test
    void createLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-line-split-nb-l.xml");
    }

    @Test
    void createLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-line-split-bb-l.xml");
    }

    @Test
    void testSetters() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        CreateLineOnLine modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.setPositionPercent(40.0)
                .setFictitiousVlId("FICT_VL")
                .setFictitiousVlName("FICT")
                .setCreateFictSubstation(true)
                .setFictitiousSubstationId("FICT_S")
                .setFictitiousSubstationName("FICT2")
                .setLine1Id(line.getId() + "_A")
                .setLine1Name("A")
                .setLine2Id(line.getId() + "_B")
                .setLine2Name("B");
        assertEquals(40, modification.getPositionPercent(), 0.0);
        assertEquals("FICT_VL", modification.getFictitiousVlId());
        assertEquals("FICT", modification.getFictitiousVlName());
        assertTrue(modification.isCreateFictSubstation());
        assertEquals("FICT_S", modification.getFictitiousSubstationId());
        assertEquals("FICT2", modification.getFictitiousSubstationName());
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
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(line)
                .withLineAdder(adder)
                .withPositionPercent(40)
                .withFictitiousVoltageLevelId("FICTVL")
                .withFictitiousVoltageLevelName("FICTITIOUSVL")
                .withCreateFictitiousSubstation(true)
                .withFictitiousSubstationId("FICTSUB")
                .withFictitiousSubstationName("FICTITIOUSSUB")
                .withLine1Id("FICT1L")
                .withLine1Name("FICT1LName")
                .withLine2Id("FICT2L")
                .withLine2Name("FICT2LName")
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-line-split-l-complete.xml");
    }

    @Test
    void testIncompleteBuilder() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(line)
                .withLineAdder(adder)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-line-split-l.xml");

    }

    @Test
    void testExceptions() {
        Network network1 = createNbNetworkWithBusbarSection();
        Line line1 = network1.getLine("CJ");
        LineAdder adder1 = createLineAdder(line1, network1);
        ReporterModel reporter1 = new ReporterModel("reportTestBbsNotExistingNB", "Testing reporter if busbar section does not exist in node/breaker");
        NetworkModification modification1 = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(line1)
                .withLineAdder(adder1)
                .build();
        PowsyblException exception1 = assertThrows(PowsyblException.class, () -> modification1.apply(network1, true, reporter1));
        assertEquals("Bus or busbar section NOT_EXISTING not found", exception1.getMessage());
        assertEquals("notFoundBusOrBusbarSection", reporter1.getReports().iterator().next().getReportKey());

        Network network2 = createBbNetwork();
        Line line2 = network2.getLine("NHV1_NHV2_1");
        LineAdder adder2 = createLineAdder(line2, network2);
        ReporterModel reporter2 = new ReporterModel("reportTestBbsNotExistingBB", "Testing reporter if busbar section does not exist in bus/breaker");
        NetworkModification modification2 = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId("NOT_EXISTING")
                .withLine(line2)
                .withLineAdder(adder2)
                .build();
        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> modification2.apply(network2, true, reporter2));
        assertEquals("Bus or busbar section NOT_EXISTING not found", exception2.getMessage());
        assertEquals("notFoundBusOrBusbarSection", reporter2.getReports().iterator().next().getReportKey());

        ReporterModel reporter3 = new ReporterModel("reportTestWrongTypeBbs", "Testing reporter if type of busbar section is wrong");
        NetworkModification modification3 = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId("LOAD")
                .withLine(line2)
                .withLineAdder(adder2)
                .build();
        PowsyblException exception3 = assertThrows(PowsyblException.class, () -> modification3.apply(network2, true, reporter3));
        assertEquals("Unexpected type of identifiable LOAD: LOAD", exception3.getMessage());
        assertEquals("unexpectedIdentifiableType", reporter3.getReports().iterator().next().getReportKey());

        ReporterModel reporter4 = new ReporterModel("reportTestNullFictitiousSubstationID", "Testing reporter with null fictitious substation ID");
        NetworkModification modification4 = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network1.getLine("CJ"))
                .withLineAdder(createLineAdder(network1.getLine("CJ"), network1))
                .withCreateFictitiousSubstation(true)
                .withFictitiousSubstationId(null)
                .build();
        PowsyblException exception4 = assertThrows(PowsyblException.class, () -> modification4.apply(network1, true, reporter4));
        assertEquals("Fictitious substation ID must be defined if a fictitious substation is to be created", exception4.getMessage());
        assertEquals("undefinedFictitiousSubstationId", reporter4.getReports().iterator().next().getReportKey());

        ReporterModel reporter5 = new ReporterModel("reportTestUndefinedPositionPercent", "Testing reporter with undefined position percent");
        NetworkModification modification5 = new CreateLineOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network1.getLine("CJ"))
                .withLineAdder(createLineAdder(network1.getLine("CJ"), network1))
                .withCreateFictitiousSubstation(true)
                .withPositionPercent(Double.NaN)
                .build();
        PowsyblException exception5 = assertThrows(PowsyblException.class, () -> modification5.apply(network1, true, reporter5));
        assertEquals("Percent should not be undefined", exception5.getMessage());
        assertEquals("undefinedPercent", reporter5.getReports().iterator().next().getReportKey());
    }

    @Test
    void testWithReporter() {
        Network network = createNbNetworkWithBusbarSection();
        ReporterModel reporter = new ReporterModel("reportTestCreateLineOnLine", "Testing reporter for creation of a line on line");
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network, reporter);
        testReporter(reporter, "/reporter/create-line-on-line-report.txt");
    }

    private static LineAdder createLineAdder(Line line, Network network) {
        return network.newLine()
                .setId("testLine")
                .setR(line.getR())
                .setX(line.getX())
                .setB1(line.getB1())
                .setG1(line.getG1())
                .setB2(line.getB2())
                .setG2(line.getG2());
    }
}
