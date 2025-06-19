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
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, null, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("All switch kinds must be defined", e.getMessage());
        assertEquals("core.iidm.modification.undefinedSwitchKind", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithUnsupportedSwitchKind() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.LOAD_BREAK_SWITCH, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Switch kinds must be DISCONNECTOR or BREAKER", e.getMessage());
        assertEquals("core.iidm.modification.wrongSwitchKind", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNegativeCount() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        NetworkModification modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(-1)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("busbar count must be >= 1", e.getMessage());
        assertEquals("core.iidm.modification.countLowerThanMin", reportNode.getChildren().get(0).getMessageKey());

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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Unexpected switch kinds count (1). Should be 3", e.getMessage());
        assertEquals("core.iidm.modification.unexpectedSwitchKindsCount", reportNode.getChildren().get(0).getMessageKey());

        // Check nothing is created if throwException is false
        modification.apply(network);
        assertEquals(0, network.getVoltageLevel(VLTEST).getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithNotExistingVoltageLevel() {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("NOT_EXISTING")
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Voltage level NOT_EXISTING is not found", e.getMessage());
        assertEquals("core.iidm.modification.voltageLevelNotFound", reportNode.getChildren().get(0).getMessageKey());
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTest")
                .build();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
                .withSectionCount(4)
                .build();
        assertDoesNotThrow(() -> modification.apply(network, false, ReportNode.NO_OP));
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, reportNode));
        assertEquals("Unexpected switch kinds count (0). Should be 3", e.getMessage());
        assertEquals("core.iidm.modification.unexpectedSwitchKindsCount", reportNode.getChildren().get(0).getMessageKey());
    }

    @Test
    void testWithReportNode() throws IOException {
        Network network = createNbNetwork();
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestCreateVoltageLevelTopology")
                .build();
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
    void testGetName() {
        AbstractNetworkModification networkModification = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId(VLTEST)
            .withAlignedBusesOrBusbarCount(3)
            .withSectionCount(4)
            .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
            .build();
        assertEquals("CreateVoltageLevelTopology", networkModification.getName());
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

    @Test
    void testWithAlreadyExistingTopology() {
        Network network = createNbNetwork();
        // Add busbar section position extension to the busbar section of voltage level C
        network.getBusbarSection("D").newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Add a busbar section with one section "below" the busbar section D with default naming strategy
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("C")
            .withAlignedBusesOrBusbarCount(1)
            .withSectionCount(1)
            .withLowSectionIndex(1)
            .withSwitchKinds()
            .withLowBusOrBusbarIndex(2)
            .build();
        modification.apply(network);

        BusbarSection bbs = network.getBusbarSection("C_2_1");
        assertNotNull(bbs);
        BusbarSectionPosition bbsPosition = bbs.getExtension(BusbarSectionPosition.class);
        assertEquals(2, bbsPosition.getBusbarIndex());
        assertEquals(1, bbsPosition.getSectionIndex());
        assertEquals(2, network.getVoltageLevel("C").getNodeBreakerView().getBusbarSectionCount());
    }

    @Test
    void testWithAlreadyExistingTopologyAndWrongPosition() {
        Network network = createNbNetwork();
        // Add busbar section position extension to the busbar section of voltage level C
        network.getBusbarSection("D").newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // Add a busbar section with one section with the same indexes as the the busbar section D with default naming strategy
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
            .withVoltageLevelId("C")
            .withAlignedBusesOrBusbarCount(1)
            .withSectionCount(1)
            .withLowSectionIndex(1)
            .withLowBusOrBusbarIndex(1)
            .build();
        assertThrows(PowsyblException.class, () -> modification.apply(network, true, ReportNode.NO_OP));

        // Check that nothing is added if throwException is false
        modification.apply(network, false, ReportNode.NO_OP);

        BusbarSection bbs = network.getBusbarSection("C_2_1");
        assertNull(bbs);
        assertEquals(1, network.getVoltageLevel("C").getNodeBreakerView().getBusbarSectionCount());
    }

}
