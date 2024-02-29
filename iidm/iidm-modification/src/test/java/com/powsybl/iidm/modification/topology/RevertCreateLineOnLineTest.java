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
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class RevertCreateLineOnLineTest extends AbstractModificationTest {

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

        ReportNode reporter1 = new ReportRootImpl().newReportNode().withMessageTemplate("reportTestUndefinedLine1", "Testing reporter with undefined line1").add();
        final NetworkModification modificationWithError1 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode1 = reporter1.newReportNode().withMessageTemplate("withThrowException", "throwException = true").add();
        ReportNodeImpl subReporter1a = (ReportNodeImpl) reporter1.getChildren().iterator().next();
        assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, subReportNode1), "Line line1NotFound is not found");
        assertEquals("lineNotFound", subReporter1a.getChildren().iterator().next().getKey());
        final NetworkModification modificationWithError11 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        modificationWithError11.apply(network, false, reporter1.newReportNode().withMessageTemplate("withoutThrowException", "throwException = false").add());
        assertNull(network.getLine("CJ"));
        Iterator<ReportNode> it1b = reporter1.getChildren().iterator();
        it1b.next();
        ReportNodeImpl subReporter1b = (ReportNodeImpl) it1b.next();
        assertEquals("lineNotFound", subReporter1b.getChildren().iterator().next().getKey());

        ReportNode reporter2 = new ReportRootImpl().newReportNode().withMessageTemplate("reportTestUndefinedLine2", "Testing reporter with undefined line2").add();
        final NetworkModification modificationWithError2 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode2 = reporter2.newReportNode().withMessageTemplate("withThrowException", "throwException = true").add();
        assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, subReportNode2), "Line line2NotFound is not found");
        ReportNodeImpl subReporter2a = (ReportNodeImpl) reporter2.getChildren().iterator().next();
        assertEquals("lineNotFound", subReporter2a.getChildren().iterator().next().getKey());
        final NetworkModification modificationWithError21 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        modificationWithError21.apply(network, false, reporter2.newReportNode().withMessageTemplate("withoutThrowException", "throwException = false").add());
        assertNull(network.getLine("CJ"));
        Iterator<ReportNode> it2b = reporter2.getChildren().iterator();
        it2b.next();
        ReportNodeImpl subReporter2b = (ReportNodeImpl) it2b.next();
        assertEquals("lineNotFound", subReporter2b.getChildren().iterator().next().getKey());

        ReportNode reporter3 = new ReportRootImpl().newReportNode().withMessageTemplate("reportTestUndefinedLineToBeDeleted", "Testing reporter with undefined line to be deleted").add();
        final NetworkModification modificationWithError3 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode3 = reporter3.newReportNode().withMessageTemplate("withThrowException", "throwException = true").add();
        assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, subReportNode3), "Line line3NotFound is not found");
        ReportNodeImpl subReporter3a = (ReportNodeImpl) reporter3.getChildren().iterator().next();
        assertEquals("lineNotFound", subReporter3a.getChildren().iterator().next().getKey());
        final NetworkModification modificationWithError31 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        modificationWithError31.apply(network, false, reporter3.newReportNode().withMessageTemplate("withoutThrowException", "throwException = false").add());
        assertNull(network.getLine("CJ"));
        Iterator<ReportNode> it3b = reporter3.getChildren().iterator();
        it3b.next();
        ReportNodeImpl subReporter3b = (ReportNodeImpl) it3b.next();
        assertEquals("lineNotFound", subReporter3b.getChildren().iterator().next().getKey());

        ReportNode reporter4 = new ReportRootImpl().newReportNode().withMessageTemplate("reportTestNoTeePointAndOrTappedVoltageLevel", "Testing reporter without tee point").add();
        final NetworkModification modificationWithError4 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode4 = reporter4.newReportNode().withMessageTemplate("withThrowException", "throwException = true").add();
        assertThrows(PowsyblException.class, () -> modificationWithError4.apply(network, true, subReportNode4), "Unable to find the attachment point and the tapped voltage level from lines CJ_1, CJ_2 and LINE34");
        ReportNodeImpl subReporter4a = (ReportNodeImpl) reporter4.getChildren().iterator().next();
        assertEquals("noTeePointAndOrTappedVoltageLevel", subReporter4a.getChildren().iterator().next().getKey());
        final NetworkModification modificationWithError41 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        modificationWithError41.apply(network, false, reporter4.newReportNode().withMessageTemplate("withoutThrowException", "throwException = false").add());
        assertNull(network.getLine("CJ"));
        Iterator<ReportNode> it4b = reporter4.getChildren().iterator();
        it4b.next();
        ReportNodeImpl subReporter4b = (ReportNodeImpl) it4b.next();
        assertEquals("noTeePointAndOrTappedVoltageLevel", subReporter4b.getChildren().iterator().next().getKey());

        ReportNode reporter = new ReportRootImpl().newReportNode().withMessageTemplate("reporterTestRevertCreateLineOnLine", "Testing reporter for reverting create line on line in node/breaker network").add();
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("CJ_NEW")
                .build();
        modification.apply(network, true, reporter);
        writeXmlTest(network, "/fictitious-revert-create-line-on-line-l.xml");
        testReporter(reporter, "/reporter/revert-create-line-on-line-nb-report.txt");
    }

    @Test
    void revertCreateLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        ReportNode reporter = new ReportRootImpl().newReportNode().withMessageTemplate("reporterTestRevertCreateLineOnLineNBBB", "Testing reporter for reverting create line on line with mixed topology network").add();
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_2")
                .withLineToBeMerged2Id("NHV1_NHV2_1_1")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reporter);
        writeXmlTest(network, "/eurostag-revert-create-line-on-line-nb-l.xml");
        testReporter(reporter, "/reporter/revert-create-line-on-line-nb-bb-report.txt");
    }

    @Test
    void revertCreateLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        ReportNode reporter = new ReportRootImpl().newReportNode().withMessageTemplate("reporterTestRevertCreateLineOnLineNBBB", "Testing reporter for reverting create line on line in bus/breaker network").add();
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_1")
                .withLineToBeMerged2Id("NHV1_NHV2_1_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reporter);
        writeXmlTest(network, "/eurostag-revert-create-line-on-line-bb-l.xml");
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
