/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class RevertConnectVoltageLevelOnLineTest extends AbstractModificationTest {

    @Test
    void revertConnectVoltageLevelOnLineNbTest() throws IOException {
        Network network = createNbNetworkWithBusbarSection();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("CJ"))
                .build();

        modification.apply(network);

        VoltageLevel vl = network.newVoltageLevel().setId("VL3").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs3").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker3").setName("breaker3").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        vl = network.newVoltageLevel().setId("VL4").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs4").setNode(0).add();
        vl.getNodeBreakerView().newSwitch().setId("breaker4").setName("breaker4").setKind(SwitchKind.BREAKER).setRetained(false).setOpen(true).setFictitious(false).setNode1(0).setNode2(1).add();
        network.newLine().setId("LINE34").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0).setNode1(1).setVoltageLevel1("VL3").setNode2(1).setVoltageLevel2("VL4").add();

        ReportNode reportNode1 = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestUndefinedLine")
                .build();
        final NetworkModification modificationWithError1 = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("line1NotFound")
                .withLine2Id("CJ_2")
                .withLineId("CJ")
                .build();
        assertDoesNotThrow(() -> modificationWithError1.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError1.apply(network, true, reportNode1), "Line line1NotFound is not found");
        assertEquals("core.iidm.modification.lineNotFound", reportNode1.getChildren().get(0).getMessageKey());

        ReportNode reportNode2 = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestUndefinedLine")
                .build();
        final NetworkModification modificationWithError2 = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("CJ_1")
                .withLine2Id("line2NotFound")
                .withLineId("CJ")
                .build();
        assertDoesNotThrow(() -> modificationWithError2.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError2.apply(network, true, reportNode2), "Line line2NotFound is not found");
        assertEquals("core.iidm.modification.lineNotFound", reportNode2.getChildren().get(0).getMessageKey());

        ReportNode reportNode3 = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestNoVLInCommon")
                .build();
        final NetworkModification modificationWithError3 = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("CJ_1")
                .withLine2Id("LINE34")
                .withLineId("CJ")
                .build();
        assertDoesNotThrow(() -> modificationWithError3.apply(network, false, ReportNode.NO_OP));
        assertThrows(PowsyblException.class, () -> modificationWithError3.apply(network, true, reportNode3), "Lines CJ_1 and LINE34 should have one and only one voltage level in common at their extremities");
        assertEquals("core.iidm.modification.noVoltageLevelInCommon", reportNode3.getChildren().get(0).getMessageKey());

        // create limits on tee point side
        Line line1 = network.getLine("CJ_1");
        Line line2 = network.getLine("CJ_2");
        line1.newActivePowerLimits2().setPermanentLimit(100.).beginTemporaryLimit().setName("limit1").setValue(500).setAcceptableDuration(1200).endTemporaryLimit().add();
        line1.newApparentPowerLimits2().setPermanentLimit(200.).add();
        line1.newCurrentLimits2().setPermanentLimit(100.).beginTemporaryLimit().setName("limit3").setValue(900).setAcceptableDuration(60).endTemporaryLimit().add();

        line2.newActivePowerLimits1().setPermanentLimit(600.).beginTemporaryLimit().setName("limit4").setValue(1000).setAcceptableDuration(300).endTemporaryLimit().add();
        line2.newApparentPowerLimits1().setPermanentLimit(800.).add();
        line2.newCurrentLimits1().setPermanentLimit(900.).beginTemporaryLimit().setName("limit6").setValue(400).setAcceptableDuration(1200).endTemporaryLimit().add();

        ReportNode reportNode = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestRevertConnectVoltageLevelOnLine")
                .build();
        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("CJ_1")
                .withLine2Id("CJ_2")
                .withLineId("CJ")
                .build();
        modification.apply(network, true, reportNode);
        writeXmlTest(network, "/fictitious-revert-connect-voltage-level-on-line-vl.xml");
        testReportNode(reportNode, "/reportNode/revert-connect-voltage-level-on-line-nb-report.txt");
    }

    @Test
    void revertConnectVoltageLevelOnLineNbBbTest() throws IOException {
        Network network = createNbBbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId(BBS)
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();

        modification.apply(network);

        ReportNode reportNode = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestRevertConnectVoltageLevelOnLineNbBb")
                .build();
        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("NHV1_NHV2_1_1")
                .withLine2Id("NHV1_NHV2_1_2")
                .withLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reportNode);
        writeXmlTest(network, "/eurostag-revert-connect-voltage-level-on-line-nb-vl.xml");
        testReportNode(reportNode, "/reportNode/revert-connect-voltage-level-on-line-bb-nb-report.txt");
    }

    @Test
    void revertConnectVoltageLevelOnLineBbTest() throws IOException {
        Network network = createBbNetwork();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
                .withBusbarSectionOrBusId("bus")
                .withLine(network.getLine("NHV1_NHV2_1"))
                .build();

        modification.apply(network);

        ReportNode reportNode = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestRevertConnectVoltageLevelOnLineBb")
                .build();
        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id("NHV1_NHV2_1_1")
                .withLine2Id("NHV1_NHV2_1_2")
                .withLineId("NHV1_NHV2_1")
                .build();
        modification.apply(network, true, reportNode);
        writeXmlTest(network, "/eurostag-revert-connect-voltage-level-on-line-bb-vl.xml");
        testReportNode(reportNode, "/reportNode/revert-connect-voltage-level-on-line-bb-report.txt");
    }

    @Test
    void testConstructor() {
        Network network = createNbBbNetwork();
        Line line1 = network.getLine("NHV1_NHV2_1");
        Line line2 = network.getLine("NHV1_NHV2_2");
        RevertConnectVoltageLevelOnLine modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id(line1.getId())
                .withLine2Id(line2.getId())
                .withLineId("NEW LINE ID")
                .build();
        assertEquals("NHV1_NHV2_1", modification.getLine1Id());
        assertEquals("NHV1_NHV2_2", modification.getLine2Id());
        assertEquals("NEW LINE ID", modification.getLineId());
        assertNull(modification.getLineName());

        modification = new RevertConnectVoltageLevelOnLineBuilder()
                .withLine1Id(line1.getId())
                .withLine2Id(line2.getId())
                .withLineId("NEW LINE ID")
                .withLineName("NEW LINE NAME")
                .build();
        assertEquals("NEW LINE NAME", modification.getLineName());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new RevertConnectVoltageLevelOnLineBuilder()
            .withLine1Id("NHV1_NHV2_1_1")
            .withLine2Id("NHV1_NHV2_1_2")
            .withLineId("NHV1_NHV2_1")
            .build();
        assertEquals("RevertConnectVoltageLevelOnLine", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        Network network = createNbBbNetwork();
        network.newLine()
            .setId("LINE")
            .setVoltageLevel1("VLLOAD")
            .setBus1("NLOAD")
            .setConnectableBus1("NLOAD")
            .setVoltageLevel2("VLHV2")
            .setBus2("NHV2")
            .setConnectableBus2("NHV2")
            .setR(3.0)
            .setX(33.0)
            .setG1(0.0)
            .setB1(386E-6 / 2)
            .setG2(0.0)
            .setB2(386E-6 / 2)
            .add();
        NetworkModification modification = new ConnectVoltageLevelOnLineBuilder()
            .withBusbarSectionOrBusId(BBS)
            .withLine(network.getLine("NHV1_NHV2_1"))
            .build();
        modification.apply(network);

        NetworkModification modification1 = new RevertConnectVoltageLevelOnLineBuilder()
            .withLine1Id("WRONG_ID")
            .withLine2Id("NHV1_NHV2_1_2")
            .withLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new RevertConnectVoltageLevelOnLineBuilder()
            .withLine1Id("NHV1_NHV2_1_1")
            .withLine2Id("WRONG_ID")
            .withLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new RevertConnectVoltageLevelOnLineBuilder()
            .withLine1Id("NHV1_NHV2_1_1")
            .withLine2Id("NHV1_NHV2_1_2")
            .withLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        NetworkModification modification4 = new RevertConnectVoltageLevelOnLineBuilder()
            .withLine1Id("LINE")
            .withLine2Id("NHV1_NHV2_1_1")
            .withLineId("NHV1_NHV2_1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification4.hasImpactOnNetwork(network));
    }
}
