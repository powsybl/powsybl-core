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
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class RevertCreateLineOnLineTest extends AbstractSerDeTest {

    @Test
    void revertCreateLineOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        Line line = network.getLine("CJ");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        ReporterModel reporter1 = new ReporterModel("reportTestUndefinedLine1", "Testing reporter with undefined line1");
        final NetworkModification modificationWithError1 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        Reporter subReporter1 = reporter1.createSubReporter("withThrowException", "throwException = true");
        assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, subReporter1), "Line line1NotFound is not found");
        assertEquals("lineNotFound", reporter1.getSubReporters().get(0).getReports().iterator().next().getReportKey());
        final NetworkModification modificationWithError11 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        modificationWithError11.apply(network, false, reporter1.createSubReporter("withoutThrowException", "throwException = false"));
        assertNull(network.getLine("CJ"));
        assertEquals("lineNotFound", reporter1.getSubReporters().get(1).getReports().iterator().next().getReportKey());

        ReporterModel reporter2 = new ReporterModel("reportTestUndefinedLine2", "Testing reporter with undefined line2");
        final NetworkModification modificationWithError2 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        Reporter subReporter2 = reporter2.createSubReporter("withThrowException", "throwException = true");
        assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, subReporter2), "Line line2NotFound is not found");
        assertEquals("lineNotFound", reporter2.getSubReporters().get(0).getReports().iterator().next().getReportKey());
        final NetworkModification modificationWithError21 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        modificationWithError21.apply(network, false, reporter2.createSubReporter("withoutThrowException", "throwException = false"));
        assertNull(network.getLine("CJ"));
        assertEquals("lineNotFound", reporter2.getSubReporters().get(1).getReports().iterator().next().getReportKey());

        ReporterModel reporter3 = new ReporterModel("reportTestUndefinedLineToBeDeleted", "Testing reporter with undefined line to be deleted");
        final NetworkModification modificationWithError3 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        Reporter subReporter3 = reporter3.createSubReporter("withThrowException", "throwException = true");
        assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, subReporter3), "Line line3NotFound is not found");
        assertEquals("lineNotFound", reporter3.getSubReporters().get(0).getReports().iterator().next().getReportKey());
        final NetworkModification modificationWithError31 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        modificationWithError31.apply(network, false, reporter3.createSubReporter("withoutThrowException", "throwException = false"));
        assertNull(network.getLine("CJ"));
        assertEquals("lineNotFound", reporter3.getSubReporters().get(1).getReports().iterator().next().getReportKey());

        ReporterModel reporter4 = new ReporterModel("reportTestNoTeePointAndOrTappedVoltageLevel", "Testing reporter without tee point");
        final NetworkModification modificationWithError4 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        Reporter subReporter4 = reporter4.createSubReporter("withThrowException", "throwException = true");
        assertThrows(PowsyblException.class, () -> modificationWithError4.apply(network, true, subReporter4), "Unable to find the attachment point and the tapped voltage level from lines CJ_1, CJ_2 and LINE34");
        assertEquals("noTeePointAndOrTappedVoltageLevel", reporter4.getSubReporters().get(0).getReports().iterator().next().getReportKey());
        final NetworkModification modificationWithError41 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        modificationWithError41.apply(network, false, reporter4.createSubReporter("withoutThrowException", "throwException = false"));
        assertNull(network.getLine("CJ"));
        assertEquals("noTeePointAndOrTappedVoltageLevel", reporter4.getSubReporters().get(1).getReports().iterator().next().getReportKey());

        ReporterModel reporter = new ReporterModel("reporterTestRevertCreateLineOnLine", "Testing reporter for reverting create line on line in node/breaker network");
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("CJ_NEW")
                .build();
        modification.apply(network, true, reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/fictitious-revert-create-line-on-line-l.xml");
        testReporter(reporter, "/reporter/revert-create-line-on-line-nb-report.txt");
    }

    @Test
    void revertCreateLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        ReporterModel reporter = new ReporterModel("reporterTestRevertCreateLineOnLineNBBB", "Testing reporter for reverting create line on line with mixed topology network");
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_2")
                .withLineToBeMerged2Id("NHV1_NHV2_1_1")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-revert-create-line-on-line-nb-l.xml");
        testReporter(reporter, "/reporter/revert-create-line-on-line-nb-bb-report.txt");
    }

    @Test
    void revertCreateLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        ReporterModel reporter = new ReporterModel("reporterTestRevertCreateLineOnLineNBBB", "Testing reporter for reverting create line on line in bus/breaker network");
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_1")
                .withLineToBeMerged2Id("NHV1_NHV2_1_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reporter);
        roundTripXmlTest(network, NetworkSerDe::writeAndValidate, NetworkSerDe::validateAndRead,
                "/eurostag-revert-create-line-on-line-bb-l.xml");
        testReporter(reporter, "/reporter/revert-create-line-on-line-bb-report.txt");
    }

    @Test
    void testConstructor() {
        RevertCreateLineOnLine modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1")
                .withLineToBeMerged2Id("NHV1_NHV2_2")
                .withLineToBeDeletedId("NHV1_NHV2_3")
                .withMergedLineId("NEW LINE ID")
                .build();
        assertEquals("NHV1_NHV2_1", modification.getLineToBeMerged1Id());
        assertEquals("NHV1_NHV2_2", modification.getLineToBeMerged2Id());
        assertEquals("NHV1_NHV2_3", modification.getLineToBeDeletedId());
        assertEquals("NEW LINE ID", modification.getMergedLineId());
        assertNull(modification.getMergedLineName());

        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1")
                .withLineToBeMerged2Id("NHV1_NHV2_2")
                .withLineToBeDeletedId("NHV1_NHV2_3")
                .withMergedLineId("NEW LINE ID")
                .withMergedLineName("NEW LINE NAME")
                .build();
        assertEquals("NEW LINE NAME", modification.getMergedLineName());
    }

    @Test
    void testSetters() {
        RevertCreateLineOnLine modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1")
                .withLineToBeMerged2Id("NHV1_NHV2_2")
                .withLineToBeDeletedId("NHV1_NHV2_3")
                .withMergedLineId("NEW LINE ID")
                .build();
        modification.setLineToBeMerged1Id("NHV1_NHV2_1 _A")
                .setLineToBeMerged2Id("NHV1_NHV2_2 _B")
                .setLineToBeDeletedId("NHV1_NHV2_3 _C")
                .setMergedLineId("NEW LINE ID_C")
                .setMergedLineName("NEW LINE NAME");
        assertEquals("NHV1_NHV2_1 _A", modification.getLineToBeMerged1Id());
        assertEquals("NHV1_NHV2_2 _B", modification.getLineToBeMerged2Id());
        assertEquals("NHV1_NHV2_3 _C", modification.getLineToBeDeletedId());
        assertEquals("NEW LINE ID_C", modification.getMergedLineId());
        assertEquals("NEW LINE NAME", modification.getMergedLineName());
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
