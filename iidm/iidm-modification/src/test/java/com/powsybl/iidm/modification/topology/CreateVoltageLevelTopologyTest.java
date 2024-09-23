/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VLTEST;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class CreateVoltageLevelTopologyTest extends AbstractModificationTest {

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
        writeXmlTest(network, "/create-vl-topo-test.xiidm");
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
        writeXmlTest(network, "/create-vl-topo-test-complete.xiidm");
    }

    @Test
    void testWithNullSwitchKind() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, null, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("All switch kinds must be defined", e.getMessage());
        assertEquals("undefinedSwitchKind", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithUnsupportedSwitchKind() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.LOAD_BREAK_SWITCH, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Switch kinds must be DISCONNECTOR or BREAKER", e.getMessage());
        assertEquals("wrongSwitchKind", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNegativeCount() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        NetworkModification modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(-1)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("busbar count must be >= 1", e.getMessage());
        assertEquals("countLowerThanMin", reportNode.getChildren().get(0).getMessageKey());

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
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Unexpected switch kinds count (1). Should be 3", e.getMessage());
        assertEquals("unexpectedSwitchKindsCount", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNotExistingVoltageLevel() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("NOT_EXISTING")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Voltage level NOT_EXISTING is not found", e.getMessage());
        assertEquals("voltageLevelNotFound", reportNode.getChildren().get(0).getMessageKey());
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
        writeXmlTest(network, "/eurostag-new-voltage-level.xml");
    }

    @Test
    void testErrorIfNotSwitchKindsDefinedAndNodeBreaker() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Unexpected switch kinds count (0). Should be 3", e.getMessage());
        assertEquals("unexpectedSwitchKindsCount", reportNode.getChildren().get(0).getMessageKey());
    }

    @Test
    void testWithReportNode() throws IOException {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTestCreateVoltageLevelTopology", "Testing reportNode for voltage level topology creation").build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network, LocalComputationManager.getDefault(), reportNode);
        testReportNode(reportNode, "/reportNode/create-voltage-level-topology-report.txt");
    }

    @Test
    void testHasImpact() {
        Network network = createNbNetwork();
        NetworkModification modification1 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId(VLTEST)
            .withAlignedBusesOrBusbarCount(-1)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId(VLTEST)
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        NetworkModification modification3 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("WRONG_VL")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification3.hasImpactOnNetwork(network));

        NetworkModification modification4 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification4.hasImpactOnNetwork(network));

        NetworkModification modification5 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, null)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification5.hasImpactOnNetwork(network));

        NetworkModification modification6 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.LOAD_BREAK_SWITCH)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification6.hasImpactOnNetwork(network));

        network.newVoltageLevel()
            .setId("VLTEST_BUS")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        CreateVoltageLevelTopology modification7 = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("VLTEST_BUS")
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification7.hasImpactOnNetwork(network));
    }

}
