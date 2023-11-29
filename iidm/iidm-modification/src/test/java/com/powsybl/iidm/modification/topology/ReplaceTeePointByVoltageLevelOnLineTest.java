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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class ReplaceTeePointByVoltageLevelOnLineTest extends AbstractSerDeTest {

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

    @Test
    void replaceTeePointByVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        // create additional line to test bad configuration
        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        ReporterModel reporter1 = new ReporterModel("reportTestLineNotFound", "Testing reporter with wrong line1 id");
        NetworkModification modificationWithError1 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("line1NotFound")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, reporter1)).getMessage().contains("Line line1NotFound is not found"));
        assertEquals("lineNotFound", reporter1.getReports().iterator().next().getReportKey());

        ReporterModel reporter2 = new ReporterModel("reportTestLineNotFound2", "Testing reporter with wrong line2 id");
        NetworkModification modificationWithError2 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("line2NotFound")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, reporter2)).getMessage().contains("Line line2NotFound is not found"));
        assertEquals("lineNotFound", reporter2.getReports().iterator().next().getReportKey());

        ReporterModel reporter3 = new ReporterModel("reportTestLineNotFound", "Testing reporter with wrong tee point line");
        NetworkModification modificationWithError3 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("line3NotFound")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, reporter3)).getMessage().contains("Line line3NotFound is not found"));
        assertEquals("lineNotFound", reporter3.getReports().iterator().next().getReportKey());

        ReporterModel reporter4 = new ReporterModel("reportTestBbsNotFound", "Testing reporter with wrong bbs");
        NetworkModification modificationWithError4 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("notFoundBusbarSection")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError4.apply(network, true, reporter4)).getMessage().contains("Busbar section notFoundBusbarSection is not found in voltage level VLTEST"));
        assertEquals("busbarSectionNotFound", reporter4.getReports().iterator().next().getReportKey());

        ReporterModel reporter5 = new ReporterModel("reportTestWrongTeePoint", "Testing reporter with wrong tee point");
        NetworkModification modificationWithError5 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("LINE34")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError5.apply(network, true, reporter5)).getMessage().contains("Unable to find the tee point and the tapped voltage level from lines CJ_1, CJ_2 and LINE34"));
        assertEquals("noTeePointAndOrTappedVoltageLevel", reporter5.getReports().iterator().next().getReportKey());

        ReporterModel reporter6 = new ReporterModel("reportTestBbsInWrongVL", "Testing reporter with busbar section in wrong voltage level");
        NetworkModification modificationWithError6 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("bbs3")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError6.apply(network, true, reporter6)).getMessage().contains("Busbar section bbs3 is not found in voltage level VLTEST"));
        assertEquals("busbarSectionNotFound", reporter6.getReports().iterator().next().getReportKey());

        ReporterModel reporter = new ReporterModel("reportTestReplaceTeePointByVoltageLevelOnLineNB", "Testing reporter when replacing tee point by voltage level on line in Node/breaker network");
        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("CJ_1")
                .withTeePointLine2("CJ_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        modification.apply(network, true, reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-replace-tee-point-by-voltage-level-on-line-nb.xml");
        testReporter(reporter, "/reporter/replace-tee-point-by-vl-on-line-nb-report.txt");
    }

    @Test
    void replaceTeePointByVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        BusbarSection bbs = network.getBusbarSection("bbs");
        bbs.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        ReporterModel reporter = new ReporterModel("reportTestReplaceTeePointByVoltageLevelOnLineNBBb", "Testing reporter when replacing tee point by voltage level on line");
        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId(BBS)
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        modification.apply(network, true, reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-replace-tee-point-by-voltage-level-on-line-nbbb.xml");
        testReporter(reporter, "/reporter/replace-tee-point-by-vl-on-line-nb-bb-report.txt");
    }

    @Test
    void replaceTeePointByVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl.getBusBreakerView().newBus().setId("bus3").add();

        ReporterModel reporter1 = new ReporterModel("reportTestUndefinedBus", "Testing reporter with undefined bus");
        NetworkModification modificationWithError1 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("busNotFound")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, reporter1)).getMessage().contains("Bus busNotFound is not found in voltage level " + VOLTAGE_LEVEL_ID));
        assertEquals("busNotFound", reporter1.getReports().iterator().next().getReportKey());

        ReporterModel reporter2 = new ReporterModel("reportTestBusInWrongVl", "Testing reporter with bus in wrong voltage level");
        NetworkModification modificationWithError2 = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("bus3")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        assertTrue(assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, reporter2)).getMessage().contains("Bus bus3 is not found in voltage level " + VOLTAGE_LEVEL_ID));
        assertEquals("busNotFound", reporter2.getReports().iterator().next().getReportKey());

        ReporterModel reporter3 = new ReporterModel("reportTestReplaceTeePointByVoltageLevelOnLineBb", "Testing reporter when replacing tee point by voltage level on line in bus/breaker network");
        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1_1")
                .withTeePointLine2("NHV1_NHV2_1_2")
                .withTeePointLineToRemove("testLine")
                .withBbsOrBusId("bus")
                .withNewLine1Id("NEW LINE1")
                .withNewLine2Id("NEW LINE2").build();
        modification.apply(network, true, reporter3);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-replace-tee-point-by-voltage-level-on-line-bb.xml");
        testReporter(reporter3, "/reporter/replace-tee-point-by-vl-on-line-bb-report.txt");
    }

    @Test
    void testConstructor() {
        ReplaceTeePointByVoltageLevelOnLine modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1")
                .withTeePointLine2("NHV1_NHV2_2")
                .withTeePointLineToRemove("NHV1_NHV2_3")
                .withBbsOrBusId("BBS")
                .withNewLine1Id("NEW LINE1 ID")
                .withNewLine2Id("NEW LINE2 ID").build();
        assertEquals("NHV1_NHV2_1", modification.getTeePointLine1Id());
        assertEquals("NHV1_NHV2_2", modification.getTeePointLine2Id());
        assertEquals("NHV1_NHV2_3", modification.getTeePointLineToRemoveId());
        assertEquals("BBS", modification.getBbsOrBusId());
        assertEquals("NEW LINE1 ID", modification.getNewLine1Id());
        assertNull(modification.getNewLine1Name());
        assertEquals("NEW LINE2 ID", modification.getNewLine2Id());
        assertNull(modification.getNewLine2Name());

        modification = new ReplaceTeePointByVoltageLevelOnLineBuilder()
                .withTeePointLine1("NHV1_NHV2_1")
                .withTeePointLine2("NHV1_NHV2_2")
                .withTeePointLineToRemove("NHV1_NHV2_3")
                .withBbsOrBusId("BBS")
                .withNewLine1Id("NEW LINE1 ID")
                .withNewLine1Name("NEW LINE1 NAME")
                .withNewLine2Id("NEW LINE2 ID")
                .withNewLine2Name("NEW LINE2 NAME").build();
        assertEquals("NEW LINE1 NAME", modification.getNewLine1Name());
        assertEquals("NEW LINE2 NAME", modification.getNewLine2Name());
    }
}
