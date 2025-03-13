/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundles;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

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

        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportTestUndefinedLine1")
                .build();
        final NetworkModification modificationWithError1 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode1 = reportNode1.newReportNode()
                .withMessageTemplate("withThrowException")
                .add();
        ReportNode reportNodeChild1a = reportNode1.getChildren().get(0);
        assertDoesNotThrow(() -> modificationWithError1.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, subReportNode1), "Line line1NotFound is not found");
        assertEquals("core.iidm.modification.lineNotFound", reportNodeChild1a.getChildren().get(0).getMessageKey());
        final NetworkModification modificationWithError11 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("line1NotFound")
                .withLineToBeMerged2Id("CJ_1")
                .withLineToBeDeletedId("CJ_2")
                .withMergedLineId("CJ")
                .build();
        modificationWithError11.apply(network, false, reportNode1.newReportNode()
                .withMessageTemplate("withoutThrowException")
                .add());
        assertNull(network.getLine("CJ"));
        ReportNode reportNodeChild1b = reportNode1.getChildren().get(1);
        assertEquals("core.iidm.modification.lineNotFound", reportNodeChild1b.getChildren().get(0).getMessageKey());

        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportTestUndefinedLine2")
                .build();
        final NetworkModification modificationWithError2 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode2 = reportNode2.newReportNode()
                .withMessageTemplate("withThrowException")
                .add();
        assertDoesNotThrow(() -> modificationWithError2.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, subReportNode2), "Line line2NotFound is not found");
        ReportNode reportNodeChild2a = reportNode2.getChildren().get(0);
        assertEquals("core.iidm.modification.lineNotFound", reportNodeChild2a.getChildren().get(0).getMessageKey());
        final NetworkModification modificationWithError21 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("line2NotFound")
                .withLineToBeDeletedId("CJ_3")
                .withMergedLineId("CJ")
                .build();
        modificationWithError21.apply(network, false, reportNode2.newReportNode()
                .withMessageTemplate("withoutThrowException")
                .add());
        assertNull(network.getLine("CJ"));
        ReportNode reportNodeChild2b = reportNode2.getChildren().get(1);
        assertEquals("core.iidm.modification.lineNotFound", reportNodeChild2b.getChildren().get(0).getMessageKey());

        ReportNode reportNode3 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportTestUndefinedLineToBeDeleted")
                .build();
        final NetworkModification modificationWithError3 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode3 = reportNode3.newReportNode()
                .withMessageTemplate("withThrowException")
                .add();
        assertDoesNotThrow(() -> modificationWithError3.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, subReportNode3), "Line line3NotFound is not found");
        ReportNode reportNodeChild3a = reportNode3.getChildren().get(0);
        assertEquals("core.iidm.modification.lineNotFound", reportNodeChild3a.getChildren().get(0).getMessageKey());
        final NetworkModification modificationWithError31 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("line3NotFound")
                .withMergedLineId("CJ")
                .build();
        modificationWithError31.apply(network, false, reportNode3.newReportNode()
                .withMessageTemplate("withoutThrowException")
                .add());
        assertNull(network.getLine("CJ"));
        ReportNode reportNodeChild3b = reportNode3.getChildren().get(1);
        assertEquals("core.iidm.modification.lineNotFound", reportNodeChild3b.getChildren().get(0).getMessageKey());

        ReportNode reportNode4 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportTestNoTeePointAndOrTappedVoltageLevel")
                .build();
        final NetworkModification modificationWithError4 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        ReportNode subReportNode4 = reportNode4.newReportNode()
                .withMessageTemplate("withThrowException")
                .add();
        assertDoesNotThrow(() -> modificationWithError4.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError4.apply(network, true, subReportNode4), "Unable to find the attachment point and the tapped voltage level from lines CJ_1, CJ_2 and LINE34");
        ReportNode reportNodeChild4a = reportNode4.getChildren().get(0);
        assertEquals("core.iidm.modification.noTeePointAndOrTappedVoltageLevel", reportNodeChild4a.getChildren().get(0).getMessageKey());
        final NetworkModification modificationWithError41 = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("LINE34")
                .withMergedLineId("CJ")
                .build();
        modificationWithError41.apply(network, false, reportNode4.newReportNode()
                .withMessageTemplate("withoutThrowException")
                .add());
        assertNull(network.getLine("CJ"));
        ReportNode reportNodeChild4b = reportNode4.getChildren().get(1);
        assertEquals("core.iidm.modification.noTeePointAndOrTappedVoltageLevel", reportNodeChild4b.getChildren().get(0).getMessageKey());

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportNodeTestRevertCreateLineOnLine")
                .build();
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("CJ_1")
                .withLineToBeMerged2Id("CJ_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("CJ_NEW")
                .build();
        modification.apply(network, true, reportNode);
        writeXmlTest(network, "/fictitious-revert-create-line-on-line-l.xml");
        testReportNode(reportNode, "/reportNode/revert-create-line-on-line-nb-report.txt");
    }

    @Test
    void revertCreateLineOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportNodeTestRevertCreateLineOnLineNBBB")
                .build();
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_2")
                .withLineToBeMerged2Id("NHV1_NHV2_1_1")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reportNode);
        writeXmlTest(network, "/eurostag-revert-create-line-on-line-nb-l.xml");
        testReportNode(reportNode, "/reportNode/revert-create-line-on-line-nb-bb-report.txt");
    }

    @Test
    void revertCreateLineOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId("bus").withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("reportNodeTestRevertCreateLineOnLineBB")
                .build();
        modification = new RevertCreateLineOnLineBuilder()
                .withLineToBeMerged1Id("NHV1_NHV2_1_1")
                .withLineToBeMerged2Id("NHV1_NHV2_1_2")
                .withLineToBeDeletedId("testLine")
                .withMergedLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reportNode);
        writeXmlTest(network, "/eurostag-revert-create-line-on-line-bb-l.xml");
        testReportNode(reportNode, "/reportNode/revert-create-line-on-line-bb-report.txt");
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

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new RevertCreateLineOnLineBuilder()
            .withLineToBeMerged1Id("NHV1_NHV2_1")
            .withLineToBeMerged2Id("NHV1_NHV2_2")
            .withLineToBeDeletedId("NHV1_NHV2_3")
            .withMergedLineId("NEW LINE ID")
            .build();
        assertEquals("RevertCreateLineOnLine", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = createNbBbNetwork();
        Line line = network.getLine("NHV1_NHV2_1");
        LineAdder adder = createLineAdder(line, network);
        NetworkModification modification = new CreateLineOnLineBuilder().withBusbarSectionOrBusId(BBS).withLine(line).withLineAdder(adder).build();
        modification.apply(network);

        modification = new RevertCreateLineOnLineBuilder()
            .withLineToBeMerged1Id("NHV1_NHV2_1_2")
            .withLineToBeMerged2Id("NHV1_NHV2_1_1")
            .withLineToBeDeletedId("testLine")
            .withMergedLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        modification = new RevertCreateLineOnLineBuilder()
            .withLineToBeMerged1Id("WRONG_LINE")
            .withLineToBeMerged2Id("NHV1_NHV2_1_1")
            .withLineToBeDeletedId("testLine")
            .withMergedLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification.hasImpactOnNetwork(network));

        modification = new RevertCreateLineOnLineBuilder()
            .withLineToBeMerged1Id("NHV1_NHV2_1_2")
            .withLineToBeMerged2Id("WRONG_LINE")
            .withLineToBeDeletedId("testLine")
            .withMergedLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification.hasImpactOnNetwork(network));

        modification = new RevertCreateLineOnLineBuilder()
            .withLineToBeMerged1Id("NHV1_NHV2_1_2")
            .withLineToBeMerged2Id("NHV1_NHV2_1_1")
            .withLineToBeDeletedId("WRONG_LINE")
            .withMergedLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification.hasImpactOnNetwork(network));

        modification = new RevertCreateLineOnLineBuilder()
            .withLineToBeMerged1Id("NHV1_NHV2_1_2")
            .withLineToBeMerged2Id("NHV1_NHV2_1_1")
            .withLineToBeDeletedId("NHV1_NHV2_2")
            .withMergedLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification.hasImpactOnNetwork(network));
    }
}
